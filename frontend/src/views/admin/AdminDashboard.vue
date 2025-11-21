<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.users }}
              </div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon culture-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.culture }}
              </div>
              <div class="stat-label">文化资源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon event-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.events }}
              </div>
              <div class="stat-label">活动总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon post-icon">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.posts }}
              </div>
              <div class="stat-label">社区投稿</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px" class="route-analytics-row">
      <el-col :span="8">
        <el-card v-loading="routeInsightsLoading">
          <template #header>
            <div class="card-header">
              <span>路线主题统计</span>
              <el-tag v-if="routeInsights?.summary" size="small" effect="plain">
                共 {{ routeInsights?.summary.totalRoutes ?? 0 }} 条
              </el-tag>
            </div>
          </template>
          <div v-if="routeInsights" class="theme-stats">
            <div class="summary-metrics">
              <div class="metric-item">
                <div class="metric-value">
                  {{ routeInsights.summary?.avgDuration?.toFixed(1) ?? '--' }} 天
                </div>
                <div class="metric-label">平均天数</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">
                  {{ routeInsights.summary?.avgDistance?.toFixed(0) ?? '--' }} km
                </div>
                <div class="metric-label">平均里程</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">
                  {{ formatPercent(routeInsights.summary?.budgetCoverageRate ?? 0) }}
                </div>
                <div class="metric-label">预算覆盖率</div>
              </div>
            </div>
            <div
              v-for="stat in routeInsights.themeStats"
              :key="stat.theme"
              class="theme-stat-item"
            >
              <div class="stat-top">
                <span>{{ stat.label || stat.theme }}</span>
                <span>{{ stat.count }} 条 · {{ formatPercent(stat.percent) }}</span>
              </div>
              <el-progress :percentage="Number((stat.percent * 100).toFixed(1))" />
            </div>
          </div>
          <div v-else class="empty-state">
            <el-empty description="暂无路线数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-loading="routeInsightsLoading">
          <template #header>
            <div class="card-header">
              <span>预算 / 天数偏好</span>
            </div>
          </template>
          <div v-if="routeInsights">
            <el-tabs type="border-card">
              <el-tab-pane label="预算偏好">
                <div class="preference-list">
                  <div
                    v-for="pref in routeInsights.budgetPreferences"
                    :key="pref.label"
                    class="preference-item"
                  >
                    <div class="pref-label">{{ pref.label }}</div>
                    <div class="pref-value">{{ formatPercent(pref.percent) }}</div>
                    <el-progress
                      :percentage="Number((pref.percent * 100).toFixed(1))"
                      :stroke-width="10"
                    />
                  </div>
                </div>
              </el-tab-pane>
              <el-tab-pane label="天数偏好">
                <div class="preference-list">
                  <div
                    v-for="pref in routeInsights.durationPreferences"
                    :key="pref.label"
                    class="preference-item"
                  >
                    <div class="pref-label">{{ pref.label }}</div>
                    <div class="pref-value">{{ formatPercent(pref.percent) }}</div>
                    <el-progress
                      :percentage="Number((pref.percent * 100).toFixed(1))"
                      :stroke-width="10"
                      status="success"
                    />
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
          <div v-else class="empty-state">
            <el-empty description="暂无偏好数据" :image-size="80" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-loading="routeInsightsLoading">
          <template #header>
            <div class="card-header">
              <span>Top10 热力图</span>
              <el-tag v-if="routeInsights?.summary" size="small" type="success" effect="plain">
                覆盖 {{ formatPercent(routeInsights.summary.geoCoverageRate) }}
              </el-tag>
            </div>
          </template>
          <div class="heatmap-wrapper">
            <div
              ref="heatmapContainer"
              class="heatmap-container"
              :class="{ hidden: !mapReady || !!heatmapMessage }"
            ></div>
            <div v-if="heatmapMessage || !mapReady" class="heatmap-fallback">
              <div class="heatmap-message">{{ heatmapMessage || '数据加载中' }}</div>
              <div class="heatmap-list">
                <div
                  v-for="(routePoint, index) in heatmapFallbackPoints"
                  :key="routePoint.routeId ?? index"
                  class="heatmap-list-item"
                >
                  <div class="item-rank">{{ index + 1 }}</div>
                  <div class="item-info">
                    <div class="item-title">{{ routePoint.routeTitle || '未命名路线' }}</div>
                    <div class="item-meta">
                      <span>{{ routePoint.startLocation || '-' }} → {{ routePoint.endLocation || '-' }}</span>
                      <span>热度 {{ Math.round(routePoint.intensity || 0) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px" class="ops-row">
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待审核的社区投稿</span>
              <el-button link type="primary" @click="$router.push('/admin/posts?status=pending')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="pendingPostsLoading" class="dashboard-list">
            <div v-if="pendingPosts.length === 0" class="empty-state">
              <el-empty description="暂无待审核的投稿" :image-size="80" />
            </div>
            <div
              v-for="post in pendingPosts"
              :key="post.id"
              class="dashboard-item"
              @click="$router.push('/admin/posts')"
            >
              <div class="item-content">
                <div class="item-title">{{ post.title }}</div>
                <div class="item-meta">
                  <span>作者：{{ post.author?.username }}</span>
                  <span>{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>正在进行的活动</span>
              <el-button link type="primary" @click="$router.push('/admin/events?status=ongoing')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="ongoingEventsLoading" class="dashboard-list">
            <div v-if="ongoingEvents.length === 0" class="empty-state">
              <el-empty description="暂无正在进行的活动" :image-size="80" />
            </div>
            <div
              v-for="event in ongoingEvents"
              :key="event.id"
              class="dashboard-item"
              @click="$router.push('/admin/events')"
            >
              <div class="item-content">
                <div class="item-title">{{ event.title }}</div>
                <div class="item-meta">
                  <span>{{ event.startDate }} - {{ event.endDate }}</span>
                  <span v-if="event.capacity">
                    已报名：{{ event.registered }}/{{ event.capacity }}
                  </span>
                </div>
              </div>
              <el-tag type="success" size="small">进行中</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="4">
        <el-card class="operation-card">
          <template #header>
            <div class="card-header">
              <span>运营提醒</span>
            </div>
          </template>
          <div class="operation-overview">
            <div class="overview-item">
              <div class="overview-value">{{ pendingPosts.length }}</div>
              <div class="overview-label">待审投稿</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ ongoingEvents.length }}</div>
              <div class="overview-label">进行中活动</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ Math.max(stats.events - ongoingEvents.length, 0) }}</div>
              <div class="overview-label">其他活动</div>
            </div>
            <div class="overview-footer">
              <span>上次刷新：{{ lastUpdated || '同步中' }}</span>
              <el-button
                type="primary"
                link
                :loading="pendingPostsLoading || ongoingEventsLoading || routeInsightsLoading"
                @click="loadDashboardData"
              >
                重新获取
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { User, Document, Calendar, ChatLineRound, ArrowRight } from '@element-plus/icons-vue'
import { getDashboardStats, getPendingPosts, getOngoingEvents, getRouteDashboardInsights } from '@/api/admin'
import type { CommunityPost } from '@/types/community'
import type { Event } from '@/types/event'
import type { RouteAnalytics } from '@/types/route'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils'
import { loadAMapScript } from '@/utils/amap'

