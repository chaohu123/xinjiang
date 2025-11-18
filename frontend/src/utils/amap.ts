/**
 * 动态加载高德地图 API
 */
export function loadAMapScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    // 检查是否已经加载
    if (window.AMap && window.AMap.Map) {
      resolve()
      return
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

    // 设置全局回调
    ;(window as any).initAMap = () => {
      if (window.AMap && window.AMap.Map) {
        resolve()
        delete (window as any).initAMap
      } else {
        reject(new Error('高德地图加载失败'))
        delete (window as any).initAMap
      }
    }

    script.onerror = () => {
      reject(new Error('高德地图脚本加载失败，请检查网络连接或 API Key'))
      delete (window as any).initAMap
    }

    // 添加到页面
    document.head.appendChild(script)
  })
}


