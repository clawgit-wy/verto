package org.jeecg.modules.feplatform.template.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

//update-begin---author:feplatform ---date:2026-05-22  for:【模版中心】模版版本表实体---
@Data
@TableName(value = "fe_template_version")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "模版版本表")
public class FeTemplateVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "模版ID")
    private String templateId;

    @Excel(name = "版本号", width = 15)
    @Schema(description = "版本号")
    private String version;

    @Excel(name = "Git标签", width = 20)
    @Schema(description = "Git标签")
    private String gitTag;

    @Schema(description = "Commit SHA")
    private String commitSha;

    @Schema(description = "版本对应技术栈ID")
    private String techStackId;

    @Schema(description = "框架名称")
    private String framework;

    @Schema(description = "框架版本")
    private String frameworkVersion;

    @Schema(description = "版本变更说明")
    private String changelog;

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
}
//update-end---author:feplatform ---date:2026-05-22  for:【模版中心】模版版本表实体---
