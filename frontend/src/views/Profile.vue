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
                  >
                    <div class="item-head">
                      <p class="item-title" @click="router.push(`/community/post/${post.id}`)">{{ post.title }}</p>
                      <div class="item-head-right">
                        <el-tag
                          v-if="post.status"
                          size="small"
                          :type="getPostStatusType(post.status)"
                          style="margin-right: 8px"
                        >
                          {{ getPostStatusLabel(post.status) }}
                        </el-tag>
                        <span class="item-time">{{ formatRelative(post.createdAt) }}</span>
                      </div>
                    </div>
                    <p class="item-meta">
                      {{ t('profile.postMeta', { likes: post.likes, comments: post.comments }) }}
                    </p>
                    <div v-if="post.status === 'rejected' && post.rejectReason" class="reject-reason-box">
                      <el-alert
                        :title="`拒绝原因：${post.rejectReason}`"
                        type="error"
                        :closable="false"
                        show-icon
                      />
                    </div>
                    <div v-if="post.status === 'rejected'" class="item-actions">
                      <el-button
                        text
                        type="primary"
                        size="small"
                        :icon="Edit"
                        @click.stop="handleEditPost(post)"
                      >
                        重新编辑
                      </el-button>
                    </div>
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
                    <div class="timeline-info">
                      <div>
                        <p class="item-title">{{ event.title }}</p>
                        <p class="item-meta">{{ buildEventRange(event) }}</p>
                      </div>
                      <div class="timeline-actions">
                        <el-tag
                          v-if="event.registrationStatus"
                          size="small"
                          :type="getRegistrationStatusType(event.registrationStatus)"
                          style="margin-right: 8px"
                        >
                          {{ getRegistrationStatusLabel(event.registrationStatus) }}
                        </el-tag>
                        <el-tag size="small" :type="statusType(event.status)">
                          {{ statusLabel(event.status) }}
                        </el-tag>
                        <el-button
                          v-if="canCancelEvent(event)"
                          text
                          type="danger"
                          size="small"
                          :loading="cancelingEventId === event.id"
                          @click.stop="handleCancelEvent(event)"
                        >
                          {{ t('events.cancel') }}
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-else :description="t('profile.noEvents')" />
              </el-skeleton>
            </div>

            <div class="panel">
              <div class="panel-head">
                <h3>{{ t('profile.myRoutes') }}</h3>
                <el-button link @click="router.push({ name: 'Routes' })">
                  {{ t('profile.viewAll') }}
                </el-button>
              </div>
              <el-skeleton :loading="loading.routes" animated>
                <div v-if="topRoutes.length" class="route-list">
                  <div v-for="route in topRoutes" :key="route.id" class="route-item">
                    <div class="route-thumb" :style="{ backgroundImage: `url(${route.cover})` }" />
                    <div class="route-body">
                      <p class="route-title">{{ route.title }}</p>
                      <p class="route-meta">
                        {{ route.duration }} {{ t('profile.routeDays') }} · {{ routeThemeLabel(route.theme) }}
                      </p>
                      <div class="route-actions">
                        <el-button text type="primary" size="small" @click="goToRoute(route)">
                          {{ t('profile.viewDetail') }}
                        </el-button>
                        <el-button
                          text
                          type="danger"
                          size="small"
                          :loading="deletingRouteId === route.id"
                          @click.stop="confirmDeleteRoute(route)"
                        >
                          {{ t('common.delete') }}
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-else :description="t('profile.noRoutes')" />
              </el-skeleton>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="t('profile.commentsTab')" name="comments">
          <el-skeleton :loading="loading.comments" animated>
            <div v-if="myComments.list.length" class="comment-cards">
              <div v-for="comment in myComments.list" :key="comment.id" class="comment-card">
                <div class="comment-card-head">
                  <div>
                    <p class="post-title">{{ comment.postTitle }}</p>
                    <p class="item-meta">{{ formatRelative(comment.createdAt) }}</p>
                  </div>
                  <div class="comment-actions">
                    <template v-if="editingCommentId === comment.id">
                      <el-button
                        class="action-btn action-btn-success"
                        size="small"
                        :loading="commentSaving"
                        :icon="Check"
                        @click="saveCommentEdit"
                      >
                        {{ t('profile.commentSave') }}
                      </el-button>
                      <el-button
                        class="action-btn action-btn-cancel"
                        size="small"
                        :icon="Close"
                        @click="cancelEditComment"
                      >
                        {{ t('profile.commentCancelEdit') }}
                      </el-button>
                    </template>
                    <template v-else>
                      <el-button
                        class="action-btn action-btn-edit"
                        size="small"
                        :icon="Edit"
                        @click="startEditComment(comment)"
                      >
                        {{ t('profile.commentEdit') }}
                      </el-button>
                      <el-button
                        class="action-btn action-btn-delete"
                        size="small"
                        :icon="Delete"
                        :loading="deletingCommentId === comment.id"
                        @click="confirmDeleteComment(comment)"
                      >
                        {{ t('profile.commentDelete') }}
                      </el-button>
                    </template>
                  </div>
                </div>
                <div v-if="editingCommentId === comment.id" class="comment-editor">
                  <el-input v-model="commentDraft" type="textarea" :rows="3" />
                </div>
                <p v-else class="comment-text">{{ comment.content }}</p>
                <div class="comment-entry">
                  <el-button
                    class="view-detail-btn"
                    type="primary"
                    size="small"
                    :icon="View"
                    @click="router.push(`/community/post/${comment.postId}`)"
                  >
                    {{ t('profile.viewDetail') }}
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else :description="t('profile.noComments')" />
          </el-skeleton>
        </el-tab-pane>

        <el-tab-pane :label="t('profile.favoritesTab')" name="favorites">
          <div class="favorites-toolbar">
            <el-radio-group v-model="favoriteFilter" size="small">
              <el-radio-button
                v-for="option in favoriteFilterOptions"
                :key="option.value"
                :value="option.value"
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
import { useRoute, useRouter, type LocationQueryValue } from 'vue-router'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  ArrowRight,
  Edit,
  Delete,
  Check,
  Close,
  View,
} from '@element-plus/icons-vue'
import {
  ElMessage,
  ElMessageBox,
  type UploadProps,
  type UploadRequestOptions,
} from 'element-plus'
import { useUserStore } from '@/store/user'
import { deleteMyComment, getMyComments, getMyPosts, updateMyComment } from '@/api/community'
import { getFavorites } from '@/api/culture'
import { cancelEventRegistration, getMyRegisteredEvents } from '@/api/event'
import { getMyRoutes, deleteMyRoute } from '@/api/route'
import type { CommunityPost, MyComment } from '@/types/community'
import type { CultureResource } from '@/types/culture'
import type { Event } from '@/types/event'
import type { Route } from '@/types/route'
import request from '@/utils/axios'
import ScenicHero from '@/components/profile/ScenicHero.vue'
import GalleryCarousel, { type GalleryItem } from '@/components/profile/GalleryCarousel.vue'
import MiniEventsCalendar, { type CalendarPreview } from '@/components/profile/MiniEventsCalendar.vue'
import type { UserBadge } from '@/types/user'

