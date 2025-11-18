<template>
  <div class="heritage-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>非遗资源列表</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索标题或标签"
              style="width: 220px"
              clearable
              @keyup.enter="loadHeritageItems"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-input
              v-model="filterRegion"
              placeholder="地区"
              style="width: 160px"
              clearable
              @keyup.enter="loadHeritageItems"
            />
            <el-select
              v-model="filterLevel"
              placeholder="级别"
              clearable
              style="width: 160px"
              @change="loadHeritageItems"
            >
              <el-option
                v-for="level in heritageLevels"
                :key="level"
                :label="level"
                :value="level"
              />
            </el-select>
            <el-button type="primary" @click="loadHeritageItems">搜索</el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增非遗
            </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="heritageItems" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="row.cover"
              fit="cover"
              style="width: 100px; height: 60px"
            />
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="heritageLevel" label="级别" width="120">
          <template #default="{ row }">
            <el-tag effect="dark">{{ row.heritageLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="featured" label="精选" width="100">
          <template #default="{ row }">
            <el-tag :type="row.featured ? 'success' : 'info'">
              {{ row.featured ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadHeritageItems"
          @current-change="loadHeritageItems"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑非遗资源' : '新增非遗资源'"
      width="820px"
    >
      <el-form :model="form" label-width="110px" class="heritage-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标题" required>
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地区" required>
              <el-input v-model="form.region" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类别" required>
              <el-input v-model="form.category" placeholder="如：传统技艺" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="级别" required>
              <el-select v-model="form.heritageLevel" style="width: 100%">
                <el-option
                  v-for="level in heritageLevels"
                  :key="level"
                  :label="level"
                  :value="level"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="精选展示">
          <el-switch v-model="form.featured" />
        </el-form-item>
        <el-form-item label="封面图">
          <div class="upload-line">
            <el-radio-group v-model="coverUploadType">
              <el-radio-button label="url">URL链接</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>
            <el-input
              v-if="coverUploadType === 'url'"
              v-model="form.cover"
              placeholder="https://example.com/cover.jpg"
            />
            <el-upload
              v-else
              :action="''"
              :auto-upload="false"
              :on-change="handleCoverFileChange"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button>选择图片</el-button>
            </el-upload>
          </div>
          <el-image
            v-if="form.cover"
            :src="form.cover"
            fit="cover"
            style="width: 220px; height: 130px; margin-top: 10px"
          />
        </el-form-item>
        <el-form-item label="图片集">
          <div class="upload-line">
            <el-radio-group v-model="imageUploadType">
              <el-radio-button label="url">URL链接</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>
            <el-input
              v-if="imageUploadType === 'url'"
              v-model="newImageUrl"
              placeholder="图片URL"
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
              <el-button>选择图片</el-button>
            </el-upload>
          </div>
          <div v-if="form.images?.length" class="image-list">
            <div v-for="(image, index) in form.images" :key="image + index" class="image-item">
              <el-image :src="image" fit="cover" style="width: 100px; height: 80px" />
              <el-button
                size="small"
                type="danger"
                circle
                @click="removeImage(index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="视频链接">
          <el-input v-model="form.videoUrl" placeholder="可选" />
        </el-form-item>
        <el-form-item label="标签">
          <div class="tags-input">
            <el-input
              v-model="newTag"
              placeholder="输入标签后回车"
              @keyup.enter.prevent="addTag"
            >
              <template #append>
                <el-button @click="addTag">添加</el-button>
              </template>
            </el-input>
            <div class="tag-list" v-if="form.tags?.length">
              <el-tag
                v-for="(tag, index) in form.tags"
                :key="tag + index"
                closable
                @close="removeTag(index)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="详细内容">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { UploadFile } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Close } from '@element-plus/icons-vue'
import type { HeritageItem } from '@/types/heritage'
import {
  getAdminHeritageItems,
  createHeritageItem,
  updateHeritageItem,
  deleteHeritageItem,
  uploadCultureMedia,
  type HeritageRequestPayload,
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const heritageItems = ref<HeritageItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchKeyword = ref('')
const filterRegion = ref('')
const filterLevel = ref('')

const dialogVisible = ref(false)
const editingItem = ref<HeritageItem | null>(null)

const coverUploadType = ref<'url' | 'upload'>('url')
const imageUploadType = ref<'url' | 'upload'>('url')
const newImageUrl = ref('')
const newTag = ref('')

const heritageLevels = ['国家级', '自治区级', '地州市级', '县区级', '企业级']

const defaultForm: HeritageRequestPayload = {
  title: '',
  region: '',
  category: '',
  cover: '',
  images: [],
  description: '',
  content: '',
  videoUrl: '',
  heritageLevel: heritageLevels[0],
  tags: [],
  featured: false,
}

const form = ref<HeritageRequestPayload>({ ...defaultForm })

const loadHeritageItems = async () => {
  loading.value = true
  try {
    const response = await getAdminHeritageItems({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      region: filterRegion.value || undefined,
      heritageLevel: filterLevel.value || undefined,
    })
    heritageItems.value = response.list || []
    total.value = response.total || 0
  } catch (error: any) {
    console.error('加载非遗资源失败:', error)
    if (error?.response?.status === 404 || error?.response?.status === 500) {
      ElMessage.warning('非遗管理API尚未开放，请联系后端支持')
      heritageItems.value = []
      total.value = 0
    } else {
      ElMessage.error('加载失败：' + (error?.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingItem.value = null
  form.value = { ...defaultForm, images: [], tags: [] }
  coverUploadType.value = 'url'
  imageUploadType.value = 'url'
  newImageUrl.value = ''
  newTag.value = ''
  dialogVisible.value = true
}

const handleEdit = (item: HeritageItem) => {
  editingItem.value = item
  form.value = {
    title: item.title,
    region: item.region,
    category: item.category,
    cover: item.cover || '',
    images: item.images ? [...item.images] : [],
    description: item.description || '',
    content: item.content || '',
    videoUrl: item.videoUrl || '',
    heritageLevel: item.heritageLevel,
    tags: item.tags ? [...item.tags] : [],
    featured: item.featured,
  }
  coverUploadType.value = item.cover ? 'url' : 'url'
  imageUploadType.value = 'url'
  newImageUrl.value = ''
  newTag.value = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.region || !form.value.category || !form.value.heritageLevel) {
    ElMessage.error('请完整填写必填信息')
    return
  }
  submitting.value = true
  try {
    const payload: HeritageRequestPayload = {
      ...form.value,
      tags: form.value.tags?.filter((tag) => !!tag?.trim()) || [],
      images: form.value.images?.filter((img) => !!img?.trim()) || [],
    }
    if (editingItem.value) {
      await updateHeritageItem(editingItem.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createHeritageItem(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadHeritageItems()
  } catch (error) {
    console.error('保存非遗资源失败:', error)
    ElMessage.error(editingItem.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (item: HeritageItem) => {
  try {
    await ElMessageBox.confirm(`确认删除 "${item.title}" ?`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteHeritageItem(item.id)
    ElMessage.success('删除成功')
    loadHeritageItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleCoverFileChange = async (file: UploadFile) => {
  if (!file.raw) return
  try {
    const response = await uploadCultureMedia(file.raw)
    form.value.cover = response.url
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败')
  }
}

const handleImageFileChange = async (file: UploadFile) => {
  if (!file.raw) return
  try {
    const response = await uploadCultureMedia(file.raw)
    if (!form.value.images) form.value.images = []
    form.value.images.push(response.url)
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

const addImageUrl = () => {
  const url = newImageUrl.value.trim()
  if (!url) {
    ElMessage.warning('请输入图片URL')
    return
  }
  if (!form.value.images) form.value.images = []
  form.value.images.push(url)
  newImageUrl.value = ''
  ElMessage.success('图片已添加')
}

const removeImage = (index: number) => {
  form.value.images?.splice(index, 1)
}

const addTag = () => {
  const tag = newTag.value.trim()
  if (!tag) {
    ElMessage.warning('请输入标签')
    return
  }
  if (!form.value.tags) form.value.tags = []
  if (form.value.tags.includes(tag)) {
    ElMessage.info('标签已存在')
    newTag.value = ''
    return
  }
  form.value.tags.push(tag)
  newTag.value = ''
}

const removeTag = (index: number) => {
  form.value.tags?.splice(index, 1)
}

onMounted(() => {
  loadHeritageItems()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.upload-line {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.image-item {
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 4px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;

  .el-button {
    margin: 0;
  }
}

.tags-input {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>





