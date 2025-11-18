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
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane
            v-for="theme in ROUTE_THEMES"
            :key="theme.value"
            :label="theme.label"
            :name="theme.value"
          />
          <el-tab-pane label="我的路线" name="my" />
        </el-tabs>
      </div>

      <div v-loading="loading" class="routes-grid">
        <el-card
          v-for="route in routes"
          :key="route.id"
          class="route-card card-shadow"
          @click="$router.push(`/route/${route.id}`)"
        >
          <el-image :src="route.cover || defaultRouteCover" fit="cover" class="route-cover" />
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

      <el-dialog v-model="showGenerateDialog" title="生成自定义路线" width="700px">
        <el-form :model="generateForm" :rules="formRules" ref="generateFormRef" label-width="120px">
          <el-form-item label="起点" prop="startLocation">
            <el-input v-model="generateForm.startLocation" placeholder="请输入起点，如：乌鲁木齐" />
          </el-form-item>
          <el-form-item label="终点" prop="endLocation">
            <el-input v-model="generateForm.endLocation" placeholder="请输入终点，如：喀什" />
          </el-form-item>
          <el-form-item label="兴趣标签" prop="interests">
            <el-select v-model="generateForm.interests" multiple placeholder="请选择感兴趣的标签" style="width: 100%">
              <el-option label="历史" value="history" />
              <el-option label="自然" value="nature" />
              <el-option label="文化" value="culture" />
              <el-option label="美食" value="food" />
              <el-option label="宗教" value="religion" />
              <el-option label="建筑" value="architecture" />
              <el-option label="艺术" value="art" />
            </el-select>
          </el-form-item>
          <el-form-item label="人数" prop="peopleCount">
            <el-input-number v-model="generateForm.peopleCount" :min="1" :max="100" placeholder="请输入人数" style="width: 100%" />
          </el-form-item>
          <el-form-item label="旅游预算" prop="budget">
            <el-input-number
              v-model="generateForm.budget"
              :min="0"
              :precision="0"
              placeholder="请输入预算（元）"
              style="width: 100%"
            >
              <template #append>元</template>
            </el-input-number>
          </el-form-item>
          <el-form-item label="想要打卡的地点" prop="mustVisitLocations">
            <el-input
              v-model="generateForm.mustVisitLocations"
              type="textarea"
              :rows="3"
              placeholder="请输入想要打卡的地点，多个地点用逗号分隔，如：天山天池,喀纳斯湖,火焰山"
            />
          </el-form-item>
          <el-form-item label="预计天数" prop="duration">
            <el-input-number v-model="generateForm.duration" :min="1" :max="30" placeholder="请输入预计天数" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showGenerateDialog = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button type="primary" :loading="generating" @click="handleGenerate">
            {{ generating ? '生成中...' : '生成路线' }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getRoutes, generateRoute, getMyRoutes } from '@/api/route'
import type { Route } from '@/types/route'
import EmptyState from '@/components/common/EmptyState.vue'
import { ROUTE_THEMES } from '@/utils/constants'
import { Plus, Clock, MapLocation, Location, View, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { requireAuth } from '@/utils/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const routes = ref<Route[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const activeTab = ref('silkRoad')
const showGenerateDialog = ref(false)
const generating = ref(false)
const generateFormRef = ref<FormInstance>()
const defaultRouteCover = '/api/digital-images/route.jpg'
const generateForm = ref({
  startLocation: '',
  endLocation: '',
  interests: [] as string[],
  peopleCount: 1,
  budget: 0,
  mustVisitLocations: '',
  duration: 3,
})

// 计算当前是否为"我的路线"标签
const isMyRoutes = computed(() => activeTab.value === 'my')

const formRules: FormRules = {
  startLocation: [
    { required: true, message: '请输入起点', trigger: 'blur' }
  ],
  endLocation: [
    { required: true, message: '请输入终点', trigger: 'blur' }
  ],
  interests: [
    { type: 'array', min: 1, message: '请至少选择一个兴趣标签', trigger: 'change' }
  ],
  peopleCount: [
    { required: true, message: '请输入人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '人数至少为1人', trigger: 'blur' }
  ],
  budget: [
    { required: true, message: '请输入预算', trigger: 'blur' },
    { type: 'number', min: 0, message: '预算不能为负数', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入预计天数', trigger: 'blur' },
    { type: 'number', min: 1, max: 30, message: '天数范围在1-30天', trigger: 'blur' }
  ]
}

const loadRoutes = async () => {
  loading.value = true
  try {
    if (isMyRoutes.value) {
      // 加载我的路线
      if (!userStore.isLoggedIn) {
        routes.value = []
        total.value = 0
        return
      }
      const response = await getMyRoutes({
        page: currentPage.value,
        size: pageSize.value,
      })
      routes.value = Array.isArray(response?.list) ? response.list : []
      total.value = response?.total || 0
    } else {
      // 加载推荐路线
      const response = await getRoutes({
        theme: activeTab.value,
        page: currentPage.value,
        size: pageSize.value,
      })
      routes.value = Array.isArray(response?.list) ? response.list : []
      total.value = response?.total || 0
    }
  } catch (error) {
    console.error('Failed to load routes:', error)
    routes.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadRoutes()
}

const handlePageChange = () => {
  loadRoutes()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleGenerate = async () => {
  // 检查登录状态
  if (!requireAuth('请先登录后再生成路线', router)) {
    showGenerateDialog.value = false
    return
  }

  if (!generateFormRef.value) return

  await generateFormRef.value.validate(async (valid) => {
    if (!valid) return

    generating.value = true
    try {
      // 处理打卡地点：将字符串转换为数组
      const mustVisitLocationsList = generateForm.value.mustVisitLocations
        .split(',')
        .map(loc => loc.trim())
        .filter(loc => loc.length > 0)

      const requestData = {
        ...generateForm.value,
        mustVisitLocations: mustVisitLocationsList
      }

      const route = await generateRoute(requestData)
      ElMessage.success('路线生成成功！')
      showGenerateDialog.value = false
      // 重置表单
      generateForm.value = {
        startLocation: '',
        endLocation: '',
        interests: [],
        peopleCount: 1,
        budget: 0,
        mustVisitLocations: '',
        duration: 3,
      }

      // 如果当前在"我的路线"标签页，刷新列表
      if (isMyRoutes.value) {
        await loadRoutes()
      }

      // 跳转到路线详情
      if (route?.id) {
        router.push(`/route/${route.id}`)
      }
    } catch (error: any) {
      console.error('生成路线失败:', error)
      ElMessage.error(error?.response?.data?.message || '生成路线失败，请稍后重试')
    } finally {
      generating.value = false
    }
  })
}

// 监听登录状态变化，如果未登录且在"我的路线"标签页，切换到推荐路线
watch(() => userStore.isLoggedIn, (isLoggedIn) => {
  if (!isLoggedIn && isMyRoutes.value) {
    activeTab.value = 'silkRoad'
  }
})

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
