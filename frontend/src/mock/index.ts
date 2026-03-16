// ============================================
// Mock API 拦截器 - 使用 axios adapter 方式
// ============================================
import type { AxiosRequestConfig } from 'axios'
import { mockUsers, mockDepartments, buildDeptTree } from './data'
import { mockTasks, mockProcessRecords } from './taskData'
import type { Department, Task, TaskProcessRecord } from '@/types'
import { calculateLevel, RESPONSE_HOURS, COMPLETION_HOURS } from '@/utils/constants'
import { generateId } from '@/utils/format'
import dayjs from 'dayjs'

// 运行时可变数据(深拷贝)
let tasks = JSON.parse(JSON.stringify(mockTasks)) as Task[]
let records = JSON.parse(JSON.stringify(mockProcessRecords)) as TaskProcessRecord[]
let departments = JSON.parse(JSON.stringify(mockDepartments)) as Department[]

function ok<T>(data: T) {
  return { code: 200, message: 'success', data }
}
function fail(message: string, code = 400) {
  return { code, message, data: null }
}

const strongPasswordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,20}$/

// 当前登录用户（通过token判断）
let currentToken: string | null = null
function getCurrentUser() {
  if (!currentToken) return null
  const uid = currentToken.replace('mock_token_', '')
  return mockUsers.find(u => u.id === uid) || null
}

/** 处理 mock 请求 */
export function handleMockRequest(config: AxiosRequestConfig): any {
  const { url, method, data, params } = config
  const path = url || ''
  const body = typeof data === 'string' ? JSON.parse(data) : data

  // 提取 token
  const authHeader = config.headers?.Authorization as string
  if (authHeader) {
    currentToken = authHeader.replace('Bearer ', '')
  }

  // 延迟模拟
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(matchRoute(path, method || 'get', body, params))
    }, 200 + Math.random() * 300)
  })
}

