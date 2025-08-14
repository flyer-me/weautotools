/**
 * API 接口统一管理
 * 定义所有的API接口，提供统一的请求方法
 */

import { get, post, put, del } from '@/utils/request'

// 用户相关API
export const userApi = {
  // 获取用户信息
  getUserInfo: () => get('/user/info'),

  // 更新用户信息
  updateUserInfo: (data) => put('/user/info', data),

  // 获取用户统计数据
  getUserStats: () => get('/user/stats')
}

// 消息相关API
export const messageApi = {
  // 获取消息列表
  getMessageList: (params) => get('/messages', params),

  // 获取未读消息数量
  getUnreadCount: () => get('/messages/unread-count'),

  // 标记消息为已读
  markAsRead: (messageId) => put(`/messages/${messageId}/read`),

  // 获取聊天记录
  getChatHistory: (chatId, params) => get(`/chats/${chatId}/messages`, params)
}

// 订单相关API
export const orderApi = {
  // 获取订单列表
  getOrderList: (params) => get('/orders', params),

  // 获取订单详情
  getOrderDetail: (orderId) => get(`/orders/${orderId}`),

  // 获取订单统计
  getOrderStats: () => get('/orders/stats'),

  // 获取订单状态统计（用于用户页面徽章显示）
  getOrderStatusCounts: () => get('/orders/status-counts'),

  // 创建订单
  createOrder: (data) => post('/orders', data),

  // 更新订单状态
  updateOrderStatus: (orderId, status) => put(`/orders/${orderId}/status`, { status }),

  // 取消订单
  cancelOrder: (orderId) => put(`/orders/${orderId}/cancel`),

  // 确认收货
  confirmOrder: (orderId) => put(`/orders/${orderId}/confirm`),

  // 申请退款
  refundOrder: (orderId, reason) => put(`/orders/${orderId}/refund`, { reason }),

  // 删除订单
  deleteOrder: (orderId) => del(`/orders/${orderId}`),

  // 支付订单
  payOrder: (orderId, paymentData) => post(`/orders/${orderId}/pay`, paymentData)
}

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

// 默认导出所有API
export default {
  user: userApi,
  message: messageApi,
  order: orderApi,
  tools: toolsApi
}
