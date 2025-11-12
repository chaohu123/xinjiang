<template>
  <div class="event-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动列表</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索活动标题"
              style="width: 300px; margin-right: 12px"
              clearable
              @keyup.enter="loadEvents"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterStatus"
              placeholder="状态"
              style="width: 150px; margin-right: 12px"
              clearable
            >
              <el-option label="即将开始" value="upcoming" />
              <el-option label="进行中" value="ongoing" />
              <el-option label="已结束" value="past" />
            </el-select>
            <el-button type="primary" @click="loadEvents"> 搜索 </el-button>
            <el-button type="primary" style="margin-left: 12px" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加活动
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="events" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="row.cover"
              fit="cover"
              style="width: 100px; height: 60px"
            />
            <span v-else>无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="日期" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column label="报名情况" width="150">
          <template #default="{ row }"> {{ row.registered }}/{{ row.capacity || '∞' }} </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button link type="info" @click="handleViewRegistrations(row)"> 报名列表 </el-button>
            <el-button link type="danger" @click="handleDelete(row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadEvents"
          @current-change="loadEvents"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingItem ? '编辑活动' : '添加活动'" width="800px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="展览" value="exhibition" />
            <el-option label="演出" value="performance" />
            <el-option label="工坊" value="workshop" />
            <el-option label="游览" value="tour" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.cover" placeholder="封面图片URL" />
        </el-form-item>
        <el-form-item label="开始日期" required>
          <el-date-picker
            v-model="form.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" required>
          <el-date-picker
            v-model="form.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="0" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="即将开始" value="upcoming" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已结束" value="past" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false"> 取消 </el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit"> 确定 </el-button>
      </template>
    </el-dialog>

    <!-- 报名列表对话框 -->
    <el-dialog v-model="registrationsVisible" title="报名列表" width="800px">
      <el-table :data="registrations" style="width: 100%">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="registeredAt" label="报名时间">
          <template #default="{ row }">
            {{ formatDate(row.registeredAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getAdminEvents,
  createEvent,
  updateEvent,
  deleteEvent,
  getEventRegistrations,
} from '@/api/admin'
import type { Event } from '@/types/event'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { formatDate } from '@/utils'

const loading = ref(false)
const submitting = ref(false)
const events = ref<Event[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const registrationsVisible = ref(false)
const editingItem = ref<Event | null>(null)
const registrations = ref<any[]>([])
const currentEventId = ref<number | null>(null)

const form = ref({
  title: '',
  type: 'exhibition',
  description: '',
  cover: '',
  startDate: '',
  endDate: '',
  capacity: 0,
  price: 0,
  status: 'upcoming',
})

const normalizeKey = (value: string | undefined | null) => value?.toString().toLowerCase() || ''

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    exhibition: '展览',
    performance: '演出',
    workshop: '工坊',
    tour: '游览',
  }
  const key = normalizeKey(type)
  return labels[key] || type
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    upcoming: '即将开始',
    ongoing: '进行中',
    past: '已结束',
  }
  const key = normalizeKey(status)
  return labels[key] || status
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    upcoming: 'warning',
    ongoing: 'success',
    past: 'info',
  }
  const key = normalizeKey(status)
  return types[key] || 'info'
}

const loadEvents = async () => {
  loading.value = true
  try {
    const response = await getAdminEvents({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined,
    })
    events.value = (response.list || []).map(item => ({
      ...item,
      status: normalizeKey(item.status),
      type: normalizeKey(item.type),
    }))
    total.value = response.total || 0
  } catch (error: any) {
    console.error('Failed to load events:', error)
    // 如果是404或500错误，说明后端API还未实现
    if (error?.response?.status === 404 || error?.response?.status === 500) {
      ElMessage.warning('管理员API功能尚未实现，请等待后端开发完成')
      events.value = []
      total.value = 0
    } else {
      ElMessage.error('加载活动列表失败：' + (error?.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingItem.value = null
  form.value = {
    title: '',
    type: 'exhibition',
    description: '',
    cover: '',
    startDate: '',
    endDate: '',
    capacity: 0,
    price: 0,
    status: 'upcoming',
  }
  dialogVisible.value = true
}

const handleEdit = (event: Event) => {
  editingItem.value = event
  form.value = {
    title: event.title,
    type: normalizeKey(event.type),
    description: event.description || '',
    cover: event.cover || '',
    startDate: event.startDate,
    endDate: event.endDate,
    capacity: event.capacity || 0,
    price: event.price || 0,
    status: normalizeKey(event.status),
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.startDate || !form.value.endDate) {
    ElMessage.error('请填写必填项')
    return
  }

  submitting.value = true
  try {
    if (editingItem.value) {
      await updateEvent(editingItem.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createEvent(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadEvents()
  } catch (error) {
    console.error('Failed to save event:', error)
    ElMessage.error(editingItem.value ? '更新失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleViewRegistrations = async (event: Event) => {
  currentEventId.value = event.id
  try {
    const data = await getEventRegistrations(event.id)
    registrations.value = Array.isArray(data) ? data : []
    registrationsVisible.value = true
  } catch (error) {
    console.error('Failed to load registrations:', error)
    ElMessage.error('加载报名列表失败')
  }
}

const handleDelete = async (event: Event) => {
  try {
    await ElMessageBox.confirm(`确定要删除活动 "${event.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteEvent(event.id)
    ElMessage.success('删除成功')
    loadEvents()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete event:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