function matchRoute(path: string, method: string, body: any, params: any): any {
  method = method.toLowerCase()

  // ===== 认证 =====
  if (path === '/auth/login' && method === 'post') {
    const account = String(body.username || '').trim()
    const user = mockUsers.find(u => (u.username === account || u.phone === account) && u.password === body.password)
    if (!user) return fail('用户名或密码错误', 401)
    const { password, ...userInfo } = user
    return ok({ token: `mock_token_${user.id}`, user: userInfo })
  }

  if (path === '/auth/reset-password' && method === 'post') {
    const phone = String(body.phone || '').trim()
    const newPassword = String(body.newPassword || '').trim()
    const user = mockUsers.find(u => u.phone === phone)
    if (!user) return fail('手机号未绑定账号', 404)
    if (!newPassword || newPassword.length < 4) return fail('新密码不少于4位', 400)
    user.password = newPassword
    user.updatedAt = dayjs().toISOString()
    return ok(true)
  }

  if (path === '/auth/userinfo' && method === 'get') {
    const user = getCurrentUser()
    if (!user) return fail('未登录', 401)
    const { password, ...userInfo } = user
    return ok(userInfo)
  }

  // ===== 事务 =====
  if (path === '/task' && method === 'post') {
    const user = getCurrentUser()
    if (!user) return fail('未登录', 401)
    const executor = mockUsers.find(u => u.id === body.executorId)
    if (!executor) return fail('执行人不存在')
    // 根据紧急性类型映射为 importance/urgency/level
    const urgencyMap: Record<string, { importance: 1|2|3; urgency: 1|2|3 }> = {
      important_urgent: { importance: 3, urgency: 3 },
      important_not_urgent: { importance: 3, urgency: 1 },
      not_important_urgent: { importance: 1, urgency: 3 },
      not_important_not_urgent: { importance: 1, urgency: 1 },
    }
    const mapped = urgencyMap[body.urgencyType] || { importance: 1, urgency: 1 }
    const level = calculateLevel(mapped.importance, mapped.urgency)
    const now = dayjs().toISOString()
    // 优先使用传入的完成时间；兼容旧版 duration + durationUnit
    const completionFromBody = String(body.completionDeadline || '').trim()
    const completionDeadline = completionFromBody && dayjs(completionFromBody).isValid()
      ? dayjs(completionFromBody).toISOString()
      : (() => {
          const unitMap: Record<string, any> = { minute: 'minute', hour: 'hour', day: 'day', month: 'month', year: 'year' }
          const deadlineUnit = unitMap[body.durationUnit] || 'hour'
          return dayjs().add(body.duration || 1, deadlineUnit).toISOString()
        })()
    const newTask: Task = {
      id: generateId('T'),
      title: body.description?.substring(0, 50) || '新事务',
      description: body.description || '',
      assignerId: user.id, assignerName: user.name, assignerDept: user.deptName, assignerPosition: user.position,
      executorId: executor.id, executorName: executor.name, executorDept: executor.deptName,
      importance: mapped.importance, urgency: mapped.urgency, level,
      responseDeadline: dayjs().add(RESPONSE_HOURS[level], 'hour').toISOString(),
      completionDeadline,
      responseTime: null, completionTime: null,
      status: 'pending',
      attachments: body.attachments || [],
      parentTaskId: null, childTaskIds: [],
      createdAt: now, updatedAt: now,
    }
    tasks.unshift(newTask)
    records.push({
      id: generateId('R'), taskId: newTask.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'create', content: `创建并下达事务`, attachments: [],
      createdAt: now,
    })
    return ok(newTask)
  }

  if (path === '/task/list' && method === 'get') {
    const user = getCurrentUser()
    if (!user) return fail('未登录', 401)
    let list = [...tasks]

    const normalizeType = (raw?: string) => {
      if (raw === 'scope' || raw === 'assigned' || raw === 'todo' || raw === 'received' || raw === 'all') return raw
      if (user.role === 'staff') return 'todo'
      if (user.role === 'director') return 'assigned'
      return 'todo'
    }

    const type = normalizeType(params?.type)
    const finishedStatuses = ['completed', 'approved', 'rejected', 'cancelled']
    const todoExecutionStatuses = ['pending', 'accepted', 'in_progress', 'overdue']

    const managedDeptIds = new Set(user.managedDeptIds || [])
    const managedUserIds = new Set(
      mockUsers
        .filter(u => managedDeptIds.has(u.deptId) && u.role !== 'admin')
        .map(u => u.id),
    )
    managedUserIds.add(user.id)

    const getRootTask = (task: Task) => {
      let cursor: Task | undefined = task
      while (cursor?.parentTaskId) {
        cursor = tasks.find(t => t.id === cursor!.parentTaskId)
      }
      return cursor || task
    }

    if (type === 'assigned') {
      // 我的下达：仅自己新建并下发的根任务（不含转发形成的子任务）
      list = list.filter(t => t.assignerId === user.id && !t.parentTaskId)
    } else if (type === 'received') {
      // 兼容旧接口：我接收的事务
      list = list.filter(t => t.executorId === user.id)
    } else if (type === 'all') {
      // 管理员场景：全部事务
      list = [...tasks]
    } else if (type === 'todo') {
      // 我的代办：按角色区分待处理集合
      if (user.role === 'director') {
        // 下级提交给总经办待裁决
        list = list.filter(t => t.assignerId === user.id && t.status === 'submitted')
      } else if (user.role === 'manager') {
        // 经理代办 = 上级给我的待执行 + 下级提交给我的待裁决
        const superiorAssigned = list.filter(
          t => t.executorId === user.id && t.assignerId !== user.id && todoExecutionStatuses.includes(t.status),
        )
        const subordinateSubmitted = list.filter(
          // 仅审核自己直接创建并下发的任务提交；转发上级任务形成的子任务不进审核池
          t => t.assignerId === user.id && t.status === 'submitted' && !t.parentTaskId,
        )
        const unionIds = new Set([...superiorAssigned, ...subordinateSubmitted].map(t => t.id))
        list = list.filter(t => unionIds.has(t.id))
      } else {
        // 员工代办 = 上级给我的待执行
        list = list.filter(
          t => t.executorId === user.id && todoExecutionStatuses.includes(t.status),
        )
      }
    } else if (type === 'scope') {
      // 事务列表：按角色的可视范围查询（以查看跟踪为主）
      if (user.role === 'director') {
        // 总经办：自己 + 下级链路的下发任务
        list = list.filter(t => managedUserIds.has(t.assignerId))
      } else if (user.role === 'manager') {
        // 经理：自己及下级下发 + 上级下发给我
        list = list.filter(
          t => managedUserIds.has(t.assignerId) || (t.executorId === user.id && t.assignerId !== user.id),
        )
      } else {
        // 员工：仅看上级下发给我的任务
        list = list.filter(t => t.executorId === user.id)
      }

      // 父子任务链在事务列表中归并为一条（取根任务）
      if (user.role !== 'staff') {
        const merged = new Map<string, Task>()
        for (const t of list) {
          const root = getRootTask(t)
          merged.set(root.id, root)
        }
        list = Array.from(merged.values())
      }
    }

    if (params?.status) {
      if (params.status === 'finished') {
        list = list.filter(t => finishedStatuses.includes(t.status))
      } else if (params.status === 'unfinished') {
        list = list.filter(t => !finishedStatuses.includes(t.status))
      } else {
        list = list.filter(t => t.status === params.status)
      }
    }
    if (params?.level) list = list.filter(t => t.level === params.level)
    if (params?.keyword) {
      const kw = params.keyword.toLowerCase()
      list = list.filter(t => t.title.toLowerCase().includes(kw) || t.description?.toLowerCase().includes(kw))
    }
    const page = Number(params?.page) || 1
    const pageSize = Number(params?.pageSize) || 20
    const total = list.length
    const start = (page - 1) * pageSize
    return ok({ list: list.slice(start, start + pageSize), total, page, pageSize })
  }

  // 事务详情
  const taskDetailMatch = path.match(/^\/task\/([^/]+)$/)
  if (taskDetailMatch && method === 'get') {
    const task = tasks.find(t => t.id === taskDetailMatch[1])
    if (!task) return fail('事务不存在', 404)
    // 收集当前任务的处理记录
    let taskRecords = records.filter(r => r.taskId === task.id)
    // 如果有子任务，把子任务的 submit 记录也带上（普通员工提交内容透传给上级）
    if (task.childTaskIds.length > 0) {
      const childSubmits = records.filter(
        r => task.childTaskIds.includes(r.taskId) && r.action === 'submit'
      )
      taskRecords = [...taskRecords, ...childSubmits]
    }
    // 如果是子任务，把父任务的 reject 记录也带上（上级驳回原因透传给下级）
    if (task.parentTaskId) {
      const parentRejects = records.filter(
        r => r.taskId === task.parentTaskId && r.action === 'reject'
      )
      taskRecords = [...taskRecords, ...parentRejects]
    }
    taskRecords.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    return ok({ task, processRecords: taskRecords })
  }

  // 接收事务
  const acceptMatch = path.match(/^\/task\/([^/]+)\/accept$/)
  if (acceptMatch && method === 'put') {
    const task = tasks.find(t => t.id === acceptMatch[1])
    if (!task) return fail('事务不存在', 404)
    const now = dayjs().toISOString()
    task.status = 'accepted'
    task.responseTime = now
    task.updatedAt = now
    const user = getCurrentUser()!
    records.push({
      id: generateId('R'), taskId: task.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'accept', content: '已接收事务', attachments: [],
      createdAt: now,
    })
    return ok(task)
  }

  // 处理记录
  const processMatch = path.match(/^\/task\/([^/]+)\/process$/)
  if (processMatch && method === 'put') {
    const task = tasks.find(t => t.id === processMatch[1])
    if (!task) return fail('事务不存在', 404)
    const now = dayjs().toISOString()
    task.status = 'in_progress'
    task.updatedAt = now
    const user = getCurrentUser()!
    const record: TaskProcessRecord = {
      id: generateId('R'), taskId: task.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'process', content: body.content, attachments: body.attachments || [],
      createdAt: now,
    }
    records.push(record)
    return ok(record)
  }

  // 提交结果
  const submitMatch = path.match(/^\/task\/([^/]+)\/submit$/)
  if (submitMatch && method === 'put') {
    const task = tasks.find(t => t.id === submitMatch[1])
    if (!task) return fail('事务不存在', 404)
    const now = dayjs().toISOString()
    task.status = 'submitted'
    task.completionTime = now
    task.updatedAt = now
    const user = getCurrentUser()!
    records.push({
      id: generateId('R'), taskId: task.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'submit', content: body.content, attachments: body.attachments || [],
      createdAt: now,
    })
    return ok(task)
  }

  // 审核通过
  const approveMatch = path.match(/^\/task\/([^/]+)\/approve$/)
  if (approveMatch && method === 'put') {
    const task = tasks.find(t => t.id === approveMatch[1])
    if (!task) return fail('事务不存在', 404)
    const now = dayjs().toISOString()
    task.status = 'completed'
    task.completionTime = task.completionTime || now
    task.updatedAt = now
    const user = getCurrentUser()!
    records.push({
      id: generateId('R'), taskId: task.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'approve', content: body.comment || '审核通过', attachments: [],
      createdAt: now,
    })

    // 级联通过所有子任务，确保下游列表与详情状态联动为“已完成/已通过”
    const cascadeApprove = (parentTaskIds: string[]) => {
      for (const cid of parentTaskIds) {
        const child = tasks.find(t => t.id === cid)
        if (child && !['completed', 'approved', 'cancelled'].includes(child.status)) {
          child.status = 'completed'
          child.completionTime = child.completionTime || now
          child.updatedAt = now
          records.push({
            id: generateId('R'), taskId: child.id,
            operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
            action: 'approve', content: '上级审核通过', attachments: [],
            createdAt: now,
          })
          if (child.childTaskIds.length > 0) cascadeApprove(child.childTaskIds)
        }
      }
    }
    if (task.childTaskIds.length > 0) cascadeApprove(task.childTaskIds)
    return ok(task)
  }

  // 审核驳回
  const rejectMatch = path.match(/^\/task\/([^/]+)\/reject$/)
  if (rejectMatch && method === 'put') {
    const task = tasks.find(t => t.id === rejectMatch[1])
    if (!task) return fail('事务不存在', 404)
    const now = dayjs().toISOString()
    task.status = 'rejected'
    task.completionTime = null
    task.updatedAt = now
    const user = getCurrentUser()!
    const rejectContent = `驳回原因：${body.reason || body.comment}`
    records.push({
      id: generateId('R'), taskId: task.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'reject', content: rejectContent, attachments: [],
      createdAt: now,
    })
    // 级联驳回所有子任务
    const cascadeReject = (parentTaskIds: string[]) => {
      for (const cid of parentTaskIds) {
        const child = tasks.find(t => t.id === cid)
        if (child && !['completed', 'approved', 'rejected', 'cancelled'].includes(child.status)) {
          child.status = 'rejected'
          child.completionTime = null
          child.updatedAt = now
          records.push({
            id: generateId('R'), taskId: child.id,
            operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
            action: 'reject', content: rejectContent + '（上级驳回）', attachments: [],
            createdAt: now,
          })
          if (child.childTaskIds.length > 0) cascadeReject(child.childTaskIds)
        }
      }
    }
    if (task.childTaskIds.length > 0) cascadeReject(task.childTaskIds)
    return ok(task)
  }

  // 向下分派
  const reassignMatch = path.match(/^\/task\/([^/]+)\/reassign$/)
  if (reassignMatch && method === 'post') {
    const parentTask = tasks.find(t => t.id === reassignMatch[1])
    if (!parentTask) return fail('父事务不存在', 404)
    const user = getCurrentUser()!
    const executor = mockUsers.find(u => u.id === body.executorId)
    if (!executor) return fail('执行人不存在')
    const now = dayjs().toISOString()
    const childTask: Task = {
      id: generateId('T'),
      title: parentTask.title, description: parentTask.description,
      assignerId: user.id, assignerName: user.name, assignerDept: user.deptName, assignerPosition: user.position,
      executorId: executor.id, executorName: executor.name, executorDept: executor.deptName,
      importance: parentTask.importance, urgency: parentTask.urgency, level: parentTask.level,
      responseDeadline: parentTask.responseDeadline,
      completionDeadline: parentTask.completionDeadline,
      responseTime: null, completionTime: null,
      status: 'pending', attachments: [],
      parentTaskId: parentTask.id, childTaskIds: [],
      createdAt: now, updatedAt: now,
    }
    tasks.unshift(childTask)
    parentTask.childTaskIds.push(childTask.id)
    records.push({
      id: generateId('R'), taskId: parentTask.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'reassign', content: `已分派给${executor.name}处理`,
      attachments: [], createdAt: now,
    })
    return ok(childTask)
  }

  // 取消事务
  const cancelMatch = path.match(/^\/task\/([^/]+)\/cancel$/)
  if (cancelMatch && method === 'put') {
    const task = tasks.find(t => t.id === cancelMatch[1])
    if (!task) return fail('事务不存在', 404)
    const now = dayjs().toISOString()
    task.status = 'cancelled'
    task.updatedAt = now
    const user = getCurrentUser()!
    records.push({
      id: generateId('R'), taskId: task.id,
      operatorId: user.id, operatorName: user.name, operatorAvatar: user.avatar,
      action: 'cancel', content: `取消原因：${body.reason}`, attachments: [],
      createdAt: now,
    })
    return ok(task)
  }

  // ===== 组织架构 =====
  if (path === '/org/tree' && method === 'get') {
    return ok(buildDeptTree(departments))
  }

  if (path === '/dept/list' && method === 'get') {
    let list = [...departments]
    if (params?.parentId) list = list.filter(d => d.parentId === params.parentId)
    return ok(list)
  }

  if (path === '/dept' && method === 'post') {
    const now = dayjs().toISOString()
    const parent = body.parentId ? departments.find(d => d.id === body.parentId) : null
    const leader = body.leaderId ? mockUsers.find(u => u.id === body.leaderId) : null
    const newDept: Department = {
      id: generateId('D'),
      name: body.name,
      parentId: body.parentId || null,
      leaderId: body.leaderId || '',
      leaderName: leader?.name || parent?.leaderName || '',
      sort: Number(body.sort) || 0,
      level: parent ? parent.level + 1 : 0,
      memberCount: 0,
      status: body.status ?? 1,
      createdAt: now,
      updatedAt: now,
    }
    departments.push(newDept)
    return ok(newDept)
  }

  const deptUpdateMatch = path.match(/^\/dept\/([^/]+)$/)
  if (deptUpdateMatch && method === 'put') {
    const dept = departments.find(d => d.id === deptUpdateMatch[1])
    if (!dept) return fail('部门不存在', 404)
    if (typeof body.name === 'string') dept.name = body.name
    if (typeof body.sort !== 'undefined') dept.sort = Number(body.sort) || 0
    if (typeof body.status !== 'undefined') dept.status = body.status
    dept.updatedAt = dayjs().toISOString()
    return ok(dept)
  }

  const deptDeleteMatch = path.match(/^\/dept\/([^/]+)$/)
  if (deptDeleteMatch && method === 'delete') {
    const index = departments.findIndex(d => d.id === deptDeleteMatch[1])
    if (index < 0) return fail('部门不存在', 404)

    if (!departments[index].parentId) {
      return fail('顶级部门不允许删除', 400)
    }

    const targetId = departments[index].id
    const idsToDelete = new Set<string>()
    const collect = (deptId: string) => {
      idsToDelete.add(deptId)
      departments
        .filter(d => d.parentId === deptId)
        .forEach(child => collect(child.id))
    }
    collect(targetId)

    departments = departments.filter(d => !idsToDelete.has(d.id))
    return ok(true)
  }

  // ===== 人员 =====
  if (path === '/member/list' && method === 'get') {
    let list = mockUsers.map(({ password, ...u }) => u)
    if (params?.deptId) list = list.filter(u => u.deptId === params.deptId)
    if (params?.keyword) {
      const kw = params.keyword.toLowerCase()
      list = list.filter(u => u.name.includes(kw) || u.phone?.includes(kw))
    }
    if (params?.position) {
      const pos = String(params.position).toLowerCase()
      list = list.filter(u => u.position.toLowerCase().includes(pos))
    }
    const page = Number(params?.page) || 1
    const pageSize = Number(params?.pageSize) || 20
    const total = list.length
    const start = (page - 1) * pageSize
    return ok({ list: list.slice(start, start + pageSize), total, page, pageSize })
  }

  if (path === '/member' && method === 'post') {
    const username = String(body.username || '').trim()
    const phone = String(body.phone || '').trim()
    const newPassword = String(body.password || '').trim()
    if (!username) return fail('账号不能为空')
    if (!phone) return fail('手机号不能为空')
    if (!/^1[3-9]\d{9}$/.test(phone)) return fail('手机号格式错误')
    if (!strongPasswordPattern.test(newPassword)) {
      return fail('密码需8-20位，含大小写字母、数字和特殊字符')
    }
    if (mockUsers.some(u => u.username === username)) return fail('账号已存在')
    if (mockUsers.some(u => u.phone === phone)) return fail('手机号已存在')
    if (body.role === 'manager' && body.deptId) {
      const deptManagerExists = mockUsers.some(u => u.deptId === body.deptId && u.role === 'manager')
      if (deptManagerExists) return fail('同一部门只能设置一名部门经理')
    }

    const now = dayjs().toISOString()
    const dept = departments.find(d => d.id === body.deptId)
    const newUser = {
      id: generateId('U'),
      username,
      password: newPassword,
      name: body.name,
      role: body.role,
      deptId: body.deptId,
      deptName: dept?.name || '',
      managedDeptIds: body.role === 'manager' && body.deptId ? [body.deptId] : [],
      position: body.position,
      phone,
      email: body.email,
      avatar: '',
      status: body.status ?? 1,
      createdAt: now,
      updatedAt: now,
    }
    mockUsers.push(newUser)
    const { password, ...safeUser } = newUser
    return ok(safeUser)
  }

  const memberUpdateMatch = path.match(/^\/member\/([^/]+)$/)
  if (memberUpdateMatch && method === 'put') {
    const member = mockUsers.find(u => u.id === memberUpdateMatch[1])
    if (!member) return fail('成员不存在', 404)

    const nextPhone = typeof body.phone === 'undefined' ? member.phone : String(body.phone || '').trim()
    if (!nextPhone) return fail('手机号不能为空')
    if (!/^1[3-9]\d{9}$/.test(nextPhone)) return fail('手机号格式错误')
    const phoneDuplicated = mockUsers.some(u => u.id !== member.id && u.phone === nextPhone)
    if (phoneDuplicated) return fail('手机号已存在')

    const nextRole = body.role ?? member.role
    const nextDeptId = body.deptId ?? member.deptId
    if (nextRole === 'manager' && nextDeptId) {
      const deptManagerExists = mockUsers.some(u => u.id !== member.id && u.deptId === nextDeptId && u.role === 'manager')
      if (deptManagerExists) return fail('同一部门只能设置一名部门经理')
    }

    if (body.password) {
      const nextPassword = String(body.password || '').trim()
      if (!strongPasswordPattern.test(nextPassword)) {
        return fail('密码需8-20位，含大小写字母、数字和特殊字符')
      }
      member.password = nextPassword
    }

    member.username = body.username ?? member.username
    member.name = body.name ?? member.name
    member.role = nextRole
    member.deptId = nextDeptId
    member.position = body.position ?? member.position
    member.phone = nextPhone
    member.email = body.email ?? member.email
    if (typeof body.status !== 'undefined') member.status = body.status

    const dept = departments.find(d => d.id === member.deptId)
    member.deptName = dept?.name || member.deptName
    member.managedDeptIds = member.role === 'manager' && member.deptId ? [member.deptId] : []
    member.updatedAt = dayjs().toISOString()

    const { password, ...safeUser } = member
    return ok(safeUser)
  }

  const memberDeleteMatch = path.match(/^\/member\/([^/]+)$/)
  if (memberDeleteMatch && method === 'delete') {
    const index = mockUsers.findIndex(u => u.id === memberDeleteMatch[1])
    if (index < 0) return fail('成员不存在', 404)
    mockUsers.splice(index, 1)
    return ok(true)
  }

  if (path === '/member/selectable' && method === 'get') {
    const user = getCurrentUser()
    if (!user) return fail('未登录', 401)
    const childDepts = departments.filter(d => d.parentId === user.deptId)
    let list
    if (params?.scope === 'subordinates') {
      // director: 仅可选下一级部门中级管理者；manager: 仅可选本部门普通员工
      if (user.role === 'manager') {
        list = mockUsers
          .filter(u => u.deptId === user.deptId && u.id !== user.id && u.role === 'staff')
          .map(({ password, ...u }) => u)
      } else {
        const childDeptIds = childDepts.map(d => d.id)
        list = mockUsers
          .filter(u => childDeptIds.includes(u.deptId) && u.id !== user.id && u.role === 'manager')
          .map(({ password, ...u }) => u)
      }
    } else {
      // 只返回直接下级：子部门负责人
      const subordinateIds = childDepts.map(d => d.leaderId)
      list = mockUsers
        .filter(u => subordinateIds.includes(u.id))
        .map(({ password, ...u }) => u)
    }
    return ok(list)
  }

  // ===== 统计 =====
  if (path === '/stats/overview' && method === 'get') {
    const user = getCurrentUser()
    const role = user?.role || 'director'

    if (role === 'director') {
      // 陈志远：待处理/已完成/未完成/已逾期 (基于 assigned 的任务)
      const myTasks = tasks.filter(t => t.assignerId === user!.id && !t.parentTaskId)
      const total = myTasks.length
      const pending = myTasks.filter(t => !['completed', 'approved', 'rejected', 'cancelled', 'overdue'].includes(t.status)).length
      const completed = myTasks.filter(t => ['completed', 'approved'].includes(t.status)).length
      // 未完成：不通过 + 进行中 + 已逾期（不含已取消）
      const unfinished = myTasks.filter(t => !['completed', 'approved', 'cancelled'].includes(t.status)).length
      const overdue = myTasks.filter(t => t.status === 'overdue').length
      return ok({
        totalTasks: total, pendingTasks: pending, inProgressTasks: unfinished,
        completedTasks: completed, overdueTasks: overdue,
        overdueRate: total > 0 ? Math.round(overdue / total * 100) : 0,
        completionRate: total > 0 ? Math.round(completed / total * 100) : 0,
        avgResponseHours: 2.8, avgCompletionHours: 12.5,
      })
    } else {
      // 王建华/黄晓龙：已完成/未完成 (基于 received 的任务)
      const myTasks = tasks.filter(t => t.executorId === user!.id)
      const total = myTasks.length
      const approved = myTasks.filter(t => ['completed', 'approved'].includes(t.status)).length
      const unfinished = myTasks.filter(t => !['completed', 'approved', 'cancelled'].includes(t.status)).length
      return ok({
        totalTasks: total, pendingTasks: unfinished, inProgressTasks: unfinished,
        completedTasks: approved, overdueTasks: 0,
        overdueRate: 0,
        completionRate: total > 0 ? Math.round(approved / total * 100) : 0,
        avgResponseHours: 2.8, avgCompletionHours: 12.5,
      })
    }
  }

  // 最近动态（角色专属）
  if (path === '/stats/recent-activities' && method === 'get') {
    const user = getCurrentUser()
    const role = user?.role || 'director'
    let activities: { id: string; type: string; content: string; time: string }[] = []

    const directSubordinateIds = mockDepartments
      .filter(d => d.parentId === user?.deptId)
      .map(d => d.leaderId)
      .filter(Boolean)

    const myDept = mockDepartments.find(d => d.id === user?.deptId)
    const parentDept = myDept?.parentId ? mockDepartments.find(d => d.id === myDept.parentId) : null
    const directSuperiorId = parentDept?.leaderId || ''

    if (role === 'director') {
      // 陈志远：直属下级谁提交
      const myTasks = tasks.filter(t => t.assignerId === user!.id && !t.parentTaskId)
      activities = records
        .filter(r => {
          if (r.action !== 'submit') return false
          const task = myTasks.find(t => t.id === r.taskId)
          return !!task && directSubordinateIds.includes(r.operatorId)
        })
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 6)
        .map(r => {
          const task = myTasks.find(t => t.id === r.taskId)
          return {
            id: r.id,
            type: 'submit',
            content: `${r.operatorName} 已提交「${(task?.description || task?.title || '').substring(0, 15)}...」`,
            time: r.createdAt,
          }
        })
    } else if (role === 'manager') {
      // 王建华：直属上级下发给你 + 直属下级提交给你
      const receivedTasks = tasks.filter(t => t.executorId === user!.id)
      const assignRecords = records
        .filter(r => {
          if (!(r.action === 'create' || r.action === 'assign')) return false
          const task = receivedTasks.find(t => t.id === r.taskId)
          return !!task && r.operatorId === directSuperiorId
        })
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 4)
        .map(r => {
          const task = receivedTasks.find(t => t.id === r.taskId)
          return {
            id: r.id,
            type: 'assign',
            content: `${r.operatorName} 下发了「${(task?.description || task?.title || '').substring(0, 15)}...」`,
            time: r.createdAt,
          }
        })
      const assignedTasks = tasks.filter(t => t.assignerId === user!.id && directSubordinateIds.includes(t.executorId))
      const subSubmitRecords = records
        .filter(r => r.action === 'submit' && assignedTasks.some(t => t.id === r.taskId) && directSubordinateIds.includes(r.operatorId))
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 4)
        .map(r => {
          const task = assignedTasks.find(t => t.id === r.taskId)
          return {
            id: r.id,
            type: 'submit',
            content: `${r.operatorName} 提交了「${(task?.description || task?.title || '').substring(0, 15)}...」`,
            time: r.createdAt,
          }
        })
      activities = [...assignRecords, ...subSubmitRecords]
        .sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
        .slice(0, 6)
    } else {
      // 黄晓龙（staff）：直属上级下发给你的任务
      const receivedTasks = tasks.filter(t => t.executorId === user!.id)
      activities = records
        .filter(r => {
          if (!(r.action === 'create' || r.action === 'assign')) return false
          const task = receivedTasks.find(t => t.id === r.taskId)
          return !!task && r.operatorId === directSuperiorId
        })
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 6)
        .map(r => {
          const task = receivedTasks.find(t => t.id === r.taskId)
          return {
            id: r.id,
            type: 'assign',
            content: `${r.operatorName} 下发了「${(task?.description || task?.title || '').substring(0, 15)}...」`,
            time: r.createdAt,
          }
        })
    }

    return ok(activities)
  }

  if (path === '/stats/by-dept' && method === 'get') {
    const deptStats = departments.filter(d => d.level >= 1).map(d => {
      const deptTasks = tasks.filter(t => {
        const executor = mockUsers.find(u => u.id === t.executorId)
        return executor?.deptId === d.id
      })
      return {
        deptId: d.id, deptName: d.name, level: d.level,
        total: deptTasks.length,
        completed: deptTasks.filter(t => ['completed', 'approved'].includes(t.status)).length,
        overdue: deptTasks.filter(t => t.status === 'overdue').length,
        avgHours: Math.round(Math.random() * 20 + 5),
      }
    })
    return ok(deptStats)
  }

  if (path === '/stats/by-level' && method === 'get') {
    const levels = ['A', 'B', 'C', 'D'] as const
    const levelStats = levels.map(level => ({
      level,
      total: tasks.filter(t => t.level === level).length,
      completed: tasks.filter(t => t.level === level && ['completed', 'approved'].includes(t.status)).length,
      avgHours: { A: 3.5, B: 7.2, C: 18.6, D: 35.1 }[level],
    }))
    return ok(levelStats)
  }

  if (path === '/stats/by-person' && method === 'get') {
    const personStats = mockUsers.filter(u => u.role !== 'admin').map(u => {
      const dept = departments.find(d => d.id === u.deptId)
      const userTasks = tasks.filter(t => t.executorId === u.id)
      const completedCount = userTasks.filter(t => ['completed', 'approved'].includes(t.status)).length
      const overdueCount = userTasks.filter(t => t.status === 'overdue').length
      return {
        userId: u.id,
        name: u.name,
        deptId: u.deptId,
        deptName: dept?.name || '未知部门',
        totalTasks: userTasks.length,
        completedTasks: completedCount,
        overdueTasks: overdueCount,
        completionRate: userTasks.length > 0 ? Math.round(completedCount / userTasks.length * 100) : 0,
        avgResponseHours: +(Math.random() * 5 + 0.5).toFixed(1),
        avgCompletionHours: +(Math.random() * 20 + 2).toFixed(1),
      }
    })
    return ok(personStats)
  }

  if (path === '/stats/trend' && method === 'get') {
    const days = params?.range === 'week' ? 7 : params?.range === 'quarter' ? 90 : 30
    const dates: string[] = []
    const created: number[] = []
    const completed: number[] = []
    for (let i = days - 1; i >= 0; i--) {
      dates.push(dayjs().subtract(i, 'day').format('MM-DD'))
      created.push(Math.floor(Math.random() * 5 + 1))
      completed.push(Math.floor(Math.random() * 4))
    }
    return ok({ dates, created, completed })
  }

  // ===== 文件上传 =====
  if (path === '/upload' && method === 'post') {
    return ok({
      id: generateId('F'),
      name: 'uploaded-file',
      url: `/files/${generateId('f')}`,
      size: Math.floor(Math.random() * 5000000),
      type: 'application/octet-stream',
    })
  }

  return fail('接口不存在', 404)
}
