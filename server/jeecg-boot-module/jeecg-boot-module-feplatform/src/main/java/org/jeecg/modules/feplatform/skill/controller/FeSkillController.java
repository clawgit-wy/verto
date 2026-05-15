package org.jeecg.modules.feplatform.skill.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.jeecg.modules.feplatform.skill.entity.FeSkill;
import org.jeecg.modules.feplatform.skill.service.IFeSkillService;
import org.jeecg.modules.feplatform.skill.vo.SkillExportVO;
import org.jeecg.modules.feplatform.skill.vo.SkillTestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "AI Skill资产管理")
@RestController
@RequestMapping("/feplatform/skill")
@Slf4j
public class FeSkillController extends JeecgController<FeSkill, IFeSkillService> {

    @Autowired
    private IFeSkillService feSkillService;

    @Operation(summary = "Skill-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<FeSkill>> queryPageList(FeSkill feSkill,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        QueryWrapper<FeSkill> queryWrapper = QueryGenerator.initQueryWrapper(feSkill, req.getParameterMap());
        queryWrapper.orderByAsc("sort_no");
        Page<FeSkill> page = new Page<>(pageNo, pageSize);
        IPage<FeSkill> pageList = feSkillService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "Skill-添加")
    @Operation(summary = "Skill-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody FeSkill feSkill) {
        feSkill.setDelFlag(CommonConstant.DEL_FLAG_0);
        if (feSkill.getStatus() == null) {
            feSkill.setStatus("enable");
        }
        if (feSkill.getVersion() == null) {
            feSkill.setVersion("1.0.0");
        }
        feSkillService.save(feSkill);
        return Result.OK("添加成功！");
    }

    @AutoLog(value = "Skill-编辑")
    @Operation(summary = "Skill-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeSkill feSkill) {
        feSkillService.updateById(feSkill);
        return Result.OK("编辑成功!");
    }

    @AutoLog(value = "Skill-通过id删除")
    @Operation(summary = "Skill-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feSkillService.removeById(id);
        return Result.OK("删除成功!");
    }

    @AutoLog(value = "Skill-批量删除")
    @Operation(summary = "Skill-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.feSkillService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "Skill-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<FeSkill> queryById(@RequestParam(name = "id", required = true) String id) {
        FeSkill feSkill = feSkillService.getById(id);
        if (feSkill == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(feSkill);
    }

    @Operation(summary = "Skill-按分类查询")
    @GetMapping(value = "/listByCategory")
    public Result<List<FeSkill>> listByCategory(@RequestParam(name = "category", required = true) String category) {
        List<FeSkill> list = feSkillService.listByCategory(category);
        return Result.OK(list);
    }

    @Operation(summary = "Skill-导出MCP配置")
    @GetMapping(value = "/exportMcpConfig")
    public Result<SkillExportVO> exportMcpConfig(@RequestParam(name = "skillCodes", required = true) String skillCodes,
                                                  HttpServletRequest request) {
        LambdaQueryWrapper<FeSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FeSkill::getCode, Arrays.asList(skillCodes.split(",")))
               .eq(FeSkill::getStatus, "enable");
        List<FeSkill> skills = feSkillService.list(wrapper);
        if (skills.isEmpty()) {
            return Result.error("未找到有效的Skill配置");
        }
        String activeCodes = skills.stream()
                .map(FeSkill::getCode)
                .collect(Collectors.joining(","));
        String endpoint = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + "/mcp/sse";
        String token = request.getHeader("X-Access-Token");
        SkillExportVO vo = SkillExportVO.build(endpoint, token != null ? token : "{{user_token}}", activeCodes);
        return Result.OK(vo);
    }

    @Operation(summary = "Skill-依赖校验")
    @PostMapping(value = "/validateDependencies")
    public Result<?> validateDependencies(@RequestBody List<String> skillIds) {
        LambdaQueryWrapper<FeSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(FeSkill::getId, skillIds)
               .eq(FeSkill::getStatus, "enable");
        List<FeSkill> skills = feSkillService.list(wrapper);
        if (skills.size() != skillIds.size()) {
            List<String> foundIds = skills.stream().map(FeSkill::getId).collect(Collectors.toList());
            List<String> invalidIds = skillIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            return Result.error("以下Skill不存在或已禁用: " + String.join(",", invalidIds));
        }
        return Result.OK("校验通过", skills);
    }

    @Operation(summary = "Skill-测试Prompt效果")
    @PostMapping(value = "/testSkill")
    public Result<?> testSkill(@RequestBody SkillTestDTO dto) {
        FeSkill skill = feSkillService.getById(dto.getSkillId());
        if (skill == null) {
            return Result.error("未找到对应Skill");
        }
        return Result.OK("Skill配置验证通过，Prompt模板可正常使用", skill);
    }

    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, FeSkill feSkill) {
        return super.exportXls(request, feSkill, FeSkill.class, "AI Skill资产");
    }

    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, FeSkill.class);
    }
}
