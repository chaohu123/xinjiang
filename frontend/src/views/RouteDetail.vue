<template>
  <div class="route-detail-page">
    <div class="container">
      <div v-loading="loading" class="route-detail-content">
        <div v-if="routeDetail" class="route-header">
          <h1>{{ routeDetail.title }}</h1>
          <p>{{ routeDetail.description }}</p>
          <div class="route-info">
            <span>
              <el-icon><Clock /></el-icon>
              {{ routeDetail.duration }} 天
            </span>
            <span>
              <el-icon><MapLocation /></el-icon>
              {{ routeDetail.distance }}km
            </span>
            <span>
              <el-icon><Location /></el-icon>
              {{ routeDetail.startLocation }} → {{ routeDetail.endLocation }}
            </span>
          </div>
        </div>

        <div v-if="routeDetail" class="route-itinerary">
          <h2>行程安排</h2>
          <el-timeline>
            <el-timeline-item
              v-for="item in routeDetail.itinerary"
              :key="item.day"
              :timestamp="`第 ${item.day} 天`"
              placement="top"
            >
              <el-card>
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
                <div v-if="item.locations.length > 0" class="locations">
                  <el-tag
                    v-for="loc in item.locations"
                    :key="loc.name"
                    type="info"
                    style="margin-right: 8px"
                  >
                    {{ loc.name }}
                  </el-tag>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getRouteDetail } from '@/api/route'
import type { RouteDetail } from '@/types/route'
import { Clock, MapLocation, Location } from '@element-plus/icons-vue'

const route = useRoute()
const routeDetail = ref<RouteDetail | null>(null)
const loading = ref(false)

const loadDetail = async () => {
  const id = parseInt(route.params.id as string)
  loading.value = true
  try {
    const data = await getRouteDetail(id)
    routeDetail.value = data
  } catch (error) {
    console.error('Failed to load route detail:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.route-detail-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.route-detail-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.route-header {
  margin-bottom: 40px;
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
    margin-bottom: 20px;
  }
}

.route-info {
  display: flex;
  gap: 24px;
  color: #909399;
  font-size: 14px;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.route-itinerary {
  h2 {
    font-size: 24px;
    margin-bottom: 24px;
    color: #303133;
  }
}
</style>
