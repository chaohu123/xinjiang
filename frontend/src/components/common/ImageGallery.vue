<template>
  <div class="image-gallery" :style="galleryStyle">
    <el-image
      v-for="(image, index) in images"
      :key="index"
      :src="image"
      :preview-src-list="images"
      :initial-index="index"
      :preview-teleported="true"
      fit="cover"
      class="gallery-image"
      loading="eager"
      :style="{ height: `${itemHeight}px` }"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  images: string[]
  itemHeight?: number
  columns?: number
}

const props = withDefaults(defineProps<Props>(), {
  itemHeight: 220,
  columns: 2,
})

const galleryStyle = computed(() => {
  const columns = Math.max(1, props.columns)
  return {
    gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))`,
  }
})
</script>

<style lang="scss" scoped>
.image-gallery {
  display: grid;
  gap: 16px;
}

.gallery-image {
  width: 100%;
  min-height: 200px;
  border-radius: 12px;
  cursor: zoom-in;
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
  }

  :deep(img) {
    object-fit: cover;
    width: 100%;
    height: 100%;
    border-radius: 12px;
  }
}
</style>
















