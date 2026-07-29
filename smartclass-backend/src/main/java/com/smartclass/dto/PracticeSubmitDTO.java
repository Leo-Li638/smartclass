package com.smartclass.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 自主练习/错题重做提交参数
 */
@Data
public class PracticeSubmitDTO {

    @NotNull(message = "题目 id 不能为空")
    private Long questionId;

    private String answer;

    /** PRACTICE 自主练习 / WRONG 错题重做 */
    private String source;
}
