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
import java.util.Map;

//update-begin---author:feplatform ---date:2026-05-22  for:【模版中心】应用创建记录表实体---
@Data
@TableName(value = "fe_app_create_record", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "应用创建记录表")
public class FeAppCreateRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "模版ID")
    private String templateId;

    @Schema(description = "模版版本ID")
    private String versionId;

    @Schema(description = "关联应用ID")
    private String appId;

    @Excel(name = "应用名称", width = 20)
    @Schema(description = "应用名称")
    private String appName;

    @Excel(name = "应用编码", width = 20)
    @Schema(description = "应用编码")
    private String appCode;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "用户填写的配置参数")
    private Map<String, Object> params;

    @Excel(name = "输出类型", width = 15)
    @Dict(dicCode = "fe_app_output_type")
    @Schema(description = "输出类型: download=下载, gitlab=创建GitLab仓库")
    private String outputType;

    @Schema(description = "创建的GitLab仓库地址")
    private String gitlabUrl;

    @Excel(name = "状态", width = 15)
    @Dict(dicCode = "fe_app_create_status")
    @Schema(description = "状态: generating=生成中, success=成功, failed=失败")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建人")
    private String creator;

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
//update-end---author:feplatform ---date:2026-05-22  for:【模版中心】应用创建记录表实体---
