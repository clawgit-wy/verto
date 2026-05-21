-- ============================================================
-- 前端研发门户 - 完整菜单初始化脚本 (v3 设计方案)
-- 基于 New_Frontend_Platform_Design_v3.md 第5章菜单结构
-- 注意: 所有 id 使用 19 位数字格式，兼容 JeecgBoot varchar(32) 列
-- 幂等设计: 使用 INSERT IGNORE，支持重复执行不报错
-- ============================================================

-- -----------------------------------------------------------
-- 1. AI 开发者中心 (一级菜单)
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000100', NULL, 'AI 开发者中心', '/ai-hub', 'layouts/default/index', NULL, '/ai-hub/skills/list', 0, NULL, '1', 3.00, 0, 'ant-design:robot-outlined', 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: Skill 资产库
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000101', '1900100000000000100', 'Skill 资产库', '/ai-hub/skills/list', 'feplatform/skill/SkillList', NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:tool-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 2. AI 开发者中心 - Skill 资产库 按钮权限
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000110', '1900100000000000101', '添加Skill', NULL, NULL, NULL, NULL, 2, 'aihub:skill:add', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000111', '1900100000000000101', '编辑Skill', NULL, NULL, NULL, NULL, 2, 'aihub:skill:edit', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000112', '1900100000000000101', '删除Skill', NULL, NULL, NULL, NULL, 2, 'aihub:skill:delete', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000113', '1900100000000000101', '测试Skill', NULL, NULL, NULL, NULL, 2, 'aihub:skill:test', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000114', '1900100000000000101', '导出配置', NULL, NULL, NULL, NULL, 2, 'aihub:skill:export', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000115', '1900100000000000101', '查看详情', NULL, NULL, NULL, NULL, 2, 'aihub:skill:query', '1', 6.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);


-- -----------------------------------------------------------
-- 3. 低代码工坊 (一级菜单)
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000200', NULL, '低代码工坊', '/lowcode-workshop', 'layouts/default/index', NULL, '/lowcode-workshop/chat2code', 0, NULL, '1', 4.00, 0, 'ant-design:thunderbolt-outlined', 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 对话出码
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000201', '1900100000000000200', '对话出码', '/lowcode-workshop/chat2code', 'feplatform/lowcode/Chat2Code', NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:message-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: Schema 转换
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000202', '1900100000000000200', 'SQL转Schema', '/lowcode-workshop/sql2schema', 'feplatform/lowcode/Sql2Schema', NULL, NULL, 1, NULL, '1', 2.00, 0, 'ant-design:swap-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 4. 低代码工坊 - 按钮权限
-- -----------------------------------------------------------

-- 对话出码按钮
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000210', '1900100000000000201', '发送对话', NULL, NULL, NULL, NULL, 2, 'lowcode:chat2code:send', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000211', '1900100000000000201', '复制代码', NULL, NULL, NULL, NULL, 2, 'lowcode:chat2code:copy', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000212', '1900100000000000201', '下载代码', NULL, NULL, NULL, NULL, 2, 'lowcode:chat2code:download', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000213', '1900100000000000201', '清空对话', NULL, NULL, NULL, NULL, 2, 'lowcode:chat2code:clear', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000214', '1900100000000000201', '预览组件', NULL, NULL, NULL, NULL, 2, 'lowcode:chat2code:preview', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- Schema转换按钮
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000220', '1900100000000000202', '转换DDL', NULL, NULL, NULL, NULL, 2, 'lowcode:sql2schema:convert', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000221', '1900100000000000202', '导出JSON', NULL, NULL, NULL, NULL, 2, 'lowcode:sql2schema:export', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000222', '1900100000000000202', '批量导入', NULL, NULL, NULL, NULL, 2, 'lowcode:sql2schema:batchImport', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000223', '1900100000000000202', '预览表单', NULL, NULL, NULL, NULL, 2, 'lowcode:sql2schema:preview', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000224', '1900100000000000202', '复制结果', NULL, NULL, NULL, NULL, 2, 'lowcode:sql2schema:copy', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);


