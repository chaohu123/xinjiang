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
          <div v-loading="favoritesLoading" class="favorites-grid">
            <CultureCard v-for="item in favorites" :key="item.id" :resource="item" />
          </div>
          <EmptyState
            v-if="!favoritesLoading && favorites.length === 0"
            :text="$t('common.noData')"
          />
        </el-tab-pane>

        <el-tab-pane :label="$t('profile.posts')" name="posts">
          <div class="posts-list">
            <el-card v-for="post in myPosts" :key="post.id" class="post-card">
              <h3 @click="$router.push(`/community/post/${post.id}`)">
                {{ post.title }}
              </h3>
              <p>{{ post.content }}</p>
              <div class="post-meta">
                <span>{{ fromNow(post.createdAt) }}</span>
                <span>
                  <el-icon><Star /></el-icon>
                  {{ post.likes }}
                </span>
                <span>
                  <el-icon><ChatLineRound /></el-icon>
                  {{ post.comments }}
                </span>
              </div>
            </el-card>
          </div>
          <EmptyState v-if="myPosts.length === 0" :text="$t('common.noData')" />
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
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { getFavorites } from '@/api/culture'
import { getCommunityPosts } from '@/api/community'
import { changePassword } from '@/api/user'
import type { CultureResource } from '@/types/culture'
import type { CommunityPost } from '@/types/community'
import CultureCard from '@/components/common/CultureCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { fromNow } from '@/utils'
import { Star, ChatLineRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('info')
const favorites = ref<CultureResource[]>([])
const favoritesLoading = ref(false)
const myPosts = ref<CommunityPost[]>([])
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
  try {
    const response = await getCommunityPosts({ page: 1, size: 20 })
    myPosts.value = response.list
  } catch (error) {
    console.error('Failed to load posts:', error)
  }
}

onMounted(() => {
  if (activeTab.value === 'favorites') {
    loadFavorites()
  } else if (activeTab.value === 'posts') {
    loadMyPosts()
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

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: translateX(8px);
  }

  h3 {
    font-size: 18px;
    margin-bottom: 12px;
    color: #303133;
  }

  p {
    color: #606266;
    line-height: 1.6;
    margin-bottom: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
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
