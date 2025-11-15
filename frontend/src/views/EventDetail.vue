<template>
  <div class="event-detail-page">
    <div class="container">
      <div v-loading="loading" class="event-detail-content">
        <div v-if="eventDetail" class="event-header">
          <h1>{{ eventDetail.title }}</h1>
          <el-tag :type="getStatusType(eventDetail.status)">
            {{ getStatusText(eventDetail.status) }}
          </el-tag>
          <p>{{ eventDetail.description }}</p>
          <div class="event-meta">
            <span>
              <el-icon><Calendar /></el-icon>
              {{ formatDate(eventDetail.startDate, 'YYYY-MM-DD') }} ~
              {{ formatDate(eventDetail.endDate, 'YYYY-MM-DD') }}
            </span>
            <span v-if="eventDetail.location">
              <el-icon><Location /></el-icon>
              {{ eventDetail.location.name }} - {{ eventDetail.location.address }}
            </span>
            <span v-if="eventDetail.capacity">
              <el-icon><User /></el-icon>
              {{ eventDetail.registered }}/{{ eventDetail.capacity }}
            </span>
          </div>
          <el-button
            v-if="eventDetail.status !== 'past'"
            type="primary"
            size="large"
            @click="handleRegister"
          >
            {{ $t('events.register') }}
          </el-button>
        </div>

        <div v-if="eventDetail" class="event-body">
        <div v-if="eventDetail.images && eventDetail.images.length > 0" class="event-images">
            <ImageGallery :images="eventDetail.images" />
          </div>

        <div v-if="eventDetail.videos && eventDetail.videos.length > 0" class="event-videos">
          <h2>活动视频</h2>
          <div class="event-video-grid">
            <VideoPlayer v-for="(video, index) in eventDetail.videos" :key="`${video}-${index}`" :src="video" />
          </div>
        </div>

          <div class="event-content" v-html="eventDetail.content" />

          <div v-if="eventDetail.schedule" class="event-schedule">
            <h2>活动安排</h2>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in eventDetail.schedule"
                :key="index"
                :timestamp="item.time"
              >
                {{ item.activity }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEventDetail, registerEvent } from '@/api/event'
import type { EventDetail } from '@/types/event'
import ImageGallery from '@/components/common/ImageGallery.vue'
import VideoPlayer from '@/components/common/VideoPlayer.vue'
import { formatDate } from '@/utils'
import { Calendar, Location, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { requireAuth } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const eventDetail = ref<EventDetail | null>(null)
const loading = ref(false)

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    upcoming: 'success',
    ongoing: 'warning',
    past: 'info',
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    upcoming: '即将开始',
    ongoing: '进行中',
    past: '已结束',
  }
  return map[status] || status
}

const loadDetail = async () => {
  const id = parseInt(route.params.id as string)
  loading.value = true
  try {
    const data = await getEventDetail(id)
    // Ensure images array is properly initialized
    eventDetail.value = {
      ...data,
      images: Array.isArray(data.images) ? data.images.filter(img => img && img.trim()) : [],
      videos: Array.isArray(data.videos) ? data.videos.filter(video => video && video.trim()) : [],
      schedule: Array.isArray(data.schedule) ? data.schedule : [],
    }
  } catch (error) {
    console.error('Failed to load event detail:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!eventDetail.value) return

  // 检查登录状态
  if (!requireAuth('请先登录后再报名', router)) {
    return
  }

  try {
    await registerEvent(eventDetail.value.id)
    ElMessage.success('报名成功')
    loadDetail()
  } catch (error: any) {
    // 如果是401错误，说明未登录，已经在axios拦截器中处理
    // 其他错误显示具体错误信息
    const errorMessage = error.response?.data?.message || error.message || '报名失败'
    if (error.response?.status !== 401) {
      ElMessage.error(errorMessage)
    }
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.event-detail-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.event-detail-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.event-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;

  h1 {
    font-size: 32px;
    margin-bottom: 16px;
    color: #303133;
  }

  p {
    font-size: 16px;
    color: #606266;
    line-height: 1.8;
    margin: 16px 0;
  }
}

.event-meta {
  display: flex;
  gap: 24px;
  margin: 20px 0;
  color: #909399;
  font-size: 14px;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.event-body {
  > div {
    margin-bottom: 30px;
  }
}

.event-videos {
  h2 {
    font-size: 24px;
    margin-bottom: 20px;
    color: #303133;
  }
}

.event-video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.event-content {
  line-height: 1.8;
  color: #606266;
  font-size: 16px;
}

.event-schedule {
  h2 {
    font-size: 24px;
    margin-bottom: 20px;
    color: #303133;
  }
}
</style>
