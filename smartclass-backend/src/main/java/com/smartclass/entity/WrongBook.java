package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本表:记录学生在每道题上的累计错误情况
 */
@Data
@TableName("wrong_book")
public class WrongBook {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long questionId;

    /** 累计答错次数 */
    private Integer wrongCount;

    /** 重做答对次数 */
    private Integer rightCount;

    /** 0 未掌握 1 已掌握 */
    private Integer mastered;

    private LocalDateTime lastWrongTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
