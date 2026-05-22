-- ============================================================
-- 应用模版中心 - 数据库初始化脚本
-- 严格按 PRD_Incremental_Features.md §2.1 设计
-- 幂等设计: 使用 DROP TABLE IF EXISTS + INSERT IGNORE
-- ============================================================

-- -----------------------------------------------------------
-- 1. 模版主表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `fe_template`;
CREATE TABLE `fe_template` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '模版名称',
  `code` varchar(50) NOT NULL COMMENT '模版编码(唯一标识)',
  `gitlab_url` varchar(500) NOT NULL COMMENT 'GitLab仓库地址',
  `branch` varchar(100) DEFAULT 'main' COMMENT '默认分支',
  `status` varchar(10) DEFAULT 'enable' COMMENT '状态: enable=上架, disable=下架',
  `sort_no` decimal(8,2) DEFAULT 0 COMMENT '排序号(置顶用)',
  `visibility` varchar(10) DEFAULT 'public' COMMENT '可见性: public=公开, private=私有',
  `description` text COMMENT '模版描述',
  `tech_stack_id` varchar(36) DEFAULT NULL COMMENT '关联技术栈ID',
  `framework` varchar(50) DEFAULT NULL COMMENT '框架名称(Vue2/Vue3/React/Angular/jQuery)',
  `framework_version` varchar(20) DEFAULT NULL COMMENT '框架版本',
  `node_version_range` varchar(50) DEFAULT NULL COMMENT '推荐Node版本范围',
  `placeholder_vars` json DEFAULT NULL COMMENT '占位变量声明(JSON)',
  `preview_image` varchar(500) DEFAULT NULL COMMENT '模版预览图',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模版主表';

-- -----------------------------------------------------------
-- 2. 模版版本表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `fe_template_version`;
CREATE TABLE `fe_template_version` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `template_id` varchar(36) NOT NULL COMMENT '模版ID',
  `version` varchar(30) NOT NULL COMMENT '版本号',
  `git_tag` varchar(100) DEFAULT NULL COMMENT 'Git标签',
  `commit_sha` varchar(40) DEFAULT NULL COMMENT 'Commit SHA',
  `tech_stack_id` varchar(36) DEFAULT NULL COMMENT '版本对应技术栈ID',
  `framework` varchar(50) DEFAULT NULL COMMENT '框架名称',
  `framework_version` varchar(20) DEFAULT NULL COMMENT '框架版本',
  `changelog` text COMMENT '版本变更说明',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模版版本表';

