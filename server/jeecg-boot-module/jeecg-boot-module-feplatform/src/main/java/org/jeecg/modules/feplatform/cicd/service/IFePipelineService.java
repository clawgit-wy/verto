package org.jeecg.modules.feplatform.cicd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.cicd.entity.FePipeline;

import java.util.Map;

public interface IFePipelineService extends IService<FePipeline> {

    /**
     * 触发流水线构建：调用 Jenkins → 写入 fe_pipeline_build。
     *
     * @return 创建的构建记录 ID
     */
    String triggerBuild(String pipelineId, Map<String, String> parameters);

    /**
     * 中止构建：调 Jenkins stop → 更新 fe_pipeline_build 状态。
     */
    void abortBuild(String pipelineId, Integer buildNo);

    /**
     * 从 Jenkins 同步构建历史到 fe_pipeline_build。
     *
     * @return 本次同步的记录数
     */
    int syncBuilds(String pipelineId);
}
