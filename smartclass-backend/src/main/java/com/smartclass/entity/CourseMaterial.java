package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程学习资料表
 */
@Data
@TableName("course_material")
public class CourseMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String title;

    /** DOC / VIDEO / LINK */
    private String type;

    private String content;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
