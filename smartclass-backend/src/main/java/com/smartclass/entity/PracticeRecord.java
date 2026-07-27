package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习记录表:作业答题与自主练习都会落一条记录,供薄弱度分析使用
 */
@Data
@TableName("practice_record")
public class PracticeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long questionId;

    private Long knowledgeId;

    /** 1 正确 0 错误 */
    private Integer isCorrect;

    /** 来源:HOMEWORK 作业 / PRACTICE 自主练习 / WRONG 重做错题 */
    private String source;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
