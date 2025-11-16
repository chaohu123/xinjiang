<template>
  <div class="main-layout">
    <Header />
    <main class="main-content">
      <router-view />
    </main>
    <Footer />
    <BackToTop />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/layout/Header.vue'
import Footer from '@/components/layout/Footer.vue'
import BackToTop from '@/components/common/BackToTop.vue'
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fafafa;
  position: relative;
}

.main-content {
  flex: 1;
  padding-top: 70px; // Header高度
  position: relative;
  z-index: 1;
}

// 移动端适配
@media (max-width: 768px) {
  .main-content {
    padding-top: 60px; // 移动端Header高度
  }
}
</style>












