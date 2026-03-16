// ============================================
// Mock 数据 - 事务
// ============================================
import type { Task, TaskProcessRecord } from '@/types'

const CLEAR_ALL_SEEDED_TASKS = true

/** 事务数据 */
const seededTasks: Task[] = [
  {
    id: 'T20260201001', title: 'Q1季度产品规划方案制定',
    description: '请根据公司战略目标，制定Q1季度产品规划方案，涵盖功能迭代计划、技术升级路线和市场投放策略。需要与市场部协调沟通。',
    assignerId: 'U001', assignerName: '陈志远', assignerDept: '星辰科技集团', assignerPosition: '总经理',
    executorId: 'U002', executorName: '王建华', executorDept: '技术研发部',
    importance: 3, urgency: 2, level: 'A',
    responseDeadline: '2026-02-01T10:00:00Z', completionDeadline: '2026-02-05T18:00:00Z',
    responseTime: '2026-02-01T09:30:00Z', completionTime: '2026-02-04T16:00:00Z',
    status: 'completed', attachments: [
      { id: 'F001', name: 'Q1规划草案.docx', url: '/files/q1-plan.docx', size: 2048576, type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadedAt: '2026-02-01T08:00:00Z' }
    ],
    parentTaskId: null, childTaskIds: ['T20260201002'],
    createdAt: '2026-02-01T08:00:00Z', updatedAt: '2026-02-04T16:00:00Z',
  },
  {
    id: 'T20260201002', title: '前端技术选型报告',
    description: '针对新项目进行前端技术选型评估，包括框架对比、性能测试、社区生态分析等。',
    assignerId: 'U002', assignerName: '王建华', assignerDept: '技术研发部', assignerPosition: '技术总监',
    executorId: 'U006', executorName: '赵阳', executorDept: '前端开发组',
    importance: 2, urgency: 2, level: 'B',
    responseDeadline: '2026-02-02T10:00:00Z', completionDeadline: '2026-02-06T18:00:00Z',
    responseTime: '2026-02-02T09:00:00Z', completionTime: null,
    status: 'in_progress', attachments: [],
    parentTaskId: 'T20260201001', childTaskIds: [],
    createdAt: '2026-02-01T14:00:00Z', updatedAt: '2026-02-02T09:00:00Z',
  },
  {
    id: 'T20260210001', title: '品牌推广活动策划书',
    description: '策划春季品牌推广活动，包含线上线下推广方案、预算分配、预期效果评估等。',
    assignerId: 'U003', assignerName: '李雪梅', assignerDept: '市场营销部', assignerPosition: '市场总监',
    executorId: 'U008', executorName: '周婷', executorDept: '品牌推广组',
    importance: 2, urgency: 3, level: 'A',
    responseDeadline: '2026-02-10T11:00:00Z', completionDeadline: '2026-02-14T18:00:00Z',
    responseTime: null, completionTime: null,
    status: 'pending', attachments: [
      { id: 'F002', name: '往年活动参考.pdf', url: '/files/ref.pdf', size: 5120000, type: 'application/pdf', uploadedAt: '2026-02-10T09:00:00Z' }
    ],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-10T09:00:00Z', updatedAt: '2026-02-10T09:00:00Z',
  },
  {
    id: 'T20260215001', title: '年度财务审计准备',
    description: '请准备年度财务审计所需的全部资料，包括各部门费用明细、项目成本核算、税务申报资料等。',
    assignerId: 'U001', assignerName: '陈志远', assignerDept: '星辰科技集团', assignerPosition: '总经理',
    executorId: 'U004', executorName: '张丽华', executorDept: '财务部',
    importance: 3, urgency: 3, level: 'A',
    responseDeadline: '2026-02-15T10:00:00Z', completionDeadline: '2026-02-20T18:00:00Z',
    responseTime: '2026-02-15T09:45:00Z', completionTime: null,
    status: 'in_progress', attachments: [],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-15T08:00:00Z', updatedAt: '2026-02-15T09:45:00Z',
  },
  {
    id: 'T20260218001', title: '新员工入职培训方案',
    description: '制定3月份新员工入职培训方案，包括培训课程安排、导师分配、考核标准等。',
    assignerId: 'U001', assignerName: '陈志远', assignerDept: '星辰科技集团', assignerPosition: '总经理',
    executorId: 'U005', executorName: '刘明', executorDept: '人力资源部',
    importance: 1, urgency: 2, level: 'C',
    responseDeadline: '2026-02-18T14:00:00Z', completionDeadline: '2026-02-25T18:00:00Z',
    responseTime: '2026-02-18T13:00:00Z', completionTime: '2026-02-24T10:00:00Z',
    status: 'submitted', attachments: [
      { id: 'F003', name: '培训方案v1.pptx', url: '/files/training.pptx', size: 3145728, type: 'application/vnd.openxmlformats-officedocument.presentationml.presentation', uploadedAt: '2026-02-24T10:00:00Z' }
    ],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-18T08:00:00Z', updatedAt: '2026-02-24T10:00:00Z',
  },
  {
    id: 'T20260220001', title: '后端API接口文档整理',
    description: '整理并更新所有后端API接口文档，确保每个接口都有详细的请求参数和响应格式说明。使用Swagger或类似工具进行标准化。',
    assignerId: 'U002', assignerName: '王建华', assignerDept: '技术研发部', assignerPosition: '技术总监',
    executorId: 'U007', executorName: '孙伟', executorDept: '后端开发组',
    importance: 2, urgency: 1, level: 'C',
    responseDeadline: '2026-02-20T14:00:00Z', completionDeadline: '2026-02-28T18:00:00Z',
    responseTime: '2026-02-20T10:00:00Z', completionTime: null,
    status: 'accepted', attachments: [],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-20T08:00:00Z', updatedAt: '2026-02-20T10:00:00Z',
  },
  {
    id: 'T20260222001', title: '客户反馈数据分析报告',
    description: '分析近半年客户反馈数据，提炼核心问题点和改进机会，形成可视化分析报告。',
    assignerId: 'U003', assignerName: '李雪梅', assignerDept: '市场营销部', assignerPosition: '市场总监',
    executorId: 'U008', executorName: '周婷', executorDept: '品牌推广组',
    importance: 1, urgency: 1, level: 'D',
    responseDeadline: '2026-02-23T18:00:00Z', completionDeadline: '2026-03-05T18:00:00Z',
    responseTime: '2026-02-23T09:00:00Z', completionTime: null,
    status: 'in_progress', attachments: [],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-22T10:00:00Z', updatedAt: '2026-02-23T09:00:00Z',
  },
  {
    id: 'T20260225001', title: '移动端首页UI改版设计',
    description: '对移动端首页进行UI改版设计，需提供高保真设计稿和交互说明文档。重点优化用户首次打开体验。',
    assignerId: 'U006', assignerName: '赵阳', assignerDept: '前端开发组', assignerPosition: '前端组长',
    executorId: 'U009', executorName: '黄晓龙', executorDept: '前端开发组',
    importance: 2, urgency: 2, level: 'B',
    responseDeadline: '2026-02-25T12:00:00Z', completionDeadline: '2026-03-01T18:00:00Z',
    responseTime: null, completionTime: null,
    status: 'pending', attachments: [],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-25T09:00:00Z', updatedAt: '2026-02-25T09:00:00Z',
  },
  {
    id: 'T20260226001', title: '数据库性能优化方案',
    description: '针对近期系统响应变慢问题，排查数据库慢查询，提出优化方案并执行。',
    assignerId: 'U007', assignerName: '孙伟', assignerDept: '后端开发组', assignerPosition: '后端组长',
    executorId: 'U010', executorName: '吴倩', executorDept: '后端开发组',
    importance: 3, urgency: 3, level: 'A',
    responseDeadline: '2026-02-26T10:00:00Z', completionDeadline: '2026-02-27T18:00:00Z',
    responseTime: '2026-02-26T09:15:00Z', completionTime: '2026-02-27T15:00:00Z',
    status: 'submitted', attachments: [
      { id: 'F004', name: '优化报告.pdf', url: '/files/db-optimize.pdf', size: 1024000, type: 'application/pdf', uploadedAt: '2026-02-27T15:00:00Z' },
      { id: 'F005', name: '性能测试截图.png', url: '/files/perf-test.png', size: 512000, type: 'image/png', uploadedAt: '2026-02-27T15:00:00Z' }
    ],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-26T08:00:00Z', updatedAt: '2026-02-27T15:00:00Z',
  },
  {
    id: 'T20260227001', title: '员工满意度调查问卷设计',
    description: '设计2026年度员工满意度调查问卷，涵盖工作环境、薪酬福利、职业发展等维度。',
    assignerId: 'U001', assignerName: '陈志远', assignerDept: '星辰科技集团', assignerPosition: '总经理',
    executorId: 'U005', executorName: '刘明', executorDept: '人力资源部',
    importance: 1, urgency: 1, level: 'D',
    responseDeadline: '2026-02-28T18:00:00Z', completionDeadline: '2026-03-10T18:00:00Z',
    responseTime: null, completionTime: null,
    status: 'pending', attachments: [],
    parentTaskId: null, childTaskIds: [],
    createdAt: '2026-02-27T16:00:00Z', updatedAt: '2026-02-27T16:00:00Z',
  },
  {
    id: 'T20260305001', title: '系统安全漏洞排查报告',
    description: '请对公司内部系统进行全面安全漏洞排查，包括网络安全、数据安全、权限管理等，并提交排查报告及修复建议。',
    assignerId: 'U001', assignerName: '陈志远', assignerDept: '星辰科技集团', assignerPosition: '总经理',
    executorId: 'U002', executorName: '王建华', executorDept: '技术研发部',
    importance: 3, urgency: 2, level: 'A',
    responseDeadline: '2026-03-05T10:00:00Z', completionDeadline: '2026-03-15T18:00:00Z',
    responseTime: '2026-03-05T09:20:00Z', completionTime: '2026-03-10T14:00:00Z',
    status: 'submitted', attachments: [
      { id: 'F010', name: '安全排查报告.pdf', url: '/files/security-report.pdf', size: 2048000, type: 'application/pdf', uploadedAt: '2026-03-10T14:00:00Z' }
    ],
    parentTaskId: null, childTaskIds: ['T20260305002'],
    createdAt: '2026-03-05T08:00:00Z', updatedAt: '2026-03-10T14:00:00Z',
  },
  {
    id: 'T20260305002', title: '前端安全漏洞检测与修复',
    description: '对前端项目进行XSS、CSRF等安全漏洞检测，完成修复并提交修复报告。',
    assignerId: 'U002', assignerName: '王建华', assignerDept: '技术研发部', assignerPosition: '技术总监',
    executorId: 'U009', executorName: '黄晓龙', executorDept: '前端开发组',
    importance: 3, urgency: 2, level: 'A',
    responseDeadline: '2026-03-06T10:00:00Z', completionDeadline: '2026-03-12T18:00:00Z',
    responseTime: '2026-03-06T09:00:00Z', completionTime: '2026-03-09T16:00:00Z',
    status: 'submitted', attachments: [
      { id: 'F011', name: '前端安全修复报告.docx', url: '/files/frontend-security.docx', size: 1536000, type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadedAt: '2026-03-09T16:00:00Z' }
    ],
    parentTaskId: 'T20260305001', childTaskIds: [],
    createdAt: '2026-03-05T14:00:00Z', updatedAt: '2026-03-09T16:00:00Z',
  },
]

