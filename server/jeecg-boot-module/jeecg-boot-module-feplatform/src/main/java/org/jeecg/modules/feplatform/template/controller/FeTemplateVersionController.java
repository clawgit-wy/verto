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
import org.jeecg.modules.feplatform.template.entity.FeTemplateVersion;
import org.jeecg.modules.feplatform.template.service.IFeTemplateVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

//update-begin---author:feplatform ---date:2026-05-22  for:【模版中心】模版版本Controller---
@Tag(name = "模版版本管理")
@RestController
@RequestMapping("/feplatform/templateVersion")
@Slf4j
public class FeTemplateVersionController extends JeecgController<FeTemplateVersion, IFeTemplateVersionService> {

    @Autowired
    private IFeTemplateVersionService feTemplateVersionService;

    @Operation(summary = "模版版本-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeTemplateVersion>> queryPageList(FeTemplateVersion feTemplateVersion,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<FeTemplateVersion> queryWrapper = QueryGenerator.initQueryWrapper(feTemplateVersion, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        Page<FeTemplateVersion> page = new Page<>(pageNo, pageSize);
        IPage<FeTemplateVersion> pageList = feTemplateVersionService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "模版版本-添加")
    @Operation(summary = "模版版本-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeTemplateVersion feTemplateVersion) {
        feTemplateVersionService.save(feTemplateVersion);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "模版版本-编辑")
    @Operation(summary = "模版版本-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeTemplateVersion feTemplateVersion) {
        feTemplateVersionService.updateById(feTemplateVersion);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "模版版本-通过id删除")
    @Operation(summary = "模版版本-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feTemplateVersionService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "模版版本-批量删除")
    @Operation(summary = "模版版本-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feTemplateVersionService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "模版版本-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeTemplateVersion> queryById(@RequestParam(name = "id", required = true) String id) {
        FeTemplateVersion feTemplateVersion = feTemplateVersionService.getById(id);
        return Result.OK(feTemplateVersion);
    }
}
//update-end---author:feplatform ---date:2026-05-22  for:【模版中心】模版版本Controller---
