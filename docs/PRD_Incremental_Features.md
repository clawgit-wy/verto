# 前端研发平台 PRD（增量功能版）

## 1. 项目背景

### 1.1 现状盘点

| 模块 | 状态 | 说明 |
|------|------|------|
| 应用列表 | ✅ 已完成 | 应用简称、所属领域、应用等级、负责人、Git 仓库等基础信息管理 |
| 人员列表 | ✅ 已完成 | 姓名、工号、邮箱、技能标签、状态等人员信息管理 |
| 低代码工具 | ✅ 已完成 | 基于 JeecgBoot Online 表单抽离的低代码开发工具 |
| 应用模版中心 | 🆕 待建 | 基于模版快速创建应用，支持下载或直接创建 GitLab 仓库 |
| CI/CD 治理 | 🆕 待建 | 统一接入 Jenkins，管理流水线、构建记录、MR 质量 |
| AI 提效中心 | 🆕 待建 | Skill 资产库、Chat2Code 对话出码、MCP 配置导出 |
| 效能看板 | 🆕 待建 | 统一数据统计，支持年终汇报生成 |

### 1.2 项目目标

- **提效**：通过模版 + AI + CI/CD 标准化，缩短应用开发周期 40%+
- **规范**：统一 CI/CD 标准，100% 覆盖前端项目
- **量化**：AI 生成代码量、节省工时可追踪，年终汇报有数据支撑

---

## 2. 功能模块设计

### 2.1 应用模版中心

#### 2.1.1 功能概述

基于 GitLab 仓库中的前端模版进行可视化管理，在线页面提供模版管理和应用创建功能，记录创建历史。模版包含基础技术栈信息，支持与 CI/CD 流程联动。

#### 2.1.2 核心功能

| 功能 | 说明 |
|------|------|
| 模版列表 | 卡片视图展示模版，支持上架/下架/置顶，显示 GitLab 仓库地址和最新 commit；根据权限过滤（普通用户只看自己的，管理员看全部） |
| 模版管理 | 新建模版、编辑模版基本信息、删除模版、查看模版详情；配置技术栈和框架版本信息 |
| 模版版本管理 | 支持创建版本（打 tag）、切换版本、查看版本差异；记录版本对应的技术栈信息 |
| 应用创建向导 | 选模版 → 选版本 → 填配置 → 下载/建 GitLab 仓库；自动继承模版技术栈信息 |
| 创建记录 | 记录谁、何时、用何模版版本创建了什么应用；根据权限过滤（普通用户只看自己的，管理员看全部） |
| 技术栈统计 | 统计各技术栈使用情况，生成部门应用技术栈分布报告 |

#### 2.1.3 权限管理

**权限模型**
- **管理员**：可以查看、编辑、删除所有模版，查看所有创建记录
- **普通用户**：只能查看、编辑、删除自己创建的模版，只能查看自己的创建记录

**权限控制点**
- 模版列表：根据 `create_by` 字段过滤数据
- 模版编辑：校验当前用户是否为模版创建者或管理员
- 模版删除：校验当前用户是否为模版创建者或管理员
- 创建记录：根据 `creator` 字段过滤数据

#### 2.1.4 关键配置项

- **模版配置**
  - 模版名称、编码（唯一标识）
  - GitLab 仓库地址（支持 HTTPS/SSH）
  - 分支选择（默认 main/master）
  - 占位变量声明（JSON 格式配置）
  - 模版描述、技术栈标签
  - 权限范围（公开/私有，私有仅创建者可见）

- **应用创建配置**
  - 应用简称 / 编码（自动写入应用列表）
  - 基础路由前缀
  - 服务端口
  - 数据库连接信息（加密存储）
  - 关联负责人（复用人员列表）
  - 所属领域 / 应用等级（复用数据字典）
  - 目标 GitLab 仓库（可选，直接创建）

#### 2.1.5 数据表