dayjs.extend(relativeTime)

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const userStore = useUserStore()

const activeTab = ref('activity')
const commentsInitialized = ref(false)
const TAB_KEYS = ['activity', 'comments', 'favorites', 'settings'] as const

const syncTabFromQuery = (tab?: LocationQueryValue | LocationQueryValue[]) => {
  if (typeof tab !== 'string') return
  if (!TAB_KEYS.includes(tab as (typeof TAB_KEYS)[number])) return
  if (activeTab.value !== tab) {
    activeTab.value = tab
  }
}

watch(
  () => route.query.tab,
  tab => {
    syncTabFromQuery(tab)
  },
  { immediate: true },
)

watch(activeTab, tab => {
  const nextQuery = { ...route.query }
  if (tab === 'activity') {
    delete nextQuery.tab
  } else {
    nextQuery.tab = tab
  }
  if (route.query.tab !== nextQuery.tab) {
    router.replace({ query: nextQuery })
  }
  if (tab === 'comments' && !commentsInitialized.value && !loading.comments) {
    loadMyComments()
  }
})
const editDialog = ref(false)
const avatarUploading = ref(false)
const pageLoading = ref(false)

const posts = reactive<{ list: CommunityPost[]; total: number }>({ list: [], total: 0 })
const favorites = reactive<{ list: CultureResource[]; total: number }>({ list: [], total: 0 })
const favoriteFilter = ref<'all' | 'article' | 'exhibit' | 'video' | 'audio'>('all')
const events = reactive<{ list: Event[]; total: number }>({ list: [], total: 0 })
const routes = reactive<{ list: Route[]; total: number }>({ list: [], total: 0 })
const myComments = reactive<{ list: MyComment[]; total: number }>({ list: [], total: 0 })

const loading = reactive({
  posts: false,
  favorites: false,
  events: false,
  routes: false,
  comments: false,
  save: false,
})

const editForm = reactive({
  nickname: '',
  bio: '',
  phone: '',
})

