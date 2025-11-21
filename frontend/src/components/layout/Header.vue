<template>
  <header class="header" :class="{ scrolled: isScrolled }">
    <div class="container header-content">
      <div class="logo" @click="$router.push('/home')">
        <el-icon :size="32">
          <LocationFilled />
        </el-icon>
        <span class="logo-text">{{ $t('app.name') }}</span>
      </div>

      <!-- 移动端菜单按钮 -->
      <el-button
        v-if="isMobile"
        class="mobile-menu-btn"
        text
        @click="toggleMobileMenu"
      >
        <el-icon :size="24"><Menu /></el-icon>
      </el-button>

      <nav class="nav-menu" :class="{ 'mobile-menu': isMobile, 'mobile-menu-open': mobileMenuVisible }">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="active"
          @click="closeMobileMenu"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="header-actions">
        <el-input
          v-if="!isMobile"
          v-model="searchKeyword"
          :placeholder="$t('common.search')"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          v-else
          class="search-icon-btn"
          text
          :aria-label="$t('common.search')"
          @click="goToSearchPage"
        >
          <el-icon><Search /></el-icon>
        </el-button>

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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
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
  Calendar,
  ChatLineRound,
  Menu,
  CollectionTag,
  Guide,
} from '@element-plus/icons-vue'

const router = useRouter()
const { locale, t } = useI18n()
const userStore = useUserStore()

const isScrolled = ref(false)
const searchKeyword = ref('')
const mobileMenuVisible = ref(false)
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1920)

const currentLocale = computed(() => locale.value)

// 响应式检测
const isMobile = computed(() => windowWidth.value <= 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
  if (windowWidth.value > 768) {
    mobileMenuVisible.value = false
  }
}

const baseMenuItems = [
  { path: '/home', labelKey: 'nav.home', icon: 'HomeFilled' },
  { path: '/map', labelKey: 'nav.map', icon: 'MapLocation' },
  { path: '/heritage', labelKey: '', fallbackLabel: '非遗专题', icon: 'CollectionTag' },
  { path: '/routes', labelKey: 'nav.routes', icon: 'Guide' },
  { path: '/events', labelKey: 'nav.events', icon: 'Calendar' },
  { path: '/community', labelKey: 'nav.community', icon: 'ChatLineRound' },
] as const

const menuItems = computed(() =>
  baseMenuItems.map(item => ({
    path: item.path,
    icon: item.icon,
    label: item.labelKey ? t(item.labelKey) : item.fallbackLabel,
  })),
)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { q: searchKeyword.value } })
  }
}

const goToSearchPage = () => {
  router.push({ name: 'Search' })
}

const handleCommand = (command: string) => {
  if (command !== locale.value) {
    locale.value = command
    localStorage.setItem('locale', command)
  }
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

const toggleMobileMenu = () => {
  if (isMobile.value) {
    mobileMenuVisible.value = !mobileMenuVisible.value
  }
}

const closeMobileMenu = () => {
  if (isMobile.value) {
    mobileMenuVisible.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('resize', handleResize)
  handleResize()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: background 0.3s ease, box-shadow 0.3s ease;

  &::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 2px;
    background: linear-gradient(135deg, #0073e6 0%, #e6b000 50%, #e60000 100%);
    opacity: 0.85;
    pointer-events: none;
  }

  &.scrolled {
    box-shadow: 0 6px 24px rgba(0, 115, 230, 0.15);
    background: rgba(255, 255, 255, 0.98);
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
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #0073e6;
  font-weight: bold;
  font-size: 20px;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.05);
  }

  .logo-text {
    background: linear-gradient(135deg, #0073e6 0%, #e6b000 50%, #e60000 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    white-space: nowrap;
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
    flex-shrink: 0;
    white-space: nowrap;
    padding: 8px 16px;
    border-radius: 8px;
    color: #606266;
    text-decoration: none;
    transition: all 0.3s;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(0, 115, 230, 0.1), transparent);
      transition: left 0.5s;
    }

    &:hover {
      background: linear-gradient(135deg, rgba(0, 115, 230, 0.1) 0%, rgba(230, 176, 0, 0.1) 100%);
      color: #0073e6;
      transform: translateY(-2px);

      &::before {
        left: 100%;
      }
    }

    &.active {
      background: linear-gradient(135deg, rgba(0, 115, 230, 0.15) 0%, rgba(230, 176, 0, 0.15) 100%);
      color: #0073e6;
      font-weight: 600;
      box-shadow: 0 2px 8px rgba(0, 115, 230, 0.2);
    }
  }

  // 移动端菜单样式
  &.mobile-menu {
    position: fixed;
    top: 70px;
    left: 0;
    right: 0;
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(12px);
    flex-direction: column;
    padding: 16px 20px;
    gap: 8px;
    transform: translateY(-12px);
    opacity: 0;
    pointer-events: none;
    transition: transform 0.25s ease, opacity 0.25s ease;
    box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);

    &.mobile-menu-open {
      transform: translateY(0);
      opacity: 1;
      pointer-events: auto;
    }

    .nav-item {
      width: 100%;
      justify-content: flex-start;
      padding: 12px 16px;
      font-size: 16px;
    }
  }
}

.mobile-menu-btn {
  display: none;
  color: #0073e6;
  font-size: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 200px;
}

.search-icon-btn {
  color: #606266;
  padding: 0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
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

@media (max-width: 1100px) {
  .header-content {
    flex-wrap: wrap;
    height: auto;
    padding: 10px 0;
    row-gap: 10px;
  }

  .nav-menu {
    order: 3;
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 8px;

    .nav-item {
      padding: 6px 12px;
      font-size: 14px;
    }
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
    flex-wrap: wrap;
  }

  .search-input {
    width: 160px;
  }
}

@media (max-width: 768px) {
  .nav-menu:not(.mobile-menu) {
    display: none;
  }

  .mobile-menu-btn {
    display: block;
  }

  .search-input {
    width: 120px;
    font-size: 14px;
  }

  .logo {
    font-size: 18px;

    .logo-text {
      display: none;
    }
  }

  .header-content {
    height: 60px;
    gap: 12px;
  }

  .header-actions {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .search-input {
    width: 100px;
    font-size: 12px;
  }

  .logo {
    font-size: 16px;
  }
}
</style>
