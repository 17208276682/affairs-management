<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-copy">
          <h1>
            <span>中小企业</span>
            <span>事务管理数智化系统</span>
          </h1>
          <div class="brand-keywords">
            <span class="keyword-tag">轻量易用</span>
            <span class="keyword-tag">组织管理</span>
            <span class="keyword-tag">高效协同</span>
            <span class="keyword-tag">数据可视</span>
            <span class="keyword-tag">及时触达</span>
          </div>
        </div>

        <div class="brand-illustrations">
          <!-- 卡片1: 协作平台 -->
          <div class="illus-card illus-card--hero">
            <div class="illus-hero-bg">
              <div class="illus-hero-title">协作平台</div>
              <div class="illus-hero-sub">协作进度</div>
              <div class="illus-progress">
                <div class="illus-progress-bar"></div>
              </div>
              <div class="illus-hero-shapes">
                <div class="illus-cube"></div>
                <div class="illus-ring"></div>
              </div>
            </div>
          </div>
          <!-- 卡片2: 组织管理 -->
          <div class="illus-card illus-card--org">
            <div class="illus-org-scene">
              <div class="illus-platform"></div>
              <div class="illus-doc-card">
                <div class="illus-avatar-circle"></div>
                <div class="illus-doc-lines">
                  <span></span><span></span><span></span>
                </div>
              </div>
              <div class="illus-node illus-node--a"></div>
              <div class="illus-node illus-node--b"></div>
            </div>
          </div>
          <!-- 卡片3: 高效协同 -->
          <div class="illus-card illus-card--chart-line">
            <div class="illus-chart-header">
              <span class="illus-chart-title">高效协同</span>
            </div>
            <svg class="illus-line-svg" viewBox="0 0 280 100" preserveAspectRatio="none">
              <defs>
                <linearGradient id="lineGrad" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stop-color="rgba(77,124,254,0.25)" />
                  <stop offset="100%" stop-color="rgba(77,124,254,0)" />
                </linearGradient>
              </defs>
              <path d="M0,70 Q30,60 60,45 T120,35 T180,20 T240,30 T280,25" fill="none" stroke="#4D7CFE" stroke-width="2.5"/>
              <path d="M0,70 Q30,60 60,45 T120,35 T180,20 T240,30 T280,25 L280,100 L0,100 Z" fill="url(#lineGrad)"/>
            </svg>
          </div>
          <!-- 卡片4: 数据可视 -->
          <div class="illus-card illus-card--chart-bar">
            <div class="illus-chart-header">
              <span class="illus-chart-title">数据可视</span>
            </div>
            <div class="illus-bars">
              <div class="illus-bar-group" v-for="h in [55, 70, 48, 82, 62, 75, 45, 68]" :key="h">
                <span class="illus-bar" :style="{ height: h + '%' }"></span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="login-panel">
        <div class="login-card">

          <el-form
            v-if="!forgotVisible"
            ref="formRef"
            :model="form"
            :rules="formRules"
            size="large"
            @submit.prevent="handleLogin"
          >
            <div class="login-header">
              <div class="brand-mark">
                <el-icon :size="26" color="#4D7CFE"><Monitor /></el-icon>
              </div>
              <div>
                <h2 class="login-title">欢迎登录</h2>
                <p class="login-subtitle">Task Management System</p>
              </div>
            </div>
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入手机号"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <!-- 验证码功能暂时隐藏 -->
            <el-form-item v-if="false" prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="form.captcha"
                  placeholder="请输入验证码"
                  clearable
                />
                <button type="button" class="captcha-code" @click="refreshCaptcha">{{ captchaText }}</button>
              </div>
            </el-form-item>

            <el-form-item>
              <div class="login-options">
                <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
                <el-button text type="primary" @click="forgotVisible = true">忘记密码</el-button>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <el-form v-else ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" size="large" @submit.prevent="handleResetPassword">
            <div class="login-header" style="margin-bottom: 20px;">
              <div class="brand-mark">
                <el-icon :size="26" color="#4D7CFE"><Lock /></el-icon>
              </div>
              <div>
                <h2 class="login-title">密码修改</h2>
                <p class="login-subtitle">Reset Password</p>
              </div>
            </div>
            <el-form-item prop="phone">
              <el-input v-model="forgotForm.phone" placeholder="请输入手机号" clearable />
            </el-form-item>
            <el-form-item prop="newPassword">
              <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="forgotForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="captcha-row">
                <el-input v-model="forgotForm.smsCode" placeholder="请输入短信验证码" clearable />
                <el-button @click="sendFakeSms">获取验证码</el-button>
              </div>
            </el-form-item>
            <div class="sms-hint" v-if="smsHint">{{ smsHint }}</div>
            <el-form-item>
              <div class="forgot-actions">
                <el-button @click="closeForgot">返回登录</el-button>
                <el-button type="primary" :loading="resetting" @click="handleResetPassword">确认重置</el-button>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </section>
    </div>

    <footer class="login-footer">
      Copyright ©2026 国信金宏信息咨询有限公司 All Rights Reserved
    </footer>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores'
