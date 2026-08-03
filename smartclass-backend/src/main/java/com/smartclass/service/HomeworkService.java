package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartclass.common.BusinessException;
import com.smartclass.common.Constants;
import com.smartclass.dto.HomeworkSaveDTO;
import com.smartclass.dto.SubmitDTO;
import com.smartclass.entity.*;
import com.smartclass.mapper.*;
import com.smartclass.util.JsonUtil;
import com.smartclass.util.UserContext;
import com.smartclass.vo.HomeworkVO;
import com.smartclass.vo.QuestionVO;
import com.smartclass.vo.SubmitDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业服务:教师端发布与批阅、学生端答题与成绩查看
 */
@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkMapper homeworkMapper;
    private final HomeworkQuestionMapper homeworkQuestionMapper;
    private final HomeworkSubmitMapper homeworkSubmitMapper;
    private final HomeworkAnswerMapper homeworkAnswerMapper;
    private final QuestionMapper questionMapper;
    private final QuestionService questionService;
    private final CourseMapper courseMapper;
    private final ClazzMapper clazzMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;
    private final Grader grader;
    private final PracticeRecorder practiceRecorder;

    // ==================== 教师端 ====================

    @Transactional(rollbackFor = Exception.class)
    public void save(HomeworkSaveDTO dto) {
        Homework homework = new Homework();
        homework.setId(dto.getId());
        homework.setCourseId(dto.getCourseId());
        homework.setClazzId(dto.getClazzId());
        homework.setTitle(dto.getTitle());
        homework.setDescription(dto.getDescription());
        homework.setStartTime(dto.getStartTime());
        homework.setEndTime(dto.getEndTime());
        homework.setStatus(dto.getPublish() != null && dto.getPublish() == 1 ? 1 : 0);

        if (dto.getId() == null) {
            homework.setTeacherId(UserContext.getUserId());
            homework.setCreateTime(LocalDateTime.now());
            homeworkMapper.insert(homework);
        } else {
            homework.setTeacherId(UserContext.getUserId());
            homeworkMapper.updateById(homework);
            homeworkQuestionMapper.delete(new LambdaQueryWrapper<HomeworkQuestion>()
                    .eq(HomeworkQuestion::getHomeworkId, dto.getId()));
        }

        List<HomeworkQuestion> links = new ArrayList<>();
        int sort = 1;
        for (HomeworkSaveDTO.HomeworkItemDTO item : dto.getQuestions()) {
            HomeworkQuestion link = new HomeworkQuestion();
            link.setHomeworkId(homework.getId());
            link.setQuestionId(item.getQuestionId());
            link.setSort(sort++);
            link.setScore(item.getScore() == null ? 5 : item.getScore());
            links.add(link);
        }
        links.forEach(homeworkQuestionMapper::insert);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Homework homework = homeworkMapper.selectById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        Homework update = new Homework();
        update.setId(id);
        update.setStatus(1);
        homeworkMapper.updateById(update);
    }

    public void delete(Long id) {
        homeworkSubmitMapper.delete(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, id));
        homeworkQuestionMapper.delete(new LambdaQueryWrapper<HomeworkQuestion>()
                .eq(HomeworkQuestion::getHomeworkId, id));
        homeworkMapper.deleteById(id);
    }

    public Page<HomeworkVO> teacherPage(Long current, Long size) {
        Page<Homework> page = homeworkMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Homework>()
                        .eq(Homework::getTeacherId, UserContext.getUserId())
                        .orderByDesc(Homework::getId));
        return assemble(page, true);
    }

    public List<HomeworkSubmit> submitList(Long homeworkId) {
        return homeworkSubmitMapper.selectList(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, homeworkId)
                .orderByDesc(HomeworkSubmit::getScore));
    }

    /**
     * 教师批阅:确认成绩并填写评语
     */
    public void grade(Long submitId, String comment) {
        HomeworkSubmit submit = homeworkSubmitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException("提交记录不存在");
        }
        HomeworkSubmit update = new HomeworkSubmit();
        update.setId(submitId);
        update.setStatus(2);
        update.setComment(comment);
        homeworkSubmitMapper.updateById(update);
    }

    // ==================== 学生端 ====================

    public Page<HomeworkVO> studentPage(Long current, Long size, Integer status) {
        User student = userMapper.selectById(UserContext.getUserId());
        if (student.getClazzId() == null) {
            return new Page<>(current, size, 0);
        }
        Page<Homework> page = homeworkMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Homework>()
                        .eq(Homework::getClazzId, student.getClazzId())
                        .eq(Homework::getStatus, 1)
                        .orderByDesc(Homework::getId));
        Page<HomeworkVO> result = assemble(page, false);

        Map<Long, HomeworkSubmit> mySubmits = homeworkSubmitMapper.selectList(
                        new LambdaQueryWrapper<HomeworkSubmit>()
                                .eq(HomeworkSubmit::getStudentId, student.getId()))
                .stream().collect(Collectors.toMap(HomeworkSubmit::getHomeworkId, s -> s));
        result.getRecords().forEach(vo -> {
            HomeworkSubmit submit = mySubmits.get(vo.getId());
            vo.setMyStatus(submit == null ? 0 : submit.getStatus());
            vo.setMyScore(submit == null ? null : submit.getScore());
        });
        return result;
    }

    /**
     * 学生进入作业:返回题目列表(不带答案),同时返回自己的提交状态
     */
    public Map<String, Object> studentDetail(Long homeworkId) {
        Homework homework = homeworkMapper.selectById(homeworkId);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        List<HomeworkQuestion> links = homeworkQuestionMapper.selectList(
                new LambdaQueryWrapper<HomeworkQuestion>()
                        .eq(HomeworkQuestion::getHomeworkId, homeworkId)
                        .orderByAsc(HomeworkQuestion::getSort));

        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        Map<Long, String> knowledgeNames = questionService.knowledgeNames();

        List<Map<String, Object>> questions = links.stream().map(link -> {
            Question question = questionMapper.selectById(link.getQuestionId());
            if (question == null) {
                return null;
            }
            QuestionVO vo = questionService.toVO(question, subjectNames, knowledgeNames, false);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("linkId", link.getId());
            item.put("score", link.getScore());
            item.put("question", vo);
            return item;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        HomeworkSubmit submit = homeworkSubmitMapper.selectOne(
                new LambdaQueryWrapper<HomeworkSubmit>()
                        .eq(HomeworkSubmit::getHomeworkId, homeworkId)
                        .eq(HomeworkSubmit::getStudentId, UserContext.getUserId()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("homework", homework);
        result.put("questions", questions);
        result.put("submit", submit);
        return result;
    }

    /**
     * 学生提交作业:客观题自动判分、落练习流水、维护错题本
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submit(SubmitDTO dto) {
        Long studentId = UserContext.getUserId();
        Homework homework = homeworkMapper.selectById(dto.getHomeworkId());
        if (homework == null || homework.getStatus() != 1) {
            throw new BusinessException("作业不存在或未发布");
        }
        Long exists = homeworkSubmitMapper.selectCount(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, dto.getHomeworkId())
                .eq(HomeworkSubmit::getStudentId, studentId));
        if (exists > 0) {
            throw new BusinessException("你已提交过该作业,不能重复提交");
        }
        if (homework.getEndTime() != null && LocalDateTime.now().isAfter(homework.getEndTime())) {
            throw new BusinessException("已超过截止时间,无法提交");
        }

        List<HomeworkQuestion> links = homeworkQuestionMapper.selectList(
                new LambdaQueryWrapper<HomeworkQuestion>()
                        .eq(HomeworkQuestion::getHomeworkId, dto.getHomeworkId())
                        .orderByAsc(HomeworkQuestion::getSort));
        Map<Long, String> answerMap = dto.getAnswers().stream()
                .collect(Collectors.toMap(SubmitDTO.AnswerItemDTO::getQuestionId,
                        a -> a.getAnswer() == null ? "" : a.getAnswer(), (a, b) -> a));

        HomeworkSubmit submit = new HomeworkSubmit();
        submit.setHomeworkId(dto.getHomeworkId());
        submit.setStudentId(studentId);
        submit.setStatus(1);
        submit.setSubmitTime(LocalDateTime.now());
        submit.setCreateTime(LocalDateTime.now());

        int totalScore = 0;
        int myScore = 0;
        List<HomeworkAnswer> answers = new ArrayList<>();
        for (HomeworkQuestion link : links) {
            Question question = questionMapper.selectById(link.getQuestionId());
            if (question == null) {
                continue;
            }
            String myAnswer = answerMap.getOrDefault(link.getQuestionId(), "");
            boolean correct = grader.judge(question, myAnswer);
            int gained = correct ? link.getScore() : 0;
            totalScore += link.getScore();
            myScore += gained;

            HomeworkAnswer answer = new HomeworkAnswer();
            answer.setSubmitId(submit.getId());
            answer.setQuestionId(link.getQuestionId());
            answer.setAnswer(myAnswer);
            answer.setIsCorrect(correct ? 1 : 0);
            answer.setScore(gained);
            answer.setQuestionScore(link.getScore());
            answers.add(answer);

            practiceRecorder.record(studentId, question, correct, "HOMEWORK");
        }

        submit.setScore(myScore);
        submit.setTotalScore(totalScore);
        homeworkSubmitMapper.insert(submit);
        for (HomeworkAnswer answer : answers) {
            answer.setSubmitId(submit.getId());
            homeworkAnswerMapper.insert(answer);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", myScore);
        result.put("totalScore", totalScore);
        result.put("correctCount", answers.stream().filter(a -> a.getIsCorrect() == 1).count());
        result.put("questionCount", answers.size());
        return result;
    }

    /**
     * 提交明细:学生查成绩、教师批阅共用
     */
    public SubmitDetailVO submitDetail(Long submitId) {
        HomeworkSubmit submit = homeworkSubmitMapper.selectById(submitId);
        if (submit == null) {
            throw new BusinessException("提交记录不存在");
        }
        Homework homework = homeworkMapper.selectById(submit.getHomeworkId());
        User student = userMapper.selectById(submit.getStudentId());

        SubmitDetailVO vo = new SubmitDetailVO();
        vo.setSubmitId(submit.getId());
        vo.setHomeworkId(submit.getHomeworkId());
        vo.setHomeworkTitle(homework == null ? "" : homework.getTitle());
        vo.setStudentName(student == null ? "" : student.getRealName());
        vo.setStatus(submit.getStatus());
        vo.setScore(submit.getScore());
        vo.setTotalScore(submit.getTotalScore());
        vo.setSubmitTime(submit.getSubmitTime());
        vo.setComment(submit.getComment());

        if (student != null && student.getClazzId() != null) {
            Clazz clazz = clazzMapper.selectById(student.getClazzId());
            vo.setClazzName(clazz == null ? "" : clazz.getName());
        }

        List<HomeworkAnswer> answerList = homeworkAnswerMapper.selectList(
                new LambdaQueryWrapper<HomeworkAnswer>()
                        .eq(HomeworkAnswer::getSubmitId, submitId));
        Map<Long, String> knowledgeNames = questionService.knowledgeNames();

        List<SubmitDetailVO.AnswerDetailVO> answerVos = answerList.stream().map(answer -> {
            Question question = questionMapper.selectById(answer.getQuestionId());
            SubmitDetailVO.AnswerDetailVO detail = new SubmitDetailVO.AnswerDetailVO();
            detail.setQuestionId(answer.getQuestionId());
            detail.setTitle(question == null ? "题目已删除" : question.getTitle());
            detail.setType(question == null ? "" : question.getType());
            detail.setOptions(question == null ? List.of() : JsonUtil.parseList(question.getOptions()));
            detail.setMyAnswer(answer.getAnswer());
            detail.setRightAnswer(question == null ? "" : question.getAnswer());
            detail.setAnalysis(question == null ? "" : question.getAnalysis());
            detail.setIsCorrect(answer.getIsCorrect());
            detail.setScore(answer.getScore());
            detail.setQuestionScore(answer.getQuestionScore());
            return detail;
        }).collect(Collectors.toList());
        vo.setAnswers(answerVos);
        return vo;
    }

    public HomeworkSubmit mySubmit(Long homeworkId) {
        return homeworkSubmitMapper.selectOne(new LambdaQueryWrapper<HomeworkSubmit>()
                .eq(HomeworkSubmit::getHomeworkId, homeworkId)
                .eq(HomeworkSubmit::getStudentId, UserContext.getUserId()));
    }

    // ==================== 组装 ====================

    private Page<HomeworkVO> assemble(Page<Homework> page, boolean teacherView) {
        Map<Long, Course> courseMap = courseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Map<Long, Clazz> clazzMap = clazzMapper.selectList(null).stream()
                .collect(Collectors.toMap(Clazz::getId, c -> c));
        Map<Long, Subject> subjectMap = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));
        Map<Long, String> teacherNames = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getRole, Constants.ROLE_TEACHER))
                .stream().collect(Collectors.toMap(User::getId, User::getRealName));

        List<HomeworkVO> records = page.getRecords().stream().map(homework -> {
            HomeworkVO vo = new HomeworkVO();
            org.springframework.beans.BeanUtils.copyProperties(homework, vo);
            Course course = courseMap.get(homework.getCourseId());
            vo.setCourseName(course == null ? "" : course.getName());
            Subject subject = course == null ? null : subjectMap.get(course.getSubjectId());
            vo.setSubjectName(subject == null ? "" : subject.getName());
            Clazz clazz = clazzMap.get(homework.getClazzId());
            vo.setClazzName(clazz == null ? "" : clazz.getName());
            vo.setTeacherName(teacherNames.getOrDefault(homework.getTeacherId(), ""));

            List<HomeworkQuestion> links = homeworkQuestionMapper.selectList(
                    new LambdaQueryWrapper<HomeworkQuestion>()
                            .eq(HomeworkQuestion::getHomeworkId, homework.getId()));
            vo.setQuestionCount(links.size());
            vo.setTotalScore(links.stream().mapToInt(HomeworkQuestion::getScore).sum());

            if (teacherView) {
                List<HomeworkSubmit> submits = homeworkSubmitMapper.selectList(
                        new LambdaQueryWrapper<HomeworkSubmit>()
                                .eq(HomeworkSubmit::getHomeworkId, homework.getId()));
                vo.setSubmitCount(submits.size());
                vo.setAvgScore(submits.isEmpty() ? 0.0 :
                        submits.stream().mapToInt(HomeworkSubmit::getScore).average().orElse(0));
            }
            return vo;
        }).collect(Collectors.toList());

        Page<HomeworkVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }
}
