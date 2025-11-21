<template>
  <div class="home">
    <!-- Carousel Section -->
    <div class="home-carousel">
      <Carousel
        v-loading="carouselLoading"
        :loading="carouselLoading"
        :items="carouselItems"
        :height="'50vh'"
      />
    </div>

    <!-- Quick Navigation Section -->
    <section class="quick-nav-section">
      <div class="container">
        <div class="quick-nav-grid">
          <div
            v-for="nav in quickNavItems"
            :key="nav.path"
            class="quick-nav-card"
            @click="$router.push(nav.path)"
          >
            <div class="quick-nav-icon" :style="{ background: nav.color }">
              <el-icon :size="32">
                <component :is="nav.icon" />
              </el-icon>
            </div>
            <div class="quick-nav-content">
              <h3>{{ nav.title }}</h3>
              <p>{{ nav.description }}</p>
            </div>
            <el-icon class="quick-nav-arrow">
              <ArrowRight />
            </el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- Resource Sections -->
    <section
      v-for="section in resourceSections"
      :key="section.key"
      class="section"
      :class="{ 'section-alt': section.isAlt }"
    >
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            {{ $t(section.titleKey) }}
          </h2>
          <el-button
            v-if="section.ctaRoute"
            link
            @click="$router.push(section.ctaRoute)"
          >
            {{ $t('common.more') }} <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
<<<<<<< HEAD
        <div v-loading="section.loading" class="resources-grid">
          <CultureCard v-for="item in section.resources" :key="item.id" :resource="item" />
=======
        <div v-loading="featuredLoading" class="resources-grid">
          <CultureCard v-for="item in featuredResources" :key="item.id" :resource="item" />
        </div>
      </div>
    </section>

    <!-- Hot Resources Section -->
    <section class="section section-alt">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            {{ $t('home.hot') }}
          </h2>
        </div>
        <div v-loading="hotLoading" class="resources-grid">
          <CultureCard v-for="item in hotResources" :key="item.id" :resource="item" />
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
        </div>
      </div>
    </section>

    <!-- Latest Events Section -->
    <section class="section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            {{ $t('home.latest') }}
          </h2>
          <el-button link @click="$router.push('/events')">
            {{ $t('common.more') }} <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div v-loading="eventsLoading" class="events-list">
          <el-card
            v-for="event in latestEvents"
            :key="event.id"
            class="event-card"
            @click="$router.push(`/event/${event.id}`)"
          >
            <div class="event-content">
              <el-image :src="event.cover" fit="cover" class="event-image" />
              <div class="event-info">
                <h3>{{ event.title }}</h3>
                <p>{{ event.description }}</p>
                <div class="event-meta">
                  <span v-if="event.startDate">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(event.startDate, 'YYYY-MM-DD') }}
                  </span>
                  <span v-if="event.location?.name">
                    <el-icon><Location /></el-icon>
                    {{ event.location.name }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getRecommendedResources, getHotResources } from '@/api/culture'
import { getLatestEvents } from '@/api/event'
import { getCarousels } from '@/api/carousel'
import type { CultureResource, HomeResource } from '@/types/culture'
import type { Event } from '@/types/event'
import type { CarouselItem } from '@/types/carousel'
import CultureCard from '@/components/common/CultureCard.vue'
import Carousel from '@/components/common/Carousel.vue'
import { formatDate } from '@/utils'
import { ArrowRight, Calendar, Location } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

<<<<<<< HEAD
const { t } = useI18n()

const featuredResources = ref<Array<CultureResource | HomeResource>>([])
const hotResources = ref<Array<CultureResource | HomeResource>>([])
=======
const featuredResources = ref<HomeResource[]>([])
const hotResources = ref<HomeResource[]>([])
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
const latestEvents = ref<Event[]>([])
const carouselItems = ref<CarouselItem[]>([])
const featuredLoading = ref(false)
const hotLoading = ref(false)
const eventsLoading = ref(false)
const carouselLoading = ref(false)

interface QuickNavItem {
  path: string
  title: string
  description: string
  icon: string
  color: string
}