export const mockTasks: Task[] = CLEAR_ALL_SEEDED_TASKS ? [] : seededTasks

/** 事务处理记录 */
const seededProcessRecords: TaskProcessRecord[] = [
  {
    id: 'R001', taskId: 'T20260201001', operatorId: 'U001', operatorName: '陈志远', operatorAvatar: '',
    action: 'create', content: '创建并下达事务：Q1季度产品规划方案制定', attachments: [],
    createdAt: '2026-02-01T08:00:00Z',
  },
  {
    id: 'R002', taskId: 'T20260201001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'accept', content: '已接收事务，将尽快安排处理', attachments: [],
    createdAt: '2026-02-01T09:30:00Z',
  },
  {
    id: 'R003', taskId: 'T20260201001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'process', content: '已完成市场调研部分，正在整理技术升级路线图', attachments: [],
    createdAt: '2026-02-02T14:00:00Z',
  },
  {
    id: 'R004', taskId: 'T20260201001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'reassign', content: '将前端技术选型部分分派给赵阳处理', attachments: [],
    createdAt: '2026-02-01T14:00:00Z',
  },
  {
    id: 'R005', taskId: 'T20260201001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'submit', content: 'Q1规划方案已完成，请查收附件中的最终版本', attachments: [
      { id: 'F001', name: 'Q1规划终版.docx', url: '/files/q1-final.docx', size: 3145728, type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadedAt: '2026-02-04T16:00:00Z' }
    ],
    createdAt: '2026-02-04T16:00:00Z',
  },
  {
    id: 'R006', taskId: 'T20260201001', operatorId: 'U001', operatorName: '陈志远', operatorAvatar: '',
    action: 'approve', content: '方案内容详实，执行计划清晰，审核通过', attachments: [],
    createdAt: '2026-02-04T17:00:00Z',
  },
  {
    id: 'R010', taskId: 'T20260226001', operatorId: 'U007', operatorName: '孙伟', operatorAvatar: '',
    action: 'create', content: '创建并下达事务：数据库性能优化方案', attachments: [],
    createdAt: '2026-02-26T08:00:00Z',
  },
  {
    id: 'R011', taskId: 'T20260226001', operatorId: 'U010', operatorName: '吴倩', operatorAvatar: '',
    action: 'accept', content: '已接收，开始排查慢查询', attachments: [],
    createdAt: '2026-02-26T09:15:00Z',
  },
  {
    id: 'R012', taskId: 'T20260226001', operatorId: 'U010', operatorName: '吴倩', operatorAvatar: '',
    action: 'process', content: '发现3个主要慢查询，已添加索引优化', attachments: [],
    createdAt: '2026-02-26T16:00:00Z',
  },
  {
    id: 'R013', taskId: 'T20260226001', operatorId: 'U010', operatorName: '吴倩', operatorAvatar: '',
    action: 'submit', content: '优化完成，平均查询时间从2.3s降至0.15s，详见优化报告', attachments: [
      { id: 'F004', name: '优化报告.pdf', url: '/files/db-optimize.pdf', size: 1024000, type: 'application/pdf', uploadedAt: '2026-02-27T15:00:00Z' }
    ],
    createdAt: '2026-02-27T15:00:00Z',
  },
  // === T20260305001 流程记录：陈志远→王建华 （王建华已提交，等待陈志远审核） ===
  {
    id: 'R020', taskId: 'T20260305001', operatorId: 'U001', operatorName: '陈志远', operatorAvatar: '',
    action: 'create', content: '创建并下达事务：系统安全漏洞排查报告', attachments: [],
    createdAt: '2026-03-05T08:00:00Z',
  },
  {
    id: 'R021', taskId: 'T20260305001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'accept', content: '已接收，将组织团队进行排查', attachments: [],
    createdAt: '2026-03-05T09:20:00Z',
  },
  {
    id: 'R022', taskId: 'T20260305001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'reassign', content: '将前端安全部分分派给黄晓龙处理', attachments: [],
    createdAt: '2026-03-05T14:00:00Z',
  },
  {
    id: 'R023', taskId: 'T20260305001', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'submit', content: '全部安全排查已完成，共发现高危漏洞3个、中危漏洞7个，均已修复。详见附件报告。', attachments: [
      { id: 'F010', name: '安全排查报告.pdf', url: '/files/security-report.pdf', size: 2048000, type: 'application/pdf', uploadedAt: '2026-03-10T14:00:00Z' }
    ],
    createdAt: '2026-03-10T14:00:00Z',
  },
  // === T20260305002 流程记录：王建华→黄晓龙 （黄晓龙已提交，等待王建华审核） ===
  {
    id: 'R030', taskId: 'T20260305002', operatorId: 'U002', operatorName: '王建华', operatorAvatar: '',
    action: 'create', content: '创建并下达事务：前端安全漏洞检测与修复', attachments: [],
    createdAt: '2026-03-05T14:00:00Z',
  },
  {
    id: 'R031', taskId: 'T20260305002', operatorId: 'U009', operatorName: '黄晓龙', operatorAvatar: '',
    action: 'accept', content: '已接收，开始进行XSS/CSRF漏洞扫描', attachments: [],
    createdAt: '2026-03-06T09:00:00Z',
  },
  {
    id: 'R032', taskId: 'T20260305002', operatorId: 'U009', operatorName: '黄晓龙', operatorAvatar: '',
    action: 'submit', content: '前端安全漏洞检测完成，发现并修复XSS漏洞2个、CSRF风险1个，详见修复报告', attachments: [
      { id: 'F011', name: '前端安全修复报告.docx', url: '/files/frontend-security.docx', size: 1536000, type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', uploadedAt: '2026-03-09T16:00:00Z' }
    ],
    createdAt: '2026-03-09T16:00:00Z',
  },
]

export const mockProcessRecords: TaskProcessRecord[] = CLEAR_ALL_SEEDED_TASKS
  ? []
  : seededProcessRecords.filter(record => mockTasks.some(task => task.id === record.taskId))
