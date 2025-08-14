/**
 * 分类页面工具函数
 */

import type { Category, ToolItem, SearchSuggestion, PerformanceData } from '@/types/category'

/**
 * 创建所有工具的扁平化列表
 * @param categories 分类列表
 * @returns 扁平化的工具列表
 */
export function createFlatToolsList(categories: Category[]): ToolItem[] {
  const tools: ToolItem[] = []
  
  categories.forEach((category, categoryIndex) => {
    category.sub.forEach((tool, toolIndex) => {
      tools.push({
        ...tool,
        id: `${categoryIndex}-${toolIndex}`,
        categoryName: category.name,
        categoryIndex,
        toolIndex
      })
    })
  })
  
  return tools
}

/**
 * 执行搜索
 * @param tools 工具列表
 * @param keyword 搜索关键词
 * @param options 搜索选项
 * @returns 搜索结果和性能数据
 */
export function performSearch(
  tools: ToolItem[], 
  keyword: string,
  options: {
    caseSensitive?: boolean
    maxResults?: number
  } = {}
): { results: ToolItem[], performance: PerformanceData } {
  const startTime = performance.now()
  
  if (!keyword.trim()) {
    return {
      results: [],
      performance: {
        searchTime: 0,
        resultCount: 0,
        keyword: '',
        timestamp: Date.now()
      }
    }
  }
  
  const { caseSensitive = false, maxResults = 50 } = options
  const searchKeyword = caseSensitive ? keyword : keyword.toLowerCase()
  
  const results = tools.filter(tool => {
    const toolName = caseSensitive ? tool.name : tool.name.toLowerCase()
    const toolDesc = caseSensitive ? tool.desc : tool.desc.toLowerCase()
    const categoryName = caseSensitive ? tool.categoryName || '' : (tool.categoryName || '').toLowerCase()
    
    // 优先匹配工具名称
    if (toolName.includes(searchKeyword)) return true
    
    // 匹配关键词
    if (tool.keywords) {
      const keywords = caseSensitive ? tool.keywords : tool.keywords.map(k => k.toLowerCase())
      if (keywords.some(k => k.includes(searchKeyword))) return true
    }
    
    // 匹配描述
    if (toolDesc.includes(searchKeyword)) return true
    
    // 匹配分类名称
    if (categoryName.includes(searchKeyword)) return true
    
    return false
  })
  
  // 按匹配度排序
  const sortedResults = results.sort((a, b) => {
    const aName = caseSensitive ? a.name : a.name.toLowerCase()
    const bName = caseSensitive ? b.name : b.name.toLowerCase()
    
    const aNameMatch = aName.includes(searchKeyword)
    const bNameMatch = bName.includes(searchKeyword)
    
    // 名称匹配的优先级最高
    if (aNameMatch && !bNameMatch) return -1
    if (!aNameMatch && bNameMatch) return 1
    
    // 如果都匹配名称或都不匹配名称，按字母顺序排序
    return a.name.localeCompare(b.name)
  })
  
  const endTime = performance.now()
  const finalResults = maxResults > 0 ? sortedResults.slice(0, maxResults) : sortedResults
  
  return {
    results: finalResults,
    performance: {
      searchTime: endTime - startTime,
      resultCount: finalResults.length,
      keyword,
      timestamp: Date.now()
    }
  }
}

/**
 * 生成搜索建议
 * @param tools 工具列表
 * @param keyword 搜索关键词
 * @param maxSuggestions 最大建议数量
 * @returns 搜索建议列表
 */
export function generateSearchSuggestions(
  tools: ToolItem[], 
  keyword: string, 
  maxSuggestions: number = 5
): SearchSuggestion[] {
  if (!keyword || keyword.length < 2) {
    return []
  }
  
  const lowerKeyword = keyword.toLowerCase()
  const suggestions: SearchSuggestion[] = []
  
  for (const tool of tools) {
    if (suggestions.length >= maxSuggestions) break
    
    const toolName = tool.name.toLowerCase()
    const hasKeywordMatch = tool.keywords?.some(k => k.toLowerCase().includes(lowerKeyword))
    
    if (toolName.includes(lowerKeyword) || hasKeywordMatch) {
      suggestions.push({
        ...tool,
        id: tool.id || `${tool.categoryIndex}-${tool.toolIndex}`,
        categoryName: tool.categoryName || ''
      })
    }
  }
  
  return suggestions
}

/**
 * 防抖函数
 * @param func 要防抖的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T, 
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout | null = null
  
  return (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      func.apply(null, args)
    }, delay)
  }
}

/**
 * 记录用户行为
 * @param data 用户行为数据
 */
export function logUserBehavior(data: {
  toolName: string
  categoryName: string
  searchKeyword?: string
}): void {
  const logData = {
    ...data,
    timestamp: new Date().toISOString(),
    userAgent: navigator.userAgent,
    url: window.location.href
  }
  
  console.log('用户行为记录:', logData)
  
  // 这里可以发送到分析服务
  // analytics.track('tool_click', logData)
}

/**
 * 格式化搜索结果数量文本
 * @param count 结果数量
 * @returns 格式化后的文本
 */
export function formatResultCount(count: number): string {
  if (count === 0) return '未找到相关工具'
  if (count === 1) return '找到 1 个相关工具'
  return `找到 ${count} 个相关工具`
}

/**
 * 检查工具是否可用
 * @param tool 工具项
 * @returns 是否可用
 */
export function isToolAvailable(tool: ToolItem): boolean {
  return Boolean(tool.route && tool.route.trim())
}

/**
 * 获取工具的完整URL
 * @param tool 工具项
 * @returns 完整的URL路径
 */
export function getToolUrl(tool: ToolItem): string {
  if (!tool.route) return ''
  
  // 确保路径以 / 开头
  return tool.route.startsWith('/') ? tool.route : `/${tool.route}`
}

/**
 * 高亮搜索关键词
 * @param text 原始文本
 * @param keyword 搜索关键词
 * @returns 高亮后的HTML字符串
 */
export function highlightKeyword(text: string, keyword: string): string {
  if (!keyword) return text
  
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}
