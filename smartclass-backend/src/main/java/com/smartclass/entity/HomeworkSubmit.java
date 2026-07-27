package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交记录表
 */
@Data
@TableName("homework_submit")
public class HomeworkSubmit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homeworkId;

    private Long studentId;

    /** 0 未提交 1 已提交 2 已批改 */
    private Integer status;

    private Integer score;

    private Integer totalScore;

    private LocalDateTime submitTime;

    /** 教师评语 */
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
