package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.Constants;
import com.smartclass.entity.KnowledgePoint;
import com.smartclass.entity.PracticeRecord;
import com.smartclass.entity.Question;
import com.smartclass.entity.WrongBook;
import com.smartclass.mapper.KnowledgePointMapper;
import com.smartclass.mapper.PracticeRecordMapper;
import com.smartclass.mapper.QuestionMapper;
import com.smartclass.mapper.WrongBookMapper;
import com.smartclass.util.UserContext;
import com.smartclass.vo.KnowledgeStatVO;
import com.smartclass.vo.QuestionVO;
import com.smartclass.vo.RecommendGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推题规则引擎
 *
 * 算法分三步:
 * 1. 掌握度评估 —— 按知识点聚合练习流水,用拉普拉斯平滑计算正确率(避免小样本失真),
 *    再叠加遗忘曲线衰减(每 7 天掌握度衰减 10%)
 * 2. 薄弱度排序 —— weakScore = (1 - 掌握度) × 置信权重,置信权重随做题量增加而趋近 1;
 *    从未练过的知识点给固定薄弱分 0.30,保证新知识有机会进入推荐
 * 3. 分层选题 —— 取最薄弱的前几个知识点,每个知识点内优先推「做错且未掌握」的题,
 *    其次推「没做过」的题,难度按掌握度阶梯匹配,并生成推荐理由
 */
@Service
@RequiredArgsConstructor
public class RecommendService {

    /** 未接触知识点的固定薄弱分 */
    private static final double UNSEEN_WEAK_SCORE = 0.30;
    /** 置信权重平滑系数:做题量越大,统计结论越可信 */
    private static final double CONFIDENCE_K = 5.0;
    /** 拉普拉斯平滑系数 */
    private static final double LAPLACE = 1.0;
    /** 遗忘曲线:每过 7 天掌握度乘以该系数 */
    private static final double DECAY_PER_WEEK = 0.90;

    private final KnowledgePointMapper knowledgeMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final QuestionMapper questionMapper;
    private final QuestionService questionService;
    private final WrongBookMapper wrongBookMapper;

    /**
     * 知识点掌握度分析:某学科下所有知识点的做题统计与薄弱度
     */
    public List<KnowledgeStatVO> analyze(Long subjectId) {
        Long studentId = UserContext.getUserId();

        List<KnowledgePoint> points = knowledgeMapper.selectList(
                new LambdaQueryWrapper<KnowledgePoint>()
                        .eq(subjectId != null, KnowledgePoint::getSubjectId, subjectId));
        List<PracticeRecord> records = practiceRecordMapper.selectList(
                new LambdaQueryWrapper<PracticeRecord>()
                        .eq(PracticeRecord::getStudentId, studentId));
        Map<Long, String> knowledgeNames = points.stream()
                .collect(Collectors.toMap(KnowledgePoint::getId, KnowledgePoint::getName));
        Map<Long, Long> subjectByKnowledge = points.stream()
                .collect(Collectors.toMap(KnowledgePoint::getId, KnowledgePoint::getSubjectId));

        // 只统计属于当前学科知识点的练习记录
        Map<Long, List<PracticeRecord>> byKnowledge = records.stream()
                .filter(r -> subjectByKnowledge.containsKey(r.getKnowledgeId())
                        && (subjectId == null || subjectByKnowledge.get(r.getKnowledgeId()).equals(subjectId)))
                .collect(Collectors.groupingBy(PracticeRecord::getKnowledgeId));

        List<KnowledgeStatVO> stats = new ArrayList<>();
        for (KnowledgePoint point : points) {
            List<PracticeRecord> list = byKnowledge.getOrDefault(point.getId(), List.of());
            KnowledgeStatVO vo = new KnowledgeStatVO();
            vo.setKnowledgeId(point.getId());
            vo.setKnowledgeName(point.getName());
            vo.setTotal(list.size());
            vo.setCorrect((int) list.stream().filter(r -> r.getIsCorrect() == 1).count());
            vo.setAccuracy(list.isEmpty() ? null :
                    vo.getCorrect() * 100.0 / list.size());
            vo.setLastPracticeTime(list.stream()
                    .map(PracticeRecord::getCreateTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null));
            vo.setMastery(mastery(list, vo.getLastPracticeTime()));
            vo.setWeakScore(weakScore(vo));
            stats.add(vo);
        }

        // 没有练过的知识点不计入学科过滤,而是补进结果(仅当查询指定学科时已包含)
        stats.sort(Comparator.comparing(KnowledgeStatVO::getWeakScore).reversed());
        return stats;
    }

    /**
     * 掌握度 = 拉普拉斯平滑正确率 × 时间衰减因子
     */
    private double mastery(List<PracticeRecord> list, LocalDateTime lastTime) {
        if (list.isEmpty()) {
            return 0.0;
        }
        int total = list.size();
        int correct = (int) list.stream().filter(r -> r.getIsCorrect() == 1).count();
        double smoothed = (correct + LAPLACE) / (total + 2 * LAPLACE);
        if (lastTime != null) {
            long days = Duration.between(lastTime, LocalDateTime.now()).toDays();
            smoothed *= Math.pow(DECAY_PER_WEEK, days / 7.0);
        }
        return Math.max(0.0, Math.min(1.0, smoothed));
    }

