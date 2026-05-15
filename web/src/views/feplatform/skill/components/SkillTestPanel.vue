<template>
  <div class="skill-test-panel">
    <a-alert v-if="!skillId" message="请先保存Skill后再进行测试" type="info" show-icon class="mb-4" />
    <template v-else>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="输入参数" size="small">
            <a-textarea
              v-model:value="userMessage"
              :rows="6"
              placeholder="请输入测试消息..."
            />
            <div class="mt-2 text-right">
              <a-button type="primary" :loading="testing" @click="handleTest">
                <template #icon><PlayCircleOutlined /></template>
                执行测试
              </a-button>
            </div>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="测试结果" size="small">
            <a-spin :spinning="testing">
              <div class="test-result" v-if="testResult">
                <a-result
                  :status="testResult.success ? 'success' : 'error'"
                  :title="testResult.success ? '验证通过' : '验证失败'"
                  :sub-title="testResult.message"
                />
              </div>
              <a-empty v-else description="暂无测试结果" />
            </a-spin>
          </a-card>
        </a-col>
      </a-row>
      <a-card title="Prompt模板预览" size="small" class="mt-4">
        <pre class="prompt-preview">{{ promptTemplate }}</pre>
      </a-card>
    </template>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { PlayCircleOutlined } from '@ant-design/icons-vue';
  import { testSkill } from '/@/api/feplatform/skill';

  const props = defineProps({
    skillId: { type: String, default: '' },
    promptTemplate: { type: String, default: '' },
  });

  const userMessage = ref('');
  const testing = ref(false);
  const testResult = ref<any>(null);

  async function handleTest() {
    if (!userMessage.value) return;
    testing.value = true;
    testResult.value = null;
    try {
      const res = await testSkill({
        skillId: props.skillId,
        userMessage: userMessage.value,
      });
      testResult.value = { success: true, message: 'Skill配置验证通过，Prompt模板可正常使用' };
    } catch (e: any) {
      testResult.value = { success: false, message: '测试失败: ' + (e?.message || '未知错误') };
    } finally {
      testing.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .prompt-preview {
    max-height: 300px;
    overflow: auto;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 4px;
    font-family: 'Fira Code', Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
  }
</style>