import { loginFormRules } from '@/utils/validate'
import { resetPasswordApi } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const forgotFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const forgotVisible = ref(false)
const resetting = ref(false)
const captchaText = ref(generateCaptcha())
const fakeSmsCode = ref('')
const smsHint = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: '',
})

const forgotForm = reactive({
  phone: '',
  newPassword: '',
  confirmPassword: '',
  smsCode: '',
})

const formRules: FormRules = {
  ...loginFormRules,
  // 验证码规则暂时禁用
  // captcha: [
  //   { required: true, message: '请输入验证码', trigger: 'blur' },
  //   {
  //     validator: (_rule: any, value: string, callback: any) => {
  //       if (value && value.toUpperCase() !== captchaText.value.toUpperCase()) {
  //         callback(new Error('验证码不正确'))
  //       } else {
  //         callback()
  //       }
  //     },
  //     trigger: 'blur',
  //   },
  // ],
}

const strongPasswordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,20}$/

const forgotRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确手机号', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) { callback(new Error('请输入新密码')); return }
        if (!strongPasswordPattern.test(value)) {
          callback(new Error('密码需 8-20 位，含大小写字母、数字和特殊字符'))
        } else { callback() }
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value && value !== forgotForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

function generateCaptcha() {
  return Math.random().toString(36).slice(2, 6).toUpperCase()
}

function refreshCaptcha() {
  captchaText.value = generateCaptcha()
  form.captcha = ''
}

function sendFakeSms() {
  if (!/^1[3-9]\d{9}$/.test(forgotForm.phone)) {
    ElMessage.warning('请先输入正确手机号')
    return
  }
  fakeSmsCode.value = Math.floor(100000 + Math.random() * 900000).toString()
  smsHint.value = `模拟短信验证码：${fakeSmsCode.value}`
  ElMessage.success('短信验证码已发送（模拟）')
}

// 记住密码功能
const REMEMBER_KEY = 'tm_remember'

function loadRemembered() {
  try {
    const saved = localStorage.getItem(REMEMBER_KEY)
    if (saved) {
      const { username, password } = JSON.parse(saved)
      form.username = username || ''
      form.password = password || ''
      rememberMe.value = true
    }
  } catch { /* ignore */ }
}

function saveRemembered() {
  if (rememberMe.value) {
    localStorage.setItem(REMEMBER_KEY, JSON.stringify({ username: form.username, password: form.password }))
  } else {
    localStorage.removeItem(REMEMBER_KEY)
  }
}

onMounted(() => {
  loadRemembered()
})

function closeForgot() {
  forgotVisible.value = false
  forgotForm.phone = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  forgotForm.smsCode = ''
  fakeSmsCode.value = ''
  smsHint.value = ''
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate()

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    saveRemembered()
    ElMessage.success(`欢迎回来，${userStore.userInfo?.name}`)
    // 管理员/CEO默认跳转部门管理，其他角色默认跳转事务总览
    const role = userStore.userInfo?.role
    const defaultPath = (role === 'admin' || role === 'ceo') ? '/org/dept' : '/dashboard'
    const redirect = (route.query.redirect as string) || defaultPath
    router.push(redirect)
  } catch (e: any) {
    // captcha refresh disabled
  } finally {
    loading.value = false
  }
}

