package com.smartclass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建/编辑作业参数
 */
@Data
public class HomeworkSaveDTO {

    private Long id;

    @NotNull(message = "请选择课程")
    private Long courseId;

    @NotNull(message = "请选择班级")
    private Long clazzId;

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0 保存草稿 1 立即发布 */
    private Integer publish;

    /** 题目及分值 */
    @NotEmpty(message = "请至少选择一道题目")
    private List<HomeworkItemDTO> questions;

    @Data
    public static class HomeworkItemDTO {

        @NotNull(message = "题目 id 不能为空")
        private Long questionId;

        private Integer score;
    }
}
