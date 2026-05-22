package org.jeecg.modules.feplatform.developer.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.feplatform.developer.entity.FeDeveloper;
import org.jeecg.modules.feplatform.developer.service.IFeDeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 前端开发人员表
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Tag(name = "前端开发人员管理")
@RestController
@RequestMapping("/feplatform/developer")
@Slf4j
public class FeDeveloperController extends JeecgController<FeDeveloper, IFeDeveloperService> {

    @Autowired
    private IFeDeveloperService feDeveloperService;

    @Operation(summary = "开发人员-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeDeveloper>> queryPageList(FeDeveloper feDeveloper,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FeDeveloper> queryWrapper = QueryGenerator.initQueryWrapper(feDeveloper, req.getParameterMap());
        Page<FeDeveloper> page = new Page<>(pageNo, pageSize);
        IPage<FeDeveloper> pageList = feDeveloperService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "开发人员-添加")
    @Operation(summary = "开发人员-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeDeveloper feDeveloper) {
        feDeveloper.setDelFlag(CommonConstant.DEL_FLAG_0);
        if (feDeveloper.getStatus() == null) {
            feDeveloper.setStatus("active");
        }
        if (feDeveloper.getRole() == null) {
            feDeveloper.setRole("developer");
        }
        if (feDeveloper.getUserId() == null) {
            feDeveloper.setUserId("");
        }
        feDeveloperService.save(feDeveloper);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "开发人员-编辑")
    @Operation(summary = "开发人员-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeDeveloper feDeveloper) {
        feDeveloperService.updateById(feDeveloper);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "开发人员-通过id删除")
    @Operation(summary = "开发人员-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feDeveloperService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "开发人员-批量删除")
    @Operation(summary = "开发人员-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feDeveloperService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "开发人员-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeDeveloper> queryById(@RequestParam(name = "id", required = true) String id) {
        FeDeveloper feDeveloper = feDeveloperService.getById(id);
        if (feDeveloper == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(feDeveloper);
    }

    @Operation(summary = "开发人员-根据团队ID查询")
    @GetMapping(value = "/listByTeam")
    public Result<List<FeDeveloper>> listByTeam(@RequestParam(name = "teamId", required = true) String teamId) {
        List<FeDeveloper> developers = feDeveloperService.getByTeamId(teamId);
        return Result.OK(developers);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeDeveloper feDeveloper) {
        return super.exportXls(request, feDeveloper, FeDeveloper.class, "前端开发人员管理");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeDeveloper.class);
    }
}