<template>
  <header class="header" :class="{ scrolled: isScrolled }">
    <div class="container header-content">
      <div class="logo" @click="$router.push('/home')">
        <el-icon :size="32">
          <LocationFilled />
        </el-icon>
        <span class="logo-text">{{ $t('app.name') }}</span>
      </div>

      <nav class="nav-menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('common.search')"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-dropdown @command="handleCommand">
          <el-button link class="lang-btn">
            <el-icon><Operation /></el-icon>
            {{ currentLocale === 'zh' ? '中文' : 'English' }}
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh" :disabled="currentLocale === 'zh'">
                中文
              </el-dropdown-item>
              <el-dropdown-item command="en" :disabled="currentLocale === 'en'">
                English
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown v-if="userStore.isLoggedIn" @command="handleUserCommand">
          <div class="user-avatar">
            <el-avatar :size="36" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.username?.[0] }}
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                {{ $t('common.profile') }}
              </el-dropdown-item>
              <el-dropdown-item
                v-if="userStore.userInfo?.role?.toUpperCase() === 'ADMIN'"
                command="admin"
                divided
              >
                <el-icon><Setting /></el-icon>
                管理员
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                {{ $t('common.logout') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button v-else type="primary" @click="$router.push('/login')">
          {{ $t('common.login') }}
        </el-button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/store/user'
import {
  LocationFilled,
  Search,
  Operation,
  User,
  SwitchButton,
  Setting,
  HomeFilled,
  MapLocation,
  Route,
  Calendar,
  ChatLineRound,
} from '@element-plus/icons-vue'

const router = useRouter()
const { locale, t } = useI18n()
const userStore = useUserStore()

const isScrolled = ref(false)
const searchKeyword = ref('')

const currentLocale = computed(() => locale.value)

const menuItems = computed(() => [
  { path: '/home', label: t('nav.home'), icon: 'HomeFilled' },
  { path: '/map', label: t('nav.map'), icon: 'MapLocation' },
  { path: '/routes', label: t('nav.routes'), icon: 'Route' },
  { path: '/events', label: t('nav.events'), icon: 'Calendar' },
  { path: '/community', label: t('nav.community'), icon: 'ChatLineRound' },
])

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { q: searchKeyword.value } })
  }
}

const handleCommand = (command: string) => {
  locale.value = command
  localStorage.setItem('locale', command)
}

const handleUserCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/home')
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e4e7ed;
  transition: all 0.3s;

  &.scrolled {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #409eff;
  font-weight: bold;
  font-size: 20px;

  .logo-text {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.nav-menu {
  display: flex;
  gap: 10px;
  flex: 1;
  justify-content: center;

  .nav-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 6px;
    color: #606266;
    text-decoration: none;
    transition: all 0.3s;

    &:hover {
      background: #f5f7fa;
      color: #409eff;
    }

    &.active {
      background: #ecf5ff;
      color: #409eff;
      font-weight: 500;
    }
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 200px;
}

.lang-btn {
  color: #606266;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.1);
  }
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }

  .search-input {
    width: 120px;
  }
}
</style>
