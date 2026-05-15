# 前端研发平台 - 开发进度跟踪

> 最后更新: 2026-05-12

## 阶段一：基础设施与 Skill 核心 - ✅ 已完成

### 步骤 1.1：创建后端模块骨架 ✅
- [x] 创建 `jeecg-boot-module-feplatform` Maven 模块
- [x] pom.xml 配置依赖（jeecg-system-local-api + jeecg-boot-module-airag）
- [x] 注册到父 pom.xml（jeecg-boot-module/pom.xml）
- [x] 添加到 jeecg-system-biz 依赖
- [x] 创建完整包结构（skill/application/developer/team/lowcode/aiusage/cicd/codedoctor）
- **待验证**: `mvn compile` 编译通过

### 步骤 1.2：创建数据库表 ✅
- [x] 创建 `fe_skill` 表 SQL
- [x] 创建 `fe_mcp_skill_rel` 关联表 SQL
- [x] 5条官方Skill种子数据
- **待执行**: 在 MySQL 中执行 `feplatform_init.sql`
- **文件**: `server/jeecg-boot-module/jeecg-boot-module-feplatform/src/main/resources/feplatform_init.sql`

### 步骤 1.3：后端 Skill CRUD ✅
- [x] Entity: `FeSkill.java`（含JSON字段TypeHandler）
- [x] Entity: `FeMcpSkillRel.java`
- [x] Mapper: `FeSkillMapper.java` + XML
- [x] Mapper: `FeMcpSkillRelMapper.java`
- [x] Service: `IFeSkillService.java` + `FeSkillServiceImpl.java`
- [x] Service: `IFeMcpSkillRelService.java` + `FeMcpSkillRelServiceImpl.java`
- [x] Controller: `FeSkillController.java`（含增强接口）
- [x] VO: `SkillTestDTO.java` + `SkillExportVO.java`
- **已实现增强接口**:
  - `GET /feplatform/skill/listByCategory` - 按分类查询
  - `GET /feplatform/skill/exportMcpConfig` - 导出MCP配置
  - `POST /feplatform/skill/validateDependencies` - 依赖校验
  - `POST /feplatform/skill/testSkill` - Prompt测试

### 步骤 1.4：前端 Skill 资产库页面 ✅
- [x] API 层: `web/src/api/feplatform/skill.ts`
- [x] 数据定义: `Skill.data.ts`（columns/formSchema/searchFormSchema）
- [x] SkillList.vue - 列表页（卡片+列表双视图+分类Tab）
- [x] SkillCard.vue - 卡片组件
- [x] SkillEditModal.vue - 编辑弹窗（含Prompt编辑器+测试Tab）
- [x] SkillTestPanel.vue - Prompt测试面板
- [x] SkillExportModal.vue - MCP配置导出弹窗
- **待验证**: 连接后端后的功能验证

### 步骤 1.5：MCP 配置导出与连接验证 ✅
- [x] 后端 `exportMcpConfig` 接口实现
- [x] `FeMcpSkillRel` 实体和服务（绑定/解绑Skill）
- [x] 前端导出弹窗（选择Skill → 生成JSON → 复制/下载）
- **待完成**: 与已有 `AiragMcpController` 的深度集成
- **待验证**: Claude Code 端到端测试

### 步骤 1.6：Chat2Code 基础对话界面 ✅
- [x] 后端: `Chat2CodeController.java` + `Chat2CodeDTO.java`
- [x] 前端API: `web/src/api/feplatform/lowcode.ts`
- [x] Chat2Code.vue - 三栏布局（Skill选择器+对话区+代码预览）
- **待完成**: 接入真实AI对话引擎（当前为占位接口）
- **待增强**: 代码语法高亮、沙箱预览（阶段二）

---

## 阶段二：业务管理 + Skill 增强 + 低代码服务 - 🔲 待开发

### 步骤 2.1：应用管理模块 ✅
- [x] 创建 `fe_application` 表（SQL已完成）
- [x] 后端 CRUD + 增强接口（bindSkills/skillList）
- [x] 前端页面（ApplicationList/Application.data.ts）
- [ ] 应用级 Skill 优先级实现（待完善）

### 步骤 2.2：人员与团队管理 ✅
- [x] 创建 `fe_team` + `fe_developer` 表（SQL已完成）
- [x] 后端 CRUD + 关联系统用户
- [x] 前端页面（TeamList/DeveloperList）

### 步骤 2.3：编写业务 Skill
- [ ] bus-code-style Prompt模板
- [ ] bus-api-standard Prompt模板
- [ ] bus-component-lib Prompt模板
- [ ] 通过管理界面录入

