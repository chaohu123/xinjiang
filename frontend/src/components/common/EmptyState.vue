<template>
  <div class="empty-state">
    <el-icon :size="64" class="empty-icon">
      <component :is="icon" />
    </el-icon>
    <p class="empty-text">
      {{ text }}
    </p>
    <el-button v-if="actionText" type="primary" @click="$emit('action')">
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Box } from '@element-plus/icons-vue'

interface Props {
  text?: string
  icon?: any
  actionText?: string
}

withDefaults(defineProps<Props>(), {
  text: '暂无数据',
  icon: Box,
  actionText: '',
})

defineEmits<{
  action: []
}>()
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 20px;
}
</style>
