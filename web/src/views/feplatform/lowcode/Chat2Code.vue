<template>
  <div class="chat2code-container">
    <div class="chat2code-left">
      <a-card title="Skill选择器" size="small" class="h-full">
        <a-checkbox-group v-model:value="selectedSkillIds" class="skill-checkbox-group">
          <template v-for="cat in skillCategories" :key="cat.key">
            <div class="skill-category-title">{{ cat.label }}</div>
            <div v-for="skill in cat.skills" :key="skill.id" class="skill-checkbox-item">
              <a-checkbox :value="skill.id">
                <a-tooltip :title="skill.description">
                  <span>{{ skill.name }}</span>
                </a-tooltip>
              </a-checkbox>
            </div>
            <a-empty v-if="cat.skills.length === 0" :image="null" description="暂无" />
          </template>
        </a-checkbox-group>
      </a-card>
    </div>

    <div class="chat2code-center">
      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="chat-empty">
          <ThunderboltOutlined style="font-size: 48px; color: #1890ff" />
          <p class="mt-4 text-lg">选择Skill，开始对话生成代码</p>
          <p class="text-gray-400">支持JeecgBoot代码生成、Online表单、报表等多种Skill</p>
        </div>
        <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-message', msg.role === 'user' ? 'chat-message-user' : 'chat-message-assistant']">
          <div class="chat-message-avatar">
            <a-avatar v-if="msg.role === 'user'" size="small" style="background-color: #1890ff">U</a-avatar>
            <a-avatar v-else size="small" style="background-color: #722ed1">AI</a-avatar>
          </div>
          <div class="chat-message-content">
            <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
            <div v-else>{{ msg.content }}</div>
          </div>
        </div>
        <div v-if="loading" class="chat-message chat-message-assistant">
          <div class="chat-message-avatar">
            <a-avatar size="small" style="background-color: #722ed1">AI</a-avatar>
          </div>
          <div class="chat-message-content">
            <a-spin size="small" /> 思考中...
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <a-textarea
          v-model:value="inputMessage"
          :rows="3"
          placeholder="输入你的需求，例如：生成一个车辆管理的CRUD代码..."
          @pressEnter="handleSend"
        />
        <div class="chat-input-actions">
          <a-button type="primary" :loading="loading" :disabled="!inputMessage.trim()" @click="handleSend">
            <template #icon><SendOutlined /></template>
            发送
          </a-button>
        </div>
      </div>
    </div>

    <div class="chat2code-right">
      <a-card title="代码预览" size="small" class="h-full">
        <a-empty v-if="!codePreview" description="对话中的代码将在此展示" />
        <template v-else>
          <div class="code-preview-header">
            <span class="code-language">{{ codeLanguage }}</span>
            <a-button size="small" type="link" @click="copyCode">
              <CopyOutlined /> 复制
            </a-button>
          </div>
          <pre class="code-preview-content"><code>{{ codePreview }}</code></pre>
        </template>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" name="feplatform-lowcode-chat2code" setup>
  import { ref, computed, onMounted, nextTick } from 'vue';
  import { ThunderboltOutlined, SendOutlined, CopyOutlined } from '@ant-design/icons-vue';
  import { chat2code, getAvailableSkills } from '/@/api/feplatform/lowcode';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  const selectedSkillIds = ref<string[]>([]);
  const allSkills = ref<any[]>([]);
  const messages = ref<Array<{ role: string; content: string }>>([]);
  const inputMessage = ref('');
  const loading = ref(false);
  const codePreview = ref('');
  const codeLanguage = ref('');
  const messagesRef = ref<HTMLElement>();

  const skillCategories = computed(() => {
    const categories: Array<{ key: string; label: string; skills: any[] }> = [
      { key: 'official', label: '官方Skill', skills: [] },
      { key: 'business', label: '业务Skill', skills: [] },
      { key: 'app', label: '应用级Skill', skills: [] },
    ];
    allSkills.value.forEach((skill) => {
      const cat = categories.find((c) => c.key === skill.category);
      if (cat) cat.skills.push(skill);
    });
    return categories;
  });

  onMounted(async () => {
    const res = await getAvailableSkills();
    allSkills.value = res || [];
  });

  async function handleSend(e?: any) {
    if (e?.shiftKey) return;
    e?.preventDefault?.();

    const msg = inputMessage.value.trim();
    if (!msg || loading.value) return;

    messages.value.push({ role: 'user', content: msg });
    inputMessage.value = '';
    loading.value = true;

    try {
      const res = await chat2code({
        message: msg,
        skillIds: selectedSkillIds.value,
      });
      const assistantMsg = res?.message || 'Chat2Code接口已就绪，等待接入AI对话引擎';
      messages.value.push({ role: 'assistant', content: assistantMsg });
      extractCode(assistantMsg);
    } catch (e: any) {
      messages.value.push({ role: 'assistant', content: '请求失败: ' + (e?.message || '未知错误') });
    } finally {
      loading.value = false;
      await nextTick();
      scrollToBottom();
    }
  }

  function extractCode(text: string) {
    const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g;
    let match;
    let allCode = '';
    let lang = '';
    while ((match = codeBlockRegex.exec(text)) !== null) {
      lang = match[1] || 'text';
      allCode += match[2] + '\n';
    }
    if (allCode) {
      codePreview.value = allCode.trim();
      codeLanguage.value = lang;
    }
  }

  function renderMarkdown(content: string) {
    return content
      .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre class="code-block"><code>$2</code></pre>')
      .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br/>');
  }

  function scrollToBottom() {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
    }
  }

  async function copyCode() {
    if (!codePreview.value) return;
    try {
      await navigator.clipboard.writeText(codePreview.value);
      createMessage.success('代码已复制到剪贴板');
    } catch {
      createMessage.error('复制失败');
    }
  }
