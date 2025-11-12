<template>
  <div class="carousel-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>轮播图列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加轮播图
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="carousels" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="200">
          <template #default="{ row }">
            <el-image :src="row.image" fit="cover" style="width: 150px; height: 100px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="subtitle" label="副标题" />
        <el-table-column prop="order" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)"> 编辑 </el-button>
            <el-button link type="danger" @click="handleDelete(row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingItem ? '编辑轮播图' : '添加轮播图'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="图片" required>
          <el-upload
            class="image-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <el-image
              v-if="form.image"
              :src="form.image"
              fit="cover"
              style="width: 100%; height: 300px"
            />
            <el-icon v-else class="uploader-icon">
              <Plus />
            </el-icon>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png 格式，建议尺寸 1920x500</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="可选，轮播图标题" />
        </el-form-item>

        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="可选，轮播图副标题" />
        </el-form-item>

        <el-form-item label="链接">
          <el-input v-model="form.link" placeholder="可选，点击跳转的链接（如 /search）" />
        </el-form-item>

        <el-form-item label="按钮文字">
          <el-input v-model="form.buttonText" placeholder="可选，按钮显示的文字" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.order" :min="0" :max="999" />
          <div class="form-tip">数字越小越靠前</div>
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
          <span style="margin-left: 10px">{{ form.enabled ? '启用' : '禁用' }}</span>
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
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import {
  getAllCarousels,
  createCarousel,
  updateCarousel,
  deleteCarousel,
  uploadCarouselImage,
} from '@/api/carousel'
import type { CarouselItem, CarouselCreateRequest, CarouselUpdateRequest } from '@/types/carousel'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const carousels = ref<CarouselItem[]>([])
const dialogVisible = ref(false)
const editingItem = ref<CarouselItem | null>(null)

const form = ref<CarouselCreateRequest & { enabled: boolean }>({
  image: '',
  title: '',
  subtitle: '',
  link: '',
  buttonText: '',
  order: 0,
  enabled: true,
})

const uploadAction = computed(() => '/api/carousel/upload')
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`,
  }
})

const loadCarousels = async () => {
  loading.value = true
  try {
    const data = await getAllCarousels()
    carousels.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Failed to load carousels:', error)
    ElMessage.error('加载轮播图失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingItem.value = null
  form.value = {
    image: '',
    title: '',
    subtitle: '',
    link: '',
    buttonText: '',
    order: carousels.value.length,
    enabled: true,
  }
  dialogVisible.value = true
}

const handleEdit = (item: CarouselItem) => {
  editingItem.value = item
  form.value = {
    image: item.image,
    title: item.title || '',
    subtitle: item.subtitle || '',
    link: item.link || '',
    buttonText: item.buttonText || '',
    order: item.order,
    enabled: item.enabled,
  }
  dialogVisible.value = true
}

const handleDelete = async (item: CarouselItem) => {
  try {
    await ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteCarousel(item.id)
    ElMessage.success('删除成功')
    loadCarousels()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete carousel:', error)
      ElMessage.error('删除失败')
    }
  }
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = async (response: any) => {
  if (response.url) {
    form.value.image = response.url
    ElMessage.success('上传成功')
  } else if (response.data?.url) {
    form.value.image = response.data.url
    ElMessage.success('上传成功')
  } else {
    try {
      const file = response.raw || response.file
      if (file) {
        const result = await uploadCarouselImage(file)
        form.value.image = result.url
        ElMessage.success('上传成功')
      }
    } catch (error) {
      console.error('Upload error:', error)
      ElMessage.error('上传失败，请重试')
    }
  }
}

const handleSubmit = async () => {
  if (!form.value.image) {
    ElMessage.error('请上传图片')
    return
  }

  submitting.value = true
  try {
    if (editingItem.value) {
      const updateData: CarouselUpdateRequest = {
        image: form.value.image,
        title: form.value.title || undefined,
        subtitle: form.value.subtitle || undefined,
        link: form.value.link || undefined,
        buttonText: form.value.buttonText || undefined,
        order: form.value.order,
        enabled: form.value.enabled,
      }
      await updateCarousel(editingItem.value.id, updateData)
      ElMessage.success('更新成功')
    } else {
      const createData: CarouselCreateRequest = {
        image: form.value.image,
        title: form.value.title || undefined,
        subtitle: form.value.subtitle || undefined,
        link: form.value.link || undefined,
        buttonText: form.value.buttonText || undefined,
        order: form.value.order,
        enabled: form.value.enabled,
      }
      await createCarousel(createData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadCarousels()
  } catch (error) {
    console.error('Failed to save carousel:', error)
    ElMessage.error(editingItem.value ? '更新失败' : '添加失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCarousels()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    width: 100%;
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #409eff;
    }
  }

  .uploader-icon {
    font-size: 28px;
    color: #8c939d;
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
