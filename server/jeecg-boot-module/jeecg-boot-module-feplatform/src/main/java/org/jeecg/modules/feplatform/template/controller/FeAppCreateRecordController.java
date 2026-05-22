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
import org.jeecg.modules.feplatform.template.entity.FeAppCreateRecord;
import org.jeecg.modules.feplatform.template.service.IFeAppCreateRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

//update-begin---author:feplatform ---date:2026-05-22  for:【模版中心】应用创建记录Controller---
@Tag(name = "应用创建记录")
@RestController
@RequestMapping("/feplatform/createRecord")
@Slf4j
public class FeAppCreateRecordController extends JeecgController<FeAppCreateRecord, IFeAppCreateRecordService> {

    @Autowired
    private IFeAppCreateRecordService feAppCreateRecordService;

    @Operation(summary = "创建记录-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeAppCreateRecord>> queryPageList(FeAppCreateRecord feAppCreateRecord,
                                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                            HttpServletRequest req) {
        QueryWrapper<FeAppCreateRecord> queryWrapper = QueryGenerator.initQueryWrapper(feAppCreateRecord, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        Page<FeAppCreateRecord> page = new Page<>(pageNo, pageSize);
        IPage<FeAppCreateRecord> pageList = feAppCreateRecordService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "创建记录-添加")
    @Operation(summary = "创建记录-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeAppCreateRecord feAppCreateRecord) {
        feAppCreateRecordService.save(feAppCreateRecord);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "创建记录-通过id删除")
    @Operation(summary = "创建记录-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feAppCreateRecordService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "创建记录-批量删除")
    @Operation(summary = "创建记录-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feAppCreateRecordService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "创建记录-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeAppCreateRecord> queryById(@RequestParam(name = "id", required = true) String id) {
        FeAppCreateRecord feAppCreateRecord = feAppCreateRecordService.getById(id);
        return Result.OK(feAppCreateRecord);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeAppCreateRecord feAppCreateRecord) {
        return super.exportXls(request, feAppCreateRecord, FeAppCreateRecord.class, "应用创建记录");
    }
}
//update-end---author:feplatform ---date:2026-05-22  for:【模版中心】应用创建记录Controller---