-- -----------------------------------------------------------
-- 3. 应用创建记录表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `fe_app_create_record`;
CREATE TABLE `fe_app_create_record` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `template_id` varchar(36) NOT NULL COMMENT '模版ID',
  `version_id` varchar(36) DEFAULT NULL COMMENT '模版版本ID',
  `app_id` varchar(36) DEFAULT NULL COMMENT '关联应用ID(自动写入应用列表)',
  `app_name` varchar(100) DEFAULT NULL COMMENT '应用名称',
  `app_code` varchar(50) DEFAULT NULL COMMENT '应用编码',
  `params` json DEFAULT NULL COMMENT '用户填写的配置参数',
  `output_type` varchar(20) NOT NULL COMMENT '输出类型: download=下载, gitlab=创建GitLab仓库',
  `gitlab_url` varchar(500) DEFAULT NULL COMMENT '创建的GitLab仓库地址',
  `status` varchar(20) DEFAULT 'success' COMMENT '状态: generating=生成中, success=成功, failed=失败',
  `error_message` text COMMENT '错误信息',
  `creator` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_creator` (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用创建记录表';

-- -----------------------------------------------------------
-- 4. 菜单数据 (sys_permission)
-- 一级菜单: 前端研发平台 (复用或新建)
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000001', NULL, '前端研发平台', '/feplatform', 'layouts/default/index', NULL, '/feplatform/template/list', 0, NULL, '1', 3.00, 0, 'ant-design:code-twotone', 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 模版管理
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000002', '1910100000000000001', '模版管理', '/feplatform/template/list', 'feplatform/template/FeTemplateList', NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:code-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 模版版本管理
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000003', '1910100000000000001', '模版版本', '/feplatform/template-version/list', 'feplatform/template/FeTemplateVersionList', NULL, NULL, 1, NULL, '1', 2.00, 0, 'ant-design:tags-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 应用创建记录
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000004', '1910100000000000001', '创建记录', '/feplatform/create-record/list', 'feplatform/template/FeAppCreateRecordList', NULL, NULL, 1, NULL, '1', 3.00, 0, 'ant-design:history-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 5. 模版管理 - 按钮权限
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000010', '1910100000000000002', '添加模版', NULL, NULL, NULL, NULL, 2, 'feplatform:template:add', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000011', '1910100000000000002', '编辑模版', NULL, NULL, NULL, NULL, 2, 'feplatform:template:edit', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000012', '1910100000000000002', '删除模版', NULL, NULL, NULL, NULL, 2, 'feplatform:template:delete', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000013', '1910100000000000002', '生成项目', NULL, NULL, NULL, NULL, 2, 'feplatform:template:generate', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 6. 创建记录 - 按钮权限
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000020', '1910100000000000004', '查看创建记录', NULL, NULL, NULL, NULL, 2, 'feplatform:create_record:query', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1910100000000000021', '1910100000000000004', '删除创建记录', NULL, NULL, NULL, NULL, 2, 'feplatform:create_record:delete', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 7. 给管理员角色授权 (admin角色ID: f6817f48af4fb3af11b9e8bf182f618b)
-- -----------------------------------------------------------
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000050', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000001', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000051', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000002', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000052', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000003', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000053', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000004', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000054', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000010', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000055', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000011', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000056', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000012', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000057', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000013', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000058', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000020', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1910100000000000059', 'f6817f48af4fb3af11b9e8bf182f618b', '1910100000000000021', NULL, NOW(), '127.0.0.1');

-- -----------------------------------------------------------
-- 8. 字典数据
-- -----------------------------------------------------------

-- 模版状态字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910100000000000100', '模版状态', 'fe_template_status', '模版上架/下架状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000101', '1910100000000000100', '上架', 'enable', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000102', '1910100000000000100', '下架', 'disable', NULL, 2, 1, 'admin', NOW());

-- 模版可见性字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910100000000000110', '模版可见性', 'fe_template_visibility', '模版公开/私有', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000111', '1910100000000000110', '公开', 'public', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000112', '1910100000000000110', '私有', 'private', NULL, 2, 1, 'admin', NOW());

-- 框架名称字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910100000000000120', '前端框架', 'fe_framework', '前端框架类型', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000121', '1910100000000000120', 'Vue2', 'Vue2', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000122', '1910100000000000120', 'Vue3', 'Vue3', NULL, 2, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000123', '1910100000000000120', 'React', 'React', NULL, 3, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000124', '1910100000000000120', 'Angular', 'Angular', NULL, 4, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000125', '1910100000000000120', 'jQuery', 'jQuery', NULL, 5, 1, 'admin', NOW());

-- 输出类型字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910100000000000130', '应用创建输出类型', 'fe_app_output_type', '应用创建的输出方式', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000131', '1910100000000000130', '下载ZIP', 'download', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000132', '1910100000000000130', '创建GitLab仓库', 'gitlab', NULL, 2, 1, 'admin', NOW());

-- 创建状态字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1910100000000000140', '应用创建状态', 'fe_app_create_status', '应用创建的状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000141', '1910100000000000140', '生成中', 'generating', NULL, 1, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000142', '1910100000000000140', '成功', 'success', NULL, 2, 1, 'admin', NOW());
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`) VALUES ('1910100000000000143', '1910100000000000140', '失败', 'failed', NULL, 3, 1, 'admin', NOW());
