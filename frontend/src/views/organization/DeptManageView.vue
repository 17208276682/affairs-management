<template>
  <div class="dept-manage">
    <div class="page-header">
      <h3>组织管理</h3>
    </div>

    <el-row :gutter="16" class="content-row" v-loading="loading">
      <el-col :span="7">
        <el-card class="tree-panel">
          <template #header>
            <div class="panel-header">
              <span class="panel-title">部门结构</span>
              <el-button type="primary" size="small" :disabled="!selectedDeptId" @click="handleAddMember">
                新增人员
              </el-button>
            </div>
          </template>
          <el-tree
            class="no-collapse-tree"
            :data="deptOnlyTree"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
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
            <div class="info-item"><span>负责人</span><strong>{{ displayLeaderName }}</strong></div>
            <div class="info-item"><span>成员数</span><strong>{{ displayMemberCount }}</strong></div>
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
          <div class="table-scroll">
            <el-table :data="childrenDepts" size="small" stripe max-height="220" class="inner-table">
              <el-table-column label="部门名称" prop="name" min-width="180" />
              <el-table-column label="负责人" prop="leaderName" width="140" />
              <el-table-column label="人数" prop="memberCount" width="100" align="center" />
              <el-table-column label="层级" prop="level" width="100" align="center" />
            </el-table>
          </div>

          <el-divider />
          <div class="child-title">本部门成员</div>
          <div class="table-scroll">
            <el-table :data="deptMembers" size="small" stripe max-height="260" class="inner-table">
              <el-table-column label="姓名" prop="name" min-width="120" />
              <el-table-column label="部门" prop="deptName" min-width="160" />
              <el-table-column label="职位" prop="position" min-width="140" />
              <el-table-column label="手机号" prop="phone" min-width="140" />
              <el-table-column label="邮箱" prop="email" min-width="220" />
              <el-table-column label="角色" min-width="220" align="center">
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
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="handleEditMember(row)">编辑</el-button>
                  <el-button link type="danger" @click="handleDeleteMemberConfirm(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
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

    <el-dialog v-model="showMemberForm" :title="memberFormTitle" width="520px" destroy-on-close>
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberEditRules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item v-if="!memberForm.id || memberForm.needResetPassword" label="密码" prop="password">
          <div class="password-row">
            <el-input v-model="memberForm.password" type="password" show-password placeholder="请输入强密码" />
            <el-button @click="generateAndFillPassword">随机生成</el-button>
          </div>
        </el-form-item>
        <el-form-item v-if="memberForm.id" label="重置密码">
          <el-switch v-model="memberForm.needResetPassword" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-tree-select
            v-model="memberForm.deptId"
            :data="deptOnlyTree"
            :props="treeNodeProps"
            placeholder="选择部门"
            check-strictly
            node-key="id"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="memberForm.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="memberForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="memberForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="memberForm.role" style="width: 100%">
            <el-option v-for="option in roleOptionsForSelectedDept" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="showTopRoleManagedDept" label="兼任负责人">
          <el-select
            v-model="memberForm.managedDeptIds"
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
        <el-button @click="showMemberForm = false">取消</el-button>
        <el-button type="primary" :loading="memberSaving" @click="saveMember">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useOrganizationStore } from '@/stores'
import { getMemberListApi } from '@/api/organization'
import { deptFormRules, memberFormRules } from '@/utils/validate'
import type { Department, OrgTreeNode, User } from '@/types'
import { ROLE_MAP } from '@/utils/constants'

const orgStore = useOrganizationStore()
const loading = ref(false)
const showForm = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const selectedDeptId = ref('')
const deptMembers = ref<User[]>([])
const showMemberForm = ref(false)
const memberSaving = ref(false)
const memberFormRef = ref<FormInstance>()
const treeNodeProps = { label: 'label', children: 'children', value: 'id' }

const memberForm = ref({
  id: '',
  name: '',
  password: '',
  deptId: '',
  position: '',
  phone: '',
  email: '',
  role: 'staff',
  managedDeptIds: [] as string[],
  needResetPassword: false,
})
const memberEditRules = memberFormRules

const form = ref({ id: '', name: '', parentId: '', parentName: '无（顶级部门）' })
const formTitle = computed(() => form.value.id ? '编辑部门' : '新增部门')
const memberFormTitle = computed(() => memberForm.value.id ? '编辑成员' : '新增成员')

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

const deptNameMap = computed<Record<string, string>>(() => {
  return orgStore.deptList.reduce((acc, dept) => {
    acc[dept.id] = dept.name
    return acc
  }, {} as Record<string, string>)
})

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

watch(() => memberForm.value.deptId, (newDeptId) => {
  if (!newDeptId) return
  const options = getRoleOptionsByDeptId(newDeptId)
  if (!options.some(option => option.value === memberForm.value.role)) {
    memberForm.value.role = options[0]?.value || 'staff'
  }
})

watch(() => memberForm.value.role, (newRole) => {
  if (newRole !== 'ceo' && newRole !== 'director') {
    memberForm.value.managedDeptIds = []
  }
})

const roleOptionsForSelectedDept = computed(() => getRoleOptionsByDeptId(memberForm.value.deptId))

const showTopRoleManagedDept = computed(() => memberForm.value.role === 'ceo' || memberForm.value.role === 'director')

