<template>
  <div class="activity-calendar">
    <div class="calendar-header">
      <el-button text :icon="ArrowLeft" @click="$emit('change-month', -1)" />
      <div class="month-label">{{ displayMonth }}</div>
      <el-button text :icon="ArrowRight" @click="$emit('change-month', 1)" />
    </div>
    <div class="weekdays">
      <span v-for="day in WEEK_DAYS" :key="day">{{ day }}</span>
    </div>
    <div class="calendar-grid">
      <div
        v-for="cell in calendarCells"
        :key="cell.key"
        class="calendar-cell"
        :class="{
          'is-other-month': cell.otherMonth,
          'has-events': cell.events.length > 0,
          selected: selectedDay === cell.day && !cell.otherMonth,
        }"
        @click="handleSelect(cell)"
      >
        <div class="cell-header">
          <span>{{ cell.day }}</span>
          <el-tag v-if="cell.events.length" size="small" effect="dark">
            {{ cell.events.length }}
          </el-tag>
        </div>
        <div class="event-dots">
          <span
            v-for="event in cell.events.slice(0, 3)"
            :key="event.id"
            class="event-dot"
            :class="`type-${event.type}`"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import dayjs from 'dayjs'
import type { Event, EventCalendarResponse } from '@/types/event'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const WEEK_DAYS = ['一', '二', '三', '四', '五', '六', '日']

interface Props {
  data: EventCalendarResponse | null
}

const props = defineProps<Props>()
const emits = defineEmits<{
  (e: 'select-day', payload: { day: number; events: Event[] }): void
  (e: 'change-month', offset: number): void
}>()

const selectedDay = ref<number | null>(null)

const displayMonth = computed(() => props.data?.month || dayjs().format('YYYY-MM'))

const calendarCells = computed(() => {
  const month = displayMonth.value
  const start = dayjs(`${month}-01`)
  const firstDayOfWeek = (start.day() + 6) % 7 // 将周日调整到末尾
  const totalDays = start.daysInMonth()
  const cells: Array<{ key: string; day: number; otherMonth: boolean; events: Event[] }> = []
  const eventMap = new Map<number, Event[]>()
  props.data?.days.forEach(day => eventMap.set(day.day, day.events))

  const prevMonthDays = firstDayOfWeek
  const prevMonth = start.subtract(1, 'month')
  for (let i = prevMonthDays; i > 0; i--) {
    cells.push({
      key: `prev-${i}`,
      day: prevMonth.daysInMonth() - i + 1,
      otherMonth: true,
      events: [],
    })
  }

  for (let day = 1; day <= totalDays; day++) {
    cells.push({
      key: `curr-${day}`,
      day,
      otherMonth: false,
      events: eventMap.get(day) || [],
    })
  }

  const nextDays = 42 - cells.length
  for (let day = 1; day <= nextDays; day++) {
    cells.push({
      key: `next-${day}`,
      day,
      otherMonth: true,
      events: [],
    })
  }

  return cells
})

const handleSelect = (cell: { day: number; otherMonth: boolean; events: Event[] }) => {
  if (cell.otherMonth) return
  selectedDay.value = cell.day
  emits('select-day', { day: cell.day, events: cell.events })
}
</script>

<style scoped lang="scss">
.activity-calendar {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .month-label {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.calendar-cell {
  min-height: 80px;
  border-radius: 10px;
  padding: 6px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;

  &.is-other-month {
    opacity: 0.4;
    cursor: default;
  }

  &.has-events {
    border-color: #ebeef5;
  }

  &.selected {
    border-color: #409eff;
    box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.3);
  }
}

.cell-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #303133;

  :deep(.el-tag) {
    transform: scale(0.8);
    transform-origin: top right;
  }
}

.event-dots {
  margin-top: 12px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;

  .event-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;

    &.type-exhibition {
      background: #f56c6c;
    }

    &.type-performance {
      background: #e6a23c;
    }

    &.type-workshop {
      background: #67c23a;
    }

    &.type-tour {
      background: #409eff;
    }
  }
}
</style>















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