// 快速导航配置（响应语言切换）
const quickNavItems = computed<QuickNavItem[]>(() => [
  {
    path: '/map',
    title: t('home.quickNav.map.title'),
    description: t('home.quickNav.map.description'),
    icon: 'MapLocation',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    path: '/routes',
    title: t('home.quickNav.routes.title'),
    description: t('home.quickNav.routes.description'),
    icon: 'Guide',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  },
  {
    path: '/events',
    title: t('home.quickNav.events.title'),
    description: t('home.quickNav.events.description'),
    icon: 'Calendar',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  },
  {
    path: '/community',
    title: t('home.quickNav.community.title'),
    description: t('home.quickNav.community.description'),
    icon: 'ChatLineRound',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  },
])

const resourceSections = computed(() => [
  {
    key: 'featured',
    titleKey: 'home.featured',
    ctaRoute: '/search',
    resources: featuredResources.value,
    loading: featuredLoading.value,
    isAlt: false,
  },
  {
    key: 'hot',
    titleKey: 'home.hot',
    resources: hotResources.value,
    loading: hotLoading.value,
    isAlt: true,
  },
])

const withFallback = async <T>(promise: Promise<T>, fallback: T) => {
  try {
    return await promise
  } catch (error) {
    console.error('Load home data fallback triggered:', error)
    return fallback
  }
}