```sql
fe_template            -- 模版主表（id, name, code, gitlab_url, branch, status, sort_no, visibility, create_by, tech_stack_id, framework, framework_version, node_version_range...）
fe_template_version    -- 模版版本（id, template_id, version, git_tag, commit_sha, tech_stack_id, framework, framework_version, create_time）
fe_app_create_record   -- 应用创建记录（id, template_id, version_id, app_id, params, output_type, gitlab_url, creator, create_time）
```

**字段说明**
- `fe_template.visibility`：可见性（public=公开，private=私有）
- `fe_template.create_by`：创建人，用于权限过滤
- `fe_template.tech_stack_id`：关联技术栈ID（关联 fe_tech_stack）
- `fe_template.framework`：框架名称（Vue2/Vue3/React/Angular/jQuery）
- `fe_template.framework_version`：框架版本
- `fe_template.node_version_range`：推荐的 Node 版本范围
- `fe_template_version.tech_stack_id`：版本对应的技术栈ID
- `fe_template_version.framework`：版本对应的框架名称
- `fe_template_version.framework_version`：版本对应的框架版本
- `fe_app_create_record.creator`：创建人，用于权限过滤

#### 2.1.6 技术实现方案

**GitLab 集成**
- 使用 GitLab API v4 进行仓库操作
- 支持个人访问令牌（Personal Access Token）和 OAuth 两种认证方式
- 仓库地址格式：`https://gitlab.com/{group}/{project}.git`

**变量替换引擎**
- 使用 Mustache 模板引擎进行变量替换
- 支持嵌套变量和条件判断
- 变量来源：用户输入 + 数据字典 + 系统默认值

**应用创建流程**
```
1. 拉取模版仓库代码（指定版本）
2. 解析占位变量
3. 替换变量值
4. 打包成 zip
5. 根据输出类型处理：
   - 下载：返回 zip 文件流
   - GitLab 仓库：调用 GitLab API 创建仓库 + 推送代码
6. 记录创建日志
```

**权限控制实现**
- 后端接口统一校验用户权限
- 查询接口：根据用户角色和 `create_by` 字段过滤数据
- 编辑/删除接口：校验当前用户是否为创建者或管理员
- 使用 Shiro 注解进行权限控制：`@RequiresPermissions("feplatform:template:edit")`

---

### 2.2 CI/CD 治理

#### 2.2.1 功能概述

统一接入 Jenkins，管理流水线、构建记录、MR 质量，实现 CI/CD 标准化。支持测试环境和生产环境双 Jenkins 实例，统一 Node 版本管理，支持制品库上传和在线部署。

#### 2.2.2 核心功能

| 功能 | 说明 |
|------|------|
| Jenkins 实例管理 | 维护多套 Jenkins（测试环境/生产环境），包括地址、Token、域、环境类型 |
| 技术栈管理 | 支持多技术栈（Vue2/Vue3/React/Angular/jQuery/微前端），自动识别或手动指定 |
| Node 版本管理 | 统一管理 Node 版本，支持按应用/环境配置不同版本，强制版本检查（可配置豁免） |
| 流水线管理 | 应用绑定多条流水线（dev/test/prod），保存 Jenkinsfile 模版，支持测试/生产环境差异化配置 |
| 一键接入向导 | 选择应用 → 关联模板/自动识别技术栈 → 选 Jenkins 实例 → 选标准 Jenkinsfile → 配置 Node 版本 → 选择检查级别 → 创建 Job |  
| 构建记录 | 拉取构建历史，展示状态、耗时、触发人、变更 commit、Node 版本、制品信息、技术栈、模板版本 |
| 触发与回滚 | 平台内构建/中止，常用环境支持回滚到上次成功版本 |
| 制品库管理 | 支持上传构建产物到制品库（Nexus/Artifactory），记录制品版本信息 |
| MR 质量看板 | 接收 GitLab Webhook，记录 MR 数量、Lint/TypeCheck 通过率、AI Review 命中率 |
| 标准化检查 | 维护 CI 必备项清单（按技术栈分级），支持严格/标准/宽松三种检查级别，对每个应用打分 |
| 部署策略配置 | 测试环境：自动部署 OR 上传制品库；生产环境：在线部署 |
| 微前端支持 | 支持主应用和子应用分别配置，支持子应用独立构建和部署 |
| 模板版本检查 | 构建时检查应用关联的模板版本，记录模板变更历史 |
| 技术栈统计 | 自动采集应用技术栈信息，生成部门技术栈分布报告 |

