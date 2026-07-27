package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划表:由规则引擎根据薄弱知识点自动生成
 */
@Data
@TableName("study_plan")
public class StudyPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long subjectId;

    private String title;

    private String goal;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 每日建议题量 */
    private Integer dailyCount;

    /** 0 进行中 1 已完成 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
