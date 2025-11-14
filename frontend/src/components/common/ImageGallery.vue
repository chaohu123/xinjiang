<template>
  <div class="image-gallery">
    <el-image
      v-for="(image, index) in images"
      :key="index"
      :src="image"
      :preview-src-list="images"
      :initial-index="index"
      fit="cover"
      class="gallery-image"
      lazy
    />
  </div>
</template>

<script setup lang="ts">
interface Props {
  images: string[]
}

defineProps<Props>()
</script>

<style lang="scss" scoped>
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.gallery-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.05);
  }
}
</style>







