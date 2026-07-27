package com.smartclass.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 知识点表:题目与薄弱度分析都以知识点为粒度
 */
@Data
@TableName("knowledge_point")
public class KnowledgePoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private String name;

    private String description;
}
