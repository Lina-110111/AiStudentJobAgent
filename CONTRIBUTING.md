# 团队协作规范

本文档是项目"标准成文"的一部分，所有成员提交代码前必须阅读并遵守。

## 一、分支模型

| 分支 | 用途 | 说明 |
| --- | --- | --- |
| `main` | 稳定可交付分支 | 仅接受来自 `develop` 的合并，每次合并打 tag（如 `v0.1.0`） |
| `develop` | 集成分支 | 功能完成后先合到 `develop`，由组长统一合并到 `main` |
| `feature/xxx` | 功能分支 | 如 `feature/resume-diagnosis`，从 `develop` 切出 |
| `fix/xxx` | 缺陷修复分支 | 如 `fix/jwt-expire` |
| `docs/xxx` | 文档分支 | 如 `docs/api-spec` |

分支命名统一使用小写英文 + 中划线，禁止使用中文、空格与个人姓名缩写。

## 二、提交信息规范（Conventional Commits）

```
<type>(<scope>): <subject>
```

| type | 含义 |
| --- | --- |
| `feat` | 新增功能 |
| `fix` | 修复缺陷 |
| `docs` | 只改文档 |
| `style` | 不影响逻辑的格式调整 |
| `refactor` | 重构（既不是新增功能也不是修缺陷） |
| `test` | 新增或修改测试 |
| `chore` | 构建、依赖、配置等杂项 |

示例：

```
feat(resume): 新增简历 AI 诊断接口
fix(auth): 修复令牌过期后仍可访问接口的问题
docs(api): 补充政策问答接口的引用字段说明
```

要求：一次提交只做一件事；subject 用中文或英文均可，但同一仓库保持一致；禁止出现 `update`、`修改` 等无意义描述。

## 三、提交前自检清单

- [ ] 代码可编译：`cd backend && mvn -q compile`、`cd frontend && npm run build`
- [ ] 单元测试通过：`cd backend && mvn test`
- [ ] 没有提交 `.env`、密钥、数据库密码、`target/`、`node_modules/`
- [ ] 新增或修改接口后，同步更新 `docs/03-系统接口设计文档.md`
- [ ] 数据库结构变化后，同步更新 `deploy/sql/schema.sql` 并说明升级方式

## 四、代码规范摘要

### 后端（Java）

- 分层：`controller` 只做参数校验与路由，业务写在 `service`，数据访问写在 `mapper`
- 统一返回 `Result<T>`；业务异常抛 `BizException`，禁止在 controller 里写 `try-catch` 兜底
- 类名大驼峰、方法名小驼峰、常量全大写下划线；包名全小写
- 所有对外接口必须写 `@Operation`（Swagger 注解），便于自动生成接口文档
- 禁止在日志中打印密码、令牌、身份证明文

### 前端（Vue 3 + TS）

- 组件文件使用大驼峰（`ResumeView.vue`），组合式 API + `<script setup lang="ts">`
- 所有后端调用统一走 `src/api/`，禁止在页面里直接写 `axios`
- 接口返回类型必须在 `src/types/api.ts` 中声明
- 页面样式使用 `<style scoped>`，公共样式放 `src/styles/`

## 五、评审（Code Review）流程

1. 功能分支完成后，向 `develop` 发起 Pull Request（PR）；
2. PR 标题与提交信息同规范，描述中必须包含：**改动内容 / 自测方式 / 影响范围**；
3. 至少 1 名成员评审通过（组长负责最终合并）；
4. 评审关注点：功能是否满足需求、是否有越权风险、异常是否被正确捕获、是否补充了文档与测试；
5. 合并方式统一使用 `Squash and merge`，保持主线提交历史整洁。

## 六、每日与每周协作

- 每周六 18:00 前提交项目周报（格式见 `docs/08-实施周报-*.md`）；
- 每周至少一次例会，进度以看板文档为准，未完成事项必须写明原因与补救措施；
- 需求变更必须记录在周报的"问题/风险/建议"部分，并评估工作量。
