// ============================================
// 组织架构 API
// ============================================
import { get, post, put, del } from './request'
import type { Department, User, OrgTreeNode, PaginatedData, DeptFormData, MemberListParams } from '@/types'

/** 获取组织架构树 */
export function getOrgTreeApi() {
  return get<OrgTreeNode[]>('/org/tree')
}

/** 获取部门列表 */
export function getDeptListApi(parentId?: string) {
  return get<Department[]>('/dept/list', { parentId })
}

/** 新增部门 */
export function createDeptApi(data: DeptFormData) {
  return post<Department>('/dept', data)
}

/** 编辑部门 */
export function updateDeptApi(id: string, data: Partial<DeptFormData>) {
  return put<Department>(`/dept/${id}`, data)
}

/** 删除部门 */
export function deleteDeptApi(id: string) {
  return del(`/dept/${id}`)
}

/** 获取人员列表 */
export function getMemberListApi(params: MemberListParams) {
  return get<PaginatedData<User>>('/member/list', params)
}

/** 新增人员 */
export function createMemberApi(data: Partial<User>) {
  return post<User>('/member', data)
}

/** 编辑人员 */
export function updateMemberApi(id: string, data: Partial<User>) {
  return put<User>(`/member/${id}`, data)
}

/** 删除人员 */
export function deleteMemberApi(id: string) {
  return del(`/member/${id}`)
}

/** 获取可选执行人列表 */
export function getSelectableMembersApi(deptIds?: string[], scope?: 'subordinates') {
  return get<User[]>('/member/selectable', { deptIds: deptIds?.join(','), scope })
}

/** 获取下属成员列表（用于下发任务） */
export function getSubordinatesApi() {
  return get<User[]>('/member/selectable', { scope: 'subordinates' })
}
