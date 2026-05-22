package org.jeecg.modules.feplatform.cicd.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.feplatform.cicd.client.JenkinsClient;
import org.jeecg.modules.feplatform.cicd.entity.FeJenkinsInstance;
import org.jeecg.modules.feplatform.cicd.entity.FePipeline;
import org.jeecg.modules.feplatform.cicd.entity.FePipelineBuild;
import org.jeecg.modules.feplatform.cicd.mapper.FePipelineMapper;
import org.jeecg.modules.feplatform.cicd.service.IFeJenkinsInstanceService;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineBuildService;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class FePipelineServiceImpl extends ServiceImpl<FePipelineMapper, FePipeline> implements IFePipelineService {

    @Autowired
    private JenkinsClient jenkinsClient;

    @Autowired
    private IFeJenkinsInstanceService jenkinsInstanceService;

    @Autowired
    private IFePipelineBuildService pipelineBuildService;

    @Override
    public String triggerBuild(String pipelineId, Map<String, String> parameters) {
        FePipeline pipeline = getById(pipelineId);
        if (pipeline == null) {
            throw new JeecgBootException("流水线不存在: " + pipelineId);
        }
        FeJenkinsInstance instance = jenkinsInstanceService.getById(pipeline.getJenkinsId());
        if (instance == null) {
            throw new JeecgBootException("Jenkins实例不存在: " + pipeline.getJenkinsId());
        }

        String triggerUser = safeUsername();
        log.info("[CICD] triggerBuild pipelineId={} job={} by={}", pipelineId, pipeline.getJobName(), triggerUser);

        try {
            if (parameters == null || parameters.isEmpty()) {
                jenkinsClient.triggerBuild(instance, pipeline.getJobName());
            } else {
                jenkinsClient.triggerBuildWithParameters(instance, pipeline.getJobName(), parameters);
            }
        } catch (Exception ex) {
            log.error("Jenkins trigger failed", ex);
            throw new JeecgBootException("调用Jenkins触发构建失败: " + ex.getMessage());
        }

        // Jenkins 触发返回的是队列项 URL；真实 build number 需稍后从 /api/json 拉取
        // 这里先落一条 running 占位记录，buildNo 设为 0，待 sync 时回填
        FePipelineBuild build = new FePipelineBuild();
        build.setPipelineId(pipelineId);
        build.setBuildNo(nextLocalBuildNo(pipelineId));
        build.setStatus("running");
        build.setTriggerUser(triggerUser);
        build.setCreateTime(new Date());
        pipelineBuildService.save(build);

        return build.getId();
    }

    @Override
    public void abortBuild(String pipelineId, Integer buildNo) {
        FePipeline pipeline = getById(pipelineId);
        if (pipeline == null) {
            throw new JeecgBootException("流水线不存在: " + pipelineId);
        }
        FeJenkinsInstance instance = jenkinsInstanceService.getById(pipeline.getJenkinsId());
        if (instance == null) {
            throw new JeecgBootException("Jenkins实例不存在");
        }
        jenkinsClient.stopBuild(instance, pipeline.getJobName(), buildNo);

        QueryWrapper<FePipelineBuild> qw = new QueryWrapper<>();
        qw.eq("pipeline_id", pipelineId).eq("build_no", buildNo);
        FePipelineBuild build = pipelineBuildService.getOne(qw);
        if (build != null) {
            build.setStatus("aborted");
            build.setFinishTime(new Date());
            pipelineBuildService.updateById(build);
        }
    }

    @Override
    public int syncBuilds(String pipelineId) {
        FePipeline pipeline = getById(pipelineId);
        if (pipeline == null) {
            throw new JeecgBootException("流水线不存在");
        }
        FeJenkinsInstance instance = jenkinsInstanceService.getById(pipeline.getJenkinsId());
        if (instance == null) {
            throw new JeecgBootException("Jenkins实例不存在");
        }

        JSONArray builds = jenkinsClient.listBuilds(instance, pipeline.getJobName(), 20);
        int saved = 0;
        for (int i = 0; i < builds.size(); i++) {
            JSONObject b = builds.getJSONObject(i);
            Integer no = b.getInteger("number");
            if (no == null) continue;

            QueryWrapper<FePipelineBuild> qw = new QueryWrapper<>();
            qw.eq("pipeline_id", pipelineId).eq("build_no", no);
            FePipelineBuild exist = pipelineBuildService.getOne(qw);

            String result = b.getString("result");
            Boolean building = b.getBoolean("building");
            String status;
            if (Boolean.TRUE.equals(building)) {
                status = "running";
            } else if ("SUCCESS".equalsIgnoreCase(result)) {
                status = "success";
            } else if ("ABORTED".equalsIgnoreCase(result)) {
                status = "aborted";
            } else if (result != null) {
                status = "failed";
            } else {
                status = "running";
            }

            if (exist == null) {
                FePipelineBuild row = new FePipelineBuild();
                row.setPipelineId(pipelineId);
                row.setBuildNo(no);
                row.setStatus(status);
                row.setDuration(b.getLong("duration"));
                row.setCreateTime(new Date(b.getLongValue("timestamp")));
                if (!"running".equals(status)) {
                    row.setFinishTime(new Date());
                }
                pipelineBuildService.save(row);
                saved++;
            } else {
                exist.setStatus(status);
                exist.setDuration(b.getLong("duration"));
                if (!"running".equals(status) && exist.getFinishTime() == null) {
                    exist.setFinishTime(new Date());
                }
                pipelineBuildService.updateById(exist);
            }
        }
        return saved;
    }

    private int nextLocalBuildNo(String pipelineId) {
        QueryWrapper<FePipelineBuild> qw = new QueryWrapper<>();
        qw.eq("pipeline_id", pipelineId).orderByDesc("build_no").last("limit 1");
        FePipelineBuild last = pipelineBuildService.getOne(qw);
        return last == null || last.getBuildNo() == null ? 1 : last.getBuildNo() + 1;
    }

    private String safeUsername() {
        try {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            return user == null || user.getUsername() == null ? "system" : user.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
