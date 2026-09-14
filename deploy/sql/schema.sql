-- =============================================================
--  AI 大学生就业服务智能体 —— 数据库结构脚本
--  MySQL 8.0+ / utf8mb4
--  执行方式：mysql -uroot -p < schema.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS `job_agent`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `job_agent`;

-- -------------------------------------------------------------
-- 1. 用户表（四类角色：学生 / 辅导员 / 企业HR / 院系管理员）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`       VARCHAR(32)  NOT NULL COMMENT '登录账号',
    `password`       VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
    `real_name`      VARCHAR(32)           DEFAULT NULL COMMENT '真实姓名',
    `role_code`      VARCHAR(20)  NOT NULL COMMENT 'STUDENT/COUNSELOR/HR/COLLEGE_ADMIN',
    `phone`          VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `email`          VARCHAR(64)           DEFAULT NULL COMMENT '邮箱',
    `avatar`         VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    `college`        VARCHAR(64)           DEFAULT NULL COMMENT '学院',
    `major`          VARCHAR(64)           DEFAULT NULL COMMENT '专业',
    `grade`          VARCHAR(16)           DEFAULT NULL COMMENT '年级',
    `education`      VARCHAR(16)           DEFAULT NULL COMMENT '学历',
    `skills`         VARCHAR(255)          DEFAULT NULL COMMENT '技能标签，逗号分隔',
    `job_intention`  VARCHAR(255)          DEFAULT NULL COMMENT '求职意向',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- -------------------------------------------------------------
-- 2. 简历表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT       NOT NULL COMMENT '所属学生',
    `title`          VARCHAR(128) NOT NULL COMMENT '简历标题',
    `file_name`      VARCHAR(255)          DEFAULT NULL COMMENT '原始文件名',
    `file_path`      VARCHAR(255)          DEFAULT NULL COMMENT '相对存储路径',
    `file_type`      VARCHAR(16)           DEFAULT NULL COMMENT 'pdf/docx/txt',
    `content_text`   LONGTEXT              DEFAULT NULL COMMENT '简历正文（供模型诊断）',
    `target_job`     VARCHAR(128)          DEFAULT NULL COMMENT '目标岗位',
    `score`          INT                   DEFAULT NULL COMMENT '最近一次诊断得分',
    `diagnosis_json` LONGTEXT              DEFAULT NULL COMMENT '最近一次诊断报告 JSON',
    `status`         TINYINT      NOT NULL DEFAULT 1,
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '简历表';

-- -------------------------------------------------------------
-- 3. 岗位表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `job_post`;
CREATE TABLE `job_post` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `title`          VARCHAR(128) NOT NULL COMMENT '岗位名称',
    `company_name`   VARCHAR(128) NOT NULL COMMENT '企业名称',
    `publisher_id`   BIGINT                DEFAULT NULL COMMENT '发布人',
    `job_category`   VARCHAR(32)           DEFAULT NULL COMMENT '岗位类别',
    `city`           VARCHAR(32)           DEFAULT NULL COMMENT '工作城市',
    `salary_min`     INT                   DEFAULT NULL COMMENT '薪资下限',
    `salary_max`     INT                   DEFAULT NULL COMMENT '薪资上限',
    `headcount`      INT                   DEFAULT NULL COMMENT '招聘人数',
    `education_req`  VARCHAR(16)           DEFAULT NULL COMMENT '学历要求',
    `description`    TEXT                  DEFAULT NULL COMMENT '岗位职责',
    `requirement`    TEXT                  DEFAULT NULL COMMENT '任职要求（匹配关键词来源）',
    `deadline`       DATE                  DEFAULT NULL COMMENT '截止日期',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '1招聘中 0已下线',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`job_category`),
    KEY `idx_city` (`city`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '岗位表';

-- -------------------------------------------------------------
-- 4. 岗位申请表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `job_application`;
CREATE TABLE `job_application` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `job_id`      BIGINT      NOT NULL,
    `student_id`  BIGINT      NOT NULL,
    `resume_id`   BIGINT               DEFAULT NULL,
    `match_score` INT                  DEFAULT NULL COMMENT '投递时匹配分',
    `status`      VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED/VIEWED/INTERVIEW/OFFER/REJECTED',
    `remark`      VARCHAR(255)         DEFAULT NULL,
    `apply_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_student` (`job_id`, `student_id`),
    KEY `idx_student` (`student_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '岗位申请表';

