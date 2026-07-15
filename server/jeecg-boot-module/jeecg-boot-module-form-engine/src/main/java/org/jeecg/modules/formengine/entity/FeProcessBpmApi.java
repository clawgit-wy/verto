package org.jeecg.modules.formengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程BPM对接接口配置实体
 * 每个流程独立拥有7个BPM对接接口（对应docs/流程配置.txt规范）
 */
@Data
@TableName(value = "fe_process_bpm_api")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "流程BPM对接接口配置")
public class FeProcessBpmApi implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "流程ID（关联 fe_process.id）")
    private String processId;

    @Schema(description = "接口标识: formFields/roles/eventListener/formValues/templateEvent/callback/templates")
    private String apiKey;

    @Schema(description = "接口名称")
    private String apiName;

    @Schema(description = "接口URL（业务系统提供的访问地址）")
    private String apiUrl;

    @Schema(description = "HTTP方法: GET/POST")
    private String apiMethod;

    @Schema(description = "接口说明")
    private String apiDescription;

    @Schema(description = "同步状态: unsynced=未同步, synced=已同步, failed=同步失败")
    private String syncStatus;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近同步时间")
    private Date syncTime;

    @Schema(description = "同步结果信息")
    private String syncResult;

    @Schema(description = "排序号")
    private Integer sortNo;

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
}
