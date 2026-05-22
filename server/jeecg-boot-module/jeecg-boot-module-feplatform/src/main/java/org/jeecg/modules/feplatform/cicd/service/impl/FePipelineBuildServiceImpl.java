package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.feplatform.cicd.client.JenkinsClient;
import org.jeecg.modules.feplatform.cicd.entity.FeJenkinsInstance;
import org.jeecg.modules.feplatform.cicd.entity.FePipeline;
import org.jeecg.modules.feplatform.cicd.entity.FePipelineBuild;
import org.jeecg.modules.feplatform.cicd.mapper.FePipelineBuildMapper;
import org.jeecg.modules.feplatform.cicd.service.IFeJenkinsInstanceService;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineBuildService;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FePipelineBuildServiceImpl extends ServiceImpl<FePipelineBuildMapper, FePipelineBuild>
        implements IFePipelineBuildService {

    @Autowired
    private JenkinsClient jenkinsClient;

    @Autowired
    @Lazy
    private IFePipelineService pipelineService;

    @Autowired
    private IFeJenkinsInstanceService jenkinsInstanceService;

    @Override
    public String fetchConsoleLog(String buildId, long start) {
        FePipelineBuild build = getById(buildId);
        if (build == null) {
            throw new JeecgBootException("构建记录不存在");
        }
        FePipeline pipeline = pipelineService.getById(build.getPipelineId());
        if (pipeline == null) {
            throw new JeecgBootException("流水线不存在");
        }
        FeJenkinsInstance instance = jenkinsInstanceService.getById(pipeline.getJenkinsId());
        if (instance == null) {
            throw new JeecgBootException("Jenkins实例不存在");
        }
        Integer no = build.getBuildNo() == null ? 0 : build.getBuildNo();
        return jenkinsClient.getConsoleLog(instance, pipeline.getJobName(), no, start);
    }
}
