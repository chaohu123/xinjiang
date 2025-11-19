<template>
  <div class="map-view">
    <div id="map" ref="mapContainer" class="map-canvas" />

    <RouteCard
      :visible="cardVisible"
      :map="map"
      :point="activePoint"
      :name="activePoint?.name || ''"
      :info="activePoint?.info || ''"
      :image="activePoint?.image"
      :order="activePoint?.order || 1"
    />

    <div class="control-panel">
      <el-card shadow="hover">
        <template #header>
          <div class="panel-title">
            <span>智能旅游路线</span>
            <el-button
              link
              size="small"
              :loading="loadingRoutes"
              @click="loadRouteOptions"
            >
              刷新列表
            </el-button>
          </div>
        </template>

        <div class="panel-section">
          <label class="panel-label">选择路线</label>
          <el-select
            v-model="selectedRouteId"
            placeholder="请选择"
            :loading="loadingRoutes || loadingRoute"
            @change="handleRouteChange"
          >
            <el-option
              v-for="route in routeOptions"
              :key="route.id"
              :label="route.title"
              :value="route.id"
            >
              <div class="route-option">
                <strong>{{ route.title }}</strong>
                <small>{{ route.startLocation }} → {{ route.endLocation }}</small>
              </div>
            </el-option>
          </el-select>
        </div>

        <div v-if="activeRoute" class="panel-section meta">
          <p>
            <span>总天数</span>
            <strong>{{ activeRoute.itinerary?.length || 0 }} 天</strong>
          </p>
          <p>
            <span>路线距离</span>
            <strong>{{ activeRoute.distance || '--' }} km</strong>
          </p>
          <p>
            <span>站点数量</span>
            <strong>{{ filteredPoints.length }} 站</strong>
          </p>
        </div>

        <div v-if="activeRoute" class="panel-section view-switch">
          <label class="panel-label">展示范围</label>
          <el-radio-group v-model="viewMode" size="small" @change="handleViewModeChange">
            <el-radio-button label="full">完整路线</el-radio-button>
            <el-radio-button
              v-for="option in dayOptions"
              :key="option.value"
              :label="option.value"
            >
              {{ option.label }}
            </el-radio-button>
          </el-radio-group>
        </div>

        <el-alert
          v-if="routeError"
          type="warning"
          show-icon
          :closable="false"
          :title="routeError"
        />
      </el-card>
    </div>

    <div v-if="!routeOptions.length && !loadingRoutes" class="empty-hint">
      <p>暂无可演示的路线，请先生成路线后再试。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import RouteCard from '@/components/RouteCard.vue'
import { useMap } from '@/hooks/useMap'
import { getMyRoutes, getRouteDetail } from '@/api/route'
import type { Route, RouteDetail } from '@/types/route'

interface RoutePoint {
  id: string
  lng: number
  lat: number
  name: string
  info?: string
  image?: string
  order: number
  day: number
  dayTitle: string
  ticketPrice?: number | string
  bestTime?: string
  duration?: string
  featureActivity?: string
  alternate?: string
  tags?: string[]
}

const { map, mapContainer, mapReady, initializeMap } = useMap()

const routeOptions = ref<Route[]>([])
const selectedRouteId = ref<number | null>(null)
const activeRoute = ref<RouteDetail | null>(null)
const loadingRoutes = ref(false)
const loadingRoute = ref(false)
const routeError = ref('')

const routePoints = ref<RoutePoint[]>([])
const activePoint = ref<RoutePoint | null>(null)
const viewMode = ref('full')
const cardVisible = computed(() => !!activePoint.value)
const filteredPoints = computed<RoutePoint[]>(() => {
  if (viewMode.value === 'full') {
    return routePoints.value
  }
  const dayNumber = Number(viewMode.value)
  return routePoints.value.filter((point) => point.day === dayNumber)
})
const dayOptions = computed(() =>
  activeRoute.value?.itinerary?.map((day) => ({
    value: String(day.day),
    label: day.title ? `第${day.day}天 · ${day.title}` : `第${day.day}天`,
  })) || []
)

const routePolyline = ref<AMap.Polyline | null>(null)
const routeMarkers = ref<AMap.Marker[]>([])

const focusOnPoint = (point: RoutePoint | null, options: { pan?: boolean } = {}) => {
  activePoint.value = point
  if (point && options.pan !== false && map.value) {
    map.value.panTo([point.lng, point.lat])
  }
}

const loadRouteOptions = async () => {
  loadingRoutes.value = true
  try {
    const response = await getMyRoutes({ page: 1, size: 10 })
    routeOptions.value = response.list || []
    if (!routeOptions.value.length) {
      selectedRouteId.value = null
      activeRoute.value = null
      routePoints.value = []
      viewMode.value = 'full'
      clearRouteGraphics()
      focusOnPoint(null, { pan: false })
      ElMessage.info('暂无个人智能旅游路线，请先生成后再查看地图')
      return
    }

    const currentExists = routeOptions.value.some((route) => route.id === selectedRouteId.value)
    if (!currentExists) {
      selectedRouteId.value = routeOptions.value[0].id
    }

    if (selectedRouteId.value) {
      await loadRouteDetail(selectedRouteId.value)
    }
  } catch (error: any) {
    console.error('[Map] 加载路线列表失败', error)
    ElMessage.error(error?.message || '获取我的路线失败，请稍后再试')
  } finally {
    loadingRoutes.value = false
  }
}

