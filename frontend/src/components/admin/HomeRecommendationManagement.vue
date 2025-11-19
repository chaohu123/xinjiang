<template>
  <div class="home-recommendation-management">
    <!-- 标题和标签页 -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>首页推荐配置</span>
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="精选推荐" name="FEATURED" />
            <el-tab-pane label="热门资源" name="HOT" />
          </el-tabs>
        </div>
      </template>

      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加推荐
        </el-button>
        <el-button @click="loadCurrentResources">
          <el-icon><Refresh /></el-icon>
          刷新当前首页资源
        </el-button>
      </div>
    </el-card>

      <!-- 当前首页显示的资源 -->
      <el-card style="margin-bottom: 20px">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>当前首页显示的资源</span>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-tag type="info">共 {{ currentResources.length }} 个</el-tag>
              <el-tag v-if="currentResources.length >= 6" type="warning">
                已达上限（最多6个）
              </el-tag>
            </div>
          </div>
        </template>
        <el-table v-loading="currentResourcesLoading" :data="currentResources" style="width: 100%">
          <el-table-column label="资源信息" min-width="300">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 12px">
                <el-image
                  v-if="row.resource.cover"
                  :src="row.resource.cover"
                  fit="cover"
                  style="width: 80px; height: 60px; border-radius: 4px"
                />
                <div style="flex: 1">
                  <div style="font-weight: 600; margin-bottom: 4px">
                    {{ row.resource.title }}
                  </div>
                  <div style="font-size: 12px; color: #909399">
                    {{ row.resource.description?.substring(0, 50) }}...
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">
              <el-tag :type="getSourceTagType(row.resource.source)">
                {{ getSourceLabel(row.resource.source) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="显示顺序" width="150">
            <template #default="{ row }">
              <el-input-number
                v-if="row.configured"
                v-model="row.displayOrder"
                :min="0"
                size="small"
                style="width: 100px"
                @change="handleCurrentResourceOrderChange(row)"
              />
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="是否展示" width="120">
            <template #default="{ row }">
              <el-switch
                v-if="row.configured"
                v-model="row.enabled"
                @change="handleCurrentResourceStatusChange(row)"
              />
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="!row.configured"
                type="primary"
                size="small"
                :disabled="currentResources.length >= 6"
                @click="handleAddFromCurrent(row)"
              >
                添加到配置
              </el-button>
              <el-button
                v-else
                link
                type="danger"
                size="small"
                @click="handleDeleteFromCurrent(row)"
              >
                删除配置
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

    <!-- 添加推荐对话框 -->
    <el-dialog v-model="dialogVisible" title="添加推荐" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="推荐类型" required>
          <el-select v-model="form.type" style="width: 100%" disabled>
            <el-option label="精选推荐" value="FEATURED" />
            <el-option label="热门资源" value="HOT" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源来源" required>
          <el-radio-group v-model="form.source" @change="handleSourceChange">
            <el-radio value="CULTURE_RESOURCE">文化资源</el-radio>
            <el-radio value="COMMUNITY_POST">社区投稿</el-radio>
            <el-radio value="HERITAGE_ITEM">文化遗产</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择资源" required>
          <el-select
            v-model="form.resourceId"
            filterable
            :loading="searchLoading"
            placeholder="请选择资源"
            style="width: 100%"
          >
            <el-option
              v-for="item in resourceOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number v-model="form.displayOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false"> 取消 </el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  addHomeRecommendation,
  updateHomeRecommendation,
  deleteHomeRecommendation,
  getCurrentDisplayedResources,
  getAdminCultureResources,
  getAdminPosts,
  getAdminHeritageItems,
  type HomeResourceWithConfigInfo,
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

const activeTab = ref<'FEATURED' | 'HOT'>('FEATURED')
const currentResources = ref<HomeResourceWithConfigInfo[]>([])
const currentResourcesLoading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const searchLoading = ref(false)
const resourceOptions = ref<Array<{ id: number; title: string }>>([])

const form = ref({
  type: 'FEATURED' as 'FEATURED' | 'HOT',
  source: 'CULTURE_RESOURCE' as 'CULTURE_RESOURCE' | 'COMMUNITY_POST' | 'HERITAGE_ITEM',
  resourceId: undefined as number | undefined,
  displayOrder: 0,
})

const sourceLabelMap: Record<string, string> = {
  CULTURE_RESOURCE: '文化资源',
  COMMUNITY_POST: '社区投稿',
  HERITAGE_ITEM: '文化遗产',
}

const sourceTagTypeMap: Record<string, 'success' | 'info' | 'warning'> = {
  CULTURE_RESOURCE: 'success',
  COMMUNITY_POST: 'info',
  HERITAGE_ITEM: 'warning',
}

const getSourceLabel = (source: string) => sourceLabelMap[source] || '其他'
const getSourceTagType = (source: string) => sourceTagTypeMap[source] || 'info'

const handleTabChange = () => {
  form.value.type = activeTab.value
  loadCurrentResources()
}

const handleAdd = async () => {
  // 检查是否已达到最大配置数量
  if (currentResources.value.filter(r => r.configured).length >= 6) {
    ElMessage.warning('该推荐类型最多只能配置6个资源')
    return
  }

  form.value = {
    type: activeTab.value,
    source: 'CULTURE_RESOURCE',
    resourceId: undefined,
    displayOrder: currentResources.value.filter(r => r.configured).length,
  }
  resourceOptions.value = []
  dialogVisible.value = true

  // 自动加载默认资源列表（文化资源）
  await handleSourceChange()
}

const handleSourceChange = async () => {
  form.value.resourceId = undefined
  resourceOptions.value = []

  // 自动加载对应类型的资源列表
  searchLoading.value = true
  try {
    if (form.value.source === 'CULTURE_RESOURCE') {
      const response = await getAdminCultureResources({
        page: 1,
        size: 100, // 加载更多资源供选择
      })
      resourceOptions.value =
        response.list?.map((r) => ({ id: r.id, title: r.title })) || []
    } else if (form.value.source === 'COMMUNITY_POST') {
      const response = await getAdminPosts({
        page: 1,
        size: 100, // 加载更多资源供选择
        status: 'approved',
      })
      resourceOptions.value =
        response.list?.map((r) => ({ id: r.id, title: r.title })) || []
    } else if (form.value.source === 'HERITAGE_ITEM') {
      const response = await getAdminHeritageItems({
        page: 1,
        size: 100,
      })
      resourceOptions.value =
        response.list?.map((r) => ({ id: r.id, title: r.title })) || []
    } else {
      resourceOptions.value = []
    }
  } catch (error) {
    console.error('Failed to load resources:', error)
    ElMessage.error('加载资源列表失败')
  } finally {
    searchLoading.value = false
  }
}


const handleSubmit = async () => {
  if (!form.value.resourceId) {
    ElMessage.warning('请选择资源')
    return
  }

  // 检查是否已达到最大配置数量
  if (currentResources.value.filter(r => r.configured).length >= 6) {
    ElMessage.warning('该推荐类型最多只能配置6个资源')
    return
  }

  submitting.value = true
  try {
    await addHomeRecommendation({
      type: form.value.type,
      source: form.value.source,
      resourceId: form.value.resourceId,
      displayOrder: form.value.displayOrder,
    })
    ElMessage.success('添加成功')
    dialogVisible.value = false
    loadCurrentResources()
  } catch (error: any) {
    console.error('Failed to add recommendation:', error)
    ElMessage.error('添加失败：' + (error?.response?.data?.message || error?.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}


const loadCurrentResources = async () => {
  currentResourcesLoading.value = true
  try {
    const response = await getCurrentDisplayedResources(activeTab.value, 20)
    currentResources.value = response || []
  } catch (error: any) {
    console.error('Failed to load current resources:', error)
    ElMessage.error('加载当前资源失败：' + (error?.message || '未知错误'))
    currentResources.value = []
  } finally {
    currentResourcesLoading.value = false
  }
}

const handleAddFromCurrent = async (row: HomeResourceWithConfigInfo) => {
  // 检查是否已达到最大配置数量
  if (currentResources.value.filter(r => r.configured).length >= 6) {
    ElMessage.warning('该推荐类型最多只能配置6个资源')
    return
  }

  try {
    await addHomeRecommendation({
      type: activeTab.value,
      source: row.resource.source,
      resourceId: row.resource.id,
      displayOrder: currentResources.value.filter(r => r.configured).length,
    })
    ElMessage.success('添加成功')
    loadCurrentResources()
  } catch (error: any) {
    console.error('Failed to add recommendation:', error)
    ElMessage.error('添加失败：' + (error?.response?.data?.message || error?.message || '未知错误'))
  }
}

const handleCurrentResourceOrderChange = async (row: HomeResourceWithConfigInfo) => {
  if (!row.recommendationId) return

  try {
    await updateHomeRecommendation(row.recommendationId, { displayOrder: row.displayOrder })
    ElMessage.success('排序更新成功')
    loadCurrentResources()
  } catch (error: any) {
    console.error('Failed to update order:', error)
    ElMessage.error('更新失败：' + (error?.response?.data?.message || error?.message || '未知错误'))
    loadCurrentResources()
  }
}

const handleCurrentResourceStatusChange = async (row: HomeResourceWithConfigInfo) => {
  if (!row.recommendationId) return

  try {
    await updateHomeRecommendation(row.recommendationId, { enabled: row.enabled })
    ElMessage.success('状态更新成功')
    loadCurrentResources()
  } catch (error: any) {
    console.error('Failed to update status:', error)
    ElMessage.error('更新失败：' + (error?.response?.data?.message || error?.message || '未知错误'))
    loadCurrentResources()
  }
}

const handleDeleteFromCurrent = async (row: HomeResourceWithConfigInfo) => {
  if (!row.recommendationId) return

  try {
    await ElMessageBox.confirm(`确定要删除该推荐配置吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteHomeRecommendation(row.recommendationId)
    ElMessage.success('删除成功')
    loadCurrentResources()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete recommendation:', error)
      ElMessage.error('删除失败：' + (error?.response?.data?.message || error?.message || '未知错误'))
    }
  }
}


onMounted(() => {
  loadCurrentResources()
})
</script>

<style lang="scss" scoped>
.home-recommendation-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .toolbar {
    margin-bottom: 16px;
  }
}
</style>

