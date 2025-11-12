<template>
  <div class="routes-page">
    <div class="container">
      <div class="page-header">
        <h1>{{ $t('routes.title') }}</h1>
        <el-button type="primary" @click="showGenerateDialog = true">
          <el-icon><Plus /></el-icon>
          {{ $t('routes.generate') }}
        </el-button>
      </div>

      <div class="themes-section">
        <el-tabs v-model="activeTheme" @tab-change="handleThemeChange">
          <el-tab-pane
            v-for="theme in ROUTE_THEMES"
            :key="theme.value"
            :label="theme.label"
            :name="theme.value"
          />
        </el-tabs>
      </div>

      <div v-loading="loading" class="routes-grid">
        <el-card
          v-for="route in routes"
          :key="route.id"
          class="route-card card-shadow"
          @click="$router.push(`/route/${route.id}`)"
        >
          <el-image :src="route.cover" fit="cover" class="route-cover" />
          <div class="route-content">
            <h3>{{ route.title }}</h3>
            <p>{{ route.description }}</p>
            <div class="route-meta">
              <span>
                <el-icon><Clock /></el-icon>
                {{ route.duration }} {{ $t('routes.duration') }}
              </span>
              <span>
                <el-icon><MapLocation /></el-icon>
                {{ route.distance }}km
              </span>
              <span>
                <el-icon><Location /></el-icon>
                {{ route.waypoints }} {{ $t('common.more') }} 站点
              </span>
            </div>
            <div class="route-footer">
              <span>{{ route.startLocation }} → {{ route.endLocation }}</span>
              <div class="route-stats">
                <span>
                  <el-icon><View /></el-icon>
                  {{ route.views }}
                </span>
                <span>
                  <el-icon><Star /></el-icon>
                  {{ route.favorites }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <EmptyState v-if="!loading && routes.length === 0" :text="$t('common.noData')" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        class="pagination"
        @current-change="handlePageChange"
      />

      <el-dialog v-model="showGenerateDialog" title="生成自定义路线" width="600px">
        <el-form :model="generateForm" label-width="100px">
          <el-form-item label="起点">
            <el-input v-model="generateForm.startLocation" />
          </el-form-item>
          <el-form-item label="终点">
            <el-input v-model="generateForm.endLocation" />
          </el-form-item>
          <el-form-item label="兴趣标签">
            <el-select v-model="generateForm.interests" multiple>
              <el-option label="历史" value="history" />
              <el-option label="自然" value="nature" />
              <el-option label="文化" value="culture" />
              <el-option label="美食" value="food" />
            </el-select>
          </el-form-item>
          <el-form-item label="预计天数">
            <el-input-number v-model="generateForm.duration" :min="1" :max="30" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showGenerateDialog = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button type="primary" @click="handleGenerate">
            {{ $t('common.submit') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRoutes, generateRoute } from '@/api/route'
import type { Route } from '@/types/route'
import EmptyState from '@/components/common/EmptyState.vue'
import { ROUTE_THEMES } from '@/utils/constants'
import { Plus, Clock, MapLocation, Location, View, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const routes = ref<Route[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const activeTheme = ref('silkRoad')
const showGenerateDialog = ref(false)
const generateForm = ref({
  startLocation: '',
  endLocation: '',
  interests: [] as string[],
  duration: 3,
})

const loadRoutes = async () => {
  loading.value = true
  try {
    const response = await getRoutes({
      theme: activeTheme.value,
      page: currentPage.value,
      size: pageSize.value,
    })
    routes.value = Array.isArray(response?.list) ? response.list : []
    total.value = response?.total || 0
  } catch (error) {
    console.error('Failed to load routes:', error)
    routes.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleThemeChange = () => {
  currentPage.value = 1
  loadRoutes()
}

const handlePageChange = () => {
  loadRoutes()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleGenerate = async () => {
  try {
    const route = await generateRoute(generateForm.value)
    ElMessage.success('路线生成成功')
    showGenerateDialog.value = false
    // 跳转到路线详情
    // router.push(`/route/${route.id}`)
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

onMounted(() => {
  loadRoutes()
})
</script>

<style lang="scss" scoped>
.routes-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;

  h1 {
    font-size: 32px;
    color: #303133;
  }
}

.themes-section {
  margin-bottom: 30px;
}

.routes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.route-card {
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-4px);
  }
}

.route-cover {
  width: 100%;
  height: 200px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.route-content {
  h3 {
    font-size: 20px;
    margin-bottom: 12px;
    color: #303133;
  }

  p {
    color: #606266;
    line-height: 1.6;
    margin-bottom: 16px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.route-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #909399;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.route-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
  color: #909399;
}

.route-stats {
  display: flex;
  gap: 16px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
