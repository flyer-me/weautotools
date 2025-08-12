/**
 * 订单管理组合式API
 * 处理订单相关的业务逻辑
 */

import { ref, computed } from 'vue'
import { orderApi } from '@/api'
import { ORDER_STATUS, ORDER_STATUS_TEXT } from '@/constants'
import { useGlobalBadge } from './useBadge'

/**
 * 订单管理Hook
 */
export function useOrder() {
  const orders = ref([])
  const loading = ref(false)
  const refreshing = ref(false)
  const hasMore = ref(false)
  
  // 获取全局徽章实例
  const { updateOrderBadge, decreaseOrderBadge, increaseOrderBadge } = useGlobalBadge()
  
  // 计算属性
  const ordersByStatus = computed(() => {
    const result = {
      all: orders.value,
      [ORDER_STATUS.PENDING]: [],
      [ORDER_STATUS.PAID]: [],
      [ORDER_STATUS.PROCESSING]: [],
      [ORDER_STATUS.SHIPPING]: [],
      [ORDER_STATUS.COMPLETED]: [],
      [ORDER_STATUS.CANCELLED]: [],
      [ORDER_STATUS.REFUNDING]: [],
      [ORDER_STATUS.REFUNDED]: []
    }
    
    orders.value.forEach(order => {
      if (result[order.status]) {
        result[order.status].push(order)
      }
    })
    
    return result
  })
  
  const orderCounts = computed(() => {
    const counts = {
      [ORDER_STATUS.PENDING]: 0,
      [ORDER_STATUS.PAID]: 0,
      [ORDER_STATUS.PROCESSING]: 0,
      [ORDER_STATUS.SHIPPING]: 0,
      [ORDER_STATUS.COMPLETED]: 0,
      [ORDER_STATUS.CANCELLED]: 0,
      [ORDER_STATUS.REFUNDING]: 0,
      [ORDER_STATUS.REFUNDED]: 0
    }
    
    orders.value.forEach(order => {
      counts[order.status]++
    })
    
    return counts
  })
  
  // 需要用户关注的订单数量
  const pendingOrderCount = computed(() => {
    return orderCounts.value[ORDER_STATUS.PENDING] +
           orderCounts.value[ORDER_STATUS.PROCESSING] +
           orderCounts.value[ORDER_STATUS.SHIPPING] +
           orderCounts.value[ORDER_STATUS.REFUNDING]
  })
  
  // 获取订单列表
  const getOrderList = async (status = 'all', refresh = false) => {
    if (refresh) {
      refreshing.value = true
    } else {
      loading.value = true
    }

    try {
      // 构建请求参数
      const params = {}
      if (status !== 'all') {
        params.status = status
      }

      // 调用API获取订单列表
      const response = await orderApi.getOrderList(params)

      if (response.statusCode === 200 && response.data) {
        orders.value = response.data.list || response.data
        hasMore.value = response.data.hasMore || false

        // 更新徽章数据
        updateBadgeFromOrders()

        return ordersByStatus.value[status] || []
      } else {
        throw new Error(response.data?.message || '获取订单列表失败')
      }
    } catch (error) {
      console.error('获取订单列表失败:', error)
      uni.showToast({
        title: error.message || '获取订单失败',
        icon: 'error'
      })
      return []
    } finally {
      loading.value = false
      refreshing.value = false
    }
  }
  
  // 获取订单详情
  const getOrderDetail = async (orderId) => {
    try {
      loading.value = true

      // 调用API获取订单详情
      const response = await orderApi.getOrderDetail(orderId)

      if (response.statusCode === 200 && response.data) {
        return response.data
      } else {
        throw new Error(response.data?.message || '订单不存在')
      }
    } catch (error) {
      console.error('获取订单详情失败:', error)
      uni.showToast({
        title: error.message || '获取订单详情失败',
        icon: 'error'
      })
      return null
    } finally {
      loading.value = false
    }
  }
  
  // 更新订单状态
  const updateOrderStatus = async (orderId, newStatus) => {
    try {
      // 调用API更新订单状态
      const response = await orderApi.updateOrderStatus(orderId, newStatus)

      if (response.statusCode === 200) {
        // 更新本地订单数据
        const orderIndex = orders.value.findIndex(o => o.id === orderId)
        if (orderIndex !== -1) {
          const order = orders.value[orderIndex]
          const oldStatus = order.status

          // 更新订单状态
          order.status = newStatus
          order.updateTime = new Date().toLocaleString()

          // 根据状态更新时间字段
          if (newStatus === ORDER_STATUS.PAID) {
            order.payTime = order.updateTime
          } else if (newStatus === ORDER_STATUS.SHIPPING) {
            order.deliveryTime = order.updateTime
          } else if (newStatus === ORDER_STATUS.COMPLETED) {
            order.completeTime = order.updateTime
          }

          // 更新徽章
          decreaseOrderBadge(oldStatus, 1)
          increaseOrderBadge(newStatus, 1)
        }

        return true
      } else {
        throw new Error(response.data?.message || '更新订单状态失败')
      }
    } catch (error) {
      console.error('更新订单状态失败:', error)
      uni.showToast({
        title: error.message || '操作失败',
        icon: 'error'
      })
      return false
    }
  }
  
  // 取消订单
  const cancelOrder = async (orderId) => {
    try {
      const response = await orderApi.cancelOrder(orderId)
      if (response.statusCode === 200) {
        await updateOrderStatus(orderId, ORDER_STATUS.CANCELLED)
        return true
      } else {
        throw new Error(response.data?.message || '取消订单失败')
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      uni.showToast({
        title: error.message || '取消订单失败',
        icon: 'error'
      })
      return false
    }
  }

  // 确认收货
  const confirmOrder = async (orderId) => {
    try {
      const response = await orderApi.confirmOrder(orderId)
      if (response.statusCode === 200) {
        await updateOrderStatus(orderId, ORDER_STATUS.COMPLETED)
        return true
      } else {
        throw new Error(response.data?.message || '确认收货失败')
      }
    } catch (error) {
      console.error('确认收货失败:', error)
      uni.showToast({
        title: error.message || '确认收货失败',
        icon: 'error'
      })
      return false
    }
  }

  // 申请退款
  const refundOrder = async (orderId, reason = '') => {
    try {
      const response = await orderApi.refundOrder(orderId, reason)
      if (response.statusCode === 200) {
        await updateOrderStatus(orderId, ORDER_STATUS.REFUNDING)
        return true
      } else {
        throw new Error(response.data?.message || '申请退款失败')
      }
    } catch (error) {
      console.error('申请退款失败:', error)
      uni.showToast({
        title: error.message || '申请退款失败',
        icon: 'error'
      })
      return false
    }
  }
  
  // 创建订单
  const createOrder = async (orderData) => {
    try {
      // 调用API创建订单
      const response = await orderApi.createOrder(orderData)

      if (response.statusCode === 200 && response.data) {
        const newOrder = response.data

        // 添加到本地订单列表
        orders.value.unshift(newOrder)

        // 更新徽章
        increaseOrderBadge(ORDER_STATUS.PENDING, 1)

        uni.showToast({
          title: '订单创建成功',
          icon: 'success'
        })

        return newOrder
      } else {
        throw new Error(response.data?.message || '创建订单失败')
      }
    } catch (error) {
      console.error('创建订单失败:', error)
      uni.showToast({
        title: error.message || '创建订单失败',
        icon: 'error'
      })
      return null
    }
  }
  
  // 删除订单
  const deleteOrder = async (orderId) => {
    try {
      // 调用API删除订单
      const response = await orderApi.deleteOrder(orderId)

      if (response.statusCode === 200) {
        const orderIndex = orders.value.findIndex(o => o.id === orderId)
        if (orderIndex !== -1) {
          const order = orders.value[orderIndex]

          // 更新徽章
          decreaseOrderBadge(order.status, 1)

          // 删除订单
          orders.value.splice(orderIndex, 1)
        }

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })

        return true
      } else {
        throw new Error(response.data?.message || '删除订单失败')
      }
    } catch (error) {
      console.error('删除订单失败:', error)
      uni.showToast({
        title: error.message || '删除失败',
        icon: 'error'
      })
      return false
    }
  }
  
  // 获取订单状态统计
  const getOrderStatusCounts = async () => {
    try {
      const response = await orderApi.getOrderStatusCounts()

      if (response.statusCode === 200 && response.data) {
        // 更新徽章数据
        Object.keys(response.data).forEach(status => {
          updateOrderBadge(status, response.data[status])
        })

        return response.data
      } else {
        throw new Error(response.data?.message || '获取订单统计失败')
      }
    } catch (error) {
      console.error('获取订单统计失败:', error)
      // 静默失败，不显示错误提示
      return {}
    }
  }

  // 从订单数据更新徽章
  const updateBadgeFromOrders = () => {
    const counts = orderCounts.value
    Object.keys(counts).forEach(status => {
      updateOrderBadge(status, counts[status])
    })
  }
  
  // 刷新订单列表
  const refreshOrders = async (status = 'all') => {
    return await getOrderList(status, true)
  }
  
  // 获取订单状态文本
  const getStatusText = (status) => {
    return ORDER_STATUS_TEXT[status] || '未知状态'
  }
  
  // 获取订单操作按钮
  const getOrderActions = (order) => {
    const actions = []
    
    switch (order.status) {
      case ORDER_STATUS.PENDING:
        actions.push({ type: 'pay', text: '立即支付', primary: true })
        actions.push({ type: 'cancel', text: '取消订单' })
        break
      case ORDER_STATUS.PROCESSING:
        actions.push({ type: 'contact', text: '联系商家' })
        break
      case ORDER_STATUS.SHIPPING:
        actions.push({ type: 'confirm', text: '确认收货', primary: true })
        actions.push({ type: 'contact', text: '联系商家' })
        break
      case ORDER_STATUS.COMPLETED:
        actions.push({ type: 'review', text: '评价', primary: true })
        actions.push({ type: 'rebuy', text: '再次购买' })
        break
      case ORDER_STATUS.REFUNDING:
        actions.push({ type: 'contact', text: '联系客服' })
        break
      default:
        break
    }
    
    return actions
  }
  
  return {
    // 状态
    orders,
    loading,
    refreshing,
    hasMore,

    // 计算属性
    ordersByStatus,
    orderCounts,
    pendingOrderCount,

    // 方法
    getOrderList,
    getOrderDetail,
    getOrderStatusCounts,
    updateOrderStatus,
    cancelOrder,
    confirmOrder,
    refundOrder,
    createOrder,
    deleteOrder,
    refreshOrders,
    getStatusText,
    getOrderActions,
    updateBadgeFromOrders
  }
}