const editingCommentId = ref<number | null>(null)
const commentDraft = ref('')
const commentSaving = ref(false)
const deletingCommentId = ref<number | null>(null)
const cancelingEventId = ref<number | null>(null)
const deletingRouteId = ref<number | null>(null)

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
  { key: 'routes', label: t('profile.myRoutes'), value: routes.total },
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

const getPostStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
  }
  return labels[status] || status
}

const getPostStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
  const types: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
  }
  return types[status] || 'info'
}

const getRegistrationStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    PENDING: '等待审核',
    APPROVED: '报名成功',
    REJECTED: '已拒绝',
  }
  return labels[status] || status
}

const getRegistrationStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
  const types: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
  }
  return types[status] || 'info'
}

const statusTypeMap: Record<string, CalendarPreview['statusType']> = {
  upcoming: 'info',
  ongoing: 'success',
  past: 'warning',
}

const statusType = (status?: string): CalendarPreview['statusType'] => {
  const normalized = status?.toLowerCase()
  return statusTypeMap[normalized ?? ''] ?? 'info'
}

const isActiveEvent = (event?: Event) => {
  if (!event?.status) return true
  return event.status.toLowerCase() !== 'past'
}

const canCancelEvent = (event: Event) => isActiveEvent(event)

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

const topRoutes = computed(() => routes.list.slice(0, 3))

