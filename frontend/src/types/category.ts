/**
 * 分类页面相关类型定义
 */

// 工具项接口
export interface ToolItem {
  /** 工具名称 */
  name: string
  /** 工具图标路径 */
  img: string
  /** 工具描述 */
  desc: string
  /** 搜索关键词 */
  keywords: string[]
  /** 路由路径 */
  route: string
  /** 唯一标识符（搜索时生成） */
  id?: string
  /** 所属分类名称（搜索时生成） */
  categoryName?: string
  /** 分类索引（搜索时生成） */
  categoryIndex?: number
  /** 工具索引（搜索时生成） */
  toolIndex?: number
}

// 分类接口
export interface Category {
  /** 分类名称 */
  name: string
  /** 分类图标 */
  icon: string
  /** 子工具列表 */
  sub: ToolItem[]
}

// 搜索建议项接口
export interface SearchSuggestion extends ToolItem {
  /** 唯一标识符 */
  id: string
  /** 所属分类名称 */
  categoryName: string
}

// 用户行为记录接口
export interface UserClickData {
  /** 工具名称 */
  toolName: string
  /** 分类名称 */
  categoryName: string
  /** 点击时间戳 */
  timestamp: string
  /** 搜索关键词（如果有） */
  searchKeyword: string | null
}

// 搜索状态接口
export interface SearchState {
  /** 搜索关键词 */
  keyword: string
  /** 是否处于搜索模式 */
  isSearchMode: boolean
  /** 是否显示搜索建议 */
  showSuggestions: boolean
  /** 搜索结果 */
  results: ToolItem[]
  /** 搜索建议 */
  suggestions: SearchSuggestion[]
}

// 分类页面状态接口
export interface CategoryPageState {
  /** 当前激活的侧边栏索引 */
  activeSidebar: number
  /** 搜索状态 */
  search: SearchState
  /** 分类列表 */
  categories: Category[]
}

// 搜索选项接口
export interface SearchOptions {
  /** 是否区分大小写 */
  caseSensitive?: boolean
  /** 最大结果数量 */
  maxResults?: number
  /** 搜索延迟（毫秒） */
  debounceDelay?: number
  /** 是否启用模糊搜索 */
  fuzzySearch?: boolean
}

// 性能监控数据接口
export interface PerformanceData {
  /** 搜索耗时（毫秒） */
  searchTime: number
  /** 结果数量 */
  resultCount: number
  /** 搜索关键词 */
  keyword: string
  /** 时间戳 */
  timestamp: number
}
