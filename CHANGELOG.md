# 更新日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 规范，
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### 待修复
- 学生端首次进入答题页路由闪烁（P0-1，见 [ROADMAP.md](./ROADMAP.md) §1.x）
- 管理员端教师/学生详情页缺失（P0-2）
- 教师无自主批阅流、无教师注册入口（P0-3 / P1-1）
- 多选题与文言文题选项渲染丢失（P0-4）
- 教师端"所属课程"选项仅 2 项、"班级"仅 3 项（P1-2）
- 教师无个人信息修改入口（P1-3）

### 已完成（自 2026-07 起独立开发）

#### 后端
- Spring Boot 3.2 工程骨架、统一响应结构、全局异常处理
- 16 张业务表设计 + MyBatis-Plus Mapper 自动生成
- JWT 无状态登录注册 + HandlerInterceptor 三级角色权限拦截
- 管理员模块：用户 / 学科 / 公告 / 平台数据看板
- 题库与课程学习资料模块
- 作业组卷发布 / 学生作答 / 客观题自动判分（Grader）
- 学生学习模块：练习流水 / 错题本 / 智能推题规则引擎 / 学习计划
- 启动时自动初始化演示数据（账号 / 题库 / 作业 / 练习流水）

#### 前端
- Vue 3 + Element Plus 工程初始化，登录与注册页
- 主布局与路由权限守卫
- 管理端全部页面（数据看板 / 用户 / 班级 / 学科 / 课程 / 公告）
- 教师端页面（题库 / 作业发布 / 批阅 / 学情分析 / 我的课程）
- 学生端页面（学习看板 / 作业 / 自主练习 / 推题 / 错题本 / 学习计划）

#### 工程化
- 本地私密配置（`application-local.yml`）与仓库分离
- 幂等 SQL 初始化脚本 + 首次启动自动灌入演示数据
- 端口规划：`OrderBoss 8080` / `agent-radar 8090` / `smartclass 8091` / `naming-advisor 8092`，前端 `5180` / `5181`，互不冲突
- JWT 签名密钥移出仓库，使用 `${JWT_SECRET:dev-only-secret-do-not-use-in-production}` 占位

## [1.0.0] - 2026-09-02

### 首次公开版

- 全栈核心功能（鉴权 / 作业 / 推题 / 错题 / 计划 / 学情）跑通
- LICENSE 切换为 **AGPL v3**（商业防御性开源：阻止第三方把代码用作付费 SaaS 而不公开改造）
- 作者署名统一为 **Leo-Li638**（GitHub: <https://github.com/Leo-Li638>）
- 提交记录见 `git log`

[Unreleased]: https://github.com/Leo-Li638/smartclass/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Leo-Li638/smartclass/releases/tag/v1.0.0
