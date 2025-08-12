/**
 * 消息管理组合式API
 * 处理消息相关的业务逻辑
 */

import { ref, computed } from 'vue'
import { mockMessages } from '@/mock'
import { MESSAGE_TYPE } from '@/constants'
import { useGlobalBadge } from './useBadge'

/**
 * 消息管理Hook
 */
export function useMessage() {
  const messages = ref([...mockMessages])
  const loading = ref(false)
  const refreshing = ref(false)
  const hasMore = ref(false)
  
  // 获取全局徽章实例
  const { updateMessageBadge, decreaseMessageBadge, clearBadge } = useGlobalBadge()
  
  // 计算属性
  const messagesByType = computed(() => {
    const result = {
      all: messages.value,
      [MESSAGE_TYPE.CHAT]: [],
      [MESSAGE_TYPE.SYSTEM]: [],
      [MESSAGE_TYPE.SERVICE]: [],
      [MESSAGE_TYPE.ORDER]: [],
      [MESSAGE_TYPE.PROMOTION]: []
    }
    
    messages.value.forEach(msg => {
      if (result[msg.type]) {
        result[msg.type].push(msg)
      }
    })
    
    return result
  })
  
  const unreadCounts = computed(() => {
    const counts = {
      total: 0,
      [MESSAGE_TYPE.CHAT]: 0,
      [MESSAGE_TYPE.SYSTEM]: 0,
      [MESSAGE_TYPE.SERVICE]: 0,
      [MESSAGE_TYPE.ORDER]: 0,
      [MESSAGE_TYPE.PROMOTION]: 0
    }
    
    messages.value.forEach(msg => {
      if (!msg.isRead && msg.unreadCount > 0) {
        counts.total += msg.unreadCount
        counts[msg.type] += msg.unreadCount
      }
    })
    
    return counts
  })
  
  // 获取消息列表
  const getMessageList = async (type = 'all', refresh = false) => {
    if (refresh) {
      refreshing.value = true
    } else {
      loading.value = true
    }
    
    try {
      // 模拟API请求延迟
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 在实际项目中，这里会调用API
      // const response = await messageApi.getMessageList({ type })
      // messages.value = response.data
      
      // 更新徽章数据
      updateBadgeFromMessages()
      
      return messagesByType.value[type] || []
    } catch (error) {
      console.error('获取消息列表失败:', error)
      uni.showToast({
        title: '获取消息失败',
        icon: 'error'
      })
      return []
    } finally {
      loading.value = false
      refreshing.value = false
    }
  }
  
  // 标记消息为已读
  const markAsRead = async (messageId, type = MESSAGE_TYPE.CHAT) => {
    try {
      const messageIndex = messages.value.findIndex(msg => msg.id === messageId)
      if (messageIndex !== -1) {
        const message = messages.value[messageIndex]
        if (!message.isRead && message.unreadCount > 0) {
          // 更新本地数据
          message.isRead = true
          const unreadCount = message.unreadCount
          message.unreadCount = 0
          
          // 更新徽章
          decreaseMessageBadge('total', unreadCount)
          decreaseMessageBadge(type, unreadCount)
          
          // 在实际项目中，这里会调用API
          // await messageApi.markAsRead(messageId)
          
          console.log(`消息 ${messageId} 已标记为已读`)
        }
      }
    } catch (error) {
      console.error('标记消息已读失败:', error)
    }
  }
  
  // 标记所有消息为已读
  const markAllAsRead = async (type = 'all') => {
    try {
      const messagesToUpdate = type === 'all' 
        ? messages.value 
        : messages.value.filter(msg => msg.type === type)
      
      let totalCleared = 0
      const typeCleared = {}
      
      messagesToUpdate.forEach(msg => {
        if (!msg.isRead && msg.unreadCount > 0) {
          totalCleared += msg.unreadCount
          typeCleared[msg.type] = (typeCleared[msg.type] || 0) + msg.unreadCount
          
          msg.isRead = true
          msg.unreadCount = 0
        }
      })
      
      // 更新徽章
      if (type === 'all') {
        clearBadge('message')
      } else {
        Object.keys(typeCleared).forEach(msgType => {
          decreaseMessageBadge(msgType, typeCleared[msgType])
        })
      }
      
      console.log(`已标记 ${type} 类型的所有消息为已读`)
    } catch (error) {
      console.error('标记所有消息已读失败:', error)
    }
  }
  
  // 删除消息
  const deleteMessage = async (messageId) => {
    try {
      const messageIndex = messages.value.findIndex(msg => msg.id === messageId)
      if (messageIndex !== -1) {
        const message = messages.value[messageIndex]
        
        // 如果是未读消息，需要更新徽章
        if (!message.isRead && message.unreadCount > 0) {
          decreaseMessageBadge('total', message.unreadCount)
          decreaseMessageBadge(message.type, message.unreadCount)
        }
        
        // 删除消息
        messages.value.splice(messageIndex, 1)
        
        // 在实际项目中，这里会调用API
        // await messageApi.deleteMessage(messageId)
        
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    } catch (error) {
      console.error('删除消息失败:', error)
      uni.showToast({
        title: '删除失败',
        icon: 'error'
      })
    }
  }
  
  // 添加新消息（用于模拟接收新消息）
  const addNewMessage = (messageData) => {
    const newMessage = {
      id: Date.now(),
      type: messageData.type || MESSAGE_TYPE.CHAT,
      senderName: messageData.senderName || '新消息',
      senderAvatar: messageData.senderAvatar || '/static/default-avatar.png',
      lastMessage: messageData.content || '',
      lastTime: new Date().toLocaleString(),
      unreadCount: 1,
      isRead: false,
      ...messageData
    }
    
    messages.value.unshift(newMessage)
    
    // 更新徽章
    updateBadgeFromMessages()
    
    // 显示通知
    uni.showToast({
      title: '收到新消息',
      icon: 'none'
    })
  }
  
  // 从消息数据更新徽章
  const updateBadgeFromMessages = () => {
    const counts = unreadCounts.value
    Object.keys(counts).forEach(key => {
      updateMessageBadge(key, counts[key])
    })
  }
  
  // 刷新消息列表
  const refreshMessages = async (type = 'all') => {
    return await getMessageList(type, true)
  }
  
  return {
    // 状态
    messages,
    loading,
    refreshing,
    hasMore,
    
    // 计算属性
    messagesByType,
    unreadCounts,
    
    // 方法
    getMessageList,
    markAsRead,
    markAllAsRead,
    deleteMessage,
    addNewMessage,
    refreshMessages,
    updateBadgeFromMessages
  }
}
