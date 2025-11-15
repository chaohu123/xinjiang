<template>
  <div class="culture-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文化资源列表</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索资源标题"
              style="width: 300px; margin-right: 12px"
              clearable
              @keyup.enter="loadResources"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterType"
              placeholder="类型"
              style="width: 150px; margin-right: 12px"
              clearable
            >
              <el-option label="文章" value="article" />
              <el-option label="展品" value="exhibit" />
              <el-option label="视频" value="video" />
              <el-option label="音频" value="audio" />
            </el-select>
            <el-button type="primary" @click="loadResources"> 搜索 </el-button>
            <el-button type="primary" style="margin-left: 12px" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加资源
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="resources" style="width: 100%">
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
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="favorites" label="收藏数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
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
          @size-change="loadResources"
          @current-change="loadResources"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑文化资源' : '添加文化资源'"
      width="800px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="文章" value="article" />
            <el-option label="展品" value="exhibit" />
            <el-option label="视频" value="video" />
            <el-option label="音频" value="audio" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="封面">
          <div style="display: flex; gap: 10px; align-items: flex-start">
            <el-radio-group v-model="coverUploadType" style="margin-right: 10px">
              <el-radio-button label="url">URL链接</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>
            <el-input
              v-if="coverUploadType === 'url'"
              v-model="form.cover"
              placeholder="封面图片URL"
              style="flex: 1"
            />
            <el-upload
              v-else
              :action="''"
              :auto-upload="false"
              :on-change="handleCoverFileChange"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary">选择图片</el-button>
            </el-upload>
          </div>
          <el-image
            v-if="form.cover"
            :src="form.cover"
            fit="cover"
            style="width: 200px; height: 120px; margin-top: 10px"
          />
        </el-form-item>
        <el-form-item label="图片列表">
          <div style="margin-bottom: 10px">
            <el-radio-group v-model="imageUploadType" style="margin-right: 10px">
              <el-radio-button label="url">URL链接</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>
            <el-input
              v-if="imageUploadType === 'url'"
              v-model="newImageUrl"
              placeholder="输入图片URL"
              style="width: 300px; margin-right: 10px"
              @keyup.enter="addImageUrl"
            >
              <template #append>
                <el-button @click="addImageUrl">添加</el-button>
              </template>
            </el-input>
            <el-upload
              v-else
              :action="''"
              :auto-upload="false"
              :on-change="handleImageFileChange"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary">选择图片</el-button>
            </el-upload>
          </div>
          <div v-if="form.images && form.images.length > 0" class="image-list">
            <div
              v-for="(image, index) in form.images"
              :key="index"
              class="image-item"
            >
              <el-image :src="image" fit="cover" style="width: 100px; height: 100px" />
              <el-button
                type="danger"
                size="small"
                circle
                style="position: absolute; top: -5px; right: -5px"
                @click="removeImage(index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="视频">
          <div style="display: flex; gap: 10px; align-items: flex-start">
            <el-radio-group v-model="videoUploadType" style="margin-right: 10px">
              <el-radio-button label="url">URL链接</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>
            <el-input
              v-if="videoUploadType === 'url'"
              v-model="form.videoUrl"
              placeholder="视频URL"
              style="flex: 1"
            />
            <el-upload
              v-else
              :action="''"
              :auto-upload="false"
              :on-change="handleVideoFileChange"
              :show-file-list="false"
              accept="video/*"
            >
              <el-button type="primary">选择视频</el-button>
            </el-upload>
          </div>
          <video
            v-if="form.videoUrl"
            :src="form.videoUrl"
            controls
            style="width: 100%; max-width: 500px; margin-top: 10px"
          />
        </el-form-item>
        <el-form-item label="地区" required>
          <el-input v-model="form.region" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" />
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
  getAdminCultureResources,
  createCultureResource,
  updateCultureResource,
  deleteCultureResource,
  uploadCultureMedia,
} from '@/api/admin'
import type { CultureResource, CultureType } from '@/types/culture'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Close } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const resources = ref<CultureResource[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterType = ref<CultureType | ''>('')
const dialogVisible = ref(false)
const editingItem = ref<CultureResource | null>(null)

const form = ref({
  type: 'article' as CultureType,
  title: '',
  description: '',
  cover: '',
  images: [] as string[],
  videoUrl: '',
  region: '',
  content: '',
})

const coverUploadType = ref<'url' | 'upload'>('url')
const imageUploadType = ref<'url' | 'upload'>('url')
const videoUploadType = ref<'url' | 'upload'>('url')
const newImageUrl = ref('')
const uploading = ref(false)

const getTypeLabel = (type: CultureType) => {
  const labels: Record<CultureType, string> = {
    article: '文章',
    exhibit: '展品',
    video: '视频',
    audio: '音频',
  }
  return labels[type] || type
}

const loadResources = async () => {
  loading.value = true
  try {
    const response = await getAdminCultureResources({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      type: filterType.value || undefined,
    })
    // 将后端返回的大写类型转换为小写
    const resourcesList = (response.list || []).map((resource: any) => ({
      ...resource,
      type: resource.type?.toLowerCase() as CultureType,
    }))
    resources.value = resourcesList
    total.value = response.total || 0
  } catch (error: any) {
    console.error('Failed to load resources:', error)
    // 如果是404或500错误，说明后端API还未实现
    if (error?.response?.status === 404 || error?.response?.status === 500) {
      ElMessage.warning('管理员API功能尚未实现，请等待后端开发完成')
      resources.value = []
      total.value = 0
    } else {
      ElMessage.error('加载资源列表失败：' + (error?.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingItem.value = null
  form.value = {
    type: 'article',
    title: '',
    description: '',
    cover: '',
    images: [],
    videoUrl: '',
    region: '',
    content: '',
  }
  coverUploadType.value = 'url'
  imageUploadType.value = 'url'
  videoUploadType.value = 'url'
  newImageUrl.value = ''
  dialogVisible.value = true
}

const handleEdit = (resource: CultureResource) => {
  editingItem.value = resource
  form.value = {
    type: resource.type,
    title: resource.title,
    description: resource.description || '',
    cover: resource.cover || '',
    images: resource.images || [],
    videoUrl: resource.videoUrl || '',
    region: resource.region,
    content: resource.content || '',
  }
  coverUploadType.value = resource.cover ? (resource.cover.startsWith('http') ? 'url' : 'upload') : 'url'
  imageUploadType.value = 'url'
  videoUploadType.value = resource.videoUrl ? (resource.videoUrl.startsWith('http') ? 'url' : 'upload') : 'url'
  newImageUrl.value = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.region) {
    ElMessage.error('请填写必填项')
    return
  }

  submitting.value = true
  try {
    // 将小写类型转换为大写，以匹配后端枚举
    const submitData = {
      ...form.value,
      type: form.value.type.toUpperCase() as any,
    }
    if (editingItem.value) {
      await updateCultureResource(editingItem.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createCultureResource(submitData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadResources()
  } catch (error) {
    console.error('Failed to save resource:', error)
    ElMessage.error(editingItem.value ? '更新失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (resource: CultureResource) => {
  try {
    await ElMessageBox.confirm(`确定要删除资源 "${resource.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteCultureResource(resource.id)
    ElMessage.success('删除成功')
    loadResources()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete resource:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 处理封面上传
const handleCoverFileChange = async (file: UploadFile) => {
  if (!file.raw) return
  uploading.value = true
  try {
    const response = await uploadCultureMedia(file.raw)
    form.value.cover = response.url
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('Failed to upload cover:', error)
    ElMessage.error('封面上传失败')
  } finally {
    uploading.value = false
  }
}

// 处理图片上传
const handleImageFileChange = async (file: UploadFile) => {
  if (!file.raw) return
  uploading.value = true
  try {
    const response = await uploadCultureMedia(file.raw)
    if (!form.value.images) {
      form.value.images = []
    }
    form.value.images.push(response.url)
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('Failed to upload image:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

// 添加图片URL
const addImageUrl = () => {
  if (!newImageUrl.value.trim()) {
    ElMessage.warning('请输入图片URL')
    return
  }
  if (!form.value.images) {
    form.value.images = []
  }
  form.value.images.push(newImageUrl.value.trim())
  newImageUrl.value = ''
  ElMessage.success('图片添加成功')
}

// 移除图片
const removeImage = (index: number) => {
  if (form.value.images) {
    form.value.images.splice(index, 1)
  }
}

// 处理视频上传
const handleVideoFileChange = async (file: UploadFile) => {
  if (!file.raw) return
  uploading.value = true
  try {
    const response = await uploadCultureMedia(file.raw)
    form.value.videoUrl = response.url
    ElMessage.success('视频上传成功')
  } catch (error) {
    console.error('Failed to upload video:', error)
    ElMessage.error('视频上传失败')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadResources()
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

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.image-item {
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 5px;
}
</style>
