package com.smartclass.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交明细:教师批阅 + 学生查看成绩共用
 */
@Data
public class SubmitDetailVO {

    private Long submitId;

    private Long homeworkId;

    private String homeworkTitle;

    private String studentName;

    private String clazzName;

    private Integer status;

    private Integer score;

    private Integer totalScore;

    private LocalDateTime submitTime;

    private String comment;

    private List<AnswerDetailVO> answers;

    @Data
    public static class AnswerDetailVO {

        private Long questionId;

        private String title;

        private String type;

        private List<String> options;

        private String myAnswer;

        private String rightAnswer;

        private String analysis;

        private Integer isCorrect;

        private Integer score;

        private Integer questionScore;
    }
}
