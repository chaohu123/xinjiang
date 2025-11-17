<template>
  <div class="profile-page">
    <ScenicHero
      :display-name="displayName"
      :bio="userInfo?.bio ?? ''"
      :avatar="userInfo?.avatar"
      :initials="initials"
      :membership-tier="membershipTier"
      :join-date="joinDate"
      :email="userInfo?.email ?? ''"
      :days-active="daysSinceJoin"
      :stats="highlightStats"
      :completion="profileCompletion"
      :badges="heroBadges"
      :cover="heroCover"
      :avatar-uploading="avatarUploading"
      :before-upload="beforeAvatarUpload"
      :upload-request="handleAvatarUpload"
      :refresh-loading="pageLoading"
      @edit="openEditProfile"
      @refresh="refreshAll"
    />

    <section class="profile-enhance-grid">
      <GalleryCarousel
        :title="t('profile.galleryTitle')"
        :subtitle="t('profile.gallerySubtitle')"
        :items="galleryItems"
        :loading="loading.favorites"
        :empty-text="t('profile.noGallery')"
      >
        <template #action>
          <el-button text type="primary" @click="router.push({ name: 'Community' })">
            {{ t('profile.galleryViewAll') }}
          </el-button>
        </template>
      </GalleryCarousel>

      <MiniEventsCalendar
        :title="t('profile.calendarTitle')"
        :subtitle="t('profile.calendarSubtitle')"
        :action-label="t('profile.calendarViewAll')"
        :events="calendarPreview"
        :empty-text="t('profile.noEvents')"
        :loading="loading.events"
        @view-all="router.push({ name: 'Events' })"
      />
    </section>

    <div class="quick-cards">
      <el-card
        v-for="quick in quickActions"
        :key="quick.key"
        class="quick-card"
        shadow="hover"
        @click="quick.action()"
      >
        <div class="quick-card-inner">
          <div>
            <p class="quick-title">{{ quick.title }}</p>
            <p class="quick-desc">{{ quick.desc }}</p>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>

    <el-card class="content-card" shadow="never">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane :label="t('profile.activityTab')" name="activity">
          <div class="activity-grid">
            <div class="panel">
              <div class="panel-head">
                <h3>{{ t('profile.recentPosts') }}</h3>
                <el-button link @click="router.push({ name: 'Community' })">{{
                  t('profile.viewAll')
                }}</el-button>
              </div>
              <el-skeleton :loading="loading.posts" animated>
                <div v-if="posts.list.length" class="list">
                  <div
                    v-for="post in posts.list"
                    :key="post.id"
                    class="list-item"
                    @click="router.push(`/community/post/${post.id}`)"
                  >
                    <div class="item-head">
                      <p class="item-title">{{ post.title }}</p>
                      <span class="item-time">{{ formatRelative(post.createdAt) }}</span>
                    </div>
                    <p class="item-meta">
                      {{ t('profile.postMeta', { likes: post.likes, comments: post.comments }) }}
                    </p>
                  </div>
                </div>
                <el-empty v-else :description="t('profile.noPosts')" />
              </el-skeleton>
            </div>

            <div class="panel">
              <div class="panel-head">
                <h3>{{ t('profile.registeredEvents') }}</h3>
                <el-button link @click="router.push({ name: 'Events' })">{{
                  t('profile.manage')
                }}</el-button>
              </div>
              <el-skeleton :loading="loading.events" animated>
                <div v-if="events.list.length" class="timeline">
                  <div v-for="event in events.list" :key="event.id" class="timeline-item">
                    <div>
                      <p class="item-title">{{ event.title }}</p>
                      <p class="item-meta">{{ buildEventRange(event) }}</p>
                    </div>
                    <el-tag size="small" :type="statusType(event.status)">
                      {{ statusLabel(event.status) }}
                    </el-tag>
                  </div>
                </div>
                <el-empty v-else :description="t('profile.noEvents')" />
              </el-skeleton>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="t('profile.favoritesTab')" name="favorites">
          <div class="favorites-toolbar">
            <el-radio-group v-model="favoriteFilter" size="small">
              <el-radio-button
                v-for="option in favoriteFilterOptions"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-radio-button>
            </el-radio-group>
            <span class="favorites-count">
              {{ t('profile.favoritesCount', { count: filteredFavorites.length }) }}
            </span>
          </div>
          <el-skeleton :loading="loading.favorites" animated>
            <div v-if="filteredFavorites.length" class="favorites-grid">
              <el-card
                v-for="fav in filteredFavorites"
                :key="fav.id"
                class="favorite-card"
                shadow="never"
              >
                <div class="favorite-thumb" :style="{ backgroundImage: `url(${fav.cover})` }" />
                <div class="favorite-body">
                  <p class="fav-title">{{ fav.title }}</p>
                  <p class="fav-meta">
                    {{ fav.region }} · {{ favoriteTypeLabel(fav.type) }}
                  </p>
                  <el-button text type="primary" @click="goToResource(fav)">
                    {{ t('profile.viewDetail') }}
                  </el-button>
                </div>
              </el-card>
            </div>
            <el-empty v-else :description="t('profile.noFavorites')" />
          </el-skeleton>
        </el-tab-pane>

        <el-tab-pane :label="t('profile.settingsTab')" name="settings">
          <div class="settings-grid">
            <el-form label-position="top" :model="editForm" class="settings-form">
              <el-form-item :label="t('profile.nickname')">
                <el-input v-model="editForm.nickname" />
              </el-form-item>
              <el-form-item :label="t('profile.bio')">
                <el-input v-model="editForm.bio" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item :label="t('profile.phone')">
                <el-input v-model="editForm.phone" />
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" :loading="loading.save" @click="saveProfile">
                  {{ t('profile.saveChanges') }}
                </el-button>
                <el-button @click="resetEditForm">{{ t('profile.cancel') }}</el-button>
              </div>
            </el-form>

            <div class="safety-card">
              <h4>{{ t('profile.accountSecurity') }}</h4>
              <p>{{ t('profile.securityHint') }}</p>
              <el-button text type="primary" @click="goChangePassword">
                {{ t('profile.changePassword') }}
              </el-button>
              <el-divider />
              <h4>{{ t('profile.deleteAccount') }}</h4>
              <p>{{ t('profile.deleteHint') }}</p>
              <el-button type="danger" plain @click="confirmDelete">
                {{ t('profile.delete') }}
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="editDialog" width="520px">
      <template #header>
        <span>{{ t('profile.editProfile') }}</span>
      </template>
      <el-form label-position="top" :model="editForm">
        <el-form-item :label="t('profile.nickname')">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item :label="t('profile.bio')">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="t('profile.phone')">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">{{ t('profile.cancel') }}</el-button>
        <el-button type="primary" :loading="loading.save" @click="saveProfile">
          {{ t('profile.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  ArrowRight,
} from '@element-plus/icons-vue'
import {
  ElMessage,
  ElMessageBox,
  type UploadProps,
  type UploadRequestOptions,
} from 'element-plus'
import { useUserStore } from '@/store/user'
import { getMyPosts } from '@/api/community'
import { getFavorites } from '@/api/culture'
import { getMyRegisteredEvents } from '@/api/event'
import type { CommunityPost } from '@/types/community'
import type { CultureResource } from '@/types/culture'
import type { Event } from '@/types/event'
import request from '@/utils/axios'
import ScenicHero from '@/components/profile/ScenicHero.vue'
import GalleryCarousel, { type GalleryItem } from '@/components/profile/GalleryCarousel.vue'
import MiniEventsCalendar, { type CalendarPreview } from '@/components/profile/MiniEventsCalendar.vue'
import type { UserBadge } from '@/types/user'

dayjs.extend(relativeTime)

const router = useRouter()
const { t, locale } = useI18n()
const userStore = useUserStore()

const activeTab = ref('activity')
const editDialog = ref(false)
const avatarUploading = ref(false)
const pageLoading = ref(false)

const posts = reactive<{ list: CommunityPost[]; total: number }>({ list: [], total: 0 })
const favorites = reactive<{ list: CultureResource[]; total: number }>({ list: [], total: 0 })
const favoriteFilter = ref<'all' | 'article' | 'exhibit' | 'video' | 'audio'>('all')
const events = reactive<{ list: Event[]; total: number }>({ list: [], total: 0 })

const loading = reactive({
  posts: false,
  favorites: false,
  events: false,
  save: false,
})

const editForm = reactive({
  nickname: '',
  bio: '',
  phone: '',
})

const userInfo = computed(() => userStore.userInfo)

const displayName = computed(() => {
  return (
    userInfo.value?.nickname ||
    userInfo.value?.username ||
    t('profile.anonymous')
  )
})

const initials = computed(() => {
  if (!displayName.value) return ''
  return displayName.value.slice(0, 2).toUpperCase()
})

const joinDate = computed(() => {
  if (!userInfo.value?.createdAt) return '--'
  return dayjs(userInfo.value.createdAt).format('YYYY.MM.DD')
})

const daysSinceJoin = computed(() => {
  if (!userInfo.value?.createdAt) return 0
  return Math.max(dayjs().diff(dayjs(userInfo.value.createdAt), 'day'), 0)
})

const profileCompletion = computed(() => {
  const fields = ['avatar', 'nickname', 'bio', 'phone'] as const
  const filled = fields.reduce((count, field) => {
    return count + (userInfo.value?.[field] ? 1 : 0)
  }, 0)
  return Math.round((filled / fields.length) * 100)
})

const highlightStats = computed(() => [
  { key: 'posts', label: t('profile.statPosts'), value: posts.total },
  { key: 'favorites', label: t('profile.statFavorites'), value: favorites.total },
  { key: 'events', label: t('profile.statEvents'), value: events.total },
])

const membershipTier = computed(() => {
  const score = posts.total * 2 + favorites.total + events.total * 3
  if (score >= 30) return t('profile.tierGuardian')
  if (score >= 12) return t('profile.tierExplorer')
  return t('profile.tierNewcomer')
})

const heroCover = computed(() => {
  if (userInfo.value?.cover) return userInfo.value.cover
  if (userInfo.value?.gallery?.length) return userInfo.value.gallery[0] ?? ''
  if (favorites.list.length) return favorites.list[0].cover
  if (events.list.length) return events.list[0].cover
  return ''
})

const heroBadges = computed<UserBadge[]>(() => {
  if (userInfo.value?.badges?.length) {
    return userInfo.value.badges
  }
  return [
    { id: 'story', label: t('profile.badgeStoryteller') },
    { id: 'explorer', label: t('profile.badgeExplorer') },
    { id: 'guardian', label: t('profile.badgeCultureKeeper') },
  ]
})

const quickActions = computed(() => [
  {
    key: 'community',
    title: t('profile.quickPost'),
    desc: t('profile.quickPostDesc'),
    action: () => router.push({ name: 'Community' }),
  },
  {
    key: 'events',
    title: t('profile.quickEvent'),
    desc: t('profile.quickEventDesc'),
    action: () => router.push({ name: 'Events' }),
  },
  {
    key: 'routes',
    title: t('profile.quickRoute'),
    desc: t('profile.quickRouteDesc'),
    action: () => router.push({ name: 'Routes' }),
  },
  {
    key: 'search',
    title: t('profile.quickExplore'),
    desc: t('profile.quickExploreDesc'),
    action: () => router.push({ name: 'Search' }),
  },
])

const formatRelative = (date?: string) => {
  if (!date) return '--'
  const currLocale = locale.value === 'zh' ? 'zh-cn' : 'en'
  dayjs.locale(currLocale)
  return dayjs(date).fromNow()
}

const statusLabel = (status?: string) => {
  if (!status) return '--'
  const normalized = status.toLowerCase()
  return (
    {
      upcoming: t('profile.eventUpcoming'),
      ongoing: t('profile.eventOngoing'),
      past: t('profile.eventPast'),
    }[normalized] || status
  )
}

const statusType = (status?: string) => {
  const normalized = status?.toLowerCase()
  return (
    {
      upcoming: 'info',
      ongoing: 'success',
      past: 'warning',
    }[normalized ?? ''] || 'info'
  )
}

const buildEventRange = (event: Event) => {
  const start = dayjs(event.startDate).format('MM.DD')
  const end = dayjs(event.endDate ?? event.startDate).format('MM.DD')
  return `${start} - ${end}`
}

const favoriteTypeLabel = (type: string) => {
  const normalized = (type || '').toLowerCase()
  return (
    {
      article: t('profile.resourceArticle'),
      exhibit: t('profile.resourceExhibit'),
      video: t('profile.resourceVideo'),
      audio: t('profile.resourceAudio'),
    }[normalized as 'article' | 'exhibit' | 'video' | 'audio'] || type
  )
}

const favoriteFilterOptions = computed(() => [
  { label: t('profile.favoriteFilterAll'), value: 'all' },
  { label: t('profile.favoriteFilterArticle'), value: 'article' },
  { label: t('profile.favoriteFilterExhibit'), value: 'exhibit' },
  { label: t('profile.favoriteFilterVideo'), value: 'video' },
  { label: t('profile.favoriteFilterAudio'), value: 'audio' },
])

const filteredFavorites = computed(() => {
  if (favoriteFilter.value === 'all') {
    return favorites.list
  }
  return favorites.list.filter(
    item => (item.type || '').toLowerCase() === favoriteFilter.value,
  )
})

const galleryItems = computed<GalleryItem[]>(() => {
  const gallery = userInfo.value?.gallery ?? []
  if (gallery.length) {
    return gallery.map((url, index) => ({
      id: `gallery-${index}`,
      title: t('profile.galleryItemLabel', { index: index + 1 }),
      cover: url,
      description: userInfo.value?.bio ?? '',
    }))
  }
  return favorites.list.slice(0, 6).map(item => ({
    id: `favorite-${item.id}`,
    title: item.title,
    cover: item.cover,
    description: `${item.region} · ${favoriteTypeLabel(item.type)}`,
  }))
})

const calendarPreview = computed<CalendarPreview[]>(() => {
  const baseEvents: Event[] = events.list.length
    ? events.list
    : userInfo.value?.registeredEvents ?? []
  return baseEvents.slice(0, 4).map(event => ({
    id: event.id,
    title: event.title,
    dateRange: buildEventRange(event),
    statusLabel: statusLabel(event.status),
    statusType: statusType(event.status),
    location: event.location?.name ?? event.location?.address ?? '',
  }))
})

const beforeAvatarUpload: UploadProps['beforeUpload'] = file => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error(t('profile.avatarImageOnly'))
  }
  if (!isLt2M) {
    ElMessage.error(t('profile.avatarSizeLimit'))
  }
  return isImage && isLt2M
}

