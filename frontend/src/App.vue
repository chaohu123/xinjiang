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
  background: #fafafa;
  position: relative;
}

// 页面过渡动画 - 淡入淡出
.fade-enter-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 滑动过渡动画
.slide-enter-active {
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94), opacity 0.4s ease;
}

.slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.55, 0.06, 0.68, 0.19), opacity 0.3s ease;
}

.slide-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-30%);
  opacity: 0;
}

// 缩放淡入动画
.scale-enter-active {
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.4s ease;
}

.scale-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.scale-enter-from {
  transform: scale(0.9);
  opacity: 0;
}

.scale-leave-to {
  transform: scale(1.05);
  opacity: 0;
}
</style>
