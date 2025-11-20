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
          >
            <template #label>
              <span class="theme-tab-label">
                <el-icon><component :is="theme.icon" /></el-icon>
                {{ theme.label }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="我的路线" name="my">
            <template #label>
              <span class="theme-tab-label">
                <el-icon><User /></el-icon>
                我的路线
              </span>
            </template>
          </el-tab-pane>
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

      <el-dialog v-model="showGenerateDialog" title="生成自定义路线" width="900px" :close-on-click-modal="false">
        <el-form :model="generateForm" :rules="formRules" ref="generateFormRef" label-width="140px">
          <el-divider content-position="left">目的地与时间</el-divider>
          <el-form-item label="目的地" prop="destinations">
            <el-input
              v-model="generateForm.destinations"
              placeholder="请输入目的地（城市/地区），多地请按顺序列出，如：乌鲁木齐,喀什,伊犁 或 乌鲁木齐→喀什→伊犁"
            />
            <div class="form-tip">支持多个城市，用逗号或→分隔，按顺序排列</div>
          </el-form-item>
          <el-form-item label="旅行日期" prop="travelDates">
            <el-input
              v-model="generateForm.travelDates"
              placeholder="请输入精确日期或大致时间，如：2025-06-10 至 2025-06-16 或 2025年6月"
            />
          </el-form-item>
          <el-form-item label="行程天数" prop="duration">
            <el-input-number v-model="generateForm.duration" :min="1" :max="30" placeholder="请输入行程天数" style="width: 100%" />
          </el-form-item>
          <el-form-item label="到达时段">
            <el-select v-model="generateForm.arrivalTime" placeholder="请选择到达时段" style="width: 100%">
              <el-option label="上午（6:00-12:00）" value="上午" />
              <el-option label="下午（12:00-18:00）" value="下午" />
              <el-option label="晚上（18:00-24:00）" value="晚上" />
              <el-option label="凌晨（0:00-6:00）" value="凌晨" />
            </el-select>
          </el-form-item>
          <el-form-item label="离开时段">
            <el-select v-model="generateForm.departureTime" placeholder="请选择离开时段" style="width: 100%">
              <el-option label="上午（6:00-12:00）" value="上午" />
              <el-option label="下午（12:00-18:00）" value="下午" />
              <el-option label="晚上（18:00-24:00）" value="晚上" />
              <el-option label="凌晨（0:00-6:00）" value="凌晨" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">人员信息</el-divider>
          <el-form-item label="人数" prop="peopleCount">
            <el-input-number v-model="generateForm.peopleCount" :min="1" :max="100" placeholder="请输入人数" style="width: 100%" />
          </el-form-item>
          <el-form-item label="年龄段">
            <el-input
              v-model="generateForm.ageGroups"
              placeholder="如：2个成人,1个儿童(5岁),1个老人(70岁)"
            />
            <div class="form-tip">请详细说明人员构成，包括儿童和老人的年龄</div>
          </el-form-item>
          <el-form-item label="行动不便者">
            <el-switch v-model="generateForm.hasMobilityIssues" />
            <span style="margin-left: 10px; color: #909399; font-size: 12px">如有行动不便者，路线将考虑无障碍设施</span>
          </el-form-item>
          <el-form-item label="特殊饮食">
            <el-select v-model="generateForm.specialDietary" placeholder="请选择特殊饮食需求" style="width: 100%">
              <el-option label="无特殊需求" value="" />
              <el-option label="素食" value="素食" />
              <el-option label="清真" value="清真" />
              <el-option label="无麸质" value="无麸质" />
              <el-option label="低盐" value="低盐" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">风格与节奏</el-divider>
          <el-form-item label="风格偏好">
            <el-select v-model="generateForm.stylePreferences" multiple placeholder="请选择风格偏好" style="width: 100%">
              <el-option label="休闲" value="休闲" />
              <el-option label="紧凑" value="紧凑" />
              <el-option label="深度文化" value="深度文化" />
              <el-option label="美食" value="美食" />
              <el-option label="摄影" value="摄影" />
              <el-option label="徒步" value="徒步" />
              <el-option label="购物" value="购物" />
              <el-option label="亲子" value="亲子" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">预算信息</el-divider>
          <el-form-item label="总预算">
            <el-input-number
              v-model="generateForm.totalBudget"
              :min="0"
              :precision="0"
              placeholder="请输入总预算（元）"
              style="width: 100%"
            >
              <template #append>元</template>
            </el-input-number>
          </el-form-item>
          <el-form-item label="每日预算">
            <el-input-number
              v-model="generateForm.dailyBudget"
              :min="0"
              :precision="0"
              placeholder="请输入每日预算（元）"
              style="width: 100%"
            >
              <template #append>元/天</template>
            </el-input-number>
            <div class="form-tip">如填写总预算，可不填每日预算</div>
          </el-form-item>
          <el-form-item label="是否含机票">
            <el-switch v-model="generateForm.includesFlight" />
          </el-form-item>

          <el-divider content-position="left">住宿偏好</el-divider>
          <el-form-item label="住宿类型">
            <el-select v-model="generateForm.accommodationPreferences" multiple placeholder="请选择住宿偏好" style="width: 100%">
              <el-option label="经济型酒店" value="经济型酒店" />
              <el-option label="舒适型酒店" value="舒适型酒店" />
              <el-option label="豪华酒店" value="豪华酒店" />
              <el-option label="民宿" value="民宿" />
              <el-option label="Airbnb" value="Airbnb" />
              <el-option label="靠近地铁" value="靠近地铁" />
              <el-option label="市中心" value="市中心" />
              <el-option label="景区附近" value="景区附近" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">交通偏好</el-divider>
          <el-form-item label="交通方式">
            <el-select v-model="generateForm.transportationPreferences" multiple placeholder="请选择交通偏好" style="width: 100%">
              <el-option label="自驾" value="自驾" />
              <el-option label="公共交通" value="公共交通" />
              <el-option label="包车" value="包车" />
              <el-option label="不想坐夜车" value="不想坐夜车" />
              <el-option label="不租车" value="不租车" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">景点偏好</el-divider>
          <el-form-item label="必看景点/体验">
            <el-input
              v-model="generateForm.mustVisitText"
              type="textarea"
              :rows="2"
              placeholder="请输入必看的景点或体验，如：想看樱花、体验民族风情、观看日出等"
            />
          </el-form-item>
          <el-form-item label="必须避开">
            <el-input
              v-model="generateForm.mustAvoidText"
              type="textarea"
              :rows="2"
              placeholder="请输入必须避开的景点或体验，如：不想去太商业化的夜市、避开人多的景点等"
            />
          </el-form-item>

          <el-divider content-position="left">其他要求</el-divider>
          <el-form-item label="天气/季节敏感">
            <el-input
              v-model="generateForm.weatherSensitivity"
              type="textarea"
              :rows="2"
              placeholder="如：不想在雨季徒步、避开高温天气、希望看到雪景等"
            />
          </el-form-item>

          <el-divider content-position="left">额外服务</el-divider>
          <el-form-item label="需要服务">
            <el-checkbox-group v-model="generateForm.extraServices">
              <el-checkbox value="餐厅预订建议">餐厅预订建议</el-checkbox>
              <el-checkbox value="门票预订建议">门票预订建议</el-checkbox>
              <el-checkbox value="交通预订建议">交通预订建议</el-checkbox>
              <el-checkbox value="打包清单">打包清单</el-checkbox>
              <el-checkbox value="安全提示">安全提示</el-checkbox>
              <el-checkbox value="签证/入境提醒">签证/入境提醒</el-checkbox>
            </el-checkbox-group>
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
import { Plus, Clock, MapLocation, Location, View, Star, User } from '@element-plus/icons-vue'
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
  // 目的地与时间
  destinations: '',
  travelDates: '',
  duration: 3,
  arrivalTime: '',
  departureTime: '',

  // 人员信息
  peopleCount: 1,
  ageGroups: '',
  hasMobilityIssues: false,
  specialDietary: '',

  // 风格与节奏
  stylePreferences: [] as string[],

  // 预算信息
  totalBudget: undefined as number | undefined,
  dailyBudget: undefined as number | undefined,
  includesFlight: false,

  // 住宿偏好
  accommodationPreferences: [] as string[],

  // 交通偏好
  transportationPreferences: [] as string[],

  // 景点偏好
  mustVisitText: '',
  mustAvoidText: '',

  // 其他要求
  weatherSensitivity: '',

  // 额外服务
  extraServices: [] as string[],

  // 兼容旧字段（可选）
  startLocation: '',
  endLocation: '',
  interests: [] as string[],
  budget: 0,
  mustVisitLocations: '',
})

