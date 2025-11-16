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
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { searchResources } from '@/api/culture'
import type { CultureResource } from '@/types/culture'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { loadAMapScript } from '@/utils/amap'

const router = useRouter()
const mapContainer = ref<HTMLElement>()
const searchPlace = ref('')
const selectedLayers = ref(['markers', 'routes'])
const map = ref<AMap.Map | null>(null)
const markers = ref<AMap.Marker[]>([])
const routesLayer = ref<string | null>(null)
const resources = ref<CultureResource[]>([])
const loading = ref(false)
const mapLoaded = ref(false)

// 初始化地图
const initMap = async () => {
  if (!mapContainer.value) return

  try {
    // 加载高德地图脚本
    loading.value = true
    await loadAMapScript()

    if (!window.AMap) {
      throw new Error('高德地图加载失败')
    }

    // 创建地图实例
    map.value = new window.AMap.Map(mapContainer.value, {
      center: [87.68, 43.77], // 乌鲁木齐坐标 [经度, 纬度]
      zoom: 10,
      viewMode: '3D', // 3D视图
      mapStyle: 'amap://styles/normal', // 地图样式
    })

    mapLoaded.value = true

    // 地图加载完成事件
    map.value.on('complete', () => {
      loadResources()
    })

    // 直接加载资源（有些情况下 complete 事件可能不触发）
    setTimeout(() => {
      if (mapLoaded.value && resources.value.length === 0) {
        loadResources()
      }
    }, 500)
  } catch (error: any) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败：' + (error?.message || '请配置 VITE_AMAP_KEY 环境变量'))
  } finally {
    loading.value = false
  }
}

// 加载文化资源数据
const loadResources = async () => {
  loading.value = true
  try {
    const response = await searchResources({
      page: 1,
      size: 100, // 获取更多资源以显示在地图上
    })

    console.log('获取到的资源数据:', response)
    console.log('资源列表:', response.list)

    // 过滤出有位置信息的资源
    const allResources = (response.list || []).map((resource: any) => ({
      ...resource,
      // 将后端返回的大写类型转换为小写
      type: resource.type?.toLowerCase() as CultureType,
    }))
    console.log('总资源数:', allResources.length)
    console.log('资源示例（第一个）:', allResources[0])

    resources.value = allResources.filter((resource: CultureResource) => {
      const hasLocation = resource.location &&
                         resource.location.lat != null &&
                         resource.location.lat !== undefined &&
                         resource.location.lng != null &&
                         resource.location.lng !== undefined &&
                         !isNaN(resource.location.lat) &&
                         !isNaN(resource.location.lng)

      if (!hasLocation) {
        console.log('资源缺少位置信息:', {
          title: resource.title,
          location: resource.location,
          hasLocation: !!resource.location,
          hasLat: resource.location?.lat != null,
          hasLng: resource.location?.lng != null,
        })
      }

      return hasLocation
    })

    console.log('有位置信息的资源数:', resources.value.length)
    console.log('筛选后的资源:', resources.value)

    if (resources.value.length === 0 && allResources.length > 0) {
      ElMessage.warning('当前没有包含位置信息的文化资源。请在后台为资源添加位置信息。')
    } else if (resources.value.length === 0) {
      ElMessage.info('暂无文化资源数据')
    }

    updateMarkers()
  } catch (error: any) {
    console.error('加载文化资源失败:', error)
    ElMessage.error('加载文化资源失败：' + (error?.message || '未知错误'))
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

  // 如果标记点图层未选中，则不显示
  if (!selectedLayers.value.includes('markers')) {
    return
  }

  // 添加新标记
  resources.value.forEach((resource) => {
    if (!resource.location || !resource.location.lat || !resource.location.lng) {
      return
    }

    // 创建标记内容（使用自定义 HTML）
    const markerContent = document.createElement('div')
    markerContent.className = 'map-marker'
    markerContent.style.width = '32px'
    markerContent.style.height = '32px'
    markerContent.style.borderRadius = '50%'
    markerContent.style.backgroundColor = '#409EFF'
    markerContent.style.border = '2px solid #fff'
    markerContent.style.cursor = 'pointer'
    markerContent.style.boxShadow = '0 2px 4px rgba(0,0,0,0.3)'
    markerContent.style.position = 'relative'
    markerContent.title = resource.title

    // 创建高德地图标记
    const marker = new window.AMap.Marker({
      position: [resource.location!.lng, resource.location!.lat], // [经度, 纬度]
      content: markerContent,
      title: resource.title,
      offset: new window.AMap.Pixel(-16, -16) as any, // 调整标记位置
    })

    // 添加点击事件
    marker.on('click', () => {
      router.push(`/detail/${resource.type}/${resource.id}`)
    })

    // 添加到地图
    map.value!.add(marker)
    markers.value.push(marker)
  })

  // 如果有标记点，调整地图视野以包含所有标记
  if (markers.value.length > 0) {
    const bounds = new window.AMap.Bounds()
    resources.value.forEach((resource) => {
      if (resource.location?.lng && resource.location?.lat) {
        bounds.extend([resource.location.lng, resource.location.lat])
      }
    })
    if (bounds.getNorthEast() && bounds.getSouthWest()) {
      map.value!.setFitView(markers.value, false, [50, 50, 50, 50])
    }
  }
}

// 处理搜索
const handleSearch = () => {
  if (!searchPlace.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  searchResources({
    keyword: searchPlace.value,
    page: 1,
    size: 100,
  })
    .then((response) => {
      console.log('搜索结果:', response)

      const allResources = (response.list || []).map((resource: any) => ({
        ...resource,
        // 将后端返回的大写类型转换为小写
        type: resource.type?.toLowerCase() as CultureType,
      }))

      resources.value = allResources.filter((resource: CultureResource) => {
        const hasLocation = resource.location &&
                           resource.location.lat != null &&
                           resource.location.lat !== undefined &&
                           resource.location.lng != null &&
                           resource.location.lng !== undefined &&
                           !isNaN(resource.location.lat) &&
                           !isNaN(resource.location.lng)

        if (!hasLocation) {
          console.log('搜索结果中资源缺少位置信息:', {
            title: resource.title,
            location: resource.location,
            hasLocation: !!resource.location,
            hasLat: resource.location?.lat != null,
            hasLng: resource.location?.lng != null,
          })
        }

        return hasLocation
      })

      console.log('搜索结果中有位置信息的资源数:', resources.value.length)

      updateMarkers()

      if (resources.value.length > 0 && map.value) {
        // 如果搜索结果有位置，缩放到第一个结果
        const firstLocation = resources.value[0].location!
        map.value.setCenter([firstLocation.lng, firstLocation.lat])
        map.value.setZoom(12)
        ElMessage.success(`找到 ${resources.value.length} 个包含位置信息的资源`)
      } else if (allResources.length > 0) {
        ElMessage.warning(`找到 ${allResources.length} 个资源，但均不包含位置信息`)
      } else {
        ElMessage.info('未找到相关资源')
      }
    })
    .catch((error: any) => {
      console.error('搜索失败:', error)
      ElMessage.error('搜索失败：' + (error?.message || '未知错误'))
    })
    .finally(() => {
      loading.value = false
    })
}

// 监听图层变化
watch(selectedLayers, () => {
  updateMarkers()
  // TODO: 实现路线图层的显示/隐藏
})

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  // 清理地图实例和标记
  if (map.value) {
    markers.value.forEach((marker) => {
      map.value!.remove(marker)
    })
    markers.value = []
    map.value.destroy()
    map.value = null
  }
  mapLoaded.value = false
})
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
</style>
