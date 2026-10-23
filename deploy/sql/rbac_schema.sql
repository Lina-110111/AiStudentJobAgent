-- =============================================================
--  成员2 · 用户与权限模块 —— 权限相关 SQL（对齐《需求分析文档 v2.2》）
--  说明：团队数据模型为 8 张表 / 116 个数据项，权限【无需新增任何表】，
--        完全复用 sys_user.role_code（RoleCodeEnum 枚举）。
--  执行方式：mysql -uroot -p job_agent < rbac_schema.sql
--  注意：本脚本不建表，只做「角色初始化 + 权限查询示例」，可重复执行。
-- =============================================================
USE `job_agent`;

-- -------------------------------------------------------------
-- 1. 演示账号角色初始化（幂等，可重复执行）
--    与 README 演示账号一致：student01 / counselor01 / hr01 / admin01
-- -------------------------------------------------------------
UPDATE sys_user SET role_code = 'STUDENT'       WHERE username = 'student01';
UPDATE sys_user SET role_code = 'COUNSELOR'     WHERE username = 'counselor01';
UPDATE sys_user SET role_code = 'HR'            WHERE username = 'hr01';
UPDATE sys_user SET role_code = 'COLLEGE_ADMIN' WHERE username = 'admin01';

-- -------------------------------------------------------------
-- 2. 按角色查询用户（管理 / 验收用）
-- -------------------------------------------------------------
-- SELECT id, username, real_name, role_code, status FROM sys_user ORDER BY role_code;

-- -------------------------------------------------------------
-- 3. 权限落地说明（供验收对照，不是建表语句）
--    角色编码取值：STUDENT / COUNSELOR / HR / COLLEGE_ADMIN
--    数据范围：    学生=仅本人 / 辅导员=所辖班级 / HR=本企业 / 管理员=全院
--    鉴权：        接口级校验角色 + 数据归属字段过滤（student_id/publisher_id 等）
-- -------------------------------------------------------------

-- =============================================================
--  附：sys_user 表结构确认（已存在于 schema.sql，此处不重复建表）
--  关键字段：
--    id          BIGINT  PK
--    username    VARCHAR(32) UK
--    password    VARCHAR(100) BCrypt
--    real_name   VARCHAR(32)
--    role_code   VARCHAR(20)   -- 角色枚举 STUDENT/COUNSELOR/HR/COLLEGE_ADMIN
--    status      TINYINT       -- 1启用 0停用（FR-1.4 账号状态管理）
--    deleted     TINYINT       -- 逻辑删除
-- =============================================================
