<template>
  <div class="events-page">
    <div class="container">
      <div class="page-header">
        <h1>{{ $t('events.title') }}</h1>
      </div>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane :label="$t('events.upcoming')" name="upcoming" />
        <el-tab-pane :label="$t('events.ongoing')" name="ongoing" />
        <el-tab-pane :label="$t('events.past')" name="past" />
      </el-tabs>

      <div v-loading="loading" class="events-list">
        <el-card
          v-for="event in events"
          :key="event.id"
          class="event-card"
          @click="$router.push(`/event/${event.id}`)"
        >
          <div class="event-content">
            <el-image :src="event.cover" fit="cover" class="event-image" />
            <div class="event-info">
              <div class="event-header">
                <h3>{{ event.title }}</h3>
                <el-tag :type="getStatusType(event.status)">
                  {{ getStatusText(event.status) }}
                </el-tag>
              </div>
              <p>{{ event.description }}</p>
              <div class="event-meta">
                <span v-if="event.startDate">
                  <el-icon><Calendar /></el-icon>
                  {{ formatDate(event.startDate, 'YYYY-MM-DD') }}
                  <template v-if="event.endDate">
                    ~ {{ formatDate(event.endDate, 'YYYY-MM-DD') }}
                  </template>
                </span>
                <span v-if="event.location?.name">
                  <el-icon><Location /></el-icon>
                  {{ event.location.name }}
                </span>
                <span v-if="event.capacity">
                  <el-icon><User /></el-icon>
                  {{ event.registered || 0 }}/{{ event.capacity }}
                </span>
              </div>
              <div class="event-actions">
                <el-button
                  v-if="event.status === 'upcoming' || event.status === 'ongoing'"
                  type="primary"
                  @click.stop="handleRegister(event.id)"
                >
                  {{ $t('events.register') }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <EmptyState v-if="!loading && events.length === 0" :text="$t('common.noData')" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        class="pagination"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getEvents, registerEvent } from '@/api/event'
import type { Event } from '@/types/event'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatDate } from '@/utils'
import { Calendar, Location, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const events = ref<Event[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const activeTab = ref('upcoming')

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

const loadEvents = async () => {
  loading.value = true
  try {
    const response = await getEvents({
      status: activeTab.value, // 使用 status 而不是 type
      page: currentPage.value,
      size: pageSize.value,
    })
    // 将后端返回的大写状态和类型转换为小写
    const eventsList = (response.list || []).map(event => ({
      ...event,
      status: event.status?.toLowerCase() as 'upcoming' | 'ongoing' | 'past',
      type: event.type?.toLowerCase() as 'exhibition' | 'performance' | 'workshop' | 'tour',
    }))
    events.value = eventsList
    total.value = response.total || 0
  } catch (error) {
    console.error('Failed to load events:', error)
    events.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadEvents()
}

const handlePageChange = () => {
  loadEvents()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleRegister = async (id: number) => {
  try {
    await registerEvent(id)
    ElMessage.success('报名成功')
    loadEvents()
  } catch (error) {
    ElMessage.error('报名失败')
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style lang="scss" scoped>
.events-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.page-header {
  margin-bottom: 30px;

  h1 {
    font-size: 32px;
    color: #303133;
  }
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
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
  width: 250px;
  height: 180px;
  border-radius: 8px;
  flex-shrink: 0;
}

.event-info {
  flex: 1;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h3 {
    font-size: 20px;
    color: #303133;
  }
}

.event-meta {
  display: flex;
  gap: 24px;
  margin: 16px 0;
  font-size: 14px;
  color: #909399;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.event-actions {
  margin-top: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
