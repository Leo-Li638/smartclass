package com.smartclass.dto;

import lombok.Data;

/**
 * 题目分页查询参数
 */
@Data
public class QuestionQueryDTO {

    private Long current = 1L;

    private Long size = 10L;

    private Long subjectId;

    private Long knowledgeId;

    private String type;

    private Integer difficulty;

    private String keyword;
}