#### 2.2.3 部署策略

**测试环境 Jenkins**
- **自动部署模式**：构建成功后自动部署到测试服务器
- **制品库模式**：构建成功后上传制品到制品库，等待手动部署
- **触发方式**：GitLab Webhook（MR 合并/推送）、手动触发、定时触发

**生产环境 Jenkins**
- **在线部署模式**：从制品库拉取指定版本，直接部署到生产服务器
- **触发方式**：手动触发（需审批）、定时触发（维护窗口）
- **回滚机制**：支持快速回滚到上一个稳定版本

**部署流程**
```
测试环境：
代码提交 → MR 合并 → GitLab Webhook 触发 Jenkins 构建
   ↓
检查 Node 版本、依赖锁文件、Lint、TypeCheck
   ↓
构建产物（dist/）
   ↓
[自动部署模式] → 部署到测试服务器
[制品库模式] → 上传到制品库（Nexus/Artifactory）

生产环境：
选择制品版本 → 手动触发 Jenkins 部署
   ↓
从制品库拉取产物
   ↓
部署到生产服务器
   ↓
健康检查 → 回滚（如失败）
```

#### 2.2.4 技术栈管理

**支持的技术栈**
| 技术栈 | 识别方式 | Node 版本要求 | Lint 工具 | 构建工具 |
|--------|---------|-------------|----------|---------|
| Vue2 | `package.json` 中有 `vue@^2.x` | v14-v16 | ESLint + eslint-plugin-vue | Webpack |
| Vue3 | `package.json` 中有 `vue@^3.x` | v16+ | ESLint + eslint-plugin-vue | Vite/Webpack |
| React | `package.json` 中有 `react` | v14+ | ESLint + eslint-plugin-react | Webpack/Vite |
| Angular | `package.json` 中有 `@angular/core` | v14-v18 | AngularLint | AngularCLI |
| jQuery | 无框架依赖，手动指定 | v12+ | ESLint（宽松配置） | Webpack/Gulp |
| 微前端 | `package.json` 中有 `qiankun`/`single-spa` | v16+ | 主应用+子应用分别检查 | Webpack |

**技术栈自动识别**
- 扫描 `package.json`，根据依赖自动识别技术栈
- 如果无法识别，提示用户手动指定
- 支持混合技术栈（如微前端）

**技术栈配置**
- 每个技术栈维护一套默认的 Jenkinsfile 模版
- 每个技术栈维护一套默认的 Lint 配置
- 支持用户自定义 Jenkinsfile 和 Lint 配置

#### 2.2.5 Node 版本管理

**版本统一策略**
- 平台维护公司标准 Node 版本列表（如 v14.21.3、v16.20.2、v18.19.0、v20.10.0）
- 每个技术栈推荐一个 Node 版本范围
- 每个应用在接入 CI 时必须选择一个标准版本（可配置豁免）
- 构建时强制检查 `.nvmrc` 或 `.node-version` 文件，版本不匹配则构建失败（可配置警告）

**版本配置方式**
```yaml
# Jenkinsfile 模版
node_version: ${NODE_VERSION}  # 从平台配置读取
```

**版本检查规则**
- 检查 `.nvmrc` 或 `.node-version` 文件是否存在（可配置豁免）
- 检查文件中的版本是否在平台标准版本列表中
- 检查是否与应用配置的 Node 版本一致
- 不满足任一条件则构建失败并提示（可配置为警告）

**豁免机制**
- 老旧项目可以申请豁免 Node 版本检查
- 豁免需要管理员审批
- 豁免项目在构建记录中标记"版本豁免"

#### 2.2.6 模板版本检查与技术栈联动

**模板版本检查**
- CI/CD 构建时自动检查应用关联的模板版本
- 对比当前应用使用的模板版本与模板最新版本
- 如果应用使用的模板版本过旧，发出警告或提示更新
- 记录模板版本变更历史，支持追溯

