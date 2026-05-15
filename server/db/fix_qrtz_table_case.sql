-- ===============================================
-- JeecgBoot Quartz 表名大小写修复脚本
-- 修复原因: Linux MySQL 默认 lower_case_table_names=0，表名区分大小写
-- Quartz 配置使用大写前缀 QRTZ_，但 SQL 脚本创建的是小写 qrtz_
-- 执行方式: 在 MySQL 客户端执行此脚本
-- ===============================================

-- 先检查当前数据库中存在的 qrtz_ 小写表
-- SHOW TABLES LIKE 'qrtz_%';

-- 事务开始
START TRANSACTION;

-- 重命名所有 qrtz_ 表为大写 QRTZ_
RENAME TABLE `qrtz_blob_triggers` TO `QRTZ_BLOB_TRIGGERS`;
RENAME TABLE `qrtz_calendars` TO `QRTZ_CALENDARS`;
RENAME TABLE `qrtz_cron_triggers` TO `QRTZ_CRON_TRIGGERS`;
RENAME TABLE `qrtz_fired_triggers` TO `QRTZ_FIRED_TRIGGERS`;
RENAME TABLE `qrtz_job_details` TO `QRTZ_JOB_DETAILS`;
RENAME TABLE `qrtz_locks` TO `QRTZ_LOCKS`;
RENAME TABLE `qrtz_paused_trigger_grps` TO `QRTZ_PAUSED_TRIGGER_GRPS`;
RENAME TABLE `qrtz_scheduler_state` TO `QRTZ_SCHEDULER_STATE`;
RENAME TABLE `qrtz_simple_triggers` TO `QRTZ_SIMPLE_TRIGGERS`;
RENAME TABLE `qrtz_simprop_triggers` TO `QRTZ_SIMPROP_TRIGGERS`;
RENAME TABLE `qrtz_triggers` TO `QRTZ_TRIGGERS`;

-- 提交事务
COMMIT;

-- 验证: 检查表是否已重命名成功
-- SHOW TABLES LIKE 'QRTZ_%';

-- 同时检查是否有 sys_quartz_* 相关表也需要重命名
-- RENAME TABLE `sys_quartz_*` TO `SYS_QUARTZ_*`;
