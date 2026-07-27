package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业表
 */
@Data
@TableName("homework")
public class Homework {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long clazzId;

    private Long teacherId;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0 草稿 1 已发布 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
