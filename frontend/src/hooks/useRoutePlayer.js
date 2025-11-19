import { onBeforeUnmount, ref, watch } from 'vue'

const EARTH_RADIUS = 6378137

const toRad = (value) => (value * Math.PI) / 180

const distanceBetween = (a, b) => {
  if (!a || !b) return 0
  const dLat = toRad(b.lat - a.lat)
  const dLng = toRad(b.lng - a.lng)
  const lat1 = toRad(a.lat)
  const lat2 = toRad(b.lat)

  const hav =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2
  return 2 * EARTH_RADIUS * Math.asin(Math.sqrt(hav))
}

export const useRoutePlayer = ({ map, speed = 60 } = {}) => {
  const routePoints = ref([])
  const activePoint = ref(null)
  const isPlaying = ref(false)
  const routeReady = ref(false)
  const polyline = ref(null)
  const stopMarkers = ref([])
  const movingMarker = ref(null)

  let animationId = 0
  let lastTimestamp = 0
  let progress = 0
  let totalDistance = 0
  let milestones = []

  const buildMilestones = () => {
    milestones = routePoints.value.map((point, index) => {
      if (index === 0) return 0
      return milestones[index - 1] + distanceBetween(routePoints.value[index - 1], point)
    })
    totalDistance = milestones[routePoints.value.length - 1] || 0
  }

  const clearGraphics = () => {
    if (polyline.value) {
      polyline.value.setMap(null)
      polyline.value = null
    }
    if (movingMarker.value) {
      movingMarker.value.setMap(null)
      movingMarker.value = null
    }
    stopMarkers.value.forEach((marker) => marker.setMap(null))
    stopMarkers.value = []
  }

  const createStopMarker = (point, index) => {
    const el = document.createElement('div')
    el.className = 'route-stop-badge'
    el.textContent = String(index + 1)
    return new window.AMap.Marker({
      position: [point.lng, point.lat],
      content: el,
      offset: [-12, -12],
    })
  }

  const ensureGraphics = () => {
    if (!map?.value || !routePoints.value.length) return
    clearGraphics()

    polyline.value = new window.AMap.Polyline({
      path: routePoints.value.map((point) => [point.lng, point.lat]),
      strokeColor: '#53d8fb',
      strokeOpacity: 0.9,
      strokeWeight: 6,
      lineJoin: 'round',
      lineCap: 'round',
      showDir: true,
    })
    polyline.value.setMap(map.value)

    stopMarkers.value = routePoints.value.map((point, index) => {
      const marker = createStopMarker(point, index)
      marker.setMap(map.value)
      return marker
    })

    const carEl = document.createElement('div')
    carEl.className = 'route-car-marker'
    carEl.innerHTML = '🚗'

    movingMarker.value = new window.AMap.Marker({
      position: [routePoints.value[0].lng, routePoints.value[0].lat],
      content: carEl,
      offset: [-16, -16],
      autoRotation: true,
    })
    movingMarker.value.setMap(map.value)

    map.value.setFitView([polyline.value, ...stopMarkers.value], false, [80, 80, 80, 80])
  }

  const updateActivePoint = () => {
    if (!routePoints.value.length) return
    let index = milestones.findIndex((distance) => distance > progress)
    if (index === -1) {
      index = routePoints.value.length - 1
    } else if (index > 0) {
      index -= 1
    }
    const target = routePoints.value[index]
    activePoint.value = target
  }

  const updateMarkerPosition = () => {
    if (!movingMarker.value || !routePoints.value.length) return
    if (progress <= 0) {
      movingMarker.value.setPosition([routePoints.value[0].lng, routePoints.value[0].lat])
      updateActivePoint()
      return
    }
    if (progress >= totalDistance) {
      movingMarker.value.setPosition([
        routePoints.value[routePoints.value.length - 1].lng,
        routePoints.value[routePoints.value.length - 1].lat,
      ])
      updateActivePoint()
      return
    }

    let segmentIndex = milestones.findIndex((distance) => distance > progress)
    if (segmentIndex === -1) {
      segmentIndex = routePoints.value.length - 1
    }
    const startIdx = Math.max(segmentIndex - 1, 0)
    const startPoint = routePoints.value[startIdx]
    const endPoint = routePoints.value[startIdx + 1]
    const startDistance = milestones[startIdx]
    const segmentDistance = milestones[startIdx + 1] - startDistance
    const ratio = segmentDistance ? (progress - startDistance) / segmentDistance : 0

    const lng = startPoint.lng + (endPoint.lng - startPoint.lng) * ratio
    const lat = startPoint.lat + (endPoint.lat - startPoint.lat) * ratio
    movingMarker.value.setPosition([lng, lat])
    updateActivePoint()
  }

  const step = (timestamp) => {
    if (!isPlaying.value) return
    if (!lastTimestamp) {
      lastTimestamp = timestamp
    }
    const delta = timestamp - lastTimestamp
    lastTimestamp = timestamp
    progress += (delta / 1000) * speed
    if (progress >= totalDistance) {
      progress = totalDistance
      updateMarkerPosition()
      pause()
      return
    }
    updateMarkerPosition()
    animationId = requestAnimationFrame(step)
  }

  const start = () => {
    if (!routeReady.value || !routePoints.value.length) return
    if (isPlaying.value) return
    if (!movingMarker.value) {
      ensureGraphics()
    }
    isPlaying.value = true
    lastTimestamp = 0
    animationId = requestAnimationFrame(step)
  }

  const pause = () => {
    if (!isPlaying.value) return
    isPlaying.value = false
    cancelAnimationFrame(animationId)
  }

  const reset = () => {
    pause()
    progress = 0
    lastTimestamp = 0
    if (movingMarker.value && routePoints.value.length) {
      movingMarker.value.setPosition([routePoints.value[0].lng, routePoints.value[0].lat])
    }
    activePoint.value = routePoints.value[0] || null
  }

  const loadRoute = (points = []) => {
    routePoints.value = points.map((point, index) => ({
      ...point,
      order: index + 1,
    }))
    if (routePoints.value.length < 2) {
      routeReady.value = false
      clearGraphics()
      return
    }
    buildMilestones()
    ensureGraphics()
    reset()
    routeReady.value = true
  }

  watch(
    () => map?.value,
    (value) => {
      if (value && routePoints.value.length) {
        ensureGraphics()
        reset()
      }
    }
  )

  onBeforeUnmount(() => {
    pause()
    clearGraphics()
  })

  return {
    routePoints,
    activePoint,
    isPlaying,
    routeReady,
    loadRoute,
    start,
    pause,
    reset,
  }
}

export default useRoutePlayer


