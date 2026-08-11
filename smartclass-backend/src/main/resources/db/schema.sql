-- 智学云课堂数据库表结构
-- 幂等脚本:重复执行不会破坏已有数据

CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(100) NOT NULL,
    `real_name`   VARCHAR(50),
    `role`        VARCHAR(20)  NOT NULL COMMENT 'ADMIN/TEACHER/STUDENT',
    `clazz_id`    BIGINT,
    `phone`       VARCHAR(20),
    `email`       VARCHAR(100),
    `gender`      VARCHAR(10),
    `status`      TINYINT DEFAULT 1 COMMENT '1启用 0停用',
    `create_time` DATETIME,
    `update_time` DATETIME,
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '用户表';

CREATE TABLE IF NOT EXISTS `clazz` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`            VARCHAR(50) NOT NULL,
    `grade`           VARCHAR(20),
    `head_teacher_id` BIGINT,
    `create_time`     DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '班级表';

CREATE TABLE IF NOT EXISTS `subject` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(50) NOT NULL,
    `stage`       VARCHAR(20) COMMENT '小学/初中/高中',
    `create_time` DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '学科表';

CREATE TABLE IF NOT EXISTS `course` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(100) NOT NULL,
    `subject_id`  BIGINT,
    `grade`       VARCHAR(20),
    `teacher_id`  BIGINT,
    `description` VARCHAR(500),
    `status`      TINYINT DEFAULT 1 COMMENT '1上架 0下架',
    `create_time` DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '课程表';

CREATE TABLE IF NOT EXISTS `course_material` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id`   BIGINT NOT NULL,
    `title`       VARCHAR(200) NOT NULL,
    `type`        VARCHAR(20) COMMENT 'DOC/VIDEO/LINK',
    `content`     TEXT,
    `sort`        INT DEFAULT 1,
    `create_time` DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '课程学习资料表';

CREATE TABLE IF NOT EXISTS `knowledge_point` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `subject_id`  BIGINT NOT NULL,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(500)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '知识点表';

CREATE TABLE IF NOT EXISTS `question` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `subject_id`   BIGINT      NOT NULL,
    `knowledge_id` BIGINT      NOT NULL,
    `type`         VARCHAR(20) NOT NULL COMMENT 'SINGLE/MULTI/JUDGE/FILL',
    `difficulty`   TINYINT DEFAULT 3 COMMENT '难度1~5',
    `title`        TEXT        NOT NULL,
    `options`      VARCHAR(1000),
    `answer`       VARCHAR(200) NOT NULL,
    `analysis`     TEXT,
    `score`        INT DEFAULT 5,
    `creator_id`   BIGINT,
    `create_time`  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '题库题目表';

CREATE TABLE IF NOT EXISTS `homework` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `course_id`    BIGINT NOT NULL,
    `clazz_id`     BIGINT NOT NULL,
    `teacher_id`   BIGINT NOT NULL,
    `title`        VARCHAR(200) NOT NULL,
    `description`  VARCHAR(1000),
    `start_time`   DATETIME,
    `end_time`     DATETIME,
    `status`       TINYINT DEFAULT 0 COMMENT '0草稿 1已发布',
    `create_time`  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '作业表';

CREATE TABLE IF NOT EXISTS `homework_question` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `homework_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `sort`        INT DEFAULT 1,
    `score`       INT DEFAULT 5
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '作业题目关联表';

CREATE TABLE IF NOT EXISTS `homework_submit` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `homework_id`  BIGINT NOT NULL,
    `student_id`   BIGINT NOT NULL,
    `status`       TINYINT DEFAULT 0 COMMENT '0未提交 1已提交 2已批改',
    `score`        INT,
    `total_score`  INT,
    `submit_time`  DATETIME,
    `comment`      VARCHAR(500),
    `create_time`  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '作业提交表';

CREATE TABLE IF NOT EXISTS `homework_answer` (
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY,
    `submit_id`      BIGINT NOT NULL,
    `question_id`    BIGINT NOT NULL,
    `answer`         VARCHAR(500),
    `is_correct`     TINYINT,
    `score`          INT,
    `question_score` INT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '作业答题明细表';

CREATE TABLE IF NOT EXISTS `wrong_book` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`      BIGINT NOT NULL,
    `question_id`     BIGINT NOT NULL,
    `wrong_count`     INT DEFAULT 1,
    `right_count`     INT DEFAULT 0,
    `mastered`        TINYINT DEFAULT 0 COMMENT '0未掌握 1已掌握',
    `last_wrong_time` DATETIME,
    `create_time`     DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '错题本表';

CREATE TABLE IF NOT EXISTS `practice_record` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`   BIGINT NOT NULL,
    `question_id`  BIGINT NOT NULL,
    `knowledge_id` BIGINT,
    `is_correct`   TINYINT,
    `source`       VARCHAR(20) COMMENT 'HOMEWORK/PRACTICE/WRONG',
    `create_time`  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '练习记录表';

CREATE TABLE IF NOT EXISTS `study_plan` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id`  BIGINT NOT NULL,
    `subject_id`  BIGINT NOT NULL,
    `title`       VARCHAR(200),
    `goal`        VARCHAR(500),
    `start_date`  DATE,
    `end_date`    DATE,
    `daily_count` INT DEFAULT 10,
    `status`      TINYINT DEFAULT 0 COMMENT '0进行中 1已完成',
    `create_time` DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '学习计划表';

CREATE TABLE IF NOT EXISTS `plan_task` (
    `id`             BIGINT AUTO_INCREMENT PRIMARY KEY,
    `plan_id`        BIGINT NOT NULL,
    `knowledge_id`   BIGINT,
    `task_date`      DATE,
    `content`        VARCHAR(500),
    `question_count` INT,
    `done_count`     INT DEFAULT 0,
    `status`         TINYINT DEFAULT 0 COMMENT '0未开始 1进行中 2已完成'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '学习计划每日任务表';

CREATE TABLE IF NOT EXISTS `notice` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title`        VARCHAR(200) NOT NULL,
    `content`      TEXT,
    `target_role`  VARCHAR(20) DEFAULT 'ALL' COMMENT 'ALL/TEACHER/STUDENT',
    `publisher_id` BIGINT,
    `create_time`  DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '系统公告表';
