package org.jeecg.modules.feplatform.template.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.feplatform.template.entity.FeTemplate;
import org.jeecg.modules.feplatform.template.service.IFeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

//update-begin---author:feplatform ---date:2026-05-22  for:【模版中心】模版主表Controller---
@Tag(name = "模版管理")
@RestController
@RequestMapping("/feplatform/template")
@Slf4j
public class FeTemplateController extends JeecgController<FeTemplate, IFeTemplateService> {

    @Autowired
    private IFeTemplateService feTemplateService;

    @Operation(summary = "模版-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeTemplate>> queryPageList(FeTemplate feTemplate,
                                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                     HttpServletRequest req) {
        QueryWrapper<FeTemplate> queryWrapper = QueryGenerator.initQueryWrapper(feTemplate, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        Page<FeTemplate> page = new Page<>(pageNo, pageSize);
        IPage<FeTemplate> pageList = feTemplateService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "模版-添加")
    @Operation(summary = "模版-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeTemplate feTemplate) {
        feTemplateService.save(feTemplate);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "模版-编辑")
    @Operation(summary = "模版-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeTemplate feTemplate) {
        feTemplateService.updateById(feTemplate);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "模版-通过id删除")
    @Operation(summary = "模版-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feTemplateService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "模版-批量删除")
    @Operation(summary = "模版-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feTemplateService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "模版-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeTemplate> queryById(@RequestParam(name = "id", required = true) String id) {
        FeTemplate feTemplate = feTemplateService.getById(id);
        return Result.OK(feTemplate);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeTemplate feTemplate) {
        return super.exportXls(request, feTemplate, FeTemplate.class, "模版管理");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeTemplate.class);
    }
}
//update-end---author:feplatform ---date:2026-05-22  for:【模版中心】模版主表Controller---
