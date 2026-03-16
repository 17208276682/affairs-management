<template>
  <div class="dept-manage">
    <div class="page-header">
      <h3>组织管理</h3>
      <el-tag type="info" effect="plain">左侧树形架构，右侧部门信息</el-tag>
    </div>

    <el-row :gutter="16" class="content-row" v-loading="loading">
      <el-col :span="7">
        <el-card class="tree-panel">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">部门树</span>
            </div>
          </template>
          <el-tree
            :data="deptOnlyTree"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            :current-node-key="selectedDeptId"
            @node-click="handleDeptClick"
          />
        </el-card>
      </el-col>

      <el-col :span="17">
        <el-card v-if="selectedDept" class="detail-panel">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">部门信息</span>
              <div class="actions">
                <el-button type="primary" @click="handleAddChild">新增下级</el-button>
                <el-button @click="handleEdit(selectedDept)">编辑</el-button>
                <el-button type="danger" :disabled="!canDeleteCurrent" @click="handleDeleteConfirm(selectedDept.id)">删除</el-button>
              </div>
            </div>
          </template>

          <div class="info-grid">
            <div class="info-item"><span>部门名称</span><strong>{{ selectedDept.name }}</strong></div>
            <div class="info-item"><span>部门ID</span><strong>{{ selectedDept.id }}</strong></div>
            <div class="info-item"><span>层级</span><strong>{{ selectedDept.level }}</strong></div>
            <div class="info-item"><span>上级部门</span><strong>{{ parentDeptName }}</strong></div>
            <div class="info-item"><span>负责人</span><strong>{{ selectedDept.leaderName || '未设置' }}</strong></div>
            <div class="info-item"><span>成员数</span><strong>{{ selectedDept.memberCount }}</strong></div>
          </div>

          <el-alert
            v-if="selectedDept.level === 0"
            type="warning"
            show-icon
            :closable="false"
            title="顶级节点不能删除，可编辑"
          />
          <el-alert
            v-else-if="!isLeaf"
            type="info"
            show-icon
            :closable="false"
            title="删除将级联删除该部门下全部子部门"
          />

          <el-divider />
          <div class="child-title">下级部门</div>
          <el-table :data="childrenDepts" size="small" stripe>
            <el-table-column label="部门名称" prop="name" min-width="180" />
            <el-table-column label="负责人" prop="leaderName" width="120" />
            <el-table-column label="人数" prop="memberCount" width="100" align="center" />
            <el-table-column label="层级" prop="level" width="80" align="center" />
          </el-table>
        </el-card>

        <el-card v-else>
          <el-empty description="请先在左侧选择部门" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 表单弹窗 -->
    <el-dialog v-model="showForm" :title="formTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="deptFormRules" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-input v-model="form.parentName" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useOrganizationStore } from '@/stores'
import { deptFormRules } from '@/utils/validate'
import type { Department, OrgTreeNode } from '@/types'

const orgStore = useOrganizationStore()
const loading = ref(false)
const showForm = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const selectedDeptId = ref('')

const form = ref({ id: '', name: '', parentId: '', parentName: '无（顶级部门）' })
const formTitle = computed(() => form.value.id ? '编辑部门' : '新增部门')

const deptOnlyTree = computed<OrgTreeNode[]>(() => {
  const filterDeptNodes = (nodes: OrgTreeNode[]): OrgTreeNode[] => {
    return nodes
      .filter(node => node.type === 'dept')
      .map(node => ({
        ...node,
        children: node.children ? filterDeptNodes(node.children) : [],
      }))
  }
  return filterDeptNodes(orgStore.orgTree)
})

const selectedDept = computed(() => orgStore.deptList.find(d => d.id === selectedDeptId.value) || null)
const isLeaf = computed(() => {
  if (!selectedDept.value) return false
  return !orgStore.deptList.some(d => d.parentId === selectedDept.value!.id)
})
const canDeleteCurrent = computed(() => {
  if (!selectedDept.value) return false
  return selectedDept.value.level !== 0
})
const parentDeptName = computed(() => {
  if (!selectedDept.value?.parentId) return '—'
  return orgStore.deptList.find(d => d.id === selectedDept.value!.parentId)?.name || '—'
})
const childrenDepts = computed(() => {
  if (!selectedDept.value) return []
  return orgStore.deptList.filter(d => d.parentId === selectedDept.value!.id)
})

function handleDeptClick(row: OrgTreeNode) {
  if (row.type !== 'dept') return
  selectedDeptId.value = row.id
}

function handleAddChild() {
  if (!selectedDept.value) return
  form.value = { id: '', name: '', parentId: selectedDept.value.id, parentName: selectedDept.value.name }
  showForm.value = true
}

function handleEdit(row: Department) {
  const parentName = row.parentId ? (orgStore.deptList.find(d => d.id === row.parentId)?.name || '—') : '无（顶级部门）'
  form.value = { id: row.id, name: row.name, parentId: row.parentId || '', parentName }
  showForm.value = true
}

async function handleDeleteConfirm(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该部门及其全部下级部门？', '删除确认', {
      type: 'warning',
      center: true,
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await handleDelete(id)
  } catch {
    // 用户取消
  }
}

async function handleDelete(id: string) {
  try {
    await orgStore.deleteDept(id)
    ElMessage.success('删除成功')
    await loadData()
    if (selectedDeptId.value === id) {
      selectedDeptId.value = orgStore.deptList[0]?.id || ''
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function save() {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await orgStore.updateDept(form.value.id, { name: form.value.name })
    } else {
      await orgStore.createDept({ name: form.value.name, parentId: form.value.parentId || undefined })
    }
    ElMessage.success('保存成功')
    showForm.value = false
    await loadData()
    if (!form.value.id) {
      const created = orgStore.deptList.find(d => d.name === form.value.name && (d.parentId || '') === (form.value.parentId || ''))
      if (created) selectedDeptId.value = created.id
    }
  } finally {
    saving.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    await Promise.all([orgStore.fetchDeptList(), orgStore.fetchOrgTree()])
    const hasSelected = selectedDeptId.value && orgStore.deptList.some(d => d.id === selectedDeptId.value)
    if ((!selectedDeptId.value || !hasSelected) && orgStore.deptList.length) {
      selectedDeptId.value = orgStore.deptList.find(d => d.level === 0)?.id || orgStore.deptList[0].id
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<style lang="scss" scoped>
.dept-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
    h3 { font-size: $font-size-xl; font-weight: 600; margin: 0; }
  }
}

.content-row {
  flex: 1;
  min-height: 0;
}

.tree-panel,
.detail-panel {
  height: 100%;
  :deep(.el-card__body) {
    height: calc(100% - 56px);
    overflow: auto;
  }
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
}

.panel-title {
  font-size: $font-size-md;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: $spacing-sm;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-sm $spacing-md;
  margin-bottom: $spacing-md;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm;
  background: $bg-page;
  border-radius: $radius-sm;

  span {
    color: $text-secondary;
    font-size: $font-size-sm;
  }

  strong {
    color: $text-primary;
    font-size: $font-size-sm;
    font-weight: 600;
  }
}

.child-title {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
}
</style>