const calendarPreview = computed((): CalendarPreview[] => {
  const fallbackEvents = (userInfo.value?.registeredEvents ?? []).filter(isActiveEvent)
  const baseEvents: Event[] = events.list.length ? events.list : fallbackEvents
  return baseEvents
    .filter(isActiveEvent)
    .slice(0, 4)
    .map(event => ({
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
    if (result?.data?.url) {
      await userStore.updateUser({ avatar: result.data.url })
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

const goToRoute = (route: Route) => {
  router.push({ name: 'RouteDetail', params: { id: route.id } })
}

const handleEditPost = (post: CommunityPost) => {
  // 跳转到帖子详情页，用户可以在那里编辑
  router.push(`/community/post/${post.id}?edit=true`)
}

const routeThemeLabel = (theme?: string | null) => {
  if (!theme) return '--'
  const themeMap: Record<string, string> = {
    silksroad: t('routes.themes.silkRoad'),
    silkroad: t('routes.themes.silkRoad'),
    nature: t('routes.themes.nature'),
    culture: t('routes.themes.culture'),
    food: t('routes.themes.food'),
  }
  return theme
    .split(/[，,]/)
    .map(item => {
      const key = item.trim()
      const mapped = themeMap[key.toLowerCase()]
      return mapped || key
    })
    .filter(Boolean)
    .join(' · ')
}

const startEditComment = (comment: MyComment) => {
  editingCommentId.value = comment.id
  commentDraft.value = comment.content
}

const cancelEditComment = () => {
  editingCommentId.value = null
  commentDraft.value = ''
}

const saveCommentEdit = async () => {
  if (!editingCommentId.value) return
  if (!commentDraft.value.trim()) {
    ElMessage.warning(t('profile.commentRequired'))
    return
  }
  commentSaving.value = true
  try {
    await updateMyComment(editingCommentId.value, commentDraft.value.trim())
    ElMessage.success(t('profile.commentUpdated'))
    cancelEditComment()
    await loadMyComments()
  } finally {
    commentSaving.value = false
  }
}

const confirmDeleteComment = (comment: MyComment) => {
  ElMessageBox.confirm(t('profile.commentDeleteConfirm'), t('profile.commentDelete'), {
    confirmButtonText: t('profile.commentDelete'),
    cancelButtonText: t('profile.cancel'),
    type: 'warning',
  })
    .then(async () => {
      deletingCommentId.value = comment.id
      try {
        await deleteMyComment(comment.id)
        ElMessage.success(t('profile.commentDeleted'))
        if (editingCommentId.value === comment.id) {
          cancelEditComment()
        }
        // 直接从列表中移除已删除的评论
        const index = myComments.list.findIndex(c => c.id === comment.id)
        if (index !== -1) {
          myComments.list.splice(index, 1)
          myComments.total = Math.max(0, myComments.total - 1)
        }
      } finally {
        deletingCommentId.value = null
      }
    })
    .catch(() => {})
}

const handleCancelEvent = (event: Event) => {
  ElMessageBox.confirm(t('profile.cancelEventConfirm'), t('events.cancel'), {
    confirmButtonText: t('events.cancel'),
    cancelButtonText: t('profile.cancel'),
    type: 'warning',
  })
    .then(async () => {
      cancelingEventId.value = event.id
      try {
        await cancelEventRegistration(event.id)
        ElMessage.success(t('profile.cancelEventSuccess'))
        await loadEvents()
      } finally {
        cancelingEventId.value = null
      }
    })
    .catch(() => {})
}

const confirmDeleteRoute = (route: Route) => {
  ElMessageBox.confirm(
    t('profile.deleteRouteConfirm', { title: route.title }),
    t('profile.deleteRoute'),
    {
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    }
  )
    .then(async () => {
      deletingRouteId.value = route.id
      try {
        await deleteMyRoute(route.id)
        ElMessage.success(t('profile.deleteRouteSuccess'))
        // 直接从列表中移除已删除的路线
        const index = routes.list.findIndex(r => r.id === route.id)
        if (index !== -1) {
          routes.list.splice(index, 1)
          routes.total = Math.max(0, routes.total - 1)
        }
      } finally {
        deletingRouteId.value = null
      }
    })
    .catch(() => {})
}

const refreshAll = async () => {
  pageLoading.value = true
  await Promise.all([
    loadPosts(),
    loadFavorites(),
    loadEvents(),
    loadRoutes(),
    loadMyComments(true),
  ])
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
    const filtered = (res.list ?? []).filter(isActiveEvent)
    events.list = filtered
    events.total = filtered.length
  } finally {
    loading.events = false
  }
}

const loadRoutes = async () => {
  loading.routes = true
  try {
    const res = (await getMyRoutes({ page: 1, size: 6 })) as {
      list?: Route[]
      total?: number
    }
    routes.list = res.list ?? []
    routes.total = res.total ?? routes.list.length
  } finally {
    loading.routes = false
  }
}

const loadMyComments = async (force = false) => {
  if (loading.comments || (commentsInitialized.value && !force)) {
    return
  }
  loading.comments = true
  try {
    const res = (await getMyComments({ page: 1, size: 10 })) as {
      list?: MyComment[]
      total?: number
    }
    myComments.list = res.list ?? []
    myComments.total = res.total ?? myComments.list.length
    commentsInitialized.value = true
  } catch (error) {
    console.error('[Profile] Failed to load comments', error)
    myComments.list = []
    myComments.total = 0
    if (!force) {
      commentsInitialized.value = false
    }
  } finally {
    loading.comments = false
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

.timeline-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.timeline-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-item:hover {
  transform: translateX(6px);
  box-shadow: var(--card-shadow-hover);
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.item-head-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.item-title {
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.2s;
  cursor: pointer;
  flex: 1;
}

.item-title:hover {
  color: var(--el-color-primary);
}

.reject-reason-box {
  margin-top: 12px;
}

.item-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
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

.route-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.route-item {
  display: flex;
  gap: 16px;
  padding: 14px;
  border-radius: var(--radius-md);
  background: var(--bg-paper);
  box-shadow: var(--card-shadow);
}

.route-thumb {
  width: 96px;
  height: 72px;
  border-radius: var(--radius-md);
  background-size: cover;
  background-position: center;
}

.route-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.route-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.route-title {
  font-weight: 600;
  color: var(--text-primary);
}

.route-meta {
  color: var(--text-muted);
  font-size: 13px;
}

.comment-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.comment-card {
  padding: 18px;
  border-radius: var(--radius-md);
  background: var(--bg-paper);
  box-shadow: var(--card-shadow);
}

.comment-card .post-title {
  font-weight: 600;
  color: var(--text-primary);
}

.comment-card-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.comment-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.action-btn {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.action-btn-edit {
  color: #409eff;
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.action-btn-edit:hover {
  background: rgba(64, 158, 255, 0.15);
  border-color: #66b1ff;
  color: #66b1ff;
}

.action-btn-delete {
  color: #f56c6c;
  border-color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.action-btn-delete:hover {
  background: rgba(245, 108, 108, 0.15);
  border-color: #f78989;
  color: #f78989;
}

.action-btn-success {
  color: #67c23a;
  border-color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}

.action-btn-success:hover {
  background: rgba(103, 194, 58, 0.15);
  border-color: #85ce61;
  color: #85ce61;
}

.action-btn-cancel {
  color: #909399;
  border-color: #909399;
  background: rgba(144, 147, 153, 0.1);
}

.action-btn-cancel:hover {
  background: rgba(144, 147, 153, 0.15);
  border-color: #a6a9ad;
  color: #a6a9ad;
}

.view-detail-btn {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(64, 158, 255, 0.2);
}

.view-detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.comment-editor {
  margin-top: 12px;
}

.comment-text {
  margin-top: 12px;
  color: var(--text-primary);
  line-height: 1.6;
}

.comment-entry {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
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