**技术栈信息采集**
- **来源优先级**：应用关联的模板信息 > package.json 扫描 > 手动配置
- **采集时机**：应用接入 CI/CD 时、每次构建时自动更新
- **采集内容**：框架名称、框架版本、Node 版本、构建工具、包管理器

**技术栈自动识别流程**
```
1. 检查应用是否关联了模板
   ├─ 是 → 从模板获取技术栈信息（framework, framework_version, node_version_range）
   └─ 否 → 扫描 package.json
      ├─ 识别框架类型和版本
      ├─ 识别包管理器（npm/yarn/pnpm）
      └─ 识别构建工具（webpack/vite/angular-cli）
2. 将识别结果保存到应用信息表
3. 更新技术栈统计数据
```

**package.json 扫描规则**
| 依赖标识 | 框架类型 | 版本提取方式 |
|----------|---------|-------------|
| `vue@^2.x` | Vue2 | 提取 version 字段 |
| `vue@^3.x` | Vue3 | 提取 version 字段 |
| `react` | React | 提取 version 字段 |
| `@angular/core` | Angular | 提取 version 字段 |
| `jquery` | jQuery | 提取 version 字段 |
| `qiankun` | 微前端-qiankun | 标记为微前端类型 |
| `single-spa` | 微前端-single-spa | 标记为微前端类型 |
| `@module-federation/core` | 微前端-MF | 标记为微前端类型 |

**应用信息同步**
- 将采集到的技术栈信息同步到应用列表
- 支持手动编辑和自动更新两种模式
- 更新记录保留变更历史

**技术栈统计**
- 统计部门内各技术栈使用数量和占比
- 统计各框架版本分布情况
- 生成技术栈分布报告，支持导出

#### 2.2.7 分级检查策略

**检查级别**
| 级别 | 适用场景 | 检查项 | 失败处理 |
|------|---------|--------|---------|
| 严格 | 新项目、核心业务 | 所有检查项 | 构建失败 |
| 标准 | 大部分项目 | 核心检查项 | 构建失败 |
| 宽松 | 老旧项目、维护项目 | 基础检查项 | 构建警告 |

**检查项分级**

**基础检查项（所有级别）**
- 依赖锁文件：必须有 `package-lock.json` 或 `yarn.lock`
- 构建脚本：必须有 `build` 脚本
- Node 版本：在推荐版本范围内（可豁免）

**核心检查项（标准+严格）**
- Lint 检查：必须配置 Lint 并通过检查
- 环境变量：必须有 `.env.example` 文件

**完整检查项（严格）**
- TypeCheck：TypeScript 项目必须通过类型检查
- 单元测试：必须有测试脚本且通过测试
- 代码覆盖率：测试覆盖率 ≥ 60%

**不同技术栈的 Lint 配置**

**Vue2/Vue3**
```json
{
  "extends": ["eslint:recommended", "plugin:vue/recommended"],
  "rules": {
    "vue/no-unused-vars": "error",
    "no-console": "warn"
  }
}
```

**React**
```json
{
  "extends": ["eslint:recommended", "plugin:react/recommended"],
  "rules": {
    "react/prop-types": "warn",
    "no-console": "warn"
  }
}
```

**Angular**
```json
{
  "extends": ["eslint:recommended", "@angular-eslint/recommended"],
  "rules": {
    "@angular-eslint/directive-selector": "error"
  }
}
```

**jQuery（宽松配置）**
```json
{
  "extends": ["eslint:recommended"],
  "rules": {
    "no-console": "off",
    "no-unused-vars": "warn"
  }
}
```

**微前端**
- 主应用：按主应用技术栈检查
- 子应用：按子应用技术栈检查
- 支持子应用独立构建和部署

#### 2.2.7 标准化检查清单

