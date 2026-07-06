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
import java.math.BigDecimal;
import java.util.Date;

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】构建记录实体---
@Data
@TableName("fe_pipeline_build")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "构建记录")
public class FePipelineBuild implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "流水线ID")
    private String pipelineId;

    @Excel(name = "构建号", width = 15)
    @Schema(description = "构建编号")
    private Integer buildNo;

    @Excel(name = "状态", width = 15)
    @Dict(dicCode = "fe_build_status")
    @Schema(description = "状态: success/failed/running/aborted")
    private String status;

    @Schema(description = "耗时(毫秒)")
    private Long duration;

    @Schema(description = "触发用户")
    private String triggerUser;

    @Schema(description = "提交SHA")
  private String commitSha;

  @Schema(description = "技术栈")
    private String techStack;

    @Schema(description = "制品版本号")
    private String artifactVersion;

    @Schema(description = "标准化检查得分")
    private BigDecimal qualityScore;

    @Schema(description = "豁免的检查项(JSON)")
    private String checkLevelExemptions;

    @Schema(description = "构建时使用的模板版本")
    private String templateVersion;

    @Schema(description = "框架信息(JSON)")
    private String frameworkInfo;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间")
    private Date finishTime;

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
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】构建记录实体---