-- -----------------------------------------------------------
-- 5. 业务管理 (一级菜单)
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000300', NULL, '业务管理', '/fe-business', 'layouts/default/index', NULL, '/fe-business/app/list', 0, NULL, '1', 6.00, 0, 'ant-design:apartment-outlined', 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 应用管理
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000301', '1900100000000000300', '应用管理', '/fe-business/app/list', 'feplatform/application/ApplicationList', NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:appstore-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 人员管理
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000302', '1900100000000000300', '人员管理', '/fe-business/developer/list', 'feplatform/developer/DeveloperList', NULL, NULL, 1, NULL, '1', 2.00, 0, 'ant-design:user-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- 子菜单: 团队管理
INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000303', '1900100000000000300', '团队管理', '/fe-business/team/list', 'feplatform/team/TeamList', NULL, NULL, 1, NULL, '1', 3.00, 0, 'ant-design:team-outlined', 1, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 6. 业务管理 - 应用管理 按钮权限
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000310', '1900100000000000301', '添加应用', NULL, NULL, NULL, NULL, 2, 'febusiness:application:add', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000311', '1900100000000000301', '编辑应用', NULL, NULL, NULL, NULL, 2, 'febusiness:application:edit', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000312', '1900100000000000301', '删除应用', NULL, NULL, NULL, NULL, 2, 'febusiness:application:delete', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000313', '1900100000000000301', '配置Skill', NULL, NULL, NULL, NULL, 2, 'febusiness:application:configSkill', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000314', '1900100000000000301', '导出配置', NULL, NULL, NULL, NULL, 2, 'febusiness:application:export', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000315', '1900100000000000301', '查看详情', NULL, NULL, NULL, NULL, 2, 'febusiness:application:query', '1', 6.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000316', '1900100000000000301', '批量删除', NULL, NULL, NULL, NULL, 2, 'febusiness:application:deleteBatch', '1', 7.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 7. 业务管理 - 人员管理 按钮权限
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000320', '1900100000000000302', '添加人员', NULL, NULL, NULL, NULL, 2, 'febusiness:developer:add', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000321', '1900100000000000302', '编辑人员', NULL, NULL, NULL, NULL, 2, 'febusiness:developer:edit', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000322', '1900100000000000302', '删除人员', NULL, NULL, NULL, NULL, 2, 'febusiness:developer:delete', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000323', '1900100000000000302', '分配团队', NULL, NULL, NULL, NULL, 2, 'febusiness:developer:assignTeam', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000324', '1900100000000000302', '查看详情', NULL, NULL, NULL, NULL, 2, 'febusiness:developer:query', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

-- -----------------------------------------------------------
-- 8. 业务管理 - 团队管理 按钮权限
-- -----------------------------------------------------------

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000330', '1900100000000000303', '添加团队', NULL, NULL, NULL, NULL, 2, 'febusiness:team:add', '1', 1.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000331', '1900100000000000303', '编辑团队', NULL, NULL, NULL, NULL, 2, 'febusiness:team:edit', '1', 2.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000332', '1900100000000000303', '删除团队', NULL, NULL, NULL, NULL, 2, 'febusiness:team:delete', '1', 3.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000333', '1900100000000000303', '成员管理', NULL, NULL, NULL, NULL, 2, 'febusiness:team:manageMember', '1', 4.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);

INSERT IGNORE INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_route`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `status`, `del_flag`, `rule_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `internal_or_external`)
VALUES ('1900100000000000334', '1900100000000000303', '查看详情', NULL, NULL, NULL, NULL, 2, 'febusiness:team:query', '1', 5.00, 0, NULL, 0, 1, 0, 0, 0, NULL, '1', 0, 0, 'admin', NOW(), NULL, NULL, 0);


-- -----------------------------------------------------------
-- 9. 给管理员角色授权 (admin角色ID: f6817f48af4fb3af11b9e8bf182f618b)
-- -----------------------------------------------------------

-- AI 开发者中心授权
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001000', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000100', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001001', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000101', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001010', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000110', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001011', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000111', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001012', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000112', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001013', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000113', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001014', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000114', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000001015', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000115', NULL, NOW(), '127.0.0.1');

