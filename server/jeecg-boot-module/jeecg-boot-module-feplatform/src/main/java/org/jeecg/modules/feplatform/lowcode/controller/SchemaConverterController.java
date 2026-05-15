package org.jeecg.modules.feplatform.lowcode.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Description: Schema转换引擎
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Tag(name = "Schema转换引擎")
@RestController
@RequestMapping("/feplatform/lowcode/schema")
@Slf4j
public class SchemaConverterController {

    @AutoLog(value = "SQL转Schema")
    @Operation(summary = "SQL DDL转换为JSON Schema")
    @PostMapping(value = "/convert")
    public Result<Map<String, Object>> convertSqlToSchema(@RequestBody Map<String, String> params) {
        String sql = params.get("sql");
        String frameworkType = params.getOrDefault("frameworkType", "jeecg");
        
        try {
            Map<String, Object> result = parseSqlToSchema(sql);
            result.put("frameworkType", frameworkType);
            return Result.OK(result);
        } catch (JSQLParserException e) {
            log.error("SQL解析失败", e);
            return Result.error("SQL解析失败: " + e.getMessage());
        }
    }

    @AutoLog(value = "Schema转前端表单")
    @Operation(summary = "JSON Schema转换为前端表单配置")
    @PostMapping(value = "/toForm")
    public Result<List<Map<String, Object>>> convertSchemaToForm(@RequestBody Map<String, Object> schema) {
        List<Map<String, Object>> formFields = convertToFormConfig(schema);
        return Result.OK(formFields);
    }

    @AutoLog(value = "Schema转表格列")
    @Operation(summary = "JSON Schema转换为表格列配置")
    @PostMapping(value = "/toColumns")
    public Result<List<Map<String, Object>>> convertSchemaToColumns(@RequestBody Map<String, Object> schema) {
        List<Map<String, Object>> columns = convertToColumnConfig(schema);
        return Result.OK(columns);
    }

