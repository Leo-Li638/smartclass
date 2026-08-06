package com.smartclass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartclass.entity.PracticeRecord;
import com.smartclass.entity.Question;
import com.smartclass.entity.WrongBook;
import com.smartclass.mapper.PracticeRecordMapper;
import com.smartclass.mapper.WrongBookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 练习记录组件:统一落练习流水 + 维护错题本
 * 作业提交、自主练习、错题重做三条链路都经过这里,保证薄弱度分析的数据完整
 */
@Component
@RequiredArgsConstructor
public class PracticeRecorder {

    private final PracticeRecordMapper practiceRecordMapper;
    private final WrongBookMapper wrongBookMapper;

    @Transactional(rollbackFor = Exception.class)
    public void record(Long studentId, Question question, boolean correct, String source) {
        PracticeRecord record = new PracticeRecord();
        record.setStudentId(studentId);
        record.setQuestionId(question.getId());
        record.setKnowledgeId(question.getKnowledgeId());
        record.setIsCorrect(correct ? 1 : 0);
        record.setSource(source);
        record.setCreateTime(LocalDateTime.now());
        practiceRecordMapper.insert(record);

        WrongBook wrongBook = wrongBookMapper.selectOne(new LambdaQueryWrapper<WrongBook>()
                .eq(WrongBook::getStudentId, studentId)
                .eq(WrongBook::getQuestionId, question.getId()));

        if (wrongBook == null) {
            if (!correct) {
                WrongBook insert = new WrongBook();
                insert.setStudentId(studentId);
                insert.setQuestionId(question.getId());
                insert.setWrongCount(1);
                insert.setRightCount(0);
                insert.setMastered(0);
                insert.setLastWrongTime(LocalDateTime.now());
                insert.setCreateTime(LocalDateTime.now());
                wrongBookMapper.insert(insert);
            }
            return;
        }

        WrongBook update = new WrongBook();
        update.setId(wrongBook.getId());
        if (correct) {
            update.setRightCount(wrongBook.getRightCount() + 1);
            // 连续答对两次即视为掌握,移出待巩固范围
            if (wrongBook.getRightCount() + 1 >= 2) {
                update.setMastered(1);
            }
        } else {
            update.setWrongCount(wrongBook.getWrongCount() + 1);
            update.setMastered(0);
            update.setLastWrongTime(LocalDateTime.now());
        }
        wrongBookMapper.updateById(update);
    }
}
