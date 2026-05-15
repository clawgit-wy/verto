package org.jeecg.modules.feplatform.team.controller;

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
import org.jeecg.modules.feplatform.team.entity.FeTeam;
import org.jeecg.modules.feplatform.team.service.IFeTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

/**
 * @Description: 前端团队表
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Tag(name = "前端团队管理")
@RestController
@RequestMapping("/feplatform/team")
@Slf4j
public class FeTeamController extends JeecgController<FeTeam, IFeTeamService> {

    @Autowired
    private IFeTeamService feTeamService;

    @Operation(summary = "团队-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeTeam>> queryPageList(FeTeam feTeam,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FeTeam> queryWrapper = QueryGenerator.initQueryWrapper(feTeam, req.getParameterMap());
        Page<FeTeam> page = new Page<>(pageNo, pageSize);
        IPage<FeTeam> pageList = feTeamService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "团队-添加")
    @Operation(summary = "团队-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeTeam feTeam) {
        feTeam.setDelFlag(CommonConstant.DEL_FLAG_0);
        feTeamService.save(feTeam);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "团队-编辑")
    @Operation(summary = "团队-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeTeam feTeam) {
        feTeamService.updateById(feTeam);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "团队-通过id删除")
    @Operation(summary = "团队-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feTeamService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "团队-批量删除")
    @Operation(summary = "团队-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feTeamService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "团队-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeTeam> queryById(@RequestParam(name = "id", required = true) String id) {
        FeTeam feTeam = feTeamService.getById(id);
        if (feTeam == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(feTeam);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeTeam feTeam) {
        return super.exportXls(request, feTeam, FeTeam.class, "前端团队管理");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeTeam.class);
    }
}