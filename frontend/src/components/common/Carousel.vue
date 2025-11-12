<template>
  <div class="carousel-container">
    <el-carousel
      v-if="items.length > 0"
      :interval="interval"
      :height="height"
      indicator-position=""
      arrow="hover"
      class="custom-carousel"
    >
      <el-carousel-item v-for="(item, index) in items" :key="index">
        <div class="carousel-item" :style="{ backgroundImage: `url(${item.image})` }">
          <div v-if="item.title || item.subtitle || item.link" class="carousel-gradient-overlay" />
          <div class="carousel-overlay">
            <div class="carousel-content">
              <h1 v-if="item.title" class="carousel-title">
                {{ item.title }}
              </h1>
              <p v-if="item.subtitle" class="carousel-subtitle">
                {{ item.subtitle }}
              </p>
              <div v-if="item.link" class="carousel-actions">
                <el-button type="primary" size="large" @click="handleClick(item.link)">
                  {{ item.buttonText || $t('home.explore') }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
    <div v-else class="carousel-empty">
      <div class="empty-content">
        <h1 class="hero-title">
          {{ $t('home.title') }}
        </h1>
        <p class="hero-subtitle">
          {{ $t('home.subtitle') }}
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/search')">
            {{ $t('home.explore') }}
          </el-button>
          <el-button size="large" @click="$router.push('/map')">
            <el-icon><MapLocation /></el-icon>
            {{ $t('nav.map') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { MapLocation } from '@element-plus/icons-vue'

export interface CarouselItem {
  id?: number
  image: string
  title?: string
  subtitle?: string
  link?: string
  buttonText?: string
  order?: number
}

interface Props {
  items: CarouselItem[]
  interval?: number
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  interval: 5000,
  height: '500px',
})

const router = useRouter()

const handleClick = (link?: string) => {
  if (link) {
    if (link.startsWith('http')) {
      window.open(link, '_blank')
    } else {
      router.push(link)
    }
  }
}
</script>

<style lang="scss" scoped>
.carousel-container {
  width: 100%;
  margin-bottom: 60px;
  position: relative;
  // 确保容器能够充分利用可用空间
  max-width: 100%;
  box-sizing: border-box;
}

.carousel-item {
  width: 100%;
  height: 100%;
  // 使用 cover 填充整个容器，无留白
  background-size: cover !important;
  background-position: center center !important;
  background-repeat: no-repeat !important;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  // 确保图片能够自适应容器，在大屏幕上也能完整显示
  min-width: 100%;
  min-height: 100%;
  // 移除背景色，让图片填充
  background-color: transparent;
}

.carousel-gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  background: transparent;
  transition: opacity 0.5s ease;
}

.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

.carousel-content {
  text-align: center;
  color: white;
  padding: 0 20px;
  z-index: 2;
}

.carousel-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.carousel-subtitle {
  font-size: 20px;
  margin-bottom: 30px;
  opacity: 0.95;
}

.carousel-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.carousel-empty {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.8) 0%, rgba(118, 75, 162, 0.8) 100%);
  color: white;
}

.empty-content {
  text-align: center;
  padding: 0 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 30px;
  opacity: 0.95;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

@media (max-width: 768px) {
  .carousel-title,
  .hero-title {
    font-size: 32px;
  }

  .carousel-subtitle,
  .hero-subtitle {
    font-size: 16px;
  }
}
</style>
