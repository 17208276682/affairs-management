<template>
  <div class="member-manage">
    <div class="page-header">
      <h3>人员管理</h3>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增人员
      </el-button>
    </div>

    <el-row :gutter="16" class="content-row">
      <!-- 左侧部门树 -->
      <el-col :span="6">
        <el-card class="tree-panel">
          <template #header><span class="panel-title">部门筛选</span></template>
          <el-tree
            :data="orgStore.orgTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleDeptClick"
          />
        </el-card>
      </el-col>

      <!-- 右侧列表 -->
      <el-col :span="18">
        <el-card v-loading="loading" class="list-panel">
          <div class="filter-row">
            <el-input v-model="nameKeyword" placeholder="搜索姓名" prefix-icon="Search" clearable style="width: 220px" @change="loadMembers" />
            <el-input v-model="positionKeyword" placeholder="搜索职位" prefix-icon="Search" clearable style="width: 220px" @change="loadMembers" />
          </div>
          <div class="table-wrapper">
            <el-table :data="orgStore.memberList" stripe class="member-table" table-layout="auto">
              <el-table-column label="姓名" prop="name" min-width="100" />
              <el-table-column label="账号" prop="username" min-width="140" />
              <el-table-column label="部门" prop="deptName" min-width="180" />
              <el-table-column label="职位" prop="position" min-width="140" />
              <el-table-column label="手机号" prop="phone" min-width="150" />
              <el-table-column label="邮箱" prop="email" min-width="220" />
              <el-table-column label="角色" min-width="120" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.role === 'manager' ? 'warning' : 'info'">
                  {{ ROLE_MAP[row.role] || row.role }}
                </el-tag>
              </template>
              </el-table-column>
              <el-table-column label="操作" min-width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button link type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="page"
              :total="orgStore.memberTotal"
              :page-size="20"
              layout="total, prev, pager, next"
              @current-change="loadMembers"
            />
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
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :disabled="!!form.id" />
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
            :data="orgStore.orgTree"
            :props="{ label: 'name', children: 'children' }"
            placeholder="选择部门"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="form.position" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="普通员工" value="staff" />
            <el-option label="部门经理" value="manager" />
          </el-select>
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
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useOrganizationStore } from '@/stores'
import { ROLE_MAP } from '@/utils/constants'
import { memberFormRules } from '@/utils/validate'
import type { User, OrgTreeNode } from '@/types'
import { getMemberListApi } from '@/api/organization'

const orgStore = useOrganizationStore()
const loading = ref(false)
const showForm = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const nameKeyword = ref('')
const positionKeyword = ref('')
const selectedDeptId = ref('')
const page = ref(1)

const form = ref({
  id: '', name: '', username: '', password: '', deptId: '', position: '',
  phone: '', email: '', role: 'staff', needResetPassword: false,
})

const formTitle = computed(() => form.value.id ? '编辑人员' : '新增人员')

function handleDeptClick(data: OrgTreeNode) {
  selectedDeptId.value = data.id
  page.value = 1
  loadMembers()
}

async function loadMembers() {
  loading.value = true
  try {
    await orgStore.fetchMemberList({
      deptId: selectedDeptId.value || undefined,
      keyword: nameKeyword.value || undefined,
      position: positionKeyword.value || undefined,
      page: page.value,
      pageSize: 20,
    })
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = {
    id: '',
    name: '',
    username: '',
    password: generateStrongPassword(),
    deptId: selectedDeptId.value,
    position: '',
    phone: '',
    email: '',
    role: 'staff',
    needResetPassword: false,
  }
  showForm.value = true
}

function handleEdit(row: User) {
  form.value = {
    id: row.id, name: row.name, username: row.username,
    password: '',
    deptId: row.deptId, position: row.position || '',
    phone: row.phone || '', email: row.email || '', role: row.role, needResetPassword: false,
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
    throw new Error('同一部门只能设置一名部门经理')
  }
}

async function handleDelete(id: string) {
  await orgStore.deleteMember(id)
  ElMessage.success('删除成功')
  loadMembers()
}

async function save() {
  if (!formRef.value) return
  await formRef.value.validate()
  await validateManagerLimit()
  saving.value = true
  try {
    const payload: any = {
      ...form.value,
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
  await orgStore.fetchOrgTree()
  loadMembers()
})
</script>

<style lang="scss" scoped>
.member-manage {
  height: calc(100vh - 112px);
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
  margin-bottom: $spacing-sm;
}

.password-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: $spacing-sm;
}
</style>
