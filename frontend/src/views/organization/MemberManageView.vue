<template>
  <div class="member-manage">
    <div class="page-header">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增人员
      </el-button>
    </div>

    <el-row :gutter="16" class="content-row">
      <!-- 左侧部门树 -->
      <el-col :span="6">
        <el-card class="tree-panel">
          <template #header>
            <div class="tree-header-row">
              <span class="panel-title">部门结构</span>
              <div class="tree-actions">
                <el-button type="primary" size="small" :disabled="!selectedTreeDept" @click="handleAddChildDept">新增下级</el-button>
                <el-button size="small" :disabled="!selectedTreeDept" @click="handleEditDept">编辑</el-button>
                <el-button type="danger" size="small" :disabled="!canDeleteSelectedTreeDept" @click="handleDeleteDeptConfirm">删除</el-button>
              </div>
            </div>
          </template>
          <div class="all-members-btn" :class="{ active: showAllMode }" @click="handleShowAll">
            <el-icon><User /></el-icon>
            <span>所有人员</span>
          </div>
          <el-tree
            class="no-collapse-tree"
            :data="deptOnlyTree"
            :props="treeNodeProps"
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
            highlight-current
            @node-click="handleDeptClick"
          />
        </el-card>
      </el-col>

      <!-- 右侧列表 -->
      <el-col :span="18">
        <el-card v-loading="loading" class="list-panel">
          <div class="filter-row">
            <div class="filter-left">
              <el-input v-model="nameKeyword" placeholder="搜索姓名" prefix-icon="Search" clearable @keyup.enter="handleFilterChange" />
              <el-select v-model="filterDeptId" placeholder="按部门筛选" clearable @change="handleFilterChange">
                <el-option v-for="option in deptSelectOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
              <el-select v-model="positionKeyword" placeholder="按职位筛选" clearable @change="handleFilterChange">
                <el-option v-for="option in positionOptions" :key="option" :label="option" :value="option" />
              </el-select>
              <el-select v-model="roleKeyword" placeholder="按角色筛选" clearable @change="handleFilterChange">
                <el-option label="总经理" value="ceo" />
                <el-option label="副总经理" value="director" />
                <el-option label="负责人" value="manager" />
                <el-option label="普通员工" value="staff" />
              </el-select>
            </div>
            <div class="filter-right">
              <el-button type="primary" @click="handleFilterChange">搜索</el-button>
              <el-button @click="clearFilters">重置</el-button>
            </div>
          </div>
          <div class="table-wrapper">
            <el-table :data="sortedMemberList" stripe class="member-table" table-layout="auto" @sort-change="handleTableSortChange">
              <el-table-column label="姓名" prop="name" min-width="100" sortable="custom" />
              <el-table-column label="部门" prop="deptName" min-width="180" sortable="custom" />
              <el-table-column label="职位" prop="position" min-width="140" />
              <el-table-column label="手机号" prop="phone" min-width="150" />
              <el-table-column label="邮箱" prop="email" min-width="220" />
              <el-table-column label="角色" prop="role" min-width="220" align="center" sortable="custom">
              <template #default="{ row }">
                <div class="role-lines">
                  <div v-for="tag in getRoleTags(row)" :key="tag.key" class="role-line">
                    <el-tag size="small" :type="tag.type">
                      {{ tag.label }}
                    </el-tag>
                  </div>
                </div>
              </template>
              </el-table-column>
              <el-table-column label="操作" min-width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteConfirm(row.id)">删除</el-button>
              </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表单弹窗 -->
    <el-dialog v-model="showForm" :title="formTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="memberFormRules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item v-if="!form.id || form.needResetPassword" label="密码" prop="password">
          <div class="password-row">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入强密码" />
            <el-button @click="generateAndFillPassword">随机生成</el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="form.id" label="重置密码">
          <el-switch v-model="form.needResetPassword" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOnlyTree"
            :props="treeNodeProps"
            placeholder="选择部门"
            check-strictly
            node-key="id"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="option in roleOptionsForSelectedDept" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="showTopRoleManagedDept" label="兼任负责人">
          <el-select
            v-model="form.managedDeptIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
            placeholder="可选择兼任负责的第二层部门"
          >
            <el-option v-for="option in secondLevelDeptOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDeptForm" :title="deptFormTitle" width="480px" destroy-on-close>
      <el-form ref="deptFormRef" :model="deptForm" :rules="deptFormRules" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="deptForm.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-input v-model="deptForm.parentName" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeptForm = false">取消</el-button>
        <el-button type="primary" :loading="deptSaving" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useOrganizationStore } from '@/stores'
