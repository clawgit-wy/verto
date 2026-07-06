-- ============================================================
-- CI/CD 治理 - 数据库初始化脚本
-- 严格按 PRD_Incremental_Features.md §2.2 设计
-- 幂等设计: 使用 CREATE TABLE IF NOT EXISTS + INSERT IGNORE
-- ============================================================

-- -----------------------------------------------------------
-- 1. Jenkins 实例表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_jenkins_instance` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '实例名称',
  `url` varchar(500) NOT NULL COMMENT 'Jenkins访问地址',
  `token` varchar(500) DEFAULT NULL COMMENT '访问Token(加密存储)',
  `domain` varchar(200) DEFAULT NULL COMMENT '域',
  `env_type` varchar(10) NOT NULL COMMENT '环境类型: test=测试环境, prod=生产环境',
  `status` varchar(10) DEFAULT 'enable' COMMENT '状态: enable=启用, disable=停用',
  `description` text COMMENT '描述',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Jenkins实例表';

-- -----------------------------------------------------------
-- 3. 流水线表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_pipeline` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `app_id` varchar(36) NOT NULL COMMENT '关联应用ID',
  `jenkins_id` varchar(36) NOT NULL COMMENT 'Jenkins实例ID',
  `job_name` varchar(200) NOT NULL COMMENT 'Jenkins Job名称',
  `env` varchar(10) NOT NULL COMMENT '环境: dev=开发, test=测试, prod=生产',
  `tech_stack_id` varchar(36) DEFAULT NULL COMMENT '技术栈ID',
  `check_level` varchar(10) DEFAULT 'standard' COMMENT '检查级别: strict=严格, standard=标准, loose=宽松',
  `deploy_strategy` varchar(20) DEFAULT 'auto_deploy' COMMENT '部署策略: auto_deploy=自动部署, artifact_only=仅制品库, online_deploy=在线部署',
  `template_id` varchar(36) DEFAULT NULL COMMENT '关联模板ID',
  `template_version_id` varchar(36) DEFAULT NULL COMMENT '关联模板版本ID',
  `jenkinsfile` longtext COMMENT 'Jenkinsfile内容',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_jenkins_id` (`jenkins_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用-流水线表';

-- -----------------------------------------------------------
-- 4. 构建记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `fe_pipeline_build` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `pipeline_id` varchar(36) NOT NULL COMMENT '流水线ID',
  `build_no` int NOT NULL COMMENT '构建编号',
  `status` varchar(20) DEFAULT 'running' COMMENT '状态: success/failed/running/aborted',
  `duration` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `trigger_user` varchar(50) DEFAULT NULL COMMENT '触发用户',
  `commit_sha` varchar(40) DEFAULT NULL COMMENT '提交SHA',
  `tech_stack` varchar(50) DEFAULT NULL COMMENT '技术栈',
  `artifact_version` varchar(50) DEFAULT NULL COMMENT '制品版本号',
  `quality_score` decimal(5,2) DEFAULT NULL COMMENT '标准化检查得分',
  `check_level_exemptions` json DEFAULT NULL COMMENT '豁免的检查项(JSON)',
  `template_version` varchar(30) DEFAULT NULL COMMENT '构建时使用的模板版本',
  `framework_info` json DEFAULT NULL COMMENT '框架信息(JSON)',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_pipeline_id` (`pipeline_id`),
  KEY `idx_build_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='构建记录表';

-- -----------------------------------------------------------
-- CI/CD 菜单数据 (sys_permission)
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910200000000000001', '1910100000000000001', 'CI/CD治理', '/feplatform/cicd', 'layouts/default/index', NULL, '/feplatform/cicd/jenkins', 0, NULL, '1', 4.00, 0, 'ant-design:cloud-server-outlined', 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910200000000000002', '1910200000000000001', 'Jenkins实例', '/feplatform/cicd/jenkins', 'feplatform/cicd/FeJenkinsInstanceList', NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:build-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910200000000000005', '1910200000000000001', '流水线管理', '/feplatform/cicd/pipeline', 'feplatform/cicd/FePipelineList', NULL, NULL, 1, NULL, '1', 2.00, 0, 'ant-design:branches-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910200000000000006', '1910200000000000001', '构建记录', '/feplatform/cicd/build', 'feplatform/cicd/FePipelineBuildList', NULL, NULL, 1, NULL, '1', 3.00, 0, 'ant-design:history-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- CI/CD 角色授权
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910200000000000050', 'f6817f48af4fb3af11b9e8bf182f618b', '1910200000000000001', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910200000000000051', 'f6817f48af4fb3af11b9e8bf182f618b', '1910200000000000002', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910200000000000053', 'f6817f48af4fb3af11b9e8bf182f618b', '1910200000000000005', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910200000000000054', 'f6817f48af4fb3af11b9e8bf182f618b', '1910200000000000006', NULL, NOW(), '127.0.0.1');

-- -----------------------------------------------------------
-- CI/CD 字典数据
-- -----------------------------------------------------------

-- Jenkins环境类型
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910200000000000100', 'Jenkins环境类型', 'fe_jenkins_env_type', 'Jenkins实例环境类型', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000101', '1910200000000000100', '测试环境', 'test', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000102', '1910200000000000100', '生产环境', 'prod', NULL, 2, 1, 'admin', NOW());

-- Jenkins状态
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910200000000000110', 'Jenkins状态', 'fe_jenkins_status', 'Jenkins实例状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000111', '1910200000000000110', '启用', 'enable', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000112', '1910200000000000110', '停用', 'disable', NULL, 2, 1, 'admin', NOW());

-- 流水线环境
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910200000000000140', '流水线环境', 'fe_pipeline_env', '流水线部署环境', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000141', '1910200000000000140', '开发', 'dev', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000142', '1910200000000000140', '测试', 'test', NULL, 2, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000143', '1910200000000000140', '生产', 'prod', NULL, 3, 1, 'admin', NOW());

-- 检查级别
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910200000000000150', '检查级别', 'fe_check_level', 'CI标准化检查级别', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000151', '1910200000000000150', '严格', 'strict', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000152', '1910200000000000150', '标准', 'standard', NULL, 2, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000153', '1910200000000000150', '宽松', 'loose', NULL, 3, 1, 'admin', NOW());

-- 部署策略
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910200000000000160', '部署策略', 'fe_deploy_strategy', 'CI/CD部署策略', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000161', '1910200000000000160', '自动部署', 'auto_deploy', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000162', '1910200000000000160', '仅制品库', 'artifact_only', NULL, 2, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000163', '1910200000000000160', '在线部署', 'online_deploy', NULL, 3, 1, 'admin', NOW());

-- 构建状态
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910200000000000170', '构建状态', 'fe_build_status', 'CI构建状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000171', '1910200000000000170', '成功', 'success', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000172', '1910200000000000170', '失败', 'failed', NULL, 2, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000173', '1910200000000000170', '运行中', 'running', NULL, 3, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910200000000000174', '1910200000000000170', '已中止', 'aborted', NULL, 4, 1, 'admin', NOW());

-- -----------------------------------------------------------
-- 以下注释遗留表保留供参考（已删除管理功能，表结构保留兼容）
-- -----------------------------------------------------------


