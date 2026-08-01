package com.smartclass.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理端数据看板
 */
@Data
public class AdminDashboardVO {

    private Long teacherCount;

    private Long studentCount;

    private Long clazzCount;

    private Long courseCount;

    private Long homeworkCount;

    private Long questionCount;

    private Long practiceCount;

    /** 近 7 天每日做题量 */
    private List<NameValueVO> recentPractice;

    /** 各学科题库题目分布 */
    private List<NameValueVO> subjectQuestionDist;

    /** 各班级学生人数 */
    private List<NameValueVO> clazzStudentDist;
}
