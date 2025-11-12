<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon v-if="!sidebarCollapsed" class="logo-icon">
            <Setting />
          </el-icon>
          <span v-if="!sidebarCollapsed" class="logo-text">管理员后台</span>
        </div>
        <el-button text class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </el-button>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="sidebarCollapsed"
        router
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <template #title> 用户管理 </template>
        </el-menu-item>
        <el-menu-item index="/admin/carousels">
          <el-icon><Picture /></el-icon>
          <template #title> 轮播图管理 </template>
        </el-menu-item>
        <el-menu-item index="/admin/culture">
          <el-icon><Document /></el-icon>
          <template #title> 文化资源管理 </template>
        </el-menu-item>
        <el-menu-item index="/admin/events">
          <el-icon><Calendar /></el-icon>
          <template #title> 活动管理 </template>
        </el-menu-item>
        <el-menu-item index="/admin/posts">
          <el-icon><ChatLineRound /></el-icon>
          <template #title> 社区投稿审核 </template>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }"> 管理员后台 </el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/admin'">
              {{ currentPageTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><HomeFilled /></el-icon>
                  返回前台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  Setting,
  Fold,
  Expand,
  User,
  Picture,
  Document,
  Calendar,
  ChatLineRound,
  ArrowDown,
  HomeFilled,
  SwitchButton,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onBeforeMount } from 'vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const sidebarCollapsed = ref(false)

const activeMenu = computed(() => route.path)

const pageTitleMap: Record<string, string> = {
  '/admin': '首页',
  '/admin/users': '用户管理',
  '/admin/carousels': '轮播图管理',
  '/admin/culture': '文化资源管理',
  '/admin/events': '活动管理',
  '/admin/posts': '社区投稿审核',
}

const currentPageTitle = computed(() => {
  return pageTitleMap[route.path] || route.meta.title || '管理员后台'
})

const handleCommand = (command: string) => {
  if (command === 'home') {
    router.push('/home')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

// 检查管理员权限
onBeforeMount(() => {
  const role = userStore.userInfo?.role?.toUpperCase()
  if (!userStore.isLoggedIn || role !== 'ADMIN') {
    ElMessage.error('无权限访问')
    router.push('/home')
  }
})
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-sidebar {
  width: 240px;
  background: #304156;
  transition: width 0.3s;
  flex-shrink: 0;

  &.collapsed {
    width: 64px;
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 60px;
    padding: 0 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #fff;
      font-size: 18px;
      font-weight: 600;

      .logo-icon {
        font-size: 24px;
      }
    }

    .collapse-btn {
      color: #fff;
      font-size: 18px;
    }
  }

  .sidebar-menu {
    border: none;
    background: #304156;

    :deep(.el-menu-item) {
      color: rgba(255, 255, 255, 0.7);
      border-left: 3px solid transparent;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
      }

      &.is-active {
        background: rgba(64, 158, 255, 0.2);
        color: #409eff;
        border-left-color: #409eff;
      }

      .el-icon {
        color: inherit;
      }
    }
  }
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .header-left {
    :deep(.el-breadcrumb) {
      font-size: 14px;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 4px;
      transition: background 0.3s;

      &:hover {
        background: #f5f7fa;
      }

      .username {
        font-size: 14px;
        color: #303133;
      }
    }
  }
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
