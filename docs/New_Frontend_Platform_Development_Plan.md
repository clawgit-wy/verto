# 新前端应用管理平台 - 详细开发方案

> 基于 `New_Frontend_Platform_Design_v3.md` 设计文档，结合现有 JeecgBoot 3.9.2 代码库分析，输出的可执行开发方案。

---

## 总览：基于现有代码库的开发策略

**现有代码库分析结论**：
- 后端：JeecgBoot 3.9.2 + Spring Boot 3.5.5，已有 `jeecg-boot-module-airag` 模块（含 AI 对话、MCP、知识库、模型管理等能力）
- 前端：Vue3 + Vite + Ant Design Vue，已有 `super/airag` 下完整的 AI 管理页面
- **核心策略**：新建独立后端模块 `jeecg-boot-module-feplatform`，前端在 `views/feplatform` 下新建页面，复用已有 AI 基础设施

---

## 阶段一：基础设施与 Skill 核心（第1-4周）

### 步骤 1.1：创建后端模块骨架

**目标**：在 `server/jeecg-boot-module/` 下新建 `jeecg-boot-module-feplatform` 模块

**具体操作**：

1. **创建 Maven 模块目录结构**：
```
server/jeecg-boot-module/jeecg-boot-module-feplatform/
├── pom.xml
└── src/main/java/org/jeecg/modules/feplatform/
    ├── skill/          ← Skill 资产管理
    ├── application/    ← 应用管理
    ├── developer/      ← 人员管理
    ├── team/           ← 团队管理
    ├── lowcode/        ← 低代码服务
    ├── aiusage/        ← AI 效能采集
    ├── cicd/           ← CI/CD 治理
    └── codedoctor/     ← 代码医生
```

2. **pom.xml 关键依赖**（参考 `jeecg-boot-module-airag` 的 pom.xml）：
```xml
<parent>
    <groupId>org.jeecgframework.boot3</groupId>
    <artifactId>jeecg-boot-module</artifactId>
    <version>3.9.2</version>
</parent>
<artifactId>jeecg-boot-module-feplatform</artifactId>
<dependencies>
    <dependency>
        <groupId>org.jeecgframework.boot3</groupId>
        <artifactId>jeecg-boot-base-core</artifactId>
    </dependency>
    <!-- 复用 airag 的 AI 能力 -->
    <dependency>
        <groupId>org.jeecgframework.boot3</groupId>
        <artifactId>jeecg-boot-module-airag</artifactId>
        <version>${jeecgboot.version}</version>
    </dependency>
</dependencies>
```

3. **注册模块**：修改 `server/jeecg-boot-module/pom.xml`，在 `<modules>` 中添加 `jeecg-boot-module-feplatform`

**验证点**：
- [ ] `mvn compile` 编译通过
- [ ] Spring Boot 启动日志中扫描到 `org.jeecg.modules.feplatform` 包

---

### 步骤 1.2：创建数据库表（Skill 核心）

**目标**：创建 `fe_skill` 表，这是整个平台的核心数据表

**具体操作**：