const stats = ref({
  users: 0,
  culture: 0,
  events: 0,
  posts: 0,
})

const pendingPosts = ref<CommunityPost[]>([])
const ongoingEvents = ref<Event[]>([])
const pendingPostsLoading = ref(false)
const ongoingEventsLoading = ref(false)
const routeInsights = ref<RouteAnalytics | null>(null)
const routeInsightsLoading = ref(false)
const heatmapContainer = ref<HTMLDivElement | null>(null)
const heatmapMessage = ref<string | null>('加载中...')
const mapReady = ref(false)
const lastUpdated = ref('')
let mapInstance: any = null
let heatmapInstance: any = null

const geoHeatmapPoints = computed(() => {
  return routeInsights.value?.heatmapPoints.filter(point => point.lat != null && point.lng != null) ?? []
})

const heatmapFallbackPoints = computed(() => {
  return (routeInsights.value?.heatmapPoints ?? []).slice(0, 5)
})

const formatPercent = (value?: number | null) => {
  if (!value) {
    return '0%'
  }
  return `${(value * 100).toFixed(1)}%`
}

const updateTimestamp = () => {
  const pad = (num: number) => num.toString().padStart(2, '0')
  const now = new Date()
  lastUpdated.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(
    now.getHours()
  )}:${pad(now.getMinutes())}`
}

const destroyHeatmap = () => {
  if (heatmapInstance) {
    heatmapInstance.setMap(null)
    heatmapInstance = null
  }
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  mapReady.value = false
}

const renderHeatmap = async () => {
  if (!routeInsights.value) return
  const points = geoHeatmapPoints.value
  if (points.length === 0) {
    heatmapMessage.value = '暂无坐标数据，已切换列表模式'
    destroyHeatmap()
    return
  }

  try {
    await loadAMapScript()
    await nextTick()
    const AMap = (window as any).AMap
    if (!heatmapContainer.value || !AMap) {
      heatmapMessage.value = '地图组件不可用，显示列表视图'
      destroyHeatmap()
      return
    }

    if (!mapInstance) {
      mapInstance = new AMap.Map(heatmapContainer.value, {
        viewMode: '3D',
        center: [87.627704, 43.793026],
        zoom: 5.5,
        resizeEnable: true,
      })
    }

    if (!heatmapInstance && AMap.HeatMap) {
      heatmapInstance = new AMap.HeatMap(mapInstance, {
        radius: 35,
        opacity: [0, 0.85],
      })
    }

    if (!heatmapInstance) {
      heatmapMessage.value = '热力图插件不可用，显示列表视图'
      destroyHeatmap()
      return
    }

    const maxIntensity = points.reduce((max, point) => Math.max(max, point.intensity || 1), 1)
    heatmapInstance.setDataSet({
      data: points.map(point => ({
        lng: point.lng,
        lat: point.lat,
        count: Math.max(point.intensity || 1, 1),
      })),
      max: Math.max(maxIntensity, 100),
    })
    mapInstance.setFitView()
    mapReady.value = true
    heatmapMessage.value = null
  } catch (error) {
    console.error('Failed to render route heatmap:', error)
    heatmapMessage.value = '地图加载失败，显示列表视图'
    destroyHeatmap()
  }
}

const loadRouteInsights = async () => {
  routeInsightsLoading.value = true
  try {
    const insights = await getRouteDashboardInsights()
    routeInsights.value = insights
    await renderHeatmap()
  } catch (error) {
    console.error('Failed to load route analytics:', error)
    heatmapMessage.value = '路线分析数据加载失败'
    ElMessage.error('加载路线分析数据失败')
  } finally {
    routeInsightsLoading.value = false
  }
}

const loadDashboardData = async () => {
  try {
    // 加载统计数据
    const statsData = await getDashboardStats()
    stats.value = statsData

    // 加载待审核投稿
    pendingPostsLoading.value = true
    try {
      const posts = await getPendingPosts(5)
      pendingPosts.value = posts
    } catch (error) {
      console.error('Failed to load pending posts:', error)
      ElMessage.error('加载待审核投稿失败')
    } finally {
      pendingPostsLoading.value = false
    }

    // 加载正在进行的活动
    ongoingEventsLoading.value = true
    try {
      const events = await getOngoingEvents(5)
      ongoingEvents.value = events
    } catch (error) {
      console.error('Failed to load ongoing events:', error)
      ElMessage.error('加载正在进行的活动失败')
    } finally {
      ongoingEventsLoading.value = false
    }
  } catch (error) {
    console.error('Failed to load dashboard stats:', error)
    ElMessage.error('加载统计数据失败')
  }

  await loadRouteInsights()
  updateTimestamp()
}

onMounted(() => {
  loadDashboardData()
})

onBeforeUnmount(() => {
  destroyHeatmap()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;

        &.user-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.culture-icon {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.event-icon {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.post-icon {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .dashboard-list {
    min-height: 200px;
  }

  .ops-row {
    .el-card {
      height: 100%;
    }
  }

  .operation-card {
    .operation-overview {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .overview-item {
      text-align: center;
      padding: 8px 0;
      border: 1px dashed #ebeef5;
      border-radius: 8px;

      .overview-value {
        font-size: 24px;
        font-weight: 600;
        color: #409eff;
      }

      .overview-label {
        font-size: 12px;
        color: #909399;
      }
    }

    .overview-footer {
      display: flex;
      flex-direction: column;
      gap: 6px;
      font-size: 12px;
      color: #909399;
      border-top: 1px dashed #ebeef5;
      padding-top: 8px;
    }
  }

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  .dashboard-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
      padding-left: 8px;
      padding-right: 8px;
      margin-left: -8px;
      margin-right: -8px;
      border-radius: 4px;
    }

    &:last-child {
      border-bottom: none;
    }

    .item-content {
      flex: 1;
      min-width: 0;

      .item-title {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #909399;

        span {
          white-space: nowrap;
        }
      }
    }
  }

  .route-analytics-row {
    .theme-stats {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .summary-metrics {
      display: flex;
      gap: 16px;
      padding-bottom: 4px;
      border-bottom: 1px dashed #ebeef5;

      .metric-item {
        flex: 1;
        text-align: center;

        .metric-value {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }

        .metric-label {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }

    .theme-stat-item {
      .stat-top {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        margin-bottom: 6px;
        color: #606266;
      }
    }

    .preference-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .preference-item {
        .pref-label {
          font-size: 13px;
          color: #303133;
        }

        .pref-value {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }
      }
    }

    .heatmap-wrapper {
      position: relative;
      min-height: 260px;
    }

    .heatmap-container {
      width: 100%;
      height: 260px;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #ebeef5;
      transition: opacity 0.2s;

      &.hidden {
        opacity: 0;
        pointer-events: none;
      }
    }

    .heatmap-fallback {
      position: absolute;
      inset: 0;
      border-radius: 8px;
      background: #f8f9fb;
      border: 1px dashed #dcdfe6;
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .heatmap-message {
      font-size: 13px;
      color: #909399;
    }

    .heatmap-list {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .heatmap-list-item {
        display: flex;
        gap: 10px;
        padding: 8px;
        background: #fff;
        border-radius: 6px;
        border: 1px solid #ebeef5;

        .item-rank {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #409eff;
          color: #fff;
          font-size: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .item-info {
          flex: 1;

          .item-title {
            font-size: 13px;
            color: #303133;
            margin-bottom: 4px;
          }

          .item-meta {
            font-size: 12px;
            color: #909399;
            display: flex;
            flex-direction: column;
            gap: 2px;
          }
        }
      }
    }
  }
}
</style>
