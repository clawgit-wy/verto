package org.jeecg.modules.feplatform.cicd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.cicd.entity.FePipelineBuild;

public interface IFePipelineBuildService extends IService<FePipelineBuild> {

    /**
     * 拉取 Jenkins 构建实时日志。
     */
    String fetchConsoleLog(String buildId, long start);
}
