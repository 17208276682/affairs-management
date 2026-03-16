<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-copy">
          <h1>
            <span>中小企业</span>
            <span>事务管理数智化系统</span>
          </h1>
        </div>

        <div class="brand-scene" aria-hidden="true">
          <div class="scene-glow glow-a"></div>
          <div class="scene-glow glow-b"></div>
          <div class="scene-grid"></div>
          <div class="scene-card scene-card-main">
            <div class="scene-card-header">
              <span class="scene-pill pill-blue">智能分派</span>
              <span class="scene-pill pill-coral">轻量协同</span>
            </div>
            <div class="scene-bars">
              <span style="--h: 42%"></span>
              <span style="--h: 72%"></span>
              <span style="--h: 54%"></span>
              <span style="--h: 86%"></span>
            </div>
            <div class="scene-lines">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
          <div class="scene-card scene-card-float">
            <div class="orbit orbit-a"></div>
            <div class="orbit orbit-b"></div>
            <div class="signal signal-a"></div>
            <div class="signal signal-b"></div>
          </div>
          <div class="scene-badge badge-top">实时状态</div>
          <div class="scene-badge badge-bottom">流程协同</div>
        </div>
      </section>

      <section class="login-panel">
        <div class="login-card">
          <div class="login-header">
            <div class="brand-mark">
              <el-icon :size="26" color="#4D7CFE"><Monitor /></el-icon>
            </div>
            <div>
              <h2 class="login-title">欢迎登录</h2>
              <p class="login-subtitle">Task Management System</p>
            </div>
          </div>

          <el-form
            v-if="!forgotVisible"
            ref="formRef"
            :model="form"
            :rules="formRules"
            size="large"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名或手机号"
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

            <el-form-item prop="captcha">
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

          <div v-if="!forgotVisible" class="demo-accounts">
            <div class="demo-title">
              <span>演示账号</span>
              <small>点击自动填充</small>
            </div>

            <div class="account-list">
              <div
                v-for="account in demoAccounts"
                :key="account.username"
                class="account-item"
                @click="fillAccount(account)"
              >
                <div class="account-meta">
                  <el-tag :type="account.tagType" size="small" effect="light" round>
                    {{ account.role }}
                  </el-tag>
                  <span class="account-name">{{ account.name }}</span>
                </div>
                <span class="account-username">{{ account.username }}</span>
              </div>
            </div>
          </div>

          <el-form v-else ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" size="large" @submit.prevent="handleResetPassword">
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
import { ref, reactive } from 'vue'
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

const demoAccounts = [
  { username: 'zhaoyang', password: '123456', name: '赵阳', role: '管理员', tagType: 'primary' as const },
]

const forgotForm = reactive({
  phone: '',
  newPassword: '',
  confirmPassword: '',
  smsCode: '',
})

const formRules: FormRules = {
  ...loginFormRules,
}

const forgotRules: FormRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确手机号', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 4, message: '密码不少于4位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
}

function generateCaptcha() {
  return Math.random().toString(36).slice(2, 6).toUpperCase()
}

function refreshCaptcha() {
  captchaText.value = generateCaptcha()
  form.captcha = ''
}

function fillAccount(account: typeof demoAccounts[0]) {
  form.username = account.username
  form.password = account.password
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
    ElMessage.success(`欢迎回来，${userStore.userInfo?.name}`)
    // 管理员默认跳转部门管理，其他角色默认跳转事务总览
    const defaultPath = userStore.userInfo?.role === 'admin' ? '/org/dept' : '/dashboard'
    const redirect = (route.query.redirect as string) || defaultPath
    router.push(redirect)
  } catch (e: any) {
    // Error already shown by interceptor
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
  justify-content: space-between;
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

.brand-scene {
  position: relative;
  min-height: 360px;
  border-radius: 28px;
  overflow: hidden;
  background:
    linear-gradient(145deg, rgba(235, 245, 255, 0.95), rgba(255, 244, 239, 0.92)),
    linear-gradient(180deg, #fefefe 0%, #eef4ff 100%);
  border: 1px solid rgba(196, 213, 239, 0.6);
}

.scene-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(111, 150, 214, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(111, 150, 214, 0.08) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.9), transparent 88%);
}

.scene-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(18px);
  opacity: 0.85;
}