-- 低代码工坊授权
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002000', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000200', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002001', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000201', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002002', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000202', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002010', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000210', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002011', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000211', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002012', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000212', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002013', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000213', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002014', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000214', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002020', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000220', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002021', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000221', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002022', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000222', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002023', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000223', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000002024', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000224', NULL, NOW(), '127.0.0.1');

-- 业务管理授权
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003000', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000300', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003001', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000301', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003002', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000302', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003003', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000303', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003010', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000310', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003011', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000311', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003012', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000312', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003013', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000313', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003014', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000314', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003015', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000315', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003016', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000316', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003020', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000320', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003021', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000321', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003022', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000322', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003023', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000323', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003024', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000324', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003030', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000330', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003031', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000331', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003032', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000332', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003033', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000333', NULL, NOW(), '127.0.0.1');
INSERT IGNORE INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`) VALUES ('1900100000000003034', 'f6817f48af4fb3af11b9e8bf182f618b', '1900100000000000334', NULL, NOW(), '127.0.0.1');


-- -----------------------------------------------------------
-- 10. 插入字典数据 (新增模块所需字典)
-- -----------------------------------------------------------

-- Skill 分类字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000200', 'Skill分类', 'skill_category', 'AI Skill资产分类', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000201', '1900100000000000200', '官方Skill', 'official', 'Jeecg官方内置Skills', 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000202', '1900100000000000200', '通用业务Skill', 'business', '公司通用业务规范Skills', 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000203', '1900100000000000200', '应用级Skill', 'app', '针对具体应用的专属Skills', 3, 1, 'admin', NOW(), NULL, NULL);

-- Skill 状态字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000210', 'Skill状态', 'skill_status', 'AI Skill状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000211', '1900100000000000210', '启用', 'enable', 'Skill已启用', 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000212', '1900100000000000210', '禁用', 'disable', 'Skill已禁用', 2, 1, 'admin', NOW(), NULL, NULL);

-- 前端框架字典 (补充)
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000220', '前端UI库', 'fe_ui_library', '前端UI组件库类型', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000221', '1900100000000000220', 'Ant Design Vue', 'AntDesignVue', 'Ant Design Vue 4.x', 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000222', '1900100000000000220', 'Element Plus', 'ElementPlus', 'Element Plus for Vue3', 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000223', '1900100000000000220', 'Vant', 'Vant', 'Vant 移动端UI库', 3, 1, 'admin', NOW(), NULL, NULL);

-- 应用状态字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000230', '应用状态', 'fe_app_status', '前端应用状态', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000231', '1900100000000000230', '活跃', 'active', '应用正在使用中', 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000232', '1900100000000000230', '归档', 'archived', '应用已归档', 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000233', '1900100000000000230', '开发中', 'developing', '应用正在开发中', 3, 1, 'admin', NOW(), NULL, NULL);

-- 人员角色字典
INSERT IGNORE INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `update_by`, `update_time`) VALUES ('1900100000000000240', '人员角色', 'developer_role', '前端开发人员角色', 0, 'admin', NOW(), 0, NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000241', '1900100000000000240', '开发者', 'developer', '普通开发人员', 1, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000242', '1900100000000000240', '技术负责人', 'lead', '技术负责人/组长', 2, 1, 'admin', NOW(), NULL, NULL);
INSERT IGNORE INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `description`, `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES ('1900100000000000243', '1900100000000000240', '经理', 'manager', '部门经理', 3, 1, 'admin', NOW(), NULL, NULL);


-- ============================================================
-- 执行完成提示
-- ============================================================
SELECT '✅ 菜单初始化完成！新增以下一级菜单:' AS result;
SELECT '  🤖 AI 开发者中心 (/ai-hub)' AS menu_1;
SELECT '  💬 低代码工坊 (/lowcode-workshop)' AS menu_2;
SELECT '  📁 业务管理 (/fe-business)' AS menu_3;
SELECT '' AS info;
SELECT '⚠️  请重新登录或刷新页面以加载新菜单' AS tip;