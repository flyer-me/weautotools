import { get, put } from '@/utils/request'

// 用户相关API
export const userApi = {
  // 获取用户信息
  getUserInfo: () => get('/user/info'),

  // 更新用户信息
  updateUserInfo: (data) => put('/user/info', data),

  // 获取用户统计数据
  getUserStats: () => get('/user/stats')
}

export default userApi
