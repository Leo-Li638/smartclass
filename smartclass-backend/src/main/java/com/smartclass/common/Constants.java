package com.smartclass.common;

/**
 * 系统常量:角色与通用状态定义
 */
public interface Constants {

    String ROLE_ADMIN = "ADMIN";
    String ROLE_TEACHER = "TEACHER";
    String ROLE_STUDENT = "STUDENT";

    Integer STATUS_ENABLE = 1;
    Integer STATUS_DISABLE = 0;

    /** 题目类型:单选/多选/判断/填空 */
    String QUESTION_SINGLE = "SINGLE";
    String QUESTION_MULTI = "MULTI";
    String QUESTION_JUDGE = "JUDGE";
    String QUESTION_FILL = "FILL";

    /** 作业提交状态:0未提交 1已提交 2已批改 */
    Integer SUBMIT_NOT = 0;
    Integer SUBMIT_DONE = 1;
    Integer SUBMIT_GRADED = 2;

    /** 学习计划状态:0进行中 1已完成 */
    Integer PLAN_RUNNING = 0;
    Integer PLAN_FINISHED = 1;
}
