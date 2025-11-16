// 高德地图类型声明
declare namespace AMap {
  class Map {
    constructor(container: string | HTMLElement, opts?: MapOptions)
    setCenter(lnglat: [number, number] | LngLat): void
    setZoom(zoom: number): void
    setFitView(overlayList?: any[], immediately?: boolean, offsets?: number[]): void
    add(overlay: Marker | any): void
    remove(overlay: Marker | any): void
    destroy(): void
    on(event: string, callback: (e?: any) => void): void
    off(event: string, callback: (e?: any) => void): void
  }

  interface MapOptions {
    center?: [number, number] | LngLat
    zoom?: number
    viewMode?: string
    mapStyle?: string
  }

  class LngLat {
    constructor(lng: number, lat: number)
    getLng(): number
    getLat(): number
  }

  class Pixel {
    constructor(x: number, y: number)
    getX(): number
    getY(): number
  }

  class Bounds {
    constructor(southWest?: [number, number] | LngLat, northEast?: [number, number] | LngLat)
    extend(point: [number, number] | LngLat): void
    getNorthEast(): LngLat | null
    getSouthWest(): LngLat | null
  }

  class Marker {
    constructor(opts?: MarkerOptions)
    setPosition(position: [number, number] | LngLat): void
    getPosition(): LngLat
    setTitle(title: string): void
    setLabel(label: Label): void
    on(event: string, callback: (e?: any) => void): void
    off(event: string, callback: (e?: any) => void): void
  }

  interface MarkerOptions {
    position?: [number, number] | LngLat
    title?: string
    content?: string | HTMLElement
    icon?: string | Icon
    label?: Label
    offset?: Pixel | [number, number]
    zIndex?: number
  }

  interface Icon {
    size?: [number, number]
    image?: string
    imageOffset?: [number, number]
    imageSize?: [number, number]
  }

  interface Label {
    content?: string
    direction?: string
    offset?: [number, number]
  }

  class InfoWindow {
    constructor(opts?: InfoWindowOptions)
    setContent(content: string | HTMLElement): void
    open(map: Map, position?: [number, number] | LngLat): void
    close(): void
  }

  interface InfoWindowOptions {
    content?: string | HTMLElement
    position?: [number, number] | LngLat
    offset?: [number, number]
    size?: [number, number]
  }

  // 全局对象
  const event: {
    addListener(instance: any, eventName: string, handler: (e?: any) => void): void
    removeListener(instance: any, eventName: string, handler: (e?: any) => void): void
  }
}

// 扩展 Window 接口
declare global {
  interface Window {
    AMap: typeof AMap
    initAMap?: () => void
  }
}

