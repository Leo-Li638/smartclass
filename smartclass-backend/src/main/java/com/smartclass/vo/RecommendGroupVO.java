package com.smartclass.vo;

import lombok.Data;

import java.util.List;

/**
 * 智能推题分组:一个薄弱知识点 + 推荐题目 + 推荐理由
 */
@Data
public class RecommendGroupVO {

    private Long knowledgeId;

    private String knowledgeName;

    private Double weakScore;

    private Double accuracy;

    /** 推荐理由,如"该知识点正确率 45%,建议优先巩固" */
    private String reason;

    private List<QuestionVO> questions;
}