    /**
     * 薄弱度 = (1 - 掌握度) × 置信权重;从未练习返回固定值
     */
    private double weakScore(KnowledgeStatVO vo) {
        if (vo.getTotal() == 0) {
            return UNSEEN_WEAK_SCORE;
        }
        double confidence = vo.getTotal() / (vo.getTotal() + CONFIDENCE_K);
        return round((1 - vo.getMastery()) * confidence);
    }

    /**
     * 智能推题入口
     */
    public List<RecommendGroupVO> recommend(Long subjectId, Integer questionCount) {
        int perKnowledge = questionCount == null ? 3 : Math.max(1, questionCount);
        List<KnowledgeStatVO> stats = analyze(subjectId);
        Long studentId = UserContext.getUserId();

        // 薄弱度达标的知识点,最多取 5 个
        List<KnowledgeStatVO> weakPoints = stats.stream()
                .filter(s -> s.getWeakScore() >= 0.25)
                .limit(5)
                .toList();
        if (weakPoints.isEmpty()) {
            return List.of();
        }

        // 学生全部错题,用于优先推荐
        Set<Long> wrongQuestionIds = wrongBookMapper.selectList(
                        new LambdaQueryWrapper<WrongBook>()
                                .eq(WrongBook::getStudentId, studentId)
                                .eq(WrongBook::getMastered, 0))
                .stream().map(WrongBook::getQuestionId).collect(Collectors.toSet());

        // 学生做过的题目,用于避开近期重复
        Set<Long> practicedIds = practiceRecordMapper.selectList(
                        new LambdaQueryWrapper<PracticeRecord>()
                                .eq(PracticeRecord::getStudentId, studentId))
                .stream().map(PracticeRecord::getQuestionId).collect(Collectors.toSet());

        Map<Long, String> knowledgeNames = stats.stream()
                .collect(Collectors.toMap(KnowledgeStatVO::getKnowledgeId, KnowledgeStatVO::getKnowledgeName));

        List<RecommendGroupVO> groups = new ArrayList<>();
        for (KnowledgeStatVO stat : weakPoints) {
            List<Question> candidates = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>()
                            .eq(Question::getKnowledgeId, stat.getKnowledgeId())
                            .orderByAsc(Question::getId));
            if (candidates.isEmpty()) {
                continue;
            }
            List<Question> picked = pickQuestions(candidates, stat, wrongQuestionIds,
                    practicedIds, perKnowledge);
            if (picked.isEmpty()) {
                continue;
            }
            Map<Long, String> emptySubject = Map.of();
            List<QuestionVO> questionVos = picked.stream()
                    .map(q -> questionService.toVO(q, emptySubject, knowledgeNames, false))
                    .collect(Collectors.toList());

            RecommendGroupVO group = new RecommendGroupVO();
            group.setKnowledgeId(stat.getKnowledgeId());
            group.setKnowledgeName(stat.getKnowledgeName());
            group.setWeakScore(round(stat.getWeakScore()));
            group.setAccuracy(stat.getAccuracy());
            group.setReason(buildReason(stat));
            group.setQuestions(questionVos);
            groups.add(group);
        }
        return groups;
    }

    /**
     * 单知识点选题策略:错题优先 > 未做过 > 做对过的,难度随掌握度阶梯上升
     */
    private List<Question> pickQuestions(List<Question> candidates, KnowledgeStatVO stat,
                                         Set<Long> wrongIds, Set<Long> practicedIds, int count) {
        int targetDifficulty = stat.getMastery() < 0.4 ? 2
                : stat.getMastery() < 0.7 ? 3 : 4;

        Comparator<Question> byWrongFirst = Comparator
                .comparing((Question q) -> wrongIds.contains(q.getId()) ? 0 : 1)
                .thenComparing(q -> practicedIds.contains(q.getId()) ? 1 : 0)
                .thenComparing(q -> Math.abs(q.getDifficulty() - targetDifficulty))
                .thenComparing(Question::getId);

        return candidates.stream()
                .sorted(byWrongFirst)
                .limit(count)
                .collect(Collectors.toList());
    }

    private String buildReason(KnowledgeStatVO stat) {
        if (stat.getTotal() == 0) {
            return "尚未练习过该知识点,建议先接触基础题目";
        }
        if (stat.getWeakScore() >= 0.5) {
            return String.format("该知识点正确率仅 %.0f%%,掌握度较低,建议优先巩固", stat.getAccuracy());
        }
        if (stat.getLastPracticeTime() != null) {
            long days = Duration.between(stat.getLastPracticeTime(), LocalDateTime.now()).toDays();
            if (days >= 7) {
                return String.format("距上次练习已 %d 天,存在遗忘风险,建议及时复习", days);
            }
        }
        return String.format("该知识点正确率 %.0f%%,仍有一定提升空间", stat.getAccuracy());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
