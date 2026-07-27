package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 作业-题目关联表
 */
@Data
@TableName("homework_question")
public class HomeworkQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homeworkId;

    private Long questionId;

    private Integer sort;

    private Integer score;
}
