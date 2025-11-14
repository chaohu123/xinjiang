<template>
  <div class="profile-page">
    <div class="container">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane :label="$t('profile.basicInfo')" name="info">
          <el-card>
            <el-form :model="userInfo" label-width="100px" style="max-width: 600px">
              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  action="/api/upload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                >
                  <el-avatar :src="userInfo.avatar" :size="100">
                    {{ userInfo.username?.[0] }}
                  </el-avatar>
                </el-upload>
              </el-form-item>
              <el-form-item :label="$t('profile.username')">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item :label="$t('profile.email')">
                <el-input v-model="userInfo.email" />
              </el-form-item>
              <el-form-item :label="$t('profile.phone')">
                <el-input v-model="userInfo.phone" />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="userInfo.nickname" />
              </el-form-item>
              <el-form-item label="简介">
                <el-input v-model="userInfo.bio" type="textarea" :rows="4" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSave">
                  {{ $t('common.save') }}
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <el-tab-pane :label="$t('profile.favorites')" name="favorites">
          <el-tabs v-model="favoriteSubTab" class="sub-tabs">
            <el-tab-pane label="文化资源" name="culture">
              <div v-loading="favoritesLoading" class="favorites-grid">
                <CultureCard v-for="item in favorites" :key="item.id" :resource="item" />
              </div>
              <EmptyState
                v-if="!favoritesLoading && favorites.length === 0"
                :text="$t('common.noData')"
              />
            </el-tab-pane>
            <el-tab-pane label="收藏的帖子" name="posts">
              <div v-loading="favoritePostsLoading" class="posts-list">
                <el-card v-for="post in favoritePosts" :key="post.id" class="post-card" @click="$router.push(`/community/post/${post.id}`)">
                  <div class="post-header">
                    <el-avatar :src="post.author.avatar" :size="40">
                      {{ post.author.username[0] }}
                    </el-avatar>
                    <div class="post-author">
                      <div class="author-name">{{ post.author.username }}</div>
                      <div class="post-time">{{ fromNow(post.createdAt) }}</div>
                    </div>
                  </div>
                  <h3 class="post-title">{{ post.title }}</h3>
                  <p class="post-content">{{ post.content }}</p>
                  <div class="post-footer">
                    <div class="post-tags">
                      <el-tag v-for="tag in post.tags" :key="tag" size="small" style="margin-right: 8px">
                        {{ tag }}
                      </el-tag>
                    </div>
                    <div class="post-meta">
                      <span>
                        <el-icon><Star /></el-icon>
                        {{ post.likes }}
                      </span>
                      <span>
                        <el-icon><ChatLineRound /></el-icon>
                        {{ post.comments }}
                      </span>
                    </div>
                  </div>
                </el-card>
              </div>
              <EmptyState v-if="!favoritePostsLoading && favoritePosts.length === 0" :text="$t('common.noData')" />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <el-tab-pane :label="$t('profile.posts')" name="posts">
          <div v-loading="myPostsLoading" class="posts-list">
            <el-card v-for="post in myPosts" :key="post.id" class="post-card">
              <div class="post-header">
                <el-avatar :src="post.author.avatar" :size="40">
                  {{ post.author.username[0] }}
                </el-avatar>
                <div class="post-author">
                  <div class="author-name">{{ post.author.username }}</div>
                  <div class="post-time">{{ fromNow(post.createdAt) }}</div>
                </div>
                <div class="post-actions" @click.stop>
                  <el-button
                    type="primary"
                    size="small"
                    :icon="Edit"
                    @click="handleEditPost(post)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    :icon="Delete"
                    @click="handleDeletePost(post)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              <h3 class="post-title" @click="$router.push(`/community/post/${post.id}`)">{{ post.title }}</h3>
              <p class="post-content" @click="$router.push(`/community/post/${post.id}`)">{{ post.content }}</p>
              <div v-if="post.images && post.images.length > 0" class="post-images" @click.stop>
                <el-image
                  v-for="(img, index) in post.images.slice(0, 3)"
                  :key="index"
                  :src="img"
                  fit="cover"
                  class="post-image"
                  :preview-src-list="post.images"
                  :initial-index="index"
                />
              </div>
              <div class="post-footer">
                <div class="post-tags">
                  <el-tag v-for="tag in post.tags" :key="tag" size="small" style="margin-right: 8px">
                    {{ tag }}
                  </el-tag>
                </div>
                <div class="post-meta">
                  <span>
                    <el-icon><Star /></el-icon>
                    {{ post.likes }}
                  </span>
                  <span>
                    <el-icon><ChatLineRound /></el-icon>
                    {{ post.comments }}
                  </span>
                  <span>
                    <el-icon><View /></el-icon>
                    {{ post.views }}
                  </span>
                </div>
              </div>
            </el-card>
          </div>
          <EmptyState v-if="!myPostsLoading && myPosts.length === 0" :text="$t('common.noData')" />
        </el-tab-pane>

        <el-tab-pane label="我的互动" name="interactions">
          <el-tabs v-model="interactionSubTab" class="sub-tabs">
            <el-tab-pane label="点赞的帖子" name="liked">
              <div v-loading="likedPostsLoading" class="posts-list">
                <el-card v-for="post in likedPosts" :key="post.id" class="post-card" @click="$router.push(`/community/post/${post.id}`)">
                  <div class="post-header">
                    <el-avatar :src="post.author.avatar" :size="40">
                      {{ post.author.username[0] }}
                    </el-avatar>
                    <div class="post-author">
                      <div class="author-name">{{ post.author.username }}</div>
                      <div class="post-time">{{ fromNow(post.createdAt) }}</div>
                    </div>
                  </div>
                  <h3 class="post-title">{{ post.title }}</h3>
                  <p class="post-content">{{ post.content }}</p>
                  <div class="post-footer">
                    <div class="post-tags">
                      <el-tag v-for="tag in post.tags" :key="tag" size="small" style="margin-right: 8px">
                        {{ tag }}
                      </el-tag>
                    </div>
                    <div class="post-meta">
                      <span>
                        <el-icon><Star /></el-icon>
                        {{ post.likes }}
                      </span>
                      <span>
                        <el-icon><ChatLineRound /></el-icon>
                        {{ post.comments }}
                      </span>
                    </div>
                  </div>
                </el-card>
              </div>
              <EmptyState v-if="!likedPostsLoading && likedPosts.length === 0" :text="$t('common.noData')" />
            </el-tab-pane>
            <el-tab-pane label="评论的帖子" name="commented">
              <div v-loading="commentedPostsLoading" class="posts-list">
                <el-card v-for="post in commentedPosts" :key="post.id" class="post-card" @click="$router.push(`/community/post/${post.id}`)">
                  <div class="post-header">
                    <el-avatar :src="post.author.avatar" :size="40">
                      {{ post.author.username[0] }}
                    </el-avatar>
                    <div class="post-author">
                      <div class="author-name">{{ post.author.username }}</div>
                      <div class="post-time">{{ fromNow(post.createdAt) }}</div>
                    </div>
                  </div>
                  <h3 class="post-title">{{ post.title }}</h3>
                  <p class="post-content">{{ post.content }}</p>
                  <div class="post-footer">
                    <div class="post-tags">
                      <el-tag v-for="tag in post.tags" :key="tag" size="small" style="margin-right: 8px">
                        {{ tag }}
                      </el-tag>
                    </div>
                    <div class="post-meta">
                      <span>
                        <el-icon><Star /></el-icon>
                        {{ post.likes }}
                      </span>
                      <span>
                        <el-icon><ChatLineRound /></el-icon>
                        {{ post.comments }}
                      </span>
                    </div>
                  </div>
                </el-card>
              </div>
              <EmptyState v-if="!commentedPostsLoading && commentedPosts.length === 0" :text="$t('common.noData')" />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <el-tab-pane :label="$t('profile.settings')" name="settings">
          <el-card>
            <h3>{{ $t('profile.changePassword') }}</h3>
            <el-form :model="passwordForm" label-width="120px" style="max-width: 500px">
              <el-form-item label="当前密码">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword"> 修改密码 </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>

      <!-- 编辑帖子对话框 -->
      <el-dialog v-model="showEditDialog" title="编辑帖子" width="800px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="editForm.title" placeholder="请输入标题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input
              v-model="editForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入内容"
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
              <div v-if="editForm.images && editForm.images.length > 0" class="image-preview-list">
                <div v-for="(img, index) in editForm.images" :key="index" class="image-preview-item">
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
              placeholder="输入标签，用逗号分隔，按回车添加"
              @keyup.enter="addTags"
            />
            <div v-if="editForm.tags && editForm.tags.length > 0" class="tags-display">
              <el-tag
                v-for="(tag, index) in editForm.tags"
                :key="index"
                closable
                style="margin-right: 8px; margin-top: 8px"
                @close="removeTag(index)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleUpdatePost">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { getFavorites } from '@/api/culture'
