<template>
  <div class="detail-page">
    <div class="container">
      <div v-loading="loading" class="detail-content">
        <div v-if="resource" class="detail-main">
          <div class="detail-header">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/home' }"> 首页 </el-breadcrumb-item>
              <el-breadcrumb-item>{{ getTypeName(resource.type) }}</el-breadcrumb-item>
              <el-breadcrumb-item>{{ resource.title }}</el-breadcrumb-item>
            </el-breadcrumb>
            <h1 class="detail-title">
              {{ resource.title }}
            </h1>
            <div class="detail-meta">
              <span>
                <el-icon><Location /></el-icon>
                {{ resource.region }}
              </span>
              <span>
                <el-icon><View /></el-icon>
                {{ resource.views }}
              </span>
              <span>
                <el-icon><Star /></el-icon>
                {{ resource.favorites }}
              </span>
              <el-button :type="isFavorited ? 'primary' : 'default'" @click="handleFavorite">
                <el-icon><Star /></el-icon>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>

          <div class="detail-body">
            <div class="detail-description">
              <h2>{{ $t('detail.overview') }}</h2>
              <p>{{ resource.description }}</p>
            </div>

            <div v-if="sanitizedContent" class="detail-content-text">
              <h2>图文内容</h2>
              <div class="detail-rich-text" v-html="sanitizedContent" />
            </div>

            <div v-if="resource.location" class="detail-location">
              <h2>{{ $t('detail.location') }}</h2>
              <el-button type="primary" @click="showMap = true">
                <el-icon><MapLocation /></el-icon>
                {{ $t('detail.viewMap') }}
              </el-button>
              <p>{{ resource.location.address }}</p>
            </div>

            <div v-if="resource.tags && resource.tags.length > 0" class="detail-tags">
              <el-tag v-for="tag in resource.tags" :key="tag" size="large">
                {{ tag }}
              </el-tag>
            </div>

            <div v-if="galleryImages.length > 0" class="detail-media-section">
              <h2>图片展示</h2>
              <div class="detail-images">
                <ImageGallery :images="galleryImages" :item-height="galleryImageHeight" :columns="2" />
              </div>
            </div>

            <div v-if="videoList.length > 0" class="detail-media-section">
              <h2>视频展示</h2>
              <div class="detail-media-grid">
                <div v-for="(video, index) in videoList" :key="`${video}-${index}`" class="media-grid-item">
                  <VideoPlayer :src="video" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <EmptyState v-else-if="!loading" text="资源不存在" />
      </div>

      <el-dialog v-model="showMap" title="位置地图" width="80%" @opened="handleMapDialogOpened" @closed="destroyDetailMap">
        <div v-if="resource?.location" class="map-wrapper">
          <div ref="detailMapContainer" class="detail-map-container" style="height: 500px"></div>
          <div v-if="!detailMapLoaded" class="map-loading">地图加载中...</div>
        </div>
        <div v-else class="map-no-location">该资源暂无位置信息</div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getResourceDetail, favoriteResource, unfavoriteResource } from '@/api/culture'
import type { CultureResource } from '@/types/culture'
import ImageGallery from '@/components/common/ImageGallery.vue'
import VideoPlayer from '@/components/common/VideoPlayer.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { Location, View, Star, MapLocation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { loadAMapScript } from '@/utils/amap'

const route = useRoute()
const resource = ref<CultureResource | null>(null)
const loading = ref(false)
const isFavorited = ref(false)
const showMap = ref(false)
const detailMapContainer = ref<HTMLElement>()
const detailMap = ref<AMap.Map | null>(null)
const detailMarker = ref<AMap.Marker | null>(null)
const detailMapLoaded = ref(false)
const galleryImageHeight = 320

