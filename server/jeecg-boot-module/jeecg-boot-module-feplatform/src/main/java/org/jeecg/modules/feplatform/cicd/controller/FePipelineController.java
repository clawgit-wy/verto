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
import org.jeecg.modules.feplatform.cicd.entity.FePipeline;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@Tag(name = "流水线管理")
@RestController
@RequestMapping("/feplatform/cicd/pipeline")
@Slf4j
public class FePipelineController extends JeecgController<FePipeline, IFePipelineService> {

    @Autowired
    private IFePipelineService fePipelineService;

    @Operation(summary = "流水线-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FePipeline>> queryPageList(FePipeline fePipeline,
                                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                     HttpServletRequest req) {
        QueryWrapper<FePipeline> queryWrapper = QueryGenerator.initQueryWrapper(fePipeline, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        Page<FePipeline> page = new Page<>(pageNo, pageSize);
        IPage<FePipeline> pageList = fePipelineService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "流水线-添加")
    @Operation(summary = "流水线-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FePipeline fePipeline) {
        fePipelineService.save(fePipeline);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "流水线-编辑")
    @Operation(summary = "流水线-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FePipeline fePipeline) {
        fePipelineService.updateById(fePipeline);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "流水线-通过id删除")
    @Operation(summary = "流水线-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        fePipelineService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "流水线-批量删除")
    @Operation(summary = "流水线-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.fePipelineService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "流水线-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FePipeline> queryById(@RequestParam(name = "id", required = true) String id) {
        FePipeline entity = fePipelineService.getById(id);
        return Result.OK(entity);
    }

    @AutoLog(value = "流水线-触发构建")
    @Operation(summary = "流水线-触发构建")
    @PostMapping(value = "/triggerBuild")
    public Result<String> triggerBuild(@RequestParam(name = "pipelineId", required = true) String pipelineId,
                                        @RequestBody(required = false) Map<String, String> parameters) {
        String buildId = fePipelineService.triggerBuild(pipelineId, parameters);
        return Result.OK("构建已触发", buildId);
    }

    @AutoLog(value = "流水线-中止构建")
    @Operation(summary = "流水线-中止构建")
    @PostMapping(value = "/abortBuild")
    public Result<String> abortBuild(@RequestParam(name = "pipelineId", required = true) String pipelineId,
                                      @RequestParam(name = "buildNo", required = true) Integer buildNo) {
        fePipelineService.abortBuild(pipelineId, buildNo);
        return Result.OK("中止请求已发送");
    }

    @AutoLog(value = "流水线-同步Jenkins构建历史")
    @Operation(summary = "流水线-同步Jenkins构建历史")
    @PostMapping(value = "/syncBuilds")
    public Result<Integer> syncBuilds(@RequestParam(name = "pipelineId", required = true) String pipelineId) {
        int count = fePipelineService.syncBuilds(pipelineId);
        return Result.OK("同步完成", count);
    }
}