### 步骤 2.4：Schema转换引擎 ✅
- [x] 后端 `SchemaConverterController` + JSqlParser
- [x] 前端 `Sql2Schema.vue` 页面

### 步骤 2.5：可视化沙箱
- [ ] `SandboxPreview.vue` iframe沙箱组件
- [ ] SFC编译（@vue/compiler-sfc）
- [ ] 安全措施

### 步骤 2.6：CI/CD Pipeline 模板管理
- [ ] 创建 `fe_pipeline_template` + `fe_project_pipeline` 表
- [ ] 后端 CRUD + 增强接口
- [ ] 前端页面

---

## 阶段三：效能度量 + CI/CD 闭环 + 代码医生 - 🔲 待开发

### 步骤 3.1：AI 使用行为采集
### 步骤 3.2：效能看板页面
### 步骤 3.3：GitLab Webhook + AI MR 审查
### 步骤 3.4：在线代码医生
### 步骤 3.5：前端路由与菜单配置
### 步骤 3.6：全链路集成测试

---

## 已创建文件清单

### 后端文件
```
server/jeecg-boot-module/jeecg-boot-module-feplatform/
├── pom.xml
└── src/main/
    ├── resources/
    │   └── feplatform_init.sql
    └── java/org/jeecg/modules/feplatform/
        ├── skill/
        │   ├── controller/FeSkillController.java
        │   ├── entity/FeSkill.java
        │   ├── entity/FeMcpSkillRel.java
        │   ├── mapper/FeSkillMapper.java
        │   ├── mapper/FeMcpSkillRelMapper.java
        │   ├── mapper/xml/FeSkillMapper.xml
        │   ├── service/IFeSkillService.java
        │   ├── service/IFeMcpSkillRelService.java
        │   ├── service/impl/FeSkillServiceImpl.java
        │   ├── service/impl/FeMcpSkillRelServiceImpl.java
        │   └── vo/SkillTestDTO.java, SkillExportVO.java
        ├── lowcode/
        │   ├── controller/Chat2CodeController.java
        │   └── dto/Chat2CodeDTO.java
        ├── application/
        │   ├── controller/FeApplicationController.java
        │   ├── entity/FeApplication.java
        │   ├── mapper/FeApplicationMapper.java
        │   ├── mapper/xml/FeApplicationMapper.xml
        │   ├── service/IFeApplicationService.java
        │   └── service/impl/FeApplicationServiceImpl.java
        ├── developer/
        │   ├── controller/FeDeveloperController.java
        │   ├── entity/FeDeveloper.java
        │   ├── mapper/FeDeveloperMapper.java
        │   ├── service/IFeDeveloperService.java
        │   └── service/impl/FeDeveloperServiceImpl.java
        ├── team/
        │   ├── controller/FeTeamController.java
        │   ├── entity/FeTeam.java
        │   ├── mapper/FeTeamMapper.java
        │   ├── service/IFeTeamService.java
        │   └── service/impl/FeTeamServiceImpl.java
        ├── aiusage/      (空目录)
        ├── cicd/         (空目录)
        └── codedoctor/   (空目录)
```

### 前端文件
```
web/src/
├── api/feplatform/
│   ├── skill.ts
│   ├── lowcode.ts
│   ├── application.ts
│   ├── team.ts
│   └── developer.ts
└── views/feplatform/
    ├── skill/
    │   ├── SkillList.vue
    │   ├── Skill.data.ts
    │   └── components/
    │       ├── SkillCard.vue
    │       ├── SkillEditModal.vue
    │       ├── SkillTestPanel.vue
    │       └── SkillExportModal.vue
    ├── lowcode/
    │   └── Chat2Code.vue
    ├── application/
    │   ├── ApplicationList.vue
    │   └── Application.data.ts
    ├── team/
    │   ├── TeamList.vue
    │   └── Team.data.ts
    └── developer/
        ├── DeveloperList.vue
        └── Developer.data.ts
```

### 修改的文件
```
server/jeecg-boot-module/pom.xml (添加 feplatform 模块)
server/jeecg-module-system/jeecg-system-biz/pom.xml (添加 feplatform 依赖)
```

---

## 下次开发要点

1. **优先**: 在 MySQL 中执行 `feplatform_init.sql` 创建表和种子数据
2. **优先**: 验证 `mvn compile` 编译通过
3. **优先**: 在 JeecgBoot 菜单管理中配置前端路由
4. **步骤 2.4**: Schema转换引擎（后端JSqlParser + 前端Sql2Schema页面）
5. **步骤 2.5**: 可视化沙箱（SandboxPreview组件）
6. **Chat2Code增强**: 接入真实 AI 对话引擎，替换占位接口
