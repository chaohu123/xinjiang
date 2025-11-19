import { onBeforeUnmount, ref } from 'vue'
import { loadAMapScript } from '@/utils/amap'

/**
 * 初始化基础地图实例，提供容器、状态与销毁方法。
 */
export const useMap = () => {
  const map = ref(null)
  const mapContainer = ref(null)
  const mapReady = ref(false)

  const initializeMap = async (options = {}) => {
    if (map.value || !mapContainer.value) return
    await loadAMapScript()
    map.value = new window.AMap.Map(mapContainer.value, {
      viewMode: '3D',
      center: [87.627704, 43.783894],
      zoom: 6,
      resizeEnable: true,
      animateEnable: true,
      pitch: 0,
      ...options,
    })
    mapReady.value = true
  }

  const resize = () => {
    if (map.value) {
      map.value.resize()
    }
  }

  onBeforeUnmount(() => {
    if (map.value) {
      map.value.destroy()
      map.value = null
    }
    mapReady.value = false
  })

  return {
    map,
    mapContainer,
    mapReady,
    initializeMap,
    resize,
  }
}

export default useMap


