<template>
  <div class="level-progress">
    <div class="progress-header">
      <span class="level-label">Lv{{ level }}</span>
      <span class="exp-label">{{ exp }} / {{ nextLevelExp }} EXP</span>
    </div>
    <el-progress
      :percentage="percentage"
      :stroke-width="12"
      :show-text="false"
      status="success"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  level: number
  exp: number
  nextLevelExp: number
}

const props = defineProps<Props>()

const percentage = computed(() => {
  if (!props.nextLevelExp) return 0
  return Math.min(Math.round((props.exp / props.nextLevelExp) * 100), 100)
})
</script>

<style scoped lang="scss">
.level-progress {
  .progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    color: #606266;
  }

  .level-label {
    font-weight: 600;
    color: #303133;
  }

  .exp-label {
    font-size: 12px;
  }
}
</style>















