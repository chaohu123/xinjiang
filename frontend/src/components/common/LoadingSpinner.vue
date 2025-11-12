<template>
  <div class="loading-spinner">
    <el-icon class="is-loading" :size="size">
      <Loading />
    </el-icon>
    <p v-if="text" class="loading-text">
      {{ text }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

interface Props {
  size?: number
  text?: string
}

withDefaults(defineProps<Props>(), {
  size: 32,
  text: '',
})
</script>

<style lang="scss" scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 16px;
}

.loading-text {
  color: #909399;
  font-size: 14px;
}
</style>
