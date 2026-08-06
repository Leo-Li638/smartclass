package com.smartclass.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.entity.*;
import com.smartclass.mapper.*;
import com.smartclass.service.Grader;
import com.smartclass.service.PracticeRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 演示数据初始化器
 * 首次启动时创建内置账号,并生成一份已发布的作业、部分学生提交记录
 * 和近两周的练习流水,保证看板、错题本与智能推题开箱即可看到效果。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements org.springframework.boot.CommandLineRunner {

    private final UserMapper userMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkQuestionMapper homeworkQuestionMapper;
    private final HomeworkSubmitMapper homeworkSubmitMapper;
    private final HomeworkAnswerMapper homeworkAnswerMapper;
    private final QuestionMapper questionMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final PlanTaskMapper planTaskMapper;
    private final PracticeRecorder practiceRecorder;
    private final Grader grader;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        log.info("检测到空库,开始初始化演示数据...");

        createUsers();
        Long mathHomework = createMathHomework();
        createSubmissions(mathHomework);
        Long chineseHomework = createChineseHomework();
        createChineseSubmissions(chineseHomework);
        createChineseHomework2();
        createEnglishHomework();
        createPracticeFlows();
        createStudyPlan();

        log.info("演示数据初始化完成:admin/admin123, teacher1~3/teacher123, student1~6/student123");
    }

    private void createUsers() {
        insertUser("admin", "admin123", "系统管理员", "ADMIN", null, "女");
        insertUser("teacher1", "teacher123", "王雨薇", "TEACHER", null, "女");
        insertUser("teacher2", "teacher123", "张明远", "TEACHER", null, "男");
        insertUser("teacher3", "teacher123", "李思琪", "TEACHER", null, "女");

        insertUser("student1", "student123", "陈子昂", "STUDENT", 1L, "男");
        insertUser("student2", "student123", "林小满", "STUDENT", 1L, "女");
        insertUser("student3", "student123", "周天翼", "STUDENT", 1L, "男");
        insertUser("student4", "student123", "吴思彤", "STUDENT", 2L, "女");
        insertUser("student5", "student123", "郑一诺", "STUDENT", 2L, "男");
        insertUser("student6", "student123", "孙若溪", "STUDENT", 3L, "女");
    }

    private void insertUser(String username, String password, String realName,
                            String role, Long clazzId, String gender) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRealName(realName);
        user.setRole(role);
        user.setClazzId(clazzId);
        user.setGender(gender);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    /**
     * 作业 1:初二数学培优班「一元二次方程专项练习」,面向初二(1)班
     */
    private Long createMathHomework() {
        Homework homework = new Homework();
        homework.setCourseId(2L);
        homework.setClazzId(1L);
        homework.setTeacherId(3L);
        homework.setTitle("一元二次方程专项练习");
        homework.setDescription("共 5 题,覆盖一元二次方程的解法与根与系数关系,提交后系统自动批改。");
        homework.setStartTime(LocalDateTime.now().minusDays(3));
        homework.setEndTime(LocalDateTime.now().plusDays(4));
        homework.setStatus(1);
        homework.setCreateTime(LocalDateTime.now().minusDays(3));
        homeworkMapper.insert(homework);

        long[] questionIds = {6, 7, 8, 9, 10};
        int sort = 1;
        for (long questionId : questionIds) {
            HomeworkQuestion link = new HomeworkQuestion();
            link.setHomeworkId(homework.getId());
            link.setQuestionId(questionId);
            link.setSort(sort++);
            link.setScore(5);
            homeworkQuestionMapper.insert(link);
        }
        return homework.getId();
    }

    /**
     * 学生 1、2 已提交作业,学生 3 未提交,便于演示教师批阅与学生成绩页
     */
    private void createSubmissions(Long homeworkId) {
        // 学生 1:答对 3 题(6、8、10),答错 2 题(7、9)
        submitHomework(homeworkId, 5L, new long[]{6, 8, 10}, new long[]{7, 9});
        // 学生 2:全对
        submitHomework(homeworkId, 6L, new long[]{6, 7, 8, 9, 10}, new long[]{});
    }

    /**
     * 作业 2:teacher1(王雨薇)的语文作业「古诗文与写作基础专项练习」,面向初二(1)班
     */
    private Long createChineseHomework() {
        Homework homework = new Homework();
        homework.setCourseId(1L);
        homework.setClazzId(1L);
        homework.setTeacherId(2L);
        homework.setTitle("古诗文与写作基础专项练习");
        homework.setDescription("共 5 题,覆盖文言文名句、古诗词常识与记叙文写作基础,提交后系统自动批改。");
        homework.setStartTime(LocalDateTime.now().minusDays(2));
        homework.setEndTime(LocalDateTime.now().plusDays(5));
        homework.setStatus(1);
        homework.setCreateTime(LocalDateTime.now().minusDays(2));
        homeworkMapper.insert(homework);

        long[] questionIds = {49, 51, 53, 54, 55};
        int sort = 1;
        for (long questionId : questionIds) {
            HomeworkQuestion link = new HomeworkQuestion();
            link.setHomeworkId(homework.getId());
            link.setQuestionId(questionId);
            link.setSort(sort++);
            link.setScore(5);
            homeworkQuestionMapper.insert(link);
        }
        return homework.getId();
    }

    /**
     * 语文作业:学生 1、2、3 均已提交,学生 2 由弱项题构成
     */
    private void createChineseSubmissions(Long homeworkId) {
        // 学生 1:答对 3 题(49、51、54),答错 2 题(53、55)
        submitHomework(homeworkId, 5L, new long[]{49, 51, 54}, new long[]{53, 55});
        // 学生 2:答对 3 题(49、53、55),答错 2 题(51、54)
        submitHomework(homeworkId, 6L, new long[]{49, 53, 55}, new long[]{51, 54});
        // 学生 3:全对
        submitHomework(homeworkId, 7L, new long[]{49, 51, 53, 54, 55}, new long[]{});
    }

    /**
     * 作业 4:teacher1(王雨薇)的第二份语文作业「文言文名句与文学常识巩固练习」,面向初二(1)班
     * 学生 1、2、3 均已提交,丰富教师端学情分析与班级概览数据
     */
    private void createChineseHomework2() {
        Homework homework = new Homework();
        homework.setCourseId(1L);
        homework.setClazzId(1L);
        homework.setTeacherId(2L);
        homework.setTitle("文言文名句与文学常识巩固练习");
        homework.setDescription("共 5 题,覆盖文言实词、古诗文名句与文学常识,提交后系统自动批改。");
        homework.setStartTime(LocalDateTime.now().minusDays(2));
        homework.setEndTime(LocalDateTime.now().plusDays(5));
        homework.setStatus(1);
        homework.setCreateTime(LocalDateTime.now().minusDays(2));
        homeworkMapper.insert(homework);

        long[] questionIds = {49, 50, 51, 52, 56};
        int sort = 1;
        for (long questionId : questionIds) {
            HomeworkQuestion link = new HomeworkQuestion();
            link.setHomeworkId(homework.getId());
            link.setQuestionId(questionId);
            link.setSort(sort++);
            link.setScore(5);
            homeworkQuestionMapper.insert(link);
        }

        // 学生 1:答对 3 题(50、52、56),答错 2 题(49、51)
        submitHomework(homework.getId(), 5L, new long[]{50, 52, 56}, new long[]{49, 51});
        // 学生 2:答对 2 题(50、52),答错 3 题(49、51、56)
        submitHomework(homework.getId(), 6L, new long[]{50, 52}, new long[]{49, 51, 56});
        // 学生 3:全对
        submitHomework(homework.getId(), 7L, new long[]{49, 50, 51, 52, 56}, new long[]{});
    }

    /**
     * 作业 3:teacher3(李思琪)的英语作业「英语时态与从句专项练习」,面向初二(1)班
     * 所有学生均未提交,便于演示学生在线作答、自动批改的完整流程
     */
    private void createEnglishHomework() {
        Homework homework = new Homework();
        homework.setCourseId(3L);
        homework.setClazzId(1L);
        homework.setTeacherId(4L);
        homework.setTitle("英语时态与从句专项练习");
        homework.setDescription("共 5 题,覆盖一般现在时、现在进行时、被动语态与定语/宾语从句,提交后系统自动批改。");
        homework.setStartTime(LocalDateTime.now().minusDays(1));
        homework.setEndTime(LocalDateTime.now().plusDays(7));
        homework.setStatus(1);
        homework.setCreateTime(LocalDateTime.now().minusDays(1));
        homeworkMapper.insert(homework);

        long[] questionIds = {19, 20, 21, 23, 24};
        int sort = 1;
        for (long questionId : questionIds) {
            HomeworkQuestion link = new HomeworkQuestion();
            link.setHomeworkId(homework.getId());
            link.setQuestionId(questionId);
            link.setSort(sort++);
            link.setScore(5);
            homeworkQuestionMapper.insert(link);
        }
    }

    /**
     * 为学生 1 生成一份进行中的学习计划(数学薄弱知识点专项),
     * 任务日期覆盖过去 7 天,进度由练习流水实时回算,登录即可看到部分完成的真实进度
     */
    private void createStudyPlan() {
        StudyPlan plan = new StudyPlan();
        plan.setStudentId(5L);
        plan.setSubjectId(2L);
        plan.setTitle("数学薄弱知识点专项提升计划");
        plan.setGoal("针对一元二次方程、函数与图像集中突破,提升整体正确率");
        plan.setStartDate(java.time.LocalDate.now().minusDays(6));
        plan.setEndDate(java.time.LocalDate.now());
        plan.setDailyCount(4);
        plan.setStatus(0);
        plan.setCreateTime(LocalDateTime.now().minusDays(6));
        studyPlanMapper.insert(plan);

        long[] knowledgeIds = {7, 8};
        String[] knowledgeNames = {"一元二次方程", "函数与图像"};
        for (int day = 6; day >= 0; day--) {
            java.time.LocalDate taskDate = java.time.LocalDate.now().minusDays(day);
            for (int i = 0; i < knowledgeIds.length; i++) {
                PlanTask task = new PlanTask();
                task.setPlanId(plan.getId());
                task.setKnowledgeId(knowledgeIds[i]);
                task.setTaskDate(taskDate);
                task.setContent("巩固《" + knowledgeNames[i] + "》: 完成 2 题");
                task.setQuestionCount(2);
                task.setDoneCount(0);
                task.setStatus(0);
                planTaskMapper.insert(task);
            }
        }
    }

    private void submitHomework(Long homeworkId, Long studentId, long[] correctIds, long[] wrongIds) {
        List<HomeworkQuestion> links = homeworkQuestionMapper.selectList(
                new LambdaQueryWrapper<HomeworkQuestion>()
                        .eq(HomeworkQuestion::getHomeworkId, homeworkId)
                        .orderByAsc(HomeworkQuestion::getSort));

        HomeworkSubmit submit = new HomeworkSubmit();
        submit.setHomeworkId(homeworkId);
        submit.setStudentId(studentId);
        submit.setStatus(1);
        submit.setSubmitTime(LocalDateTime.now().minusDays(2));
        submit.setCreateTime(LocalDateTime.now().minusDays(2));

        int score = 0;
        int total = 0;
        for (HomeworkQuestion link : links) {
            Question question = questionMapper.selectById(link.getQuestionId());
            boolean correct = contains(correctIds, link.getQuestionId());
            total += link.getScore();
            if (correct) {
                score += link.getScore();
            }
        }
        submit.setScore(score);
        submit.setTotalScore(total);
        homeworkSubmitMapper.insert(submit);

        for (HomeworkQuestion link : links) {
            Question question = questionMapper.selectById(link.getQuestionId());
            boolean correct = contains(correctIds, link.getQuestionId());
            // 答错时故意填入一个错误答案,模拟真实作答
            String answer = correct ? question.getAnswer() : wrongAnswerOf(question);

            HomeworkAnswer homeworkAnswer = new HomeworkAnswer();
            homeworkAnswer.setSubmitId(submit.getId());
            homeworkAnswer.setQuestionId(link.getQuestionId());
            homeworkAnswer.setAnswer(answer);
            homeworkAnswer.setIsCorrect(correct ? 1 : 0);
            homeworkAnswer.setScore(correct ? link.getScore() : 0);
            homeworkAnswer.setQuestionScore(link.getScore());
            homeworkAnswerMapper.insert(homeworkAnswer);

            practiceRecorder.record(studentId, question, correct, "HOMEWORK");
        }
    }

    private String wrongAnswerOf(Question question) {
        return switch (question.getType()) {
            case "SINGLE" -> question.getAnswer().equals("A") ? "B" : "A";
            case "MULTI" -> "A";
            case "JUDGE" -> question.getAnswer().equals("对") ? "错" : "对";
            default -> "1";
        };
    }

    private boolean contains(long[] ids, long target) {
        for (long id : ids) {
            if (id == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * 为学生 1 生成近 14 天练习流水,塑造"函数与图像、时态语态薄弱"的学情画像,
     * 让智能推题与学习计划一登录就有真实效果
     */
    private void createPracticeFlows() {
        practiceFlow(5L, 6, 5);   // 有理数运算:掌握良好
        practiceFlow(6L, 6, 5);   // 一元一次方程:掌握良好
        practiceFlow(7L, 8, 3);   // 一元二次方程:偏弱
        practiceFlow(8L, 6, 2);   // 函数与图像:薄弱
        practiceFlow(11L, 6, 3);  // 英语时态语态:偏弱
        practiceFlow(13L, 4, 3);  // 词汇辨析:尚可
        practiceFlow(15L, 4, 3);  // 物理力与运动:尚可
        practiceFlow(19L, 3, 2);  // 化学方程式:尚可
        practiceFlow(22L, 3, 2);  // 生物细胞结构:尚可
        spreadPracticeDates();
    }

    private void practiceFlow(Long knowledgeId, int total, int correctCount) {
        Long studentId = 5L;
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getKnowledgeId, knowledgeId));
        if (questions.isEmpty()) {
            return;
        }
        for (int i = 0; i < total; i++) {
            Question question = questions.get(i % questions.size());
            boolean correct = i < correctCount;
            String answer = correct ? question.getAnswer() : wrongAnswerOf(question);
            if (grader.judge(question, answer) != correct) {
                correct = grader.judge(question, answer);
            }
            practiceRecorder.record(studentId, question, correct, "PRACTICE");
        }
    }

    /**
     * 把自主练习流水摊到过去 13 天,制造真实的练习节奏,
     * 让正确率趋势图和遗忘曲线衰减都有数据可看。
     * 由于 practiceFlow 是"先对后错"写入,直接按顺序摊会把错题集中到尾部日期,
     * 造成连续多天 0% 的假象,因此采用前后两半交错排列后再均匀摊开。
     */
    private void spreadPracticeDates() {
        List<PracticeRecord> records = practiceRecordMapper.selectList(
                new LambdaQueryWrapper<PracticeRecord>()
                        .eq(PracticeRecord::getStudentId, 5L)
                        .eq(PracticeRecord::getSource, "PRACTICE")
                        .orderByAsc(PracticeRecord::getId));

        List<PracticeRecord> interleaved = new ArrayList<>(records.size());
        int half = (records.size() + 1) / 2;
        for (int i = 0; i < half; i++) {
            interleaved.add(records.get(i));
            if (half + i < records.size()) {
                interleaved.add(records.get(half + i));
            }
        }

        int size = interleaved.size();
        for (int i = 0; i < size; i++) {
            int daysAgo = size == 1 ? 0 : 13 - (int) Math.round(i * 13.0 / (size - 1));
            LocalDateTime time = LocalDateTime.now().minusDays(daysAgo).withHour(19).withMinute(30);
            PracticeRecord update = new PracticeRecord();
            update.setId(interleaved.get(i).getId());
            update.setCreateTime(time);
            practiceRecordMapper.updateById(update);
        }
    }
}
