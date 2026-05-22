# 手动执行 SQL 说明

## 目录文件

| 顺序 | 文件名 | 来源 | 用途 |
|------|--------|------|------|
| 0 | `jeecgboot-mysql-5.7.sql` | JeecgBoot 原始脚本 | 全新部署：JeecgBoot 框架基础表（用户/角色/菜单/字典等）。**仅首次全新部署执行** |
| 0 | `menu_init_v3.sql` | JeecgBoot 原始脚本 | 全新部署：菜单初始化。**仅首次全新部署执行** |
| 0 | `fix_qrtz_table_case.sql` | JeecgBoot 原始脚本 | 修复 Quartz 表大小写问题 |
| 0 | `tables_nacos.sql` / `tables_xxl_job.sql` | JeecgBoot 原始脚本 | 微服务部署时使用（单体可忽略） |
| **1** | **`V01__feplatform_init.sql`** | feplatform 模块 | **前端研发平台核心表**：`fe_skill`、`fe_mcp_skill_rel`、`fe_application`、`fe_team`、`fe_developer`、`fe_pipeline_template`、`fe_project_pipeline`、`fe_ai_usage_log` + Skill 种子数据 |
| **2** | **`V02__fe_template_center_init.sql`** | feplatform/template 模块 | **应用模版中心** (PRD §2.1)：`fe_template`、`fe_template_version`、`fe_app_create_record` + 菜单 + 字典 + 角色授权 |
| **3** | **`V03__fe_cicd_init.sql`** | feplatform/cicd 模块 | **CI/CD 治理** (PRD §2.2)：`fe_jenkins_instance`、`fe_tech_stack`、`fe_node_version`、`fe_pipeline`、`fe_pipeline_build` + 菜单 + 字典 + 技术栈/Node 种子数据 |

## 执行顺序

### 全新部署（首次）

```bash
# 1. 创建数据库
mysql -uroot -p -e "CREATE DATABASE jeecg-boot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 导入 JeecgBoot 基础结构
mysql -uroot -p jeecg-boot < jeecgboot-mysql-5.7.sql
mysql -uroot -p jeecg-boot < menu_init_v3.sql
mysql -uroot -p jeecg-boot < fix_qrtz_table_case.sql

# 3. 导入业务模块（按 V01 → V02 → V03 顺序）
mysql -uroot -p jeecg-boot < V01__feplatform_init.sql
mysql -uroot -p jeecg-boot < V02__fe_template_center_init.sql
mysql -uroot -p jeecg-boot < V03__fe_cicd_init.sql
```

### 增量升级（已部署过 JeecgBoot 基础库）

只需按顺序执行业务 SQL：

```bash
mysql -uroot -p jeecg-boot < V01__feplatform_init.sql
mysql -uroot -p jeecg-boot < V02__fe_template_center_init.sql
mysql -uroot -p jeecg-boot < V03__fe_cicd_init.sql
```

## 幂等性说明

三个业务 SQL 现在均**完全幂等**，可重复安全执行而不会丢数据：

- `V01__feplatform_init.sql`：`CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`
- `V02__fe_template_center_init.sql`：`CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`
- `V03__fe_cicd_init.sql`：`CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`

> ✅ 三份脚本可任意次重复执行，**不会清空已有业务数据**，也不会因主键重复而报 #1062 错误。

## SQL 内容对应模块

```
V01__feplatform_init.sql            ← jeecg-boot-module-feplatform/src/main/resources/feplatform_init.sql
V02__fe_template_center_init.sql    ← jeecg-boot-module-feplatform/src/main/resources/fe_template_center_init.sql
V03__fe_cicd_init.sql               ← jeecg-boot-module-feplatform/src/main/resources/cicd_init.sql
```

源 SQL 仍保留在模块 `resources` 目录下随 jar 打包，本目录的副本仅用于运维同学手动执行，两边内容保持一致。

## 验证

执行完成后，可在 MySQL 中验证关键表：

```sql
SHOW TABLES LIKE 'fe_%';
-- 应包含：fe_skill, fe_mcp_skill_rel, fe_application, fe_team, fe_developer,
--        fe_pipeline_template, fe_project_pipeline, fe_ai_usage_log,
--        fe_template, fe_template_version, fe_app_create_record,
--        fe_jenkins_instance, fe_tech_stack, fe_node_version,
--        fe_pipeline, fe_pipeline_build

-- 检查菜单是否插入
SELECT id, name, url FROM sys_permission WHERE id LIKE '19101%' OR id LIKE '19102%';

-- 检查字典是否插入
SELECT dict_code FROM sys_dict WHERE dict_code LIKE 'fe_%';
```
