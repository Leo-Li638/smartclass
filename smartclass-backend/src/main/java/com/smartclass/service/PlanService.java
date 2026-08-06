package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.BusinessException;
import com.smartclass.dto.PlanGenerateDTO;
import com.smartclass.entity.PlanTask;
import com.smartclass.entity.PracticeRecord;
import com.smartclass.entity.StudyPlan;
import com.smartclass.entity.Subject;
import com.smartclass.mapper.PlanTaskMapper;
import com.smartclass.mapper.PracticeRecordMapper;
import com.smartclass.mapper.StudyPlanMapper;
import com.smartclass.mapper.SubjectMapper;
import com.smartclass.util.UserContext;
import com.smartclass.vo.KnowledgeStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习计划服务
 *
 * 生成逻辑:取薄弱度最高的知识点,按薄弱度占比把「总题量」分配到各知识点,
 * 再均匀铺到每一天,生成按天推进的任务清单。
 * 任务进度不靠手动打卡,而是实时从练习流水里统计当天该知识点的实际做题数。
 */
@Service
@RequiredArgsConstructor
public class PlanService {

    private final StudyPlanMapper studyPlanMapper;
    private final PlanTaskMapper planTaskMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final SubjectMapper subjectMapper;
    private final RecommendService recommendService;

    @Transactional(rollbackFor = Exception.class)
    public StudyPlan generate(PlanGenerateDTO dto) {
        Long studentId = UserContext.getUserId();
        int days = dto.getDays() == null ? 7 : Math.min(Math.max(dto.getDays(), 3), 30);
        int dailyCount = dto.getDailyCount() == null ? 10 : Math.min(Math.max(dto.getDailyCount(), 4), 30);

        List<KnowledgeStatVO> stats = recommendService.analyze(dto.getSubjectId());
        List<KnowledgeStatVO> weakPoints = stats.stream()
                .filter(s -> s.getWeakScore() >= 0.25)
                .limit(5)
                .toList();
        if (weakPoints.isEmpty()) {
            throw new BusinessException("该学科暂无薄弱知识点或题库为空,无需生成计划");
        }

        Subject subject = subjectMapper.selectById(dto.getSubjectId());
        StudyPlan plan = new StudyPlan();
        plan.setStudentId(studentId);
        plan.setSubjectId(dto.getSubjectId());
        plan.setTitle((subject == null ? "自定义" : subject.getName()) + "薄弱知识点专项提升计划");
        plan.setGoal(dto.getGoal() == null || dto.getGoal().isBlank()
                ? "针对 " + weakPoints.size() + " 个薄弱知识点集中突破,提升整体正确率"
                : dto.getGoal());
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(days - 1L));
        plan.setDailyCount(dailyCount);
        plan.setStatus(0);
        plan.setCreateTime(LocalDateTime.now());
        studyPlanMapper.insert(plan);

