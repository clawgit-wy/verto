package org.jeecg.modules.feplatform.lowcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Schema转换请求DTO")
public class SchemaConvertDTO {

    @Schema(description = "SQL DDL语句")
    private String sql;

    @Schema(description = "目标框架类型: jeecg, uniapp, vue3")
    private String frameworkType;
}

@Data
@Schema(description = "表字段信息")
class ColumnInfo {
    
    @Schema(description = "字段名")
    private String name;
    
    @Schema(description = "字段类型")
    private String type;
    
    @Schema(description = "字段注释")
    private String comment;
    
    @Schema(description = "是否主键")
    private boolean primaryKey;
    
    @Schema(description = "是否自增")
    private boolean autoIncrement;
    
    @Schema(description = "是否可空")
    private boolean nullable;
    
    @Schema(description = "默认值")
    private String defaultValue;
    
    @Schema(description = "字段长度")
    private Integer length;
}

@Data
@Schema(description = "表Schema信息")
class TableSchema {
    
    @Schema(description = "表名")
    private String tableName;
    
    @Schema(description = "表注释")
    private String tableComment;
    
    @Schema(description = "字段列表")
    private List<ColumnInfo> columns;
}