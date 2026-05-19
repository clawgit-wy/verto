package org.jeecg.modules.lowcode.template.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.lowcode.template.entity.FeTemplate;
import org.jeecg.modules.lowcode.template.service.IFeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Tag(name = "前端代码模板")
@RestController
@RequestMapping("/lowcode/feTemplate")
@Slf4j
public class FeTemplateController extends JeecgController<FeTemplate, IFeTemplateService> {
    @Autowired
    private IFeTemplateService feTemplateService;

    @Operation(summary = "前端代码模板-分页列表查询")
    @RequiresPermissions("lowcode:fe_template:list")
    @GetMapping(value = "/list")
    public Result<IPage<FeTemplate>> queryPageList(FeTemplate feTemplate,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                    HttpServletRequest req) {
        QueryWrapper<FeTemplate> queryWrapper = QueryGenerator.initQueryWrapper(feTemplate, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        queryWrapper.orderByAsc("sort_no");
        Page<FeTemplate> page = new Page<>(pageNo, pageSize);
        IPage<FeTemplate> pageList = feTemplateService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "前端代码模板-添加")
    @RequiresPermissions("lowcode:fe_template:add")
    @PostMapping(value = "/add")
    @AutoLog(value = "添加前端代码模板")
    public Result<String> add(@RequestBody FeTemplate feTemplate) {
        feTemplateService.save(feTemplate);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "前端代码模板-编辑")
    @RequiresPermissions("lowcode:fe_template:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑前端代码模板")
    public Result<String> edit(@RequestBody FeTemplate feTemplate) {
        feTemplateService.updateById(feTemplate);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "前端代码模板-通过id删除")
    @RequiresPermissions("lowcode:fe_template:delete")
    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除前端代码模板")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        FeTemplate template = feTemplateService.getById(id);
        if (template != null) {
            template.setDelFlag("1");
            feTemplateService.updateById(template);
        }
        return Result.OK("删除成功!");
    }

    @Operation(summary = "前端代码模板-批量删除")
    @RequiresPermissions("lowcode:fe_template:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    @AutoLog(value = "批量删除前端代码模板")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        for (String id : Arrays.asList(ids.split(","))) {
            FeTemplate template = feTemplateService.getById(id);
            if (template != null) {
                template.setDelFlag("1");
                feTemplateService.updateById(template);
            }
        }
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "前端代码模板-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeTemplate> queryById(@RequestParam(name = "id", required = true) String id) {
        FeTemplate feTemplate = feTemplateService.getById(id);
        if (feTemplate == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(feTemplate);
    }

    @Operation(summary = "前端代码模板-导出excel")
    @RequiresPermissions("lowcode:fe_template:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeTemplate feTemplate) {
        return super.exportXls(request, feTemplate, FeTemplate.class, "前端代码模板");
    }

    @Operation(summary = "前端代码模板-导入excel")
    @RequiresPermissions("lowcode:fe_template:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeTemplate.class);
    }
}
