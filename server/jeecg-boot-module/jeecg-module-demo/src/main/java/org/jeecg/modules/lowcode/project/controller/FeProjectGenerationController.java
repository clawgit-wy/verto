package org.jeecg.modules.lowcode.project.controller;

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
import org.jeecg.modules.lowcode.project.entity.FeProjectGeneration;
import org.jeecg.modules.lowcode.project.service.IFeProjectGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Tag(name = "前端项目生成记录")
@RestController
@RequestMapping("/lowcode/feProjectGeneration")
@Slf4j
public class FeProjectGenerationController extends JeecgController<FeProjectGeneration, IFeProjectGenerationService> {
    @Autowired
    private IFeProjectGenerationService feProjectGenerationService;

    @Operation(summary = "前端项目生成记录-分页列表查询")
    @RequiresPermissions("lowcode:fe_project_gen:list")
    @GetMapping(value = "/list")
    public Result<IPage<FeProjectGeneration>> queryPageList(FeProjectGeneration feProjectGeneration,
                                                             @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                             @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                             HttpServletRequest req) {
        QueryWrapper<FeProjectGeneration> queryWrapper = QueryGenerator.initQueryWrapper(feProjectGeneration, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        queryWrapper.orderByDesc("create_time");
        Page<FeProjectGeneration> page = new Page<>(pageNo, pageSize);
        IPage<FeProjectGeneration> pageList = feProjectGenerationService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "前端项目生成记录-通过id删除")
    @RequiresPermissions("lowcode:fe_project_gen:delete")
    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除前端项目生成记录")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        FeProjectGeneration record = feProjectGenerationService.getById(id);
        if (record != null) {
            record.setDelFlag("1");
            feProjectGenerationService.updateById(record);
        }
        return Result.OK("删除成功!");
    }

    @Operation(summary = "前端项目生成记录-批量删除")
    @RequiresPermissions("lowcode:fe_project_gen:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    @AutoLog(value = "批量删除前端项目生成记录")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        for (String id : Arrays.asList(ids.split(","))) {
            FeProjectGeneration record = feProjectGenerationService.getById(id);
            if (record != null) {
                record.setDelFlag("1");
                feProjectGenerationService.updateById(record);
            }
        }
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "前端项目生成记录-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeProjectGeneration> queryById(@RequestParam(name = "id", required = true) String id) {
        FeProjectGeneration record = feProjectGenerationService.getById(id);
        if (record == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(record);
    }

    @Operation(summary = "前端项目生成记录-导出excel")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeProjectGeneration feProjectGeneration) {
        return super.exportXls(request, feProjectGeneration, FeProjectGeneration.class, "前端项目生成记录");
    }

    @Operation(summary = "前端项目生成记录-导入excel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeProjectGeneration.class);
    }
}
