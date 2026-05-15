<template>
  <div class="sql2schema-container">
    <a-row :gutter="16">
      <a-col :span="12">
        <a-card title="SQL DDL输入" :bordered="false" class="sql-input-card">
          <a-textarea
            v-model:value="sqlInput"
            :rows="15"
            placeholder="请输入SQL CREATE TABLE语句..."
            class="sql-textarea"
          />
          <div class="sql-actions">
            <a-select
              v-model:value="frameworkType"
              style="width: 180px; margin-right: 12px"
            >
              <a-select-option value="jeecg">JeecgBoot</a-select-option>
              <a-select-option value="uniapp">UniApp</a-select-option>
              <a-select-option value="vue3">Vue3</a-select-option>
            </a-select>
            <a-button type="primary" @click="handleConvert">
              <template #icon><CodeOutlined /></template>
              转换
            </a-button>
            <a-button @click="handleGenerate">
              <template #icon><FileCodeOutlined /></template>
              生成代码
            </a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="Schema输出" :bordered="false" class="schema-output-card">
          <a-tabs type="card">
            <a-tab-pane key="json" tab="JSON Schema">
              <a-textarea
                :value="jsonSchema"
                :rows="15"
                readonly
                class="result-textarea"
              />
            </a-tab-pane>
            <a-tab-pane key="form" tab="表单配置">
              <a-textarea
                :value="formConfig"
                :rows="15"
                readonly
                class="result-textarea"
              />
            </a-tab-pane>
            <a-tab-pane key="columns" tab="表格列">
              <a-textarea
                :value="columnsConfig"
                :rows="15"
                readonly
                class="result-textarea"
              />
            </a-tab-pane>
            <a-tab-pane key="code" tab="生成代码">
              <a-select
                v-model:value="codeType"
                style="width: 150px; margin-bottom: 12px"
              >
                <a-select-option value="entity">Entity</a-select-option>
                <a-select-option value="mapper">Mapper</a-select-option>
                <a-select-option value="service">Service</a-select-option>
                <a-select-option value="controller">Controller</a-select-option>
                <a-select-option value="api">API</a-select-option>
                <a-select-option value="component">Component</a-select-option>
              </a-select>
              <a-textarea
                :value="generatedCode"
                :rows="15"
                readonly
                class="result-textarea"
              />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card title="Schema预览" :bordered="false" class="preview-card">
          <div v-if="parsedSchema" class="schema-preview">
            <h4>表信息</h4>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="表名">{{ parsedSchema.tableName }}</a-descriptions-item>
              <a-descriptions-item label="表注释">{{ parsedSchema.tableComment }}</a-descriptions-item>
            </a-descriptions>
            
            <h4 style="margin-top: 16px">字段列表</h4>
            <a-table :columns="previewColumns" :data-source="parsedSchema.columns" :pagination="false">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'primaryKey'">
                  <a-tag color="gold" v-if="record.primaryKey">是</a-tag>
                  <span v-else>-</span>
                </template>
                <template v-if="column.key === 'nullable'">
                  <a-tag color="green" v-if="record.nullable">是</a-tag>
                  <a-tag color="red" v-else>否</a-tag>
                </template>
                <template v-if="column.key === 'autoIncrement'">
                  <a-tag color="blue" v-if="record.autoIncrement">是</a-tag>
                  <span v-else>-</span>
                </template>
              </template>
            </a-table>
          </div>
          <div v-else class="empty-preview">
            <a-empty description="请输入SQL并点击转换" />
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { CodeOutlined, FileCodeOutlined } from '@ant-design/icons-vue';
import { defHttp } from '/@/utils/http/axios';

const sqlInput = ref(`CREATE TABLE IF NOT EXISTS \`fe_test\` (
  \`id\` varchar(36) NOT NULL COMMENT '主键ID',
  \`name\` varchar(100) NOT NULL COMMENT '名称',
  \`code\` varchar(50) NOT NULL COMMENT '编码',
  \`status\` tinyint DEFAULT 1 COMMENT '状态',
  \`create_time\` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试表';`);

const frameworkType = ref('jeecg');
const codeType = ref('entity');

const jsonSchema = ref('');
const formConfig = ref('');
const columnsConfig = ref('');
const generatedCode = ref('');
const parsedSchema = ref<any>(null);

const previewColumns = [
  { title: '字段名', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '注释', dataIndex: 'comment', key: 'comment' },
  { title: '主键', dataIndex: 'primaryKey', key: 'primaryKey' },
  { title: '可空', dataIndex: 'nullable', key: 'nullable' },
  { title: '自增', dataIndex: 'autoIncrement', key: 'autoIncrement' },
];

const handleConvert = async () => {
  if (!sqlInput.value.trim()) {
    return;
  }
  
  try {
    const res = await defHttp.post({
      url: '/feplatform/lowcode/schema/convert',
      params: { sql: sqlInput.value, frameworkType: frameworkType.value },
    });
    
    if (res.success) {
      parsedSchema.value = res.result;
      jsonSchema.value = JSON.stringify(res.result, null, 2);
      
      const formRes = await defHttp.post({
        url: '/feplatform/lowcode/schema/toForm',
        params: res.result,
      });
      if (formRes.success) {
        formConfig.value = JSON.stringify(formRes.result, null, 2);
      }
      
      const colsRes = await defHttp.post({
        url: '/feplatform/lowcode/schema/toColumns',
        params: res.result,
      });
      if (colsRes.success) {
        columnsConfig.value = JSON.stringify(colsRes.result, null, 2);
      }
    }
  } catch (error) {
    console.error('转换失败', error);
  }
};

const handleGenerate = async () => {
  if (!sqlInput.value.trim()) {
    return;
  }
  
  try {
    const res = await defHttp.post({
      url: '/feplatform/lowcode/schema/generate',
      params: { sql: sqlInput.value, frameworkType: frameworkType.value },
    });
    
    if (res.success) {
      const codeMap = res.result;
      generatedCode.value = codeMap.entity || codeMap.api || codeMap.component || '';
      
      const typeMap: Record<string, string> = {
        entity: 'entity',
        mapper: 'mapper',
        service: 'service',
        controller: 'controller',
        api: 'api',
        component: 'component',
      };
      
      if (frameworkType.value === 'uniapp') {
        typeMap.api = 'api';
        typeMap.component = 'page';
      } else if (frameworkType.value === 'vue3') {
        typeMap.api = 'api';
        typeMap.component = 'component';
      }
      
      const key = typeMap[codeType.value];
      if (key && codeMap[key]) {
        generatedCode.value = codeMap[key];
      }
    }
  } catch (error) {
    console.error('生成代码失败', error);
  }
};
</script>

<style scoped>
.sql2schema-container {
  padding: 20px;
}

.sql-input-card,
.schema-output-card {
  height: 350px;
}

.sql-textarea,
.result-textarea {
  width: 100%;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 12px;
}

.sql-actions {
  display: flex;
  align-items: center;
  margin-top: 12px;
}

.preview-card {
  min-height: 300px;
}

.schema-preview {
  padding: 16px;
}

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}
</style>