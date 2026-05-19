package org.jeecg.modules.lowcode.project.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@TableName("fe_project_generation")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "前端项目生成记录")
public class FeProjectGeneration extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Excel(name = "项目名称", width = 15)
    @Schema(description = "项目名称")
    private java.lang.String projectName;

    @Excel(name = "项目编码", width = 15)
    @Schema(description = "项目编码")
    private java.lang.String projectCode;

    @Excel(name = "使用的模板ID", width = 15)
    @Schema(description = "使用的模板ID")
    private java.lang.String templateId;

    @Excel(name = "模板名称", width = 15)
    @Schema(description = "模板名称")
    private java.lang.String templateName;

    @Schema(description = "配置快照(JSON)")
    private java.lang.String configSnapshot;

    @Excel(name = "生成方式", width = 15, dicCode = "fe_generation_type")
    @Schema(description = "生成方式: gitlab=推送到GitLab, download=下载到本地")
    private java.lang.String generationType;

    @Excel(name = "GitLab项目地址", width = 30)
    @Schema(description = "GitLab项目地址")
    private java.lang.String gitlabUrl;

    @Schema(description = "下载地址")
    private java.lang.String downloadUrl;

    @Excel(name = "状态", width = 15, dicCode = "fe_generation_status")
    @Schema(description = "状态: generating=生成中, success=成功, failed=失败")
    private java.lang.String status;

    @Schema(description = "错误信息")
    private java.lang.String errorMessage;

    @Schema(description = "生成人")
    private java.lang.String generatedBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生成时间")
    private java.util.Date generatedTime;

    @Schema(description = "删除标记")
    private java.lang.String delFlag;
}
