package com.smartclass.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 学生提交作业参数
 */
@Data
public class SubmitDTO {

    @NotNull(message = "作业 id 不能为空")
    private Long homeworkId;

    @NotNull(message = "答题内容不能为空")
    private List<AnswerItemDTO> answers;

    @Data
    public static class AnswerItemDTO {

        @NotNull
        private Long questionId;

        private String answer;
    }
}