import { ROLE_MAP } from '@/utils/constants'
import { memberFormRules, deptFormRules } from '@/utils/validate'
import type { User, OrgTreeNode, Department } from '@/types'
import { getMemberListApi } from '@/api/organization'

const orgStore = useOrganizationStore()
const loading = ref(false)
const showForm = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const showDeptForm = ref(false)
const deptSaving = ref(false)
const deptFormRef = ref<FormInstance>()
const nameKeyword = ref('')
const filterDeptId = ref('')
const positionKeyword = ref('')
const roleKeyword = ref('')
const selectedDeptId = ref('')
const selectedTreeDeptId = ref('')
const showAllMode = ref(false)
const page = ref(1)
const pageSize = ref(9999)
const sortProp = ref<'name' | 'deptName' | 'role' | ''>('')
const sortOrder = ref<'ascending' | 'descending' | null>(null)

const form = ref({
  id: '', name: '', password: '', deptId: '', position: '',
  phone: '', email: '', role: 'staff', managedDeptIds: [] as string[], needResetPassword: false,
})

const deptForm = ref({ id: '', name: '', parentId: '', parentName: '无（顶级部门）' })

const formTitle = computed(() => form.value.id ? '编辑人员' : '新增人员')
const deptFormTitle = computed(() => deptForm.value.id ? '编辑部门' : '新增部门')
const treeNodeProps = { label: 'label', children: 'children', value: 'id' }
const selectedTreeDept = computed(() => orgStore.deptList.find(d => d.id === selectedTreeDeptId.value) || null)
const canDeleteSelectedTreeDept = computed(() => !!selectedTreeDept.value && selectedTreeDept.value.level !== 0)

watch(() => form.value.deptId, (newDeptId) => {
  if (!newDeptId) return
  const options = getRoleOptionsByDeptId(newDeptId)
  if (!options.some(option => option.value === form.value.role)) {
    form.value.role = options[0]?.value || 'staff'
  }
})

watch(() => form.value.role, (newRole) => {
  if (newRole !== 'ceo' && newRole !== 'director') {
    form.value.managedDeptIds = []
  }
})

const roleOptionsForSelectedDept = computed(() => getRoleOptionsByDeptId(form.value.deptId))

const showTopRoleManagedDept = computed(() => form.value.role === 'ceo' || form.value.role === 'director')

const secondLevelDeptOptions = computed(() => {
  return orgStore.deptList
    .filter(dept => dept.level === 1)
    .map(dept => ({ label: dept.name, value: dept.id }))
})

const deptSelectOptions = computed(() => orgStore.deptList.map(dept => ({
  label: dept.name,
  value: dept.id,
})))

