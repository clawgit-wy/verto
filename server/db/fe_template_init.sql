-- ============================================================
-- 前端代码模板配置中心 - 数据库初始化脚本
-- 注意: 所有 id 使用 19 位数字格式，兼容 JeecgBoot varchar(32) 列
-- 幂等设计: 使用 INSERT IGNORE，支持重复执行不报错
-- ============================================================

-- -----------------------------------------------------------
-- 1. 创建数据表
-- -----------------------------------------------------------

CREATE TABLE IF NOT EXISTS `fe_template` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_code` varchar(50) NOT NULL COMMENT '模板编码(唯一标识)',
  `template_type` varchar(20) NOT NULL COMMENT '模板类型: mobile=移动端, pc=PC端, h5=H5',
  `description` text COMMENT '模板描述',
  `framework` varchar(50) NOT NULL COMMENT '前端框架: Vue3, React, Angular',
  `build_tool` varchar(50) NOT NULL COMMENT '构建工具: Vite, Webpack',
  `ui_library` varchar(50) DEFAULT NULL COMMENT 'UI组件库: AntDesignVue, ElementPlus, Vant',
  `language` varchar(20) DEFAULT 'TypeScript' COMMENT '开发语言: TypeScript, JavaScript',
  `modules` json DEFAULT NULL COMMENT '包含的模块配置(JSON)',
  `tech_stack` json DEFAULT NULL COMMENT '完整技术栈配置(JSON)',
  `gitlab_config` json DEFAULT NULL COMMENT 'GitLab配置(JSON)',
  `ci_cd_config` json DEFAULT NULL COMMENT 'CI/CD配置(JSON)',
  `preview_image` varchar(500) DEFAULT NULL COMMENT '模板预览图',
  `is_default` char(1) DEFAULT '0' COMMENT '是否默认模板: 1=是, 0=否',
  `status` varchar(10) DEFAULT 'enable' COMMENT '状态: enable/disable',
  `sort_no` decimal(8,2) DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端代码模板表';

CREATE TABLE IF NOT EXISTS `fe_project_generation` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `project_code` varchar(50) NOT NULL COMMENT '项目编码',
  `template_id` varchar(36) NOT NULL COMMENT '使用的模板ID',
  `template_name` varchar(100) DEFAULT NULL COMMENT '模板名称',
  `config_snapshot` json NOT NULL COMMENT '配置快照(JSON)',
  `generation_type` varchar(20) NOT NULL COMMENT '生成方式: gitlab=推送到GitLab, download=下载到本地',
  `gitlab_url` varchar(500) DEFAULT NULL COMMENT 'GitLab项目地址',
  `download_url` varchar(500) DEFAULT NULL COMMENT '下载地址',
  `status` varchar(20) DEFAULT 'success' COMMENT '状态: generating=生成中, success=成功, failed=失败',
  `error_message` text COMMENT '错误信息',
  `generated_by` varchar(50) DEFAULT NULL COMMENT '生成人',
  `generated_time` datetime DEFAULT NULL COMMENT '生成时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前端项目生成记录表';

-- -----------------------------------------------------------
-- 2. 插入菜单数据 (sys_permission)
-- menu_type: 0=一级菜单, 1=子菜单, 2=按钮/权限
-- -----------------------------------------------------------

-- 一级菜单: 低代码服务
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000001', NULL, '低代码服务', '/lowcode', 'layouts/default/index', NULL, '/lowcode/template/list', 0, NULL, '1', 5.00, 0, 'ant-design:appstore-twotone', 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 模板管理
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000002', '1900100000000000001', '模板管理', '/lowcode/template/list', 'lowcode/template/FeTemplateList', NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:code-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 项目生成记录
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000003', '1900100000000000001', '项目生成记录', '/lowcode/project/list', 'lowcode/project/FeProjectGenerationList', NULL, NULL, 1, NULL, '1', 2.00, 0, 'ant-design:history-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 3. 模板管理 - 按钮权限
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000010', '1900100000000000002', '添加模板', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:add', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000011', '1900100000000000002', '编辑模板', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:edit', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000012', '1900100000000000002', '删除模板', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:delete', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000013', '1900100000000000002', '批量删除模板', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:deleteBatch', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000014', '1900100000000000002', '导出excel', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:exportXls', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000015', '1900100000000000002', '导入excel', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:importExcel', '1', 6.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000016', '1900100000000000002', '生成项目', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_template:generate', '1', 7.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 4. 项目生成记录 - 按钮权限
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000020', '1900100000000000003', '查看生成记录', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_project_gen:query', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000021', '1900100000000000003', '删除生成记录', NULL, NULL, NULL, NULL, 2, 'lowcode:fe_project_gen:delete', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 5. 给管理员角色授权 (admin角色ID: f6817f48af4fb3af11b9e8bf182f618b)
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000050', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000001', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000051', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000002', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000052', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000003', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000053', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000010', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000054', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000011', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000055', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000012', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000056', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000013', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000057', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000014', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000058', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000015', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000059', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000016', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000060', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000020', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000000061', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000021', NULL, NOW(), '127.0.0.1');

-- -----------------------------------------------------------
-- 6. 插入字典数据
-- -----------------------------------------------------------

-- 模板类型字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000100', '前端模板类型', 'fe_template_type', '前端代码模板类型', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000101', '1900100000000000100', '移动端', 'mobile', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000102', '1900100000000000100', 'PC端', 'pc', NULL, 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000103', '1900100000000000100', 'H5', 'h5', NULL, 3, 1, 'admin', NOW(), NULL, NULL);

-- 模板状态字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000110', '前端模板状态', 'fe_template_status', '前端代码模板状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000111', '1900100000000000110', '启用', 'enable', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000112', '1900100000000000110', '禁用', 'disable', NULL, 2, 1, 'admin', NOW(), NULL, NULL);

-- 生成方式字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000120', '项目生成方式', 'fe_generation_type', '前端项目生成方式', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000121', '1900100000000000120', '推送到GitLab', 'gitlab', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000122', '1900100000000000120', '下载到本地', 'download', NULL, 2, 1, 'admin', NOW(), NULL, NULL);

-- 生成状态字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000130', '项目生成状态', 'fe_generation_status', '前端项目生成状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000131', '1900100000000000130', '生成中', 'generating', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000132', '1900100000000000130', '成功', 'success', NULL, 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000133', '1900100000000000130', '失败', 'failed', NULL, 3, 1, 'admin', NOW(), NULL, NULL);

-- 前端框架字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000140', '前端框架', 'fe_framework', '前端开发框架类型', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000141', '1900100000000000140', 'Vue3', 'Vue3', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000142', '1900100000000000140', 'React', 'React', NULL, 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000143', '1900100000000000140', 'Angular', 'Angular', NULL, 3, 1, 'admin', NOW(), NULL, NULL);

-- 构建工具字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000150', '构建工具', 'fe_build_tool', '前端项目构建工具', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000151', '1900100000000000150', 'Vite', 'Vite', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000152', '1900100000000000150', 'Webpack', 'Webpack', NULL, 2, 1, 'admin', NOW(), NULL, NULL);

-- UI组件库字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000160', 'UI组件库', 'fe_ui_library', '前端UI组件库', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000161', '1900100000000000160', 'Ant Design Vue', 'AntDesignVue', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000162', '1900100000000000160', 'Element Plus', 'ElementPlus', NULL, 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000163', '1900100000000000160', 'Vant', 'Vant', NULL, 3, 1, 'admin', NOW(), NULL, NULL);

-- 开发语言字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000170', '开发语言', 'fe_language', '前端开发语言', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000171', '1900100000000000170', 'TypeScript', 'TypeScript', NULL, 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000172', '1900100000000000170', 'JavaScript', 'JavaScript', NULL, 2, 1, 'admin', NOW(), NULL, NULL);

-- -----------------------------------------------------------
-- 7. 插入预设模板数据
-- -----------------------------------------------------------

INSERT IGNORE INTO `fe_template` (`id`, `template_name`, `template_code`, `template_type`, `description`, `framework`, `build_tool`, `ui_library`, `language`, `modules`, `tech_stack`, `gitlab_config`, `ci_cd_config`, `preview_image`, `is_default`, `status`, `sort_no`, `create_by`, `create_time`, `del_flag`) VALUES
('1900100000000000200', 'Vue3 PC端管理后台', 'vue3-pc-admin', 'pc', '基于Vue3 + Vite6 + Ant Design Vue4 + TypeScript的企业级管理后台模板，内置布局、路由、状态管理、API层封装等完整能力', 'Vue3', 'Vite', 'AntDesignVue', 'TypeScript',
'{"router":{"enabled":true,"mode":"history","lazy":true},"state":{"enabled":true,"library":"pinia"},"api":{"enabled":true,"library":"axios","interceptor":true},"ui":{"enabled":true,"library":"ant-design-vue","theme":"default"},"utils":{"enabled":true,"items":["request","storage","validate","date"]},"components":{"enabled":true,"global":["Layout","Header","Sidebar"]},"styles":{"enabled":true,"preprocessor":"less","cssReset":true},"build":{"enabled":true,"proxy":true,"gzip":true,"cdn":false}}',
'{"framework":"Vue 3.4.0","buildTool":"Vite 6.0.0","uiLibrary":"Ant Design Vue 4.2.0","language":"TypeScript 5.3.0","stateManager":"Pinia 2.1.0","router":"Vue Router 4.2.0","http":"Axios 1.6.0","cssPreprocessor":"Less 4.2.0","codeStyle":"ESLint + Prettier","testing":"Vitest + Vue Test Utils"}',
NULL, NULL, NULL, '1', 'enable', 1.00, 'admin', NOW(), '0');

INSERT IGNORE INTO `fe_template` (`id`, `template_name`, `template_code`, `template_type`, `description`, `framework`, `build_tool`, `ui_library`, `language`, `modules`, `tech_stack`, `gitlab_config`, `ci_cd_config`, `preview_image`, `is_default`, `status`, `sort_no`, `create_by`, `create_time`, `del_flag`) VALUES
('1900100000000000201', 'Vue3 PC端门户', 'vue3-pc-portal', 'pc', '基于Vue3 + Vite6 的企业门户/展示型网站模板，支持SEO优化、响应式布局、多语言切换', 'Vue3', 'Vite', 'AntDesignVue', 'TypeScript',
'{"router":{"enabled":true,"mode":"history","lazy":true},"state":{"enabled":false,"library":""},"api":{"enabled":true,"library":"axios","interceptor":true},"ui":{"enabled":true,"library":"ant-design-vue","theme":"default"},"utils":{"enabled":true,"items":["request","storage","date"]},"components":{"enabled":true,"global":["Header","Footer"]},"styles":{"enabled":true,"preprocessor":"less","cssReset":true},"build":{"enabled":true,"proxy":false,"gzip":true,"cdn":true}}',
'{"framework":"Vue 3.4.0","buildTool":"Vite 6.0.0","uiLibrary":"Ant Design Vue 4.2.0","language":"TypeScript 5.3.0","router":"Vue Router 4.2.0","http":"Axios 1.6.0","cssPreprocessor":"Less 4.2.0","codeStyle":"ESLint + Prettier","testing":"Vitest + Vue Test Utils"}',
NULL, NULL, NULL, '0', 'enable', 2.00, 'admin', NOW(), '0');

INSERT IGNORE INTO `fe_template` (`id`, `template_name`, `template_code`, `template_type`, `description`, `framework`, `build_tool`, `ui_library`, `language`, `modules`, `tech_stack`, `gitlab_config`, `ci_cd_config`, `preview_image`, `is_default`, `status`, `sort_no`, `create_by`, `create_time`, `del_flag`) VALUES
('1900100000000000202', 'Vue3 移动端标准模板', 'vue3-mobile-standard', 'mobile', '基于Vue3 + Vite6 + Vant4 的移动端H5应用模板，适配移动端交互规范，支持手势操作和响应式适配', 'Vue3', 'Vite', 'Vant', 'TypeScript',
'{"router":{"enabled":true,"mode":"hash","lazy":true},"state":{"enabled":true,"library":"pinia"},"api":{"enabled":true,"library":"axios","interceptor":true},"ui":{"enabled":true,"library":"vant","theme":"default"},"utils":{"enabled":true,"items":["request","storage","validate","date"]},"components":{"enabled":true,"global":["NavBar","TabBar"]},"styles":{"enabled":true,"preprocessor":"less","cssReset":true},"build":{"enabled":true,"proxy":true,"gzip":true,"cdn":false}}',
'{"framework":"Vue 3.4.0","buildTool":"Vite 6.0.0","uiLibrary":"Vant 4.8.0","language":"TypeScript 5.3.0","stateManager":"Pinia 2.1.0","router":"Vue Router 4.2.0","http":"Axios 1.6.0","cssPreprocessor":"Less 4.2.0","codeStyle":"ESLint + Prettier","testing":"Vitest + Vue Test Utils"}',
NULL, NULL, NULL, '1', 'enable', 1.00, 'admin', NOW(), '0');

INSERT IGNORE INTO `fe_template` (`id`, `template_name`, `template_code`, `template_type`, `description`, `framework`, `build_tool`, `ui_library`, `language`, `modules`, `tech_stack`, `gitlab_config`, `ci_cd_config`, `preview_image`, `is_default`, `status`, `sort_no`, `create_by`, `create_time`, `del_flag`) VALUES
('1900100000000000203', 'React PC端管理后台', 'react-pc-admin', 'pc', '基于React 18 + Vite6 + Ant Design 5 + TypeScript的企业级管理后台模板', 'React', 'Vite', 'AntDesignReact', 'TypeScript',
'{"router":{"enabled":true,"mode":"history","lazy":true},"state":{"enabled":true,"library":"redux"},"api":{"enabled":true,"library":"axios","interceptor":true},"ui":{"enabled":true,"library":"antd","theme":"default"},"utils":{"enabled":true,"items":["request","storage","validate","date"]},"components":{"enabled":true,"global":["Layout","Header","Sidebar"]},"styles":{"enabled":true,"preprocessor":"less","cssReset":true},"build":{"enabled":true,"proxy":true,"gzip":true,"cdn":false}}',
'{"framework":"React 18.2.0","buildTool":"Vite 6.0.0","uiLibrary":"Ant Design 5.12.0","language":"TypeScript 5.3.0","stateManager":"Redux Toolkit 2.0.0","router":"React Router 6.20.0","http":"Axios 1.6.0","cssPreprocessor":"Less 4.2.0","codeStyle":"ESLint + Prettier","testing":"Jest + React Testing Library"}',
NULL, NULL, NULL, '0', 'enable', 3.00, 'admin', NOW(), '0');

INSERT IGNORE INTO `fe_template` (`id`, `template_name`, `template_code`, `template_type`, `description`, `framework`, `build_tool`, `ui_library`, `language`, `modules`, `tech_stack`, `gitlab_config`, `ci_cd_config`, `preview_image`, `is_default`, `status`, `sort_no`, `create_by`, `create_time`, `del_flag`) VALUES
('1900100000000000204', 'React 移动端标准模板', 'react-mobile-standard', 'mobile', '基于React 18 + Vite6 + Ant Design Mobile 5 的移动端H5应用模板', 'React', 'Vite', 'AntDesignMobile', 'TypeScript',
'{"router":{"enabled":true,"mode":"hash","lazy":true},"state":{"enabled":true,"library":"redux"},"api":{"enabled":true,"library":"axios","interceptor":true},"ui":{"enabled":true,"library":"antd-mobile","theme":"default"},"utils":{"enabled":true,"items":["request","storage","validate","date"]},"components":{"enabled":true,"global":["NavBar","TabBar"]},"styles":{"enabled":true,"preprocessor":"less","cssReset":true},"build":{"enabled":true,"proxy":true,"gzip":true,"cdn":false}}',
'{"framework":"React 18.2.0","buildTool":"Vite 6.0.0","uiLibrary":"Ant Design Mobile 5.30.0","language":"TypeScript 5.3.0","stateManager":"Redux Toolkit 2.0.0","router":"React Router 6.20.0","http":"Axios 1.6.0","cssPreprocessor":"Less 4.2.0","codeStyle":"ESLint + Prettier","testing":"Jest + React Testing Library"}',
NULL, NULL, NULL, '0', 'enable', 2.00, 'admin', NOW(), '0');