1. **执行 SQL**（在设计文档中已定义）：
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
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `sys_org_code` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Skill资产表';
```

2. **插入官方 Skill 种子数据**：
```sql
INSERT INTO fe_skill (id, name, code, category, description, prompt_template, status, sort_no) VALUES
('1', 'Jeecg代码生成', 'jeecg-codegen', 'official', '一句话生成全套CRUD代码', '你是JeecgBoot代码生成专家...', 'enable', 1),
('2', 'Online表单', 'jeecg-onlform', 'official', '一句话创建Online表单', '你是JeecgBoot Online表单配置专家...', 'enable', 2),
('3', 'Online报表', 'jeecg-onlreport', 'official', '一句话创建Online报表', '你是JeecgBoot Online报表配置专家...', 'enable', 3),
('4', '设计器表单', 'jeecg-desform', 'official', '一句话创建设计器表单', '你是JeecgBoot表单设计器专家...', 'enable', 4),
('5', '审批流程', 'jeecg-bpmn', 'official', '一句话画审批流程', '你是JeecgBoot BPMN流程设计专家...', 'enable', 5);
```

**验证点**：
- [ ] MySQL 中 `fe_skill` 表创建成功
- [ ] 5 条官方 Skill 数据插入成功
- [ ] `SELECT * FROM fe_skill WHERE category='official'` 返回 5 条

---

### 步骤 1.3：后端 Skill CRUD 开发

**目标**：使用 JeecgBoot 代码生成器生成 Skill 的标准 CRUD 代码，然后手动增强

**具体操作**：

1. **使用 JeecgBoot 代码生成器**（Online 表单 → 代码生成）：
   - 在 JeecgBoot 管理后台的"代码生成器"中，导入 `fe_skill` 表
   - 生成代码的包路径设为 `org.jeecg.modules.feplatform.skill`
   - 生成后代码结构：
     ```
     skill/
     ├── controller/FeskillController.java
     ├── entity/Feskill.java
     ├── mapper/FeskillMapper.java
     ├── mapper/xml/FeskillMapper.xml
     ├── service/IFeskillService.java
     └── service/impl/FeskillServiceImpl.java
     ```

2. **手动增强 Controller**，添加以下接口：
   ```java
   // Skill 配置导出 - 根据选中的 Skill 生成 mcp-config.json
   @GetMapping("/exportMcpConfig")
   public Result<?> exportMcpConfig(@RequestParam String skillCodes)
   
   // Skill 分类查询
   @GetMapping("/listByCategory")
   public Result<?> listByCategory(@RequestParam String category)
   
   // Skill 依赖校验
   @PostMapping("/validateDependencies")
   public Result<?> validateDependencies(@RequestBody List<String> skillIds)
   
   // Skill 测试（调用 AI 验证 Prompt 效果）
   @PostMapping("/testSkill")
   public Result<?> testSkill(@RequestBody SkillTestDTO dto)
   ```

3. **Entity 增强**：
   - `Feskill.java` 继承 `JeecgEntity`（已含 id/createBy/createTime 等）
   - JSON 字段使用 `@TableField(typeHandler = JacksonTypeHandler.class)` 处理
   - 注意：由于 `prompt_template` 是 `longtext`，不需要特殊处理

**验证点**：
- [ ] 启动后访问 `/jeecg-boot/feplatform/skill/list` 接口返回数据
- [ ] Swagger 文档中可见所有 Skill 接口
- [ ] CRUD 操作（增删改查）均可正常执行
- [ ] `/exportMcpConfig?skillCodes=jeecg-codegen,bus-code-style` 返回正确 JSON

---

### 步骤 1.4：前端 Skill 资产库页面

**目标**：在 `web/src/views/feplatform/` 下开发 Skill 管理页面

**具体操作**：

1. **创建目录结构**：
```
web/src/views/feplatform/
├── skill/
│   ├── SkillList.vue         ← 资产总览（卡片+列表双视图）
│   ├── SkillDetail.vue       ← Skill 详情
│   ├── SkillEdit.vue         ← 创建/编辑
│   ├── SkillExport.vue       ← 配置导出
│   └── components/
│       ├── SkillCard.vue     ← 卡片组件
│       ├── SkillForm.vue     ← 表单组件（含 Prompt 编辑器）
│       └── SkillTest.vue     ← Prompt 测试面板
├── application/   ← 应用管理（阶段二）
├── developer/     ← 人员管理（阶段二）
├── lowcode/       ← 低代码工坊（阶段二）
├── cicd/          ← CI/CD治理（阶段三）
├── dashboard/     ← 效能看板（阶段三）
└── codedoctor/    ← 代码医生（阶段三）
```

2. **API 层**：创建 `web/src/api/feplatform/skill.ts`
   ```typescript
   enum Api {
     list = '/feplatform/skill/list',
     add = '/feplatform/skill/add',
     edit = '/feplatform/skill/edit',
     delete = '/feplatform/skill/delete',
     exportMcpConfig = '/feplatform/skill/exportMcpConfig',
     listByCategory = '/feplatform/skill/listByCategory',
     testSkill = '/feplatform/skill/testSkill',
   }
   ```

3. **SkillList.vue 核心功能**：
   - 卡片/列表双视图切换（参考现有 `CardList` 组件）
   - 分类筛选 Tab（官方/业务/应用级）
   - 搜索框（支持名称和编码搜索）
   - 操作按钮：新增、编辑、删除、导出配置、测试

4. **SkillEdit.vue 核心功能**：
   - 基于 `BasicForm` 的表单布局
   - Prompt 模板编辑器（使用 `CodeEditor` 组件，支持 Markdown 高亮）
   - Input/Output Schema 可视化编辑（JSON 编辑器）
   - 使用示例管理（动态添加/删除示例条目）
   - 实时测试面板（右侧面板，输入参数后调用 `testSkill` 接口）

5. **SkillExport.vue 核心功能**：
   - Skill 勾选表格（带分类分组）
   - 依赖冲突校验结果展示
   - 一键生成 mcp-config.json
   - 复制到剪贴板 / 下载文件

**验证点**：
- [ ] Skill 列表页正确展示 5 条官方 Skill 数据
- [ ] 分类筛选切换正常
- [ ] 新增/编辑 Skill 表单提交成功
- [ ] Prompt 编辑器正常工作
- [ ] 导出页面勾选 Skill 后生成正确的 mcp-config.json

---

### 步骤 1.5：MCP 配置导出与连接验证

**目标**：实现 mcp-config.json 导出功能，验证本地 Claude Code 能通过 MCP 连接平台

**具体操作**：

1. **后端导出接口** (`FeskillController.exportMcpConfig`)：
   ```java
   @GetMapping("/exportMcpConfig")
   public Result<?> exportMcpConfig(@RequestParam String skillCodes) {
       // 1. 解析 skillCodes（逗号分隔）
       // 2. 查询 Skill 列表，校验状态和依赖
       // 3. 获取当前用户的 Token
       // 4. 组装 mcp-config.json 结构
       // 5. 返回配置对象
   }
   ```
   
   返回结构：
   ```json
   {
     "mcpServers": {
       "fe-platform": {
         "command": "npx",
         "args": ["-y", "@jeecg/mcp-server"],
         "env": {
           "MCP_ENDPOINT": "https://your-domain/jeecg-boot/mcp/sse",
           "MCP_TOKEN": "{{user_token}}",
           "SKILLS": "bus-code-style,bus-api-standard"
         }
       }
     }
   }
   ```

2. **MCP Server 扩展**（复用已有 `AiragMcpController`）：
   - 在 `AiragMcpServiceImpl` 中增加 Skill 绑定逻辑
   - 新增 `fe_mcp_skill_rel` 关联表：
   ```sql
   CREATE TABLE `fe_mcp_skill_rel` (
     `id` varchar(36) NOT NULL,
     `mcp_id` varchar(36) NOT NULL COMMENT 'MCP Server ID',
     `skill_id` varchar(36) NOT NULL COMMENT 'Skill ID',
     `sort_no` decimal(8,2) DEFAULT 0,
     PRIMARY KEY (`id`),
     KEY `idx_mcp_id` (`mcp_id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP-Skill关联表';
   ```

3. **端到端测试**：
   - 下载 mcp-config.json
   - 放到 Claude Code 配置目录
   - 验证 Claude Code 能调用平台 Skill

**验证点**：
- [ ] 导出接口返回正确格式的 JSON
- [ ] MCP Server 绑定 Skill 后，工具列表包含 Skill 对应工具
- [ ] Claude Code 连接平台 MCP 后能列出 Skill 工具
- [ ] 调用 Skill 工具后 AI 输出符合 Skill Prompt 定义

---

### 步骤 1.6：Chat2Code 基础对话界面

**目标**：基于现有 `AiChat.vue` 扩展，增加 Skill 选择和代码预览功能

**具体操作**：

1. **后端**：复用 `AiragChatController` 的对话接口，在 `AIChatHandler` 的 `buildPlugins` 阶段注入 Skill：
   ```java
   // 在 FeplatformChatController 中
   @PostMapping("/chat2code")
   public void chat2code(@RequestBody Chat2CodeDTO dto, HttpServletResponse response) {
       // 1. 根据 dto.getSkillIds() 查询 Skill 列表
       // 2. 将 Skill 的 prompt_template 注入到系统 Prompt
       // 3. 调用 AIChatHandler 进行对话（流式）
       // 4. 记录 AI 使用行为到 fe_ai_usage_log
   }
   ```

2. **前端 Chat2Code 页面** (`lowcode/Chat2Code.vue`)：
   - **三栏布局**：
     - 左栏：Skill 选择器（Checkbox Group，按分类分组）
     - 中栏：对话区（复用 `chatMessage.vue` + `chatText.vue` 的消息渲染逻辑）
     - 右栏：代码预览区（检测到代码块时自动展示）
   - **代码块增强**：
     - 语法高亮（使用 highlight.js 或 prismjs）
     - 一键复制按钮
     - "在沙箱中预览"按钮（阶段二实现）
     - "下载文件"按钮

**验证点**：
- [ ] Chat2Code 页面正常渲染
- [ ] 选择 Skill 后对话，AI 回复中包含符合 Skill 规范的代码
- [ ] 代码块语法高亮正确
- [ ] 一键复制功能正常

---

## 阶段二：业务管理 + Skill 增强 + 低代码服务（第5-8周）

### 步骤 2.1：应用管理模块

**目标**：实现 `fe_application` CRUD 和应用级 Skill 绑定

**具体操作**：

1. **创建数据库表**（设计文档已定义）：
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
     `create_by` varchar(50) DEFAULT NULL,
     `create_time` datetime DEFAULT NULL,
     `update_by` varchar(50) DEFAULT NULL,
     `update_time` datetime DEFAULT NULL,
     `del_flag` char(1) DEFAULT '0',
     `sys_org_code` varchar(64) DEFAULT NULL,
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_app_code` (`app_code`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端应用管理表';
   ```

2. **后端开发**：
   - 使用代码生成器生成 CRUD 代码（包路径 `org.jeecg.modules.feplatform.application`）
   - 增强接口：
     ```java
     // 应用关联 Skill
     @PostMapping("/bindSkills")
     public Result<?> bindSkills(@RequestBody AppSkillBindDTO dto)
     
     // 获取应用关联的 Skill 列表
     @GetMapping("/skillList")
     public Result<?> skillList(@RequestParam String appId)
     
     // 应用级 Prompt 模板测试
     @PostMapping("/testAppPrompt")
     public Result<?> testAppPrompt(@RequestBody AppPromptTestDTO dto)
     ```

3. **前端页面**：
   - `ApplicationList.vue`：卡片+列表双视图（参考 SkillList）
   - `ApplicationDetail.vue`：展示基本信息 + 关联 Skill + 团队成员
   - `ApplicationEdit.vue`：编辑基本信息 + 技术栈选择器 + Skill 关联
   - `ApplicationSkill.vue`：应用专属 Skill 配置 + Prompt 模板编辑

4. **应用级 Skill 优先级实现**：
   ```java
   // 查询 Skill 时的优先级排序逻辑
   public List<Feskill> getEffectiveSkills(String appId) {
       List<Feskill> skills = new ArrayList<>();
       // 1. 应用级 Skill（优先级最高）
       skills.addAll(getAppSkills(appId));
       // 2. 通用业务 Skill
       skills.addAll(getBusinessSkills());
       // 3. 官方 Skill
       skills.addAll(getOfficialSkills());
       // 按 code 去重，高优先级覆盖低优先级
       return deduplicateByCode(skills);
   }
   ```

**验证点**：
- [ ] 应用 CRUD 正常
- [ ] 应用绑定 Skill 后，列表展示正确
- [ ] 应用级 Skill 优先级逻辑正确（同 code 时应用级覆盖业务级）
- [ ] 技术栈 JSON 字段正确存取

---

### 步骤 2.2：人员与团队管理

**目标**：实现 `fe_developer` 和 `fe_team` CRUD

**具体操作**：

1. **创建数据库表**（设计文档已定义）：
   ```sql
   CREATE TABLE `fe_team` (
     `id` varchar(36) NOT NULL COMMENT '主键ID',
     `team_name` varchar(100) NOT NULL COMMENT '团队名称',
     `team_code` varchar(50) NOT NULL COMMENT '团队编码',
     `leader_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
     `description` text COMMENT '团队描述',
     `create_by` varchar(50) DEFAULT NULL,
     `create_time` datetime DEFAULT NULL,
     `update_by` varchar(50) DEFAULT NULL,
     `update_time` datetime DEFAULT NULL,
     `del_flag` char(1) DEFAULT '0',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uk_team_code` (`team_code`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端团队表';

   CREATE TABLE `fe_developer` (
     `id` varchar(36) NOT NULL COMMENT '主键ID',
     `user_id` varchar(36) NOT NULL COMMENT '关联系统用户ID',
     `real_name` varchar(50) NOT NULL COMMENT '姓名',
     `employee_no` varchar(30) DEFAULT NULL COMMENT '工号',
     `team_id` varchar(36) DEFAULT NULL COMMENT '所属团队ID',
     `role` varchar(20) DEFAULT 'developer' COMMENT '角色: developer/lead/manager',
     `skill_tags` json DEFAULT NULL COMMENT '技能标签',
     `status` varchar(10) DEFAULT 'active' COMMENT '状态: active/inactive',
     `create_by` varchar(50) DEFAULT NULL,
     `create_time` datetime DEFAULT NULL,
     `update_by` varchar(50) DEFAULT NULL,
     `update_time` datetime DEFAULT NULL,
     `del_flag` char(1) DEFAULT '0',
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端开发人员表';
   ```

2. **后端开发**：
   - `fe_team`：标准 CRUD + 队长指定
   - `fe_developer`：标准 CRUD + 关联系统用户（`user_id` 关联 `sys_user`）

3. **前端页面**：
   - `TeamList.vue`：团队列表 + 成员管理
   - `DeveloperList.vue`：人员列表 + 技能标签管理

**验证点**：
- [ ] 团队增删改查正常
- [ ] 人员关联系统用户正常
- [ ] 团队成员列表展示正确

---

### 步骤 2.3：编写业务 Skill

**目标**：编写公司通用业务 Skill 的 Prompt 模板

**具体操作**：

1. **bus-code-style（公交业务代码规范）**：
   ```
   你是公司前端代码规范专家。所有生成的代码必须遵循以下规范：
   
   ## Vue 3 组件规范
   - 文件名使用 PascalCase：VehicleList.vue
   - 组件名使用 PascalCase：VehicleList
   - 使用 <script setup lang="ts"> 语法
   - Props 使用 defineProps<T>() + withDefaults
   - Emits 使用 defineEmits<T>()
   - 生命周期使用 onMounted/onUnmounted
   
   ## API 层规范
   - 统一使用 /src/api/ 模块化封装
   - 接口函数命名：getXxxList, getXxxDetail, saveXxx, deleteXxx
   - 使用 defHttp 封装请求
   - 响应类型必须定义 TypeScript 接口
   
   ## 状态管理规范
   - 全局状态使用 Pinia Store
   - Store 文件名：useXxxStore.ts
   - 优先使用 storeToRefs 保持响应性
   ...
   ```

2. **bus-api-standard（接口规范）**、**bus-component-lib（组件库规范）** 等类似编写

3. **通过 Skill 管理界面录入**，确保 Prompt 可在线编辑和测试

**验证点**：
- [ ] 每个 Skill 的 Prompt 能指导 AI 生成符合规范的代码
- [ ] 在 Chat2Code 中选择业务 Skill，生成的代码符合规范要求
- [ ] Skill 测试面板验证通过

---

### 步骤 2.4：Schema 转换引擎

**目标**：解析 SQL DDL，生成 JeecgBoot Online 表单 JSON 配置

**具体操作**：

1. **后端开发** (`lowcode/SchemaConverterController.java`)：
   ```java
   @PostMapping("/sql2schema")
   public Result<?> sql2schema(@RequestBody Sql2SchemaDTO dto) {
       // 1. 使用 JSqlParser 解析 DDL（已有依赖）
       // 2. 提取表名、字段信息、注释
       // 3. 按映射规则转换为 Online 表单配置
       // 4. 返回 JSON 配置
   }
   ```
   
   映射规则核心逻辑：
   ```java
   private String mapSqlTypeToWidget(String sqlType) {
       sqlType = sqlType.toUpperCase();
       if (sqlType.contains("VARCHAR")) return "input";
       if (sqlType.contains("TEXT")) return "textarea";
       if (sqlType.contains("INT") || sqlType.contains("BIGINT")) return "inputnumber";
       if (sqlType.contains("TINYINT")) return "switch";
       if (sqlType.contains("DATETIME") || sqlType.contains("DATE")) return "datepicker";
       if (sqlType.contains("DECIMAL")) return "inputnumber";
       return "input";
   }
   ```

2. **前端页面** (`lowcode/Sql2Schema.vue`)：
   - 左侧：SQL DDL 输入区（CodeEditor 组件）
   - 右侧：转换结果展示（JSON 预览 + 表单渲染预览）
   - 操作按钮：转换、一键导入到 Online 表单、复制 JSON

**验证点**：
- [ ] 输入设计文档中的 `bus_vehicle` DDL，输出正确的 Online 表单 JSON
- [ ] varchar → Input, tinyint → Switch 等映射正确
- [ ] COMMENT 自动填充为 Label
- [ ] 外键字段识别并映射为 Popup 控件

---

### 步骤 2.5：可视化沙箱

**目标**：实时预览 AI 生成的 Vue 3 组件

**具体操作**：

1. **沙箱容器**（前端核心组件 `SandboxPreview.vue`）：
   ```vue
   <template>
     <iframe
       ref="sandboxRef"
       sandbox="allow-scripts allow-same-origin"
       :srcdoc="sandboxHtml"
       class="sandbox-frame"
     />
   </template>
   <script setup lang="ts">
   // 构建沙箱 HTML
   const sandboxHtml = computed(() => {
     return `
       <!DOCTYPE html>
       <html>
       <head>
         <script type="importmap">
         {
           "imports": {
             "vue": "https://unpkg.com/vue@3/dist/vue.esm-browser.js",
             "ant-design-vue": "https://unpkg.com/ant-design-vue@4/dist/ant-design-vue.esm.js"
           }
         }
         </script>
       </head>
       <body>
         <div id="app"></div>
         <script type="module">
           import { createApp } from 'vue';
           // 动态编译组件...
         </script>
       </body>
       </html>
     `;
   });
   </script>
   ```

2. **SFC 编译**：
   - 使用 `@vue/compiler-sfc` 在主窗口编译 SFC 为 render function
   - 通过 `postMessage` 将编译结果传入沙箱 iframe
   - 沙箱内动态挂载组件

3. **安全措施**：
   - iframe sandbox 属性限制
   - 代码注入检测（正则过滤危险 API：`eval`、`Function`、`document.cookie` 等）

**验证点**：
- [ ] 简单 Vue3 组件（如计数器）能在沙箱中正确渲染
- [ ] Ant Design Vue 组件能在沙箱中渲染
- [ ] 恶意代码（如 `alert(document.cookie)`）被拦截
- [ ] 沙箱与主窗口通过 postMessage 通信正常

---

### 步骤 2.6：CI/CD Pipeline 模板管理

**目标**：创建 `fe_pipeline_template` 和 `fe_project_pipeline` 表，实现模板 CRUD

**具体操作**：

1. **创建数据库表**（设计文档已定义）：
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
     `create_by` varchar(50) DEFAULT NULL,
     `create_time` datetime DEFAULT NULL,
     `update_by` varchar(50) DEFAULT NULL,
     `update_time` datetime DEFAULT NULL,
     `del_flag` char(1) DEFAULT '0',
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
     `jenkins_status` varchar(20) DEFAULT 'not_configured' COMMENT 'Jenkins接入状态',
     `webhook_configured` tinyint DEFAULT 0 COMMENT 'Webhook是否已配置',
     `quality_gate_enabled` tinyint DEFAULT 1 COMMENT '质量门禁是否开启',
     `ai_review_enabled` tinyint DEFAULT 0 COMMENT 'AI审查是否开启',
     `last_pipeline_status` varchar(20) DEFAULT NULL COMMENT '最近一次流水线状态',
     `last_pipeline_time` datetime DEFAULT NULL COMMENT '最近一次流水线时间',
     `create_by` varchar(50) DEFAULT NULL,
     `create_time` datetime DEFAULT NULL,
     `update_by` varchar(50) DEFAULT NULL,
     `update_time` datetime DEFAULT NULL,
     `del_flag` char(1) DEFAULT '0',
     PRIMARY KEY (`id`),
     KEY `idx_app_id` (`app_id`),
     KEY `idx_gitlab_project_id` (`gitlab_project_id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目Pipeline配置表';
   ```

2. **后端 CRUD**（标准 JeecgBoot 模式）

3. **增强接口**：
   ```java
   // 一键生成 .gitlab-ci.yml
   @PostMapping("/generateCiConfig")
   public Result<?> generateCiConfig(@RequestBody CiConfigGenerateDTO dto)
   
   // 同步 GitLab 项目
   @PostMapping("/syncGitlabProjects")
   public Result<?> syncGitlabProjects(@RequestParam String appId)
   ```

**验证点**：
- [ ] Pipeline 模板 CRUD 正常
- [ ] 根据模板生成 .gitlab-ci.yml 内容正确
- [ ] 项目关联模板后配置信息完整

---

## 阶段三：效能度量 + CI/CD 闭环 + 代码医生（第9-12周）

### 步骤 3.1：AI 使用行为采集

**目标**：创建 `fe_ai_usage_log` 表，在关键节点自动记录 AI 使用数据

**具体操作**：

1. **创建数据库表**（设计文档已定义）：
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

2. **采集埋点**：
   - **Chat2Code 对话完成**：在 `FeplatformChatController` 中记录
   - **MCP 调用**：在 MCP Server 请求拦截器中记录
   - **Schema 转换**：在转换接口中记录

3. **AOP 切面方式**实现自动采集：
   ```java
   @Aspect
   @Component
   public class AiUsageLogAspect {
       @AfterReturning("execution(* org.jeecg.modules.feplatform..*Controller.*(..)) && @annotation(aiLog)")
       public void logAiUsage(JoinPoint jp, AiUsageLog aiLog) {
           // 记录到 fe_ai_usage_log
       }
   }
   ```

**验证点**：
- [ ] Chat2Code 对话后自动生成日志记录
- [ ] 日志包含正确的 user_id/skill_code/code_lines 等字段
- [ ] 日志数据可按时间/用户/Skill 维度查询

---

### 步骤 3.2：效能看板页面

**目标**：开发 4 个看板页面，实现数据可视化

**具体操作**：

1. **后端聚合接口**：
   ```java
   @GetMapping("/overview")     // 效能总览
   @GetMapping("/teamStats")    // 团队效能
   @GetMapping("/personalStats")// 个人效能
   @GetMapping("/skillHeat")    // Skill 热力数据
   @GetMapping("/trend")        // 趋势数据
   ```

2. **前端看板页面**（使用 ECharts，已有 `useECharts` hook）：
   - `DashboardOverview.vue`：4 个统计卡片 + 趋势折线图 + Skill 排行
   - `DashboardTeam.vue`：团队横向柱状图 + 成员列表
   - `DashboardPersonal.vue`：个人贡献图 + 采纳率环形图
   - `DashboardSkillHeat.vue`：Skill 使用热力矩阵

**验证点**：
- [ ] 看板数据与 fe_ai_usage_log 一致
- [ ] 趋势图按周/月维度正确展示
- [ ] 统计卡片数值正确（AI代码量、采纳率、工时节省、Token消耗）

---

### 步骤 3.3：GitLab Webhook + AI MR 审查

**目标**：接收 GitLab MR 事件，AI 审查 diff，结果回写 MR 评论

**具体操作**：

1. **Webhook 接收**：
   ```java
   @PostMapping("/cicd/webhook")
   public Result<?> handleWebhook(@RequestHeader("X-Gitlab-Token") String token,
                                   @RequestBody JSONObject payload) {
       // 1. 验证 Token
       // 2. 解析事件类型（Merge Request / Push）
       // 3. 如果是 MR 事件，触发 AI 审查
   }
   ```

2. **AI 审查逻辑**：
   ```java
   public AiReviewResult performAiReview(String projectId, String mrIid) {
       // 1. 调用 GitLab API 获取 MR diff
       // 2. 拼装审查 Prompt（注入 code-style Skill + diff 内容）
       // 3. 调用大模型进行审查
       // 4. 解析审查结果（严重/建议/合规三级）
       // 5. 回写 GitLab MR 评论
       // 6. 保存审查记录
   }
   ```

3. **GitLab API 调用**（使用 RestTemplate 或 GitLab4J）：
   - 获取 MR diff：`GET /projects/:id/merge_requests/:iid/changes`
   - 回写评论：`POST /projects/:id/merge_requests/:iid/notes`

**验证点**：
- [ ] Webhook 接收 MR 事件正常
- [ ] AI 审查结果按严重程度分级
- [ ] 审查意见成功回写到 GitLab MR 评论
- [ ] 审查记录保存到数据库

---

### 步骤 3.4：在线代码医生

**目标**：对存量代码进行 AI 分析，提供重构建议

**具体操作**：

1. **后端接口**：
   ```java
   @PostMapping("/codedoctor/analyze")
   public Result<?> analyzeCode(@RequestBody CodeAnalyzeDTO dto) {
       // 1. 接收代码内容（粘贴 / 文件上传）
       // 2. 拼装分析 Prompt（注入 code-style + performance Skill）
       // 3. 调用大模型分析
       // 4. 返回问题列表 + 质量评分 + 重构建议
   }
   ```

2. **前端页面** (`CodeDoctor.vue`)：
   - 代码输入区（CodeEditor + 文件上传）
   - 分析结果区（问题列表，按严重程度着色）
   - 建议输出区（重构前后代码 diff 对比视图）

**验证点**：
- [ ] 粘贴代码后 AI 分析返回问题列表
- [ ] 问题按严重程度分类（规范/性能/安全/可维护性）
- [ ] 重构建议中代码 diff 对比清晰

---

### 步骤 3.5：前端路由与菜单配置

**目标**：在 JeecgBoot 后台配置完整的菜单结构

**具体操作**：

1. **通过 JeecgBoot 菜单管理**配置（设计文档第5节菜单结构）：

   | 菜单层级 | 菜单名称 | 路由路径 | 组件路径 |
   |---------|---------|---------|---------|
   | 一级 | 前端研发门户 | /feplatform | RouteView |
   | 二级 | AI 开发者中心 | /feplatform/ai-hub | RouteView |
   | 三级 | Skill 资产库 | /feplatform/ai-hub/skills | feplatform/skill/SkillList |
   | 三级 | MCP 配置管理 | /feplatform/ai-hub/mcp | super/airag/aimcp/AiragMcpList |
   | 三级 | 配置导出 | /feplatform/ai-hub/export | feplatform/skill/SkillExport |
   | 二级 | 低代码工坊 | /feplatform/lowcode | RouteView |
   | 三级 | 对话出码 | /feplatform/lowcode/chat2code | feplatform/lowcode/Chat2Code |
   | 三级 | Schema 转换 | /feplatform/lowcode/sql2schema | feplatform/lowcode/Sql2Schema |
   | 二级 | 业务管理 | /feplatform/business | RouteView |
   | 三级 | 应用管理 | /feplatform/business/app | feplatform/application/ApplicationList |
   | 三级 | 人员管理 | /feplatform/business/developer | feplatform/developer/DeveloperList |
   | 三级 | 团队管理 | /feplatform/business/team | feplatform/team/TeamList |
   | 二级 | CI/CD 治理 | /feplatform/cicd | RouteView |
   | 三级 | 构建态势总览 | /feplatform/cicd/overview | feplatform/cicd/CicdOverview |
   | 三级 | Pipeline 模板 | /feplatform/cicd/templates | feplatform/cicd/PipelineTemplates |
   | 三级 | AI 审查日志 | /feplatform/cicd/ai-reviews | feplatform/cicd/AiReviewLog |
   | 二级 | 效能看板 | /feplatform/dashboard | RouteView |
   | 三级 | 效能总览 | /feplatform/dashboard/overview | feplatform/dashboard/DashboardOverview |
   | 三级 | 团队效能 | /feplatform/dashboard/team | feplatform/dashboard/DashboardTeam |
   | 三级 | 个人效能 | /feplatform/dashboard/personal | feplatform/dashboard/DashboardPersonal |
   | 三级 | Skill 热力图 | /feplatform/dashboard/skill-heat | feplatform/dashboard/DashboardSkillHeat |
   | 二级 | 代码医生 | /feplatform/codedoctor | RouteView |
   | 三级 | 代码诊断 | /feplatform/codedoctor/analyze | feplatform/codedoctor/CodeDoctor |

2. **权限配置**：在 JeecgBoot 权限管理中配置按钮级权限

**验证点**：
- [ ] 所有菜单在后台正确显示
- [ ] 页面路由跳转正常
- [ ] 权限控制生效（不同角色看到不同菜单和按钮）

---

### 步骤 3.6：全链路集成测试

**目标**：端到端验证整个平台的核心流程

**测试场景**：

1. **Skill → MCP → Claude Code 完整链路**：
   - 创建自定义 Skill → 导出 mcp-config.json → Claude Code 连接 → 生成代码 → 验证代码符合 Skill 规范

2. **Chat2Code 全流程**：
   - 选择 Skill → 输入需求 → AI 生成代码 → 代码预览 → 沙箱渲染 → 下载代码

3. **Schema 转换全流程**：
   - 输入 DDL → 转换为 Online 配置 → 预览表单 → 一键导入

4. **AI MR 审查全流程**：
   - 提交 MR → GitLab 触发 Webhook → 平台 AI 审查 → 评论回写 → 查看审查记录

5. **效能看板数据验证**：
   - 执行上述操作后 → 检查效能看板数据是否正确采集和展示

**验证点**：
- [ ] 所有 5 个测试场景全部通过
- [ ] 效能看板数据与操作记录一致
- [ ] 无严重 Bug，性能满足要求

---

## 附录 A：数据库表创建顺序总览

| 顺序 | 表名 | 所属阶段 | 说明 |
|------|------|---------|------|
| 1 | fe_skill | 阶段一 | Skill 资产表（核心） |
| 2 | fe_mcp_skill_rel | 阶段一 | MCP-Skill 关联表 |
| 3 | fe_application | 阶段二 | 应用管理表 |
| 4 | fe_team | 阶段二 | 团队管理表 |
| 5 | fe_developer | 阶段二 | 人员管理表 |
| 6 | fe_pipeline_template | 阶段二 | Pipeline 模板表 |
| 7 | fe_project_pipeline | 阶段二 | 项目 Pipeline 配置表 |
| 8 | fe_ai_usage_log | 阶段三 | AI 使用行为日志表 |

---

## 附录 B：后端模块包结构总览

```
org.jeecg.modules.feplatform/
├── skill/
│   ├── controller/FeskillController.java
│   ├── entity/Feskill.java
│   ├── mapper/FeskillMapper.java
│   ├── mapper/xml/FeskillMapper.xml
│   ├── service/IFeskillService.java
│   ├── service/impl/FeskillServiceImpl.java
│   └── vo/SkillExportVO.java, SkillTestDTO.java
├── application/
│   ├── controller/FeApplicationController.java
│   ├── entity/FeApplication.java
│   ├── mapper/FeApplicationMapper.java
│   ├── service/IFeApplicationService.java
│   └── service/impl/FeApplicationServiceImpl.java
├── developer/
│   ├── controller/FeDeveloperController.java
│   ├── entity/FeDeveloper.java
│   └── ...
├── team/
│   ├── controller/FeTeamController.java
│   ├── entity/FeTeam.java
│   └── ...
├── lowcode/
│   ├── controller/SchemaConverterController.java
│   ├── controller/Chat2CodeController.java
│   ├── service/ISchemaConverterService.java
│   ├── service/impl/SchemaConverterServiceImpl.java
│   └── dto/Sql2SchemaDTO.java, Chat2CodeDTO.java
├── aiusage/
│   ├── controller/AiUsageController.java
│   ├── entity/FeAiUsageLog.java
│   ├── service/IAiUsageService.java
│   └── aspect/AiUsageLogAspect.java
├── cicd/
│   ├── controller/CicdController.java
│   ├── entity/FePipelineTemplate.java
│   ├── entity/FeProjectPipeline.java
│   ├── service/ICicdService.java
│   ├── service/IGitlabService.java
│   └── service/IAiReviewService.java
└── codedoctor/
    ├── controller/CodeDoctorController.java
    └── dto/CodeAnalyzeDTO.java
```

---

## 附录 C：前端页面结构总览

```
web/src/views/feplatform/
├── skill/
│   ├── SkillList.vue
│   ├── SkillDetail.vue
│   ├── SkillEdit.vue
│   ├── SkillExport.vue
│   └── components/
│       ├── SkillCard.vue
│       ├── SkillForm.vue
│       └── SkillTest.vue
├── application/
│   ├── ApplicationList.vue
│   ├── ApplicationDetail.vue
│   ├── ApplicationEdit.vue
│   └── ApplicationSkill.vue
├── developer/
│   └── DeveloperList.vue
├── team/
│   └── TeamList.vue
├── lowcode/
│   ├── Chat2Code.vue
│   ├── Sql2Schema.vue
│   └── components/
│       └── SandboxPreview.vue
├── cicd/
│   ├── CicdOverview.vue
│   ├── CicdProject.vue
│   ├── PipelineTemplates.vue
│   ├── QualityGate.vue
│   └── AiReviewLog.vue
├── dashboard/
│   ├── DashboardOverview.vue
│   ├── DashboardTeam.vue
│   ├── DashboardPersonal.vue
│   └── DashboardSkillHeat.vue
└── codedoctor/
    └── CodeDoctor.vue
```

---

## 附录 D：关键技术决策与注意事项

1. **后端模块隔离**：新建 `jeecg-boot-module-feplatform` 而非在 `airag` 中修改，避免污染原有代码
2. **AI 能力复用**：通过依赖 `jeecg-boot-module-airag`，直接注入 `IAIChatHandler`、`LLMHandler` 等已有 Bean
3. **代码生成优先**：所有标准 CRUD 表先用 JeecgBoot 代码生成器生成，再手动增强
4. **前端组件复用**：`AiChat.vue`、`CodeEditor`、`BasicTable`、`BasicForm`、`CardList` 等组件直接复用
5. **MCP 不重复造轮子**：MCP 管理页面直接复用 `AiragMcpList.vue`，只做 Skill 绑定扩展
6. **Schema 解析**：JSqlParser 已在 `jeecg-boot-base-core` 中引入（`JSqlParserUtils.java`），可直接复用
7. **ECharts 已集成**：前端已有 `useECharts` hook，效能看板直接使用
8. **JSON 字段处理**：MySQL JSON 类型在 MyBatis-Plus 中使用 `JacksonTypeHandler` 处理

---

## 附录 E：API 接口清单

### E.1 Skill 管理 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 分页查询 | GET | `/feplatform/skill/list` | 标准 CRUD 列表 |
| 新增 | POST | `/feplatform/skill/add` | 新增 Skill |
| 编辑 | PUT | `/feplatform/skill/edit` | 编辑 Skill |
| 删除 | DELETE | `/feplatform/skill/delete` | 删除 Skill |
| 批量删除 | DELETE | `/feplatform/skill/deleteBatch` | 批量删除 |
| 导出 Excel | GET | `/feplatform/skill/exportXls` | 导出 |
| 分类查询 | GET | `/feplatform/skill/listByCategory` | 按分类查询 |
| 导出 MCP 配置 | GET | `/feplatform/skill/exportMcpConfig` | 生成 mcp-config.json |
| 依赖校验 | POST | `/feplatform/skill/validateDependencies` | 校验 Skill 依赖 |
| 测试 Skill | POST | `/feplatform/skill/testSkill` | 在线测试 Prompt |

### E.2 应用管理 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 标准 CRUD | CRUD | `/feplatform/application/*` | 增删改查 |
| 绑定 Skill | POST | `/feplatform/application/bindSkills` | 应用关联 Skill |
| 获取 Skill 列表 | GET | `/feplatform/application/skillList` | 获取应用关联 Skill |
| 测试应用 Prompt | POST | `/feplatform/application/testAppPrompt` | 测试应用级 Prompt |

### E.3 人员/团队 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 团队 CRUD | CRUD | `/feplatform/team/*` | 团队增删改查 |
| 人员 CRUD | CRUD | `/feplatform/developer/*` | 人员增删改查 |

### E.4 效能度量 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 效能总览 | GET | `/feplatform/aiUsage/overview` | 聚合统计概览数据 |
| 团队效能 | GET | `/feplatform/aiUsage/teamStats` | 按团队维度统计 |
| 个人效能 | GET | `/feplatform/aiUsage/personalStats` | 按个人维度统计 |
| Skill 热力数据 | GET | `/feplatform/aiUsage/skillHeat` | 各 Skill 使用频次与采纳率 |
| 趋势数据 | GET | `/feplatform/aiUsage/trend` | 按时间维度趋势 |

### E.5 低代码服务 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| SQL 转 Schema | POST | `/feplatform/lowcode/sql2schema` | DDL 转 Online 表单配置 |
| Schema 预览 | POST | `/feplatform/lowcode/schemaPreview` | 预览生成的表单配置 |
| 沙箱渲染 | POST | `/feplatform/lowcode/sandbox/render` | 在沙箱中渲染组件 |
| Chat2Code 对话 | POST | `/feplatform/lowcode/chat2code` | 对话式代码生成(流式) |

### E.6 CI/CD 治理 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 接收 GitLab Webhook | POST | `/feplatform/cicd/webhook` | 接收 GitLab MR/Push 事件 |
| 触发 AI 代码审查 | POST | `/feplatform/cicd/aiReview` | 对 MR diff 执行 AI 审查 |
| 回写 MR 评论 | POST | `/feplatform/cicd/postComment` | 将审查结果回写到 GitLab MR |
| 获取项目 CI 状态 | GET | `/feplatform/cicd/projectStatus` | 获取项目的 CI/CD 运行状态 |
| 获取 Pipeline 运行记录 | GET | `/feplatform/cicd/pipelineRuns` | 分页查询 Pipeline 运行历史 |
| 获取 AI 审查记录 | GET | `/feplatform/cicd/aiReviews` | 分页查询 AI 审查历史 |
| 获取 CI 总览数据 | GET | `/feplatform/cicd/overview` | 全项目 CI/CD 聚合数据 |
| 管理 Pipeline 模板 | CRUD | `/feplatform/cicd/template/*` | Pipeline 模板增删改查 |
| 生成项目 CI 配置 | POST | `/feplatform/cicd/generateConfig` | 一键生成 .gitlab-ci.yml |
| 同步 GitLab 项目 | POST | `/feplatform/cicd/syncGitlabProjects` | 从 GitLab 拉取项目列表 |

### E.7 代码医生 API

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 代码分析 | POST | `/feplatform/codedoctor/analyze` | AI 分析代码并返回问题+建议 |
