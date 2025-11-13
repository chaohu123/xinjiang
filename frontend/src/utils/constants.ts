// 文化资源类型
export const CULTURE_TYPES = [
  { label: '文章', value: 'article' },
  { label: '展品', value: 'exhibit' },
  { label: '视频', value: 'video' },
  { label: '音频', value: 'audio' },
] as const

// 地区列表
export const REGIONS = [
  '乌鲁木齐',
  '喀什',
  '伊犁',
  '吐鲁番',
  '阿克苏',
  '和田',
  '库尔勒',
  '昌吉',
  '其他',
] as const

// 路线主题
export const ROUTE_THEMES = [
  { label: '丝绸之路', value: 'silkRoad', icon: 'Route' },
  { label: '自然风光', value: 'nature', icon: 'Mountain' },
  { label: '文化体验', value: 'culture', icon: 'Reading' },
  { label: '美食之旅', value: 'food', icon: 'Food' },
] as const

// 活动类型
export const EVENT_TYPES = [
  { label: '展览', value: 'exhibition' },
  { label: '演出', value: 'performance' },
  { label: '工作坊', value: 'workshop' },
  { label: '旅游', value: 'tour' },
] as const

// 分页默认值
export const PAGINATION = {
  page: 1,
  size: 12,
} as const



