package org.jeecg.modules.lowcode.template.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
@TableName("fe_template")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "前端代码模板")
public class FeTemplate extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Excel(name = "模板名称", width = 15)
    @Schema(description = "模板名称")
    private java.lang.String templateName;

    @Excel(name = "模板编码", width = 15)
    @Schema(description = "模板编码(唯一标识)")
    private java.lang.String templateCode;

    @Excel(name = "模板类型", width = 15, dicCode = "fe_template_type")
    @Schema(description = "模板类型: mobile=移动端, pc=PC端, h5=H5")
    private java.lang.String templateType;

    @Excel(name = "模板描述", width = 30)
    @Schema(description = "模板描述")
    private java.lang.String description;

    @Excel(name = "前端框架", width = 15)
    @Schema(description = "前端框架: Vue3, React, Angular")
    private java.lang.String framework;

    @Excel(name = "构建工具", width = 15)
    @Schema(description = "构建工具: Vite, Webpack")
    private java.lang.String buildTool;

    @Excel(name = "UI组件库", width = 15)
    @Schema(description = "UI组件库: AntDesignVue, ElementPlus, Vant")
    private java.lang.String uiLibrary;

    @Excel(name = "开发语言", width = 15)
    @Schema(description = "开发语言: TypeScript, JavaScript")
    private java.lang.String language;

    @Schema(description = "包含的模块配置(JSON)")
    private java.lang.String modules;

    @Schema(description = "完整技术栈配置(JSON)")
    private java.lang.String techStack;

    @Schema(description = "GitLab配置(JSON)")
    private java.lang.String gitlabConfig;

    @Schema(description = "CI/CD配置(JSON)")
    private java.lang.String ciCdConfig;

    @Schema(description = "模板预览图")
    private java.lang.String previewImage;

    @Excel(name = "是否默认模板", width = 15, dicCode = "yn")
    @Schema(description = "是否默认模板: 1=是, 0=否")
    private java.lang.String isDefault;

    @Excel(name = "状态", width = 15, dicCode = "fe_template_status")
    @Schema(description = "状态: enable/disable")
    private java.lang.String status;

    @Schema(description = "排序号")
    private java.math.BigDecimal sortNo;

    @Schema(description = "删除标记")
    private java.lang.String delFlag;

    @Schema(description = "所属部门")
    private java.lang.String sysOrgCode;
}
