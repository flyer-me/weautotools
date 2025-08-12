/**
 * 订单状态统一配置
 */

// 订单状态枚举
export const ORDER_STATUS = {
  PENDING: 'pending',        // 待付款
  PROCESSING: 'processing',  // 待交付
  SHIPPING: 'shipping',      // 待收货
  COMPLETED: 'completed',    // 已完成
  CANCELLED: 'cancelled',    // 已取消
  REFUND: 'refund'          // 退款/售后
}

// 订单状态文本映射
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING]: '待付款',
  [ORDER_STATUS.PROCESSING]: '待交付',
  [ORDER_STATUS.SHIPPING]: '待收货',
  [ORDER_STATUS.COMPLETED]: '评价',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUND]: '退款/售后'
}

// 订单状态图标映射（用于用户页面）
export const ORDER_STATUS_ICONS = {
  [ORDER_STATUS.PENDING]: 'wallet',
  [ORDER_STATUS.PROCESSING]: 'shop',
  [ORDER_STATUS.SHIPPING]: 'gift',
  [ORDER_STATUS.COMPLETED]: 'chat',
  [ORDER_STATUS.CANCELLED]: 'close',
  [ORDER_STATUS.REFUND]: 'help'
}

// 订单状态颜色映射
export const ORDER_STATUS_COLORS = {
  [ORDER_STATUS.PENDING]: '#faad14',
  [ORDER_STATUS.PROCESSING]: '#1890ff',
  [ORDER_STATUS.SHIPPING]: '#52c41a',
  [ORDER_STATUS.COMPLETED]: '#666',
  [ORDER_STATUS.CANCELLED]: '#ff4d4f',
  [ORDER_STATUS.REFUND]: '#722ed1'
}

// 获取状态文本
export const getStatusText = (status) => {
  return ORDER_STATUS_TEXT[status] || '未知状态'
}

// 获取状态图标
export const getStatusIcon = (status) => {
  return ORDER_STATUS_ICONS[status] || 'help'
}

// 获取状态颜色
export const getStatusColor = (status) => {
  return ORDER_STATUS_COLORS[status] || '#666'
}

// 用户页面订单状态配置（5个状态）
export const getUserOrderStatusConfig = () => [
  {
    icon: getStatusIcon(ORDER_STATUS.PENDING),
    text: getStatusText(ORDER_STATUS.PENDING),
    count: 0,
    status: ORDER_STATUS.PENDING
  },
  {
    icon: getStatusIcon(ORDER_STATUS.PROCESSING),
    text: getStatusText(ORDER_STATUS.PROCESSING),
    count: 0,
    status: ORDER_STATUS.PROCESSING
  },
  {
    icon: getStatusIcon(ORDER_STATUS.SHIPPING),
    text: getStatusText(ORDER_STATUS.SHIPPING),
    count: 0,
    status: ORDER_STATUS.SHIPPING
  },
  {
    icon: getStatusIcon(ORDER_STATUS.REFUND),
    text: getStatusText(ORDER_STATUS.REFUND),
    count: 0,
    status: ORDER_STATUS.REFUND
  },
  {
    icon: getStatusIcon(ORDER_STATUS.COMPLETED),
    text: getStatusText(ORDER_STATUS.COMPLETED),
    count: 0,
    status: ORDER_STATUS.COMPLETED
  }
]

// 订单列表页面状态标签配置（全部 + 5个状态 = 6个标签）
export const getOrderListStatusTabs = () => [
  { label: '全部', value: 'all', count: 0 },
  { label: getStatusText(ORDER_STATUS.PENDING), value: ORDER_STATUS.PENDING, count: 0 },
  { label: getStatusText(ORDER_STATUS.PROCESSING), value: ORDER_STATUS.PROCESSING, count: 0 },
  { label: getStatusText(ORDER_STATUS.SHIPPING), value: ORDER_STATUS.SHIPPING, count: 0 },
  { label: getStatusText(ORDER_STATUS.REFUND), value: ORDER_STATUS.REFUND, count: 0 },
  { label: getStatusText(ORDER_STATUS.COMPLETED), value: ORDER_STATUS.COMPLETED, count: 0 }
]

// 获取订单操作按钮
export const getOrderActions = (status) => {
  const actionMap = {
    [ORDER_STATUS.PENDING]: [
      { type: 'cancel', text: '取消订单' },
      { type: 'pay', text: '立即付款' }
    ],
    [ORDER_STATUS.PROCESSING]: [
      { type: 'contact', text: '联系客服' }
    ],
    [ORDER_STATUS.SHIPPING]: [
      { type: 'logistics', text: '查看物流' },
      { type: 'confirm', text: '确认收货' }
    ],
    [ORDER_STATUS.COMPLETED]: [
      { type: 'review', text: '评价' },
      { type: 'rebuy', text: '再次购买' }
    ],
    [ORDER_STATUS.REFUND]: [
      { type: 'contact', text: '联系客服' },
      { type: 'rebuy', text: '再次购买' }
    ]
  }
  return actionMap[status] || []
}
