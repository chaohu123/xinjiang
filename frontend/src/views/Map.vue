<template>
  <div class="map-page">
    <div ref="mapContainer" class="map-container" />
    <div class="map-sidebar">
      <el-card>
        <template #header>
          <h3>{{ $t('map.title') }}</h3>
        </template>
        <el-input
          v-model="searchPlace"
          :placeholder="$t('map.searchPlaceholder')"
          :loading="loading"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          type="primary"
          style="width: 100%; margin-top: 12px"
          @click="handleSearch"
          :loading="loading"
        >
          搜索
        </el-button>
        <div class="map-layers">
          <h4>{{ $t('map.layers') }}</h4>
          <el-checkbox-group v-model="selectedLayers">
            <el-checkbox value="markers">
              {{ $t('map.markers') }}
            </el-checkbox>
            <el-checkbox value="routes">
              {{ $t('map.routes') }}
            </el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="category-filters">
          <h4>分类筛选</h4>
          <div class="filter-tags">
            <el-check-tag
              v-for="filter in CATEGORY_FILTERS"
              :key="filter.key"
              :checked="activeCategory === filter.key"
              @change="() => (activeCategory = filter.key)"
            >
              <span class="filter-tag-label">
                {{ filter.label }}
                <small v-if="getCategoryCount(filter.key)">{{ getCategoryCount(filter.key) }}</small>
              </span>
            </el-check-tag>
          </div>
        </div>

        <div v-if="routeOptions.length || routeOptionsLoading || featuredRoute" class="route-playback-card">
          <h4>
            路线动画
            <template v-if="featuredRoute"> · {{ featuredRoute.title }}</template>
          </h4>
          <p v-if="featuredRoute" class="route-meta">
            {{ featuredRoute.startLocation }} → {{ featuredRoute.endLocation }}
          </p>
          <div class="route-selector">
            <el-select
              v-model="selectedRouteId"
              placeholder="选择路线"
              size="small"
              :loading="routeOptionsLoading || routePlaybackLoading"
              :disabled="routeOptionsLoading || !routeOptions.length"
              @change="handleRouteSelection"
            >
              <el-option
                v-for="route in routeOptions"
                :key="route.id"
                :label="route.title"
                :value="route.id"
              >
                <div class="route-option">
                  <span>{{ route.title }}</span>
                  <small>{{ route.duration }} 天 · {{ route.startLocation }} → {{ route.endLocation }}</small>
                </div>
              </el-option>
            </el-select>
            <el-button
              link
              size="small"
              :loading="routeOptionsLoading"
              @click="loadRouteOptions"
            >
              刷新列表
            </el-button>
          </div>
          <el-alert
            v-if="!routeOptionsLoading && !routeOptions.length"
            type="info"
            show-icon
            :closable="false"
            title="暂无可播放路线，请先生成路线"
            class="route-alert"
          />
          <div v-if="featuredRoute" class="playback-status">
            <span>第 {{ currentDayIndex + 1 }} / {{ featuredRoute.itinerary.length }} 天</span>
            <el-button-group>
              <el-button size="small" @click="changeRouteDay(-1)">前一天</el-button>
              <el-button size="small" @click="changeRouteDay(1)">后一天</el-button>
            </el-button-group>
          </div>
          <el-button
            v-if="featuredRoute"
            text
            type="primary"
            size="small"
            @click="togglePlayback"
          >
            {{ playbackTimer ? '停止播放' : '自动播放' }}
          </el-button>
          <el-button v-if="featuredRoute" text size="small" @click="routeDetailVisible = true">查看详情</el-button>

          <div v-if="featuredRoute && timelineLength > 1" class="route-timeline-control">
            <el-slider
              v-model="timelineCursor"
              :min="0"
              :max="timelineLength - 1"
              :step="1"
              :marks="timelineMarks"
              show-stops
            />
            <ul class="timeline-meta">
              <li v-for="item in routeTimeline" :key="item.day">
                <strong>{{ item.label }}</strong>
                <small>{{ item.startTime }} - {{ item.endTime }} · {{ item.stopCount }} 站</small>
              </li>
            </ul>
          </div>
        </div>

        <div v-if="currentRouteDay" class="route-day-detail">
          <div class="detail-header">
            <div>
              <h4>第 {{ currentRouteDay.day }} 天 · {{ currentRouteDay.title }}</h4>
              <p>{{ currentRouteDay.description }}</p>
            </div>
            <el-tag size="small" type="success">{{ currentDayLocations.length }} 个打卡点</el-tag>
          </div>
          <ul class="route-stop-list">
            <li
              v-for="(location, index) in currentDayLocations"
              :key="location.name + index"
              class="route-stop"
            >
              <span class="stop-index">{{ index + 1 }}</span>
              <div class="stop-info">
                <strong>{{ location.name }}</strong>
                <small>{{ location.description || '点击定位查看地图路径' }}</small>
              </div>
              <el-button text size="small" @click="focusOnLocation(location)">定位</el-button>
            </li>
          </ul>
        </div>
      </el-card>
    </div>

    <el-drawer
      v-model="routeDetailVisible"
      title="路线详情"
      size="420px"
      append-to-body
    >
      <template v-if="featuredRoute">
        <div class="route-summary">
          <p>
            <strong>路线概览：</strong>{{ featuredRoute.duration }} 天 ·
            {{ featuredRoute.distance }} 公里
          </p>
          <p><strong>主题：</strong>{{ featuredRoute.theme }}</p>
          <p><strong>起止：</strong>{{ featuredRoute.startLocation }} → {{ featuredRoute.endLocation }}</p>
        </div>
        <div
          v-for="(day, index) in featuredRoute.itinerary"
          :key="day.day"
          class="route-day-card"
        >
          <div class="route-day-card__header">
            <div>
              <strong>第 {{ day.day }} 天 · {{ day.title }}</strong>
              <p>{{ day.description }}</p>
            </div>
            <el-button text size="small" @click="jumpToRouteDay(index)">地图预览</el-button>
          </div>
          <ul>
            <li v-for="(location, locIndex) in day.locations" :key="location.name + locIndex">
              <span>{{ locIndex + 1 }}.</span>
              <span>{{ location.name }}</span>
              <small v-if="location.description"> - {{ location.description }}</small>
            </li>
          </ul>
          <p v-if="day.accommodation"><strong>住宿：</strong>{{ day.accommodation }}</p>
          <p v-if="day.transportation"><strong>交通：</strong>{{ day.transportation }}</p>
        </div>
      </template>
      <el-empty v-else description="暂无路线数据" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { loadAMapScript } from '@/utils/amap'