</script>

<style lang="less" scoped>
  .chat2code-container {
    display: flex;
    height: calc(100vh - 180px);
    gap: 12px;
    padding: 12px;
  }

  .chat2code-left {
    width: 240px;
    flex-shrink: 0;

    .skill-category-title {
      font-weight: 600;
      font-size: 13px;
      padding: 8px 0 4px;
      color: #333;
    }

    .skill-checkbox-item {
      padding: 2px 0;
    }

    .skill-checkbox-group {
      width: 100%;
    }
  }

  .chat2code-center {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .chat-messages {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      background: #fafafa;
      border-radius: 8px;
      margin-bottom: 12px;
    }

    .chat-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #999;
    }

    .chat-message {
      display: flex;
      margin-bottom: 16px;

      &-user {
        flex-direction: row-reverse;

        .chat-message-content {
          background: #1890ff;
          color: #fff;
          border-radius: 12px 2px 12px 12px;
        }
      }

      &-assistant {
        .chat-message-content {
          background: #fff;
          border-radius: 2px 12px 12px 12px;
          border: 1px solid #f0f0f0;
        }
      }

      &-avatar {
        flex-shrink: 0;
        margin: 0 8px;
      }

      &-content {
        max-width: 70%;
        padding: 10px 14px;
        font-size: 14px;
        line-height: 1.6;
        word-break: break-word;

        :deep(.code-block) {
          background: #1e1e1e;
          color: #d4d4d4;
          padding: 12px;
          border-radius: 4px;
          overflow-x: auto;
          font-family: 'Fira Code', Consolas, monospace;
          font-size: 13px;
          margin: 8px 0;
        }

        :deep(.inline-code) {
          background: #f5f5f5;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: 'Fira Code', Consolas, monospace;
          font-size: 13px;
        }
      }
    }

    .chat-input-area {
      position: relative;

      .chat-input-actions {
        position: absolute;
        right: 12px;
        bottom: 8px;
      }
    }
  }

  .chat2code-right {
    width: 360px;
    flex-shrink: 0;

    .code-preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .code-language {
        font-size: 12px;
        color: #999;
        background: #f5f5f5;
        padding: 2px 8px;
        border-radius: 4px;
      }
    }

    .code-preview-content {
      max-height: calc(100vh - 280px);
      overflow: auto;
      background: #1e1e1e;
      color: #d4d4d4;
      padding: 12px;
      border-radius: 4px;
      font-family: 'Fira Code', Consolas, monospace;
      font-size: 13px;
      line-height: 1.6;
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
</style>
