import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import pinia from './stores'
import { useUserStore } from './stores'
import { getToken } from './utils/auth'

import './styles/global.scss'
import './styles/element-override.scss'

async function bootstrap() {
  const app = createApp(App)

  // 注册所有 Element Plus 图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  // 1. 先安装 Pinia（store 依赖）
  app.use(pinia)
  app.use(ElementPlus, { locale: zhCn })

  // 2. 在安装路由之前，预加载用户信息
  //    这样路由守卫中不再需要 await，消除首次导航竞态
  const token = getToken()
  if (token) {
    const userStore = useUserStore()
    try {
      await userStore.getUserInfo()
    } catch {
      userStore.logout()
    }
  }

  // 3. 安装路由（触发初始导航，此时 userInfo 已就绪，守卫同步通过）
  app.use(router)
  await router.isReady()

  app.mount('#app')
}

bootstrap()
