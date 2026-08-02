package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartclass.common.BusinessException;
import com.smartclass.common.Constants;
import com.smartclass.dto.QuestionQueryDTO;
import com.smartclass.dto.QuestionSaveDTO;
import com.smartclass.entity.HomeworkQuestion;
import com.smartclass.entity.KnowledgePoint;
import com.smartclass.entity.Question;
import com.smartclass.entity.Subject;
import com.smartclass.mapper.HomeworkQuestionMapper;
import com.smartclass.mapper.KnowledgePointMapper;
import com.smartclass.mapper.QuestionMapper;
import com.smartclass.mapper.SubjectMapper;
import com.smartclass.util.JsonUtil;
import com.smartclass.util.UserContext;
import com.smartclass.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题库服务:题目的增删改查,选项以 JSON 数组存储
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgeMapper;
    private final HomeworkQuestionMapper homeworkQuestionMapper;

    public Page<QuestionVO> page(QuestionQueryDTO dto) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(dto.getSubjectId() != null, Question::getSubjectId, dto.getSubjectId())
                .eq(dto.getKnowledgeId() != null, Question::getKnowledgeId, dto.getKnowledgeId())
                .eq(StringUtils.hasText(dto.getType()), Question::getType, dto.getType())
                .eq(dto.getDifficulty() != null, Question::getDifficulty, dto.getDifficulty())
                .like(StringUtils.hasText(dto.getKeyword()), Question::getTitle, dto.getKeyword())
                .orderByDesc(Question::getId);

        Page<Question> page = questionMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);
        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        Map<Long, String> knowledgeNames = knowledgeNames();

        List<QuestionVO> records = page.getRecords().stream()
                .map(question -> toVO(question, subjectNames, knowledgeNames, true))
                .collect(Collectors.toList());
        Page<QuestionVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 按学科列出全部题目(教师创建作业选题用)
     */
    public List<QuestionVO> listBySubject(Long subjectId) {
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(subjectId != null, Question::getSubjectId, subjectId)
                .orderByDesc(Question::getId));
        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        Map<Long, String> knowledgeNames = knowledgeNames();
        return questions.stream()
                .map(question -> toVO(question, subjectNames, knowledgeNames, true))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(QuestionSaveDTO dto) {
        Question question = new Question();
        BeanUtils.copyProperties(dto, question);
        question.setOptions(JsonUtil.writeList(dto.getOptions()));
        question.setScore(dto.getScore() == null ? 5 : dto.getScore());
        if (Constants.QUESTION_JUDGE.equals(dto.getType()) && dto.getOptions() == null) {
            question.setOptions(JsonUtil.writeList(List.of("对", "错")));
        }
        if (dto.getId() == null) {
            question.setCreatorId(UserContext.getUserId());
            questionMapper.insert(question);
        } else {
            questionMapper.updateById(question);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long used = homeworkQuestionMapper.selectCount(new LambdaQueryWrapper<HomeworkQuestion>()
                .eq(HomeworkQuestion::getQuestionId, id));
        if (used > 0) {
            throw new BusinessException("该题目已被 " + used + " 份作业引用,请先移除后再删除");
        }
        questionMapper.deleteById(id);
    }

    public QuestionVO detail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        Map<Long, String> subjectNames = subjectMapper.selectList(null).stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));
        return toVO(question, subjectNames, knowledgeNames(), true);
    }

    public QuestionVO toVO(Question question, Map<Long, String> subjectNames,
                           Map<Long, String> knowledgeNames, boolean withAnswer) {
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        vo.setSubjectName(subjectNames.get(question.getSubjectId()));
        vo.setKnowledgeName(knowledgeNames.get(question.getKnowledgeId()));
        vo.setOptions(JsonUtil.parseList(question.getOptions()));
        if (!withAnswer) {
            vo.setAnswer(null);
            vo.setAnalysis(null);
        }
        return vo;
    }

    public Map<Long, String> knowledgeNames() {
        return knowledgeMapper.selectList(null).stream()
                .collect(Collectors.toMap(KnowledgePoint::getId, KnowledgePoint::getName));
    }

    public List<KnowledgePoint> knowledgeList(Long subjectId) {
        return knowledgeMapper.selectList(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(subjectId != null, KnowledgePoint::getSubjectId, subjectId)
                .orderByAsc(KnowledgePoint::getId));
    }

    public void saveKnowledge(KnowledgePoint knowledgePoint) {
        if (knowledgePoint.getId() == null) {
            knowledgeMapper.insert(knowledgePoint);
        } else {
            knowledgeMapper.updateById(knowledgePoint);
        }
    }
}
