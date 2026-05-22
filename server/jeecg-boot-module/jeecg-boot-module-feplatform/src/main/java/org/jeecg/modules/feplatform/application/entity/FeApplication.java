package org.jeecg.modules.feplatform.application.entity;

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

@Data
@TableName(value = "fe_application", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "前端应用管理表")
public class FeApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Excel(name = "应用简称", width = 15)
    @Schema(description = "应用简称")
    private String appShortName;

    @Excel(name = "应用名称", width = 15)
    @Schema(description = "应用名称")
    private String appName;

    @Excel(name = "应用编码", width = 15)
    @Schema(description = "应用编码")
    private String appCode;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "所属领域")
    private String domain;

    @Schema(description = "应用等级")
    private String appLevel;

    @Schema(description = "创建人ID")
    @Dict(dictTable = "fe_developer", dicCode = "id", dicText = "real_name")
    private String creatorId;

    @Schema(description = "应用图标")
    private String icon;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "技术栈(JSON)")
    private Map<String, Object> techStack;

    @Schema(description = "代码仓库地址")
    private String repoUrl;

    @Schema(description = "主分支")
    private String repoBranch;

    @Schema(description = "部署地址")
    private String deployUrl;

    @Schema(description = "负责人ID")
    @Dict(dictTable = "fe_developer", dicCode = "id", dicText = "real_name")
    private String ownerId;

    @Schema(description = "所属团队ID")
    private String teamId;

    @Excel(name = "状态", width = 15)
    @Schema(description = "状态: active/archived/developing")
    private String status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "关联Skill ID列表")
    private List<String> skillIds;

    @Schema(description = "应用级Prompt模板")
    private String promptTemplate;

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
    @Schema(description = "删除标志")
    private Integer delFlag;

    @Schema(description = "所属部门")
    private String sysOrgCode;
}