| 检查项 | 说明 | 适用级别 | 权重 |
|--------|------|---------|------|
| Node 版本 | 必须使用平台标准版本 | 基础 | 20% |
| 依赖锁文件 | 必须有 `package-lock.json` 或 `yarn.lock` | 基础 | 20% |
| 构建脚本 | 必须有 `build` 脚本 | 基础 | 10% |
| Lint 检查 | 必须配置 Lint 并通过检查 | 核心 | 20% |
| 环境变量 | 必须有 `.env.example` 文件 | 核心 | 10% |
| TypeCheck | TypeScript 项目必须通过类型检查 | 完整 | 10% |
| 单元测试 | 必须有测试脚本且通过测试 | 完整 | 5% |
| 代码覆盖率 | 测试覆盖率 ≥ 60% | 完整 | 5% |

**打分规则**
- 根据检查级别计算总分（基础=50分，核心=80分，完整=100分）
- 每项检查通过得对应权重分，不通过得 0 分
- 总分 ≥ 80 分为"优秀"，60-79 分为"合格"，< 60 分为"不合格"
- 不合格的应用禁止接入 CI/CD（宽松模式除外）

#### 2.2.8 微前端支持

**微前端识别**
- `package.json` 中有 `qiankun`、`single-spa`、`module-federation` 等依赖
- 支持手动指定微前端模式

**微前端配置**
- 主应用和子应用分别配置流水线
- 支持子应用独立构建和部署
- 支持主应用构建时自动拉取子应用最新版本

**微前端构建流程**
```
主应用构建：
1. 检查主应用代码质量
2. 拉取子应用最新版本（或指定版本）
3. 合并主应用和子应用
4. 构建产物

子应用构建：
1. 检查子应用代码质量
2. 独立构建子应用
3. 上传子应用制品到制品库
4. 通知主应用更新
```

**微前端部署策略**
- 主应用部署：部署主应用 + 子应用
- 子应用部署：独立部署子应用，主应用热更新

#### 2.2.9 数据采集策略

- **构建数据**：定时任务轮询 Jenkins API + Webhook 实时回调
- **MR 数据**：GitLab Webhook 统一接收，平台暴露 `/feplatform/cicd/webhook/gitlab`
- **制品数据**：构建成功后记录制品信息（版本号、大小、MD5、上传时间）
- **质量数据**：每次构建后记录标准化检查得分

#### 2.2.10 数据表

```sql
fe_jenkins_instance        -- Jenkins 实例（id, name, url, token, domain, env_type, status...）
fe_tech_stack              -- 技术栈管理（id, name, code, node_version_range, lint_config, jenkinsfile_tpl, status...）
fe_node_version            -- Node 版本管理（id, version, status, is_standard, create_time...）
fe_pipeline                -- 应用-流水线（id, app_id, jenkins_id, job_name, env, tech_stack_id, node_version_id, check_level, deploy_strategy, template_id, template_version_id...）
fe_pipeline_build          -- 构建记录（id, pipeline_id, build_no, status, duration, trigger_user, commit_sha, node_version, tech_stack, artifact_version, quality_score, check_level_exemptions, template_version, framework_info, finish_time...）
fe_artifact                -- 制品记录（id, build_id, version, size, md5, storage_url, upload_time...）
fe_mr_review_log           -- MR 审查记录（id, app_id, mr_id, lint_pass, type_pass, ai_review_score, reviewer, merge_time）
fe_ci_standard_score       -- 标准化打分（id, app_id, item_code, pass, score, check_time）
fe_micro_frontend          -- 微前端配置（id, app_id, is_main, parent_app_id, sub_app_ids, build_strategy...）
fe_app_tech_info           -- 应用技术栈信息（id, app_id, template_id, template_version_id, framework, framework_version, node_version, build_tool, package_manager, tech_stack_id, create_time, update_time）
```