const handleAvatarUpload = async ({ file }: UploadRequestOptions) => {
  if (!(file instanceof File)) return
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const result = await request.post<{ url: string }>(
      '/admin/culture/upload',
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      },
    )
    if (result?.url) {
      await userStore.updateUser({ avatar: result.url })
      ElMessage.success(t('profile.avatarUpdated'))
    }
  } finally {
    avatarUploading.value = false
  }
}

const saveProfile = async () => {
  loading.save = true
  try {
    await userStore.updateUser({
      nickname: editForm.nickname,
      bio: editForm.bio,
      phone: editForm.phone,
    })
    ElMessage.success(t('profile.saved'))
    editDialog.value = false
  } finally {
    loading.save = false
  }
}

const resetEditForm = () => {
  if (!userInfo.value) return
  editForm.nickname = userInfo.value.nickname ?? ''
  editForm.bio = userInfo.value.bio ?? ''
  editForm.phone = userInfo.value.phone ?? ''
}

const openEditProfile = () => {
  editDialog.value = true
}

const confirmDelete = () => {
  ElMessageBox.confirm(t('profile.deleteWarning'), t('profile.deleteAccount'), {
    confirmButtonText: t('profile.delete'),
    cancelButtonText: t('profile.cancel'),
    type: 'warning',
  })
    .then(() => {
      ElMessage.info(t('profile.pendingFeature'))
    })
    .catch(() => {})
}

