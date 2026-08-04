package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.common.BusinessException;
import com.smartclass.dto.PracticeSubmitDTO;
import com.smartclass.entity.Question;
import com.smartclass.entity.WrongBook;
import com.smartclass.mapper.QuestionMapper;
import com.smartclass.mapper.WrongBookMapper;
import com.smartclass.util.UserContext;
import com.smartclass.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题本服务:查看错题、重做判分、标记掌握
 */
@Service
@RequiredArgsConstructor
public class WrongBookService {

    private final WrongBookMapper wrongBookMapper;
    private final QuestionMapper questionMapper;
    private final QuestionService questionService;
    private final Grader grader;
    private final PracticeRecorder practiceRecorder;

    public List<Map<String, Object>> myList(Long subjectId, Integer mastered) {
        Long studentId = UserContext.getUserId();
        List<WrongBook> wrongBooks = wrongBookMapper.selectList(
                new LambdaQueryWrapper<WrongBook>()
                        .eq(WrongBook::getStudentId, studentId)
                        .eq(mastered != null, WrongBook::getMastered, mastered)
                        .orderByDesc(WrongBook::getLastWrongTime));

        Map<Long, String> knowledgeNames = questionService.knowledgeNames();
        return wrongBooks.stream().map(wrongBook -> {
            Question question = questionMapper.selectById(wrongBook.getQuestionId());
            if (question == null) {
                return null;
            }
            QuestionVO vo = questionService.toVO(question, Map.of(), knowledgeNames, false);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", wrongBook.getId());
            item.put("wrongCount", wrongBook.getWrongCount());
            item.put("rightCount", wrongBook.getRightCount());
            item.put("mastered", wrongBook.getMastered());
            item.put("lastWrongTime", wrongBook.getLastWrongTime());
            item.put("question", vo);
            return item;
        }).filter(Objects::nonNull)
                .filter(item -> subjectId == null
                        || subjectId.equals(((QuestionVO) item.get("question")).getSubjectId()))
                .collect(Collectors.toList());
    }

    /**
     * 错题重做:当场判分并更新错题本状态
     */
    public Map<String, Object> redo(PracticeSubmitDTO dto) {
        Long studentId = UserContext.getUserId();
        Question question = questionMapper.selectById(dto.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        boolean correct = grader.judge(question, dto.getAnswer());
        practiceRecorder.record(studentId, question, correct, "WRONG");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("correct", correct);
        result.put("rightAnswer", question.getAnswer());
        result.put("analysis", question.getAnalysis());
        return result;
    }

    public void markMastered(Long wrongBookId) {
        WrongBook update = new WrongBook();
        update.setId(wrongBookId);
        update.setMastered(1);
        wrongBookMapper.updateById(update);
    }

    public void remove(Long wrongBookId) {
        wrongBookMapper.deleteById(wrongBookId);
    }
}
