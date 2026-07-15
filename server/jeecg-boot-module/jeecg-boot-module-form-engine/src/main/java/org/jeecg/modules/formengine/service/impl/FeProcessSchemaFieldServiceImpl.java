package org.jeecg.modules.formengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.formengine.entity.FeProcessSchemaField;
import org.jeecg.modules.formengine.mapper.FeProcessSchemaFieldMapper;
import org.jeecg.modules.formengine.service.IFeProcessSchemaFieldService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 流程级表单Schema字段定义 Service实现
 */
@Service
public class FeProcessSchemaFieldServiceImpl
        extends ServiceImpl<FeProcessSchemaFieldMapper, FeProcessSchemaField>
        implements IFeProcessSchemaFieldService {

    @Override
    public List<FeProcessSchemaField> listByProcessId(String processId) {
        return this.list(new QueryWrapper<FeProcessSchemaField>()
                .eq("process_id", processId)
                .orderByAsc("sort_no"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchByProcessId(String processId, List<FeProcessSchemaField> fields) {
        // 先删除该流程的所有旧字段
        this.remove(new QueryWrapper<FeProcessSchemaField>().eq("process_id", processId));
        // 批量插入新字段
        if (fields != null && !fields.isEmpty()) {
            Date now = new Date();
            for (int i = 0; i < fields.size(); i++) {
                FeProcessSchemaField field = fields.get(i);
                field.setProcessId(processId);
                if (field.getSortNo() == null) {
                    field.setSortNo(i + 1);
                }
                if (field.getCreateTime() == null) {
                    field.setCreateTime(now);
                }
                field.setUpdateTime(now);
            }
            this.saveBatch(fields);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProcessId(String processId) {
        this.remove(new QueryWrapper<FeProcessSchemaField>().eq("process_id", processId));
    }
}
