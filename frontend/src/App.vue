<template>
  <div id="app">
    <router-view v-slot="{ Component, route }">
      <transition :name="route.meta.transition || 'fade'" mode="out-in" :css="true">
        <component :is="Component" v-if="Component" :key="route.path" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onErrorCaptured } from 'vue'
import { useUserStore } from '@/store/user'
import { useI18n } from 'vue-i18n'

const userStore = useUserStore()
const { locale } = useI18n()

// 捕获组件错误，防止整个应用崩溃
onErrorCaptured((err, instance, info) => {
  console.error('Component error:', err, info)
  // 返回 false 阻止错误继续传播
  return false
})

onMounted(() => {
  // 初始化用户信息（从localStorage恢复）
  userStore.initUser().catch(err => {
    console.error('Failed to init user:', err)
  })
  // 恢复语言设置
  const savedLocale = localStorage.getItem('locale')
  if (savedLocale) {
    locale.value = savedLocale
  }
})
</script>

<style lang="scss">
#app {
  min-height: 100vh;
  background: #f5f5f5;
}

// 页面过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from {
  transform: translateX(100%);
}

.slide-leave-to {
  transform: translateX(-100%);
}
</style>