async function handleResetPassword() {
  if (!forgotFormRef.value) return
  await forgotFormRef.value.validate()
  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }
  if (!fakeSmsCode.value || forgotForm.smsCode.trim() !== fakeSmsCode.value) {
    ElMessage.warning('短信验证码错误')
    return
  }
  resetting.value = true
  try {
    await resetPasswordApi({ phone: forgotForm.phone, newPassword: forgotForm.newPassword })
    ElMessage.success('密码重置成功，请使用新密码登录')
    closeForgot()
  } finally {
    resetting.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  padding: 28px;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top left, rgba(143, 197, 255, 0.35), transparent 30%),
    radial-gradient(circle at bottom right, rgba(255, 207, 182, 0.45), transparent 28%),
    linear-gradient(135deg, #f8fbff 0%, #fef7f1 52%, #f5f8ff 100%);
}

.login-shell {
  flex: 1;
  min-height: calc(100vh - 130px);
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(380px, 460px);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 32px;
  box-shadow: 0 26px 80px rgba(126, 152, 197, 0.16);
  overflow: hidden;
  backdrop-filter: blur(16px);
}

.brand-panel {
  position: relative;
  padding: 56px 54px 44px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.92), rgba(245, 250, 255, 0.78)),
    linear-gradient(135deg, #edf5ff 0%, #fff7f3 100%);
}

.brand-copy {
  max-width: 520px;
  position: relative;
  z-index: 1;

  h1 {
    font-size: clamp(34px, 4vw, 52px);
    line-height: 1.15;
    color: #20304d;
    margin-bottom: 18px;

    span {
      display: block;
    }
  }
}

.brand-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;

  .keyword-tag {
    padding: 6px 16px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 600;
    color: #3768e5;
    background: rgba(77, 124, 254, 0.10);
    border: 1px solid rgba(77, 124, 254, 0.18);
    letter-spacing: 1px;
  }
}

.brand-illustrations {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 14px;
  flex: 1;
  min-height: 300px;
}

.illus-card {
  border-radius: 20px;
  overflow: hidden;
  position: relative;
}

