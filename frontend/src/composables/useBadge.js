/**
 * 红点徽章管理组合式API
 * 统一管理TabBar和页面中的红点显示逻辑
 */

import { ref, computed, reactive } from 'vue'
import { calculateStats } from '@/mock'

// 全局状态
const badgeState = reactive({
  // 消息相关徽章
  message: {
    total: 0,
    chat: 0,
    system: 0,
    service: 0,
    order: 0
  },
  
  // 订单相关徽章
  order: {
    pending: 0,      // 待付款
    processing: 0,   // 待交付
    shipping: 0,     // 待收货
    completed: 0,    // 待评价
    refunding: 0,    // 退款中
    cancelled: 0,    // 已取消
    refunded: 0      // 已退款
  },
  
  // 用户相关徽章
  user: {
    coupon: 0,       // 优惠券
    points: 0        // 积分变动提醒
  }
})

// 计算属性
const totalMessageBadge = computed(() => badgeState.message.total)
const totalOrderBadge = computed(() => {
  // 只计算需要用户关注的订单状态
  return badgeState.order.pending + 
         badgeState.order.processing + 
         badgeState.order.shipping + 
         badgeState.order.refunding
})

const totalUserBadge = computed(() => badgeState.user.points + badgeState.user.coupon)

const tabBarBadges = computed(() => ({
  message: totalMessageBadge.value,
  user: totalUserBadge.value
}))

/**
 * 红点徽章管理Hook
 */
export function useBadge() {
  
  // 初始化徽章数据
  const initBadges = () => {
    const stats = calculateStats()
    
    // 更新消息徽章
    badgeState.message = { ...stats.message }
    
    // 更新订单徽章
    badgeState.order = { ...stats.order }
    
    console.log('Badge initialized:', { message: badgeState.message, order: badgeState.order })
  }
  
  // 更新消息徽章
  const updateMessageBadge = (type, count) => {
    if (type === 'total') {
      badgeState.message.total = count
    } else if (badgeState.message.hasOwnProperty(type)) {
      badgeState.message[type] = count
      // 重新计算总数
      badgeState.message.total = Object.keys(badgeState.message)
        .filter(key => key !== 'total')
        .reduce((sum, key) => sum + badgeState.message[key], 0)
    }
  }
  
  // 更新订单徽章
  const updateOrderBadge = (status, count) => {
    if (badgeState.order.hasOwnProperty(status)) {
      badgeState.order[status] = count
    }
  }
  
  // 减少消息徽章
  const decreaseMessageBadge = (type = 'total', amount = 1) => {
    if (type === 'total') {
      badgeState.message.total = Math.max(0, badgeState.message.total - amount)
    } else if (badgeState.message.hasOwnProperty(type)) {
      badgeState.message[type] = Math.max(0, badgeState.message[type] - amount)
      badgeState.message.total = Math.max(0, badgeState.message.total - amount)
    }
  }
  
  // 减少订单徽章
  const decreaseOrderBadge = (status, amount = 1) => {
    if (badgeState.order.hasOwnProperty(status)) {
      badgeState.order[status] = Math.max(0, badgeState.order[status] - amount)
    }
  }
  
  // 增加消息徽章
  const increaseMessageBadge = (type = 'total', amount = 1) => {
    if (type === 'total') {
      badgeState.message.total += amount
    } else if (badgeState.message.hasOwnProperty(type)) {
      badgeState.message[type] += amount
      badgeState.message.total += amount
    }
  }
  
  // 增加订单徽章
  const increaseOrderBadge = (status, amount = 1) => {
    if (badgeState.order.hasOwnProperty(status)) {
      badgeState.order[status] += amount
    }
  }
  
  // 清空指定类型的徽章
  const clearBadge = (category, type = null) => {
    if (category === 'message') {
      if (type && badgeState.message.hasOwnProperty(type)) {
        const clearedAmount = badgeState.message[type]
        badgeState.message[type] = 0
        badgeState.message.total = Math.max(0, badgeState.message.total - clearedAmount)
      } else {
        badgeState.message = {
          total: 0,
          chat: 0,
          system: 0,
          service: 0,
          order: 0
        }
      }
    } else if (category === 'order') {
      if (type && badgeState.order.hasOwnProperty(type)) {
        badgeState.order[type] = 0
      } else {
        Object.keys(badgeState.order).forEach(key => {
          badgeState.order[key] = 0
        })
      }
    }
  }
  
  // 获取指定徽章数量
  const getBadgeCount = (category, type = null) => {
    if (category === 'message') {
      return type ? (badgeState.message[type] || 0) : badgeState.message.total
    } else if (category === 'order') {
      return type ? (badgeState.order[type] || 0) : totalOrderBadge.value
    } else if (category === 'tabbar') {
      return type ? (tabBarBadges.value[type] || 0) : tabBarBadges.value
    }
    return 0
  }
  
  return {
    // 状态
    badgeState,
    totalMessageBadge,
    totalOrderBadge,
    tabBarBadges,
    
    // 方法
    initBadges,
    updateMessageBadge,
    updateOrderBadge,
    decreaseMessageBadge,
    decreaseOrderBadge,
    increaseMessageBadge,
    increaseOrderBadge,
    clearBadge,
    getBadgeCount
  }
}

// 全局单例实例
let globalBadgeInstance = null

/**
 * 获取全局徽章实例
 */
export function useGlobalBadge() {
  if (!globalBadgeInstance) {
    globalBadgeInstance = useBadge()
    // 初始化数据
    globalBadgeInstance.initBadges()
  }
  return globalBadgeInstance
}
