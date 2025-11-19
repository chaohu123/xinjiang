/**
 * 动态加载高德地图 API
 */
export function loadAMapScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    // 检查是否已经加载，并验证所有必要的API
    if (window.AMap && window.AMap.Map && window.AMap.InfoWindow && window.AMap.Marker) {
      // 额外验证关键API是否可用
      const requiredAPIs = ['Map', 'InfoWindow', 'Marker', 'Polyline', 'MarkerClusterer']
      const allLoaded = requiredAPIs.every(api => {
        const apiPath = api.split('.')
        let current: any = window.AMap
        for (const part of apiPath) {
          if (!current || !current[part]) return false
          current = current[part]
        }
        return true
      })

      if (allLoaded) {
        console.log('[AMap] API已加载，跳过重复加载')
        resolve()
        return
      }
    }

    // 获取 API Key
    const key = import.meta.env.VITE_AMAP_KEY || ''
    if (!key) {
      console.warn('高德地图 API Key 未配置，请在 .env 文件中设置 VITE_AMAP_KEY')
    }

    // 创建脚本标签
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.async = true
    const pluginParam = 'AMap.MarkerClusterer'
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}&plugin=${pluginParam}&callback=initAMap`

    // 设置超时
    const timeout = setTimeout(() => {
      reject(new Error('高德地图加载超时'))
      delete (window as any).initAMap
    }, 30000) // 30秒超时

    // 设置全局回调
    ;(window as any).initAMap = () => {
      clearTimeout(timeout)

      // 验证所有必要的API是否加载
      const requiredAPIs = ['Map', 'InfoWindow', 'Marker', 'Polyline']
      const missingAPIs: string[] = []

      requiredAPIs.forEach(api => {
        if (!window.AMap || !window.AMap[api]) {
          missingAPIs.push(api)
        }
      })

      if (missingAPIs.length > 0) {
        const error = new Error(`高德地图API加载不完整，缺少: ${missingAPIs.join(', ')}`)
        console.error('[AMap]', error)
        reject(error)
        delete (window as any).initAMap
        return
      }

      console.log('[AMap] API加载成功')
      resolve()
      delete (window as any).initAMap
    }

    script.onerror = () => {
      clearTimeout(timeout)
      const error = new Error('高德地图脚本加载失败，请检查网络连接或 API Key')
      console.error('[AMap]', error)
      reject(error)
      delete (window as any).initAMap
    }

    // 添加到页面
    document.head.appendChild(script)
  })
}

/**
 * 验证AMap API是否完全加载
 */
export function isAMapReady(): boolean {
  if (!window.AMap) return false

  const requiredAPIs = ['Map', 'InfoWindow', 'Marker', 'Polyline', 'MarkerClusterer']
  return requiredAPIs.every(api => {
    const apiPath = api.split('.')
    let current: any = window.AMap
    for (const part of apiPath) {
      if (!current || !current[part]) return false
      current = current[part]
    }
    return true
  })
}


