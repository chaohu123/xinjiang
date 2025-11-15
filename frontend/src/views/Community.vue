<template>
  <div class="community-page">
    <div class="container">
      <div class="page-header">
        <h1>{{ $t('community.title') }}</h1>
        <el-button type="primary" @click="handleCreatePostClick">
          <el-icon><Plus /></el-icon>
          {{ $t('community.createPost') }}
        </el-button>
      </div>

      <div class="community-tabs">
        <el-radio-group v-model="sortBy" @change="loadPosts">
          <el-radio-button value="latest">
            {{ $t('community.latest') }}
          </el-radio-button>
          <el-radio-button value="hot">
            {{ $t('community.hot') }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-loading="loading" class="posts-list">
        <el-card v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <el-avatar :src="post.author.avatar" :size="40">
              {{ post.author.username[0] }}
            </el-avatar>
            <div class="post-author">
              <div class="author-name">
                {{ post.author.username }}
              </div>
              <div class="post-time">
                {{ fromNow(post.createdAt) }}
              </div>
            </div>
          </div>
          <h3 class="post-title" @click="$router.push(`/community/post/${post.id}`)">
            {{ post.title }}
          </h3>
          <p class="post-content">
            {{ post.content }}
          </p>
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <el-image
              v-for="(img, index) in post.images.slice(0, 3)"
              :key="index"
              :src="img"
              fit="cover"
              class="post-image"
            />
          </div>
          <div class="post-footer">
            <div class="post-tags">
              <el-tag v-for="tag in post.tags" :key="tag" size="small" style="margin-right: 8px">
                {{ tag }}
              </el-tag>
            </div>
            <div class="post-actions">
              <LikeButton
                :initial-count="post.likes"
                :initial-active="post.isLiked || false"
                @like-toggled="(isActive) => handleLikeToggle(post, isActive)"
              />
              <el-button text>
                <el-icon><ChatLineRound /></el-icon>
                {{ post.comments }}
              </el-button>
              <el-button text>
                <el-icon><Share /></el-icon>
                {{ $t('community.share') }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <EmptyState v-if="!loading && posts.length === 0" :text="$t('common.noData')" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        class="pagination"
        @current-change="handlePageChange"
      />

      <el-dialog v-model="showCreateDialog" :title="$t('community.createPost')" width="800px">
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="createForm.title" :placeholder="$t('community.titlePlaceholder')" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input
              v-model="createForm.content"
              type="textarea"
              :rows="6"
              :placeholder="$t('community.contentPlaceholder')"
            />
          </el-form-item>
          <el-form-item label="图片">
            <div class="image-upload-section">
              <el-tabs v-model="imageUploadTab" class="image-upload-tabs">
                <el-tab-pane label="本地上传" name="upload">
                  <el-upload
                    :file-list="uploadFileList"
                    :action="''"
                    :auto-upload="false"
                    :on-change="handleFileChange"
                    :on-remove="handleFileRemove"
                    list-type="picture-card"
                    :limit="9"
                    accept="image/*"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <div style="margin-top: 10px">
                    <el-button
                      type="primary"
                      size="small"
                      :loading="uploading"
                      @click="handleUploadImages"
                    >
                      上传图片
                    </el-button>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="URL添加" name="url">
                  <div class="url-input-section">
                    <el-input
                      v-model="imageUrlInput"
                      placeholder="输入图片URL，按回车添加"
                      @keyup.enter="addImageUrl"
                    >
                      <template #append>
                        <el-button @click="addImageUrl">添加</el-button>
                      </template>
                    </el-input>
                  </div>
                </el-tab-pane>
              </el-tabs>
              <div v-if="createForm.images && createForm.images.length > 0" class="image-preview-list">
                <div v-for="(img, index) in createForm.images" :key="index" class="image-preview-item">
                  <el-image :src="img" fit="cover" class="preview-image" />
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    class="remove-image-btn"
                    @click="removeImage(index)"
                  >
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="标签">
            <el-input
              v-model="tagInput"
              placeholder="输入标签，用逗号分隔"
              @keyup.enter="addTags"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button type="primary" @click="handleCreate">
            {{ $t('common.submit') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCommunityPosts, createPost, likePost, unlikePost, uploadImage } from '@/api/community'
import type { CommunityPost } from '@/types/community'
import type { UploadFile, UploadFiles } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import LikeButton from '@/components/common/LikeButton.vue'
import { fromNow } from '@/utils'
import { Plus, Star, ChatLineRound, Share, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { requireAuth } from '@/utils/auth'

const router = useRouter()
const posts = ref<CommunityPost[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('latest')
const showCreateDialog = ref(false)
const createForm = ref({
  title: '',
  content: '',
  images: [] as string[],
  tags: [] as string[],
})
const tagInput = ref('')
const imageUploadTab = ref('upload')
const uploadFileList = ref<UploadFiles>([])
const imageUrlInput = ref('')
const uploading = ref(false)

const addTags = () => {
  if (tagInput.value.trim()) {
    const tags = tagInput.value
      .split(',')
      .map(t => t.trim())
      .filter(t => t)
    createForm.value.tags = [...new Set([...createForm.value.tags, ...tags])]
    tagInput.value = ''
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const response = await getCommunityPosts({
      page: currentPage.value,
      size: pageSize.value,
      sort: sortBy.value as 'latest' | 'hot',
    })
    posts.value = response.list || []
    total.value = response.total || 0
  } catch (error) {
    console.error('Failed to load posts:', error)
    posts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  loadPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleLikeToggle = async (post: CommunityPost, isActive: boolean) => {
  // 检查登录状态
  if (!requireAuth('请先登录后再点赞', router)) {
    // 如果未登录，回滚状态
    post.isLiked = !isActive
    post.likes = isActive ? Math.max(0, (post.likes || 0) - 1) : (post.likes || 0) + 1
    return
  }

  try {
    if (isActive) {
      await likePost(post.id)
      post.likes = (post.likes || 0) + 1
      post.isLiked = true
    } else {
      await unlikePost(post.id)
      post.likes = Math.max(0, (post.likes || 0) - 1)
      post.isLiked = false
    }
  } catch (error: any) {
    const errorMessage = error?.response?.data?.message || error.message || '操作失败'
    if (error.response?.status !== 401) {
      ElMessage.error(errorMessage)
    }
    // 回滚状态
    post.isLiked = !isActive
    post.likes = isActive ? Math.max(0, (post.likes || 0) - 1) : (post.likes || 0) + 1
  }
}

const handleFileChange = (file: UploadFile, fileList: UploadFiles) => {
  uploadFileList.value = fileList
}

const handleFileRemove = (file: UploadFile, fileList: UploadFiles) => {
  uploadFileList.value = fileList
}

const handleUploadImages = async () => {
  if (uploadFileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的图片')
    return
  }

  uploading.value = true
  try {
    const uploadPromises = uploadFileList.value.map(file => {
      if (file.raw) {
        return uploadImage(file.raw)
      }
      return Promise.resolve(null)
    })

    const results = await Promise.all(uploadPromises)
    const uploadedUrls = results
      .filter((result): result is { url: string; type: string } => result !== null)
      .map(result => result.url)

    createForm.value.images = [...createForm.value.images, ...uploadedUrls]
    uploadFileList.value = []
    ElMessage.success(`成功上传${uploadedUrls.length}张图片`)
  } catch (error) {
    console.error('Failed to upload images:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

const addImageUrl = () => {
  const url = imageUrlInput.value.trim()
  if (!url) {
    ElMessage.warning('请输入图片URL')
    return
  }

  // 简单的URL验证
  try {
    new URL(url)
    if (!createForm.value.images.includes(url)) {
      createForm.value.images.push(url)
      imageUrlInput.value = ''
      ElMessage.success('图片URL已添加')
    } else {
      ElMessage.warning('该图片URL已存在')
    }
  } catch (error) {
    ElMessage.error('请输入有效的图片URL')
  }
}

const removeImage = (index: number) => {
  createForm.value.images.splice(index, 1)
}

const handleCreatePostClick = () => {
  // 检查登录状态
  if (!requireAuth('请先登录后再发布帖子', router)) {
    return
  }
  showCreateDialog.value = true
}

const handleCreate = async () => {
  if (!createForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!createForm.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  // 再次检查登录状态（防止在填写表单期间登录状态失效）
  if (!requireAuth('请先登录后再发布帖子', router)) {
    return
  }

  try {
    await createPost({
      title: createForm.value.title,
      content: createForm.value.content,
      images: createForm.value.images,
      tags: createForm.value.tags,
    })
    ElMessage.success('发布成功')
    showCreateDialog.value = false
    createForm.value = { title: '', content: '', images: [], tags: [] }
    uploadFileList.value = []
    imageUrlInput.value = ''
    imageUploadTab.value = 'upload'
    loadPosts()
  } catch (error: any) {
    console.error('Failed to create post:', error)
    // 如果是401错误，说明未登录，已经在axios拦截器中处理
    // 其他错误显示具体错误信息
    const errorMessage = error.response?.data?.message || error.message || '发布失败'
    if (error.response?.status !== 401) {
      ElMessage.error(errorMessage)
    }
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style lang="scss" scoped>
.community-page {
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

.community-tabs {
  margin-bottom: 24px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.post-card {
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-2px);
  }
}

.post-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.post-author {
  flex: 1;
}

.author-name {
  font-weight: 500;
  color: #303133;
}

.post-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #409eff;
  }
}

.post-content {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.post-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.post-tags {
  flex: 1;
}

.post-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.image-upload-section {
  width: 100%;
}

.image-upload-tabs {
  margin-bottom: 20px;
}

.url-input-section {
  margin-top: 10px;
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.image-preview-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.remove-image-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  padding: 4px;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  color: white;

  &:hover {
    background: rgba(245, 108, 108, 0.8);
  }
}
</style>