const secondLevelDeptOptions = computed(() => {
  return orgStore.deptList
    .filter(dept => dept.level === 1)
    .map(dept => ({ label: dept.name, value: dept.id }))
})

// 顶级节点：负责人显示CEO名字；非顶级：显示该部门负责人（leaderName）
const displayLeaderName = computed(() => {
  if (!selectedDept.value) return '未设置'
  if (selectedDept.value.level === 0) {
    // 顶级节点：从orgTree中找到CEO用户名
    const findCeoInTree = (nodes: OrgTreeNode[]): string | null => {
      for (const node of nodes) {
        if (node.type === 'member' && node.id) {
          // 检查是否在顶级部门中
          const topDeptId = selectedDept.value!.id
          if (node.parentId === topDeptId) return node.label
        }
        if (node.children) {
          const found = findCeoInTree(node.children)
          if (found) return found
        }
      }
      return null
    }
    return findCeoInTree(orgStore.orgTree) || selectedDept.value.leaderName || '未设置'
  }
  return selectedDept.value.leaderName || '未设置'
})

// 顶级节点：成员数显示所有人的数量；非顶级：显示部门人员数量
const displayMemberCount = computed(() => {
  if (!selectedDept.value) return 0
  return selectedDept.value.memberCount
})

function handleDeptClick(row: OrgTreeNode) {
  if (row.type !== 'dept') return
  selectedDeptId.value = row.id
  loadSelectedDeptMembers()
}

async function loadSelectedDeptMembers() {
  if (!selectedDeptId.value) {
    deptMembers.value = []
    return
  }
  const [deptRes, allRes] = await Promise.all([
    getMemberListApi({ deptId: selectedDeptId.value, page: 1, pageSize: 999 }),
    getMemberListApi({ page: 1, pageSize: 9999 }),
  ])

  const baseMembers = deptRes.data.list.filter(item => item.role !== 'admin')
  const needAppendManagedTopRoles = selectedDept.value?.level === 1
  const extraManagedTopRoles = needAppendManagedTopRoles ? allRes.data.list.filter(item => {
    if (item.role !== 'ceo' && item.role !== 'director') return false
    if (!item.managedDeptIds?.length) return false
    if (!item.managedDeptIds.includes(selectedDeptId.value)) return false
    return !baseMembers.some(base => base.id === item.id)
  }) : []

  deptMembers.value = [...baseMembers, ...extraManagedTopRoles]
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
    await loadSelectedDeptMembers()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

function handleEditMember(row: User) {
  memberForm.value = {
    id: row.id,
    name: row.name || '',
    password: '',
    deptId: row.deptId || selectedDeptId.value,
    position: row.position || '',
    phone: row.phone || '',
    email: row.email || '',
    role: row.role || 'staff',
    managedDeptIds: row.managedDeptIds || [],
    needResetPassword: false,
  }
  showMemberForm.value = true
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
  memberForm.value.password = generateStrongPassword()
}

async function validateManagerLimit() {
  if (memberForm.value.role !== 'manager' || !memberForm.value.deptId) return
  const res = await getMemberListApi({ deptId: memberForm.value.deptId, page: 1, pageSize: 999 })
  const hasOtherManager = res.data.list.some(m => m.role === 'manager' && m.id !== memberForm.value.id)
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

function handleAddMember() {
  if (!selectedDeptId.value) return
  memberForm.value = {
    id: '',
    name: '',
    password: generateStrongPassword(),
    deptId: selectedDeptId.value,
    position: '',
    phone: '',
    email: '',
    role: 'staff',
    managedDeptIds: [],
    needResetPassword: false,
  }
  const options = getRoleOptionsByDeptId(memberForm.value.deptId)
  if (options.length > 0) {
    memberForm.value.role = options[0].value
  }
  showMemberForm.value = true
}

async function handleDeleteMemberConfirm(id: string) {
  try {
    await ElMessageBox.confirm('确认删除该人员？', '删除确认', {
      type: 'warning',
      center: true,
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await orgStore.deleteMember(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch {
    // 用户取消
  }
}

async function saveMember() {
  if (!memberFormRef.value) return
  await memberFormRef.value.validate()
  try {
    await validateManagerLimit()
  } catch (e: any) {
    ElMessage.warning(e?.message || '校验失败')
    return
  }
  memberSaving.value = true
  try {
    const payload: any = {
      name: memberForm.value.name,
      password: (!memberForm.value.id || memberForm.value.needResetPassword) ? memberForm.value.password : undefined,
      deptId: memberForm.value.deptId,
      position: memberForm.value.position,
      phone: memberForm.value.phone,
      email: memberForm.value.email,
      role: memberForm.value.role,
      managedDeptIds: showTopRoleManagedDept.value ? memberForm.value.managedDeptIds : [],
    }
    if (memberForm.value.id) {
      await orgStore.updateMember(memberForm.value.id, payload)
    } else {
      await orgStore.createMember(payload)
    }
    ElMessage.success('保存成功')
    showMemberForm.value = false
    await loadData()
  } finally {
    memberSaving.value = false
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
    await loadSelectedDeptMembers()
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
    await loadSelectedDeptMembers()
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

.table-scroll {
  width: 100%;
  overflow: auto;
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

.password-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

:deep(.inner-table) {
  min-width: 980px;
}

:deep(.no-collapse-tree .el-tree-node__expand-icon) {
  visibility: hidden;
  pointer-events: none;
}
</style>
