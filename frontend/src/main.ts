import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import 'nprogress/nprogress.css'
import '@/styles/main.scss'
import App from './App.vue'
import router from './router'
import { setupI18n } from './i18n'
import { setupAxios } from './utils/axios'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// Pinia
app.use(createPinia())

// Element Plus
app.use(ElementPlus, {
  locale: zhCn,
})

// 国际化
setupI18n(app)

// 路由
app.use(router)

// Axios 配置
setupAxios()

app.mount('#app')