        // 总题量按薄弱度加权分配
        double weakSum = weakPoints.stream().mapToDouble(KnowledgeStatVO::getWeakScore).sum();
        int totalCount = days * dailyCount;
        List<PlanTask> tasks = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int day = 0; day < days; day++) {
            LocalDate taskDate = today.plusDays(day);
            int assigned = 0;
            for (KnowledgeStatVO stat : weakPoints) {
                int share = (int) Math.round(totalCount * stat.getWeakScore() / weakSum / days);
                if (share <= 0) {
                    continue;
                }
                PlanTask task = new PlanTask();
                task.setPlanId(plan.getId());
                task.setKnowledgeId(stat.getKnowledgeId());
                task.setTaskDate(taskDate);
                task.setContent("巩固《" + stat.getKnowledgeName() + "》: 完成 " + share + " 题");
                task.setQuestionCount(share);
                task.setDoneCount(0);
                task.setStatus(0);
                tasks.add(task);
                assigned += share;
            }
            if (assigned == 0) {
                // 薄弱度太低导致取整后为 0 时,保底安排一个知识点
                KnowledgeStatVO stat = weakPoints.get(0);
                PlanTask task = new PlanTask();
                task.setPlanId(plan.getId());
                task.setKnowledgeId(stat.getKnowledgeId());
                task.setTaskDate(taskDate);
                task.setContent("巩固《" + stat.getKnowledgeName() + "》: 完成 1 题");
                task.setQuestionCount(1);
                task.setDoneCount(0);
                task.setStatus(0);
                tasks.add(task);
            }
        }
        tasks.forEach(planTaskMapper::insert);
        return plan;
    }

    public List<Map<String, Object>> myPlans() {
        Long studentId = UserContext.getUserId();
        List<StudyPlan> plans = studyPlanMapper.selectList(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getStudentId, studentId)
                        .orderByDesc(StudyPlan::getId));
        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));

        return plans.stream().map(plan -> {
            List<PlanTask> tasks = planTaskMapper.selectList(
                    new LambdaQueryWrapper<PlanTask>()
                            .eq(PlanTask::getPlanId, plan.getId())
                            .orderByAsc(PlanTask::getTaskDate));
            refreshProgress(plan, tasks);
            int totalQuestions = tasks.stream().mapToInt(PlanTask::getQuestionCount).sum();
            int doneQuestions = tasks.stream().mapToInt(PlanTask::getDoneCount).sum();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", plan.getId());
            item.put("subjectName", subjectNames.get(plan.getSubjectId()));
            item.put("title", plan.getTitle());
            item.put("goal", plan.getGoal());
            item.put("startDate", plan.getStartDate());
            item.put("endDate", plan.getEndDate());
            item.put("dailyCount", plan.getDailyCount());
            item.put("status", plan.getStatus());
            item.put("taskCount", tasks.size());
            item.put("finishedTaskCount", tasks.stream().filter(t -> t.getStatus() == 2).count());
            item.put("totalQuestions", totalQuestions);
            item.put("doneQuestions", doneQuestions);
            item.put("progress", totalQuestions == 0 ? 0
                    : Math.round(doneQuestions * 1000.0 / totalQuestions) / 10.0);
            return item;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> planDetail(Long planId) {
        StudyPlan plan = studyPlanMapper.selectById(planId);
        if (plan == null || !plan.getId().equals(planId)) {
            throw new BusinessException("计划不存在");
        }
        List<PlanTask> tasks = planTaskMapper.selectList(
                new LambdaQueryWrapper<PlanTask>()
                        .eq(PlanTask::getPlanId, planId)
                        .orderByAsc(PlanTask::getTaskDate));
        refreshProgress(plan, tasks);

        Map<Long, String> knowledgeNames = recommendService.analyze(plan.getSubjectId()).stream()
                .collect(Collectors.toMap(KnowledgeStatVO::getKnowledgeId, KnowledgeStatVO::getKnowledgeName));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", plan.getId());
        result.put("title", plan.getTitle());
        result.put("goal", plan.getGoal());
        result.put("subjectId", plan.getSubjectId());
        result.put("startDate", plan.getStartDate());
        result.put("endDate", plan.getEndDate());
        result.put("status", plan.getStatus());
        result.put("tasks", tasks.stream().map(task -> {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("id", task.getId());
            t.put("taskDate", task.getTaskDate());
            t.put("knowledgeId", task.getKnowledgeId());
            t.put("knowledgeName", knowledgeNames.getOrDefault(task.getKnowledgeId(), ""));
            t.put("content", task.getContent());
            t.put("questionCount", task.getQuestionCount());
            t.put("doneCount", task.getDoneCount());
            t.put("status", task.getStatus());
            return t;
        }).collect(Collectors.toList()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long planId) {
        planTaskMapper.delete(new LambdaQueryWrapper<PlanTask>()
                .eq(PlanTask::getPlanId, planId));
        studyPlanMapper.deleteById(planId);
    }

    /**
     * 用练习流水刷新每个任务的完成数与状态,全部完成时计划置为已完成
     */
    private void refreshProgress(StudyPlan plan, List<PlanTask> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        List<PracticeRecord> records = practiceRecordMapper.selectList(
                new LambdaQueryWrapper<PracticeRecord>()
                        .eq(PracticeRecord::getStudentId, plan.getStudentId())
                        .ge(PracticeRecord::getCreateTime, plan.getStartDate().atStartOfDay()));

        Map<String, Integer> countMap = new HashMap<>();
        for (PracticeRecord record : records) {
            if (record.getKnowledgeId() == null || record.getCreateTime() == null) {
                continue;
            }
            String key = record.getKnowledgeId() + "@" + record.getCreateTime().toLocalDate();
            countMap.merge(key, 1, Integer::sum);
        }

        boolean allDone = true;
        for (PlanTask task : tasks) {
            int done = countMap.getOrDefault(task.getKnowledgeId() + "@" + task.getTaskDate(), 0);
            if (done != task.getDoneCount() || statusOf(task, done) != task.getStatus()) {
                task.setDoneCount(done);
                task.setStatus(statusOf(task, done));
                planTaskMapper.updateById(task);
            }
            allDone &= task.getStatus() == 2;
        }
        if (allDone && plan.getStatus() == 0) {
            StudyPlan update = new StudyPlan();
            update.setId(plan.getId());
            update.setStatus(1);
            studyPlanMapper.updateById(update);
            plan.setStatus(1);
        }
    }

    private int statusOf(PlanTask task, int done) {
        if (done >= task.getQuestionCount()) {
            return 2;
        }
        return done > 0 ? 1 : 0;
    }
}
