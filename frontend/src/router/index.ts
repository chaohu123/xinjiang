import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', transition: 'fade' },
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue'),
        meta: { title: '搜索', transition: 'fade' },
      },
      {
        path: 'detail/:type/:id',
        name: 'Detail',
        component: () => import('@/views/Detail.vue'),
        meta: { title: '详情', transition: 'fade' },
      },
      {
        path: 'map',
        name: 'Map',
        component: () => import('@/views/Map.vue'),
        meta: { title: '地图', transition: 'fade' },
      },
      {
        path: 'routes',
        name: 'Routes',
        component: () => import('@/views/Routes.vue'),
        meta: { title: '路线推荐', transition: 'fade' },
      },
      {
        path: 'route/:id',
        name: 'RouteDetail',
        component: () => import('@/views/RouteDetail.vue'),
        meta: { title: '路线详情', transition: 'fade' },
      },
      {
        path: 'events',
        name: 'Events',
        component: () => import('@/views/Events.vue'),
        meta: { title: '活动日历', transition: 'fade' },
      },
      {
        path: 'event/:id',
        name: 'EventDetail',
        component: () => import('@/views/EventDetail.vue'),
        meta: { title: '活动详情', transition: 'fade' },
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/Community.vue'),
        meta: { title: '社区', transition: 'fade' },
      },
      {
        path: 'community/post/:id',
        name: 'CommunityPost',
        component: () => import('@/views/CommunityPost.vue'),
        meta: { title: '社区投稿', transition: 'fade' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true, transition: 'fade' },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/users',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: '管理员首页' },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'carousels',
        name: 'AdminCarousels',
        component: () => import('@/views/admin/AdminCarousels.vue'),
        meta: { title: '轮播图管理' },
      },
      {
        path: 'culture',
        name: 'AdminCulture',
        component: () => import('@/views/admin/AdminCulture.vue'),
        meta: { title: '文化资源管理' },
      },
      {
        path: 'events',
        name: 'AdminEvents',
        component: () => import('@/views/admin/AdminEvents.vue'),
        meta: { title: '活动管理' },
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('@/views/admin/AdminPosts.vue'),
        meta: { title: '社区投稿审核' },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', transition: 'slide' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', transition: 'slide' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()

  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 检查是否需要管理员权限
  // 后端返回的 role 是大写 'ADMIN' 或 'USER'
  if (to.meta.requiresAdmin) {
    const role = userStore.userInfo?.role?.toUpperCase()
    if (!userStore.isLoggedIn || role !== 'ADMIN') {
      ElMessage.error('无权限访问')
      next({ name: 'Home' })
      return
    }
  }

  // 已登录用户访问登录/注册页，重定向到首页
  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
