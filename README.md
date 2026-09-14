# AI 大学生就业服务智能体

> 2024 级《软件工程》课程设计 —— 题目《基于AI的大学生就业服务智能体开发》

面向高校学生、辅导员、企业 HR 与院系管理员的就业服务智能体平台：以简历诊断、岗位匹配、实习流程、
AI 面试训练、就业政策问答与就业数据看板为核心，把"大模型能力"嵌入真实的就业业务流程。

## 一、技术选型

| 层次 | 技术 | 说明 |
| --- | --- | --- |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts | PC Web 优先，按角色动态渲染菜单 |
| 后端 | Java 17 + Spring Boot 3.5 + Spring Security(JWT) + MyBatis-Plus | RESTful API + WebSocket(STOMP) |
| 数据库 | MySQL 8（业务数据） + Redis 7（热点缓存） | 逻辑删除、分页插件、防全表更新 |
| AI 能力 | OpenAI 兼容协议（DeepSeek / 通义千问 / 智谱 / 本地 Ollama） | 未配置模型时自动降级为本地规则引擎 |
| 部署 | Docker + Docker Compose + Nginx | 一键起 MySQL / Redis / 后端 / 前端 |
| 文档 | Markdown（docs/）+ Swagger/OpenAPI | 接口文档随代码自动生成 |

## 二、目录结构

```text
AiStudentJobAgent/
├── backend/                     后端服务（Spring Boot，43 个文件）
│   ├── src/main/java/com/campus/jobagent/
│   │   ├── common/              统一响应 Result、业务异常、安全工具
│   │   ├── config/              安全配置、MyBatis-Plus 分页、大模型配置
│   │   ├── security/            JWT 令牌、认证过滤器、登录用户
│   │   ├── agent/               智能体调度、提示词库、本地规则引擎、大模型客户端
│   │   └── modules/             示例业务模块：auth（认证）、user（档案）、job（岗位）、dashboard（看板）
│   ├── src/main/resources/      application.yml、application-prod.yml
│   ├── src/test/java/           单元测试（JWT、规则引擎）
│   └── Dockerfile
├── frontend/                    前端工程（Vue 3 + Vite，19 个文件）
│   ├── src/api/                 接口封装（认证 / 岗位 / 看板）
│   ├── src/router/              路由与菜单（按角色过滤）
│   ├── src/store/               Pinia 用户状态
│   ├── src/views/               页面：登录、就业数据看板、岗位匹配、个人档案
│   ├── src/components/          ECharts 图表封装
│   ├── nginx.conf               生产环境 Nginx 配置
│   └── Dockerfile
├── deploy/sql/                  数据库脚本
│   ├── schema.sql               建库建表（8 张表）
│   └── data.sql                 初始化数据（含 50 条就业政策）
├── docs/                        课程设计文档（标准成文交付物）
├── docker-compose.yml           一键部署编排
└── .github/workflows/ci.yml     持续集成：后端编译测试 + 前端构建
```

### 2.1 脚手架当前实现范围

脚手架按"**一条最小可运行闭环 + 一个可照抄的示例模块**"设计，避免文件数量过多：

| 模块 | 脚手架中的状态 | 说明 |
| --- | --- | --- |
| 认证 / 用户档案 | ✅ 已实现 | 注册、登录、JWT 鉴权、四类角色、档案维护 |
| 岗位匹配 / 投递 | ✅ 已实现 | **示例模块**：发布、检索、智能匹配、投递、进度跟踪 |
| 就业数据看板 | ✅ 已实现 | 就业率、行业分布、薪资分析、投递趋势（SQL 实时聚合） |
| 智能体（AI） | ✅ 已实现 | 大模型通道 + 本地规则引擎降级，岗位匹配解读已接入 |
| 简历 / 实习 / 面试 / 政策 | 📋 设计已完成，代码待补 | 表结构与接口已在 `docs/` 定义，按 `modules/job` 的结构照抄即可 |

> 新增业务模块的标准做法：在 `modules/` 下新建 `entity / mapper / service / controller` 四个包，
> 参照 `modules/job` 的写法；前端在 `views/` 增加页面，并在 `router/index.ts` 中注册路由与菜单。

## 三、快速开始

### 方式一：Docker 一键启动（推荐演示使用）

```bash
cp .env.example .env          # Windows: copy .env.example .env
docker compose up -d --build
```

| 服务 | 地址 |
| --- | --- |
| 前端 | http://localhost:8081 |
| 后端接口文档 | http://localhost:8080/swagger-ui.html |
| 健康检查 | http://localhost:8080/actuator/health |

### 方式二：本地开发

```bash
# 1) 初始化数据库（MySQL 8，库名 job_agent）
mysql -uroot -p < deploy/sql/schema.sql
mysql -uroot -p job_agent < deploy/sql/data.sql

# 2) 启动后端（默认 dev 环境，端口 8080）
cd backend && mvn spring-boot:run

# 3) 启动前端（端口 5173，已配置 /api 代理）
cd frontend && npm install && npm run dev
```

> 未安装 Maven 时，可用 IDEA 自带的 Maven，或在项目根目录执行 `./mvnw`（需先执行一次 `mvn -N wrapper:wrapper` 生成 wrapper）。

## 四、演示账号

| 角色 | 账号 | 密码 | 可用功能 |
| --- | --- | --- | --- |
| 学生 | `student01` | `123456` | 简历诊断、岗位匹配与投递、实习申请与日志、AI 面试训练、政策问答 |
| 辅导员 | `counselor01` | `123456` | 学生档案、实习审批、日志批阅、政策热点统计 |
| 企业HR | `hr01` | `123456` | 岗位发布、申请管理、实习审批 |
| 院系管理员 | `admin01` | `123456` | 全量数据、看板、岗位与流程管理 |

## 五、AI 能力说明

系统提供**双通道**智能体：

1. **大模型通道**：在 `.env`（或 `application-dev.yml`）中配置 `AI_ENABLED=true` 与 `AI_API_KEY`，
   即可接入任意 OpenAI 兼容服务（DeepSeek / 通义千问 / 智谱 / 本地 Ollama）；
2. **本地规则引擎通道**：未配置模型或调用失败时自动降级，保证离线演示与答辩环境可用，
   且面试评分等场景具备"可解释、可复现"的基准分。

可访问 `GET /api/ai/status` 查看当前生效的通道（前端顶栏也会显示状态标签）。

## 六、文档索引

课程设计要求的交付文档全部位于 `docs/`，详见 [docs/README.md](docs/README.md)。

## 七、团队协作

分支策略、提交规范、评审流程见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 八、后续迭代计划

| 阶段 | 内容 |
| --- | --- |
| 第 1 周（已完成） | 仓库联通、脚手架搭建、标准文档成文 |
| 第 2 周 | 用户与权限模块、档案模块联调；PDF/Word 简历解析接入 |
| 第 3 周 | 岗位匹配算法调优、实习流程完整跑通 |
| 第 4 周 | 面试训练 WebSocket 实时化、政策知识库向量检索（RAG） |
| 第 5 周 | 系统测试、性能优化、部署文档与操作手册定稿、答辩演示 |
