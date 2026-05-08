# 新前端应用管理平台（AI & Low-Code 赋能版）产品设计方案 v2

---

## 1. 方案背景与目标

### 1.1 现状分析
当前公司前端开发基于 **Vue 3 + Vite + Spring Boot 2** 技术栈，存在以下痛点：
- **重复开发**：标准 CRUD 页面占日常开发 60%+，但每次仍手工编写前后端代码
- **规范不统一**：不同开发者代码风格差异大，代码审查成本高
- **知识散落**：业务逻辑、组件用法、接口规范等经验散落在各处，新人上手慢
- **效能不可量化**：引入 AI 工具后缺乏数据支撑，无法证明提效成果
- **CI/CD 流程混乱**：各前端项目接入 Jenkins 进度不一，缺乏统一的流水线标准；代码质量检查（Lint/TypeCheck）虽已配置但未在 CI 中强制执行；MR 代码审查依赖人工，标准不一且容易遗漏

### 1.2 项目目标
基于 **JeecgBoot 3.9.2** 搭建一个集"应用管理、AI 赋能、低代码出码、效能度量"为一体的**前端研发门户**，实现：
| 目标维度 | 量化指标 |
|---------|---------|
| 开发提效 | 标准 CRUD 模块开发周期缩短 40%+ |
| 规范落地 | AI 生成代码 100% 符合公司编码规范 |
| 资产沉淀 | 公司核心业务 Skill 覆盖率达到 80% |
| 效能量化 | AI 生成代码量、工时节省可量化追踪 |
| CI/CD 治理 | 前端项目 CI 标准化覆盖率达到 100%，MR 代码审查 AI 辅助率 > 80% |

---

## 2. 系统架构设计

### 2.1 整体架构分层

```
┌─────────────────────────────────────────────────────────┐
│                    前端用户层                             │
│  Web 管理台 (Vue3 + Ant Design Vue4 + Vite6 + TS)       │
│  本地 IDE (Claude Code / Cursor)                         │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   网关 & 鉴权层                           │
│  Spring Cloud Gateway / Shiro + JWT + RBAC               │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                 业务服务层 (JeecgBoot 3.9.2)              │
│                                                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │ AI 开发者 │ │ 低代码    │ │ 业务管理  │ │ 效能度量   │  │
│  │ 中心     │ │ 公共服务  │ │ (V2)     │ │ 看板      │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘  │
│                                                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │ Skill    │ │ MCP      │ │ AI 流程   │ │ CI/CD     │  │
│  │ 引擎     │ │ Server   │ │ 编排      │ │ 治理中心   │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘  │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   AI 基础设施层                           │
│                                                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │ 大模型    │ │ 向量库    │ │ 知识库    │ │ RAG 管道  │  │
│  │ 调度     │ │ pgvector │ │ 管理     │ │          │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘  │
│                                                          │
│  支持: DeepSeek / ChatGPT / Ollama / 通义千问 / 智谱     │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   数据存储层                              │
│  MySQL 8.0+ │ Redis │ PostgreSQL+pgvector │ MinIO       │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心技术栈

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| 前端框架 | Vue 3 + TypeScript + Vite 6 | 3.x / 6.x |
| UI 组件库 | Ant Design Vue 4 | 4.x |
| 后端框架 | Spring Boot 3.5.5 | 3.5.5 |
| ORM | MyBatis-Plus | 3.5.12 |
| 鉴权 | Apache Shiro 2.0.5 + JWT 4.5.0 | 2.0.5 / 4.5.0 |
| 低代码底座 | JeecgBoot | 3.9.2 |
| AI 模型调度 | Jeecg AI Rag Module | 内置 |
| 向量数据库 | PostgreSQL + pgvector | - |
| 文件存储 | MinIO / 阿里 OSS | - |
| 缓存 | Redis | 7.x |
| 关系数据库 | MySQL | 8.0+ |

### 2.3 核心交互流程

```
开发者 ──→ 平台获取 Skill 配置 ──→ 下载 mcp-config.json
                                        │
                                        ▼
本地 IDE (Claude Code) ──→ 读取 mcp-config.json ──→ 连接平台 MCP Server
                                                              │
                                                              ▼
                                                    平台注入业务上下文
                                                    (Skill + 知识库 + 规范)
                                                              │
                                                              ▼
                                                    AI 生成符合规范的代码
                                                              │
                                                              ▼
                                                    开发者确认/合并代码
                                                              │
                                                              ▼
                                                    平台记录效能数据
```

---

## 3. 功能模块详细设计

### 3.1 AI 开发者中心 (AI Developer Hub)

#### 3.1.1 Skill 资产库

**功能描述**：统一管理 AI Skill 资产，包括官方 Skills 和公司自定义 Skills，是平台的核心价值模块。

**页面清单**：

| 页面 | 路由 | 说明 |
|------|------|------|
| Skill 资产总览 | `/ai-hub/skills` | 卡片式展示所有 Skill，支持分类筛选 |
| Skill 详情 | `/ai-hub/skills/detail/:id` | 查看 Skill 描述、使用示例、参数说明 |
| Skill 创建/编辑 | `/ai-hub/skills/edit/:id?` | 可视化编辑 Skill Prompt + 测试 |
| Skill 配置导出 | `/ai-hub/skills/export` | 一键生成 mcp-config.json |

**Skill 数据模型**：

```sql
CREATE TABLE `fe_skill` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT 'Skill名称',
  `code` varchar(50) NOT NULL COMMENT 'Skill编码(唯一标识)',
  `category` varchar(20) NOT NULL COMMENT '分类: official=官方, business=业务, app=应用级',
  `icon` varchar(500) DEFAULT NULL COMMENT '图标',
  `description` text COMMENT 'Skill描述',
  `prompt_template` longtext NOT NULL COMMENT 'Prompt模板内容',
  `input_schema` json DEFAULT NULL COMMENT '输入参数Schema(JSON)',
  `output_schema` json DEFAULT NULL COMMENT '输出参数Schema(JSON)',
  `examples` json DEFAULT NULL COMMENT '使用示例(JSON数组)',
  `app_id` varchar(36) DEFAULT NULL COMMENT '关联应用ID(应用级Skill)',
  `version` varchar(20) DEFAULT '1.0.0' COMMENT '版本号',
  `status` varchar(10) DEFAULT 'enable' COMMENT '状态: enable/disable',
  `sort_no` decimal(8,2) DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Skill资产表';
