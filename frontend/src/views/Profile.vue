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
            <el-card v-for="post in myPosts" :key="post.id" class="post-card" @click="$router.push(`/community/post/${post.id}`)">
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/store/user'
import { getFavorites } from '@/api/culture'
import { getMyPosts, getLikedPosts, getCommentedPosts, getFavoritePosts } from '@/api/community'
import { changePassword } from '@/api/user'
import type { CultureResource } from '@/types/culture'
import type { CommunityPost } from '@/types/community'
import CultureCard from '@/components/common/CultureCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { fromNow } from '@/utils'
import { Star, ChatLineRound, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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
}

.profile-tabs {
  :deep(.el-tabs__content) {
    padding: 20px 0;
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    cursor: pointer;
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
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.post-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.post-author {
  flex: 1;
}

.author-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.post-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.post-title {
  font-size: 18px;
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
  flex-wrap: wrap;
}

.post-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
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

.post-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #909399;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
