<template>
  <transition name="route-card-fade">
    <div
      v-if="visible && point"
      ref="cardRef"
      class="route-card"
      :style="{ left: `${position.left}px`, top: `${position.top}px` }"
    >
      <div class="route-card__wrapper">
        <div class="route-card__hero">
          <div class="route-card__hero-info">
            <span class="route-card__chip">第 {{ point?.day || order }} 天</span>
            <p class="route-card__hero-title">{{ highlightTitle }}</p>
            <h3 class="route-card__place">{{ name }}</h3>
          </div>
          <div class="route-card__order">
            <span>#{{ order }}</span>
            <small>路线序号</small>
          </div>
        </div>
        <div class="route-card__body">
          <img v-if="image" class="route-card__image" :src="image" alt="route cover" />

          <div class="route-card__stats">
            <div class="stat-item">
              <span class="stat-label">🎫 门票</span>
              <strong>{{ priceLabel }}</strong>
            </div>
            <div class="stat-item">
              <span class="stat-label">⏱ 游览</span>
              <strong>{{ durationLabel }}</strong>
            </div>
            <div class="stat-item">
              <span class="stat-label">🌤 最佳</span>
              <strong>{{ bestTimeLabel }}</strong>
            </div>
          </div>

          <div class="mustgo-row">
            <span class="mustgo-chip">必去推荐</span>
            <span class="route-stage">行程位置 · 第 {{ point?.day || order }} 站</span>
          </div>

          <div class="highlight-section">
            <div class="experience-card">
              <div class="experience-icon">🎯</div>
              <div>
                <strong>特色活动</strong>
                <p>{{ featureActivityLabel }}</p>
              </div>
            </div>
            <div class="experience-card ghost">
              <div class="experience-icon">🔁</div>
              <div>
                <strong>替代方案</strong>
                <p>{{ alternateLabel }}</p>
              </div>
            </div>
          </div>

          <p :class="['route-card__info', { 'is-clamped': !detailExpanded && moreThanSummary }]">
            {{ summaryInfo }}
          </p>
          <el-button
            v-if="moreThanSummary"
            text
            size="small"
            @click="detailExpanded = !detailExpanded"
          >
            {{ detailExpanded ? '收起详情' : '展开更多' }}
          </el-button>

          <ul class="route-card__meta">
            <li>
              <span class="meta-label">站点类型</span>
              <span class="meta-value">{{ point?.dayTitle || '精选打卡点' }}</span>
            </li>
            <li>
              <span class="meta-label">地理坐标</span>
              <span class="meta-value coords" @click="copyCoords">{{ formattedCoords }}</span>
            </li>
            <li>
              <span class="meta-label">替代方案</span>
              <span class="meta-value">{{ alternateLabel }}</span>
            </li>
          </ul>

          <div class="route-card__badges">
            <span
              v-for="tag in tags"
              :key="tag"
              class="badge"
            >
              {{ tag }}
            </span>
            <span class="badge badge--ghost">AI 导航推荐</span>
          </div>

          <div class="route-card__actions">
            <el-button type="primary" size="small" @click="openNavigation">
              AI 导航
            </el-button>
            <el-button size="small" plain @click="copyCoords">复制坐标</el-button>
            <el-button size="small" text @click="markPlanned">标记计划</el-button>
            <el-button size="small" text @click="markVisited">标记已去</el-button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import type { PropType } from 'vue'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

interface RoutePoint {
  lng: number
  lat: number
}

const props = defineProps({
  visible: { type: Boolean, default: false },
  map: { type: Object as PropType<AMap.Map | null>, default: null },
  point: { type: Object as PropType<(RoutePoint & Record<string, any>) | null>, default: null },
  name: { type: String, default: '' },
  info: { type: String, default: '' },
  image: { type: String, default: '' },
  order: { type: Number, default: 1 },
})

const cardRef = ref<HTMLElement>()
const position = ref({ left: -9999, top: -9999 })
const highlightTitle = computed(() => props.point?.dayTitle || '行程亮点')
const formattedCoords = computed(() => {
  if (!props.point) return '--'
  const { lng, lat } = props.point
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return '--'
  return `${lng.toFixed(3)}, ${lat.toFixed(3)}`
})
const priceLabel = computed(() => {
  const value = props.point?.ticketPrice
  if (value === undefined || value === null || value === '') return '待定'
  if (typeof value === 'number') return `￥${value}`
  return String(value)
})
const durationLabel = computed(() => props.point?.duration || '约 1-2 小时')
const bestTimeLabel = computed(() => props.point?.bestTime || '下午时段最佳')
const alternateLabel = computed(() => props.point?.alternate || '可切换至附近博物馆/市集')
const tags = computed(() => {
  const list = props.point?.tags
  if (list && list.length) return list
  return ['沉浸式体验', '文化推荐']
})
const featureActivityLabel = computed(() => props.point?.featureActivity || '精选体验')
const detailExpanded = ref(false)
const summaryInfo = computed(() => {
  const content = props.info || ''
  if (!content) return '暂无简介信息'
  if (detailExpanded.value || content.length <= 80) return content
  return `${content.slice(0, 80)}...`
})
const moreThanSummary = computed(() => !!(props.info && props.info.length > 80))

const containerSize = computed(() => {
  if (!props.map) return { width: window.innerWidth, height: window.innerHeight }
  const size = props.map.getSize?.()
  return size
    ? { width: size.getWidth?.() ?? size.width ?? window.innerWidth, height: size.getHeight?.() ?? size.height ?? window.innerHeight }
    : { width: window.innerWidth, height: window.innerHeight }
})

