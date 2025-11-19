<template>
  <section class="mini-calendar">
    <div class="mini-calendar__head">
      <div>
        <p class="calendar-eyebrow">{{ subtitle }}</p>
        <h3>{{ title }}</h3>
      </div>
      <el-button text type="primary" @click="$emit('view-all')">
        {{ actionLabel }}
      </el-button>
    </div>

    <el-skeleton :loading="loading" animated>
      <div v-if="events.length" class="calendar-list">
        <article v-for="event in events" :key="event.id" class="calendar-item">
          <div class="calendar-date">
            <span>{{ event.dateRange }}</span>
            <small v-if="event.location">{{ event.location }}</small>
          </div>
          <div class="calendar-body">
            <p class="title">{{ event.title }}</p>
            <el-tag :type="event.statusType" size="small">{{ event.statusLabel }}</el-tag>
          </div>
        </article>
      </div>
      <el-empty v-else :description="emptyText" />
    </el-skeleton>
  </section>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'

export interface CalendarPreview {
  id: string | number
  title: string
  dateRange: string
  statusLabel: string
  statusType?: '' | 'info' | 'success' | 'warning' | 'danger'
  location?: string
}

defineProps({
  title: {
    type: String,
    default: 'Events',
  },
  subtitle: {
    type: String,
    default: '',
  },
  actionLabel: {
    type: String,
    default: 'View all',
  },
  events: {
    type: Array as PropType<CalendarPreview[]>,
    default: () => [],
  },
  emptyText: {
    type: String,
    default: '暂无活动',
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

defineEmits<{
  'view-all': []
}>()
</script>

<style scoped>
.mini-calendar {
  background: var(--bg-paper);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--card-shadow);
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 320px;
}

.mini-calendar__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.calendar-eyebrow {
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

.calendar-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.calendar-item {
  display: flex;
  gap: 16px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-soft);
  transition: var(--transition-base);
}

.calendar-item:hover {
  border-color: rgba(15, 124, 143, 0.4);
  box-shadow: 0 18px 35px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.calendar-date {
  min-width: 140px;
  color: var(--text-muted);
  font-weight: 600;
  display: flex;
  flex-direction: column;
}

.calendar-date small {
  font-weight: 400;
  font-size: 13px;
}

.calendar-body {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.calendar-body .title {
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 600;
}

@media (max-width: 640px) {
  .calendar-item {
    flex-direction: column;
  }

  .calendar-date {
    min-width: auto;
  }

  .calendar-body {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>


















