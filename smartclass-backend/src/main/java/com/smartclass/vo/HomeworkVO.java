package com.smartclass.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业列表返回对象(教师端/学生端共用,学生端多 myStatus/myScore)
 */
@Data
public class HomeworkVO {

    private Long id;

    private Long courseId;

    private String courseName;

    private String subjectName;

    private Long clazzId;

    private String clazzName;

    private Long teacherId;

    private String teacherName;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer questionCount;

    private Integer totalScore;

    /** 教师端:已提交人数 */
    private Integer submitCount;

    /** 教师端:平均分 */
    private Double avgScore;

    /** 学生端:0 未提交 1 已提交 2 已批改 */
    private Integer myStatus;

    /** 学生端:我的得分 */
    private Integer myScore;
}