import { getRoutes, getRouteDetail, getMyRoutes } from '@/api/route'
import { getMapPois } from '@/api/map'
import type { MapPoi } from '@/types/map'
import type { RouteDetail, ItineraryItem, RouteTimelineItem, Route } from '@/types/route'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const mapContainer = ref<HTMLElement>()
const searchPlace = ref('')
const selectedLayers = ref(['markers', 'routes'])
const map = ref<AMap.Map | null>(null)
const markers = ref<AMap.Marker[]>([])
const markerCluster = ref<AMap.MarkerClusterer | null>(null)
const loading = ref(false)
const mapLoaded = ref(false)
const activeCategory = ref('all')
const mapPois = ref<MapPoi[]>([])
const poiStats = ref<Record<string, number>>({})

const CATEGORY_FILTERS = [
  { key: 'all', label: '全部类型' },
  { key: 'scenic', label: '景区' },
  { key: 'relic', label: '遗址' },
  { key: 'museum', label: '博物馆' },
  { key: 'heritage', label: '非遗点' },
]

const CATEGORY_COLORS: Record<string, string> = {
  scenic: '#409EFF',
  relic: '#E6A23C',
  museum: '#67C23A',
  heritage: '#A066FF',
}

const featuredRoute = ref<RouteDetail | null>(null)
const currentDayIndex = ref(0)
const routePolyline = ref<AMap.Polyline | null>(null)
const routeMarker = ref<AMap.Marker | null>(null)
const playbackTimer = ref<number | null>(null)
const routeStopMarkers = ref<AMap.Marker[]>([])
const routeDetailVisible = ref(false)
const infoWindow = ref<AMap.InfoWindow | null>(null)
const routeOptions = ref<Route[]>([])
const selectedRouteId = ref<number | null>(null)
const routeOptionsLoading = ref(false)
const routePlaybackLoading = ref(false)

