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
        <el-table-column label="封面" width="150">
          <template #default="{ row }">
            <video
              v-if="row.cover && isVideo(row.cover)"
              :src="row.cover"
              controls
              style="width: 120px; height: 68px; object-fit: cover; border-radius: 4px"
            ></video>
            <el-image
              v-else-if="row.cover"
              :src="row.cover"
              fit="cover"
              style="width: 120px; height: 68px"
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
        <el-form-item label="封面" required>
          <div class="cover-upload-container">
            <el-upload
              class="cover-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              accept="image/*,video/*"
              :before-upload="beforeCoverUpload"
              :on-success="handleCoverUploadSuccess"
              :on-error="handleUploadError"
            >
              <template v-if="form.cover">
                <video
                  v-if="isVideo(form.cover)"
                  :src="form.cover"
                  controls
                  class="cover-preview"
                ></video>
                <el-image
                  v-else
                  :src="form.cover"
                  fit="cover"
                  class="cover-preview"
                />
              </template>
              <el-icon v-else class="uploader-icon">
                <Plus />
              </el-icon>
              <template #tip>
                <div class="el-upload__tip">支持上传本地图片或视频，最大 {{ MAX_VIDEO_SIZE_MB }}MB</div>
              </template>
            </el-upload>
            <el-input
              v-model="form.cover"
              placeholder="或粘贴图片/视频链接"
              class="cover-url-input"
              clearable
            />
          </div>
        </el-form-item>
        <el-form-item label="图片集">
          <div class="media-uploader-row">
            <el-upload
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              multiple
              accept="image/*"
              :before-upload="beforeImageUpload"
              :on-success="handleImageUploadSuccess"
              :on-error="handleUploadError"
            >
              <el-button type="primary" link>上传图片</el-button>
            </el-upload>
          </div>
          <div class="media-preview-list">
            <div
              v-for="(url, index) in form.images"
              :key="`${url}-${index}`"
              class="media-preview-item"
            >
              <el-image :src="url" fit="cover" class="media-image" />
              <el-button link type="danger" class="remove-btn" @click="removeImage(index)">
                移除
              </el-button>
            </div>
            <span v-if="!form.images.length" class="media-empty">暂无图片</span>
          </div>
        </el-form-item>
        <el-form-item label="视频集">
          <div class="media-uploader-row">
            <el-upload
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              multiple
              accept="video/*"
              :before-upload="beforeVideoUpload"
              :on-success="handleVideoUploadSuccess"
              :on-error="handleUploadError"
            >
              <el-button type="primary" link>上传视频</el-button>
            </el-upload>
          </div>
          <div class="media-preview-list">
            <div
              v-for="(url, index) in form.videos"
              :key="`${url}-${index}`"
              class="media-preview-item"
            >
              <video :src="url" controls class="media-video"></video>
              <el-button link type="danger" class="remove-btn" @click="removeVideo(index)">
                移除
              </el-button>
            </div>
            <span v-if="!form.videos.length" class="media-empty">暂无视频</span>
          </div>
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
    <el-dialog v-model="registrationsVisible" title="报名列表" width="1000px">
      <el-table v-loading="registrationsLoading" :data="registrations" style="width: 100%">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="registeredAt" label="报名时间">
          <template #default="{ row }">
            {{ formatDate(row.registeredAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getRegistrationStatusType(row.status)">
              {{ getRegistrationStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核人" width="140">
          <template #default="{ row }">
            {{ row.processedByNickname || row.processedBy || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="审核时间" width="180">
          <template #default="{ row }">
            {{ row.processedAt ? formatDate(row.processedAt, 'YYYY-MM-DD HH:mm') : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180">
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="success"
              :disabled="row.status === 'approved'"
              :loading="registrationLoading[row.id]"
              @click="handleApproveRegistration(row.id)"
            >
              通过
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status === 'rejected'"
              :loading="registrationLoading[row.id]"
              @click="handleRejectRegistration(row.id)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  getAdminEvents,
  createEvent,
  updateEvent,
  deleteEvent,
  getEventRegistrations,
  approveEventRegistration,
  rejectEventRegistration,
} from '@/api/admin'
import type { Event } from '@/types/event'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { formatDate } from '@/utils'
import { useUserStore } from '@/store/user'

interface EventRegistrationItem {
  id: number
  userId: number
  username: string
  nickname?: string
  email: string
  phone?: string
  registeredAt: string
  status: string
  remark?: string
  processedAt?: string
  processedBy?: string
  processedByNickname?: string
}

const userStore = useUserStore()

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
const registrations = ref<EventRegistrationItem[]>([])
const currentEventId = ref<number | null>(null)
const registrationsLoading = ref(false)
const registrationLoading = ref<Record<number, boolean>>({})

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
  images: [] as string[],
  videos: [] as string[],
})

const uploadAction = computed(() => '/api/admin/events/upload')
const uploadHeaders = computed(() => {
  const headers: Record<string, string> = {}
  if (userStore.token) {
    headers.Authorization = `Bearer ${userStore.token}`
  }
  return headers
})

const MAX_IMAGE_SIZE_MB = 20
const MAX_VIDEO_SIZE_MB = 200

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

const isVideo = (url: string | undefined | null) => {
  if (!url) return false
  const lower = url.toLowerCase()
  return ['.mp4', '.mov', '.mkv', '.webm', '.avi'].some(ext => lower.endsWith(ext))
}

const parseUploadResponse = (raw: any): { url: string; type: string } => {
  const tryParse = (value: any) => {
    if (!value) return null
    if (typeof value === 'string') {
      try {
        return JSON.parse(value)
      } catch {
        return null
      }
    }
    return value
  }

  const parsed =
    tryParse(raw) ??
    tryParse(raw?.data) ??
    tryParse(raw?.response) ??
    tryParse(raw?.response?.data) ??
    tryParse(raw?.target?.response)

  const data = parsed?.data ?? parsed ?? {}
  return {
    url: data.url || '',
    type: data.type || data.contentType || '',
  }
}

const beforeCoverUpload = (file: File) => {
  const isSupported = file.type.startsWith('image/') || file.type.startsWith('video/')
  if (!isSupported) {
    ElMessage.error('封面仅支持上传图片或视频文件')
    return false
  }
  const sizeLimit = file.type.startsWith('video/') ? MAX_VIDEO_SIZE_MB : MAX_IMAGE_SIZE_MB
  if (file.size / 1024 / 1024 > sizeLimit) {
    ElMessage.error(`文件大小不能超过 ${sizeLimit}MB`)
    return false
  }
  return true
}

const beforeImageUpload = (file: File) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('仅支持上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 > MAX_IMAGE_SIZE_MB) {
    ElMessage.error(`图片大小不能超过 ${MAX_IMAGE_SIZE_MB}MB`)
    return false
  }
  return true
}

const beforeVideoUpload = (file: File) => {
  if (!file.type.startsWith('video/')) {
    ElMessage.error('仅支持上传视频文件')
    return false
  }
  if (file.size / 1024 / 1024 > MAX_VIDEO_SIZE_MB) {
    ElMessage.error(`视频大小不能超过 ${MAX_VIDEO_SIZE_MB}MB`)
    return false
  }
  return true
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败，请重试')
}

const handleCoverUploadSuccess = (response: any) => {
  const { url } = parseUploadResponse(response)
  if (url) {
    form.value.cover = url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('未获取到封面地址，请重试')
  }
}

const handleImageUploadSuccess = (response: any) => {
  const { url } = parseUploadResponse(response)
  if (url) {
    if (!form.value.images.includes(url)) {
      form.value.images.push(url)
    }
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('未获取到图片地址，请重试')
  }
}

const handleVideoUploadSuccess = (response: any) => {
  const { url } = parseUploadResponse(response)
  if (url) {
    if (!form.value.videos.includes(url)) {
      form.value.videos.push(url)
    }
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error('未获取到视频地址，请重试')
  }
}

const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
}

const removeVideo = (index: number) => {
  form.value.videos.splice(index, 1)
}

const setRegistrationLoading = (id: number, value: boolean) => {
  registrationLoading.value = {
    ...registrationLoading.value,
    [id]: value,
  }
}

const getRegistrationStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
  }
  const key = normalizeKey(status)
  return labels[key] || status
}

const getRegistrationStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
  }
  const key = normalizeKey(status)
  return types[key] || 'info'
}

const fetchRegistrations = async (eventId: number) => {
  registrationsLoading.value = true
  try {
    const data = await getEventRegistrations(eventId)
    registrations.value = (Array.isArray(data) ? data : []).map(item => ({
      ...item,
      status: normalizeKey(item.status),
    }))
    registrationLoading.value = {}
  } catch (error) {
    console.error('Failed to load registrations:', error)
    ElMessage.error('加载报名列表失败')
  } finally {
    registrationsLoading.value = false
  }
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
      images: Array.isArray(item.images) ? [...item.images] : [],
      videos: Array.isArray(item.videos) ? [...item.videos] : [],
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
    images: [],
    videos: [],
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
    images: Array.isArray(event.images) ? [...event.images] : [],
    videos: Array.isArray(event.videos) ? [...event.videos] : [],
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
    const payload = {
      ...form.value,
      cover: form.value.cover?.trim() || '',
      images: [...form.value.images],
      videos: [...form.value.videos],
    }
    if (editingItem.value) {
      await updateEvent(editingItem.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createEvent(payload)
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

const handleApproveRegistration = async (registrationId: number) => {
  if (!currentEventId.value) return
  try {
    await ElMessageBox.confirm('确认通过该报名吗？', '通过报名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  setRegistrationLoading(registrationId, true)
  try {
    await approveEventRegistration(currentEventId.value, registrationId)
    ElMessage.success('已通过报名')
    await fetchRegistrations(currentEventId.value)
    loadEvents()
  } catch (error) {
    console.error('Failed to approve registration:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    setRegistrationLoading(registrationId, false)
  }
}

const handleRejectRegistration = async (registrationId: number) => {
  if (!currentEventId.value) return
  let reason = ''
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝报名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '可填写拒绝原因',
    })
    reason = value?.trim() || ''
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Prompt error:', error)
    }
    return
  }

  setRegistrationLoading(registrationId, true)
  try {
    await rejectEventRegistration(currentEventId.value, registrationId, reason || undefined)
    ElMessage.success('已拒绝报名')
    await fetchRegistrations(currentEventId.value)
    loadEvents()
  } catch (error) {
    console.error('Failed to reject registration:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    setRegistrationLoading(registrationId, false)
  }
}

const handleViewRegistrations = async (event: Event) => {
  currentEventId.value = event.id
  registrationsVisible.value = true
  await fetchRegistrations(event.id)
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

.cover-upload-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    width: 200px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.cover-preview {
  width: 200px;
  height: 120px;
  border-radius: 6px;
  object-fit: cover;
}

.cover-url-input {
  max-width: 360px;
}

.uploader-icon {
  font-size: 24px;
  color: #8c939d;
}

.media-uploader-row {
  margin-bottom: 12px;
}

.media-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.media-preview-item {
  width: 140px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.media-image,
.media-video {
  width: 140px;
  height: 90px;
  border-radius: 4px;
  object-fit: cover;
  background: #f5f7fa;
}

.media-empty {
  color: #909399;
  font-size: 12px;
}

.remove-btn {
  padding: 0;
}
</style>
