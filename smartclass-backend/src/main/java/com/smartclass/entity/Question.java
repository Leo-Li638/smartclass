package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库题目表
 * options 存 JSON 数组字符串,如 ["A. 选项内容","B. 选项内容"]
 * answer: 单选存 "A",多选存 "ABD",判断存 "对"/"错",填空存标准答案文本
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private Long knowledgeId;

    /** SINGLE / MULTI / JUDGE / FILL */
    private String type;

    /** 难度 1~5,数字越大越难 */
    private Integer difficulty;

    private String title;

    private String options;

    private String answer;

    private String analysis;

    /** 默认分值 */
    private Integer score;

    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
