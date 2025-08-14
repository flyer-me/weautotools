/**
 * 常量定义
 * 统一管理项目中使用的常量
 */

// 订单状态常量
export const ORDER_STATUS = {
  PENDING: 'pending',           // 待付款
  PAID: 'paid',                // 已付款
  PROCESSING: 'processing',     // 处理中/待交付
  SHIPPING: 'shipping',         // 配送中/待收货
  COMPLETED: 'completed',       // 已完成/待评价
  CANCELLED: 'cancelled',       // 已取消
  REFUNDING: 'refunding',       // 退款中
  REFUNDED: 'refunded'          // 已退款
}

// 订单状态显示文本
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING]: '待付款',
  [ORDER_STATUS.PAID]: '已付款',
  [ORDER_STATUS.PROCESSING]: '待交付',
  [ORDER_STATUS.SHIPPING]: '待收货',
  [ORDER_STATUS.COMPLETED]: '待评价',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUNDING]: '退款中',
  [ORDER_STATUS.REFUNDED]: '已退款'
}

// 消息类型常量
export const MESSAGE_TYPE = {
  CHAT: 'chat',                 // 聊天消息
  SYSTEM: 'system',             // 系统消息
  SERVICE: 'service',           // 客服消息
  ORDER: 'order',               // 订单消息
  PROMOTION: 'promotion'        // 推广消息
}

// 消息类型显示文本
export const MESSAGE_TYPE_TEXT = {
  [MESSAGE_TYPE.CHAT]: '聊天',
  [MESSAGE_TYPE.SYSTEM]: '系统',
  [MESSAGE_TYPE.SERVICE]: '客服',
  [MESSAGE_TYPE.ORDER]: '订单',
  [MESSAGE_TYPE.PROMOTION]: '推广'
}

// 产品分类常量
export const GOODS_CATEGORY = {
  WORD: 'word',
  EXCEL: 'excel',
  PPT: 'ppt',
  PDF: 'pdf',
  WEB: 'web',
  DATA: 'data',
  AUTOMATION: 'automation'
}

// 产品分类显示文本
export const GOODS_CATEGORY_TEXT = {
  [GOODS_CATEGORY.WORD]: 'Word处理',
  [GOODS_CATEGORY.EXCEL]: 'Excel处理',
  [GOODS_CATEGORY.PPT]: 'PPT制作',
  [GOODS_CATEGORY.PDF]: 'PDF处理',
  [GOODS_CATEGORY.WEB]: 'Web自动化',
  [GOODS_CATEGORY.DATA]: '数据处理',
  [GOODS_CATEGORY.AUTOMATION]: '办公自动化'
}

// 页面路径常量
export const ROUTES = {
  // 主要页面
  CATEGORY: '/pages/category/category',
  MESSAGE: '/pages/message/message',
  USER: '/pages/user/user',

  // 产品相关
  PRODUCT_SHOWCASE: '/pages/product-showcase/product-showcase', // 产品展示页面（因审核规则隐藏）
  GOODS_DETAIL: '/pages/goods-detail/goods-detail',
  SEARCH: '/pages/search/search',
  
  // 订单相关
  ORDER_LIST: '/pages/order-list/order-list',
  ORDER_DETAIL: '/pages/order-detail/order-detail',
  ORDER_CONFIRM: '/pages/order-confirm/order-confirm',
  
  // 用户相关
  PROFILE: '/pages/profile/profile',
  ADDRESS: '/pages/address/address',
  COUPON: '/pages/coupon/coupon',
  POINTS: '/pages/points/points',
  
  // 消息相关
  CHAT_DETAIL: '/pages/chat-detail/chat-detail',

  // 其他
  HELP: '/pages/help/help',
  PAYMENT: '/pages/payment/payment',
  REVIEW: '/pages/review/review'
}

// TabBar配置
export const TAB_BAR_CONFIG = [
  {
    pagePath: 'pages/category/category',
    text: '工具',
    icon: 'list'
  },
  {
    pagePath: 'pages/user/user',
    text: '我的',
    icon: 'person'
  }
]

// 存储键名常量
export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  SEARCH_HISTORY: 'searchHistory',
  CART_ITEMS: 'cartItems',
  SETTINGS: 'settings'
}

// 默认配置
export const DEFAULT_CONFIG = {
  // 分页配置
  PAGE_SIZE: 20,
  
  // 图片配置
  DEFAULT_AVATAR: '/static/default-avatar.png',
  DEFAULT_GOODS_IMAGE: '/static/default-goods.png',
  
  // 动画配置
  ANIMATION_DURATION: 300,
  
  // 请求超时时间
  REQUEST_TIMEOUT: 10000
}