const currentRouteDay = computed(() => featuredRoute.value?.itinerary?.[currentDayIndex.value])
const currentDayLocations = computed(() => currentRouteDay.value?.locations || [])
const routeTimeline = computed<RouteTimelineItem[]>(() => featuredRoute.value?.timeline || [])
const timelineLength = computed(() => routeTimeline.value.length)
const timelineMarks = computed(() =>
  routeTimeline.value.reduce((acc, item, index) => {
    acc[index] = item.label
    return acc
  }, {} as Record<number | string, string>)
)
const timelineCursor = ref(0)

const getCategoryCount = (key: string) => {
  if (key === 'all') {
    return poiStats.value.total ?? mapPois.value.length
  }
  return poiStats.value[key] ?? 0
}

const loadRouteDetail = async (routeId: number) => {
  if (!routeId) return
  routePlaybackLoading.value = true
  try {
    selectedRouteId.value = routeId
    featuredRoute.value = await getRouteDetail(routeId)
    currentDayIndex.value = 0
    renderRouteDay()
  } catch (error: any) {
    console.error('加载路线详情失败:', error)
    ElMessage.error('加载路线详情失败：' + (error?.message || '未知错误'))
  } finally {
    routePlaybackLoading.value = false
  }
}

const loadRouteOptions = async () => {
  routeOptionsLoading.value = true
  try {
    let list: Route[] = []
    if (userStore.isLoggedIn) {
      const mine = await getMyRoutes({ page: 1, size: 10 })
      list = mine.list || []
    }
    if (!list.length) {
      const response = await getRoutes({ page: 1, size: 5 })
      list = response.list || []
    }
    routeOptions.value = list
    if (list.length) {
      await loadRouteDetail(list[0].id)
    } else {
      selectedRouteId.value = null
      featuredRoute.value = null
    }
  } catch (error: any) {
    console.error('加载路线列表失败:', error)
    ElMessage.error('加载路线列表失败：' + (error?.message || '未知错误'))
  } finally {
    routeOptionsLoading.value = false
  }
}

const handleRouteSelection = (routeId: number) => {
  if (routeId && routeId !== selectedRouteId.value) {
    loadRouteDetail(routeId)
  }
}

const ensureInfoWindow = () => {
  if (!window.AMap) return null
  if (!infoWindow.value) {
    infoWindow.value = new window.AMap.InfoWindow({
      offset: new window.AMap.Pixel(0, -25) as any,
      closeWhenClickMap: true,
    })
  }
  return infoWindow.value
}

const showInfoWindow = (content: string, position: [number, number]) => {
  if (!map.value) return
  const infowindow = ensureInfoWindow()
  infowindow?.setContent(content)
  infowindow?.open(map.value, position)
}

const focusOnResource = (resource: MapPoi) => {
  if (!map.value) return
  map.value.setCenter([resource.lng, resource.lat])
  map.value.setZoom(Math.max(map.value.getZoom() || 11, 12))
  showInfoWindow(
    `<div class="map-info-window"><strong>${resource.title}</strong><p>${resource.region || ''}</p></div>`,
    [resource.lng, resource.lat]
  )
}

const focusOnLocation = (location: ItineraryItem['locations'][number]) => {
  if (!map.value || !location.lat || !location.lng) return
  map.value.setCenter([location.lng, location.lat])
  map.value.setZoom(Math.max(map.value.getZoom() || 11, 12))
  showInfoWindow(
    `<div class="map-info-window"><strong>${location.name}</strong><p>${
      location.description || ''
    }</p></div>`,
    [location.lng, location.lat]
  )
}

const getBoundsParams = () => {
  if (!map.value?.getBounds) return {}
  const bounds = map.value.getBounds()
  if (!bounds) return {}
  const northEast = bounds.getNorthEast?.()
  const southWest = bounds.getSouthWest?.()
  return {
    north: northEast?.getLat?.(),
    east: northEast?.getLng?.(),
    south: southWest?.getLat?.(),
    west: southWest?.getLng?.(),
  }
}

