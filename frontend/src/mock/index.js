/**
 * 模拟数据管理
 * 统一管理所有的模拟数据，便于开发和测试
 */

import { MESSAGE_TYPE } from '@/constants'

// 用户模拟数据
export const mockUser = {
  id: 1,
  username: '用户123',
  nickname: '自动化爱好者',
  avatar: '/static/my-avatar.png',
  phone: '138****8888',
  email: 'user@example.com',
  level: '普通用户',
  createTime: '2024-01-01',
  lastLoginTime: '2024-01-15 10:30:00'
}

// 消息模拟数据
export const mockMessages = [
  {
    id: 1,
    type: MESSAGE_TYPE.SYSTEM,
    senderName: '系统通知',
    senderAvatar: '/static/system-avatar.png',
    lastMessage: '欢迎使用自动化工具平台',
    lastTime: '2024-01-15 14:30',
    unreadCount: 1,
    isRead: false
  },
  {
    id: 2,
    type: MESSAGE_TYPE.SERVICE,
    senderName: '系统客服',
    senderAvatar: '/static/avatar3.png',
    lastMessage: '您好，有什么可以帮助您的吗？',
    lastTime: '2024-01-15 12:00',
    unreadCount: 1,
    isRead: false
  },
  {
    id: 3,
    type: MESSAGE_TYPE.SYSTEM,
    senderName: '更新通知',
    senderAvatar: '/static/system-avatar.png',
    lastMessage: '平台功能更新，新增多个实用工具',
    lastTime: '2024-01-14 16:45',
    unreadCount: 0,
    isRead: true
  }
]

// 工具使用记录数据（替代订单数据）
export const mockToolRecords = [
  {
    id: 'TOOL001',
    toolId: 1,
    toolName: 'Excel自动化处理工具',
    toolImage: '/static/goods1.png',
    category: 'excel',
    useTime: '2024-01-15 10:30:00',
    status: 'completed',
    remark: '数据处理完成'
  },
  {
    id: 'TOOL002',
    toolId: 2,
    toolName: 'Word文档批量处理',
    toolImage: '/static/goods2.png',
    category: 'word',
    useTime: '2024-01-14 15:20:00',
    status: 'completed',
    remark: '文档转换成功'
  }
]

// 工具展示数据
export const mockTools = [
  {
    id: 1,
    title: 'Excel自动化处理工具',
    subtitle: '高效办公，一键搞定复杂表格处理',
    tags: ['推荐', '实用'],
    category: 'excel',
    images: ['/static/goods1.png', '/static/goods2.png'],
    description: '这是一款功能强大的Excel自动化处理工具，支持批量数据处理、图表生成等功能。',
    features: ['支持批量数据处理', '一键生成各类图表', '智能公式计算'],
    rating: 4.8,
    downloadCount: 1580,
    version: 'v1.2.0'
  },
  {
    id: 2,
    title: 'Word文档批量处理',
    subtitle: '批量处理Word文档，提升工作效率',
    tags: ['推荐'],
    category: 'word',
    images: ['/static/goods2.png', '/static/goods1.png'],
    description: '专业的Word文档批量处理工具，支持格式转换、文档合并等功能。',
    features: ['批量格式转换', '文档合并拆分', '模板应用'],
    rating: 4.6,
    downloadCount: 920,
    version: 'v1.1.0'
  }
]

// 统计数据计算函数
export const calculateStats = () => {
  // 计算消息统计
  const messageStats = {
    total: 0,
    system: 0,
    service: 0
  }

  mockMessages.forEach(msg => {
    if (!msg.isRead && msg.unreadCount > 0) {
      messageStats.total += msg.unreadCount
      messageStats[msg.type] += msg.unreadCount
    }
  })

  // 计算工具使用统计
  const toolStats = {
    totalTools: mockTools.length,
    totalDownloads: mockTools.reduce((sum, tool) => sum + tool.downloadCount, 0),
    recentUse: mockToolRecords.length
  }

  return {
    message: messageStats,
    tool: toolStats
  }
}

// 导出所有模拟数据
export default {
  user: mockUser,
  messages: mockMessages,
  toolRecords: mockToolRecords,
  tools: mockTools,
  calculateStats
}
