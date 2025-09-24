import { get, post, put, del } from '@/utils/request'

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

export default orderApi
