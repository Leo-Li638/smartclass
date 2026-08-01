package com.smartclass.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点掌握度统计:规则引擎的核心输出
 */
@Data
public class KnowledgeStatVO {

    private Long knowledgeId;

    private String knowledgeName;

    /** 做题总数 */
    private Integer total;

    /** 答对次数 */
    private Integer correct;

    /** 原始正确率(百分数,0~100,如 46.15 表示 46.15%) */
    private Double accuracy;

    /** 掌握度(0~1,经拉普拉斯平滑 + 遗忘曲线衰减) */
    private Double mastery;

    /** 薄弱度得分(0~1,越大越薄弱) */
    private Double weakScore;

    /** 最近一次练习时间 */
    private LocalDateTime lastPracticeTime;
}
