// ============================================
// 组织架构状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Department, User, OrgTreeNode, MemberListParams } from '@/types'
import {
  getOrgTreeApi, getDeptListApi, createDeptApi, updateDeptApi, deleteDeptApi,
  getMemberListApi, createMemberApi, updateMemberApi, deleteMemberApi,
  getSelectableMembersApi,
} from '@/api/organization'

export const useOrganizationStore = defineStore('organization', () => {
  const orgTree = ref<OrgTreeNode[]>([])
  const deptList = ref<Department[]>([])
  const memberList = ref<User[]>([])
  const memberTotal = ref(0)
  const selectableMembers = ref<User[]>([])
  const loading = ref(false)

  async function fetchOrgTree() {
    loading.value = true
    try {
      const res = await getOrgTreeApi()
      orgTree.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchDeptList(parentId?: string) {
    const res = await getDeptListApi(parentId)
    deptList.value = res.data
  }

  async function createDept(data: any) {
    const res = await createDeptApi(data)
    return res.data
  }

  async function updateDept(id: string, data: any) {
    const res = await updateDeptApi(id, data)
    return res.data
  }

  async function deleteDept(id: string) {
    await deleteDeptApi(id)
  }

  async function fetchMemberList(params: MemberListParams) {
    loading.value = true
    try {
      const res = await getMemberListApi(params)
      const visibleMembers = res.data.list.filter(item => item.role !== 'admin')
      memberList.value = visibleMembers
      memberTotal.value = visibleMembers.length
    } finally {
      loading.value = false
    }
  }

  async function createMember(data: Partial<User>) {
    const res = await createMemberApi(data)
    return res.data
  }

  async function updateMember(id: string, data: Partial<User>) {
    const res = await updateMemberApi(id, data)
    return res.data
  }

  async function deleteMember(id: string) {
    await deleteMemberApi(id)
  }

  async function fetchSelectableMembers(deptIds?: string[], scope?: 'subordinates') {
    const res = await getSelectableMembersApi(deptIds, scope)
    const visibleMembers = res.data.filter(item => item.role !== 'admin')
    selectableMembers.value = visibleMembers
    return visibleMembers
  }

  return {
    orgTree, deptList, memberList, memberTotal, selectableMembers, loading,
    fetchOrgTree, fetchDeptList, createDept, updateDept, deleteDept,
    fetchMemberList, createMember, updateMember, deleteMember, fetchSelectableMembers,
  }
})
