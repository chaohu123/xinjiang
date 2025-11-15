<template>
  <div class="events-page">
    <div class="container">
      <div class="page-header">
        <h1>{{ $t('events.title') }}</h1>
      </div>

      <div class="tabs-container">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane :label="$t('events.upcoming')" name="upcoming">
            <template #label>
              <span>{{ $t('events.upcoming') }}</span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="$t('events.ongoing')" name="ongoing" />
          <el-tab-pane :label="$t('events.past')" name="past" />
        </el-tabs>
        <el-button
          v-if="activeTab === 'upcoming'"
          :icon="Calendar"
          circle
          class="calendar-btn"
          @click="handleCalendarOpen(); showCalendarDialog = true"
        />
      </div>

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
                <el-tag :type="getStatusType(getActualStatus(event))">
                  {{ getStatusText(getActualStatus(event)) }}
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
                  v-if="event.isRegistered"
                  disabled
                  type="success"
                >
                  已报名
                </el-button>
                <el-button
                  v-else-if="getActualStatus(event) === 'upcoming' || getActualStatus(event) === 'ongoing'"
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

    <!-- 日历对话框 -->
    <el-dialog
      v-model="showCalendarDialog"
      title="活动日历"
      width="500px"
    >
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <div class="calendar-date">{{ data.day.split('-').slice(2).join('-') }}</div>
            <div v-if="getEventsByDate(data.day).length > 0" class="calendar-events">
              <el-tag
                v-for="event in getEventsByDate(data.day)"
                :key="event.id"
                size="small"
                type="success"
                class="calendar-event-tag"
                @click.stop="handleCalendarEventClick(event.id)"
              >
                {{ event.title }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEvents, registerEvent } from '@/api/event'
import type { Event } from '@/types/event'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatDate } from '@/utils'
import { Calendar, Location, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { requireAuth } from '@/utils/auth'

const router = useRouter()

const events = ref<Event[]>([])
const allEvents = ref<Event[]>([]) // 存储所有活动用于日历显示
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const activeTab = ref('upcoming')
const showCalendarDialog = ref(false)
const calendarDate = ref(new Date())

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

/**
 * 根据活动时间判断实际状态
 * 即使后端返回的是upcoming，如果活动已结束，应该显示为past
 */
const getActualStatus = (event: Event): 'upcoming' | 'ongoing' | 'past' => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const endDate = new Date(event.endDate)
  endDate.setHours(23, 59, 59, 999)

  // 如果结束日期在今天之前，活动已结束
  if (endDate < today) {
    return 'past'
  }

  const startDate = new Date(event.startDate)
  startDate.setHours(0, 0, 0, 0)

  // 如果开始日期在今天之后，活动即将开始
  if (startDate > today) {
    return 'upcoming'
  }

  // 否则活动进行中
  return 'ongoing'
}

/**
 * 获取指定日期的活动列表
 */
const getEventsByDate = (dateStr: string): Event[] => {
  return allEvents.value.filter(event => {
    const eventStart = new Date(event.startDate)
    const eventEnd = new Date(event.endDate)
    const targetDate = new Date(dateStr)

    eventStart.setHours(0, 0, 0, 0)
    eventEnd.setHours(23, 59, 59, 999)
    targetDate.setHours(0, 0, 0, 0)

    return targetDate >= eventStart && targetDate <= eventEnd
  })
}

/**
 * 处理日历中活动点击
 */
const handleCalendarEventClick = (eventId: number) => {
  router.push(`/event/${eventId}`)
  showCalendarDialog.value = false
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

    // 根据实际状态过滤活动，确保每个标签页只显示对应状态的活动
    // 例如：已结束的活动不应该显示在"即将开始"页面
    const filteredEvents = eventsList.filter(event => {
      const actualStatus = getActualStatus(event)
      return actualStatus === activeTab.value
    })

    events.value = filteredEvents
    // 注意：这里使用后端返回的总数，虽然可能不准确，但分页逻辑仍然可用
    // 如果过滤后没有活动，会显示空状态
    total.value = response.total || 0

    // 如果是"即将开始"标签页，加载所有即将开始的活动用于日历显示
    if (activeTab.value === 'upcoming') {
      loadAllUpcomingEvents()
    } else {
      // 清空日历活动列表
      allEvents.value = []
    }
  } catch (error) {
    console.error('Failed to load events:', error)
    events.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 加载所有即将开始的活动用于日历显示
 */
const loadAllUpcomingEvents = async () => {
  try {
    const response = await getEvents({
      status: 'upcoming',
      page: 1,
      size: 1000, // 加载足够多的活动
    })
    const eventsList = (response.list || []).map(event => ({
      ...event,
      status: event.status?.toLowerCase() as 'upcoming' | 'ongoing' | 'past',
      type: event.type?.toLowerCase() as 'exhibition' | 'performance' | 'workshop' | 'tour',
    }))

    // 只保留真正即将开始的活动
    allEvents.value = eventsList.filter(event => getActualStatus(event) === 'upcoming')
  } catch (error) {
    console.error('Failed to load all upcoming events:', error)
    allEvents.value = []
  }
}

/**
 * 当打开日历时，确保加载了活动数据
 */
const handleCalendarOpen = () => {
  if (allEvents.value.length === 0 && activeTab.value === 'upcoming') {
    loadAllUpcomingEvents()
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
  // 检查登录状态
  if (!requireAuth('请先登录后再报名', router)) {
    return
  }

  try {
    await registerEvent(id)
    ElMessage.success('报名成功')
    loadEvents()
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
  loadEvents()
  // 如果是"即将开始"标签页，也加载所有活动用于日历
  if (activeTab.value === 'upcoming') {
    loadAllUpcomingEvents()
  }
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

.tabs-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  :deep(.el-tabs__header) {
    margin: 0;
  }
}

.calendar-btn {
  margin-left: 16px;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 4px;
}

.calendar-date {
  font-size: 12px;
  margin-bottom: 4px;
}

.calendar-events {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  overflow: hidden;
}

.calendar-event-tag {
  cursor: pointer;
  font-size: 10px;
  padding: 2px 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
</style>