-- -------------------------------------------------------------
-- 5. 实习申请表（含流程节点）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `internship_apply`;
CREATE TABLE `internship_apply` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT      NOT NULL,
    `company_name`    VARCHAR(128) NOT NULL COMMENT '实习单位',
    `position`        VARCHAR(128) NOT NULL COMMENT '实习岗位',
    `mentor_id`       BIGINT               DEFAULT NULL COMMENT '企业导师',
    `teacher_id`      BIGINT               DEFAULT NULL COMMENT '校内指导教师',
    `status`          VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/PASSED/REJECTED',
    `current_node`    VARCHAR(32) NOT NULL DEFAULT 'MENTOR_REVIEW' COMMENT '当前流程节点',
    `flow_trace_json` LONGTEXT             DEFAULT NULL COMMENT '流程流转轨迹',
    `start_date`      DATE                 DEFAULT NULL,
    `end_date`        DATE                 DEFAULT NULL,
    `self_score`      INT                  DEFAULT NULL COMMENT '学生自评',
    `mentor_score`    INT                  DEFAULT NULL COMMENT '企业导师评价',
    `teacher_score`   INT                  DEFAULT NULL COMMENT '校方评定',
    `final_score`     INT                  DEFAULT NULL COMMENT '加权汇总分',
    `remark`          VARCHAR(255)         DEFAULT NULL,
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_node` (`current_node`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实习申请表';

-- -------------------------------------------------------------
-- 6. 实习日志表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `internship_log`;
CREATE TABLE `internship_log` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `apply_id`        BIGINT      NOT NULL,
    `student_id`      BIGINT      NOT NULL,
    `week_no`         INT                  DEFAULT NULL COMMENT '第几周',
    `log_date`        DATE                 DEFAULT NULL,
    `content`         TEXT                 DEFAULT NULL COMMENT '日志正文',
    `teacher_comment` VARCHAR(500)         DEFAULT NULL COMMENT '辅导员批阅留言',
    `score`           INT                  DEFAULT NULL COMMENT '辅导员评分',
    `ai_review_json`  LONGTEXT             DEFAULT NULL COMMENT '智能体点评',
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_apply` (`apply_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实习日志表';

-- -------------------------------------------------------------
-- 7. 面试训练表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `interview_session`;
CREATE TABLE `interview_session` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT,
    `student_id`     BIGINT      NOT NULL,
    `category`       VARCHAR(32)          DEFAULT NULL COMMENT '技术岗/管理岗/综合素质',
    `difficulty`     VARCHAR(16)          DEFAULT NULL COMMENT 'EASY/NORMAL/HARD',
    `questions_json` LONGTEXT             DEFAULT NULL COMMENT '题目列表',
    `answers_json`   LONGTEXT             DEFAULT NULL COMMENT '作答与逐题评估',
    `report_json`    LONGTEXT             DEFAULT NULL COMMENT '训练报告',
    `total_score`    INT                  DEFAULT NULL,
    `status`         VARCHAR(16) NOT NULL DEFAULT 'RUNNING',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_student` (`student_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '面试训练表';

-- -------------------------------------------------------------
-- 8. 就业政策表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `policy_item`;
CREATE TABLE `policy_item` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `title`        VARCHAR(200) NOT NULL COMMENT '政策标题',
    `category`     VARCHAR(32)           DEFAULT NULL COMMENT '分类',
    `region`       VARCHAR(32)           DEFAULT NULL COMMENT '适用地区',
    `publish_org`  VARCHAR(128)          DEFAULT NULL COMMENT '发布单位',
    `publish_date` DATE                  DEFAULT NULL,
    `content`      LONGTEXT              DEFAULT NULL COMMENT '政策正文',
    `source_url`   VARCHAR(255)          DEFAULT NULL,
    `tags`         VARCHAR(255)          DEFAULT NULL COMMENT '检索标签',
    `view_count`   INT          NOT NULL DEFAULT 0,
    `status`       TINYINT      NOT NULL DEFAULT 1,
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '就业政策表';
