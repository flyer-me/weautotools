/**
 * 模拟数据管理
 * 统一管理所有的模拟数据，便于开发和测试
 */

import { ORDER_STATUS, MESSAGE_TYPE } from '@/constants'

// 用户模拟数据
export const mockUser = {
  id: 1,
  username: '用户123',
  nickname: '自动化爱好者',
  avatar: '/static/my-avatar.png',
  phone: '138****8888',
  email: 'user@example.com',
  level: 'VIP',
  points: 2580,
  balance: 1299.50,
  couponCount: 10,
  createTime: '2024-01-01',
  lastLoginTime: '2024-01-15 10:30:00'
}

// 消息模拟数据
export const mockMessages = [
  {
    id: 1,
    type: MESSAGE_TYPE.CHAT,
    senderName: '商家小助手',
    senderAvatar: '/static/avatar1.png',
    lastMessage: '您好，有什么可以帮助您的吗？',
    lastTime: '2024-01-15 14:30',
    unreadCount: 2,
    isRead: false
  },
  {
    id: 2,
    type: MESSAGE_TYPE.SYSTEM,
    senderName: '系统消息',
    senderAvatar: '/static/system-avatar.png',
    lastMessage: '您的订单已发货，请注意查收',
    lastTime: '2024-01-15 12:00',
    unreadCount: 1,
    isRead: false
  },
  {
    id: 3,
    type: MESSAGE_TYPE.CHAT,
    senderName: '开发者工具',
    senderAvatar: '/static/avatar2.png',
    lastMessage: '感谢您的购买，如有问题请联系我们',
    lastTime: '2024-01-14 16:45',
    unreadCount: 0,
    isRead: true
  },
  {
    id: 4,
    type: MESSAGE_TYPE.ORDER,
    senderName: '订单通知',
    senderAvatar: '/static/system-avatar.png',
    lastMessage: '您有新的订单需要处理',
    lastTime: '2024-01-14 09:20',
    unreadCount: 1,
    isRead: false
  },
  {
    id: 5,
    type: MESSAGE_TYPE.SERVICE,
    senderName: '客服中心',
    senderAvatar: '/static/avatar3.png',
    lastMessage: '您的问题已解决，感谢您的耐心等待',
    lastTime: '2024-01-13 18:30',
    unreadCount: 1,
    isRead: false
  }
]

// 订单模拟数据
export const mockOrders = [
  {
    id: 'ORD001',
    goodsId: 1,
    goodsName: 'Excel自动化处理工具',
    goodsImage: '/static/goods1.png',
    price: 299,
    quantity: 1,
    totalAmount: 299,
    status: ORDER_STATUS.PENDING,
    createTime: '2024-01-15 10:30:00',
    payTime: null,
    deliveryTime: null,
    completeTime: null,
    remark: '请尽快处理'
  },
  {
    id: 'ORD002',
    goodsId: 2,
    goodsName: 'Word文档批量处理',
    goodsImage: '/static/goods2.png',
    price: 199,
    quantity: 2,
    totalAmount: 398,
    status: ORDER_STATUS.PROCESSING,
    createTime: '2024-01-14 15:20:00',
    payTime: '2024-01-14 15:25:00',
    deliveryTime: null,
    completeTime: null,
    remark: ''
  },
  {
    id: 'ORD003',
    goodsId: 3,
    goodsName: 'PDF转换工具包',
    goodsImage: '/static/goods1.png',
    price: 159,
    quantity: 1,
    totalAmount: 159,
    status: ORDER_STATUS.SHIPPING,
    createTime: '2024-01-13 09:15:00',
    payTime: '2024-01-13 09:20:00',
    deliveryTime: '2024-01-13 14:30:00',
    completeTime: null,
    remark: ''
  },
  {
    id: 'ORD004',
    goodsId: 4,
    goodsName: 'Web自动化脚本',
    goodsImage: '/static/goods2.png',
    price: 399,
    quantity: 1,
    totalAmount: 399,
    status: ORDER_STATUS.SHIPPING,
    createTime: '2024-01-12 16:45:00',
    payTime: '2024-01-12 16:50:00',
    deliveryTime: '2024-01-13 10:00:00',
    completeTime: null,
    remark: '加急处理'
  },
  {
    id: 'ORD005',
    goodsId: 5,
    goodsName: '数据分析工具',
    goodsImage: '/static/goods1.png',
    price: 259,
    quantity: 1,
    totalAmount: 259,
    status: ORDER_STATUS.COMPLETED,
    createTime: '2024-01-10 11:20:00',
    payTime: '2024-01-10 11:25:00',
    deliveryTime: '2024-01-10 16:30:00',
    completeTime: '2024-01-11 09:15:00',
    remark: ''
  },
  {
    id: 'ORD006',
    goodsId: 6,
    goodsName: '办公自动化套件',
    goodsImage: '/static/goods2.png',
    price: 499,
    quantity: 1,
    totalAmount: 499,
    status: ORDER_STATUS.PENDING,
    createTime: '2024-01-15 16:20:00',
    payTime: null,
    deliveryTime: null,
    completeTime: null,
    remark: ''
  },
  {
    id: 'ORD007',
    goodsId: 7,
    goodsName: '表格处理专家',
    goodsImage: '/static/goods1.png',
    price: 189,
    quantity: 3,
    totalAmount: 567,
    status: ORDER_STATUS.SHIPPING,
    createTime: '2024-01-14 08:30:00',
    payTime: '2024-01-14 08:35:00',
    deliveryTime: '2024-01-14 14:20:00',
    completeTime: null,
    remark: '团购订单'
  }
]

// 商品模拟数据
export const mockGoods = [
  {
    id: 1,
    title: 'Excel自动化处理工具',
    subtitle: '高效办公，一键搞定复杂表格处理',
    price: 299,
    originPrice: 399,
    tags: ['热销', '限时优惠'],
    category: 'excel',
    images: ['/static/goods1.png', '/static/goods2.png'],
    description: '这是一款功能强大的Excel自动化处理工具...',
    features: ['支持批量数据处理', '一键生成各类图表', '智能公式计算'],
    rating: 4.8,
    reviewCount: 128,
    salesCount: 1580
  },
  {
    id: 2,
    title: 'Word文档批量处理',
    subtitle: '批量处理Word文档，提升工作效率',
    price: 199,
    originPrice: 299,
    tags: ['推荐'],
    category: 'word',
    images: ['/static/goods2.png', '/static/goods1.png'],
    description: '专业的Word文档批量处理工具...',
    features: ['批量格式转换', '文档合并拆分', '模板应用'],
    rating: 4.6,
    reviewCount: 89,
    salesCount: 920
  }
]

// 统计数据计算函数
export const calculateStats = () => {
  // 计算消息统计
  const messageStats = {
    total: 0,
    chat: 0,
    system: 0,
    service: 0,
    order: 0
  }
  
  mockMessages.forEach(msg => {
    if (!msg.isRead && msg.unreadCount > 0) {
      messageStats.total += msg.unreadCount
      messageStats[msg.type] += msg.unreadCount
    }
  })
  
  // 计算订单统计
  const orderStats = {
    pending: 0,
    processing: 0,
    shipping: 0,
    completed: 0,
    cancelled: 0,
    refunding: 0,
    refunded: 0
  }
  
  mockOrders.forEach(order => {
    orderStats[order.status]++
  })
  
  return {
    message: messageStats,
    order: orderStats
  }
}

// 导出所有模拟数据
export default {
  user: mockUser,
  messages: mockMessages,
  orders: mockOrders,
  goods: mockGoods,
  calculateStats
}