const positionOptions = computed(() => {
  const set = new Set<string>()
  orgStore.memberList.forEach(m => { if (m.position) set.add(m.position) })
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

const deptLevelMap = computed<Record<string, number>>(() => {
  return orgStore.deptList.reduce((acc, dept: Department) => {
    acc[dept.id] = dept.level
    return acc
  }, {} as Record<string, number>)
})

const sortedMemberList = computed(() => {
  const baseList = orgStore.memberList.filter(member => member.role !== 'admin')
  const list = roleKeyword.value
    ? baseList.filter(member => matchRoleFilter(member))
    : baseList

  const rolePriority = (role: string) => {
    if (role === 'ceo') return -1
    if (role === 'director') return 0
    if (role === 'manager') return 1
    return 2
  }

  const sorted = [...list].sort((a, b) => {
    const roleDiff = rolePriority(a.role) - rolePriority(b.role)
    if (roleDiff !== 0) return roleDiff

    const levelA = deptLevelMap.value[a.deptId] ?? 99
    const levelB = deptLevelMap.value[b.deptId] ?? 99
    if (levelA !== levelB) return levelA - levelB

    const deptDiff = (a.deptName || '').localeCompare(b.deptName || '', 'zh-CN')
    if (deptDiff !== 0) return deptDiff

    return (a.name || '').localeCompare(b.name || '', 'zh-CN')
  })

  if (!sortProp.value || !sortOrder.value) return sorted

  const direction = sortOrder.value === 'ascending' ? 1 : -1
  return [...sorted].sort((a, b) => {
    if (sortProp.value === 'name') {
      return (a.name || '').localeCompare(b.name || '', 'zh-CN') * direction
    }
    if (sortProp.value === 'deptName') {
      return (a.deptName || '').localeCompare(b.deptName || '', 'zh-CN') * direction
    }
    const roleDiff = rolePriority(a.role) - rolePriority(b.role)
    return roleDiff * direction
  })
})

const deptNameMap = computed<Record<string, string>>(() => {
  return orgStore.deptList.reduce((acc, dept) => {
    acc[dept.id] = dept.name
    return acc
  }, {} as Record<string, string>)
})

function matchRoleFilter(member: User) {
  if (!roleKeyword.value) return true
  if (member.role === roleKeyword.value) return true
  if (roleKeyword.value === 'manager' && (member.role === 'ceo' || member.role === 'director')) {
    return (member.managedDeptIds?.length || 0) > 0
  }
  return false
}

function getRoleTags(member: User) {
  const tags = [{
    key: `base-${member.id}`,
    label: ROLE_MAP[member.role] || member.role,
    type: member.role === 'manager' ? 'warning' : (member.role === 'staff' ? 'info' : 'danger'),
  }]

  if ((member.role === 'ceo' || member.role === 'director') && member.managedDeptIds?.length) {
    for (const deptId of member.managedDeptIds) {
      const deptName = deptNameMap.value[deptId] || deptId
      tags.push({ key: `managed-${member.id}-${deptId}`, label: `负责人(${deptName})`, type: 'warning' as const })
    }
  }
  return tags
}

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

function handleDeptClick(data: OrgTreeNode) {
  if (data.type !== 'dept') return
  showAllMode.value = false
  selectedTreeDeptId.value = data.id
  filterDeptId.value = ''
  selectedDeptId.value = data.id
  page.value = 1
  loadMembers()
}

function handleShowAll() {
  showAllMode.value = true
  selectedTreeDeptId.value = ''
  selectedDeptId.value = ''
  filterDeptId.value = ''
  page.value = 1
  loadMembers()
}

function handleTableSortChange({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) {
  if (prop !== 'name' && prop !== 'deptName' && prop !== 'role') {
    sortProp.value = ''
    sortOrder.value = null
    return
  }
  sortProp.value = prop
  sortOrder.value = order
}

function handleFilterChange() {
  page.value = 1
  loadMembers()
}

function clearFilters() {
  nameKeyword.value = ''
  filterDeptId.value = ''
  positionKeyword.value = ''
  roleKeyword.value = ''
  page.value = 1
  loadMembers()
}

async function loadMembers() {
  loading.value = true
  try {
    const deptId = filterDeptId.value || selectedDeptId.value || undefined
    await orgStore.fetchMemberList({
      deptId,
      keyword: nameKeyword.value || undefined,
      position: positionKeyword.value || undefined,
      page: 1,
      pageSize: 9999,
    })
    if (deptId) {
      const currentDept = orgStore.deptList.find(d => d.id === deptId)
      const needAppendManagedTopRoles = currentDept?.level === 1
      const allRes = await getMemberListApi({ page: 1, pageSize: 9999 })
      const baseMembers = [...orgStore.memberList]
      const extraManagedTopRoles = needAppendManagedTopRoles ? allRes.data.list.filter(item => {
        if (item.role !== 'ceo' && item.role !== 'director') return false
        if (!item.managedDeptIds?.length) return false
        if (!item.managedDeptIds.includes(deptId)) return false
        return !baseMembers.some(base => base.id === item.id)
      }) : []
      orgStore.memberList = [...baseMembers, ...extraManagedTopRoles]
      orgStore.memberTotal = orgStore.memberList.length
    }
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  const defaultDeptId = filterDeptId.value || selectedTreeDeptId.value || selectedDeptId.value
  form.value = {
    id: '',
    name: '',
    password: generateStrongPassword(),
    deptId: defaultDeptId,
    position: '',
    phone: '',
    email: '',
    role: 'staff',
    managedDeptIds: [],
    needResetPassword: false,
  }
  const options = getRoleOptionsByDeptId(form.value.deptId)
  if (options.length > 0) {
    form.value.role = options[0].value
  }
  showForm.value = true
}

function handleEdit(row: User) {
  form.value = {
    id: row.id, name: row.name,
    password: '',
    deptId: row.deptId, position: row.position || '',
    phone: row.phone || '', email: row.email || '', role: row.role, managedDeptIds: row.managedDeptIds || [], needResetPassword: false,
  }
  showForm.value = true
}

function generateStrongPassword() {
  const uppers = 'ABCDEFGHJKLMNPQRSTUVWXYZ'
  const lowers = 'abcdefghijkmnopqrstuvwxyz'
  const numbers = '23456789'
  const symbols = '!@#$%^&*'
  const all = uppers + lowers + numbers + symbols
  const chars = [
    uppers[Math.floor(Math.random() * uppers.length)],
    lowers[Math.floor(Math.random() * lowers.length)],
    numbers[Math.floor(Math.random() * numbers.length)],
    symbols[Math.floor(Math.random() * symbols.length)],
  ]
  while (chars.length < 12) {
    chars.push(all[Math.floor(Math.random() * all.length)])
  }
  for (let i = chars.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[chars[i], chars[j]] = [chars[j], chars[i]]
  }
  return chars.join('')
}

function generateAndFillPassword() {
  form.value.password = generateStrongPassword()
}

async function validateManagerLimit() {
  if (form.value.role !== 'manager' || !form.value.deptId) return
  const res = await getMemberListApi({ deptId: form.value.deptId, page: 1, pageSize: 999 })
  const hasOtherManager = res.data.list.some(m => m.role === 'manager' && m.id !== form.value.id)
  if (hasOtherManager) {
    throw new Error('同一部门只能设置一名负责人')
  }
}

function getRoleOptionsByDeptId(deptId?: string) {
  const dept = orgStore.deptList.find(d => d.id === deptId)
  const isTopLevel = !!dept && (dept.level === 0 || !dept.parentId)
  if (isTopLevel) {
    return [
      { label: '总经理', value: 'ceo' },
      { label: '副总经理', value: 'director' },
    ]
  }
  return [
    { label: '负责人', value: 'manager' },
    { label: '普通员工', value: 'staff' },
  ]
}

async function handleDeleteConfirm(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该人员？', '删除确认', {
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
  await orgStore.deleteMember(id)
  ElMessage.success('删除成功')
  loadMembers()
}

function handleAddChildDept() {
  if (!selectedTreeDept.value) return
  deptForm.value = {
    id: '',
    name: '',
    parentId: selectedTreeDept.value.id,
    parentName: selectedTreeDept.value.name,
  }
  showDeptForm.value = true
}

function handleEditDept() {
  if (!selectedTreeDept.value) return
  const parentName = selectedTreeDept.value.parentId
    ? (orgStore.deptList.find(d => d.id === selectedTreeDept.value!.parentId)?.name || '—')
    : '无（顶级部门）'
  deptForm.value = {
    id: selectedTreeDept.value.id,
    name: selectedTreeDept.value.name,
    parentId: selectedTreeDept.value.parentId || '',
    parentName,
  }
  showDeptForm.value = true
}

async function handleDeleteDeptConfirm() {
  if (!selectedTreeDept.value) return
  try {
    await ElMessageBox.confirm('确认删除该部门及其全部下级部门？', '删除确认', {
      type: 'warning',
      center: true,
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await orgStore.deleteDept(selectedTreeDept.value.id)
    ElMessage.success('删除成功')
    await Promise.all([orgStore.fetchOrgTree(), orgStore.fetchDeptList()])
    selectedTreeDeptId.value = orgStore.deptList.find(d => d.level === 0)?.id || ''
    selectedDeptId.value = selectedTreeDeptId.value
    loadMembers()
  } catch {
    // 用户取消
  }
}

async function saveDept() {
  if (!deptFormRef.value) return
  await deptFormRef.value.validate()
  deptSaving.value = true
  try {
    if (deptForm.value.id) {
      await orgStore.updateDept(deptForm.value.id, { name: deptForm.value.name })
    } else {
      await orgStore.createDept({ name: deptForm.value.name, parentId: deptForm.value.parentId || undefined })
    }
    ElMessage.success('保存成功')
    showDeptForm.value = false
    await Promise.all([orgStore.fetchOrgTree(), orgStore.fetchDeptList()])
  } finally {
    deptSaving.value = false
  }
}

async function save() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    await validateManagerLimit()
  } catch (e: any) {
    ElMessage.warning(e?.message || '校验失败')
    return
  }
  saving.value = true
  try {
    const payload: any = {
      ...form.value,
      managedDeptIds: showTopRoleManagedDept.value ? form.value.managedDeptIds : [],
      password: (!form.value.id || form.value.needResetPassword) ? form.value.password : undefined,
    }
    if (form.value.id) {
      await orgStore.updateMember(form.value.id, payload)
    } else {
      await orgStore.createMember(payload)
    }
    ElMessage.success('保存成功')
    showForm.value = false
    loadMembers()
  } catch (error: any) {
    ElMessage.error(error?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([orgStore.fetchOrgTree(), orgStore.fetchDeptList()])
  selectedTreeDeptId.value = orgStore.deptList.find(d => d.level === 0)?.id || ''
  selectedDeptId.value = selectedTreeDeptId.value
  loadMembers()
})
</script>

<style lang="scss" scoped>
.member-manage {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .page-header {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-bottom: $spacing-sm;
  }
}

.content-row {
  flex: 1;
  min-height: 0;
}

.tree-panel {
  height: 100%;
  :deep(.el-card__body) {
    height: calc(100% - 56px);
    overflow: auto;
  }

  .panel-title {
    font-weight: 600;
    font-size: $font-size-sm;
  }
}

.tree-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.tree-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-lines {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

.role-line {
  width: 100%;
  display: flex;
  justify-content: center;
}

:deep(.no-collapse-tree .el-tree-node__expand-icon) {
  visibility: hidden;
  pointer-events: none;
}

/* 树结构竖线 */
:deep(.no-collapse-tree) {
  .el-tree-node {
    position: relative;
  }

  .el-tree-node__children > .el-tree-node::before {
    content: '';
    position: absolute;
    left: 11px;
    top: 0;
    height: 100%;
    border-left: 1px solid #dcdfe6;
  }

  .el-tree-node__children > .el-tree-node::after {
    content: '';
    position: absolute;
    left: 11px;
    top: 20px;
    width: 12px;
    border-top: 1px solid #dcdfe6;
  }

  .el-tree-node__children > .el-tree-node:last-child::before {
    height: 20px;
  }

  .el-tree-node__children {
    padding-left: 12px;
  }
}

.list-panel {
  height: 100%;
  :deep(.el-card__body) {
    height: calc(100% - 2px);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .table-wrapper {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  :deep(.member-table) {
    min-width: 1180px;
  }

  :deep(.member-table .el-table__header-wrapper th .cell) {
    white-space: nowrap;
  }
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;

  .filter-left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    :deep(.el-input),
    :deep(.el-select) {
      width: 200px;
    }
  }

  .filter-right {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }
}

.password-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

.all-members-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  margin-bottom: 4px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
  color: $text-regular;
  transition: background $transition-fast, color $transition-fast;

  &:hover {
    background: $bg-hover;
    color: $primary;
  }

  &.active {
    background: rgba($primary, 0.08);
    color: $primary;
    font-weight: 500;
  }
}
</style>
