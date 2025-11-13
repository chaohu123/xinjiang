/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'mapbox-gl' {
  export * from 'mapbox-gl'
}

declare module 'video.js' {
  export * from 'video.js'
}

declare module 'photoswipe' {
  export * from 'photoswipe'
}


