package com.smartclass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 新增/编辑题目参数
 */
@Data
public class QuestionSaveDTO {

    private Long id;

    @NotNull(message = "请选择学科")
    private Long subjectId;

    @NotNull(message = "请选择知识点")
    private Long knowledgeId;

    @NotBlank(message = "请选择题型")
    private String type;

    @NotNull(message = "请选择难度")
    private Integer difficulty;

    @NotBlank(message = "题干不能为空")
    private String title;

    /** 选择题选项列表,如 ["A. xxx","B. xxx"] */
    private List<String> options;

    @NotBlank(message = "标准答案不能为空")
    private String answer;

    private String analysis;

    private Integer score;
}