const updatePosition = () => {
  if (!props.map || !props.point) return
  const { lng, lat } = props.point
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return
  const pixel = props.map.lngLatToContainer(new window.AMap.LngLat(lng, lat))
  if (!pixel) return
  const width = cardRef.value?.offsetWidth || 280
  const height = cardRef.value?.offsetHeight || 200
  const x = pixel.x - width / 2
  const y = pixel.y - height - 24
  const maxLeft = containerSize.value.width - width - 24
  const clampedX = Math.min(Math.max(x, 24), Math.max(maxLeft, 24))
  const minTop = 24
  const clampedY = Math.max(y, minTop)
  position.value = { left: clampedX, top: clampedY }
}

const registerMapEvents = () => {
  if (!props.map) return
  props.map.on('move', updatePosition)
  props.map.on('zoom', updatePosition)
}

const unregisterMapEvents = () => {
  if (!props.map) return
  props.map.off('move', updatePosition)
  props.map.off('zoom', updatePosition)
}

const openNavigation = () => {
  if (!props.point) return
  const { lng, lat, name } = props.point
  const url = `https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(name || '目的地')}`
  window.open(url, '_blank')
}

const copyCoords = async () => {
  if (!props.point || !navigator.clipboard) return
  await navigator.clipboard.writeText(formattedCoords.value)
  ElMessage.success('坐标已复制到剪贴板')
}

const markPlanned = () => {
  ElMessage.success('已标记为计划行程')
}

const markVisited = () => {
  ElMessage.success('已标记为已去过')
}

const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? `${text.slice(0, length)}...` : text
}

watch(
  () => [props.point, props.visible],
  () => {
    if (props.visible) {
      requestAnimationFrame(updatePosition)
    }
  }
)

watch(
  () => props.map,
  (newMap, oldMap) => {
    if (oldMap) {
      oldMap.off('move', updatePosition)
      oldMap.off('zoom', updatePosition)
    }
    if (newMap) {
      registerMapEvents()
      requestAnimationFrame(updatePosition)
    }
  }
)

onMounted(() => {
  registerMapEvents()
  requestAnimationFrame(updatePosition)
  window.addEventListener('resize', updatePosition)
})

onBeforeUnmount(() => {
  unregisterMapEvents()
  window.removeEventListener('resize', updatePosition)
})
</script>

<style scoped lang="scss">
.route-card {
  position: absolute;
  z-index: 10;
  pointer-events: none;
}

.route-card__wrapper {
  width: 380px;
  pointer-events: auto;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 25px 60px rgba(15, 23, 42, 0.25);
  overflow: hidden;
  animation: fadeInUp 0.3s ease;
}

.route-card__hero {
  position: relative;
  padding: 20px 24px 24px;
  background: linear-gradient(135deg, #f87956, #fdb448 70%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.route-card__hero-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.route-card__chip {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: #fff;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.4px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.route-card__hero-title {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}

.route-card__place {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.route-card__order {
  text-align: right;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: flex-end;

  span {
    font-size: 26px;
    font-weight: 700;
    line-height: 1;
  }

  small {
    font-size: 12px;
    opacity: 0.8;
  }
}

.route-card__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px 24px;
}

.route-card__image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.4);
}

.route-card__info {
  margin: 0;
  font-size: 14px;
  color: #5c6b7a;
  line-height: 1.7;
}

.route-card__meta {
  list-style: none;
  margin: 0;
  padding: 12px 14px;
  border: 1px solid #eef1f5;
  border-radius: 12px;
  background: #f9fbff;
  display: flex;
  flex-direction: column;
  gap: 10px;

  li {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
  }
}

.meta-label {
  color: #8a97a6;
}

.meta-value {
  color: #1f2d3d;
  font-weight: 600;
}

.route-card__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.badge {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(88, 101, 242, 0.12);
  color: #5865f2;
  font-size: 12px;
  font-weight: 600;
}

.badge--ghost {
  background: rgba(0, 0, 0, 0.05);
  color: #475467;
}

.coords {
  cursor: pointer;
  text-decoration: underline;
}

.route-card__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.stat-item {
  border: 1px solid #f0f2f5;
  border-radius: 12px;
  padding: 10px 12px;
  background: #fffaf6;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #f08b4b;
}

.stat-item strong {
  font-size: 14px;
  color: #d65f2f;
}

.mustgo-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.mustgo-chip {
  background: rgba(255, 87, 51, 0.12);
  color: #ff5a3c;
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 12px;
}

.route-stage {
  font-size: 12px;
  color: #8a97a6;
}

.highlight-section {
  display: flex;
  gap: 12px;
  margin: 16px 0;
}

.experience-card {
  flex: 1;
  border-radius: 14px;
  padding: 12px;
  border: 1px solid #eef2ff;
  background: #f9fbff;
  display: flex;
  gap: 10px;
}

.experience-card.ghost {
  background: #fff7ef;
  border-color: #ffe5c8;
}

.experience-icon {
  font-size: 20px;
}

.experience-card strong {
  font-size: 13px;
  color: #1f2d3d;
}

.experience-card p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #7a8899;
}

.route-card__info.is-clamped {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.route-card__actions {
  display: flex;
  gap: 10px;
  justify-content: space-between;
}

.route-card-fade-enter-active,
.route-card-fade-leave-active {
  transition: all 0.25s ease;
}

.route-card-fade-enter-from,
.route-card-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

