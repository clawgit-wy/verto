package org.jeecg.modules.feplatform.cicd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】流水线实体---
@Data
@TableName("fe_pipeline")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "应用-流水线")
public class FePipeline implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "关联应用ID")
    private String appId;

    @Schema(description = "Jenkins实例ID")
    private String jenkinsId;

    @Excel(name = "Job名称", width = 30)
    @Schema(description = "Jenkins Job名称")
    private String jobName;

    @Excel(name = "环境", width = 15)
    @Dict(dicCode = "fe_pipeline_env")
    @Schema(description = "环境: dev=开发, test=测试, prod=生产")
    private String env;

    @Schema(description = "技术栈ID")
    private String techStackId;

    @Schema(description = "Node版本ID")
    private String nodeVersionId;

    @Excel(name = "检查级别", width = 15)
    @Dict(dicCode = "fe_check_level")
    @Schema(description = "检查级别: strict=严格, standard=标准, loose=宽松")
    private String checkLevel;

    @Excel(name = "部署策略", width = 20)
    @Dict(dicCode = "fe_deploy_strategy")
    @Schema(description = "部署策略: auto_deploy=自动部署, artifact_only=仅制品库, online_deploy=在线部署")
    private String deployStrategy;

    @Schema(description = "关联模板ID")
    private String templateId;

    @Schema(description = "关联模板版本ID")
    private String templateVersionId;

    @Schema(description = "Jenkinsfile内容")
    private String jenkinsfile;

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
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】流水线实体---
