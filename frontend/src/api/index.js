/**
 * API 接口统一管理
 * 定义所有的API接口，提供统一的请求方法
 */

// 基础配置
const BASE_URL = 'https://api.weautomarket.com'
const API_VERSION = '/v1'

// 请求拦截器
const request = (url, options = {}) => {
  const fullUrl = `${BASE_URL}${API_VERSION}${url}`
  
  const defaultOptions = {
    timeout: 10000,
    header: {
      'Content-Type': 'application/json',
      'Authorization': uni.getStorageSync('token') || ''
    }
  }
  
  return uni.request({
    url: fullUrl,
    ...defaultOptions,
    ...options
  })
}

// 用户相关API
export const userApi = {
  // 获取用户信息
  getUserInfo: () => request('/user/info'),
  
  // 更新用户信息
  updateUserInfo: (data) => request('/user/info', {
    method: 'PUT',
    data
  }),
  
  // 获取用户统计数据
  getUserStats: () => request('/user/stats')
}

// 消息相关API
export const messageApi = {
  // 获取消息列表
  getMessageList: (params) => request('/messages', {
    method: 'GET',
    data: params
  }),
  
  // 获取未读消息数量
  getUnreadCount: () => request('/messages/unread-count'),
  
  // 标记消息为已读
  markAsRead: (messageId) => request(`/messages/${messageId}/read`, {
    method: 'PUT'
  }),
  
  // 获取聊天记录
  getChatHistory: (chatId, params) => request(`/chats/${chatId}/messages`, {
    method: 'GET',
    data: params
  })
}

// 订单相关API
export const orderApi = {
  // 获取订单列表
  getOrderList: (params) => request('/orders', {
    method: 'GET',
    data: params
  }),

  // 获取订单详情
  getOrderDetail: (orderId) => request(`/orders/${orderId}`),

  // 获取订单统计
  getOrderStats: () => request('/orders/stats'),

  // 获取订单状态统计（用于用户页面徽章显示）
  getOrderStatusCounts: () => request('/orders/status-counts'),

  // 创建订单
  createOrder: (data) => request('/orders', {
    method: 'POST',
    data
  }),

  // 更新订单状态
  updateOrderStatus: (orderId, status) => request(`/orders/${orderId}/status`, {
    method: 'PUT',
    data: { status }
  }),

  // 取消订单
  cancelOrder: (orderId) => request(`/orders/${orderId}/cancel`, {
    method: 'PUT'
  }),

  // 确认收货
  confirmOrder: (orderId) => request(`/orders/${orderId}/confirm`, {
    method: 'PUT'
  }),

  // 申请退款
  refundOrder: (orderId, reason) => request(`/orders/${orderId}/refund`, {
    method: 'PUT',
    data: { reason }
  }),

  // 删除订单
  deleteOrder: (orderId) => request(`/orders/${orderId}`, {
    method: 'DELETE'
  }),

  // 支付订单
  payOrder: (orderId, paymentData) => request(`/orders/${orderId}/pay`, {
    method: 'POST',
    data: paymentData
  })
}

// 商品相关API
export const goodsApi = {
  // 获取商品列表
  getGoodsList: (params) => request('/goods', {
    method: 'GET',
    data: params
  }),
  
  // 获取商品详情
  getGoodsDetail: (goodsId) => request(`/goods/${goodsId}`),
  
  // 搜索商品
  searchGoods: (keyword, params) => request('/goods/search', {
    method: 'GET',
    data: { keyword, ...params }
  }),
  
  // 获取商品分类
  getCategories: () => request('/goods/categories')
}

// 默认导出所有API
export default {
  user: userApi,
  message: messageApi,
  order: orderApi,
  goods: goodsApi
}
