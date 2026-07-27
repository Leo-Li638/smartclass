package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 作业答题明细表:每道题的作答与判分结果
 */
@Data
@TableName("homework_answer")
public class HomeworkAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long submitId;

    private Long questionId;

    private String answer;

    /** 1 正确 0 错误 */
    private Integer isCorrect;

    private Integer score;

    private Integer questionScore;
}
