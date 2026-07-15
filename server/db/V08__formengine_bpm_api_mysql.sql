-- =====================================================================
-- 表单引擎 - 流程BPM对接接口配置表 (MySQL 8.0)
-- 说明：每个流程独立拥有7个BPM对接接口（对应docs/流程配置.txt规范）
--       创建流程时自动生成7条接口记录，可在流程详情中查看并同步到BPM平台
-- 7个接口：
--   1. formFields    - 获取业务表单字段接口
--   2. roles         - 业务系统角色配置接口
--   3. eventListener - 流程事件调用业务监听
--   4. formValues    - 获取业务表单字段值接口
--   5. templateEvent - 流程模板保存/更新/删除事件接口
--   6. callback      - 流程事件的业务监听接口
--   7. templates     - 获取业务表单模板接口
-- =====================================================================

CREATE TABLE IF NOT EXISTS fe_process_bpm_api (
    id              VARCHAR(64)  NOT NULL COMMENT '主键ID',
    process_id      VARCHAR(64)  NOT NULL COMMENT '流程ID（关联 fe_process.id）',
    api_key         VARCHAR(50)  NOT NULL COMMENT '接口标识: formFields/roles/eventListener/formValues/templateEvent/callback/templates',
    api_name        VARCHAR(200) NOT NULL COMMENT '接口名称',
    api_url         VARCHAR(500)          COMMENT '接口URL（业务系统提供的访问地址）',
    api_method      VARCHAR(10)  DEFAULT 'GET' COMMENT 'HTTP方法: GET/POST',
    api_description VARCHAR(500)          COMMENT '接口说明',
    sync_status     VARCHAR(20)  DEFAULT 'unsynced' COMMENT '同步状态: unsynced=未同步, synced=已同步, failed=同步失败',
    sync_time       TIMESTAMP             COMMENT '最近同步时间',
    sync_result     VARCHAR(500)          COMMENT '同步结果信息',
    sort_no         INT          DEFAULT 0 COMMENT '排序号',
    create_by       VARCHAR(50)           COMMENT '创建人',
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by       VARCHAR(50)           COMMENT '更新人',
    update_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_process_id (process_id),
    UNIQUE KEY uk_process_api (process_id, api_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单引擎-流程BPM对接接口配置';