const goChangePassword = () => {
  router.push({ name: 'Profile', query: { tab: 'settings' } })
  ElMessage.info(t('profile.pendingFeature'))
}

const goToResource = (resource: CultureResource) => {
  router.push(`/detail/${resource.type}/${resource.id}`)
}

const refreshAll = async () => {
  pageLoading.value = true
  await Promise.all([loadPosts(), loadFavorites(), loadEvents()])
  pageLoading.value = false
}

const loadPosts = async () => {
  loading.posts = true
  try {
    const res = await getMyPosts({ page: 1, size: 5 })
    posts.list = res.list ?? []
    posts.total = res.total ?? posts.list.length
  } finally {
    loading.posts = false
  }
}

const loadFavorites = async () => {
  loading.favorites = true
  try {
    const res = await getFavorites({ page: 1, size: 8 })
    favorites.list = res.list ?? []
    favorites.total = res.total ?? favorites.list.length
  } finally {
    loading.favorites = false
  }
}

const loadEvents = async () => {
  loading.events = true
  try {
    const res = await getMyRegisteredEvents({ page: 1, size: 6 })
    events.list = res.list ?? []
    events.total = res.total ?? events.list.length
  } finally {
    loading.events = false
  }
}

watch(
  () => userInfo.value,
  info => {
    if (info) {
      resetEditForm()
    }
  },
  { immediate: true },
)

