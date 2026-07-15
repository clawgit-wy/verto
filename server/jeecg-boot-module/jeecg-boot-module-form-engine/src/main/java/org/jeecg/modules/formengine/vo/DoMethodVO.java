package org.jeecg.modules.formengine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】方法执行VO---
@Data
@Schema(description = "方法执行VO")
public class DoMethodVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "方法Key")
    private String methodKey;

    @Schema(description = "方法参数")
    private Map<String, Object> params;
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】方法执行VO---
