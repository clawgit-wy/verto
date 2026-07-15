package org.jeecg.modules.formengine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.formengine.entity.FeProcessSchemaField;

import java.util.List;

/**
 * 流程级表单Schema字段定义 Service接口
 */
public interface IFeProcessSchemaFieldService extends IService<FeProcessSchemaField> {

    /**
     * 根据流程ID查询Schema字段列表（按sortNo排序）
     * @param processId 流程ID
     * @return 字段列表
     */
    List<FeProcessSchemaField> listByProcessId(String processId);

    /**
     * 批量保存流程Schema字段（先删除旧的，再批量插入）
     * @param processId 流程ID
     * @param fields 字段列表
     */
    void saveBatchByProcessId(String processId, List<FeProcessSchemaField> fields);

    /**
     * 删除流程的所有Schema字段
     * @param processId 流程ID
     */
    void deleteByProcessId(String processId);
}