const sanitizedContent = computed(() => {
  if (!resource.value?.content) return ''
  return resource.value.content
    .replace(/<figure[\s\S]*?>/gi, match => (/<img/i.test(match) ? '' : match))
    .replace(/<img[\s\S]*?>/gi, '')
    .replace(/<video[\s\S]*?<\/video>/gi, '')
    .replace(/<iframe[\s\S]*?<\/iframe>/gi, '')
    .replace(/\sloading=(?:"|')?lazy(?:"|')?/gi, '')
    .replace(/\sdata-src=(?:"|')[^"']*(?:"|')/gi, '')
})

const galleryImages = computed(() => {
  if (!resource.value) return []

  const normalized =
    Array.isArray(resource.value.images) && resource.value.images.length
      ? resource.value.images
          .map(img => img?.trim())
          .filter((img): img is string => Boolean(img))
      : []

  const unique = Array.from(new Set(normalized))
  const cover = resource.value.cover?.trim()

  if (cover && !unique.includes(cover)) {
    unique.unshift(cover)
  }

  if (!unique.length && cover) {
    return [cover]
  }

  return unique
})

const videoList = computed(() => {
  const raw = resource.value?.videoUrl as string | string[] | undefined
  if (!raw) return []

  if (Array.isArray(raw)) {
    return raw.map(url => url?.trim()).filter((url): url is string => Boolean(url))
  }

  if (typeof raw === 'string') {
    return raw
      .split(',')
      .map(url => url.trim())
      .filter(url => !!url)
  }

  return []
})

const getTypeName = (type: string) => {
  // 将类型转换为小写以支持大小写不敏感的匹配
  const normalizedType = type?.toLowerCase() || ''
  const map: Record<string, string> = {
    article: '文章',
    exhibit: '展品',
    video: '视频',
    audio: '音频',
  }
  return map[normalizedType] || type
}

const loadDetail = async () => {
  const type = route.params.type as string
  const id = parseInt(route.params.id as string)

  loading.value = true
  try {
    const data = await getResourceDetail(type as any, id)
    resource.value = data
  } catch (error) {
    console.error('Failed to load detail:', error)
  } finally {
    loading.value = false
  }
}

const handleFavorite = async () => {
  if (!resource.value) return

  try {
    if (isFavorited.value) {
      await unfavoriteResource(resource.value.type, resource.value.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteResource(resource.value.type, resource.value.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 处理地图对话框打开事件
const handleMapDialogOpened = () => {
  // 延迟初始化，确保对话框动画完成
  setTimeout(() => {
    initDetailMap()
  }, 150)
}

// 初始化详情页地图
const initDetailMap = async () => {
  if (!resource.value?.location) {
    return
  }

  // 等待 DOM 完全渲染
  await nextTick()

  // 再次等待，确保对话框动画完成
  await new Promise(resolve => setTimeout(resolve, 100))

  if (!detailMapContainer.value) {
    console.error('地图容器元素不存在')
    return
  }

  // 确保容器可见且有尺寸
  if (!detailMapContainer.value.offsetWidth || !detailMapContainer.value.offsetHeight) {
    console.error('地图容器尺寸无效')
    // 再等待一段时间
    await new Promise(resolve => setTimeout(resolve, 200))
    if (!detailMapContainer.value.offsetWidth || !detailMapContainer.value.offsetHeight) {
      ElMessage.error('地图容器未准备好')
      return
    }
  }

  try {
    // 加载高德地图脚本
    await loadAMapScript()

    if (!window.AMap || !window.AMap.Map) {
      ElMessage.error('高德地图加载失败')
      return
    }

    // 清除旧的地图实例
    if (detailMap.value) {
      detailMap.value.destroy()
      detailMap.value = null
    }

    // 再次检查容器是否存在
    if (!detailMapContainer.value) {
      console.error('地图容器在初始化过程中被移除')
      return
    }

    // 创建地图实例
    detailMap.value = new window.AMap.Map(detailMapContainer.value, {
      center: [resource.value.location.lng, resource.value.location.lat],
      zoom: 15,
      viewMode: '3D',
      mapStyle: 'amap://styles/normal',
    })

    detailMapLoaded.value = true

    // 创建标记点
    if (detailMarker.value) {
      detailMap.value.remove(detailMarker.value)
      detailMarker.value = null
    }

    // 创建标记内容
    const markerContent = document.createElement('div')
    markerContent.className = 'detail-map-marker'
    markerContent.style.width = '40px'
    markerContent.style.height = '40px'
    markerContent.style.borderRadius = '50%'
    markerContent.style.backgroundColor = '#409EFF'
    markerContent.style.border = '3px solid #fff'
    markerContent.style.cursor = 'pointer'
    markerContent.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)'
    markerContent.style.position = 'relative'
    markerContent.title = resource.value.title

    // 创建标记
    detailMarker.value = new window.AMap.Marker({
      position: [resource.value.location.lng, resource.value.location.lat],
      content: markerContent,
      title: resource.value.title,
      offset: new window.AMap.Pixel(-20, -20) as any,
    })

    detailMap.value.add(detailMarker.value)

    // 创建信息窗口
    if (resource.value.location.address) {
      const infoWindow = new window.AMap.InfoWindow({
        content: `
          <div style="padding: 8px; max-width: 300px;">
            <h3 style="margin: 0 0 8px 0; font-size: 16px; font-weight: bold;">${resource.value.title}</h3>
            <p style="margin: 0; color: #666; font-size: 14px;">${resource.value.location.address}</p>
          </div>
        `,
        offset: new window.AMap.Pixel(0, -30) as any,
      })

      // 标记点击时显示信息窗口
      detailMarker.value.on('click', () => {
        infoWindow.open(detailMap.value!, [resource.value!.location!.lng, resource.value!.location!.lat])
      })

      // 默认打开信息窗口
      setTimeout(() => {
        infoWindow.open(detailMap.value!, [resource.value!.location!.lng, resource.value!.location!.lat])
      }, 300)
    }
  } catch (error: any) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败：' + (error?.message || '请检查配置'))
  }
}

// 销毁详情页地图
const destroyDetailMap = () => {
  if (detailMarker.value && detailMap.value) {
    detailMap.value.remove(detailMarker.value)
    detailMarker.value = null
  }
  if (detailMap.value) {
    detailMap.value.destroy()
    detailMap.value = null
  }
  detailMapLoaded.value = false
}

onMounted(() => {
  loadDetail()
})

onUnmounted(() => {
  destroyDetailMap()
})
</script>

<style lang="scss" scoped>
.detail-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.detail-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.detail-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.detail-title {
  font-size: 32px;
  font-weight: 600;
  margin: 20px 0;
  color: #303133;
}

.detail-meta {
  display: flex;
  gap: 24px;
  align-items: center;
  color: #909399;
  font-size: 14px;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.detail-body {
  > div {
    margin-bottom: 30px;
  }
}

.detail-media-section {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 0;

  h2 {
    font-size: 24px;
    margin-bottom: 16px;
    color: #303133;
  }
}

.detail-media-section + .detail-media-section {
  border-top: 1px solid rgba(230, 230, 230, 0.6);
}

.detail-images {
  border-radius: 16px;
  overflow: hidden;

  :deep(.image-gallery) {
    width: 100%;
    margin: 0 auto;
    gap: 20px !important;
  }

  :deep(.gallery-image) {
    min-height: 240px;
    border-radius: 16px;
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.12);
    border: 4px solid #fff;
  }
}

.detail-media-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.media-grid-item {
  border-radius: 16px;
  overflow: hidden;
  background: #000;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
  min-height: 260px;

  :deep(video),
  :deep(.video-js) {
    display: block;
    width: 100%;
    height: 100%;
  }
}

@media (max-width: 768px) {
  .detail-media-grid {
    grid-template-columns: 1fr;
  }

  :deep(.image-gallery) {
    grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
  }
}

.detail-description,
.detail-content-text {
  h2 {
    font-size: 24px;
    margin-bottom: 16px;
    color: #303133;
  }

  p {
    line-height: 1.8;
    color: #606266;
    font-size: 16px;
  }
}

.detail-content-text {
  :deep(img),
  :deep(video),
  :deep(iframe) {
    max-width: 100%;
    height: auto;
    display: block;
    margin: 20px auto;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  }

  :deep(video) {
    background: #000;
  }
}

.detail-rich-text {
  line-height: 1.8;
  color: #606266;
  font-size: 16px;
}

.detail-location {
  h2 {
    font-size: 24px;
    margin-bottom: 16px;
  }

  p {
    margin-top: 12px;
    color: #606266;
  }
}

.detail-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.map-wrapper {
  position: relative;
  width: 100%;
  height: 500px;
}

.detail-map-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #f5f7fa;

  :deep(.amap-controls) {
    right: 10px;
    top: 10px;
  }

  :deep(.amap-logo) {
    display: none !important;
  }

  :deep(.amap-copyright) {
    bottom: 0;
    font-size: 10px;
  }
}

.detail-map-marker {
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 14px;
    height: 14px;
    background: #fff;
    border-radius: 50%;
  }
}

.map-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #909399;
  font-size: 14px;
}

.map-no-location {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 500px;
  color: #909399;
  font-size: 14px;
}
</style>