/* ---- Card 1: Hero / 协作平台 ---- */
.illus-card--hero {
  background: linear-gradient(135deg, #4D7CFE 0%, #5B9BFF 100%);
}

.illus-hero-bg {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.illus-hero-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.illus-hero-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
}

.illus-progress {
  width: 65%;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.illus-progress-bar {
  width: 68%;
  height: 100%;
  border-radius: 999px;
  background: #fff;
}

.illus-hero-shapes {
  position: absolute;
  right: 16px;
  bottom: 16px;
}

.illus-cube {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
  transform: rotate(-12deg);
}

.illus-ring {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  position: absolute;
  right: -8px;
  top: -18px;
}

/* ---- Card 2: 组织管理 ---- */
.illus-card--org {
  background: linear-gradient(145deg, #f1f5ff 0%, #f8f9ff 100%);
  border: 1px solid rgba(196, 213, 239, 0.5);
}

.illus-org-scene {
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.illus-platform {
  width: 100px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #c8d8f8 0%, #e0ebff 100%);
  position: absolute;
  bottom: 25%;
  left: 50%;
  transform: translateX(-50%);
}

.illus-doc-card {
  width: 80px;
  height: 96px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(100, 130, 180, 0.15);
  padding: 14px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1;
}

.illus-avatar-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a8c4ff 0%, #89b0ff 100%);
}

.illus-doc-lines {
  display: flex;
  flex-direction: column;
  gap: 5px;
  width: 100%;
  span {
    height: 6px;
    border-radius: 999px;
    background: #e4ecf8;
    &:nth-child(1) { width: 90%; }
    &:nth-child(2) { width: 70%; }
    &:nth-child(3) { width: 55%; }
  }
}

.illus-node {
  position: absolute;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  &--a {
    background: #4D7CFE;
    top: 22%;
    right: 20%;
    box-shadow: 0 0 0 6px rgba(77, 124, 254, 0.12);
  }
  &--b {
    background: #6DD5C8;
    bottom: 20%;
    left: 18%;
    box-shadow: 0 0 0 6px rgba(109, 213, 200, 0.15);
  }
}

/* ---- Card 3: 高效协同 (line chart) ---- */
.illus-card--chart-line {
  background: #fff;
  border: 1px solid rgba(196, 213, 239, 0.5);
  display: flex;
  flex-direction: column;
}

.illus-chart-header {
  padding: 16px 18px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.illus-chart-title {
  font-size: 16px;
  font-weight: 700;
  color: #20304d;
}

.illus-line-svg {
  flex: 1;
  width: 100%;
  min-height: 0;
}

/* ---- Card 4: 数据可视 (bar chart) ---- */
.illus-card--chart-bar {
  background: #fff;
  border: 1px solid rgba(196, 213, 239, 0.5);
  display: flex;
  flex-direction: column;
}

.illus-bars {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  align-items: end;
  gap: 8px;
  padding: 0 18px 16px;
}

.illus-bar-group {
  height: 100%;
  display: flex;
  align-items: end;
}

.illus-bar {
  width: 100%;
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, #4D7CFE 0%, #7EB2FF 100%);
  animation: rise-in 0.8s ease both;
  &:nth-child(odd) { opacity: 0.8; }
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(230, 236, 245, 0.95);
  border-radius: 28px;
  padding: 34px 32px 28px;
  box-shadow: 0 20px 60px rgba(127, 149, 183, 0.16);

  .login-header {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 30px;

    .brand-mark {
      width: 52px;
      height: 52px;
      border-radius: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(145deg, #eef4ff 0%, #fff4ee 100%);
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
    }

    .login-title {
      font-size: 28px;
      font-weight: 700;
      color: #22324e;
      margin: 0;
    }

    .login-subtitle {
      margin-top: 4px;
      font-size: $font-size-sm;
      color: #8c98aa;
      letter-spacing: 0.08em;
    }
  }

  .login-options {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .login-btn {
    width: 100%;
    height: 50px;
    font-size: $font-size-md;
    font-weight: 600;
    border: none;
    border-radius: 16px;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #6d95ff 0%, #78c1ff 100%);
    box-shadow: 0 14px 28px rgba(111, 149, 255, 0.28);
  }
}

.captcha-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 112px;
  gap: 10px;
}

.captcha-code {
  border: 1px solid #d9e3f2;
  border-radius: 12px;
  background: linear-gradient(135deg, #eef4ff 0%, #fff5ef 100%);
  color: #3f5f94;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
}

.forgot-actions {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.sms-hint {
  margin-top: -8px;
  margin-bottom: 10px;
  font-size: 12px;
  color: #58708e;
}

.login-footer {
  margin-top: 12px;
  text-align: center;
  color: #6d7d92;
  font-size: 13px;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(24px) scaleY(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scaleY(1);
  }
}

@media (max-width: 1100px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    min-height: 420px;
    padding-bottom: 24px;
  }

  .brand-illustrations {
    min-height: 240px;
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    min-height: auto;
    border-radius: 24px;
  }

  .brand-panel {
    padding: 28px 22px 18px;
  }

  .brand-copy {
    h1 {
      font-size: 30px;
    }

    p {
      font-size: 14px;
      line-height: 1.75;
    }
  }

  .brand-illustrations {
    min-height: 200px;
    gap: 10px;
  }

  .login-panel {
    padding: 18px;
  }

  .login-card {
    padding: 26px 20px 20px;
  }

  .login-card .login-header {
    .brand-mark {
      width: 46px;
      height: 46px;
    }

    .login-title {
      font-size: 24px;
    }
  }

  .login-card .login-options {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .demo-accounts .account-item {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

}
</style>
