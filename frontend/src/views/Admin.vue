<template>
  <div class="admin-page">
    <div class="container">
      <h1 class="page-title">管理员后台</h1>

      <el-tabs v-model="activeTab" class="admin-tabs">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <UserManagement />
        </el-tab-pane>

        <!-- 轮播图管理 -->
        <el-tab-pane label="轮播图管理" name="carousels">
          <CarouselManagement />
        </el-tab-pane>

        <!-- 文化资源管理 -->
        <el-tab-pane label="文化资源管理" name="culture">
          <CultureManagement />
        </el-tab-pane>

        <!-- 活动管理 -->
        <el-tab-pane label="活动管理" name="events">
          <EventManagement />
        </el-tab-pane>

        <!-- 社区投稿审核 -->
        <el-tab-pane label="社区投稿审核" name="posts">
          <PostManagement />
        </el-tab-pane>

        <!-- 首页推荐配置 -->
        <el-tab-pane label="首页推荐配置" name="recommendations">
          <HomeRecommendationManagement />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import UserManagement from '@/components/admin/UserManagement.vue'
import CarouselManagement from '@/components/admin/CarouselManagement.vue'
import CultureManagement from '@/components/admin/CultureManagement.vue'
import EventManagement from '@/components/admin/EventManagement.vue'
import PostManagement from '@/components/admin/PostManagement.vue'
import HomeRecommendationManagement from '@/components/admin/HomeRecommendationManagement.vue'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('users')

// 检查是否为管理员
// 后端返回的 role 是大写 'ADMIN' 或 'USER'
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role?.toUpperCase()
  return role === 'ADMIN'
})

onMounted(() => {
  if (!isAdmin.value) {
    ElMessage.error('无权限访问')
    router.push('/home')
  }
})
</script>

<style lang="scss" scoped>
.admin-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #303133;
}

.admin-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }

  :deep(.el-tabs__item) {
    font-size: 16px;
    padding: 0 24px;
  }
}
</style>
