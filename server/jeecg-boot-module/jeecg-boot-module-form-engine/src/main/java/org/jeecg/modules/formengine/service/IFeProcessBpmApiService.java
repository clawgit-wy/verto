package org.jeecg.modules.formengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.formengine.entity.FeProcessBpmApi;

import java.util.List;

/**
 * 流程BPM对接接口配置 Service接口
 */
public interface IFeProcessBpmApiService extends IService<FeProcessBpmApi> {

    /**
     * 根据流程ID查询BPM对接接口列表（按sortNo排序）
     * @param processId 流程ID
     * @return 接口列表
     */
    List<FeProcessBpmApi> listByProcessId(String processId);

    /**
     * 为流程自动生成7个BPM对接接口记录（创建流程时调用）
     * @param processId 流程ID
     * @param processCode 流程编码（用于生成接口URL）
     */
    void generateBpmApisForProcess(String processId, String processCode);

    /**
     * 更新接口的URL和同步状态
     * @param id 接口ID
     * @param apiUrl 接口URL
     * @param syncStatus 同步状态
     * @param syncResult 同步结果
     */
    void updateApiConfig(String id, String apiUrl, String syncStatus, String syncResult);

    /**
     * 批量同步接口到BPM平台
     * @param processId 流程ID
     * @return 同步结果信息
     */
    String syncToBpmPlatform(String processId);

    /**
     * 删除流程的所有BPM接口记录
     * @param processId 流程ID
     */
    void deleteByProcessId(String processId);
}
