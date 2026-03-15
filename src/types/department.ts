// ============================================
// 部门 & 组织架构 相关类型
// ============================================

/** 部门 */
export interface Department {
  id: string
  name: string
  parentId: string | null
  leaderId: string
  leaderName: string
  sort: number
  level: number
  memberCount: number
  status: 0 | 1
  createdAt: string
  updatedAt: string
  children?: Department[]
}

/** 组织架构树节点 */
export interface OrgTreeNode {
  id: string
  label: string
  type: 'dept' | 'member'
  parentId: string | null
  leaderName?: string
  memberCount?: number
  position?: string
  avatar?: string
  children?: OrgTreeNode[]
}

/** 创建/编辑部门请求 */
export interface DeptFormData {
  name: string
  parentId: string | null
  leaderId: string
  sort: number
  status?: 0 | 1
}

/** 人员列表查询参数 */
export interface MemberListParams {
  deptId?: string
  keyword?: string
  position?: string
  page: number
  pageSize: number
}