import {
  getMyPosts,
  getLikedPosts,
  getCommentedPosts,
  getFavoritePosts,
  updatePost,
  deletePost,
  uploadImage,
} from '@/api/community'
import { changePassword } from '@/api/user'
import type { CultureResource } from '@/types/culture'
import type { CommunityPost } from '@/types/community'
import type { UploadFile, UploadFiles } from 'element-plus'
import CultureCard from '@/components/common/CultureCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { fromNow } from '@/utils'
import { Star, ChatLineRound, View, Edit, Delete, Plus, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('info')
const favoriteSubTab = ref('culture')
const interactionSubTab = ref('liked')
const favorites = ref<CultureResource[]>([])
const favoritesLoading = ref(false)
const myPosts = ref<CommunityPost[]>([])
const myPostsLoading = ref(false)
const favoritePosts = ref<CommunityPost[]>([])
const favoritePostsLoading = ref(false)
const likedPosts = ref<CommunityPost[]>([])
const likedPostsLoading = ref(false)
const commentedPosts = ref<CommunityPost[]>([])
const commentedPostsLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const showEditDialog = ref(false)
const editingPost = ref<CommunityPost | null>(null)
const submitting = ref(false)
const editForm = ref({
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

const userInfo = computed(() => userStore.userInfo || {})

const handleAvatarSuccess = (response: any) => {
  if (userStore.userInfo) {
    userStore.userInfo.avatar = response.url
  }
}

const handleSave = async () => {
  try {
    await userStore.updateUser(userInfo.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  try {
    await changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error) {
    ElMessage.error('密码修改失败')
  }
}

const loadFavorites = async () => {
  favoritesLoading.value = true
  try {
    const response = await getFavorites({ page: 1, size: 20 })
    favorites.value = response.list
  } catch (error) {
    console.error('Failed to load favorites:', error)
  } finally {
    favoritesLoading.value = false
  }
}

const loadMyPosts = async () => {
  myPostsLoading.value = true
  try {
    const response = await getMyPosts({ page: 1, size: 20 })
    myPosts.value = response.list || []
  } catch (error) {
    console.error('Failed to load posts:', error)
    myPosts.value = []
  } finally {
    myPostsLoading.value = false
  }
}

const loadFavoritePosts = async () => {
  favoritePostsLoading.value = true
  try {
    const response = await getFavoritePosts({ page: 1, size: 20 })
    favoritePosts.value = response.list || []
  } catch (error) {
    console.error('Failed to load favorite posts:', error)
    favoritePosts.value = []
  } finally {
    favoritePostsLoading.value = false
  }
}

const loadLikedPosts = async () => {
  likedPostsLoading.value = true
  try {
    const response = await getLikedPosts({ page: 1, size: 20 })
    likedPosts.value = response.list || []
  } catch (error) {
    console.error('Failed to load liked posts:', error)
    likedPosts.value = []
  } finally {
    likedPostsLoading.value = false
  }
}

const loadCommentedPosts = async () => {
  commentedPostsLoading.value = true
  try {
    const response = await getCommentedPosts({ page: 1, size: 20 })
    commentedPosts.value = response.list || []
  } catch (error) {
    console.error('Failed to load commented posts:', error)
    commentedPosts.value = []
  } finally {
    commentedPostsLoading.value = false
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'favorites') {
    if (favoriteSubTab.value === 'culture') {
      loadFavorites()
    } else {
      loadFavoritePosts()
    }
  } else if (newTab === 'posts') {
    loadMyPosts()
  } else if (newTab === 'interactions') {
    if (interactionSubTab.value === 'liked') {
      loadLikedPosts()
    } else {
      loadCommentedPosts()
    }
  }
})

watch(favoriteSubTab, (newTab) => {
  if (newTab === 'posts') {
    loadFavoritePosts()
  }
})

watch(interactionSubTab, (newTab) => {
  if (newTab === 'liked') {
    loadLikedPosts()
  } else {
    loadCommentedPosts()
  }
})

const handleEditPost = (post: CommunityPost) => {
  editingPost.value = post
  editForm.value = {
    title: post.title,
    content: post.content,
    images: post.images ? [...post.images] : [],
    tags: post.tags ? [...post.tags] : [],
  }
  tagInput.value = ''
  imageUrlInput.value = ''
  uploadFileList.value = []
  imageUploadTab.value = 'upload'
  showEditDialog.value = true
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

    editForm.value.images = [...editForm.value.images, ...uploadedUrls]
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

  try {
    new URL(url)
    if (!editForm.value.images.includes(url)) {
      editForm.value.images.push(url)
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
  editForm.value.images.splice(index, 1)
}

const addTags = () => {
  if (tagInput.value.trim()) {
    const tags = tagInput.value
      .split(',')
      .map(t => t.trim())
      .filter(t => t)
    editForm.value.tags = [...new Set([...editForm.value.tags, ...tags])]
    tagInput.value = ''
  }
}

const removeTag = (index: number) => {
  editForm.value.tags.splice(index, 1)
}

const handleUpdatePost = async () => {
  if (!editForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!editForm.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  if (!editingPost.value) {
    return
  }

  submitting.value = true
  try {
    await updatePost(editingPost.value.id, {
      title: editForm.value.title,
      content: editForm.value.content,
      images: editForm.value.images,
      tags: editForm.value.tags,
    })
    ElMessage.success('更新成功')
    showEditDialog.value = false
    loadMyPosts()
  } catch (error) {
    console.error('Failed to update post:', error)
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

const handleDeletePost = async (post: CommunityPost) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除帖子 "${post.title}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
      }
    )

    try {
      await deletePost(post.id)
      ElMessage.success('删除成功')
      loadMyPosts()
    } catch (error) {
      console.error('Failed to delete post:', error)
      ElMessage.error('删除失败')
    }
  } catch (error) {
    // 用户取消删除
    if (error !== 'cancel') {
      console.error('Delete confirmation error:', error)
    }
  }
}

onMounted(() => {
  if (activeTab.value === 'favorites') {
    if (favoriteSubTab.value === 'culture') {
      loadFavorites()
    } else {
      loadFavoritePosts()
    }
  } else if (activeTab.value === 'posts') {
    loadMyPosts()
  } else if (activeTab.value === 'interactions') {
    if (interactionSubTab.value === 'liked') {
      loadLikedPosts()
    } else {
      loadCommentedPosts()
    }
  }
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.profile-tabs {
  :deep(.el-tabs__header) {
    background: #fff;
    padding: 0 20px;
    border-radius: 12px 12px 0 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  :deep(.el-tabs__content) {
    padding: 30px 20px;
    background: #fff;
    border-radius: 0 0 12px 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  :deep(.el-tab-pane) {
    min-height: 400px;
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    cursor: pointer;
    border-radius: 50%;
    overflow: hidden;
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.sub-tabs {
  :deep(.el-tabs__content) {
    padding: 20px 0;
  }
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  background: #fff;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #409eff;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.post-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  position: relative;
}

.post-author {
  flex: 1;
}

.post-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.author-name {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
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
  line-height: 1.4;

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
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #303133;
  }
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  .post-image {
    width: 140px;
    height: 140px;
    border-radius: 8px;
    object-fit: cover;
    cursor: pointer;
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;
}

.post-tags {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #909399;

  span {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: default;
    transition: color 0.3s;

    &:hover {
      color: #409eff;
    }
  }
}

// 编辑对话框样式
.image-upload-section {
  width: 100%;
}

.image-upload-tabs {
  margin-bottom: 16px;
}

.url-input-section {
  margin-top: 10px;
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.image-preview-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;

  .preview-image {
    width: 100%;
    height: 100%;
  }

  .remove-image-btn {
    position: absolute;
    top: 4px;
    right: 4px;
    z-index: 10;
  }
}

.tags-display {
  margin-top: 12px;
  min-height: 32px;
}
</style>
