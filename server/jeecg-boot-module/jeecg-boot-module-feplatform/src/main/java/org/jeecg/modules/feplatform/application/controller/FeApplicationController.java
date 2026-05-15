package org.jeecg.modules.feplatform.application.controller;

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
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.feplatform.application.entity.FeApplication;
import org.jeecg.modules.feplatform.application.service.IFeApplicationService;
import org.jeecg.modules.feplatform.skill.entity.FeSkill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

@Tag(name = "前端应用管理")
@RestController
@RequestMapping("/feplatform/application")
@Slf4j
public class FeApplicationController extends JeecgController<FeApplication, IFeApplicationService> {

    @Autowired
    private IFeApplicationService feApplicationService;

    @Operation(summary = "应用-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeApplication>> queryPageList(FeApplication feApplication,
                                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest req) {
        QueryWrapper<FeApplication> queryWrapper = QueryGenerator.initQueryWrapper(feApplication, req.getParameterMap());
        Page<FeApplication> page = new Page<>(pageNo, pageSize);
        IPage<FeApplication> pageList = feApplicationService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "应用-添加")
    @Operation(summary = "应用-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeApplication feApplication) {
        feApplication.setDelFlag(CommonConstant.DEL_FLAG_0);
        if (feApplication.getStatus() == null) {
            feApplication.setStatus("active");
        }
        if (feApplication.getRepoBranch() == null) {
            feApplication.setRepoBranch("main");
        }
        feApplicationService.save(feApplication);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "应用-编辑")
    @Operation(summary = "应用-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeApplication feApplication) {
        feApplicationService.updateById(feApplication);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "应用-通过id删除")
    @Operation(summary = "应用-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feApplicationService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "应用-批量删除")
    @Operation(summary = "应用-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feApplicationService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "应用-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeApplication> queryById(@RequestParam(name = "id", required = true) String id) {
        FeApplication feApplication = feApplicationService.getById(id);
        if (feApplication == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(feApplication);
    }

    @Operation(summary = "应用-获取关联Skill列表")
    @GetMapping(value = "/skillList")
    public Result<List<FeSkill>> skillList(@RequestParam(name = "appId", required = true) String appId) {
        List<FeSkill> skills = feApplicationService.getSkillList(appId);
        return Result.OK(skills);
    }

    @AutoLog(value = "应用-绑定Skill")
    @Operation(summary = "应用-绑定Skill")
    @PostMapping(value = "/bindSkills")
    public Result<String> bindSkills(@RequestParam(name = "appId", required = true) String appId,
                                      @RequestBody List<String> skillIds) {
        feApplicationService.bindSkills(appId, skillIds);
        return Result.OK("绑定成功!");
    }

    @AutoLog(value = "应用-解绑Skill")
    @Operation(summary = "应用-解绑Skill")
    @PostMapping(value = "/unbindSkills")
    public Result<String> unbindSkills(@RequestParam(name = "appId", required = true) String appId,
                                        @RequestBody List<String> skillIds) {
        feApplicationService.unbindSkills(appId, skillIds);
        return Result.OK("解绑成功!");
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeApplication feApplication) {
        return super.exportXls(request, feApplication, FeApplication.class, "前端应用管理");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeApplication.class);
    }
}
