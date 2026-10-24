# 10 · 禅道 RBAC 权限表设计参考（仿照禅道全套权限表）

> 项目：基于AI的大学生就业服务智能体（AiStudentJobAgent）
> 模块：用户与权限管理 · 成员2（RBAC 权限）
> 性质：**调研禅道后设计的 RBAC 全套权限表**（设计成果 / 参考方案）
> 结论先行：最终项目采用 **`sys_user.role_code` 枚举方案**（与团队 8 张表对齐），本套 4 张权限表作为「禅道参考设计」保留，**不进入运行数据库**，供答辩/汇报体现"仿照禅道设计 RBAC 全套权限表"这一成果。

---

## 一、禅道权限逻辑调研（设计依据）

禅道的权限体系建立在「组织管理」之上，核心是**部门、用户、分组、权限**四层配合：

| 禅道概念 | 作用 | 映射到本项目 |
|---|---|---|
| 部门 department | 组织归属，用于数据隔离 | 用 `college/major/grade` 字段承担，RBAC 表不重复建模 |
| 用户 user（zt_user） | 账号主体 | `sys_user`（现有表） |
| 分组 group（zt_group） | 权限载体，功能权限先给分组再给人 | `sys_role` |
| 权限 privilege（zt_grouppriv） | 模块+方法级功能权限 | `sys_menu`（目录/菜单/按钮三级）+ `sys_role_menu` |
| 用户-分组关联（zt_usergroup） | 用户属于哪些组，取权限并集 | `sys_user_role` |

**禅道核心表**：`zt_group`（含 `acl` 字段存数据可见范围）、`zt_grouppriv`（group→module+method）、`zt_usergroup`（user→group）。权限判定 `checkPriv() → hasPriv()`：超级管理员直接放行，其余查 `rights[module][method]`（功能权限）+ `acls[...]`（数据可见范围）。

**三条启示**：
1. 功能权限与数据权限分开配——权限表只管"能不能进菜单/点按钮"，数据范围由业务 SQL 按归属字段过滤。
2. 角色是权限的唯一载体，用户不直接持权限；一个用户可挂多角色，取并集。
3. 超级管理员全量放行，其余角色最小授权。

---

## 二、RBAC 权限表设计（共 4 张新增表）

| 表名 | 说明 | 对应禅道 | 关系 |
|---|---|---|---|
| `sys_role` | 角色表 | zt_group | 用户↔角色 多对多 |
| `sys_menu` | 菜单/权限表（目录/菜单/按钮树） | zt_grouppriv | 角色↔权限 多对多 |
| `sys_user_role` | 用户-角色关联表 | zt_usergroup | |
| `sys_role_menu` | 角色-权限关联表 | zt_grouppriv | |

### 2.1 建表 SQL

```sql
-- =============================================================
-- 禅道 RBAC 全套权限表（4 张）· 参考设计
-- 注：本套表为设计参考，项目运行库使用 sys_user.role_code 方案，
--     本脚本不执行，仅供答辩/对比参考。
-- =============================================================

-- 1. 角色表 sys_role
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code`   VARCHAR(32)  NOT NULL                COMMENT '角色编码 SUPER_ADMIN/COLLEGE_ADMIN/COUNSELOR/HR/STUDENT',
  `role_name`   VARCHAR(64)  NOT NULL                COMMENT '角色名称',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '显示顺序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '1启用 0停用',
  `remark`      VARCHAR(255)          DEFAULT NULL   COMMENT '备注',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 菜单/权限表 sys_menu（目录 M / 菜单 C / 按钮 F 三级树）
CREATE TABLE `sys_menu` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父节点，0为顶级',
  `menu_name`   VARCHAR(64)  NOT NULL                COMMENT '名称',
  `menu_type`   CHAR(1)      NOT NULL                COMMENT 'M目录 C菜单 F按钮',
  `perm_code`   VARCHAR(100)          DEFAULT NULL   COMMENT '权限标识 模块:资源:操作，如 job:post:add',
  `path`        VARCHAR(255)          DEFAULT NULL   COMMENT '前端路由',
  `component`   VARCHAR(255)          DEFAULT NULL   COMMENT '前端组件',
  `icon`        VARCHAR(100)          DEFAULT NULL   COMMENT '图标',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `visible`     TINYINT      NOT NULL DEFAULT 1      COMMENT '1显示 0隐藏',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '1启用 0停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  UNIQUE KEY `uk_perm` (`perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单/权限表';

-- 3. 用户-角色关联表 sys_user_role
CREATE TABLE `sys_user_role` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT   NOT NULL                COMMENT '用户ID -> sys_user.id',
  `role_id`     BIGINT   NOT NULL                COMMENT '角色ID -> sys_role.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 4. 角色-权限关联表 sys_role_menu
CREATE TABLE `sys_role_menu` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id`     BIGINT   NOT NULL                COMMENT '角色ID -> sys_role.id',
  `menu_id`     BIGINT   NOT NULL                COMMENT '菜单/权限ID -> sys_menu.id',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';
```

### 2.2 关键设计点
- **权限标识 perm_code 三级规范**：`模块:资源:操作`，如 `job:post:list`（菜单级）、`job:post:add`（按钮级），便于接口校验与前端按钮级 `v-perm` 指令。
- **多对多**：一个用户多个角色权限取并集；一个角色勾选多个目录/菜单/按钮。
- **命名统一 `sys_` 前缀**，避免与业务表（`resume/job_*`）冲突。
- 数据权限（仅看本人/本院）不放进权限表，留在业务 Mapper SQL 用 `SecurityUtils.getUserId()` 过滤——参考禅道"功能权限与数据权限分开配"。

---

## 三、与最终主方案（role_code 枚举）的关系

| 维度 | 本套 4 表方案（参考设计） | 最终采用方案（已实现并跑通） |
|---|---|---|
| 表结构 | 新增 4 张权限表 → 共 12 张表 | **保持团队 8 张表**，不新增表 |
| 角色来源 | sys_role 表 + sys_user_role 关联 | `sys_user.role_code` 枚举字段 |
| 权限控制 | @RequiresPermission + PermissionAspect 切面 | Spring Security `@PreAuthorize` 接口级鉴权 |
| 适用场景 | 角色/权限可动态配置、角色繁多 | 本项目角色固定 4 类、权限基本不变 |

**结论**：两套方案都源于禅道 RBAC 思想。考虑团队《需求分析文档 v2.2》规定 8 张表、权限用 `role_code`，为保证与队友数据模型完全一致、零表结构冲突，运行库采用 `role_code` 枚举方案；本套 4 表作为**调研禅道的 RBAC 设计成果**保留于此，若后续权限需动态化配置，可在此基础上扩展启用。
