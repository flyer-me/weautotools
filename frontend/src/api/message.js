import { get, put } from '@/utils/request'

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

export default messageApi