const handleRouteChange = async (routeId: number) => {
  if (!routeId) return
  await loadRouteDetail(routeId)
}

const loadRouteDetail = async (routeId: number) => {
  if (!routeId) return
  loadingRoute.value = true
  routeError.value = ''
  try {
    const detail = await getRouteDetail(routeId)
    activeRoute.value = detail
    viewMode.value = 'full'
    const parsed = parseRoutePoints(detail)
    if (!parsed.length) {
      routeError.value = '该路线缺少有效坐标点，请重新生成智能旅游路线以返回完整经纬度数据'
      clearRouteGraphics()
      routePoints.value = []
      focusOnPoint(null, { pan: false })
      return
    }
    routePoints.value = parsed
    focusOnPoint(null, { pan: false })
    renderRouteGraphics(filteredPoints.value)
  } catch (error: any) {
    console.error('[Map] 加载路线详情失败', error)
    ElMessage.error(error?.message || '获取路线详情失败')
  } finally {
    loadingRoute.value = false
  }
}

const isValidNumber = (value: unknown): value is number =>
  typeof value === 'number' && Number.isFinite(value)

const parseRoutePoints = (route: RouteDetail | null): RoutePoint[] => {
  if (!route?.itinerary?.length) return []
  const result: RoutePoint[] = []
  route.itinerary.forEach((day) => {
    day.locations?.forEach((location, index) => {
      const lng = Number(location.lng)
      const lat = Number(location.lat)
      if (!isValidNumber(lng) || !isValidNumber(lat)) return
      result.push({
        id: `${route.id}-${day.day}-${index}`,
        lng,
        lat,
        name: location.name || `站点 ${index + 1}`,
        info: location.description || day.description || route.description || '',
        image: location.coverImage,
        order: result.length + 1,
        day: day.day,
        dayTitle: day.title || '',
        ticketPrice:
          (location as any).ticketPrice ??
          (day as any)?.ticketPrice ??
          (route as any)?.ticketPrice ??
          extractTicketPrice(location.description) ??
          extractTicketPrice(day.description) ??
          extractTicketPrice(route.description),
        bestTime:
          (location as any).bestTime ??
          (day as any)?.bestTime ??
          extractBestTime(location.description) ??
          extractBestTime(day.description) ??
          extractBestTime(route.description),
        duration:
          (location as any).duration ??
          (location as any).visitDuration ??
          (day as any)?.duration ??
          extractDuration(location.description) ??
          extractDuration(day.description) ??
          extractDuration(route.description),
        featureActivity:
          (location as any).featureActivity ??
          (day as any)?.featureActivity ??
          extractFeatureActivity(location.description) ??
          extractFeatureActivity(day.description) ??
          extractFeatureActivity(route.description),
        alternate:
          (location as any).alternatePlan ??
          (day as any)?.alternatePlan ??
          extractAlternatePlan(location.description) ??
          extractAlternatePlan(day.description) ??
          extractAlternatePlan(route.description),
        tags: normalizeTags((location as any).tags, day.title),
      })
    })
  })
  return result
}

const normalizeTags = (tags: unknown, dayTitle?: string): string[] => {
  if (Array.isArray(tags) && tags.length) {
    return tags.slice(0, 3)
  }
  if (typeof tags === 'string' && tags.trim()) {
    return tags
      .split(/[，,\/]/)
      .map((item) => item.trim())
      .filter(Boolean)
      .slice(0, 3)
  }
  const fallback: string[] = []
  if (dayTitle?.includes('文化')) fallback.push('文化体验')
  if (dayTitle?.includes('美食')) fallback.push('风味餐饮')
  if (dayTitle?.includes('自然')) fallback.push('自然风光')
  if (!fallback.length) fallback.push('精选打卡')
  return fallback
}

const extractTicketPrice = (text?: string | null): string | undefined => {
  if (!text) return undefined
  const priceMatch = text.match(/门票(?:价格)?[：:\s]*([0-9]+(?:\.[0-9]+)?)\s*元/)
  if (priceMatch) {
    return `${priceMatch[1]}元`
  }
  const simpleMatch = text.match(/([0-9]+(?:\.[0-9]+)?)\s*元/)
  if (simpleMatch) {
    return `${simpleMatch[1]}元`
  }
  return undefined
}

const extractBestTime = (text?: string | null): string | undefined => {
  if (!text) return undefined
  const match = text.match(/最佳(?:游览)?时间[：:\s]*([^\s，。；]+)/)
  if (match) return match[1]
  const rangeMatch = text.match(/(上午|下午|傍晚|夜间)\s*([0-9]+(?:-[0-9]+)?点?)/)
  if (rangeMatch) return `${rangeMatch[1]}${rangeMatch[2]}`
  return undefined
}

