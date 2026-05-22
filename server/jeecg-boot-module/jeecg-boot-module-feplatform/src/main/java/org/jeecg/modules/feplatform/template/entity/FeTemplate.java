package org.jeecg.modules.feplatform.template.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

//update-begin---author:feplatform ---date:2026-05-22  for:【模版中心】模版主表实体---
@Data
@TableName(value = "fe_template", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "模版主表")
public class FeTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Excel(name = "模版名称", width = 15)
    @Schema(description = "模版名称")
    private String name;

    @Excel(name = "模版编码", width = 15)
    @Schema(description = "模版编码(唯一标识)")
    private String code;

    @Excel(name = "GitLab仓库地址", width = 30)
    @Schema(description = "GitLab仓库地址")
    private String gitlabUrl;

    @Excel(name = "默认分支", width = 15)
    @Schema(description = "默认分支")
    private String branch;

    @Excel(name = "状态", width = 15)
    @Dict(dicCode = "fe_template_status")
    @Schema(description = "状态: enable=上架, disable=下架")
    private String status;

    @Schema(description = "排序号(置顶用)")
    private java.math.BigDecimal sortNo;

    @Excel(name = "可见性", width = 15)
    @Dict(dicCode = "fe_template_visibility")
    @Schema(description = "可见性: public=公开, private=私有")
    private String visibility;

    @Schema(description = "模版描述")
    private String description;

    @Schema(description = "关联技术栈ID")
    private String techStackId;

    @Excel(name = "框架名称", width = 15)
    @Dict(dicCode = "fe_framework")
    @Schema(description = "框架名称(Vue2/Vue3/React/Angular/jQuery)")
    private String framework;

    @Excel(name = "框架版本", width = 15)
    @Schema(description = "框架版本")
    private String frameworkVersion;

    @Schema(description = "推荐Node版本范围")
    private String nodeVersionRange;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "占位变量声明(JSON)")
    private List<Map<String, Object>> placeholderVars;

    @Schema(description = "模版预览图")
    private String previewImage;

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableLogic
    @Schema(description = "删除标记")
    private String delFlag;

    @Schema(description = "所属部门")
    private String sysOrgCode;
}
//update-end---author:feplatform ---date:2026-05-22  for:【模版中心】模版主表实体---
