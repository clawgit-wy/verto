<template>
  <div>
    <a-tabs v-model:activeKey="activeCategory" @change="handleCategoryChange">
      <a-tab-pane key="" tab="全部" />
      <a-tab-pane key="official" tab="官方" />
      <a-tab-pane key="business" tab="业务" />
      <a-tab-pane key="app" tab="应用级" />
    </a-tabs>

    <div class="mb-2 flex justify-between">
      <div>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">新增Skill</a-button>
        <a-button class="ml-2" @click="handleExportConfig" preIcon="ant-design:export-outlined">导出MCP配置</a-button>
      </div>
      <div>
        <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
          <a-radio-button value="table">
            <UnorderedListOutlined />
          </a-radio-button>
          <a-radio-button value="card">
            <AppstoreOutlined />
          </a-radio-button>
        </a-radio-group>
      </div>
    </div>

    <div v-if="viewMode === 'table'">
      <BasicTable @register="registerTable" :rowSelection="rowSelection">
        <template #tableTitle>
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined" />
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button>
              批量操作
              <Icon icon="mdi:chevron-down" />
            </a-button>
          </a-dropdown>
        </template>
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
        <template #category="{ record }">
          <a-tag :color="categoryMap[record.category]?.color || 'default'">
            {{ categoryMap[record.category]?.label || record.category }}
          </a-tag>
        </template>
        <template #status="{ record }">
          <a-badge :status="record.status === 'enable' ? 'success' : 'error'" :text="record.status === 'enable' ? '启用' : '禁用'" />
        </template>
      </BasicTable>
    </div>

    <div v-else>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="8" :lg="6" :xl="4" v-for="item in cardData" :key="item.id">
          <SkillCard :skill="item" @edit="handleEdit" @delete="handleDelete" @test="handleTest" />
        </a-col>
      </a-row>
      <div class="mt-4 text-center">
        <a-pagination
          v-model:current="cardPageNo"
          v-model:pageSize="cardPageSize"
          :total="cardTotal"
          show-quick-jumper
          :show-total="(total) => `共 ${total} 条`"
          @change="onCardPageChange"
        />
      </div>
    </div>

    <SkillEditModal @register="registerModal" @success="handleSuccess" />
    <SkillExportModal @register="registerExportModal" />
  </div>
</template>

<script lang="ts" name="feplatform-skill-list" setup>
  import { ref, computed, onMounted } from 'vue';
  import { AppstoreOutlined, UnorderedListOutlined } from '@ant-design/icons-vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema, categoryMap } from './Skill.data';
  import { list, deleteOne, batchDelete, getExportUrl, getImportUrl } from '/@/api/feplatform/skill';
  import SkillCard from './components/SkillCard.vue';
  import SkillEditModal from './components/SkillEditModal.vue';
  import SkillExportModal from './components/SkillExportModal.vue';
  import Icon from '/@/components/Icon';

  const activeCategory = ref('');
  const viewMode = ref('table');
  const cardData = ref<any[]>([]);
  const cardPageNo = ref(1);
  const cardPageSize = ref(12);
  const cardTotal = ref(0);

  const [registerModal, { openModal }] = useModal();
  const [registerExportModal, { openModal: openExportModal }] = useModal();

  const { tableContext } = useListPage({
    tableProps: {
      title: 'Skill资产库',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
      },
      actionColumn: {
        width: 180,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        if (activeCategory.value) {
          params.category = activeCategory.value;
        }
        return params;
      },
    },
    exportConfig: {
      name: 'AI Skill资产',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
      success: reload,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  function handleCategoryChange() {
    if (viewMode.value === 'table') {
      reload();
    } else {
      loadCardData();
    }
  }

  async function loadCardData() {
    const params: any = {
      pageNo: cardPageNo.value,
      pageSize: cardPageSize.value,
    };
    if (activeCategory.value) {
      params.category = activeCategory.value;
    }
    const res = await list(params);
    cardData.value = res.records || [];
    cardTotal.value = res.total || 0;
  }

  function onCardPageChange(page: number) {
    cardPageNo.value = page;
    loadCardData();
  }

  function handleAdd() {
    openModal(true, { isUpdate: false, showFooter: true });
  }

  function handleEdit(record: Recordable) {
    openModal(true, { record, isUpdate: true, showFooter: true });
  }

  function handleDelete(record: Recordable) {
    deleteOne({ id: record.id }, reload);
  }

  function handleTest(record: Recordable) {
    openModal(true, { record, isUpdate: true, showFooter: true, activeTab: 'test' });
  }

  function handleExportConfig() {
    openExportModal(true, {});
  }

  function batchHandleDelete() {
    if (selectedRowKeys.value.length > 0) {
      batchDelete({ ids: selectedRowKeys.value.join(',') }, reload);
    }
  }

  function handleSuccess() {
    if (viewMode.value === 'table') {
      reload();
    } else {
      loadCardData();
    }
  }

  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  function getDropDownAction(record) {
    return [
      {
        label: '测试',
        onClick: handleTest.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  onMounted(() => {
    loadCardData();
  });
</script>
