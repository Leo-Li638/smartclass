package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.Constants;
import com.smartclass.entity.*;
import com.smartclass.mapper.*;
import com.smartclass.util.UserContext;
import com.smartclass.vo.AdminDashboardVO;
import com.smartclass.vo.NameValueVO;
import com.smartclass.vo.StudentDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计服务:三个角色的看板数据
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserMapper userMapper;
    private final ClazzMapper clazzMapper;
    private final CourseMapper courseMapper;
    private final HomeworkMapper homeworkMapper;
    private final QuestionMapper questionMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final HomeworkSubmitMapper homeworkSubmitMapper;
    private final HomeworkAnswerMapper homeworkAnswerMapper;
    private final SubjectMapper subjectMapper;
    private final WrongBookMapper wrongBookMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    // ==================== 管理端看板 ====================

    public AdminDashboardVO adminDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();
        vo.setTeacherCount(userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, Constants.ROLE_TEACHER)));
        vo.setStudentCount(userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, Constants.ROLE_STUDENT)));
        vo.setClazzCount(clazzMapper.selectCount(null));
        vo.setCourseCount(courseMapper.selectCount(null));
        vo.setHomeworkCount(homeworkMapper.selectCount(null));
        vo.setQuestionCount(questionMapper.selectCount(null));
        vo.setPracticeCount(practiceRecordMapper.selectCount(null));

        // 近 7 天每日做题量
        List<PracticeRecord> recent = practiceRecordMapper.selectList(
                new LambdaQueryWrapper<PracticeRecord>()
                        .ge(PracticeRecord::getCreateTime, LocalDate.now().minusDays(6).atStartOfDay()));
        Map<String, Long> byDay = recent.stream().collect(Collectors.groupingBy(
                r -> r.getCreateTime().toLocalDate().toString(), Collectors.counting()));
        List<NameValueVO> recentPractice = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String day = LocalDate.now().minusDays(i).toString();
            recentPractice.add(new NameValueVO(day, byDay.getOrDefault(day, 0L)));
        }
        vo.setRecentPractice(recentPractice);

        // 各学科题目分布
        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        Map<Long, Long> questionBySubject = questionMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(Question::getSubjectId, Collectors.counting()));
        vo.setSubjectQuestionDist(subjectNames.entrySet().stream()
                .map(e -> new NameValueVO(e.getValue(), questionBySubject.getOrDefault(e.getKey(), 0L)))
                .collect(Collectors.toList()));

        // 各班级学生人数
        vo.setClazzStudentDist(clazzMapper.selectList(null).stream()
                .map(clazz -> new NameValueVO(clazz.getName(), userMapper.selectCount(
                        new LambdaQueryWrapper<User>()
                                .eq(User::getClazzId, clazz.getId())
                                .eq(User::getRole, Constants.ROLE_STUDENT))))
                .collect(Collectors.toList()));
        return vo;
    }

    // ==================== 学生端看板 ====================

    public StudentDashboardVO studentDashboard() {
        Long studentId = UserContext.getUserId();
        StudentDashboardVO vo = new StudentDashboardVO();

        List<PracticeRecord> records = practiceRecordMapper.selectList(
                new LambdaQueryWrapper<PracticeRecord>()
                        .eq(PracticeRecord::getStudentId, studentId));
        vo.setTotalPractice((long) records.size());
        vo.setCorrectCount(records.stream().filter(r -> r.getIsCorrect() == 1).count());
        vo.setCorrectRate(records.isEmpty() ? 0.0
                : Math.round(vo.getCorrectCount() * 1000.0 / records.size()) / 10.0);
        vo.setWrongCount(wrongBookMapper.selectCount(new LambdaQueryWrapper<WrongBook>()
                .eq(WrongBook::getStudentId, studentId)
                .eq(WrongBook::getMastered, 0)));

        // 未完成作业数 = 所在班级已发布作业数 - 已提交数
        User student = userMapper.selectById(studentId);
        if (student != null && student.getClazzId() != null) {
            Long published = homeworkMapper.selectCount(new LambdaQueryWrapper<Homework>()
                    .eq(Homework::getClazzId, student.getClazzId())
                    .eq(Homework::getStatus, 1));
            Long submitted = homeworkSubmitMapper.selectCount(new LambdaQueryWrapper<HomeworkSubmit>()
                    .eq(HomeworkSubmit::getStudentId, studentId));
            vo.setUnfinishedHomework(Math.max(0, published - submitted));
            vo.setFinishedHomework(submitted);
        } else {
            vo.setUnfinishedHomework(0L);
            vo.setFinishedHomework(0L);
        }

        // 近 14 天每日正确率
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<PracticeRecord> recent = records.stream()
                .filter(r -> r.getCreateTime() != null
                        && r.getCreateTime().isAfter(LocalDate.now().minusDays(13).atStartOfDay()))
                .collect(Collectors.toList());
        Map<String, List<PracticeRecord>> byDay = recent.stream().collect(Collectors.groupingBy(
                r -> r.getCreateTime().toLocalDate().toString()));
        List<NameValueVO> trend = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            List<PracticeRecord> dayRecords = byDay.getOrDefault(day.toString(), List.of());
            double rate = dayRecords.isEmpty() ? 0.0
                    : Math.round(dayRecords.stream().filter(r -> r.getIsCorrect() == 1).count()
                    * 1000.0 / dayRecords.size()) / 10.0;
            trend.add(new NameValueVO(day.format(formatter), rate));
        }
        vo.setRecentCorrectRate(trend);

        // 各学科正确率:练习记录 → 题目 → 学科
        Map<Long, Question> questionMap = questionMapper.selectList(null).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        Map<Long, List<PracticeRecord>> bySubject = records.stream()
                .filter(r -> questionMap.containsKey(r.getQuestionId()))
                .collect(Collectors.groupingBy(r -> questionMap.get(r.getQuestionId()).getSubjectId()));
        vo.setSubjectCorrectRate(subjectNames.entrySet().stream()
                .map(e -> {
                    List<PracticeRecord> list = bySubject.getOrDefault(e.getKey(), List.of());
                    double rate = list.isEmpty() ? 0.0
                            : Math.round(list.stream().filter(r -> r.getIsCorrect() == 1).count()
                            * 1000.0 / list.size()) / 10.0;
                    return new NameValueVO(e.getValue(), rate);
                })
                .collect(Collectors.toList()));
        return vo;
    }

    // ==================== 教师端学情分析 ====================

    /**
     * 某份作业的知识点错误率分析:帮助教师定位讲评重点
     */
    public List<Map<String, Object>> homeworkKnowledgeAnalysis(Long homeworkId) {
        List<HomeworkSubmit> submits = homeworkSubmitMapper.selectList(
                new LambdaQueryWrapper<HomeworkSubmit>()
                        .eq(HomeworkSubmit::getHomeworkId, homeworkId));
        if (submits.isEmpty()) {
            return List.of();
        }
        Map<Long, List<HomeworkAnswer>> answersBySubmit = homeworkAnswerMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(HomeworkAnswer::getSubmitId));

        Map<Long, Question> questionMap = questionMapper.selectList(null).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 知识点 → (做题人次, 错误人次)
        Map<Long, int[]> knowledgeStats = new LinkedHashMap<>();
        for (HomeworkSubmit submit : submits) {
            for (HomeworkAnswer answer : answersBySubmit.getOrDefault(submit.getId(), List.of())) {
                Question question = questionMap.get(answer.getQuestionId());
                if (question == null || question.getKnowledgeId() == null) {
                    continue;
                }
                int[] stat = knowledgeStats.computeIfAbsent(question.getKnowledgeId(),
                        k -> new int[2]);
                stat[0]++;
                if (answer.getIsCorrect() == 0) {
                    stat[1]++;
                }
            }
        }

        Map<Long, String> knowledgeNames = new HashMap<>();
        for (Question question : questionMap.values()) {
            if (question.getKnowledgeId() != null) {
                knowledgeNames.putIfAbsent(question.getKnowledgeId(), "知识点#" + question.getKnowledgeId());
            }
        }
        // 补全知识点名称
        for (com.smartclass.entity.KnowledgePoint point : knowledgePoints()) {
            knowledgeNames.put(point.getId(), point.getName());
        }

        return knowledgeStats.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("knowledgeId", e.getKey());
                    item.put("knowledgeName", knowledgeNames.getOrDefault(e.getKey(), "未知"));
                    item.put("total", e.getValue()[0]);
                    item.put("errorCount", e.getValue()[1]);
                    item.put("errorRate", e.getValue()[0] == 0 ? 0.0
                            : Math.round(e.getValue()[1] * 1000.0 / e.getValue()[0]) / 10.0);
                    return item;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("errorRate"), (Double) a.get("errorRate")))
                .collect(Collectors.toList());
    }

    /**
     * 教师所带班级的成绩概览
     */
    public List<Map<String, Object>> teacherClassOverview() {
        Long teacherId = UserContext.getUserId();
        List<Homework> myHomeworks = homeworkMapper.selectList(
                new LambdaQueryWrapper<Homework>()
                        .eq(Homework::getTeacherId, teacherId));
        if (myHomeworks.isEmpty()) {
            return List.of();
        }
        Set<Long> homeworkIds = myHomeworks.stream().map(Homework::getId).collect(Collectors.toSet());
        List<HomeworkSubmit> submits = homeworkSubmitMapper.selectList(
                new LambdaQueryWrapper<HomeworkSubmit>()
                        .in(HomeworkSubmit::getHomeworkId, homeworkIds));

        Map<Long, String> clazzNames = clazzMapper.selectList(null).stream()
                .collect(Collectors.toMap(Clazz::getId, Clazz::getName));
        Map<Long, List<HomeworkSubmit>> byClazz = submits.stream()
                .filter(s -> homeworkIds.contains(s.getHomeworkId()))
                .collect(Collectors.groupingBy(s -> {
                    Homework homework = myHomeworks.stream()
                            .filter(h -> h.getId().equals(s.getHomeworkId())).findFirst().orElse(null);
                    return homework == null ? -1L : homework.getClazzId();
                }));

        return byClazz.entrySet().stream()
                .filter(e -> e.getKey() > 0)
                .map(e -> {
                    List<HomeworkSubmit> list = e.getValue();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("clazzId", e.getKey());
                    item.put("clazzName", clazzNames.getOrDefault(e.getKey(), "未知班级"));
                    item.put("submitCount", list.size());
                    item.put("avgScore", Math.round(list.stream()
                            .mapToInt(HomeworkSubmit::getScore).average().orElse(0) * 10) / 10.0);
                    item.put("passRate", list.isEmpty() ? 0.0 : Math.round(list.stream()
                            .filter(s -> s.getTotalScore() != null && s.getTotalScore() > 0
                                    && s.getScore() * 1.0 / s.getTotalScore() >= 0.6).count()
                            * 1000.0 / list.size()) / 10.0);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<com.smartclass.entity.KnowledgePoint> knowledgePoints() {
        return knowledgePointMapper.selectList(null);
    }
}
