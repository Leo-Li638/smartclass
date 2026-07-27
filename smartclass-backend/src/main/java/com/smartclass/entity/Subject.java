package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学科表
 */
@Data
@TableName("subject")
public class Subject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 适用学段:小学/初中/高中 */
    private String stage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