**字段说明**
- `fe_jenkins_instance.env_type`：环境类型（test=测试环境，prod=生产环境）
- `fe_tech_stack.node_version_range`：技术栈推荐的 Node 版本范围
- `fe_tech_stack.lint_config`：技术栈默认的 Lint 配置（JSON）
- `fe_tech_stack.jenkinsfile_tpl`：技术栈默认的 Jenkinsfile 模版
- `fe_pipeline.tech_stack_id`：应用使用的技术栈
- `fe_pipeline.check_level`：检查级别（strict=严格，standard=标准，loose=宽松）
- `fe_pipeline.deploy_strategy`：部署策略（auto_deploy=自动部署，artifact_only=仅制品库，online_deploy=在线部署）
- `fe_pipeline.template_id`：关联的模板ID（可选）
- `fe_pipeline.template_version_id`：关联的模板版本ID（可选）
- `fe_pipeline_build.tech_stack`：构建使用的技术栈
- `fe_pipeline_build.artifact_version`：制品版本号
- `fe_pipeline_build.quality_score`：本次构建的标准化检查得分
- `fe_pipeline_build.check_level_exemptions`：豁免的检查项（JSON）
- `fe_pipeline_build.template_version`：构建时使用的模板版本
- `fe_pipeline_build.framework_info`：构建时采集的框架信息（JSON）
- `fe_artifact.storage_url`：制品在制品库中的存储地址
- `fe_micro_frontend.is_main`：是否为主应用
- `fe_micro_frontend.parent_app_id`：父应用 ID（子应用）
- `fe_micro_frontend.sub_app_ids`：子应用 ID 列表（主应用）
- `fe_micro_frontend.build_strategy`：构建策略（independent=独立构建，integrated=集成构建）
- `fe_app_tech_info.framework`：框架名称（Vue2/Vue3/React/Angular/jQuery）
- `fe_app_tech_info.framework_version`：框架版本
- `fe_app_tech_info.node_version`：使用的 Node 版本
- `fe_app_tech_info.build_tool`：构建工具（webpack/vite/angular-cli）
- `fe_app_tech_info.package_manager`：包管理器（npm/yarn/pnpm）

#### 2.2.11 技术实现方案

**Jenkins 集成**
- 使用 Jenkins REST API 进行 Job 创建、触发、查询
- 支持多实例配置，根据环境类型路由到对应 Jenkins
- 使用 Jenkinsfile 模版引擎，支持变量替换（Node 版本、部署策略、技术栈等）

**技术栈自动识别**
- 扫描 `package.json`，根据依赖自动识别技术栈
- 支持正则匹配和版本范围匹配
- 识别结果缓存，避免重复扫描

**制品库集成**
- 支持 Nexus 和 Artifactory 两种制品库
- 使用 REST API 上传/下载制品
- 记录制品元数据（版本、大小、MD5、上传时间）

**Node 版本检查**
- 在 Jenkinsfile 中添加版本检查步骤
- 使用 `node -v` 命令获取当前 Node 版本
- 与平台配置的版本进行比对
- 支持豁免机制（管理员审批）

**分级检查实现**
- 根据检查级别动态生成 Jenkinsfile
- 基础检查：依赖锁文件、构建脚本、Node 版本
- 核心检查：Lint 检查、环境变量
- 完整检查：TypeCheck、单元测试、代码覆盖率
- 支持部分检查项豁免

**Lint 检查实现**
- 根据技术栈自动选择 Lint 配置
- Vue2/Vue3：ESLint + eslint-plugin-vue
- React：ESLint + eslint-plugin-react
- Angular：AngularLint
- jQuery：ESLint（宽松配置）
- 支持用户自定义 Lint 配置

**部署策略实现**
- 测试环境自动部署：在 Jenkinsfile 中添加 `ssh` 步骤，部署到测试服务器
- 测试环境制品库：在 Jenkinsfile 中添加 `curl` 步骤，上传到制品库
- 生产环境在线部署：从制品库拉取制品，部署到生产服务器

**微前端实现**
- 主应用和子应用分别配置流水线
- 支持子应用独立构建和部署
- 主应用构建时自动拉取子应用最新版本
- 支持子应用热更新

**质量检查实现**
- Lint 检查：运行 `npm run lint`，检查退出码
- TypeCheck：运行 `npm run type-check`（如配置），检查退出码
- 依赖锁文件：检查 `package-lock.json` 或 `yarn.lock` 是否存在
- 构建脚本：检查 `package.json` 中是否有 `build` 脚本
- 单元测试：运行 `npm run test`，检查退出码
- 代码覆盖率：运行 `npm run test:coverage`，检查覆盖率

