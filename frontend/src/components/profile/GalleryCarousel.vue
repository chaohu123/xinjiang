<template>
  <section class="gallery-carousel">
    <div class="gallery-head">
      <div>
        <p class="gallery-eyebrow">{{ subtitle }}</p>
        <h3>{{ title }}</h3>
      </div>
      <slot name="action" />
    </div>

    <el-skeleton :loading="loading" animated>
      <div v-if="items.length" class="gallery-track">
        <article
          v-for="item in items"
          :key="item.id"
          class="gallery-card"
          :style="getCardStyle(item.cover)"
        >
          <div class="gallery-card__meta">
            <p class="gallery-card__title">{{ item.title }}</p>
            <p v-if="item.description" class="gallery-card__desc">{{ item.description }}</p>
          </div>
        </article>
      </div>
      <el-empty v-else :description="emptyText" />
    </el-skeleton>
  </section>
</template>

<script setup lang="ts">
import type { CSSProperties, PropType } from 'vue'

export interface GalleryItem {
  id: string | number
  title: string
  cover?: string
  description?: string
}

const props = defineProps({
  title: {
    type: String,
    default: 'Gallery',
  },
  subtitle: {
    type: String,
    default: '',
  },
  items: {
    type: Array as PropType<GalleryItem[]>,
    default: () => [],
  },
  emptyText: {
    type: String,
    default: '暂无图片',
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const getCardStyle = (cover?: string): CSSProperties => {
  if (!cover) {
    return {
      backgroundImage: 'linear-gradient(135deg, rgba(15, 124, 143, 0.35), rgba(236, 170, 90, 0.35))',
    }
  }
  return {
    backgroundImage: `linear-gradient(180deg, rgba(6, 14, 24, 0) 30%, rgba(6, 14, 24, 0.75) 100%), url(${cover})`,
  }
}
</script>

<style scoped>
.gallery-carousel {
  background: var(--bg-paper);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--card-shadow);
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 320px;
}

.gallery-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.gallery-eyebrow {
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--text-muted);
  margin-bottom: 6px;
}

h3 {
  font-size: 22px;
  color: var(--text-primary);
}

.gallery-track {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.gallery-card {
  border-radius: var(--radius-md);
  min-height: 180px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.08);
}

.gallery-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.05), rgba(0, 0, 0, 0.6));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.gallery-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-shadow-hover);
}

.gallery-card:hover::after {
  opacity: 1;
}

.gallery-card__meta {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 16px;
  color: #fff;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.gallery-card__title {
  font-size: 16px;
  font-weight: 600;
}

.gallery-card__desc {
  font-size: 13px;
  opacity: 0.85;
}
</style>






