const extractDuration = (text?: string | null): string | undefined => {
  if (!text) return undefined
  const match = text.match(/(建议)?(?:游览|停留)(?:时长|时间)[：:\s]*([0-9]+(?:\.[0-9]+)?\s*(?:小时|h|分钟|min))/i)
  if (match) return match[2].replace(/min/i, '分钟').replace(/h/i, '小时')
  const simpleMatch = text.match(/([0-9]+(?:\.[0-9]+)?\s*(?:小时|分钟))/)
  if (simpleMatch) return simpleMatch[1]
  return undefined
}

const extractFeatureActivity = (text?: string | null): string | undefined => {
  return extractLabelContent(text, /(特色活动|必玩体验)/)
}

const extractAlternatePlan = (text?: string | null): string | undefined => {
  const label = extractLabelContent(text, /(替代方案|备选)/)
  if (label) return label
  const bracketMatch = text?.match(/【替代方案】([^【]+)/)
  if (bracketMatch) return bracketMatch[1].trim()
  return undefined
}

const extractLabelContent = (text?: string | null, label: RegExp): string | undefined => {
  if (!text) return undefined
  const regex = new RegExp(`${label.source}[：: ]*([^。；\\n]+)`)
  const match = text.match(regex)
  if (match) {
    const content = match[match.length - 1]?.trim()
    if (content) {
      return content.replace(/^】+/, '').replace(/】+$/, '').trim()
    }
  }
  return undefined
}

const createStopMarker = (point: RoutePoint) => {
  const el = document.createElement('div')
  el.className = 'route-stop-badge'
  el.textContent = String(point.order)
  const marker = new window.AMap.Marker({
    position: [point.lng, point.lat],
    content: el,
    offset: [-12, -12],
  })
  marker.on('click', () => {
    focusOnPoint(point)
  })
  return marker
}

const clearRouteGraphics = () => {
  if (routePolyline.value) {
    routePolyline.value.setMap(null)
    routePolyline.value = null
  }
  routeMarkers.value.forEach((marker) => marker.setMap(null))
  routeMarkers.value = []
}

const renderRouteGraphics = (points: RoutePoint[] = filteredPoints.value) => {
  if (!map.value || !mapReady.value) return
  clearRouteGraphics()
  if (!points.length) return

  const path = points.map((point) => [point.lng, point.lat])
  routePolyline.value = new window.AMap.Polyline({
    path,
    strokeColor: '#53d8fb',
    strokeOpacity: 0.9,
    strokeWeight: 6,
    lineJoin: 'round',
    lineCap: 'round',
  })
  routePolyline.value.setMap(map.value)

  routeMarkers.value = points.map((point) => {
    const marker = createStopMarker(point)
    marker.setMap(map.value!)
    return marker
  })

  map.value.setFitView([routePolyline.value, ...routeMarkers.value], false, [80, 80, 80, 80])
}

const handleViewModeChange = () => {
  if (!filteredPoints.value.length) {
    ElMessage.info('该天暂无可展示的坐标点')
    focusOnPoint(null, { pan: false })
    return
  }
  if (activePoint.value && !filteredPoints.value.some((point) => point.id === activePoint.value?.id)) {
    focusOnPoint(null, { pan: false })
  }
}

watch(filteredPoints, (points) => {
  if (!points.length) {
    focusOnPoint(null, { pan: false })
    return
  }
  const stillExists = points.some((point) => point.id === activePoint.value?.id)
  if (!stillExists) {
    focusOnPoint(points[0], { pan: false })
  } else {
    const current = points.find((point) => point.id === activePoint.value?.id)
    if (current) {
      activePoint.value = current
    }
  }
})

watch(
  [filteredPoints, mapReady],
  ([points, ready]) => {
    if (ready) {
      renderRouteGraphics(points as RoutePoint[])
    }
  },
  { immediate: true, deep: true }
)

onMounted(async () => {
  await initializeMap()
  loadRouteOptions()
})
</script>

<style scoped lang="scss">
.map-view {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #0b1524;
}

.map-canvas {
  position: absolute;
  inset: 0;
}

.control-panel {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 340px;
  z-index: 5;

  :deep(.el-card) {
    border-radius: 16px;
  }
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.panel-section {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-section.view-switch {
  :deep(.el-radio-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  :deep(.el-radio-button__inner) {
    padding: 6px 16px;
  }
}

.panel-label {
  font-size: 13px;
  color: #606266;
}

.route-option {
  display: flex;
  flex-direction: column;
  gap: 2px;

  strong {
    font-size: 14px;
  }

  small {
    color: #909399;
  }
}

.panel-section.meta {
  border-top: 1px solid #f0f2f5;
  padding-top: 12px;

  p {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin: 0 0 6px;

    strong {
      color: #303133;
    }
  }
}

.empty-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 24px 32px;
  border-radius: 12px;
  text-align: center;
  letter-spacing: 0.5px;
}

:global(.route-stop-badge) {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.45);
  cursor: pointer;
}
</style>