const loadData = async () => {
  try {
    featuredLoading.value = true
    hotLoading.value = true
    eventsLoading.value = true
    carouselLoading.value = true

    const [featured, hot, events, carousels] = await Promise.all([
<<<<<<< HEAD
      withFallback(getRecommendedResources(8), [] as Array<CultureResource | HomeResource>),
      withFallback(getHotResources(8), [] as Array<CultureResource | HomeResource>),
      withFallback(getLatestEvents({ page: 1, size: 4 }), { list: [] as Event[], total: 0 }),
      withFallback(getCarousels(), [] as CarouselItem[]),
=======
      getRecommendedResources(8).catch(() => []),
      getHotResources(8).catch(() => []),
      getLatestEvents({ page: 1, size: 4 }).catch(() => ({ list: [] as Event[], total: 0 })),
      getCarousels().catch(() => [] as CarouselItem[]),
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
    ])

    featuredResources.value = Array.isArray(featured) ? featured : []
    hotResources.value = Array.isArray(hot) ? hot : []
<<<<<<< HEAD
    const eventsData = events ?? { list: [] as Event[] }
    latestEvents.value = Array.isArray(eventsData.list) ? eventsData.list : []
=======
    const eventsData = events as { list?: Event[]; total?: number } | null
    if (eventsData && eventsData.list) {
      latestEvents.value = Array.isArray(eventsData.list) ? eventsData.list : []
    } else {
      latestEvents.value = []
    }
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
    // 只显示启用的轮播图，并按 order 排序
    const carouselsData = Array.isArray(carousels) ? carousels : []
    carouselItems.value = carouselsData
      .filter(item => item.enabled)
      .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
  } catch (error) {
    console.error('Failed to load home data:', error)
    // 确保即使出错也设置默认值
    featuredResources.value = []
    hotResources.value = []
<<<<<<< HEAD
=======
    heritageItems.value = []
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
    latestEvents.value = []
    carouselItems.value = []
  } finally {
    featuredLoading.value = false
    hotLoading.value = false
    eventsLoading.value = false
    carouselLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.home {
  min-height: calc(100vh - 70px);
  width: 100%;
  max-width: 100%;
  overflow-x: hidden; // 防止横向滚动
}

// 轮播图样式定制（仅在 Home 页面生效）
.home-carousel {
  margin-top: 0;
  margin-bottom: 0;
  position: relative;
  z-index: 1;
  width: 100%;
  overflow: hidden; // 防止内容溢出
  // 移除 Carousel 组件默认的 margin-bottom，统一在此管理间距
  :deep(.carousel-container) {
    margin-bottom: 0;
    width: 100%;
    max-width: 100%;
  }

  :deep(.custom-carousel) {
    width: 100%;
    max-width: 100%;
    margin-bottom: 0 !important; // 确保没有底部间距
    padding-bottom: 0 !important;

    .el-carousel__container {
      // 使用 clamp 控制在不同视口的高度区间
      width: 100% !important;
      height: clamp(360px, 50vh, 680px) !important;
      margin-bottom: 0 !important;
      padding-bottom: 0 !important;
    }

    .el-carousel__item {
      width: 100%;
      height: 100%;
      // 保持 overflow hidden 以确保布局正常
      overflow: hidden;
    }

    // 确保轮播图容器内部元素也是 100% 宽度
    .el-carousel__track {
      width: 100%;
    }

    // 箭头按钮样式
    .el-carousel__arrow {
      width: 48px;
      height: 48px;
      background-color: rgba(255, 255, 255, 0.9);
      border: none;
      border-radius: 50%;
      color: #303133;
      font-size: 20px;
      transition: all 0.3s ease;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

      &:hover {
        background-color: rgba(255, 255, 255, 1);
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      }

      .el-icon {
        font-size: 20px;
      }
    }

    .el-carousel__arrow--left {
      left: 20px;
      top: 50%;
      transform: translateY(-50%);
    }

    .el-carousel__arrow--right {
      right: 20px;
      top: 50%;
      transform: translateY(-50%);
    }

    // 指示器样式 - 现在在内部，确保没有额外空间
    .el-carousel__indicators {
      bottom: 20px;
      margin-bottom: 0 !important; // 移除底部margin
      padding-bottom: 0 !important; // 移除底部padding
      position: absolute; // 确保绝对定位，不占用文档流空间
      z-index: 10; // 确保在图片上方

      .el-carousel__indicator {
        padding: 8px 4px;

        .el-carousel__button {
          width: 40px;
          height: 4px;
          background-color: rgba(255, 255, 255, 0.5);
          border-radius: 2px;
          transition: all 0.3s ease;
          border: none;
        }

        &.is-active .el-carousel__button {
          background-color: rgba(255, 255, 255, 1);
          width: 50px;
          height: 4px;
        }
      }
    }

    // 移除轮播图组件可能产生的底部间距
    .el-carousel {
      margin-bottom: 0 !important;
      padding-bottom: 0 !important;
    }

    // 确保轮播图整体没有底部间距
    .el-carousel__mask {
      margin-bottom: 0 !important;
      padding-bottom: 0 !important;
    }
  }

  // 轮播图项样式
  :deep(.carousel-item) {
    width: 100%;
    min-height: clamp(360px, 50vh, 680px); // 与容器高度匹配
    height: 100%;
    // 使用 cover 填充整个容器，无留白
    // 通过合理的容器高度平衡显示效果和页面布局
    background-size: cover !important;
    background-position: center center !important;
    background-attachment: scroll;
    background-repeat: no-repeat !important;
    // 移除深色背景，让图片填充
    background-color: transparent;
    // 保持 overflow hidden 以确保布局正常
    overflow: hidden;
  }

  // 渐变叠加层样式 - 默认不显示，让图片完全清晰可见
  // 如果需要叠加层来确保文字可读性，可以取消下面的注释
  :deep(.carousel-gradient-overlay) {
    // 完全禁用叠加层，让图片清晰显示
    display: none !important;
  }

  // 如果需要轻微的叠加层（仅在文字区域），可以使用以下样式：
  // :deep(.carousel-gradient-overlay) {
  //   background: linear-gradient(
  //     to bottom,
  //     rgba(0, 0, 0, 0.1) 0%,
  //     rgba(0, 0, 0, 0.15) 50%,
  //     rgba(0, 0, 0, 0.2) 100%
  //   ) !important;
  //   opacity: 1;
  // }

  // 调整轮播图 overlay 对齐方式，确保内容在视觉中心
  :deep(.carousel-overlay) {
    align-items: center;
    padding-top: 0;
    justify-content: center;
  }

  // 内容样式 - 确保内容居中显示
  :deep(.carousel-content) {
    padding: 0 40px;
    max-width: 800px;
    animation: fadeInUp 0.8s ease-out;
    margin-top: 0;
  }

  :deep(.carousel-title) {
    font-size: 56px;
    font-weight: 700;
    margin-bottom: 24px;
    // 增强文字阴影，确保在任何背景下都清晰可读
    text-shadow:
      2px 2px 4px rgba(0, 0, 0, 0.8),
      0 0 8px rgba(0, 0, 0, 0.5),
      0 0 16px rgba(0, 0, 0, 0.3);
    line-height: 1.2;
    letter-spacing: -0.5px;
  }

  :deep(.carousel-subtitle) {
    font-size: 22px;
    margin-bottom: 40px;
    opacity: 0.95;
    // 增强文字阴影，确保可读性
    text-shadow:
      1px 1px 3px rgba(0, 0, 0, 0.8),
      0 0 6px rgba(0, 0, 0, 0.5);
    line-height: 1.6;
    font-weight: 300;
  }

  // 空状态样式
  :deep(.carousel-empty),
  :deep(.carousel-loading) {
    height: clamp(360px, 50vh, 680px);
    width: 100%;
    background: linear-gradient(
      to bottom,
      rgba(37, 99, 235, 0.85) 0%,
      rgba(67, 56, 202, 0.9) 40%,
      rgba(124, 58, 237, 0.95) 100%
    );
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-top: 0;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><defs><pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse"><path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
      opacity: 0.3;
    }
  }

  // 空状态内容样式
  :deep(.empty-content) {
    position: relative;
    z-index: 1;
    padding: 0 20px;
  }

  :deep(.hero-title) {
    font-size: 56px;
    font-weight: 700;
    margin-bottom: 24px;
    text-shadow: 2px 4px 8px rgba(0, 0, 0, 0.4);
    line-height: 1.2;
  }

  :deep(.hero-subtitle) {
    font-size: 22px;
    margin-bottom: 40px;
    opacity: 0.95;
    text-shadow: 1px 2px 4px rgba(0, 0, 0, 0.3);
    line-height: 1.6;
    font-weight: 300;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 快速导航区域
.quick-nav-section {
  padding: 0; // 移除默认padding，通过内部container控制
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  margin-top: 0;

  .container {
    padding-top: 50px;
    padding-bottom: 40px;
  }
}

.quick-nav-grid {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  flex-wrap: nowrap;
}

.quick-nav-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  flex: 1 1 0;
  min-width: 220px;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    transition: left 0.5s ease;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #409eff;

    &::before {
      left: 100%;
    }

    .quick-nav-arrow {
      transform: translateX(4px);
      color: #409eff;
    }
  }
}

.quick-nav-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.quick-nav-content {
  flex: 1;
  min-width: 0;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 4px 0;
    line-height: 1.4;
  }

  p {
    font-size: 13px;
    color: #909399;
    margin: 0;
    line-height: 1.4;
  }
}