```

**Skill 分类体系**：

| 分类 | 说明 | 示例 |
|------|------|------|
| 官方 Skill (official) | 集成 Jeecg 官方 Skills | jeecg-codegen、jeecg-onlform、jeecg-bpmn |
| 通用业务 Skill (business) | 公司通用业务规范 | company-code-style、bus-api-standard |
| 应用级 Skill (app) | 针对具体应用的专属 Skill | fleet-mgmt-rule、ticket-system-spec |

**官方 Skill 集成清单**（复用 JeecgBoot 内置能力）：

| Skill 编码 | 能力 | 来源 |
|-----------|------|------|
| jeecg-codegen | 一句话生成全套 CRUD 代码 | Jeecg Skills |
| jeecg-onlform | 一句话创建 Online 表单 | Jeecg Skills |
| jeecg-onlreport | 一句话创建 Online 报表 | Jeecg Skills |
| jeecg-desform | 一句话创建设计器表单 | Jeecg Skills |
| jeecg-bpmn | 一句话画审批流程 | Jeecg Skills |

**业务 Skill 示例**（需自行编写）：

| Skill 编码 | 能力 | 说明 |
|-----------|------|------|
| bus-code-style | 公交业务代码规范 | Vue3 组件命名、API 层封装、状态管理规范 |
| bus-api-standard | 接口规范 | RESTful 风格、统一响应格式、分页参数 |
| bus-component-lib | 业务组件库规范 | 公司封装的车辆选择器、线路选择器等 |
| bus-fleet-rule | 车队管理业务规则 | 车辆调度、排班逻辑、运力计算 |

#### 3.1.2 Claude Code 配置导出

**功能描述**：根据用户选择的 Skill 组合，一键生成 `mcp-config.json`，让本地 Claude Code 快速接入平台。

**配置文件格式**：

```json
{
  "mcpServers": {
    "fe-platform": {
      "command": "npx",
      "args": ["-y", "@jeecg/mcp-server"],
      "env": {
        "MCP_ENDPOINT": "https://your-domain/jeecg-boot/mcp/sse",
        "MCP_TOKEN": "{{user_token}}",
        "SKILLS": "bus-code-style,bus-api-standard,bus-fleet-rule"
      }
    }
  }
}
```

**导出流程**：
1. 用户在 Skill 资产库中勾选所需 Skill
2. 系统校验 Skill 间的依赖与冲突
3. 生成包含选中 Skill 配置的 mcp-config.json
4. 用户下载或一键复制到剪贴板

#### 3.1.3 MCP Server 管理

**功能描述**：管理平台的 MCP Server 实例，复用 JeecgBoot 内置的 `airag_mcp` 模块能力。

**现有能力**（已存在于 AiragMcpList.vue）：
- MCP 服务注册（SSE / STDIO 两种类型）
- MCP 服务状态监控
- MCP 工具同步与测试
- 插件类型管理

**新增扩展**：
- 为 MCP Server 绑定 Skill 集合
- 支持按应用维度隔离 MCP 配置
- 增加连接数监控和限流配置

---

### 3.2 低代码公共服务 (Low-Code Services)

#### 3.2.1 对话式出码 (Chat2Code)

**功能描述**：在 Web 端通过自然语言对话，AI 自动生成符合公司规范的 Vue 3 组件代码。

**页面设计**：

| 区域 | 说明 |
|------|------|
| 左侧面板 | Skill 选择器（可勾选本次对话激活的 Skill） |
| 中间对话区 | 对话界面，支持代码块高亮、一键复制、在线编辑 |
| 右侧预览区 | 实时预览生成的组件（沙箱渲染） |

**对话流程**：

```
用户输入需求 → 拼装 Prompt(Skill + 上下文) → 调用大模型
                                                   │
                                                   ▼
                                          AI 返回代码片段
                                                   │
                                                   ▼
                                    ┌──────────────┼──────────────┐
                                    ▼              ▼              ▼
                              代码预览        沙箱预览        一键下载
```

**技术实现**：
- 后端：复用 JeecgBoot 的 `AIChatHandler`，在 `buildPlugins` 阶段注入 Skill 作为 MCP 工具
- 前端：基于 AiChat.vue 扩展，增加代码预览面板
- 沙箱：使用 iframe + importmap 实现组件实时渲染

#### 3.2.2 Schema 转换引擎

**功能描述**：解析 SQL DDL 语句，自动生成 JeecgBoot Online 表单和列表的 JSON 配置。

**输入**：SQL DDL 语句
```sql
CREATE TABLE bus_vehicle (
  id varchar(36) PRIMARY KEY,
  plate_number varchar(20) NOT NULL COMMENT '车牌号',
  vehicle_type varchar(10) COMMENT '车辆类型',
  seat_count int COMMENT '座位数',
  fleet_id varchar(36) COMMENT '所属车队',
  status tinyint DEFAULT 1 COMMENT '状态:1运营,2停运',
  create_time datetime COMMENT '创建时间'
);
```

**输出**：Online 表单 JSON 配置（符合 JeecgBoot `onl_cgform_*` 表结构）

**转换规则**：

| SQL 类型 | 映射控件 | Online 控件类型 |
|---------|---------|----------------|
| varchar | 输入框 | Input |
| text | 文本域 | Textarea |
| int / bigint | 数字输入 | InputNumber |
| tinyint | 开关 | Switch |
| datetime / date | 日期选择 | DatePicker |
| decimal | 金额输入 | InputNumber |
| 带外键 | 弹窗选择 | Popup |

**扩展能力**：
- 支持批量导入（多表 DDL 一次转换）
- 支持字段中文注释自动填充表单 Label
- 支持一对多关系识别（通过外键推断）

#### 3.2.3 可视化沙箱

**功能描述**：实时预览 AI 生成的 Vue 3 组件或 Online 表单配置的渲染效果。

**技术方案**：

```
┌──────────────────────────────────┐
│         沙箱容器 (iframe)         │
│                                  │
│  importmap:                      │
│    vue → CDN                     │
│    ant-design-vue → CDN          │
│                                  │
│  动态编译:                        │
│    SFC → @vue/compiler-sfc       │
│    → render function             │
│    → 挂载到沙箱 DOM              │
│                                  │
└──────────────────────────────────┘
```

**安全措施**：
- iframe sandbox 属性限制
- 白名单域名加载
- 代码注入检测与过滤

---

### 3.3 业务管理模块 (Existing Business V2)

#### 3.3.1 前端应用管理 (V2)

**功能描述**：管理公司所有前端应用的元信息、技术栈、代码仓库和专属 Skill。

**数据模型**：

```sql
CREATE TABLE `fe_application` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `app_name` varchar(100) NOT NULL COMMENT '应用名称',
  `app_code` varchar(50) NOT NULL COMMENT '应用编码',
  `description` text COMMENT '应用描述',
  `icon` varchar(500) DEFAULT NULL COMMENT '应用图标',
  `tech_stack` json DEFAULT NULL COMMENT '技术栈(JSON)',
  `repo_url` varchar(500) DEFAULT NULL COMMENT '代码仓库地址',
  `repo_branch` varchar(100) DEFAULT 'main' COMMENT '主分支',
  `deploy_url` varchar(500) DEFAULT NULL COMMENT '部署地址',
  `owner_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
  `team_id` varchar(36) DEFAULT NULL COMMENT '所属团队ID',
  `status` varchar(10) DEFAULT 'active' COMMENT '状态: active/archived/developing',
  `skill_ids` json DEFAULT NULL COMMENT '关联Skill ID列表',
  `prompt_template` text COMMENT '应用级Prompt模板',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端应用管理表';
```

