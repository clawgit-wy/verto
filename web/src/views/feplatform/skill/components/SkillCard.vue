<template>
  <a-card hoverable class="skill-card" @click="$emit('edit', skill)">
    <template #cover>
      <div class="skill-card-cover">
        <Icon :icon="skill.icon || 'ant-design:thunderbolt-outlined'" :size="48" class="skill-card-icon" />
      </div>
    </template>
    <template #actions>
      <EditOutlined key="edit" @click.stop="$emit('edit', skill)" />
      <PlayCircleOutlined key="test" @click.stop="$emit('test', skill)" />
      <DeleteOutlined key="delete" @click.stop="confirmDelete" />
    </template>
    <a-card-meta>
      <template #title>
        <div class="flex items-center justify-between">
          <span class="skill-card-title">{{ skill.name }}</span>
          <a-tag :color="categoryMap[skill.category]?.color || 'default'" size="small">
            {{ categoryMap[skill.category]?.label || skill.category }}
          </a-tag>
        </div>
      </template>
      <template #description>
        <div class="skill-card-desc">
          <a-typography-paragraph :content="skill.description || '暂无描述'" :ellipsis="{ rows: 2 }" />
        </div>
        <div class="skill-card-footer">
          <span class="skill-card-code">{{ skill.code }}</span>
          <span>v{{ skill.version }}</span>
        </div>
      </template>
    </a-card-meta>
  </a-card>
</template>

<script lang="ts" setup>
  import { EditOutlined, DeleteOutlined, PlayCircleOutlined } from '@ant-design/icons-vue';
  import { Modal } from 'ant-design-vue';
  import Icon from '/@/components/Icon';
  import { categoryMap } from '../Skill.data';

  const props = defineProps({
    skill: { type: Object, required: true },
  });

  const emit = defineEmits(['edit', 'delete', 'test']);

  function confirmDelete() {
    Modal.confirm({
      title: '确认删除',
      content: `是否删除Skill「${props.skill.name}」？`,
      okText: '确认',
      cancelText: '取消',
      onOk: () => emit('delete', props.skill),
    });
  }
</script>

<style lang="less" scoped>
  .skill-card {
    height: 100%;

    &-cover {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 80px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &-icon {
      color: #fff;
    }

    &-title {
      font-size: 14px;
      font-weight: 500;
    }

    &-desc {
      min-height: 40px;
    }

    &-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 8px;
      font-size: 12px;
      color: #999;
    }

    &-code {
      font-family: monospace;
    }
  }
</style>
