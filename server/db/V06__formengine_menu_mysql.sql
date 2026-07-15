-- =====================================================================
-- 表单引擎工作台 - 系统菜单初始化（sys_permission + sys_role_permission）
-- 功能：将 form-engine 4 个页面加入 jeecg-boot 左侧导航菜单
-- 结构：1 个父菜单（menu_type=0）+ 4 个子菜单（menu_type=1）
-- 权限：授权给 admin 角色（role_code='admin'）
-- 说明：菜单路由会由 LAYOUT 包裹，解决原先 basicRoutes 全屏路由导致的整页刷新问题
-- =====================================================================

-- 防重复插入：先清理旧数据（如有）
DELETE FROM sys_role_permission WHERE permission_id IN ('fe_menu_root','fe_menu_list','fe_menu_designer','fe_menu_simulation','fe_menu_export');
DELETE FROM sys_permission WHERE id IN ('fe_menu_root','fe_menu_list','fe_menu_designer','fe_menu_simulation','fe_menu_export');

-- =====================================================================
-- 1. 父菜单：表单引擎（顶层菜单，menu_type=0）
-- =====================================================================
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external, is_route) VALUES
('fe_menu_root', NULL, '表单引擎', '/form-engine', 'layouts/default/index', 'FormEngine', '/form-engine/list', 0, NULL, '0', 6.00, 1, 'ant-design:form-outlined', 0, NULL, 0, NULL, '表单引擎与流程仿真测试工作台', 'admin', NOW(), 'admin', NOW(), 0, 0, '1', 0, 1);

-- =====================================================================
-- 2. 子菜单：流程列表（主入口，menu_type=1）
-- =====================================================================
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external, is_route) VALUES
('fe_menu_list', 'fe_menu_root', '流程列表', '/form-engine/list', 'form-engine/ProcessList', 'FormEngineList', NULL, 1, NULL, '0', 1.00, NULL, 'ant-design:unordered-list-outlined', 1, 1, 0, NULL, '流程列表-表单引擎主入口', 'admin', NOW(), 'admin', NOW(), 0, 0, '1', 0, 1);

-- =====================================================================
-- 3. 子菜单：表单设计器（menu_type=1）
-- =====================================================================
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external, is_route) VALUES
('fe_menu_designer', 'fe_menu_root', '表单设计器', '/form-engine/designer', 'form-engine/FormDesigner', 'FormEngineDesigner', NULL, 1, NULL, '0', 2.00, NULL, 'ant-design:highlight-outlined', 1, 1, 0, NULL, '基于form-create的表单设计器', 'admin', NOW(), 'admin', NOW(), 0, 0, '1', 0, 1);

-- =====================================================================
-- 4. 子菜单：流程仿真测试（menu_type=1）
-- =====================================================================
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external, is_route) VALUES
('fe_menu_simulation', 'fe_menu_root', '流程仿真测试', '/form-engine/simulation', 'form-engine/ProcessSimulation', 'FormEngineSimulation', NULL, 1, NULL, '0', 3.00, NULL, 'ant-design:play-circle-outlined', 1, 1, 0, NULL, '多角色流程仿真测试沙箱', 'admin', NOW(), 'admin', NOW(), 0, 0, '1', 0, 1);

-- =====================================================================
-- 5. 子菜单：代码导出（menu_type=1）
-- =====================================================================
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external, is_route) VALUES
('fe_menu_export', 'fe_menu_root', '代码导出', '/form-engine/export', 'form-engine/CodeExport', 'FormEngineExport', NULL, 1, NULL, '0', 4.00, NULL, 'ant-design:download-outlined', 1, 1, 0, NULL, '基于Schema生成Vue3 SFC代码导出', 'admin', NOW(), 'admin', NOW(), 0, 0, '1', 0, 1);

-- =====================================================================
-- 6. 授权给 admin 角色（sys_role_permission）
-- =====================================================================
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES
(REPLACE(UUID(),'-',''), (SELECT id FROM sys_role WHERE role_code='admin' LIMIT 1), 'fe_menu_root', NULL, NOW(), '127.0.0.1'),
(REPLACE(UUID(),'-',''), (SELECT id FROM sys_role WHERE role_code='admin' LIMIT 1), 'fe_menu_list', NULL, NOW(), '127.0.0.1'),
(REPLACE(UUID(),'-',''), (SELECT id FROM sys_role WHERE role_code='admin' LIMIT 1), 'fe_menu_designer', NULL, NOW(), '127.0.0.1'),
(REPLACE(UUID(),'-',''), (SELECT id FROM sys_role WHERE role_code='admin' LIMIT 1), 'fe_menu_simulation', NULL, NOW(), '127.0.0.1'),
(REPLACE(UUID(),'-',''), (SELECT id FROM sys_role WHERE role_code='admin' LIMIT 1), 'fe_menu_export', NULL, NOW(), '127.0.0.1');
