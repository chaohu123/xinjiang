<template>
  <el-card class="culture-card card-shadow" @click="handleClick">
    <template #header>
      <div class="card-header">
        <el-image :src="resource.cover" fit="cover" class="card-image" lazy>
          <template #error>
            <div class="image-slot">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div class="card-badge">
          <el-tag :type="getTypeColor((resource as any).type || (resource as any).resourceType)" size="small">
            {{ getTypeName((resource as any).type || (resource as any).resourceType) }}
          </el-tag>
          <el-tag v-if="'source' in resource && resource.source === 'COMMUNITY_POST'" type="info" size="small" style="margin-left: 4px">
            社区
          </el-tag>
        </div>
      </div>
    </template>

    <div class="card-body">
      <h3 class="card-title">
        {{ resource.title }}
      </h3>
      <p class="card-description">
        {{ resource.description }}
      </p>
      <div v-if="resource.tags && resource.tags.length > 0" class="card-tags">
        <el-tag v-for="tag in resource.tags.slice(0, 3)" :key="tag" size="small" effect="plain">
          {{ tag }}
        </el-tag>
      </div>
      <div class="card-footer">
        <span v-if="resource.region" class="card-region">
          <el-icon><Location /></el-icon>
          {{ resource.region }}
        </span>
        <div class="card-stats">
          <span>
            <el-icon><View /></el-icon>
            {{ resource.views || 0 }}
          </span>
          <span>
            <el-icon><Star /></el-icon>
            {{ resource.favorites || 0 }}
          </span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { CultureResource, HomeResource } from '@/types/culture'
import { Picture, Location, View, Star } from '@element-plus/icons-vue'

interface Props {
  resource: CultureResource | HomeResource
}

const props = defineProps<Props>()
const router = useRouter()
const { t } = useI18n()

const getTypeName = (type?: string) => {
  if (!type) return '资源'
  // 将类型转换为小写以支持大小写不敏感的匹配
  const normalizedType = type?.toLowerCase() || ''
  const map: Record<string, string> = {
    article: '文章',
    exhibit: '展品',
    video: '视频',
    audio: '音频',
  }
  return map[normalizedType] || type
}

const getTypeColor = (type?: string) => {
  if (!type) return 'info'
  // 将类型转换为小写以支持大小写不敏感的匹配
  const normalizedType = type?.toLowerCase() || ''
  const map: Record<string, string> = {
    article: 'primary',
    exhibit: 'success',
    video: 'warning',
    audio: 'info',
  }
  // 如果找不到匹配的类型，返回 'info' 作为默认值，而不是空字符串
  // Element Plus 的 ElTag 不接受空字符串作为 type 属性
  return map[normalizedType] || 'info'
}

const handleClick = () => {
  const resource = props.resource as any
  // 如果是HomeResource，根据source跳转
  if ('source' in resource) {
    if (resource.source === 'CULTURE_RESOURCE') {
      router.push(`/detail/${resource.resourceType?.toLowerCase() || 'article'}/${resource.id}`)
    } else {
      router.push(`/community/${resource.id}`)
    }
  } else {
    // 传统的CultureResource
    router.push(`/detail/${resource.type}/${resource.id}`)
  }
}
</script>

<style lang="scss" scoped>
.culture-card {
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-4px);
  }
}

.card-header {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
}

.card-image {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

.card-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.card-body {
  padding: 0;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

.card-region {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-stats {
  display: flex;
  gap: 16px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
