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
import org.jeecg.modules.feplatform.cicd.entity.FeTechStack;
import org.jeecg.modules.feplatform.cicd.service.IFeTechStackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】技术栈Controller---
@Tag(name = "技术栈管理")
@RestController
@RequestMapping("/feplatform/cicd/techStack")
@Slf4j
public class FeTechStackController extends JeecgController<FeTechStack, IFeTechStackService> {

    @Autowired
    private IFeTechStackService feTechStackService;

    @Operation(summary = "技术栈-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeTechStack>> queryPageList(FeTechStack feTechStack,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest req) {
        QueryWrapper<FeTechStack> queryWrapper = QueryGenerator.initQueryWrapper(feTechStack, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        Page<FeTechStack> page = new Page<>(pageNo, pageSize);
        IPage<FeTechStack> pageList = feTechStackService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "技术栈-添加")
    @Operation(summary = "技术栈-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeTechStack feTechStack) {
        if (feTechStack.getStatus() == null) {
            feTechStack.setStatus("enable");
        }
        feTechStackService.save(feTechStack);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "技术栈-编辑")
    @Operation(summary = "技术栈-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeTechStack feTechStack) {
        feTechStackService.updateById(feTechStack);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "技术栈-通过id删除")
    @Operation(summary = "技术栈-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feTechStackService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "技术栈-批量删除")
    @Operation(summary = "技术栈-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feTechStackService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "技术栈-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeTechStack> queryById(@RequestParam(name = "id", required = true) String id) {
        FeTechStack entity = feTechStackService.getById(id);
        return Result.OK(entity);
    }
}
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】技术栈Controller---
