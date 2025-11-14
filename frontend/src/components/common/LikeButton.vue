<template>
  <button
    class="like-button"
    :class="{ active: isActive }"
    @click="toggleLike"
    @dblclick="handleDoubleClick"
  >
    <div class="pulse-effect"></div>
    <svg class="like-icon" viewBox="0 0 24 24">
      <path
        d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
      />
    </svg>
    <span class="like-count">{{ count }}</span>
  </button>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  initialCount?: number
  initialActive?: boolean
  enableDoubleClick?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  initialCount: 0,
  initialActive: false,
  enableDoubleClick: true,
})

const emit = defineEmits<{
  likeToggled: [isActive: boolean]
  doubleLiked: []
}>()

const isActive = ref(props.initialActive)
const count = ref(props.initialCount)

watch(
  () => props.initialActive,
  (newVal) => {
    isActive.value = newVal
  }
)

watch(
  () => props.initialCount,
  (newVal) => {
    count.value = newVal
  }
)

const toggleLike = () => {
  isActive.value = !isActive.value
  count.value += isActive.value ? 1 : -1
  emit('likeToggled', isActive.value)
}

const handleDoubleClick = (event: MouseEvent) => {
  if (!props.enableDoubleClick) return

  if (!isActive.value) {
    isActive.value = true
    count.value += 1
    emit('likeToggled', isActive.value)
  }

  // 创建爆炸效果
  createExplosion(event.currentTarget as HTMLElement, 12)
  emit('doubleLiked')
}

const createExplosion = (container: HTMLElement, particleCount: number) => {
  const explosion = document.createElement('div')
  explosion.className = 'explosion'
  container.appendChild(explosion)

  // 创建粒子
  for (let i = 0; i < particleCount; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    explosion.appendChild(particle)

    // 随机位置和动画
    const angle = Math.random() * Math.PI * 2
    const distance = 30 + Math.random() * 50
    const size = 5 + Math.random() * 8

    particle.style.width = `${size}px`
    particle.style.height = `${size}px`
    particle.style.background = getRandomColor()

    const animation = particle.animate(
      [
        {
          transform: 'translate(0, 0) scale(1)',
          opacity: 1,
        },
        {
          transform: `translate(${Math.cos(angle) * distance}px, ${Math.sin(angle) * distance}px) scale(0)`,
          opacity: 0,
        },
      ],
      {
        duration: 600 + Math.random() * 400,
        easing: 'cubic-bezier(0.1, 0.8, 0.2, 1)',
      }
    )

    animation.onfinish = () => {
      particle.remove()
    }
  }

  // 清理爆炸容器
  setTimeout(() => {
    explosion.remove()
  }, 1000)
}

const getRandomColor = () => {
  const colors = ['#e74c3c', '#e67e22', '#f1c40f', '#2ecc71', '#3498db', '#9b59b6']
  return colors[Math.floor(Math.random() * colors.length)]
}
</script>

<style lang="scss" scoped>
.like-button {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: auto;
  transition: all 0.3s;
  outline: none;
  color: #606266;
  font-size: 14px;
  line-height: 1;
  vertical-align: middle;

  &:hover {
    color: #409eff;
  }

  &:active {
    transform: scale(0.95);
  }
}

.like-icon {
  width: 16px;
  height: 16px;
  fill: #bdc3c7;
  transition: all 0.3s;
  flex-shrink: 0;
}

.like-button.active .like-icon {
  fill: #e74c3c;
}

.pulse-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(231, 76, 60, 0.3);
  opacity: 0;
  pointer-events: none;
}

.like-button.active .pulse-effect {
  animation: pulse 0.5s ease-out;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

.explosion {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 1000;
  width: 30px;
  height: 30px;
}

.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e74c3c;
  opacity: 0;
}

.like-count {
  font-size: 14px;
  font-weight: normal;
  color: inherit;
  transition: all 0.3s;
  line-height: 1;
}

.like-button.active {
  color: #e74c3c;

  .like-count {
    color: #e74c3c;
  }

  &:hover {
    color: #e74c3c;
  }
}

// 超级点赞动画
@keyframes superPulse {
  0% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.5);
  }
  50% {
    transform: scale(1.2);
  }
  75% {
    transform: scale(1.4);
  }
  100% {
    transform: scale(1);
  }
}
</style>

