/**
 * 统一请求工具类
 * 提供统一的HTTP请求封装，包含错误处理、拦截器等功能
 */

import { getApiUrl, getConfig, isDevelopment } from '@/config/env'

// 请求状态码
export const HTTP_STATUS = {
  SUCCESS: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503
}

// 错误消息映射
const ERROR_MESSAGES = {
  [HTTP_STATUS.BAD_REQUEST]: '请求参数错误',
  [HTTP_STATUS.UNAUTHORIZED]: '未授权，请重新登录',
  [HTTP_STATUS.FORBIDDEN]: '拒绝访问',
  [HTTP_STATUS.NOT_FOUND]: '请求的资源不存在',
  [HTTP_STATUS.INTERNAL_SERVER_ERROR]: '服务器内部错误',
  [HTTP_STATUS.BAD_GATEWAY]: '网关错误',
  [HTTP_STATUS.SERVICE_UNAVAILABLE]: '服务不可用'
}

/**
 * 请求拦截器
 * @param {Object} config 请求配置
 * @returns {Object} 处理后的配置
 */
const requestInterceptor = (config) => {
  // 添加认证头
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  }

  // 开发环境日志
  if (isDevelopment()) {
    console.log('Request:', config)
  }

  return config
}

/**
 * 响应拦截器
 * @param {Object} response 响应对象
 * @returns {Object} 处理后的响应
 */
const responseInterceptor = (response) => {
  const { statusCode, data } = response

  // 开发环境日志
  if (isDevelopment()) {
    console.log('Response:', response)
  }

  // 成功响应
  if (statusCode >= 200 && statusCode < 300) {
    return Promise.resolve(data)
  }

  // 错误响应
  const errorMessage = ERROR_MESSAGES[statusCode] || `请求失败 (${statusCode})`
  
  // 401 未授权，清除token并跳转登录
  if (statusCode === HTTP_STATUS.UNAUTHORIZED) {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    // 可以在这里添加跳转到登录页的逻辑
  }

  return Promise.reject(new Error(errorMessage))
}

/**
 * 统一请求方法
 * @param {string} url 请求地址
 * @param {Object} options 请求选项
 * @returns {Promise} 请求Promise
 */
export const request = (url, options = {}) => {
  // 构建完整URL
  const fullUrl = url.startsWith('http') ? url : getApiUrl(url)
  
  // 默认配置
  const defaultConfig = {
    url: fullUrl,
    method: 'GET',
    timeout: getConfig('API_TIMEOUT') || 10000,
    header: {
      'Content-Type': 'application/json'
    }
  }

  // 合并配置
  const config = { ...defaultConfig, ...options }
  
  // 请求拦截
  const interceptedConfig = requestInterceptor(config)

  return new Promise((resolve, reject) => {
    uni.request({
      ...interceptedConfig,
      success: (response) => {
        responseInterceptor(response)
          .then(resolve)
          .catch(reject)
      },
      fail: (error) => {
        console.error('Request failed:', error)
        reject(new Error('网络请求失败'))
      }
    })
  })
}

/**
 * GET请求
 * @param {string} url 请求地址
 * @param {Object} params 查询参数
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
export const get = (url, params = {}, options = {}) => {
  return request(url, {
    method: 'GET',
    data: params,
    ...options
  })
}

/**
 * POST请求
 * @param {string} url 请求地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
export const post = (url, data = {}, options = {}) => {
  return request(url, {
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT请求
 * @param {string} url 请求地址
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
export const put = (url, data = {}, options = {}) => {
  return request(url, {
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE请求
 * @param {string} url 请求地址
 * @param {Object} options 其他选项
 * @returns {Promise} 请求Promise
 */
export const del = (url, options = {}) => {
  return request(url, {
    method: 'DELETE',
    ...options
  })
}

// 导出默认请求方法
export default {
  request,
  get,
  post,
  put,
  delete: del,
  HTTP_STATUS
}
