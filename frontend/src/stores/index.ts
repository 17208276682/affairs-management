import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

export default pinia

export { useUserStore } from './user'
export { useTaskStore } from './task'
export { useOrganizationStore } from './organization'
export { useNotificationStore } from './notification'