.quick-nav-arrow {
  color: #c0c4cc;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.section {
  padding: 50px 0;

  &.section-alt {
    background: #fafafa;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.heritage-mini-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.heritage-mini-card {
  display: flex;
  gap: 12px;
  cursor: pointer;

  .mini-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .mini-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }

  h3 {
    font-size: 16px;
    margin-bottom: 6px;
  }

  p {
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
  }
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.event-card {
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: translateX(8px);
  }
}

.event-content {
  display: flex;
  gap: 20px;
}

.event-image {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  flex-shrink: 0;
}

.event-info {
  flex: 1;

  h3 {
    font-size: 20px;
    margin-bottom: 12px;
    color: #303133;
  }

  p {
    color: #606266;
    line-height: 1.6;
    margin-bottom: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.event-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

// 中等大屏幕优化（1200px 及以上）
@media (min-width: 1200px) and (max-width: 1919px) {
  .home-carousel {
    :deep(.custom-carousel) {
      .el-carousel__container {
        height: 55vh !important; // 适中的高度
        min-height: 550px;
        max-height: 750px;
      }
    }

    :deep(.carousel-item) {
      min-height: 550px;
    }

    :deep(.carousel-empty) {
      height: 55vh;
      min-height: 550px;
      max-height: 750px;
    }
  }
}

// 大屏幕优化（1920px 及以上）
@media (min-width: 1920px) {
  .home-carousel {
    :deep(.custom-carousel) {
      .el-carousel__container {
        height: 60vh !important; // 大屏幕稍高一些，但仍保持合理
        min-height: 600px;
        max-height: 800px;
      }
    }

    :deep(.carousel-item) {
      min-height: 600px;
    }

    :deep(.carousel-empty) {
      height: 60vh;
      min-height: 600px;
      max-height: 800px;
    }
  }
}

// 超大屏幕优化（2560px 及以上）
@media (min-width: 2560px) {
  .home-carousel {
    :deep(.custom-carousel) {
      .el-carousel__container {
        height: 65vh !important; // 超大屏幕稍高，但不超过70vh
        min-height: 650px;
        max-height: 900px;
      }
    }

    :deep(.carousel-item) {
      min-height: 650px;
    }

    :deep(.carousel-empty) {
      height: 65vh;
      min-height: 650px;
      max-height: 900px;
    }
  }
}

@media (max-width: 768px) {
  .home-carousel {
    :deep(.custom-carousel) {
      .el-carousel__container {
        height: 40vh !important;
        min-height: 320px;
        max-height: 450px;
      }

      .el-carousel__arrow {
        width: 36px;
        height: 36px;
        font-size: 16px;

        .el-icon {
          font-size: 16px;
        }
      }

      .el-carousel__arrow--left {
        left: 10px;
        top: 50%;
        transform: translateY(-50%);
      }

      .el-carousel__arrow--right {
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
      }
    }

    :deep(.carousel-item) {
      min-height: 320px;
    }

    :deep(.carousel-empty) {
      height: 40vh;
      min-height: 320px;
      max-height: 450px;
      padding-top: 0;
    }

    :deep(.carousel-title),
    :deep(.hero-title) {
      font-size: 32px;
      margin-bottom: 16px;
    }

    :deep(.carousel-subtitle),
    :deep(.hero-subtitle) {
      font-size: 16px;
      margin-bottom: 24px;
    }

    // 移动端调整轮播图 overlay
    :deep(.carousel-overlay) {
      padding-top: 0;
      align-items: center;
    }

    :deep(.carousel-content) {
      padding: 0 20px;
    }
  }

  .quick-nav-section {
    padding: 40px 0 30px;
  }

  .quick-nav-grid {
    flex-wrap: wrap;
    justify-content: center;
    gap: 16px;
    max-width: 100%;
  }

  .quick-nav-card {
    padding: 16px;
    gap: 12px;
    flex: 1 1 100%;
    min-width: 0;
  }

  .quick-nav-icon {
    width: 48px;
    height: 48px;

    .el-icon {
      font-size: 24px;
    }
  }

  .quick-nav-content {
    h3 {
      font-size: 15px;
    }

    p {
      font-size: 12px;
    }
  }

  .section {
    padding: 40px 0;
  }

  .event-content {
    flex-direction: column;
  }

  .event-image {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .home-carousel {
    :deep(.custom-carousel) {
      .el-carousel__container {
        height: 35vh !important;
        min-height: 280px;
        max-height: 380px;
      }
    }

    :deep(.carousel-item) {
      min-height: 280px;
    }

    :deep(.carousel-empty) {
      height: 35vh;
      min-height: 280px;
      max-height: 380px;
      padding-top: 0;
    }

    // 小屏幕调整轮播图 overlay
    :deep(.carousel-overlay) {
      padding-top: 0;
      align-items: center;
    }

    :deep(.carousel-content) {
      padding: 0 16px;
    }
  }

  .quick-nav-grid {
    flex-direction: column;
    gap: 12px;
  }

  .quick-nav-card {
    padding: 14px;
    flex: 1 1 auto;
    width: 100%;
    min-width: auto;
  }

  .section {
    padding: 30px 0;
  }
}
</style>
