package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级表
 */
@Data
@TableName("clazz")
public class Clazz {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 年级:如 初一/初二/高一 */
    private String grade;

    private Long headTeacherId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