onMounted(async () => {
  if (!userInfo.value) {
    await userStore.initUser()
  }
  await refreshAll()
})
</script>

<style scoped>
:deep(.el-card__body) {
  padding: 0;
}

.profile-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  background: var(--bg-page);
}

.profile-enhance-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.quick-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
}

.quick-card {
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: var(--transition-base);
  background: var(--bg-paper);
  box-shadow: var(--card-shadow);
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-shadow-hover);
}

.quick-card-inner {
  padding: 20px 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.quick-desc {
  color: var(--text-muted);
  margin-top: 6px;
  font-size: 13px;
}

.content-card {
  border-radius: var(--radius-lg);
  box-shadow: var(--card-shadow);
}

.content-card :deep(.el-card__body) {
  padding: 28px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-top: 16px;
}

.panel {
  padding: 24px;
  border-radius: var(--radius-lg);
  background: #f3f6fb;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.6);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.list,
.timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-item,
.timeline-item {
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--bg-paper);
  box-shadow: 0 12px 28px rgba(15, 124, 143, 0.08);
  transition: var(--transition-base);
}

.list-item:hover {
  transform: translateX(6px);
  box-shadow: var(--card-shadow-hover);
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-title {
  font-weight: 600;
  color: var(--text-primary);
}

.item-time,
.item-meta {
  color: var(--text-muted);
  font-size: 13px;
}

.favorites-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}

.favorites-count {
  font-size: 13px;
  color: var(--text-muted);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.favorite-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--card-shadow);
}

.favorite-thumb {
  height: 160px;
  background-size: cover;
  background-position: center;
}

.favorite-body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fav-title {
  font-weight: 600;
  color: var(--text-primary);
}

.fav-meta {
  color: var(--text-muted);
  font-size: 13px;
}

.settings-grid {
  display: grid;
  grid-template-columns: minmax(0, 3fr) minmax(0, 2fr);
  gap: 24px;
  margin-top: 16px;
}

.settings-form,
.safety-card {
  border-radius: var(--radius-lg);
  padding: 24px;
  background: var(--bg-paper);
  box-shadow: var(--card-shadow);
}

.form-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.safety-card h4 {
  margin-bottom: 8px;
  color: var(--text-primary);
}

.safety-card p {
  margin-bottom: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

@media (max-width: 1200px) {
  .profile-enhance-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .profile-page {
    padding: 16px;
  }

  .content-card :deep(.el-card__body),
  .panel,
  .settings-form,
  .safety-card {
    padding: 20px;
  }

  .favorites-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .quick-cards {
    grid-template-columns: 1fr;
  }

  .favorites-grid {
    grid-template-columns: 1fr;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>

