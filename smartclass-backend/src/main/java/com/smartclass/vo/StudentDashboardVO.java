package com.smartclass.vo;

import lombok.Data;

import java.util.List;

/**
 * 学生端学习数据看板
 */
@Data
public class StudentDashboardVO {

    private Long totalPractice;

    private Long correctCount;

    private Double correctRate;

    private Long wrongCount;

    private Long unfinishedHomework;

    private Long finishedHomework;

    /** 近 14 天每日正确率 */
    private List<NameValueVO> recentCorrectRate;

    /** 各学科正确率 */
    private List<NameValueVO> subjectCorrectRate;
}
