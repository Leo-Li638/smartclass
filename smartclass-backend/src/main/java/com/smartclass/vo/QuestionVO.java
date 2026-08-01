package com.smartclass.vo;

import com.smartclass.entity.Question;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目返回对象:实体字段 + 关联名称 + 解析后的选项列表
 */
@Data
public class QuestionVO {

    private Long id;

    private Long subjectId;

    private String subjectName;

    private Long knowledgeId;

    private String knowledgeName;

    private String type;

    private Integer difficulty;

    private String title;

    private List<String> options;

    /** 学生端做题时不返回答案与解析,由 mark 控制是否下发 */
    private String answer;

    private String analysis;

    private Integer score;

    private LocalDateTime createTime;
}
