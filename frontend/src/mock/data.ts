// ============================================
// Mock 数据 - 用户与部门
// ============================================
import type { User, Department } from '@/types'

/** 部门数据 */
export const mockDepartments: Department[] = [
  {
    id: 'D001', name: 'xxx公司', parentId: null, leaderId: 'U001', leaderName: '陈志远',
    sort: 1, level: 0, memberCount: 8, status: 1,
    createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'D002', name: '总经办', parentId: 'D001', leaderId: 'U001', leaderName: '陈志远',
    sort: 1, level: 1, memberCount: 2, status: 1,
    createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'D003', name: '技术研发部', parentId: 'D002', leaderId: 'U002', leaderName: '王建华',
    sort: 1, level: 2, memberCount: 4, status: 1,
    createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'D004', name: '财务部', parentId: 'D002', leaderId: 'U005', leaderName: '王成',
    sort: 2, level: 2, memberCount: 1, status: 1,
    createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'D005', name: '人力资源部', parentId: 'D002', leaderId: 'U006', leaderName: '王芳',
    sort: 3, level: 2, memberCount: 1, status: 1,
    createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
]

/** 用户数据 */
export const mockUsers: (User & { password: string })[] = [
  {
    id: 'U001', username: 'admin', password: 'admin123', name: '陈志远',
    avatar: '', phone: '13800000001', email: 'chen@startech.com',
    deptId: 'D002', deptName: '总经办', position: '总经办负责人', role: 'director',
    managedDeptIds: ['D001', 'D002', 'D003', 'D004', 'D005'],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U002', username: 'wangjh', password: '123456', name: '王建华',
    avatar: '', phone: '13800000002', email: 'wang@startech.com',
    deptId: 'D003', deptName: '技术研发部', position: '技术研发部负责人', role: 'manager',
    managedDeptIds: ['D003'],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U003', username: 'zhangsan', password: '123456', name: '张三',
    avatar: '', phone: '13800000003', email: 'li@startech.com',
    deptId: 'D003', deptName: '技术研发部', position: '工程师', role: 'staff',
    managedDeptIds: [],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U004', username: 'lisi', password: '123456', name: '李四',
    avatar: '', phone: '13800000004', email: 'zhang@startech.com',
    deptId: 'D003', deptName: '技术研发部', position: '工程师', role: 'staff',
    managedDeptIds: [],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U005', username: 'wangcheng', password: '123456', name: '王成',
    avatar: '', phone: '13800000005', email: 'lium@startech.com',
    deptId: 'D004', deptName: '财务部', position: '财务部负责人', role: 'manager',
    managedDeptIds: ['D004'],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U006', username: 'zhaoyang', password: '123456', name: '赵阳',
    avatar: '', phone: '13800000006', email: 'zhao@startech.com',
    deptId: 'D002', deptName: '总经办', position: '系统管理员', role: 'admin',
    managedDeptIds: ['D001', 'D002', 'D003', 'D004', 'D005'],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U007', username: 'wangfang', password: '123456', name: '王芳',
    avatar: '', phone: '13800000007', email: 'wangfang@startech.com',
    deptId: 'D005', deptName: '人力资源部', position: '人力资源部负责人', role: 'manager',
    managedDeptIds: ['D005'],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'U009', username: 'huangxl', password: '123456', name: '黄晓龙',
    avatar: '', phone: '13800000009', email: 'huang@startech.com',
    deptId: 'D003', deptName: '技术研发部', position: '工程师', role: 'staff',
    managedDeptIds: [],
    status: 1, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-01-01T00:00:00Z',
  },
]

/** 构建部门树 */
export function buildDeptTree(depts: Department[], parentId: string | null = null): (Department & { children?: Department[] })[] {
  return depts
    .filter(d => d.parentId === parentId)
    .sort((a, b) => a.sort - b.sort)
    .map(d => ({
      ...d,
      children: buildDeptTree(depts, d.id),
    }))
}
