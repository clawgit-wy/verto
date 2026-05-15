-- =============================================
-- 前端研发平台 - 数据库初始化脚本
-- 模块: feplatform
-- 版本: V1.0
-- 日期: 2026-05-12
-- =============================================

-- -------------------------------------------
-- 1. AI Skill 资产表（核心表）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_skill` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_skill_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI Skill资产表';

-- -------------------------------------------
-- 2. MCP-Skill 关联表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_mcp_skill_rel` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `mcp_id` varchar(36) NOT NULL COMMENT 'MCP Server ID',
  `skill_id` varchar(36) NOT NULL COMMENT 'Skill ID',
  `sort_no` decimal(8,2) DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`),
  KEY `idx_mcp_id` (`mcp_id`),
  KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP-Skill关联表';

-- -------------------------------------------
-- 官方 Skill 种子数据
-- -------------------------------------------
INSERT INTO `fe_skill` (`id`, `name`, `code`, `category`, `description`, `prompt_template`, `version`, `status`, `sort_no`) VALUES
('1', 'Jeecg代码生成', 'jeecg-codegen', 'official', '一句话生成全套CRUD代码，包括后端Entity/Mapper/Service/Controller和前端列表/表单页面', '你是JeecgBoot代码生成专家。根据用户的需求描述，生成符合JeecgBoot规范的完整CRUD代码。

## 后端规范
- Entity继承JeecgEntity，使用@TableName注解
- Mapper继承BaseMapper，使用@Mapper注解
- Service继承IService，ServiceImpl继承ServiceImpl<Mapper, Entity>
- Controller继承JeecgController，使用@Tag和@Operation注解
- 分页查询使用QueryGenerator.initQueryWrapper
- 返回结果统一使用Result.OK()

## 前端规范
- 使用BasicTable和BasicForm组件
- API层使用defHttp封装请求
- 列表页面使用useListPage hook
- 表单使用Modal弹窗方式

请根据以下需求生成代码：', '1.0.0', 'enable', 1),

('2', 'Online表单', 'jeecg-onlform', 'official', '一句话创建Online表单，自动配置字段和校验规则', '你是JeecgBoot Online表单配置专家。根据用户的需求描述，生成Online表单的JSON配置。

## 配置规范
- 字段类型映射：varchar→Input, text→TextArea, int→InputNumber, datetime→DatePicker
- 字典字段使用Dictionary类型
- 外键字段使用Popup组件
- 必填字段设置validateRules
- 列表展示字段配置showInList
- 表单展示字段配置showInForm

请根据以下需求生成Online表单配置：', '1.0.0', 'enable', 2),

('3', 'Online报表', 'jeecg-onlreport', 'official', '一句话创建Online报表，配置数据源和展示样式', '你是JeecgBoot Online报表配置专家。根据用户的需求描述，生成Online报表的SQL和配置。

## 报表规范
- SQL使用标准MySQL语法
- 支持参数占位符 ${param}
- 统计字段使用SUM/COUNT/AVG
- 分组使用GROUP BY
- 排序使用ORDER BY

请根据以下需求生成Online报表配置：', '1.0.0', 'enable', 3),

('4', '设计器表单', 'jeecg-desform', 'official', '一句话创建设计器表单，拖拽式可视化表单', '你是JeecgBoot表单设计器专家。根据用户的需求描述，生成设计器表单的JSON配置。

## 设计器规范
- 使用Ant Design Vue组件
- 布局使用栅格系统（row/col）
- 表单验证使用rules配置
- 联动使用 linkage 配置
- 子表使用子表组件

请根据以下需求生成设计器表单配置：', '1.0.0', 'enable', 4),

('5', '审批流程', 'jeecg-bpmn', 'official', '一句话画审批流程，自动生成BPMN流程定义', '你是JeecgBoot BPMN流程设计专家。根据用户的需求描述，生成BPMN流程定义XML。

## 流程规范
- 使用BPMN 2.0规范
- 开始事件→审批节点→结束事件
- 审批节点配置候选人/候选组
- 支持会签/或签
- 支持条件分支
- 支持驳回/转办

请根据以下需求生成BPMN流程定义：', '1.0.0', 'enable', 5);

-- =============================================
-- 阶段二：业务管理表
-- =============================================

-- -------------------------------------------
-- 3. 前端应用管理表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_application` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端应用管理表';

-- -------------------------------------------
-- 4. 前端团队表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_team` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `team_name` varchar(100) NOT NULL COMMENT '团队名称',
  `team_code` varchar(50) NOT NULL COMMENT '团队编码',
  `leader_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
  `description` text COMMENT '团队描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_code` (`team_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端团队表';

-- -------------------------------------------
-- 5. 前端开发人员表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_developer` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_team_id` (`team_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端开发人员表';

-- -------------------------------------------
-- 6. Pipeline模板表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_pipeline_template` (
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
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Pipeline模板表';

-- -------------------------------------------
-- 7. 项目Pipeline配置表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_project_pipeline` (
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
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_gitlab_project_id` (`gitlab_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目Pipeline配置表';

-- -------------------------------------------
-- 8. AI使用行为日志表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_ai_usage_log` (
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

-- -------------------------------------------
-- 业务 Skill 种子数据
-- -------------------------------------------
INSERT INTO `fe_skill` (`id`, `name`, `code`, `category`, `description`, `prompt_template`, `version`, `status`, `sort_no`) VALUES
('6', '业务代码规范', 'bus-code-style', 'business', '确保生成的代码符合公司前端代码规范', '你是公司前端代码规范专家。所有生成的代码必须遵循以下规范：

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

## 样式规范
- 使用 Less 预处理器
- 类名使用 BEM 命名规范
- 全局样式变量统一管理

请确保生成的所有代码严格遵守以上规范：', '1.0.0', 'enable', 10),

('7', '业务接口规范', 'bus-api-standard', 'business', '确保API接口符合公司接口设计规范', '你是公司接口规范专家。所有接口设计必须遵循以下规范：

## 接口命名规范
- RESTful风格：GET查询/POST新增/PUT修改/DELETE删除
- URL使用kebab-case：/api/vehicle-info
- 分页接口统一参数：pageNo, pageSize

## 响应格式规范
- 统一使用 Result<T> 包装
- 成功：{code: 200, result: data, message: "操作成功"}
- 失败：{code: 500, result: null, message: "错误信息"}

## 字段命名规范
- 数据库字段使用snake_case
- Java字段使用camelCase
- JSON字段使用camelCase
- 前端字段使用camelCase

请确保所有接口设计遵守以上规范：', '1.0.0', 'enable', 11),

('8', '组件库规范', 'bus-component-lib', 'business', '确保使用公司组件库规范开发', '你是公司组件库规范专家。开发时必须遵循以下组件使用规范：

## 表格组件
- 使用 BasicTable 组件
- 配置 columns 定义列
- 使用 useListPage hook 封装逻辑
- 支持行选择、导出、导入

## 表单组件
- 使用 BasicForm 组件
- 配置 schemas 定义表单项
- 支持动态表单、表单联动
- 使用 Modal 方式编辑

## 弹窗组件
- 使用 BasicModal 组件
- 支持 useModal hook
- 支持自定义 footer

## 树形组件
- 使用 BasicTree 组件
- 支持异步加载
- 支持搜索过滤

请确保所有组件使用遵守以上规范：', '1.0.0', 'enable', 12);
