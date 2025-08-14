/**
 * 图标配置文件
 * 统一管理项目中使用的图标，优先使用 uni-icons 内置图标
 */

// 分类图标映射
export const CATEGORY_ICONS = {
  'PDF与文档': 'paperclip',
  '图片工具': 'images', 
  '文件转换': 'loop',
  '数据工具': 'bars',
  '二维码工具': 'scan'
}

// 工具图标映射
export const TOOL_ICONS = {
  // PDF与文档工具
  'PDF转换': 'refresh',
  'PDF压缩': 'download',
  'PDF合并': 'plus',
  'PDF拆分': 'minus',
  '图片转PDF': 'image',
  'OCR文字识别': 'eye',
  
  // 图片工具
  '图片压缩': 'download-filled',
  '格式转换': 'refresh-filled',
  '批量加水印': 'color',
  '批量重命名': 'compose',
  
  // 文件转换工具
  '文档格式转换': 'paperclip',
  '电子书转换': 'contact',
  
  // 数据工具
  'JSON转换': 'gear',
  
  // 二维码工具
  '二维码生成': 'plus-filled',
  '二维码识别': 'search'
}

// 状态图标
export const STATUS_ICONS = {
  success: 'checkmarkempty',
  error: 'closeempty',
  warning: 'info',
  info: 'info-filled',
  loading: 'spinner-cycle'
}

// 操作图标
export const ACTION_ICONS = {
  add: 'plus',
  delete: 'trash',
  edit: 'compose',
  save: 'checkmarkempty',
  cancel: 'close',
  search: 'search',
  filter: 'tune',
  sort: 'bars',
  refresh: 'refresh',
  upload: 'upload',
  download: 'download',
  share: 'paperplane',
  copy: 'copy',
  settings: 'settings'
}

// 导航图标
export const NAVIGATION_ICONS = {
  home: 'home',
  back: 'back',
  forward: 'forward',
  up: 'up',
  down: 'down',
  left: 'left',
  right: 'right',
  menu: 'bars',
  close: 'close'
}

// 文件类型图标
export const FILE_TYPE_ICONS = {
  pdf: 'paperclip',
  doc: 'contact',
  docx: 'contact',
  xls: 'bars',
  xlsx: 'bars',
  ppt: 'image',
  pptx: 'image',
  txt: 'font',
  jpg: 'image',
  jpeg: 'image',
  png: 'image',
  gif: 'image',
  webp: 'image',
  svg: 'image',
  json: 'gear',
  xml: 'gear',
  yaml: 'gear',
  csv: 'bars',
  zip: 'folder-add',
  rar: 'folder-add',
  '7z': 'folder-add'
}

// 社交媒体图标
export const SOCIAL_ICONS = {
  wechat: 'weixin',
  weibo: 'weibo',
  qq: 'qq',
  moments: 'pyq'
}

// 获取分类图标
export function getCategoryIcon(categoryName) {
  return CATEGORY_ICONS[categoryName] || 'list'
}

// 获取工具图标
export function getToolIcon(toolName) {
  return TOOL_ICONS[toolName] || 'gear'
}

// 获取文件类型图标
export function getFileTypeIcon(fileExtension) {
  const ext = fileExtension.toLowerCase().replace('.', '')
  return FILE_TYPE_ICONS[ext] || 'paperclip'
}

// 获取状态图标
export function getStatusIcon(status) {
  return STATUS_ICONS[status] || 'info'
}

// 获取操作图标
export function getActionIcon(action) {
  return ACTION_ICONS[action] || 'gear'
}

// 图标颜色配置
export const ICON_COLORS = {
  primary: '#007aff',
  secondary: '#5856d6',
  success: '#34c759',
  warning: '#ff9500',
  error: '#ff3b30',
  info: '#5ac8fa',
  gray: '#8e8e93',
  lightGray: '#c7c7cc',
  darkGray: '#48484a'
}

// 图标尺寸配置
export const ICON_SIZES = {
  xs: 16,
  sm: 20,
  md: 24,
  lg: 32,
  xl: 48,
  xxl: 64
}

// 获取图标颜色
export function getIconColor(type = 'primary') {
  return ICON_COLORS[type] || ICON_COLORS.primary
}

// 获取图标尺寸
export function getIconSize(size = 'md') {
  return ICON_SIZES[size] || ICON_SIZES.md
}

// 图标主题配置
export const ICON_THEMES = {
  light: {
    primary: '#007aff',
    background: '#f0f8ff',
    border: '#e6f4ff'
  },
  dark: {
    primary: '#0a84ff',
    background: '#1c1c1e',
    border: '#38383a'
  }
}

// 获取主题图标样式
export function getIconTheme(theme = 'light') {
  return ICON_THEMES[theme] || ICON_THEMES.light
}

// 验证图标是否存在
export function isValidIcon(iconName) {
  const allIcons = [
    ...Object.values(CATEGORY_ICONS),
    ...Object.values(TOOL_ICONS),
    ...Object.values(STATUS_ICONS),
    ...Object.values(ACTION_ICONS),
    ...Object.values(NAVIGATION_ICONS),
    ...Object.values(FILE_TYPE_ICONS),
    ...Object.values(SOCIAL_ICONS)
  ]
  return allIcons.includes(iconName)
}

// 图标使用统计（开发环境）
export const ICON_USAGE_STATS = {
  // 这里可以记录图标使用频率，用于优化
}

// 记录图标使用
export function recordIconUsage(iconName) {
  if (process.env.NODE_ENV === 'development') {
    ICON_USAGE_STATS[iconName] = (ICON_USAGE_STATS[iconName] || 0) + 1
  }
}
