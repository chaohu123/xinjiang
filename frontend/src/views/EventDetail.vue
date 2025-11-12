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
            <span>
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
import { useRoute } from 'vue-router'
import { getEventDetail, registerEvent } from '@/api/event'
import type { EventDetail } from '@/types/event'
import ImageGallery from '@/components/common/ImageGallery.vue'
import { formatDate } from '@/utils'
import { Calendar, Location, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const eventDetail = ref<EventDetail | null>(null)
const loading = ref(false)

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    upcoming: 'success',
    ongoing: 'warning',
    past: 'info',
  }
  return map[status] || ''
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
    eventDetail.value = data
  } catch (error) {
    console.error('Failed to load event detail:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!eventDetail.value) return
  try {
    await registerEvent(eventDetail.value.id)
    ElMessage.success('报名成功')
    loadDetail()
  } catch (error) {
    ElMessage.error('报名失败')
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