**老旧项目兼容策略**
- 支持宽松检查级别，只检查基础项
- 支持 Node 版本豁免（管理员审批）
- 支持 Lint 配置豁免（管理员审批）
- 支持自定义 Jenkinsfile（绕过标准检查）
- 豁免项目在构建记录中标记，方便后续优化

**模板版本检查实现**
- 在构建前检查应用是否关联了模板
- 如果关联了模板，获取当前模板版本与最新版本进行对比
- 如果使用的版本过旧，输出警告信息（不阻断构建）
- 记录模板版本变更历史到构建记录

**技术栈采集实现**
- **模板优先**：如果应用关联了模板，从模板获取技术栈信息
- **自动扫描**：如果没有关联模板，自动扫描 `package.json`
- **字段提取**：从 `dependencies` 和 `devDependencies` 中提取框架名称和版本
- **智能识别**：支持模糊匹配和版本范围判断
- **信息存储**：将采集结果保存到 `fe_app_tech_info` 表

**技术栈统计实现**
- 定时任务统计各技术栈使用数量
- 支持按部门、团队、时间维度筛选
- 生成技术栈分布图表和报告
- 支持导出统计结果（Excel/PDF）

**接入流程优化**
```
1. 用户选择应用
2. 检查应用是否关联模板
   ├─ 是 → 获取模板技术栈信息
   └─ 否 → 自动扫描 package.json
3. 用户确认技术栈或手动指定
4. 平台推荐 Node 版本和检查级别
5. 用户调整配置或申请豁免
6. 平台生成 Jenkinsfile
7. 用户确认 Jenkinsfile 或自定义
8. 创建 Jenkins Job
9. 触发首次构建
10. 记录构建结果和质量得分
11. 更新应用技术栈信息到平台
```

---

### 2.3 AI 提效中心

#### 2.3.1 功能概述

统一管理 AI Skill 资产，支持 Claude Code/Cursor 接入，量化 AI 提效数据。

#### 2.3.2 核心功能

| 功能 | 说明 |
|------|------|
| Skill 资产库 | 官方/通用业务/应用级 三类 Skill 的 CRUD + Prompt 编辑 + 测试 |
| MCP 配置导出 | 勾选 Skill → 生成 `mcp-config.json` → Claude Code 直接接入 |
| Chat2Code 对话出码 | Web 端三栏：Skill 选择 / 对话 / 代码预览 |
| Schema 转换 | SQL DDL → Online 表单 JSON（与低代码工具形成上下游） |
| AI 使用记录 | 记录每次调用：用户、应用、Skill、Token 数、代码行数、采纳率 |

#### 2.3.3 与既有模块的耦合

- Skill 与应用列表多对多关联（应用级 Skill）
- AI 使用记录关联人员列表，生成人均效能指标

#### 2.3.4 数据表

```sql
fe_skill                   -- Skill 资产表
fe_app_skill_rel           -- 应用-Skill 关联
fe_mcp_skill_rel           -- MCP-Skill 关联
fe_ai_usage_log            -- AI 使用日志
```

#### 2.3.5 度量口径

- **生成代码行数**：从模型返回中识别代码块统计 LOC
- **采纳率**：用户在 Chat2Code 中点了"复制/下载/合并"即视为采纳
- **节省工时**：`代码行数 × 行均工时基准`，换算成人天

---

### 2.4 效能看板

#### 2.4.1 功能概述

统一数据统计，支持多维度看板和年终汇报生成。

#### 2.4.2 看板分层

| 层级 | 关键指标 |
|------|----------|
| 总览（公司级） | 应用总数、活跃应用、累计构建数、构建成功率、AI 生成代码总行数、节省工时、模版创建应用数 |
| 应用维度 | MR 数、构建成功率、平均构建时长、AI 使用次数、AI 采纳率、CI 标准化得分 |
| 人员维度 | AI 使用次数、采纳率、代码行数、参与的应用数、MR 数 |
| 趋势图 | 月度/季度趋势：构建量、AI 使用、采纳率、节省工时 |
| 年度报告 | 一键生成 PPT/PDF：摘要、关键数字、TOP 应用、TOP 贡献者、趋势曲线 |

