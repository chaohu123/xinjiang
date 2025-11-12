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
            <div v-if="resource.images && resource.images.length > 0" class="detail-images">
              <ImageGallery :images="resource.images" />
            </div>

            <div v-if="resource.videoUrl" class="detail-video">
              <VideoPlayer :src="resource.videoUrl" :poster="resource.cover" />
            </div>

            <div class="detail-description">
              <h2>{{ $t('detail.overview') }}</h2>
              <p>{{ resource.description }}</p>
            </div>

            <div v-if="resource.content" class="detail-content-text" v-html="resource.content" />

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
          </div>
        </div>

        <EmptyState v-else-if="!loading" text="资源不存在" />
      </div>

      <el-dialog v-model="showMap" title="位置地图" width="80%">
        <div v-if="resource?.location" class="map-container" style="height: 500px">
          <!-- 这里可以集成地图组件 -->
          <p>地图功能需要配置地图服务API</p>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getResourceDetail, favoriteResource, unfavoriteResource } from '@/api/culture'
import type { CultureResource } from '@/types/culture'
import ImageGallery from '@/components/common/ImageGallery.vue'
import VideoPlayer from '@/components/common/VideoPlayer.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { Location, View, Star, MapLocation } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const resource = ref<CultureResource | null>(null)
const loading = ref(false)
const isFavorited = ref(false)
const showMap = ref(false)

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

onMounted(() => {
  loadDetail()
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

.map-container {
  width: 100%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}
</style>
