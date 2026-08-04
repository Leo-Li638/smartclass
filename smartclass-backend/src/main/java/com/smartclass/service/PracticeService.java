package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.BusinessException;
import com.smartclass.dto.PracticeSubmitDTO;
import com.smartclass.entity.Question;
import com.smartclass.mapper.QuestionMapper;
import com.smartclass.util.UserContext;
import com.smartclass.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自主练习服务:按学科/难度抽题组卷,逐题提交判分
 */
@Service
@RequiredArgsConstructor
public class PracticeService {

    private final QuestionMapper questionMapper;
    private final QuestionService questionService;
    private final Grader grader;
    private final PracticeRecorder practiceRecorder;

    public List<QuestionVO> start(Long subjectId, Integer difficulty, Integer count) {
        int size = count == null ? 10 : Math.min(Math.max(count, 1), 50);
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(subjectId != null, Question::getSubjectId, subjectId)
                        .eq(difficulty != null, Question::getDifficulty, difficulty)
                        .orderByDesc(Question::getId));
        if (questions.size() > size) {
            // 简单打散:取最新 3 倍题量后随机抽样,保证每次练习组合不同
            List<Question> pool = questions.subList(0, Math.min(questions.size(), size * 3));
            java.util.Collections.shuffle(pool);
            questions = pool.subList(0, size);
        }
        Map<Long, String> knowledgeNames = questionService.knowledgeNames();
        return questions.stream()
                .map(q -> questionService.toVO(q, Map.of(), knowledgeNames, false))
                .collect(Collectors.toList());
    }

    /**
     * 提交单题答案:立即返回对错与解析,并记录练习流水
     */
    public Map<String, Object> submit(PracticeSubmitDTO dto) {
        Long studentId = UserContext.getUserId();
        Question question = questionMapper.selectById(dto.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        boolean correct = grader.judge(question, dto.getAnswer());
        practiceRecorder.record(studentId, question, correct,
                dto.getSource() == null ? "PRACTICE" : dto.getSource());

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("correct", correct);
        result.put("rightAnswer", question.getAnswer());
        result.put("analysis", question.getAnalysis());
        return result;
    }
}
