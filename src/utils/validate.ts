// ============================================
// 表单验证规则
// ============================================
import type { FormRules } from 'element-plus'

const strongPasswordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,20}$/

/** 事务创建表单验证规则 */
export const taskFormRules: FormRules = {
  description: [
    { required: true, message: '请输入事务描述', trigger: 'blur' },
  ],
  urgencyType: [
    { required: true, message: '请选择事务紧急性', trigger: 'change' },
  ],
  completionDeadline: [
    { required: true, message: '请选择完成时间', trigger: 'change' },
  ],
  executorId: [
    { required: true, message: '请选择执行人', trigger: 'change' },
  ],
}

/** 登录表单验证规则 */
export const loginFormRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码不少于4位', trigger: 'blur' },
  ],
}

/** 部门表单验证规则 */
export const deptFormRules: FormRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在2到50个字符之间', trigger: 'blur' },
  ],
  leaderId: [
    { required: true, message: '请选择部门负责人', trigger: 'change' },
  ],
}

/** 人员表单验证规则 */
export const memberFormRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
  ],
  deptId: [
    { required: true, message: '请选择所属部门', trigger: 'change' },
  ],
  position: [
    { required: true, message: '请输入职务', trigger: 'blur' },
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        const val = String(value || '')
        if (!val) {
          callback(new Error('请输入密码'))
          return
        }
        if (!strongPasswordPattern.test(val)) {
          callback(new Error('密码需8-20位，含大小写字母、数字和特殊字符'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}
