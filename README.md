# 中小企业事务管理数智化系统

> 面向中小企业的全栈事务管理平台，支持事务下达、跟踪、审批与统计分析，涵盖多角色权限体系与组织架构管理。

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [功能模块](#功能模块)
- [角色权限](#角色权限)
- [事务状态流转](#事务状态流转)
- [数据库表结构](#数据库表结构)
- [快速启动](#快速启动)
- [接口规范](#接口规范)
- [默认账号](#默认账号)

---

## 项目概述

本系统为中小企业提供数字化事务管理解决方案，核心功能包括：

- **事务全生命周期管理**：从下达、接收、执行、提交到审批的完整闭环
- **四象限优先级模型**：依据重要性与紧急度自动计算 A/B/C/D 四级优先级
- **多级组织架构**：支持部门树、人员归属及跨部门事务管理
- **多角色权限体系**：管理员、总经理（总指挥）、部门经理（经理）、普通员工四种角色
- **实时通知**：事务状态变更自动触发站内消息推送
- **数据统计看板**：部门维度、个人维度、事务级别分布等多维度 ECharts 图表

---

## 技术栈

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | ^3.5.29 | Composition API |
| Vite | ^7.3.1 | 构建工具 |
| TypeScript | ~5.9.3 | 类型安全 |
| Element Plus | ^2.13.2 | UI 组件库 |
| Pinia | ^3.0.4 | 状态管理 |
| Vue Router | ^4.6.4 | 路由（Hash 模式） |
| Axios | ^1.13.6 | HTTP 请求 |
| ECharts | ^6.0.0 | 数据可视化 |
| Day.js | ^1.11.19 | 日期处理 |

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 运行时环境 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring Security | 6.x | 认证与授权 |
| MyBatis | 3.0.3 | 数据访问层 |
| JWT（jjwt） | 0.12.5 | Token 鉴权 |
| MySQL | 8.x | 关系型数据库 |
| Maven | 3.x | 项目构建 |
| Lombok | 内置 | 实体类简化 |

---

## 项目结构

```
affairs-management/
├── frontend/               # Vue 3 + Vite 前端项目
│   ├── src/
│   │   ├── api/            # Axios 接口封装
│   │   │   ├── request.ts       # 请求基类（拦截器、Token 注入）
│   │   │   ├── task.ts          # 事务相关接口
│   │   │   ├── organization.ts  # 组织架构接口
│   │   │   ├── statistics.ts    # 统计数据接口
│   │   │   ├── notification.ts  # 通知接口
│   │   │   └── user.ts          # 用户接口
│   │   ├── layouts/        # 页面布局
│   │   │   ├── MainLayout.vue   # 主框架（侧边栏 + Header）
│   │   │   └── BlankLayout.vue  # 空白布局（登录页）
│   │   ├── router/         # 路由配置（含路由守卫）
│   │   ├── stores/         # Pinia 状态管理
│   │   │   ├── user.ts          # 用户状态
│   │   │   ├── task.ts          # 事务状态
│   │   │   ├── organization.ts  # 组织架构状态
│   │   │   └── notification.ts  # 通知状态
│   │   ├── types/          # TypeScript 类型定义
│   │   ├── utils/          # 工具函数（格式化、校验、常量）
│   │   ├── styles/         # 全局样式（SCSS）
│   │   └── views/          # 页面视图
│   │       ├── dashboard/       # 事务总览 & 统计看板
│   │       ├── login/           # 登录页
│   │       ├── task/            # 事务管理（列表/创建/详情/执行/反馈）
│   │       ├── organization/    # 组织管理（架构树/部门/人员）
│   │       └── profile/         # 个人信息
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts      # Vite 配置（含 API 代理）
│
├── backend/                # Spring Boot 后端项目
│   ├── src/main/java/com/affairs/management/
│   │   ├── controller/     # REST 控制器
│   │   ├── service/        # 业务逻辑层
│   │   ├── mapper/         # MyBatis Mapper 接口
│   │   ├── entity/         # 数据库实体类
│   │   ├── dto/
│   │   │   ├── request/    # 请求 DTO
│   │   │   └── response/   # 响应 VO
│   │   ├── config/         # 配置类（Security、CORS、密码修复器）
│   │   ├── common/         # 公共类（统一响应、异常、错误码）
│   │   └── util/           # 工具类（ID 生成、优先级计算）
│   ├── src/main/resources/
│   │   ├── mapper/         # MyBatis XML 映射文件（9 个）
│   │   └── application.yml # 应用配置
│   └── pom.xml
│
├── docs/
│   └── backend/
│       ├── 01_tables.sql   # 建表 DDL
│       └── 02_init_data.sql # 初始化数据（含 BCrypt 密码）
│
├── .gitignore
└── README.md               # 本文件
```

---

## 功能模块

### 事务总览（Dashboard）
- 五项 KPI 统计卡片：已下达 / 待办理 / 已完成 / 未完成 / 已逾期
- 任务全景追踪日历（日视图 / 三日视图 / 周视图 / 月视图）
- 四象限级别分布饼图（A/B/C/D 级）
- 下级提交动态时间线
- 部门事务总数 / 已完成数柱状图（对接后端实时数据）

### 事务列表
- 标签页切换：我的下达 / 我的代办 / 管辖范围
- 双栏布局：左侧列表 + 右侧内联详情面板
- 经理专属：内联提交 / 下发功能
- 筛选：状态 / 级别 / 关键词搜索

### 事务创建（仅下达人）
- 四象限优先级选择 → 自动计算 A/B/C/D 级别
- 执行人选择（按权限过滤可见人员）
- 完成截止时间设置

### 事务执行（执行人）
- 确认接收 → 添加处理记录 → 提交成果
- 附件上传支持

### 事务反馈（下达人审批）
- 审批通过 / 不通过 / 转派他人

### 组织管理
- **组织架构树**：可视化部门-人员树形结构，支持增删改
- **部门管理**：部门信息、层级、负责人、成员数统计
- **人员管理**：成员列表、角色分配、密码重置

### 个人信息
- 查看个人资料
- 修改密码

---

## 角色权限

| 角色 | 标识 | 权限说明 |
|------|------|---------|
| 系统管理员 | `admin` | 全部权限；组织架构 / 人员管理；重置任意用户密码 |
| 总指挥（总经理） | `director` | 总览看板（全局视图）；向任意员工下达事务 |
| 部门经理 | `manager` | 向管辖部门员工下达事务；可向下分派（转传） |
| 普通员工 | `staff` | 接收并执行事务；提交成果 |

> 路由守卫根据角色自动重定向：`admin` → 组织管理，`director/manager` → 事务总览，`staff` → 事务列表

---

## 事务状态流转

```
创建
  └─► pending（待接收）
        └─► accepted（已接收）
              └─► in_progress（执行中）
                    └─► submitted（已提交）
                          ├─► approved（通过）→ completed（已完成）
                          └─► rejected（不通过）→ 可重新提交

任意阶段可操作：
  cancelled（已取消）  ← 下达人随时取消
  overdue（已逾期）    ← 系统自动标记
```

**状态说明**

| 状态 | 含义 |
|------|------|
| `pending` | 事务已下达，等待执行人接收 |
| `accepted` | 执行人已接收，开始计时 |
| `in_progress` | 执行中（首次添加处理记录后自动切换） |
| `submitted` | 执行人已提交成果，等待审批 |
| `approved` | 审批通过 |
| `rejected` | 审批不通过，可补充后重新提交 |
| `completed` | 流程完结 |
| `overdue` | 超过截止时间未完成 |
| `cancelled` | 下达人取消 |

---

## 数据库表结构

数据库名：`affairs_management`，字符集：`utf8mb4`

### 1. departments — 部门表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 部门ID，如 D001 |
| name | VARCHAR(100) | 部门名称 |
| parent_id | VARCHAR(32) | 上级部门ID（NULL 为顶级） |
| leader_id | VARCHAR(32) | 部门负责人用户ID |
| sort_order | INT | 排序权重 |
| level | INT | 层级深度（0 = 顶级） |
| member_count | INT | 成员数量（冗余字段） |
| status | TINYINT | 0=停用，1=启用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

> 自关联外键：`parent_id → departments.id`

---

### 2. users — 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 用户ID，如 U001 |
| username | VARCHAR(50) | 登录账号（唯一） |
| password_hash | VARCHAR(255) | BCrypt 密码哈希 |
| name | VARCHAR(50) | 真实姓名 |
| avatar | VARCHAR(500) | 头像URL |
| phone | VARCHAR(20) | 手机号（唯一） |
| email | VARCHAR(100) | 邮箱 |
| dept_id | VARCHAR(32) | 所属部门ID |
| position | VARCHAR(100) | 岗位名称 |
| role | VARCHAR(20) | 角色：admin / director / manager / staff |
| status | TINYINT | 0=离职，1=在职 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

---

### 3. user_managed_departments — 用户可管理部门关系表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 自增主键 |
| user_id | VARCHAR(32) | 用户ID（manager 角色） |
| dept_id | VARCHAR(32) | 可管理部门ID |
| created_at | DATETIME | 创建时间 |

> 用于确定 `manager` 角色可下达事务的范围

---

### 4. tasks — 事务表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 事务ID，如 T20260201001 |
| title | VARCHAR(200) | 事务标题（取描述前50字） |
| description | TEXT | 事务详细描述 |
| assigner_id | VARCHAR(32) | 下达人用户ID |
| executor_id | VARCHAR(32) | 执行人用户ID |
| importance | TINYINT | 重要性：1-3 |
| urgency | TINYINT | 紧急度：1-3 |
| level | CHAR(1) | 优先级：A / B / C / D |
| response_deadline | DATETIME | 响应截止时间 |
| completion_deadline | DATETIME | 完成截止时间 |
| response_time | DATETIME | 实际响应时间 |
| completion_time | DATETIME | 实际完成时间 |
| status | VARCHAR(20) | 当前状态（见状态流转） |
| parent_task_id | VARCHAR(32) | 父任务ID（向下分派时生成） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**优先级计算规则（四象限）**

| 类型 | Importance | Urgency | Level |
|------|-----------|---------|-------|
| 重要紧急 | 3 | 3 | A |
| 重要不紧急 | 3 | 1 | B |
| 不重要紧急 | 1 | 3 | C |
| 不重要不紧急 | 1 | 1 | D |

---

### 5. task_process_records — 事务流程记录表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 记录ID，如 R001 |
| task_id | VARCHAR(32) | 关联事务ID |
| operator_id | VARCHAR(32) | 操作人用户ID |
| action | VARCHAR(20) | 操作类型（见下表） |
| content | TEXT | 操作说明 / 提交内容 |
| created_at | DATETIME | 操作时间 |

**action 枚举值**

| 值 | 含义 |
|----|------|
| `create` | 创建事务 |
| `accept` | 接收事务 |
| `process` | 添加处理记录 |
| `submit` | 提交成果 |
| `approve` | 审批通过 |
| `reject` | 审批不通过 |
| `reassign` | 转派 / 向下分派 |
| `cancel` | 取消事务 |
| `comment` | 评论备注 |

---

### 6. attachments — 附件元数据表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 附件ID，如 F001 |
| file_name | VARCHAR(255) | 原始文件名 |
| file_url | VARCHAR(500) | 文件访问地址 |
| file_size | BIGINT | 文件大小（字节） |
| mime_type | VARCHAR(120) | MIME 类型 |
| uploaded_by | VARCHAR(32) | 上传人用户ID |
| uploaded_at | DATETIME | 上传时间 |

---

### 7. task_attachments — 事务-附件关系表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 自增主键 |
| task_id | VARCHAR(32) | 事务ID |
| attachment_id | VARCHAR(32) | 附件ID |
| created_at | DATETIME | 关联时间 |

---

### 8. record_attachments — 流程记录-附件关系表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 自增主键 |
| record_id | VARCHAR(32) | 流程记录ID |
| attachment_id | VARCHAR(32) | 附件ID |
| created_at | DATETIME | 关联时间 |

---

### 9. notifications — 通知表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(32) | 通知ID，如 N001 |
| user_id | VARCHAR(32) | 接收用户ID |
| title | VARCHAR(200) | 通知标题 |
| content | TEXT | 通知正文 |
| type | VARCHAR(30) | 通知类型（见下表） |
| related_task_id | VARCHAR(32) | 关联事务ID |
| is_read | TINYINT(1) | 0=未读，1=已读 |
| created_at | DATETIME | 创建时间 |

**type 枚举值**

| 值 | 触发时机 |
|----|---------|
| `task_assigned` | 事务被分配给执行人 |
| `task_accepted` | 执行人接收事务 |
| `task_submitted` | 执行人提交成果 |
| `task_approved` | 审批通过 |
| `task_rejected` | 审批不通过 |
| `task_completed` | 事务完成 |
| `system` | 系统消息 |

---

## 快速启动

### 前置条件

- Node.js >= 18
- Java 17
- Maven 3.x
- MySQL 8.x

### 1. 初始化数据库

```sql
-- 执行建表脚本
source docs/backend/01_tables.sql

-- 执行初始化数据脚本
source docs/backend/02_init_data.sql
```

### 2. 启动后端

```bash
cd backend

# 编译
mvn compile

# 启动（默认端口 8080）
mvn spring-boot:run
```

> 后端服务启动后访问：`http://localhost:8080/api`

### 3. 启动前端

```bash
cd frontend

# 安装依赖（首次）
npm install

# 启动开发服务器（默认端口 5173）
npm run dev
```

> 前端访问地址：`http://localhost:5173`
> 已配置 Vite 代理：`/api` → `http://localhost:8080`

### 4. 生产构建

```bash
cd frontend
npm run build
# 产物输出至 frontend/dist/
```

---

## 接口规范

**Base URL**：`http://localhost:8080/api`

**统一响应格式**：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**认证方式**：Bearer Token（JWT，有效期 24 小时）

```
Authorization: Bearer <token>
```

**主要接口模块**

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证 | `/auth` | 登录、获取用户信息、修改密码 |
| 事务 | `/task` | CRUD、状态流转（接收/提交/审批/取消） |
| 部门 | `/dept` `/org` | 部门 CRUD、组织架构树 |
| 人员 | `/member` | 成员列表、新增、编辑、重置密码 |
| 统计 | `/stats` | 总览、部门维度、个人维度、最近动态 |
| 通知 | `/notification` | 通知列表、标记已读、未读数量 |
| 文件 | `/file` | 文件上传（本地存储） |
| 用户 | `/user` | 个人信息更新 |

---

## 默认账号

> 初始密码均为 `123456`，管理员账号密码为 `admin123`

| 账号 | 密码 | 角色 | 姓名 | 部门 |
|------|------|------|------|------|
| admin | admin123 | 系统管理员 | 赵阳 | 总经办 |
| zhangsan | 123456 | 总指挥 | 陈志远 | 总经办 |
| lisi | 123456 | 部门经理 | 王建华 | 技术研发部 |
| wangwu | 123456 | 部门经理 | 李梅 | 财务部 |
| staff01 | 123456 | 普通员工 | 黄晓龙 | 技术研发部 |
| staff02 | 123456 | 普通员工 | 张倩倩 | 技术研发部 |
| staff03 | 123456 | 普通员工 | 王成 | 财务部 |
