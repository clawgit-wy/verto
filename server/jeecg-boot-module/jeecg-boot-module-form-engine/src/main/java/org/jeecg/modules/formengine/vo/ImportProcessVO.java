package org.jeecg.modules.formengine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】导入流程定义VO---
@Data
@Schema(description = "导入流程定义VO")
public class ImportProcessVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "流程ID")
    private String processId;

    @Schema(description = "流程定义JSON")
    private String processDefJson;
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】导入流程定义VO---
