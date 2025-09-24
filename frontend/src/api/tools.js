import { get } from '@/utils/request'

// 工具相关API
export const toolsApi = {
  // 获取工具列表
  getToolsList: (params) => get('/tools', params),

  // 获取工具详情
  getToolDetail: (toolId) => get(`/tools/${toolId}`),

  // 搜索工具
  searchTools: (keyword, params) => get('/tools/search', { keyword, ...params }),

  // 获取工具分类
  getToolCategories: () => get('/tools/categories'),

  // 获取工具使用记录
  getToolRecords: (params) => get('/tools/records', params)
}

export default toolsApi
