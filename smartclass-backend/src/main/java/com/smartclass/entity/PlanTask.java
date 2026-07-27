package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学习计划每日任务表
 */
@Data
@TableName("plan_task")
public class PlanTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long knowledgeId;

    private LocalDate taskDate;

    private String content;

    /** 计划题量 */
    private Integer questionCount;

    /** 已完成题量 */
    private Integer doneCount;

    /** 0 未开始 1 进行中 2 已完成 */
    private Integer status;
}
