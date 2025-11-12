<template>
  <div class="video-player-wrapper">
    <video
      ref="videoRef"
      class="video-js vjs-big-play-centered"
      :poster="poster"
      controls
      preload="auto"
    >
      <source :src="src" type="video/mp4" />
      <p class="vjs-no-js">
        To view this video please enable JavaScript, and consider upgrading to a web browser that
        <a href="https://videojs.com/html5-video-support/" target="_blank">supports HTML5 video</a>.
      </p>
    </video>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import type Player from 'video.js/dist/types/player'

interface Props {
  src: string
  poster?: string
  autoplay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  autoplay: false,
})

const videoRef = ref<HTMLVideoElement>()
let player: Player | null = null

onMounted(() => {
  if (videoRef.value) {
    player = videojs(videoRef.value, {
      autoplay: props.autoplay,
      controls: true,
      responsive: true,
      fluid: true,
    })
  }
})

watch(
  () => props.src,
  () => {
    if (player) {
      player.src({ type: 'video/mp4', src: props.src })
    }
  }
)

onUnmounted(() => {
  if (player) {
    player.dispose()
  }
})
</script>

<style lang="scss" scoped>
.video-player-wrapper {
  width: 100%;
  max-width: 100%;
}

:deep(.video-js) {
  width: 100%;
  height: auto;
  max-height: 600px;
}
</style>
