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
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { Search } from '@element-plus/icons-vue'

const mapContainer = ref<HTMLElement>()
const searchPlace = ref('')
const selectedLayers = ref(['markers', 'routes'])

const handleSearch = () => {
  // 地图搜索功能
  console.log('Search:', searchPlace.value)
}

onMounted(() => {
  // 初始化地图
  // 这里需要配置Mapbox或高德地图
  if (mapContainer.value) {
    // mapboxgl.accessToken = 'YOUR_TOKEN'
    // const map = new mapboxgl.Map({
    //   container: mapContainer.value,
    //   style: 'mapbox://styles/mapbox/streets-v11',
    //   center: [87.68, 43.77], // 乌鲁木齐坐标
    //   zoom: 10
    // })
  }
})

onUnmounted(() => {
  // 清理地图实例
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