.glow-a {
  width: 180px;
  height: 180px;
  top: 32px;
  left: 32px;
  background: rgba(134, 175, 255, 0.4);
}

.glow-b {
  width: 220px;
  height: 220px;
  right: 30px;
  bottom: 24px;
  background: rgba(255, 190, 156, 0.4);
}

.scene-card {
  position: absolute;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 18px 40px rgba(142, 166, 196, 0.18);
  backdrop-filter: blur(16px);
}

.scene-card-main {
  left: 56px;
  right: 160px;
  top: 58px;
  bottom: 54px;
  padding: 26px;
}

.scene-card-header {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
}

.scene-pill {
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.pill-blue {
  color: #3768e5;
  background: rgba(77, 124, 254, 0.12);
}

.pill-coral {
  color: #d96c4f;
  background: rgba(255, 154, 126, 0.15);
}

.scene-bars {
  height: 148px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  align-items: end;
  gap: 14px;
  margin-bottom: 24px;

  span {
    height: var(--h);
    border-radius: 18px 18px 8px 8px;
    background: linear-gradient(180deg, #6f9cff 0%, #92d5ff 100%);
    animation: rise-in 0.9s ease both;
  }

  span:nth-child(2) {
    background: linear-gradient(180deg, #ffb49d 0%, #ffd2ab 100%);
    animation-delay: 0.08s;
  }

  span:nth-child(3) {
    animation-delay: 0.16s;
  }

  span:nth-child(4) {
    background: linear-gradient(180deg, #8fd3c9 0%, #b7ead7 100%);
    animation-delay: 0.24s;
  }
}

.scene-lines {
  display: grid;
  gap: 12px;

  span {
    height: 10px;
    border-radius: 999px;
    background: linear-gradient(90deg, rgba(89, 130, 223, 0.2), rgba(89, 130, 223, 0.05));
  }

  span:nth-child(1) { width: 82%; }
  span:nth-child(2) { width: 68%; }
  span:nth-child(3) { width: 54%; }
}

.scene-card-float {
  width: 190px;
  height: 190px;
  right: 44px;
  top: 110px;
}

.orbit {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(93, 136, 226, 0.18);
}

.orbit-a {
  inset: 24px;
}

.orbit-b {
  inset: 52px;
}

.signal {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(180deg, #7eb2ff 0%, #4e7ef4 100%);
  box-shadow: 0 0 0 8px rgba(110, 155, 255, 0.12);
}

.signal-a {
  top: 36px;
  left: 92px;
}

.signal-b {
  right: 42px;
  bottom: 48px;
  background: linear-gradient(180deg, #ffba9e 0%, #ff8c7f 100%);
  box-shadow: 0 0 0 8px rgba(255, 177, 157, 0.15);
}

.scene-badge {
  position: absolute;
  padding: 10px 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: #44617f;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.95);
}

.badge-top {
  top: 28px;
  right: 30px;
}

.badge-bottom {
  left: 34px;
  bottom: 26px;
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

.demo-accounts {
  margin-top: 8px;

  .demo-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    color: #5f6f86;
    margin-bottom: 12px;

    small {
      color: #9cabc0;
      font-size: 12px;
    }
  }

  .account-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .account-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border-radius: 16px;
    background: linear-gradient(135deg, #fbfdff 0%, #f6f9ff 100%);
    border: 1px solid #edf1f7;
    cursor: pointer;
    transition: transform $transition-fast, box-shadow $transition-fast, border-color $transition-fast;

    &:hover {
      transform: translateY(-2px);
      border-color: #d8e3fb;
      box-shadow: 0 10px 24px rgba(150, 173, 209, 0.14);
    }

    .account-meta {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .account-name {
      font-size: $font-size-sm;
      color: #24324a;
      font-weight: 500;
    }

    .account-username {
      font-size: $font-size-xs;
      color: #7d8ca3;
      font-weight: 600;
      letter-spacing: 0.04em;
    }
  }
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

  .brand-scene {
    min-height: 260px;
  }

  .scene-card-main {
    left: 20px;
    right: 96px;
    top: 24px;
    bottom: 24px;
    padding: 18px;
  }

  .scene-card-float {
    width: 110px;
    height: 110px;
    right: 18px;
    top: 74px;
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
