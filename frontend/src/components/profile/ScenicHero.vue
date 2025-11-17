<template>
  <section class="scenic-hero" :style="coverStyle">
    <div class="scenic-hero__overlay" />
    <div class="scenic-hero__content">
      <div class="scenic-hero__avatar-block">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          accept="image/*"
          :http-request="uploadRequest"
          :before-upload="beforeUpload"
          :disabled="avatarUploading"
        >
          <el-avatar :size="116" :src="avatar" class="avatar">
            {{ initials }}
          </el-avatar>
          <p class="upload-tip">
            <el-icon><Edit /></el-icon>
            <span>{{ t('profile.updateAvatar') }}</span>
          </p>
        </el-upload>
      </div>

      <div class="scenic-hero__main">
        <div class="scenic-hero__header">
          <div>
            <span class="membership-chip">{{ membershipTier }}</span>
            <h2>{{ displayName }}</h2>
            <p v-if="bio" class="bio">{{ bio }}</p>
          </div>
          <div class="scenic-hero__meta">
            <p>
              <el-icon><Calendar /></el-icon>
              {{ t('profile.joined', { date: joinDate }) }}
            </p>
            <p v-if="email">
              <el-icon><Location /></el-icon>
              {{ email }}
            </p>
            <p>
              <el-icon><Timer /></el-icon>
              {{ t('profile.daysActive', { days: daysActive }) }}
            </p>
          </div>
        </div>

        <div class="scenic-hero__stats" v-if="stats.length">
          <div v-for="stat in stats" :key="stat.key" class="stat-card">
            <span class="value">{{ stat.value }}</span>
            <span class="label">{{ stat.label }}</span>
          </div>
        </div>

        <div v-if="displayedBadges.length" class="scenic-hero__badges">
          <p class="badges-title">{{ t('profile.badgesTitle') }}</p>
          <div class="badge-list">
            <el-tag
              v-for="badge in displayedBadges"
              :key="badge.id"
              type="info"
              size="large"
              effect="dark"
              class="badge-chip"
            >
              {{ badge.label }}
            </el-tag>
            <el-tooltip
              v-if="badgeOverflow"
              class="badge-overflow"
              effect="dark"
              :content="t('profile.badgesMore', { count: badgeOverflow })"
              placement="top"
            >
              <span>+{{ badgeOverflow }}</span>
            </el-tooltip>
          </div>
        </div>
      </div>

      <div class="scenic-hero__side">
        <div class="completion">
          <span>{{ t('profile.profileCompletion') }}</span>
          <div class="completion-value">
            <strong>{{ completion }}%</strong>
            <el-progress :percentage="completion" :stroke-width="6" />
          </div>
        </div>
        <el-button type="primary" size="large" @click="$emit('edit')" :loading="editLoading">
          {{ t('profile.editProfile') }}
        </el-button>
        <el-button size="large" :loading="refreshLoading" @click="$emit('refresh')">
          {{ t('profile.refresh') }}
        </el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { PropType } from 'vue'
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { Calendar, Edit, Location, Timer } from '@element-plus/icons-vue'
import type { UserBadge } from '@/types/user'

interface HeroStat {
  key: string
  label: string
  value: number | string
}

const props = defineProps({
  displayName: {
    type: String,
    required: true,
  },
  bio: {
    type: String,
    default: '',
  },
  avatar: {
    type: String,
    default: '',
  },
  initials: {
    type: String,
    default: '',
  },
  membershipTier: {
    type: String,
    required: true,
  },
  joinDate: {
    type: String,
    required: true,
  },
  email: {
    type: String,
    default: '',
  },
  daysActive: {
    type: Number,
    default: 0,
  },
  stats: {
    type: Array as PropType<HeroStat[]>,
    default: () => [],
  },
  completion: {
    type: Number,
    default: 0,
  },
  badges: {
    type: Array as PropType<UserBadge[]>,
    default: () => [],
  },
  cover: {
    type: String,
    default: '',
  },
  avatarUploading: {
    type: Boolean,
    default: false,
  },
  beforeUpload: {
    type: Function as PropType<UploadProps['beforeUpload']>,
    default: undefined,
  },
  uploadRequest: {
    type: Function as PropType<(options: UploadRequestOptions) => Promise<void> | void>,
    default: undefined,
  },
  editLoading: {
    type: Boolean,
    default: false,
  },
  refreshLoading: {
    type: Boolean,
    default: false,
  },
})

defineEmits<{
  edit: []
  refresh: []
}>()

const { t } = useI18n()

const coverStyle = computed(() => {
  if (props.cover) {
    return {
      backgroundImage: `linear-gradient(120deg, rgba(10, 23, 35, 0.75), rgba(10, 41, 62, 0.8)), url(${props.cover})`,
    }
  }
  return {
    backgroundImage: 'var(--hero-gradient)',
  }
})

const displayedBadges = computed(() => props.badges.slice(0, 3))
const badgeOverflow = computed(() =>
  props.badges.length > 3 ? props.badges.length - displayedBadges.value.length : 0,
)
</script>

<style scoped>
.scenic-hero {
  position: relative;
  border-radius: var(--radius-lg);
  padding: 32px;
  background-size: cover;
  background-position: center;
  box-shadow: var(--card-shadow);
  overflow: hidden;
  color: #fff;
  min-height: 280px;
}

.scenic-hero__overlay {
  position: absolute;
  inset: 0;
  background-image: var(--hero-overlay);
  z-index: 1;
}

.scenic-hero__content {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 200px 1fr 220px;
  gap: 32px;
  align-items: center;
}

.scenic-hero__avatar-block {
  text-align: center;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  cursor: pointer;
}

.avatar {
  border: 4px solid rgba(255, 255, 255, 0.25);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.35);
}

.upload-tip {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.scenic-hero__header {
  display: flex;
  justify-content: space-between;
  gap: 32px;
  align-items: flex-start;
}

.membership-chip {
  display: inline-flex;
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

h2 {
  font-size: 32px;
  margin: 10px 0 4px;
}

.bio {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
}

.scenic-hero__meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.scenic-hero__meta p {
  display: flex;
  align-items: center;
  gap: 6px;
}

.scenic-hero__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-md);
  padding: 16px;
  backdrop-filter: blur(6px);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card .value {
  font-size: 24px;
  font-weight: 600;
}

.stat-card .label {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: rgba(255, 255, 255, 0.8);
}

.scenic-hero__badges {
  margin-top: 20px;
}

.badges-title {
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.badge-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.badge-chip {
  background: rgba(255, 255, 255, 0.2);
  border: none;
}

.badge-overflow {
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 13px;
}

.scenic-hero__side {
  display: flex;
  flex-direction: column;
  gap: 16px;
  backdrop-filter: blur(6px);
  padding: 20px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.1);
}

.completion {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.completion strong {
  font-size: 32px;
}

@media (max-width: 1024px) {
  .scenic-hero__content {
    grid-template-columns: 1fr;
  }

  .scenic-hero__header {
    flex-direction: column;
  }

  .scenic-hero__side {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }

  .completion {
    flex: 1 1 160px;
  }
}

@media (max-width: 640px) {
  .scenic-hero {
    padding: 20px;
  }

  .scenic-hero__side {
    flex-direction: column;
    width: 100%;
  }
}
</style>