**tech_stack JSON 结构**：

```json
{
  "framework": "Vue 3",
  "buildTool": "Vite 6",
  "uiLibrary": "Ant Design Vue 4",
  "language": "TypeScript",
  "stateManager": "Pinia",
  "cssPreprocessor": "Less",
  "other": ["VueRouter", "Axios"]
}
```

**页面清单**：

| 页面 | 路由 | 说明 |
|------|------|------|
| 应用列表 | `/fe/app/list` | 卡片+列表双视图，支持搜索筛选 |
| 应用详情 | `/fe/app/detail/:id` | 展示应用信息、关联Skill、团队成员 |
| 应用编辑 | `/fe/app/edit/:id?` | 编辑应用基本信息、技术栈、关联Skill |
| 应用Skill配置 | `/fe/app/skill/:id` | 为应用配置专属 Skill 和 Prompt |

**应用级 Skill 机制**：
- 每个应用可绑定多个 Skill
- 应用级 Skill 优先级 > 通用业务 Skill > 官方 Skill
- 支持为应用编写专属 Prompt 模板，注入业务特定上下文

#### 3.3.2 前端人员管理 (V2)

**功能描述**：管理前端开发人员信息、权限和 AI 使用效能数据。

**数据模型**：

```sql
CREATE TABLE `fe_developer` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `user_id` varchar(36) NOT NULL COMMENT '关联系统用户ID',
  `real_name` varchar(50) NOT NULL COMMENT '姓名',
  `employee_no` varchar(30) DEFAULT NULL COMMENT '工号',
  `team_id` varchar(36) DEFAULT NULL COMMENT '所属团队ID',
  `role` varchar(20) DEFAULT 'developer' COMMENT '角色: developer/lead/manager',
  `skill_tags` json DEFAULT NULL COMMENT '技能标签',
  `status` varchar(10) DEFAULT 'active' COMMENT '状态: active/inactive',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端开发人员表';
```

**团队管理**：

