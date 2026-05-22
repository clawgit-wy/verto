package org.jeecg.modules.feplatform.cicd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.feplatform.cicd.entity.FeNodeVersion;
import org.jeecg.modules.feplatform.cicd.service.IFeNodeVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】Node版本Controller---
@Tag(name = "Node版本管理")
@RestController
@RequestMapping("/feplatform/cicd/nodeVersion")
@Slf4j
public class FeNodeVersionController extends JeecgController<FeNodeVersion, IFeNodeVersionService> {

    @Autowired
    private IFeNodeVersionService feNodeVersionService;

    @Operation(summary = "Node版本-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeNodeVersion>> queryPageList(FeNodeVersion feNodeVersion,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                        HttpServletRequest req) {
        QueryWrapper<FeNodeVersion> queryWrapper = QueryGenerator.initQueryWrapper(feNodeVersion, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        Page<FeNodeVersion> page = new Page<>(pageNo, pageSize);
        IPage<FeNodeVersion> pageList = feNodeVersionService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "Node版本-添加")
    @Operation(summary = "Node版本-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeNodeVersion feNodeVersion) {
        if (feNodeVersion.getStatus() == null) {
            feNodeVersion.setStatus("enable");
        }
        if (feNodeVersion.getIsStandard() == null) {
            feNodeVersion.setIsStandard(0);
        }
        feNodeVersionService.save(feNodeVersion);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "Node版本-编辑")
    @Operation(summary = "Node版本-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeNodeVersion feNodeVersion) {
        feNodeVersionService.updateById(feNodeVersion);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "Node版本-通过id删除")
    @Operation(summary = "Node版本-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feNodeVersionService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "Node版本-批量删除")
    @Operation(summary = "Node版本-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feNodeVersionService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "Node版本-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeNodeVersion> queryById(@RequestParam(name = "id", required = true) String id) {
        FeNodeVersion entity = feNodeVersionService.getById(id);
        return Result.OK(entity);
    }
}
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】Node版本Controller---
