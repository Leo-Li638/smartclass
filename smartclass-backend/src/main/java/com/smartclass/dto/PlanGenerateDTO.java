package com.smartclass.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成学习计划参数
 */
@Data
public class PlanGenerateDTO {

    @NotNull(message = "请选择学科")
    private Long subjectId;

    /** 计划天数,默认 7 天 */
    private Integer days = 7;

    /** 每日题量,默认 10 题 */
    private Integer dailyCount = 10;

    private String goal;
}