```sql
CREATE TABLE `fe_team` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_name` varchar(100) NOT NULL COMMENT '团队名称',
  `team_code` varchar(50) NOT NULL COMMENT '团队编码',
  `leader_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
  `description` text COMMENT '团队描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_code` (`team_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端团队表';
```

---

### 3.4 AI 效能度量看板 (AI Efficiency Dashboard)

#### 3.4.1 效能数据采集

**功能描述**：自动采集开发者的 AI 使用行为数据，为效能评估提供数据基础。

**数据采集模型**：

```sql
CREATE TABLE `fe_ai_usage_log` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `app_id` varchar(36) DEFAULT NULL COMMENT '关联应用ID',
  `skill_code` varchar(50) DEFAULT NULL COMMENT '使用的Skill编码',
  `interaction_type` varchar(20) NOT NULL COMMENT '交互类型: chat2code/mcp_call/schema_convert',
  `input_tokens` int DEFAULT 0 COMMENT '输入Token数',
  `output_tokens` int DEFAULT 0 COMMENT '输出Token数',
  `code_lines` int DEFAULT 0 COMMENT '生成代码行数',
  `code_accepted` tinyint DEFAULT 0 COMMENT '代码是否被采纳: 0否/1是',
  `duration_ms` int DEFAULT 0 COMMENT '交互耗时(毫秒)',
  `model_name` varchar(50) DEFAULT NULL COMMENT '使用的模型名称',
  `session_id` varchar(36) DEFAULT NULL COMMENT '会话ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_app_time` (`app_id`, `create_time`),
  KEY `idx_skill_time` (`skill_code`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用行为日志表';
```

**采集方式**：
- **Web 端 Chat2Code**：在对话完成回调中记录
- **MCP 调用**：在 MCP Server 的请求拦截器中记录
- **Schema 转换**：在转换接口中记录

#### 3.4.2 效能看板页面

**页面清单**：

| 页面 | 路由 | 说明 |
|------|------|------|
| 效能总览 | `/fe/dashboard/overview` | 全局概览卡片 + 趋势图 |
| 团队效能 | `/fe/dashboard/team` | 按团队维度统计 |
| 个人效能 | `/fe/dashboard/personal` | 按个人维度统计 |
| Skill 热力图 | `/fe/dashboard/skill-heat` | 各 Skill 使用频率与采纳率 |

**看板指标体系**：

| 指标 | 计算方式 | 说明 |
|------|---------|------|
| AI 生成代码量 | SUM(code_lines) WHERE code_accepted=1 | 被采纳的 AI 生成代码行数 |
| 代码采纳率 | code_accepted=1 的次数 / 总交互次数 | 衡量 AI 输出质量 |
| 工时节省估算 | AI 生成代码量 × 行均编码时间(5min/行) | 按行业基准估算 |
| Skill 使用频次 | COUNT GROUP BY skill_code | 各 Skill 的使用频率 |
| 日均交互次数 | COUNT / 天数 | 活跃度指标 |
| Token 消耗 | SUM(input_tokens + output_tokens) | 成本追踪 |

**看板 UI 布局**：

```
┌─────────┬─────────┬─────────┬─────────┐
│ AI代码量 │ 采纳率   │ 工时节省 │ Token消耗│
│ 12,580  │ 78.3%   │ 1,048h  │ 2.1M    │
└─────────┴─────────┴─────────┴─────────┘
┌───────────────────┬───────────────────┐
│   代码生成趋势图    │   Skill使用排行    │
│   (折线图/周维度)  │   (柱状图)         │
│                   │                   │
└───────────────────┴───────────────────┘
┌───────────────────┬───────────────────┐
│   团队效能排行      │   个人效能 Top10   │
│   (横向柱状图)      │   (列表)          │
└───────────────────┴───────────────────┘
```

---

### 3.5 在线代码医生 (Code Doctor)

**功能描述**：对存量老代码进行 AI 分析，提供重构建议和代码质量评分。

**页面设计**：

| 区域 | 说明 |
|------|------|
| 代码输入区 | 支持文件上传 / 粘贴代码 / Git 仓库关联 |
| 分析结果区 | 问题列表（按严重程度分类）、代码质量评分 |
| 建议输出区 | AI 生成的重构建议、重构后代码对比 |

**分析维度**：

| 维度 | 检查项 |
|------|--------|
| 代码规范 | 命名规范、组件结构、TS 类型定义 |
| 性能问题 | 不必要的重渲染、大包引入、内存泄漏 |
| 安全隐患 | XSS 风险、敏感信息硬编码 |
| 可维护性 | 组件复杂度、重复代码、耦合度 |

---

### 3.6 CI/CD 治理中心 (CI/CD Governance)

#### 3.6.1 现状与问题

当前公司前端 CI/CD 流程存在以下问题：

| 问题 | 现状 | 影响 |
|------|------|------|
| 流水线标准不一 | 各项目 Jenkins 接入进度和配置差异大，仅靠 Excel 表格手动追踪 | 部署流程混乱，回滚困难 |
| 质量门禁缺失 | ESLint/Prettier/Stylelint/TypeCheck 已有配置但未在 CI 中强制执行；`prettier/prettier` 规则处于关闭状态 | 不合规代码可随意合入主分支 |
| 代码审查依赖人工 | MR 审查全靠人工，标准不一，容易遗漏潜在问题 | 代码质量波动大，隐患上线 |
| 无 GitLab CI 集成 | 未利用 GitLab CI/CD 原生能力，手动操作环节多 | 效率低，无法形成自动化闭环 |
| 构建产物无管控 | 构建产物直接部署，缺少质量报告和产物归档 | 线上问题难以追溯 |

#### 3.6.2 整体方案设计

**核心理念**：以 **GitLab CI/CD** 为自动化引擎，以**本平台**为治理中心，实现"统一标准 → 自动检查 → AI 审查 → 效能度量"的闭环。

```
┌─────────────────────────────────────────────────────────┐
│               GitLab 代码仓库 (各前端项目)                │
│                                                          │
│  developer push → feature branch → Merge Request         │
│                                          │               │
└──────────────────────────────────────────┼───────────────┘
                                           │
                          ┌────────────────┼────────────────┐
                          │  GitLab CI Pipeline (.gitlab-ci.yml) │
                          │                                      │
                          │  Stage 1: install    → pnpm install  │
                          │  Stage 2: lint       → eslint + stylelint + typecheck │
                          │  Stage 3: test       → jest / vitest  │
                          │  Stage 4: build      → vite build    │
                          │  Stage 5: ai-review  → 调用平台 API  │
                          │  Stage 6: deploy     → Jenkins 触发  │
                          │                                      │
                          └───────────────┬────────────────────┘
                                          │
                          ┌───────────────▼────────────────────┐
                          │       前端研发门户 (治理中心)         │
                          │                                      │
                          │  ┌──────────┐ ┌──────────────────┐  │
                          │  │ Pipeline │ │ AI Code Review   │  │
                          │  │ 模板管理  │ │ (MR自动审查)      │  │
                          │  └──────────┘ └──────────────────┘  │
                          │  ┌──────────┐ ┌──────────────────┐  │
                          │  │ 质量门禁  │ │ 构建态势大盘      │  │
                          │  │ 配置     │ │ (多项目聚合)      │  │
                          │  └──────────┘ └──────────────────┘  │
                          └──────────────────────────────────────┘
```

#### 3.6.3 Pipeline 模板管理

**功能描述**：提供标准化的 GitLab CI Pipeline 模板，各前端项目一键接入，告别"各自为政"。

**标准 Pipeline 模板** (`.gitlab-ci.yml`)：

```yaml
# ===== 前端标准化 CI Pipeline 模板 v1.0 =====
# 由前端研发门户统一管理，项目可直接引用或基于此定制

stages:
  - install
  - quality
  - build
  - ai-review
  - deploy

variables:
  NODE_VERSION: "20"
  PNPM_VERSION: "9"

# --- Stage 1: 依赖安装 ---
install:
  stage: install
  image: node:${NODE_VERSION}
  cache:
    key: ${CI_COMMIT_REF_SLUG}-pnpm
    paths:
      - node_modules/
      - .pnpm-store/
  script:
    - npm install -g pnpm@${PNPM_VERSION}
    - pnpm install --frozen-lockfile
  artifacts:
    paths:
      - node_modules/
    expire_in: 1 hour

# --- Stage 2: 代码质量检查 ---
eslint:
  stage: quality
  script:
    - npx eslint "src/**/*.{vue,ts,tsx}" --format json --output-file eslint-report.json || true
    - npx eslint "src/**/*.{vue,ts,tsx}" --format html --output-file eslint-report.html || true
  artifacts:
    reports:
      codequality: eslint-report.json
    paths:
      - eslint-report.html
    when: always

stylelint:
  stage: quality
  script:
    - npx stylelint "src/**/*.{vue,less,css}" --formatter json --output-file stylelint-report.json || true
  artifacts:
    paths:
      - stylelint-report.json
    when: always

typecheck:
  stage: quality
  script:
    - npx vue-tsc --noEmit
  allow_failure: false

prettier-check:
  stage: quality
  script:
    - npx prettier --check "src/**/*.{vue,ts,tsx,less,css,json,md}"

# --- Stage 3: 构建 ---
build:
  stage: build
  script:
    - pnpm build
  artifacts:
    paths:
      - dist/
    expire_in: 7 days

# --- Stage 4: AI 代码审查 ---
ai-review:
  stage: ai-review
  script:
    - |
      curl -X POST "${PLATFORM_API}/fe/cicd/aiReview" \
        -H "X-Gitlab-Token: ${WEBHOOK_SECRET}" \
        -H "Content-Type: application/json" \
        -d "{
          \"projectId\": \"${CI_PROJECT_ID}\",
          \"mergeRequestId\": \"${CI_MERGE_REQUEST_IID}\",
          \"sourceBranch\": \"${CI_MERGE_REQUEST_SOURCE_BRANCH_NAME}\",
          \"targetBranch\": \"${CI_MERGE_REQUEST_TARGET_BRANCH_NAME}\",
          \"commitSha\": \"${CI_COMMIT_SHA}\"
        }"
  only:
    - merge_requests
  allow_failure: true

# --- Stage 5: 部署 (触发 Jenkins) ---
deploy-staging:
  stage: deploy
  script:
    - curl -X POST "${JENKINS_URL}/job/${APP_NAME}/buildWithParameters" \
        --user "${JENKINS_USER}:${JENKINS_TOKEN}" \
        --data "ENV=staging&BRANCH=${CI_COMMIT_REF_NAME}"
  only:
    - develop
  when: manual

deploy-production:
  stage: deploy
  script:
    - curl -X POST "${JENKINS_URL}/job/${APP_NAME}/buildWithParameters" \
        --user "${JENKINS_USER}:${JENKINS_TOKEN}" \
        --data "ENV=production&BRANCH=${CI_COMMIT_REF_NAME}"
  only:
    - main
  when: manual
```

**平台侧模板管理功能**：

| 功能 | 说明 |
|------|------|
| 模板版本管理 | 维护标准 Pipeline 模板版本，项目引用特定版本 |
| 模板定制化 | 允许项目在标准模板基础上增减 Stage |
| 一键接入向导 | 为新项目生成 .gitlab-ci.yml + Webhook 配置脚本 |
| 合规检查 | 定期扫描各项目的 .gitlab-ci.yml 是否偏离标准模板 |

**Pipeline 数据模型**：

```sql
CREATE TABLE `fe_pipeline_template` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `version` varchar(20) NOT NULL COMMENT '版本号',
  `content` longtext NOT NULL COMMENT 'YAML模板内容',
  `description` text COMMENT '模板描述',
  `stages` json DEFAULT NULL COMMENT '包含的Stage列表',
  `is_default` tinyint DEFAULT 0 COMMENT '是否为默认模板',
  `status` varchar(10) DEFAULT 'enable' COMMENT '状态: enable/disable',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Pipeline模板表';

CREATE TABLE `fe_project_pipeline` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `app_id` varchar(36) NOT NULL COMMENT '关联应用ID',
  `gitlab_project_id` varchar(36) NOT NULL COMMENT 'GitLab项目ID',
  `gitlab_url` varchar(500) NOT NULL COMMENT 'GitLab仓库地址',
  `pipeline_template_id` varchar(36) DEFAULT NULL COMMENT '使用的Pipeline模板ID',
  `pipeline_override` text COMMENT '自定义覆盖的YAML片段',
  `jenkins_url` varchar(500) DEFAULT NULL COMMENT 'Jenkins Job地址',
  `jenkins_status` varchar(20) DEFAULT 'not_configured' COMMENT 'Jenkins接入状态: not_configured/configuring/active/error',
  `webhook_configured` tinyint DEFAULT 0 COMMENT 'Webhook是否已配置',
  `quality_gate_enabled` tinyint DEFAULT 1 COMMENT '质量门禁是否开启',
  `ai_review_enabled` tinyint DEFAULT 0 COMMENT 'AI审查是否开启',
  `last_pipeline_status` varchar(20) DEFAULT NULL COMMENT '最近一次流水线状态',
  `last_pipeline_time` datetime DEFAULT NULL COMMENT '最近一次流水线时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_gitlab_project_id` (`gitlab_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目Pipeline配置表';
```

#### 3.6.4 AI 代码审查 (MR Auto Review)

**功能描述**：当开发者提交 Merge Request 时，平台自动接收 GitLab Webhook 事件，调用 AI 对 MR 的 diff 进行智能审查，将审查意见自动回写到 MR 评论中。

**交互流程**：

```
开发者提交 MR → GitLab 触发 Webhook → 平台接收 MR 事件
                                          │
                                          ▼
                                  获取 MR diff 内容
                                          │
                                          ▼
                              拼装审查 Prompt (Skill + diff)
                                          │
                                          ▼
                                  AI 分析代码变更
                                          │
                                  ┌───────┴───────┐
                                  ▼               ▼
                            发现问题          代码合规
                                  │               │
                                  ▼               ▼
                          回写 MR 评论       审查通过标记
                          (含修改建议)       (👍 Approve)
```

**AI 审查维度**（由 Skill 定义）：

| 审查维度 | 检查内容 | 对应 Skill |
|---------|---------|-----------|
| 代码规范 | 命名、格式、Vue 组件结构 | bus-code-style |
| 类型安全 | any 使用、类型断言、泛型约束 | bus-ts-standard |
| 安全审查 | XSS、敏感信息、权限校验 | bus-security-check |
| 性能隐患 | 不必要的重渲染、大包引入、内存泄漏 | bus-performance |
| 业务逻辑 | 与业务规范的一致性 | 应用级 Skill |

**AI 审查结果格式**（回写到 MR 的评论）：

```markdown
## 🤖 AI Code Review Report

**审查范围**: 15 files changed, +320 / -45 lines

### 🔴 严重问题 (2)
1. **[src/utils/auth.ts:12]** 硬编码 Token 存在安全风险
   > 建议使用环境变量 `import.meta.env.VITE_TOKEN_KEY` 替代硬编码字符串

2. **[src/views/bus/VehicleList.vue:58]** `v-for` 缺少 `:key` 绑定
   > Vue3 要求 v-for 必须指定唯一 key，建议使用 `:key="item.id"`

### 🟡 建议优化 (3)
1. **[src/api/bus/fleet.ts:23]** 响应类型使用了 `any`
   > 建议定义 `FleetListResult` 接口替代 `any`

2. **[src/views/bus/OrderForm.vue:145]** 组件内直接调用 `message.success()`
   > 建议使用 `useMessage()` hook 保持一致性

3. **[src/components/BusSelector.vue:30]** 大量逻辑写在 setup 顶层
   > 建议拆分为 `useBusSelector` composable 提高可测试性

### ✅ 合规项 (10)
- TypeScript 严格模式无 any
- API 层统一使用 defHttp
- 组件命名符合 PascalCase 规范
- ...

---
*Powered by 前端研发门户 AI Review* | 审查耗时: 3.2s | Token: 2,450
```

**质量门禁规则**：

| 门禁规则 | 条件 | 行为 |
|---------|------|------|
| Lint 通过 | ESLint / Stylelint 零 error | 不通过则 MR 不可合入 |
| 类型检查通过 | `vue-tsc --noEmit` 无 error | 不通过则 MR 不可合入 |
| AI 审查无严重问题 | AI Review 无 🔴 严重问题 | 不通过则需人工确认 |
| 构建成功 | `vite build` 无 error | 不通过则 MR 不可合入 |

**GitLab 项目设置**（通过平台一键配置）：
- Settings → Merge Requests → **Pipeline must succeed** ✓
- Settings → Merge Requests → **Only allow merge requests to be merged if all discussions are resolved** ✓
- Settings → Webhooks → 添加平台 Webhook URL（MR 事件）

#### 3.6.5 构建态势大盘

**功能描述**：聚合展示所有前端项目的 CI/CD 运行状态、构建趋势和质量指标，替代原来手动的 Excel 表格。

**页面清单**：

| 页面 | 路由 | 说明 |
|------|------|------|
| CI/CD 总览 | `/fe/cicd/overview` | 全部项目流水线状态矩阵 |
| 项目详情 | `/fe/cicd/project/:id` | 单项目 Pipeline 历史与质量趋势 |
| Pipeline 模板管理 | `/fe/cicd/templates` | 管理标准 Pipeline 模板 |
| 质量门禁配置 | `/fe/cicd/gate` | 全局/项目级门禁规则配置 |
| AI 审查日志 | `/fe/cicd/ai-reviews` | AI 代码审查历史记录 |

**CI/CD 总览页面布局**：

```
┌─────────┬─────────┬─────────┬─────────┐
│ 活跃项目 │ 本周构建 │ 成功率   │ AI审查数 │
│   12    │  156    │ 94.2%   │  89    │
└─────────┴─────────┴─────────┴─────────┘

┌─────────────────────────────────────────┐
│  项目流水线状态矩阵                       │
│                                          │
│  项目名      │ Jenkins │ CI │ 上次构建  │ AI审查 │ 质量 │
│  ───────────┼─────────┼────┼──────────┼───────┼──────│
│  车队管理系统 │ ✅活跃   │ ✅ │ 2h ago  │ ✅ 2  │ A   │
│  票务平台    │ ✅活跃   │ ⚠️ │ 1d ago  │ ✅ 5  │ B   │
│  调度大屏    │ ⏳配置中 │ ❌ │ -       │ ❌ 0  │ -   │
│  运营后台    │ ✅活跃   │ ✅ │ 30m ago │ ✅ 12 │ A   │
│  ...        │         │    │         │       │     │
└─────────────────────────────────────────┘
```

**构建态势数据模型**：

```sql
CREATE TABLE `fe_pipeline_run` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `project_pipeline_id` varchar(36) NOT NULL COMMENT '关联项目Pipeline配置ID',
  `gitlab_pipeline_id` varchar(36) DEFAULT NULL COMMENT 'GitLab Pipeline ID',
  `branch` varchar(200) NOT NULL COMMENT '分支名',
  `commit_sha` varchar(40) DEFAULT NULL COMMENT '提交SHA',
  `commit_message` text COMMENT '提交信息',
  `trigger_user` varchar(50) DEFAULT NULL COMMENT '触发人',
  `status` varchar(20) NOT NULL COMMENT '状态: running/success/failed/canceled',
  `stage_results` json DEFAULT NULL COMMENT '各Stage执行结果(JSON)',
  `started_at` datetime DEFAULT NULL COMMENT '开始时间',
  `finished_at` datetime DEFAULT NULL COMMENT '结束时间',
  `duration_ms` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_status` (`project_pipeline_id`, `status`),
  KEY `idx_started_at` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Pipeline运行记录表';

CREATE TABLE `fe_ai_review_log` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `project_pipeline_id` varchar(36) NOT NULL COMMENT '关联项目Pipeline配置ID',
  `merge_request_id` varchar(36) NOT NULL COMMENT 'GitLab MR ID',
  `merge_request_url` varchar(500) DEFAULT NULL COMMENT 'MR链接',
  `source_branch` varchar(200) DEFAULT NULL COMMENT '源分支',
  `target_branch` varchar(200) DEFAULT NULL COMMENT '目标分支',
  `diff_content` longtext COMMENT 'MR diff内容',
  `review_result` longtext COMMENT 'AI审查结果(Markdown)',
  `critical_count` int DEFAULT 0 COMMENT '严重问题数',
  `warning_count` int DEFAULT 0 COMMENT '警告数',
  `pass_count` int DEFAULT 0 COMMENT '合规项数',
  `skill_codes` varchar(500) DEFAULT NULL COMMENT '使用的Skill编码列表',
  `model_name` varchar(50) DEFAULT NULL COMMENT '使用的AI模型',
  `input_tokens` int DEFAULT 0 COMMENT '输入Token数',
  `output_tokens` int DEFAULT 0 COMMENT '输出Token数',
  `duration_ms` int DEFAULT 0 COMMENT '审查耗时(毫秒)',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: pending/running/completed/failed',
  `comment_posted` tinyint DEFAULT 0 COMMENT '是否已回写MR评论',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_time` (`project_pipeline_id`, `create_time`),
  KEY `idx_mr_id` (`merge_request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI代码审查日志表';
```

#### 3.6.6 一键接入流程

**新项目接入 CI/CD 标准化只需 3 步**：

```
Step 1: 平台注册
  └→ 在"应用管理"中注册项目，填写 GitLab 仓库地址
  └→ 选择 Pipeline 模板版本
  └→ 配置 Jenkins Job 关联

Step 2: 生成配置
  └→ 平台一键生成 .gitlab-ci.yml（基于模板 + 项目定制）
  └→ 平台一键生成 GitLab Webhook 配置脚本
  └→ 平台一键生成 Jenkins Job 配置

Step 3: 执行接入
  └→ 将 .gitlab-ci.yml 提交到项目根目录
  └→ 执行 Webhook 配置脚本
  └→ 提交测试 MR 验证全链路
```

**接入状态追踪**（替代手动 Excel 表格）：

| 状态 | 说明 | 颜色标记 |
|------|------|---------|
| 未接入 | 尚未开始配置 | 🔴 灰色 |
| 配置中 | Pipeline / Webhook 配置中 | 🟡 黄色 |
| 已接入 | Pipeline 运行中，AI 审查未开启 | 🔵 蓝色 |
| 全功能 | Pipeline + AI 审查 + 质量门禁全部就绪 | 🟢 绿色 |

---

## 4. API 接口设计

### 4.1 Skill 管理 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 获取 Skill 列表 | GET | `/fe/skill/list` | 支持分页、分类筛选 |
| 获取 Skill 详情 | GET | `/fe/skill/queryById` | 按 ID 查询 |
| 创建 Skill | POST | `/fe/skill/add` | 新增 Skill |
| 编辑 Skill | PUT | `/fe/skill/edit` | 更新 Skill |
| 删除 Skill | DELETE | `/fe/skill/delete` | 删除 Skill |
| 测试 Skill | POST | `/fe/skill/test` | 用测试输入验证 Skill 输出 |
| 导出 MCP 配置 | POST | `/fe/skill/exportMcpConfig` | 生成 mcp-config.json |
| 同步官方 Skills | POST | `/fe/skill/syncOfficial` | 从 Jeecg Skills 仓库同步 |

### 4.2 应用管理 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 获取应用列表 | GET | `/fe/application/list` | 支持分页、状态筛选 |
| 获取应用详情 | GET | `/fe/application/queryById` | 按 ID 查询 |
| 创建应用 | POST | `/fe/application/add` | 新增应用 |
| 编辑应用 | PUT | `/fe/application/edit` | 更新应用 |
| 删除应用 | DELETE | `/fe/application/delete` | 删除应用 |
| 绑定 Skill | POST | `/fe/application/bindSkills` | 为应用绑定/解绑 Skill |
| 获取应用 Skill | GET | `/fe/application/skills` | 获取应用关联的 Skill 列表 |

### 4.3 效能数据 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 记录使用日志 | POST | `/fe/aiUsage/log` | 上报 AI 使用行为 |
| 效能总览 | GET | `/fe/aiUsage/overview` | 聚合统计概览数据 |
| 团队效能 | GET | `/fe/aiUsage/teamStats` | 按团队维度统计 |
| 个人效能 | GET | `/fe/aiUsage/personalStats` | 按个人维度统计 |
| Skill 热力数据 | GET | `/fe/aiUsage/skillHeat` | 各 Skill 使用频次与采纳率 |
| 趋势数据 | GET | `/fe/aiUsage/trend` | 按时间维度趋势 |

### 4.4 低代码服务 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| SQL 转 Schema | POST | `/fe/lowcode/sql2schema` | DDL 转 Online 表单配置 |
| Schema 预览 | POST | `/fe/lowcode/schemaPreview` | 预览生成的表单配置 |
| 沙箱渲染 | POST | `/fe/lowcode/sandbox/render` | 在沙箱中渲染组件 |
| Chat2Code 对话 | POST | `/fe/lowcode/chat2code` | 对话式代码生成(流式) |

### 4.5 CI/CD 治理 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 接收 GitLab Webhook | POST | `/fe/cicd/webhook` | 接收 GitLab MR/Push 事件 |
| 触发 AI 代码审查 | POST | `/fe/cicd/aiReview` | 对 MR diff 执行 AI 审查 |
| 回写 MR 评论 | POST | `/fe/cicd/postComment` | 将审查结果回写到 GitLab MR |
| 获取项目 CI 状态 | GET | `/fe/cicd/projectStatus` | 获取项目的 CI/CD 运行状态 |
| 获取 Pipeline 运行记录 | GET | `/fe/cicd/pipelineRuns` | 分页查询 Pipeline 运行历史 |
| 获取 AI 审查记录 | GET | `/fe/cicd/aiReviews` | 分页查询 AI 审查历史 |
| 获取 CI 总览数据 | GET | `/fe/cicd/overview` | 全项目 CI/CD 聚合数据 |
| 管理 Pipeline 模板 | CRUD | `/fe/cicd/template/*` | Pipeline 模板增删改查 |
| 生成项目 CI 配置 | POST | `/fe/cicd/generateConfig` | 一键生成 .gitlab-ci.yml |
| 同步 GitLab 项目 | POST | `/fe/cicd/syncGitlabProjects` | 从 GitLab 拉取项目列表 |

---

## 5. 前端路由与菜单规划

### 5.1 菜单结构

```
前端研发门户
├── AI 开发者中心
│   ├── Skill 资产库        /ai-hub/skills
│   ├── MCP 配置管理        /ai-hub/mcp          (复用已有页面)
│   └── 配置导出            /ai-hub/export
├── 低代码工坊
│   ├── 对话出码            /lowcode/chat2code
│   ├── Schema 转换         /lowcode/sql2schema
│   └── 沙箱预览            /lowcode/sandbox
├── 业务管理
│   ├── 应用管理            /fe/app/list
│   ├── 人员管理            /fe/developer/list
│   └── 团队管理            /fe/team/list
├── CI/CD 治理
│   ├── 构建态势总览        /fe/cicd/overview
│   ├── 项目CI详情          /fe/cicd/project/:id
│   ├── Pipeline 模板管理   /fe/cicd/templates
│   ├── 质量门禁配置        /fe/cicd/gate
│   └── AI 审查日志         /fe/cicd/ai-reviews
├── 效能看板
│   ├── 效能总览            /fe/dashboard/overview
│   ├── 团队效能            /fe/dashboard/team
│   ├── 个人效能            /fe/dashboard/personal
│   └── Skill 热力图        /fe/dashboard/skill-heat
└── 代码医生
    └── 代码诊断            /code-doctor/analyze
```

### 5.2 权限设计

| 角色 | Skill 管理 | 应用管理 | 人员管理 | 效能看板 | 代码医生 | CI/CD 治理 |
|------|-----------|---------|---------|---------|---------|-----------|
| 管理员 | 增删改查 | 增删改查 | 增删改查 | 全部数据 | 全功能 | 全功能 |
| 团队负责人 | 查看/编辑 | 查看/编辑 | 查看本团队 | 团队数据 | 全功能 | 查看本团队项目 |
| 开发者 | 查看/导出 | 查看本人应用 | 查看本人 | 个人数据 | 全功能 | 查看本人项目 |

---

## 6. 技术实施路线图

### 第一阶段：基础设施与 AI 核心 (Week 1-4)

| 周次 | 任务 | 交付物 |
|------|------|--------|
| W1 | 部署 JeecgBoot 3.9.2 开发环境 | 可运行的平台底座 |
| W1 | 配置大模型接入 (DeepSeek/Ollama) | AI 对话能力可用 |
| W2 | 创建 fe_skill 表，实现 Skill CRUD | Skill 资产库基础功能 |
| W2 | 集成 Jeecg 官方 Skills 数据 | 5 个官方 Skill 可选 |
| W3 | 实现 mcp-config.json 导出功能 | Claude Code 可通过 MCP 连接平台 |
| W3 | 部署平台 MCP Server (SSE 模式) | 本地 IDE 可调用平台 Skill |
| W4 | 实现 Chat2Code 基础对话界面 | Web 端可对话生代码 |
| W4 | 创建 fe_pipeline_template / fe_project_pipeline 表 | Pipeline 模板与项目配置基础 |
| W4 | 端到端测试：Skill → MCP → Claude Code → 代码生成 | 完整链路验证 |

**里程碑**：MVP 验证 —— "Skill + MCP + Claude Code" 全链路跑通

### 第二阶段：业务平移与 Skill 增强 (Week 5-8)

| 周次 | 任务 | 交付物 |
|------|------|--------|
| W5 | 创建 fe_application 表，实现应用管理 CRUD | 应用管理 V2 |
| W5 | 创建 fe_team / fe_developer 表，实现人员管理 | 人员管理 V2 |
| W6 | 编写"公司通用代码规范 Skill" | bus-code-style Skill |
| W6 | 编写"公司接口规范 Skill" | bus-api-standard Skill |
| W7 | 实现应用级 Skill 绑定功能 | 应用可关联专属 Skill |
| W7 | 实现 SQL DDL → Online Schema 转换 | Schema 转换引擎 |
| W8 | 实现可视化沙箱 (iframe 隔离渲染) | 组件实时预览 |
| W8 | 实现标准 Pipeline 模板管理 + 一键生成 .gitlab-ci.yml | CI 模板管理 |
| W8 | 业务数据迁移 (旧平台 → 新平台) | 数据迁移完成 |

**里程碑**：业务功能完整 —— 旧平台功能全部迁入 + 业务 Skill 初步建成

### 第三阶段：全流程覆盖与效能度量 (Week 9-12)

| 周次 | 任务 | 交付物 |
|------|------|--------|
| W9 | 创建 fe_ai_usage_log 表，实现数据采集 | AI 使用行为追踪 |
| W9 | 实现效能总览看板 | 核心指标可视化 |
| W10 | 实现团队/个人效能看板 | 多维度效能分析 |
| W10 | 实现 Skill 热力图 | Skill 使用情况可视化 |
| W11 | 实现在线代码医生基础版 | 代码分析 + 重构建议 |
| W11 | 实现 GitLab Webhook 接收 + AI MR 代码审查 | MR 自动审查功能 |
| W11 | 实现构建态势大盘 + 项目 CI 状态矩阵 | CI/CD 可视化治理 |
| W11 | 编写剩余业务 Skills (fleet/ticket 等) | 业务 Skill 覆盖率达到 80% |
| W12 | 实现 AI 审查结果回写 GitLab MR 评论 | 审查闭环 |
| W12 | 全链路集成测试与性能优化 | 生产就绪 |
| W12 | 产出《AI 研发效能提升报告》 | 绩效支撑材料 |

**里程碑**：全功能上线 —— 效能量化 + 代码医生 + Skill 体系完善

---

## 7. 风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| 大模型 API 不稳定 | Chat2Code / MCP 服务不可用 | 支持 Ollama 本地模型兜底；多模型切换 |
| Skill 质量参差不齐 | AI 生成代码不符合预期 | 建立 Skill 评审机制；上线前必测 |
| 开发者使用意愿低 | 效能数据不足，无法证明提效 | 降低接入门槛（一键配置）；设立激励机制 |
| MCP 连接安全风险 | 代码泄露 | Token 鉴权 + IP 白名单 + 操作审计日志 |
| 旧平台数据迁移 | 数据丢失/不一致 | 分批迁移 + 双跑验证 + 回滚方案 |
| GitLab Webhook 可靠性 | AI 审查事件丢失 | Webhook 重试机制 + 事件队列表 + 手动触发兜底 |
| AI 审查误报 | 开发者对 AI 审查产生反感 | 审查结果分为"严重/建议/合规"三级，严重才阻断；持续优化 Skill |
| GitLab CI Runner 资源不足 | Pipeline 排队等待 | 配置专用 Runner + 缓存优化 + 按需扩容 |

---

## 8. 绩效汇报点

### 8.1 架构创新
- 引入 **MCP 协议**，打造公司级 **AI Skill 调度中心**，使 AI 具备业务"上下文"
- 首创"**Skill 三级体系**"（官方 → 通用业务 → 应用级），实现从平台能力到业务场景的精准映射
- 基于 JeecgBoot 3.9.2 低代码底座，实现"**低代码 + AI**"双轮驱动
- 打造 **"GitLab CI + AI 审查 + 平台治理"** 三位一体的前端 CI/CD 治理体系，实现代码质量左移

### 8.2 提效显著
- 通过"**Skill + Claude Code**"模式，标准业务模块开发周期缩短 **40%+**
- **Chat2Code** 让非资深开发者也能产出高质量代码，降低团队技术门槛
- **Schema 转换引擎** 将数据库设计到前端页面的链路从"天"级压缩到"分钟"级
- **AI MR 审查** 将代码审查从人工全量审查升级为"AI 预审 + 人工确认"模式，审查效率提升 **3 倍**
- **标准化 Pipeline** 消除各项目 CI/CD 配置差异，新项目接入时间从"天"级压缩到"分钟"级

### 8.3 资产沉淀
- 将零散的开发经验转化为可被 AI 调用的"**Skill 资产**"，实现技术标准的强制落地
- **应用级 Skill** 机制让每个项目的业务知识不再随人员流动而丢失
- **效能看板** 让 AI 提效成果可量化、可汇报，为后续投入提供数据支撑
- **CI/CD 治理** 将分散在各项目的流水线经验沉淀为标准化模板和门禁规则

### 8.4 可量化成果目标

| 指标 | 3个月目标 | 6个月目标 |
|------|----------|----------|
| AI 生成代码占比 | 15% | 30% |
| Skill 覆盖核心业务场景 | 80% | 95% |
| 开发者日活率 | 50% | 80% |
| 代码采纳率 | >70% | >80% |
| 工时节省 | 200h/月 | 500h/月 |
| 前端项目 CI 标准化覆盖率 | 60% | 100% |
| AI MR 审查覆盖率 | 30% | >80% |
| MR 审查平均响应时间 | <30min | <10min |
