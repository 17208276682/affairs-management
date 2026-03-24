<template>
  <div class="org-tree-view">
    <div class="page-header">
      <h3>组织架构</h3>
    </div>

    <el-row :gutter="20" class="content-row">
      <!-- 左侧树形图 -->
      <el-col :span="10">
        <el-card v-loading="treeLoading" class="tree-card">
          <template #header>
            <div class="card-header">
              <span class="section-title">组织架构树</span>
              <el-button type="primary" size="small" @click="handleAddRoot">
                <el-icon><Plus /></el-icon>新增顶级部门
              </el-button>
            </div>
          </template>
          <el-input v-model="filterText" placeholder="搜索部门" prefix-icon="Search" clearable style="margin-bottom: 12px" />
          <el-tree
            ref="treeRef"
            :data="visibleOrgTree"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            :filter-node-method="filterNode"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-label">
                  <el-icon v-if="data.children?.length" color="#4F6EF7"><OfficeBuilding /></el-icon>
                  <el-icon v-else color="#909399"><User /></el-icon>
                  <span>{{ node.label }}</span>
                  <el-tag v-if="data.memberCount" size="small" type="info" effect="plain">
                    {{ data.memberCount }}人
                  </el-tag>
                </div>
                <div class="node-actions" @click.stop>
                  <el-button link size="small" @click="handleAddChild(data)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button link size="small" @click="handleEdit(data)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-popconfirm
                    title="确认删除此部门？子部门也将被删除"
                    @confirm="handleDelete(data.id)"
                  >
                    <template #reference>
                      <el-button link size="small" type="danger">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧详情 -->
      <el-col :span="14">
        <el-card v-if="selectedDept" class="detail-card">
          <template #header>
            <span class="section-title">{{ selectedDept.name }} - 部门详情</span>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="部门名称">{{ selectedDept.name }}</el-descriptions-item>
            <el-descriptions-item label="部门ID">{{ selectedDept.id }}</el-descriptions-item>
            <el-descriptions-item label="上级部门">{{ selectedDept.parentName || '无（顶级部门）' }}</el-descriptions-item>
            <el-descriptions-item label="部门层级">第 {{ selectedDept.level }} 级</el-descriptions-item>
            <el-descriptions-item label="人员数量">{{ selectedDept.memberCount || 0 }} 人</el-descriptions-item>
            <el-descriptions-item label="部门负责人">{{ selectedDept.managerName || '未设置' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 部门成员 -->
          <div class="dept-members">
            <div class="members-header">
              <h4>部门成员</h4>
              <el-button type="primary" size="small" @click="showMemberForm = true">
                <el-icon><Plus /></el-icon>添加成员
              </el-button>
            </div>
            <el-table :data="deptMembers" stripe style="width: 100%">
              <el-table-column label="姓名" prop="name" width="100" />
              <el-table-column label="职位" prop="position" width="120" />
              <el-table-column label="手机号" prop="phone" width="140" />
              <el-table-column label="邮箱" prop="email" />
              <el-table-column label="角色" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.role === 'ceo' ? 'danger' : row.role === 'manager' ? 'warning' : 'info'" size="small">
                    {{ ROLE_MAP[row.role] || row.role }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>

        <el-card v-else class="empty-card">
          <el-empty description="请在左侧选择部门查看详情" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 部门表单弹窗 -->
    <el-dialog v-model="showDeptForm" :title="deptFormTitle" width="480px" destroy-on-close>
      <el-form ref="deptFormRef" :model="deptForm" :rules="deptFormRules" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="deptForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-input :model-value="deptForm.parentName || '无（顶级部门）'" disabled />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="deptForm.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeptForm = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>

    <!-- 成员表单弹窗 -->
    <el-dialog v-model="showMemberForm" title="添加成员" width="480px" destroy-on-close>
      <el-form :model="memberForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="memberForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="memberForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="memberForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="memberForm.role" placeholder="选择角色">
            <el-option label="普通员工" value="staff" />
            <el-option label="部门经理" value="manager" />
            <el-option label="CEO" value="ceo" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMemberForm = false">取消</el-button>
        <el-button type="primary" @click="showMemberForm = false">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useOrganizationStore } from '@/stores'
import { ROLE_MAP } from '@/utils/constants'
import { deptFormRules } from '@/utils/validate'
import type { Department, OrgTreeNode, User } from '@/types'
import { getMemberListApi } from '@/api/organization'

const orgStore = useOrganizationStore()

const treeRef = ref()
const treeLoading = ref(false)
const filterText = ref('')
const selectedDept = ref<any>(null)
const showDeptForm = ref(false)
const showMemberForm = ref(false)
const saving = ref(false)
const deptFormRef = ref<FormInstance>()

const deptForm = ref({
  id: '',
  name: '',
  parentId: '',
  parentName: '',
  sort: 0,
})

const memberForm = ref({
  name: '',
  position: '',
  phone: '',
  email: '',
  role: 'staff',
})

const deptFormTitle = computed(() => deptForm.value.id ? '编辑部门' : '新增部门')

const deptMembers = ref<User[]>([])
const adminUserIds = ref<string[]>([])

const visibleOrgTree = computed<OrgTreeNode[]>(() => {
  const adminIdSet = new Set(adminUserIds.value)
  const filterNodes = (nodes: OrgTreeNode[]): OrgTreeNode[] => {
    return nodes
      .filter(node => !(node.type === 'member' && adminIdSet.has(node.id)))
      .map(node => ({
        ...node,
        children: node.children ? filterNodes(node.children) : [],
      }))
  }
  return filterNodes(orgStore.orgTree)
})

async function loadAdminUserIds() {
  try {
    const res = await getMemberListApi({ page: 1, pageSize: 9999 })
    adminUserIds.value = res.data.list.filter(item => item.role === 'admin').map(item => item.id)
  } catch {
    adminUserIds.value = []
  }
}

async function fetchDeptMembers(deptId: string) {
  try {
    const res = await getMemberListApi({ deptId, page: 1, pageSize: 100 })
    deptMembers.value = res.data.list.filter(item => item.role !== 'admin')
  } catch {
    deptMembers.value = []
  }
}

watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

function filterNode(value: string, data: any) {
  if (!value) return true
  return (data.label || data.name || '').includes(value)
}

function handleNodeClick(data: OrgTreeNode) {
  selectedDept.value = data
  if (data.type === 'dept') {
    fetchDeptMembers(data.id)
  }
}

function handleAddRoot() {
  deptForm.value = { id: '', name: '', parentId: '', parentName: '', sort: 0 }
  showDeptForm.value = true
}

function handleAddChild(parent: OrgTreeNode) {
  deptForm.value = { id: '', name: '', parentId: parent.id, parentName: parent.label, sort: 0 }
  showDeptForm.value = true
}

function handleEdit(data: OrgTreeNode) {
  deptForm.value = {
    id: data.id,
    name: data.label,
    parentId: data.parentId || '',
    parentName: '',
    sort: 0,
  }
  showDeptForm.value = true
}

async function handleDelete(id: string) {
  try {
    await orgStore.deleteDept(id)
    ElMessage.success('删除成功')
    if (selectedDept.value?.id === id) selectedDept.value = null
  } catch (e) {
    // handled
  }
}

async function saveDept() {
  if (!deptFormRef.value) return
  await deptFormRef.value.validate()
  saving.value = true
  try {
    if (deptForm.value.id) {
      await orgStore.updateDept(deptForm.value.id, { name: deptForm.value.name, sort: deptForm.value.sort })
    } else {
      await orgStore.createDept({
        name: deptForm.value.name,
        parentId: deptForm.value.parentId || undefined,
        sort: deptForm.value.sort,
      })
    }
    ElMessage.success('保存成功')
    showDeptForm.value = false
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  treeLoading.value = true
  try {
    await Promise.all([orgStore.fetchOrgTree(), loadAdminUserIds()])
  } finally {
    treeLoading.value = false
  }
})
</script>

<style lang="scss" scoped>
.org-tree-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .page-header {
    margin-bottom: $spacing-md;
    h3 {
      font-size: $font-size-xl;
      font-weight: 600;
      margin: 0;
    }
  }
}

.content-row {
  flex: 1;
  min-height: 0;
}

.tree-card {
  height: 100%;
  :deep(.el-card__body) {
    height: calc(100% - 56px);
    overflow: auto;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .section-title {
    font-weight: 600;
    font-size: $font-size-md;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;

  .node-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-sm;
  }

  .node-actions {
    display: none;
  }

  &:hover .node-actions {
    display: flex;
    align-items: center;
    gap: 2px;
  }
}

.detail-card {
  height: 100%;
  :deep(.el-card__body) {
    height: calc(100% - 2px);
    overflow: auto;
  }

  .section-title {
    font-weight: 600;
    font-size: $font-size-md;
  }
}

.dept-members {
  margin-top: $spacing-lg;

  .members-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;

    h4 {
      font-size: $font-size-md;
      font-weight: 600;
      margin: 0;
    }
  }
}

.empty-card {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