const fetchMapPois = async (options?: { keyword?: string; category?: string }) => {
  loading.value = true
  const keyword = options?.keyword ?? (searchPlace.value.trim() || undefined)
  try {
    const response = await getMapPois({
      keyword,
      category: options?.category ?? (activeCategory.value === 'all' ? undefined : activeCategory.value),
      cluster: true,
      zoom: map.value?.getZoom?.(),
      limit: 800,
      ...getBoundsParams(),
    })
    const pois = response.pois || []
    mapPois.value = pois
    poiStats.value = response.stats || {}
    updateMarkers()

    if (keyword) {
      if (pois.length && map.value) {
        const first = pois[0]
        map.value.setCenter([first.lng, first.lat])
        map.value.setZoom(Math.max(map.value.getZoom() || 11, 11))
        showInfoWindow(
          `<div class="map-info-window"><strong>${first.title}</strong><p>${first.region || ''}</p></div>`,
          [first.lng, first.lat]
        )
      } else {
        ElMessage.info('未找到相关地点')
      }
    }
  } catch (error: any) {
    console.error('加载地图资源失败:', error)
    ElMessage.error('加载地图资源失败：' + (error?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 初始化地图
const initMap = async () => {
  if (!mapContainer.value) return

  try {
    loading.value = true
    await loadAMapScript()

    if (!window.AMap) {
      throw new Error('高德地图加载失败')
    }

    map.value = new window.AMap.Map(mapContainer.value, {
      center: [87.68, 43.77],
      zoom: 10,
      viewMode: '3D',
      mapStyle: 'amap://styles/normal',
    })

    mapLoaded.value = true
    map.value.on('complete', () => {
      fetchMapPois()
      renderRouteDay()
    })

    setTimeout(() => {
      if (mapLoaded.value && mapPois.value.length === 0) {
        fetchMapPois()
        renderRouteDay()
      }
    }, 500)
  } catch (error: any) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败：' + (error?.message || '请配置 VITE_AMAP_KEY 环境变量'))
  } finally {
    loading.value = false
  }
}

// 更新地图标记点
const updateMarkers = () => {
  if (!map.value || !window.AMap) return

  // 清除现有标记
  markers.value.forEach((marker) => {
    map.value!.remove(marker)
  })
  markers.value = []
  if (markerCluster.value) {
    markerCluster.value.setMap(null)
    markerCluster.value = null
  }

  // 如果标记点图层未选中，则不显示
  if (!selectedLayers.value.includes('markers')) {
    return
  }

  const filtered = mapPois.value.filter(
    (poi) => activeCategory.value === 'all' || poi.category === activeCategory.value
  )

  filtered.forEach((poi) => {
    if (!poi.lat || !poi.lng) {
      return
    }

    const markerContent = document.createElement('div')
    markerContent.className = 'map-marker'
    markerContent.style.width = '32px'
    markerContent.style.height = '32px'
    markerContent.style.borderRadius = '50%'
    markerContent.style.backgroundColor = CATEGORY_COLORS[poi.category] || '#409EFF'
    markerContent.style.border = '2px solid #fff'
    markerContent.style.cursor = 'pointer'
    markerContent.style.boxShadow = '0 2px 4px rgba(0,0,0,0.3)'
    markerContent.style.position = 'relative'
    markerContent.title = poi.title

    const marker = new window.AMap.Marker({
      position: [poi.lng, poi.lat],
      content: markerContent,
      title: poi.title,
      offset: new window.AMap.Pixel(-16, -16) as any,
    })

    marker.on('click', () => {
      const detailType = poi.contentType || 'article'
      router.push(`/detail/${detailType}/${poi.id}`)
    })

    map.value!.add(marker)
    markers.value.push(marker)
  })

  if (markers.value.length > 0) {
    markerCluster.value = new window.AMap.MarkerClusterer(map.value, markers.value, {
      gridSize: 80,
    })
  }

  if (markers.value.length > 0) {
    const bounds = new window.AMap.Bounds()
    filtered.forEach((poi) => {
      if (poi.lng && poi.lat) {
        bounds.extend([poi.lng, poi.lat])
      }
    })
    if (bounds.getNorthEast() && bounds.getSouthWest()) {
      map.value!.setFitView(markers.value, false, [50, 50, 50, 50])
    }
  }
}

const handleSearch = () => {
  if (!searchPlace.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  fetchMapPois({ keyword: searchPlace.value })
}

// 监听图层变化
watch(selectedLayers, (layers) => {
  updateMarkers()
  if (!layers.includes('routes')) {
    clearRouteShapes()
    if (playbackTimer.value) {
      window.clearInterval(playbackTimer.value)
      playbackTimer.value = null
    }
  } else {
    renderRouteDay()
  }
})

watch(
  activeCategory,
  async () => {
    await fetchMapPois()
  },
  { immediate: false }
)

onMounted(() => {
  initMap()
  loadRouteOptions()
})

onUnmounted(() => {
  // 清理地图实例和标记
  if (map.value) {
    markers.value.forEach((marker) => {
      map.value!.remove(marker)
    })
    markers.value = []
    if (markerCluster.value) {
      markerCluster.value.setMap(null)
      markerCluster.value = null
    }
    map.value.destroy()
    map.value = null
  }
  mapLoaded.value = false
  clearRouteShapes()
  if (playbackTimer.value) {
    window.clearInterval(playbackTimer.value)
    playbackTimer.value = null
  }
  if (infoWindow.value) {
    infoWindow.value.close()
    infoWindow.value = null
  }
})

const clearRouteShapes = () => {
  if (routePolyline.value) {
    routePolyline.value.setMap(null)
    routePolyline.value = null
  }
  if (routeMarker.value) {
    routeMarker.value.setMap(null)
    routeMarker.value = null
  }
  routeStopMarkers.value.forEach((marker) => marker.setMap(null))
  routeStopMarkers.value = []
}

const renderRouteDay = () => {
  if (!selectedLayers.value.includes('routes')) return
  if (!map.value || !featuredRoute.value || !window.AMap) return
  const day = featuredRoute.value.itinerary?.[currentDayIndex.value]
  if (!day) return

  clearRouteShapes()

  const coords =
    day.locations
      ?.filter((loc) => loc.lat && loc.lng)
      .map((loc) => [loc.lng, loc.lat] as [number, number]) || []
  if (coords.length === 0) return

  routePolyline.value = new window.AMap.Polyline({
    path: coords,
    strokeColor: '#f56c6c',
    strokeWeight: 6,
    strokeOpacity: 0.85,
  })
  routePolyline.value.setMap(map.value)
  routeMarker.value = new window.AMap.Marker({
    position: coords[0],
    title: day.title,
  })
  routeMarker.value.setMap(map.value)
  routeStopMarkers.value.forEach((marker) => marker.setMap(null))
  routeStopMarkers.value = []
  day.locations?.forEach((location, index) => {
    if (!location.lat || !location.lng) return
    const stopMarkerEl = document.createElement('div')
    stopMarkerEl.className = 'route-stop-marker'
    stopMarkerEl.textContent = String(index + 1)
    const stopMarker = new window.AMap.Marker({
      position: [location.lng, location.lat],
      content: stopMarkerEl,
      offset: new window.AMap.Pixel(-12, -12) as any,
      title: location.name,
    })
    stopMarker.on('click', () => focusOnLocation(location))
    stopMarker.setMap(map.value!)
    routeStopMarkers.value.push(stopMarker)
  })
  map.value.setFitView([routePolyline.value], false, [80, 80, 80, 80])
}

const changeRouteDay = (step: number) => {
  if (!featuredRoute.value) return
  const total = featuredRoute.value.itinerary?.length || 0
  if (!total) return
  currentDayIndex.value = (currentDayIndex.value + step + total) % total
  renderRouteDay()
}

const togglePlayback = () => {
  if (!selectedLayers.value.includes('routes')) {
    selectedLayers.value.push('routes')
  }
  if (playbackTimer.value) {
    window.clearInterval(playbackTimer.value)
    playbackTimer.value = null
  } else if (featuredRoute.value) {
    playbackTimer.value = window.setInterval(() => {
      changeRouteDay(1)
    }, 4000)
  }
}

const jumpToRouteDay = (index: number) => {
  if (!featuredRoute.value) return
  currentDayIndex.value = index
  renderRouteDay()
  routeDetailVisible.value = false
}

watch(
  () => featuredRoute.value?.id,
  () => {
    timelineCursor.value = 0
    renderRouteDay()
  }
)

watch(currentDayIndex, (value) => {
  if (timelineCursor.value !== value) {
    timelineCursor.value = value
  }
})

watch(timelineCursor, (value) => {
  if (value === currentDayIndex.value) return
  currentDayIndex.value = value
  renderRouteDay()
})

watch(
  () => mapLoaded.value,
  (loaded) => {
    if (loaded) {
      renderRouteDay()
    }
  }
)
</script>

<style lang="scss" scoped>
.map-page {
  position: relative;
  height: calc(100vh - 70px);
  width: 100%;
}

.map-container {
  width: 100%;
  height: 100%;
  background: #f5f7fa;

  // 高德地图样式调整
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

.map-marker {
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 12px;
    height: 12px;
    background: #fff;
    border-radius: 50%;
  }
}

.map-sidebar {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 300px;
  z-index: 1000;
}

.map-layers {
  margin-top: 20px;

  h4 {
    margin-bottom: 12px;
    font-size: 14px;
    color: #606266;
  }
}

.category-filters {
  margin-top: 20px;

  h4 {
    margin-bottom: 10px;
    font-size: 14px;
    color: #606266;
  }

  .filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .filter-tag-label {
      display: inline-flex;
      align-items: center;
      gap: 4px;

      small {
        font-size: 11px;
        color: #909399;
      }
    }
  }
}

.route-playback-card {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;

  h4 {
    font-size: 14px;
    margin-bottom: 8px;
    color: #303133;
  }

  .route-selector {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  .route-option {
    display: flex;
    flex-direction: column;
    line-height: 1.2;

    span {
      font-size: 13px;
      color: #303133;
    }

    small {
      font-size: 11px;
      color: #909399;
    }
  }

  .route-alert {
    margin-bottom: 10px;
  }

  .route-meta {
    color: #909399;
    margin-bottom: 8px;
  }

  .playback-status {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 13px;
    color: #606266;
  }

  .route-timeline-control {
    margin-top: 12px;

    :deep(.el-slider__marks-text) {
      font-size: 11px;
    }

    .timeline-meta {
      list-style: none;
      margin: 8px 0 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 4px;

      li {
        display: flex;
        flex-direction: column;
        font-size: 12px;
        color: #909399;

        strong {
          color: #303133;
        }
      }
    }
  }
}

.route-day-detail {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;

  .detail-header {
    display: flex;
    justify-content: space-between;
    gap: 12px;

    h4 {
      margin: 0 0 6px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 12px;
    }
  }
}

.route-stop-list {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.route-stop {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #ebeef5;
  padding: 8px;
  border-radius: 8px;

  .stop-index {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #409eff;
    color: #fff;
    text-align: center;
    line-height: 24px;
    font-weight: 600;
  }

  .stop-info {
    flex: 1;

    strong {
      display: block;
      font-size: 14px;
    }

    small {
      color: #a0a3ad;
    }
  }
}

.route-summary {
  margin-bottom: 16px;
  line-height: 1.6;
  color: #606266;
}

.route-day-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;

  &__header {
    display: flex;
    justify-content: space-between;
    gap: 12px;

    strong {
      display: block;
      margin-bottom: 4px;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 13px;
    }
  }

  ul {
    list-style: none;
    margin: 8px 0;
    padding: 0;

    li {
      display: flex;
      gap: 6px;
      font-size: 13px;
      color: #606266;
      line-height: 1.4;
    }
  }
}

.route-stop-marker {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35);
}

.map-info-window {
  min-width: 160px;

  strong {
    display: block;
    margin-bottom: 4px;
  }

  p {
    margin: 0;
    font-size: 12px;
    color: #909399;
  }
}
</style>
