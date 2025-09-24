/**
 * API 接口统一管理（聚合导出）
 * 拆分为资源模块后，在此进行统一 re-export 与默认导出，保持向后兼容
 */

export { userApi } from './user'
export { messageApi } from './message'
export { orderApi } from './order'
export { toolsApi } from './tools'

import userApi from './user'
import messageApi from './message'
import orderApi from './order'
import toolsApi from './tools'

// 默认导出聚合对象，兼容原有 import api from '@/api'
export default {
  user: userApi,
  message: messageApi,
  order: orderApi,
  tools: toolsApi,
}