    @AutoLog(value = "生成代码")
    @Operation(summary = "根据Schema生成代码")
    @PostMapping(value = "/generate")
    public Result<Map<String, String>> generateCode(@RequestBody Map<String, Object> params) {
        String sql = params.get("sql").toString();
        String frameworkType = params.getOrDefault("frameworkType", "jeecg").toString();
        
        try {
            Map<String, Object> schema = parseSqlToSchema(sql);
            Map<String, String> code = generateCodeByFramework(schema, frameworkType);
            return Result.OK(code);
        } catch (JSQLParserException e) {
            log.error("SQL解析失败", e);
            return Result.error("SQL解析失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseSqlToSchema(String sql) throws JSQLParserException {
        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
        
        String tableName = createTable.getTable().getName();
        String tableComment = extractTableComment(createTable.toString());
        
        List<Map<String, Object>> columns = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        for (ColumnDefinition colDef : createTable.getColumnDefinitions()) {
            Map<String, Object> column = new HashMap<>();
            String columnName = colDef.getColumnName();
            
            column.put("name", columnName);
            column.put("type", colDef.getColDataType().getDataType());
            column.put("comment", extractColumnComment(colDef));
            column.put("nullable", !isNotNull(colDef));
            column.put("primaryKey", isPrimaryKey(colDef, createTable, columnName));
            column.put("autoIncrement", isAutoIncrement(colDef));
            column.put("defaultValue", extractDefaultValue(colDef));
            
            if ((Boolean) column.get("primaryKey")) {
                primaryKeys.add(columnName);
            }
            
            columns.add(column);
        }
        
        Map<String, Object> schema = new HashMap<>();
        schema.put("tableName", tableName);
        schema.put("tableComment", tableComment);
        schema.put("columns", columns);
        schema.put("primaryKeys", primaryKeys);
        
        return schema;
    }

    private String extractTableComment(String createTableStr) {
        int start = createTableStr.indexOf("COMMENT='");
        if (start > 0) {
            int end = createTableStr.indexOf("'", start + 9);
            if (end > start) {
                return createTableStr.substring(start + 9, end);
            }
        }
        return "";
    }

    private String extractColumnComment(ColumnDefinition colDef) {
        for (Object obj : colDef.getColumnSpecs()) {
            if (obj instanceof Expression) {
                String expr = obj.toString();
                if (expr.toUpperCase().startsWith("COMMENT")) {
                    int start = expr.indexOf("'");
                    int end = expr.lastIndexOf("'");
                    if (start > 0 && end > start) {
                        return expr.substring(start + 1, end);
                    }
                }
            }
        }
        return "";
    }

    private boolean isNotNull(ColumnDefinition colDef) {
        for (Object obj : colDef.getColumnSpecs()) {
            if (obj instanceof String) {
                if ("NOT".equalsIgnoreCase((String) obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPrimaryKey(ColumnDefinition colDef, CreateTable createTable, String columnName) {
        for (Object obj : colDef.getColumnSpecs()) {
            if (obj instanceof String && "PRIMARY".equalsIgnoreCase((String) obj)) {
                return true;
            }
        }
        
        String createStr = createTable.toString();
        return createStr.contains("PRIMARY KEY") && createStr.contains("(`" + columnName + "`)");
    }

    private boolean isAutoIncrement(ColumnDefinition colDef) {
        for (Object obj : colDef.getColumnSpecs()) {
            if (obj instanceof String) {
                if ("AUTO_INCREMENT".equalsIgnoreCase((String) obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String extractDefaultValue(ColumnDefinition colDef) {
        for (Object obj : colDef.getColumnSpecs()) {
            if (obj instanceof String && "DEFAULT".equalsIgnoreCase((String) obj)) {
                int idx = colDef.getColumnSpecs().indexOf(obj);
                if (idx + 1 < colDef.getColumnSpecs().size()) {
                    return colDef.getColumnSpecs().get(idx + 1).toString();
                }
            }
        }
        return null;
    }

    private List<Map<String, Object>> convertToFormConfig(Map<String, Object> schema) {
        List<Map<String, Object>> fields = new ArrayList<>();
        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        
        for (Map<String, Object> col : columns) {
            Map<String, Object> field = new HashMap<>();
            String name = (String) col.get("name");
            String type = (String) col.get("type");
            String comment = (String) col.get("comment");
            
            field.put("label", comment.isEmpty() ? name : comment);
            field.put("field", name);
            field.put("component", mapSqlTypeToComponent(type));
            
            List<Map<String, Object>> rules = new ArrayList<>();
            if (!(Boolean) col.get("nullable")) {
                rules.add(Map.of("required", true, "message", "请输入" + (comment.isEmpty() ? name : comment)));
            }
            field.put("rules", rules);
            
            fields.add(field);
        }
        
        return fields;
    }

    private List<Map<String, Object>> convertToColumnConfig(Map<String, Object> schema) {
        List<Map<String, Object>> columns = new ArrayList<>();
        List<Map<String, Object>> schemaColumns = (List<Map<String, Object>>) schema.get("columns");
        
        for (Map<String, Object> col : schemaColumns) {
            Map<String, Object> column = new HashMap<>();
            String name = (String) col.get("name");
            String comment = (String) col.get("comment");
            
            column.put("title", comment.isEmpty() ? name : comment);
            column.put("dataIndex", name);
            column.put("align", "center");
            
            columns.add(column);
        }
        
        return columns;
    }

    private String mapSqlTypeToComponent(String sqlType) {
        if (sqlType == null) return "Input";
        
        String type = sqlType.toUpperCase();
        if (type.contains("VARCHAR") || type.contains("TEXT")) {
            return type.contains("TEXT") ? "InputTextArea" : "Input";
        }
        if (type.contains("INT") || type.contains("DECIMAL") || type.contains("FLOAT") || type.contains("DOUBLE")) {
            return "InputNumber";
        }
        if (type.contains("DATE") || type.contains("TIME")) {
            return "DatePicker";
        }
        if (type.contains("BOOLEAN")) {
            return "Switch";
        }
        return "Input";
    }

    private Map<String, String> generateCodeByFramework(Map<String, Object> schema, String frameworkType) {
        Map<String, String> code = new HashMap<>();
        String tableName = (String) schema.get("tableName");
        String entityName = toCamelCase(tableName, true);
        String entityNameLower = toCamelCase(tableName, false);
        
        switch (frameworkType.toLowerCase()) {
            case "jeecg":
                code.put("entity", generateJeecgEntity(schema, entityName));
                code.put("mapper", generateMapper(entityName));
                code.put("service", generateService(entityName));
                code.put("controller", generateController(entityName, entityNameLower));
                break;
            case "uniapp":
                code.put("model", generateUniappModel(schema, entityName));
                code.put("api", generateUniappApi(entityNameLower));
                code.put("page", generateUniappPage(schema, entityNameLower));
                break;
            case "vue3":
                code.put("api", generateVue3Api(entityNameLower));
                code.put("component", generateVue3Component(schema, entityNameLower));
                break;
            default:
                code.put("entity", generateJeecgEntity(schema, entityName));
        }
        
        return code;
    }

    private String toCamelCase(String str, boolean capitalize) {
        String[] parts = str.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            if (i == 0 && !capitalize) {
                result.append(part);
            } else {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }

    private String generateJeecgEntity(Map<String, Object> schema, String entityName) {
        String tableName = (String) schema.get("tableName");
        String tableComment = (String) schema.get("tableComment");
        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        
        StringBuilder code = new StringBuilder();
        code.append("package org.jeecg.modules.demo.entity;\n\n");
        code.append("import com.baomidou.mybatisplus.annotation.IdType;\n");
        code.append("import com.baomidou.mybatisplus.annotation.TableField;\n");
        code.append("import com.baomidou.mybatisplus.annotation.TableId;\n");
        code.append("import com.baomidou.mybatisplus.annotation.TableName;\n");
        code.append("import com.fasterxml.jackson.annotation.JsonFormat;\n");
        code.append("import io.swagger.v3.oas.annotations.media.Schema;\n");
        code.append("import lombok.Data;\n");
        code.append("import lombok.EqualsAndHashCode;\n");
        code.append("import lombok.experimental.Accessors;\n");
        code.append("import org.jeecgframework.poi.excel.annotation.Excel;\n");
        code.append("import org.springframework.format.annotation.DateTimeFormat;\n\n");
        code.append("@Data\n");
        code.append("@TableName(\"").append(tableName).append("\")\n");
        code.append("@Accessors(chain = true)\n");
        code.append("@EqualsAndHashCode(callSuper = false)\n");
        code.append("@Schema(description = \"").append(tableComment).append("\")\n");
        code.append("public class ").append(entityName).append(" implements Serializable {\n");
        code.append("    private static final long serialVersionUID = 1L;\n\n");
        
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String type = (String) col.get("type");
            String comment = (String) col.get("comment");
            boolean isPk = (Boolean) col.get("primaryKey");
            
            String javaType = mapSqlTypeToJavaType(type);
            
            code.append("    @").append(isPk ? "TableId(type = IdType.ASSIGN_ID)\n" : "TableField\n");
            code.append("    @Schema(description = \"").append(comment).append("\")\n");
            code.append("    private ").append(javaType).append(" ").append(toCamelCase(name, false)).append(";\n\n");
        }
        
        code.append("}\n");
        return code.toString();
    }

    private String mapSqlTypeToJavaType(String sqlType) {
        if (sqlType == null) return "String";
        
        String type = sqlType.toUpperCase();
        if (type.contains("INT")) return "Integer";
        if (type.contains("BIGINT")) return "Long";
        if (type.contains("DECIMAL") || type.contains("FLOAT") || type.contains("DOUBLE")) return "BigDecimal";
        if (type.contains("DATE") || type.contains("TIME")) return "Date";
        if (type.contains("BOOLEAN")) return "Boolean";
        return "String";
    }

    private String generateMapper(String entityName) {
        return "package org.jeecg.modules.demo.mapper;\n\n" +
               "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n" +
               "import org.jeecg.modules.demo.entity." + entityName + ";\n\n" +
               "public interface " + entityName + "Mapper extends BaseMapper<" + entityName + "> {\n}\n";
    }

    private String generateService(String entityName) {
        return "package org.jeecg.modules.demo.service;\n\n" +
               "import com.baomidou.mybatisplus.extension.service.IService;\n" +
               "import org.jeecg.modules.demo.entity." + entityName + ";\n\n" +
               "public interface I" + entityName + "Service extends IService<" + entityName + "> {\n}\n";
    }

    private String generateController(String entityName, String entityNameLower) {
        return "package org.jeecg.modules.demo.controller;\n\n" +
               "import io.swagger.v3.oas.annotations.Operation;\n" +
               "import io.swagger.v3.oas.annotations.tags.Tag;\n" +
               "import org.jeecg.common.system.base.controller.JeecgController;\n" +
               "import org.jeecg.modules.demo.entity." + entityName + ";\n" +
               "import org.jeecg.modules.demo.service.I" + entityName + "Service;\n" +
               "import org.springframework.web.bind.annotation.RequestMapping;\n" +
               "import org.springframework.web.bind.annotation.RestController;\n\n" +
               "@Tag(name = \"\")\n" +
               "@RestController\n" +
               "@RequestMapping(\"/demo/" + entityNameLower + "\")\n" +
               "public class " + entityName + "Controller extends JeecgController<" + entityName + ", I" + entityName + "Service> {\n}\n";
    }

    private String generateUniappModel(Map<String, Object> schema, String entityName) {
        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        
        StringBuilder code = new StringBuilder();
        code.append("export interface ").append(entityName).append(" {\n");
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String type = (String) col.get("type");
            code.append("  ").append(name).append(": ").append(mapSqlTypeToTsType(type)).append(";\n");
        }
        code.append("}\n");
        return code.toString();
    }

    private String mapSqlTypeToTsType(String sqlType) {
        if (sqlType == null) return "string";
        
        String type = sqlType.toUpperCase();
        if (type.contains("INT") || type.contains("DECIMAL") || type.contains("FLOAT") || type.contains("DOUBLE")) {
            return "number";
        }
        if (type.contains("BOOLEAN")) return "boolean";
        return "string";
    }

    private String generateUniappApi(String entityNameLower) {
        return "import { request } from '@/utils/request'\n\n" +
               "export const get" + entityNameLower + "List = (params) => request({ url: '/api/" + entityNameLower + "/list', params })\n" +
               "export const get" + entityNameLower + "Detail = (id) => request({ url: '/api/" + entityNameLower + "/detail', params: { id } })\n" +
               "export const save" + entityNameLower + " = (data) => request({ url: '/api/" + entityNameLower + "/save', method: 'post', data })\n" +
               "export const delete" + entityNameLower + " = (id) => request({ url: '/api/" + entityNameLower + "/delete', method: 'delete', params: { id } })\n";
    }

    private String generateUniappPage(Map<String, Object> schema, String entityNameLower) {
        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        
        StringBuilder code = new StringBuilder();
        code.append("<template>\n");
        code.append("  <view class=\"container\">\n");
        code.append("    <view class=\"list\">\n");
        code.append("      <view v-for=\"item in list\" :key=\"item.id\" class=\"item\">\n");
        
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String comment = (String) col.get("comment");
            code.append("        <view class=\"row\">\n");
            code.append("          <text class=\"label\">").append(comment.isEmpty() ? name : comment).append(":</text>\n");
            code.append("          <text class=\"value\">{{ item.").append(name).append(" }}</text>\n");
            code.append("        </view>\n");
        }
        
        code.append("      </view>\n");
        code.append("    </view>\n");
        code.append("  </view>\n");
        code.append("</template>\n\n");
        code.append("<script setup>\n");
        code.append("import { ref, onMounted } from 'vue'\n");
        code.append("import { get").append(entityNameLower).append("List } from '@/api/" + entityNameLower + "'\n\n");
        code.append("const list = ref([])\n\n");
        code.append("onMounted(async () => {\n");
        code.append("  const res = await get").append(entityNameLower).append("List()\n");
        code.append("  list.value = res.data\n");
        code.append("})\n");
        code.append("</script>\n");
        return code.toString();
    }

    private String generateVue3Api(String entityNameLower) {
        return "import { defHttp } from '/@/utils/http/axios'\n\n" +
               "export const get" + entityNameLower + "List = (params) => defHttp.get({ url: '/api/" + entityNameLower + "/list', params })\n" +
               "export const get" + entityNameLower + "Detail = (id) => defHttp.get({ url: '/api/" + entityNameLower + "/detail', params: { id } })\n" +
               "export const save" + entityNameLower + " = (data) => defHttp.post({ url: '/api/" + entityNameLower + "/save', data })\n" +
               "export const delete" + entityNameLower + " = (id) => defHttp.delete({ url: '/api/" + entityNameLower + "/delete', params: { id } })\n";
    }

    private String generateVue3Component(Map<String, Object> schema, String entityNameLower) {
        List<Map<String, Object>> columns = (List<Map<String, Object>>) schema.get("columns");
        
        StringBuilder code = new StringBuilder();
        code.append("<template>\n");
        code.append("  <BasicTable @register=\"registerTable\">\n");
        code.append("    <template #toolbar>\n");
        code.append("      <a-button type=\"primary\" @click=\"handleAdd\">新增</a-button>\n");
        code.append("    </template>\n");
        code.append("  </BasicTable>\n");
        code.append("</template>\n\n");
        code.append("<script setup lang=\"ts\">\n");
        code.append("import { useTable } from '/@/components/Table'\n");
        code.append("import { get").append(entityNameLower).append("List } from '/@/api/" + entityNameLower + "'\n\n");
        
        code.append("const columns = [\n");
        for (Map<String, Object> col : columns) {
            String name = (String) col.get("name");
            String comment = (String) col.get("comment");
            code.append("  { title: '").append(comment.isEmpty() ? name : comment).append("', dataIndex: '").append(name).append("' },\n");
        }
        code.append("]\n\n");
        
        code.append("const { registerTable } = useTable({\n");
        code.append("  api: get").append(entityNameLower).append("List,\n");
        code.append("  columns,\n");
        code.append("})\n\n");
        code.append("const handleAdd = () => {}\n");
        code.append("</script>\n");
        return code.toString();
    }
}