#### 2.4.3 统一指标总线

```sql
fe_metric_event (
  id, event_type,        -- 如: 'ci.build', 'ai.invoke', 'app.create', 'mr.merge'
  app_id, user_id,
  numeric_value,         -- 通用数值
  extra json,            -- 附加信息
  event_time
)
```

每个业务模块在业务表写入的同时，向 `fe_metric_event` 投递一条事件。

---

## 3. 菜单结构

```
前端研发门户
├── 工作台（首页 Dashboard）
├── 业务管理
│   ├── 应用列表
│   └── 人员列表
├── 低代码工具
│   └── Online 表单开发
├── 应用模版中心
│   ├── 模版列表
│   ├── 模版版本管理
│   └── 应用创建记录
├── CI/CD 治理
│   ├── Jenkins 实例
│   ├── 技术栈管理
│   ├── Node 版本管理
│   ├── 流水线管理
│   ├── 构建记录
│   ├── 制品库管理
│   └── MR 质量看板
├── AI 提效中心
│   ├── Skill 资产库
│   ├── Chat2Code 对话出码
│   ├── MCP 配置导出
│   └── AI 使用记录
└── 效能看板
    ├── 总览
    ├── 应用维度
    ├── 人员维度
    └── 年度报告生成
```

---

## 4. 实施路线

| 阶段 | 周期 | 内容 | 验收 |
|------|------|------|------|
| P1 模版中心 | 第 1-2 周 | 模版 CRUD + GitLab 集成 + 版本管理 + 创建向导 + 权限控制；打通应用列表 | 能通过模版 30 分钟内创建一个可运行的新应用；权限控制正确（普通用户只能看自己的，管理员看全部） |
| P2 CI/CD 接入 | 第 3-5 周 | Jenkins 实例（测试/生产双环境）+ 技术栈管理（Vue2/Vue3/React/Angular/jQuery/微前端）+ Node 版本管理 + 分级检查策略（严格/标准/宽松）+ 流水线绑定 + 构建/MR 数据采集 + 标准化打分 + 制品库管理 + 部署策略配置 + 微前端支持 + 老旧项目兼容 + 模板版本检查 + 技术栈统计 | ≥3 个真实应用接入并显示构建历史；技术栈自动识别准确率 ≥ 90%；Node 版本统一管理；制品库上传/下载正常；测试环境自动部署和制品库模式均可用；生产环境在线部署可用；老旧项目可通过宽松模式接入；微前端主应用和子应用可独立构建部署；模板版本检查正常工作；技术栈信息自动采集并统计展示 |
| P3 AI 提效 | 第 6-8 周 | Skill 资产库 + MCP 导出 + AI 使用日志；Chat2Code 可选 | 5 条官方 + 3 条业务 Skill 上线；Claude Code 端到端连通 |
| P4 效能看板 | 第 9-10 周 | 指标总线 + 三个维度看板 + 年度报告导出 | 一键生成年度报告 PDF，含至少 10 项关键指标 |

---

## 5. 关键设计原则

1. **复用为先**：CI/CD、AI 提效强依赖"应用 + 人员"主数据
2. **闭环数据**：所有新模块都落数据埋点表，最终汇入效能看板
3. **口径统一**：通过 `fe_metric_event` 总线表保证数据口径一致
4. **渐进上线**：按 模版 → CI/CD → AI 提效 → 看板 的顺序推进

---

## 6. 风险与建议

1. **数据口径必须先定**：建议 P1 阶段就把 `fe_metric_event` 总线设计敲定
2. **Jenkins 权限**：每个 Jenkins 实例的 Token 建议走平台的密钥管理（加密存储）
3. **AI 采纳率埋点**：Chat2Code 的"复制/下载/合并"按钮必须埋点
4. **模版与应用强一致**：模版生成的应用一定要回写 `fe_application`