// 计算当前是否为"我的路线"标签
const isMyRoutes = computed(() => activeTab.value === 'my')

const formRules: FormRules = {
  destinations: [
    { required: true, message: '请输入目的地', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入行程天数', trigger: 'blur' },
    { type: 'number', min: 1, max: 30, message: '天数范围在1-30天', trigger: 'blur' }
  ],
  peopleCount: [
    { required: true, message: '请输入人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '人数至少为1人', trigger: 'blur' }
  ],
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
      // 处理必看和必须避开的景点：将字符串转换为数组
      const mustVisitList = generateForm.value.mustVisitText
        .split(/[，,、\n]/)
        .map(item => item.trim())
        .filter(item => item.length > 0)

      const mustAvoidList = generateForm.value.mustAvoidText
        .split(/[，,、\n]/)
        .map(item => item.trim())
        .filter(item => item.length > 0)

      // 处理额外服务
      const extraServices = generateForm.value.extraServices || []
      const needRestaurantSuggestions = extraServices.includes('餐厅预订建议')
      const needTicketSuggestions = extraServices.includes('门票预订建议')
      const needTransportSuggestions = extraServices.includes('交通预订建议')
      const needPackingList = extraServices.includes('打包清单')
      const needSafetyTips = extraServices.includes('安全提示')
      const needVisaInfo = extraServices.includes('签证/入境提醒')

      // 构建请求数据
      const requestData: any = {
        // 新字段
        destinations: generateForm.value.destinations,
        travelDates: generateForm.value.travelDates,
        duration: generateForm.value.duration,
        arrivalTime: generateForm.value.arrivalTime,
        departureTime: generateForm.value.departureTime,
        peopleCount: generateForm.value.peopleCount,
        ageGroups: generateForm.value.ageGroups,
        hasMobilityIssues: generateForm.value.hasMobilityIssues || false,
        specialDietary: generateForm.value.specialDietary,
        stylePreferences: generateForm.value.stylePreferences,
        totalBudget: generateForm.value.totalBudget,
        dailyBudget: generateForm.value.dailyBudget,
        includesFlight: generateForm.value.includesFlight || false,
        accommodationPreferences: generateForm.value.accommodationPreferences,
        transportationPreferences: generateForm.value.transportationPreferences,
        mustVisit: mustVisitList,
        mustAvoid: mustAvoidList,
        weatherSensitivity: generateForm.value.weatherSensitivity,
        needRestaurantSuggestions,
        needTicketSuggestions,
        needTransportSuggestions,
        needPackingList,
        needSafetyTips,
        needVisaInfo,
      }

      // 兼容旧字段（如果新字段为空，使用旧字段）
      if (!requestData.destinations && generateForm.value.startLocation && generateForm.value.endLocation) {
        requestData.destinations = `${generateForm.value.startLocation},${generateForm.value.endLocation}`
      }
      if (!requestData.totalBudget && !requestData.dailyBudget && generateForm.value.budget) {
        requestData.totalBudget = generateForm.value.budget
      }
      if (generateForm.value.interests && generateForm.value.interests.length > 0) {
        requestData.stylePreferences = [
          ...(requestData.stylePreferences || []),
          ...generateForm.value.interests
        ]
      }

      const route = await generateRoute(requestData)

      ElMessage.success('路线生成成功！')
      showGenerateDialog.value = false

      // 重置表单
      generateForm.value = {
        destinations: '',
        travelDates: '',
        duration: 3,
        arrivalTime: '',
        departureTime: '',
        peopleCount: 1,
        ageGroups: '',
        hasMobilityIssues: false,
        specialDietary: '',
        stylePreferences: [],
        totalBudget: undefined,
        dailyBudget: undefined,
        includesFlight: false,
        accommodationPreferences: [],
        transportationPreferences: [],
        mustVisitText: '',
        mustAvoidText: '',
        weatherSensitivity: '',
        extraServices: [],
        startLocation: '',
        endLocation: '',
        interests: [],
        budget: 0,
        mustVisitLocations: '',
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

.theme-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: #303133;

  .el-icon {
    font-size: 16px;
  }
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
