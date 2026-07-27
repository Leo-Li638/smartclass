package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程表
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long subjectId;

    private String grade;

    private Long teacherId;

    private String description;

    /** 1 上架 0 下架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
