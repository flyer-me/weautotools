/**
 * 环境配置管理
 * 统一管理不同环境下的配置信息
 */

// 环境类型
export const ENV_TYPES = {
  DEVELOPMENT: 'development',
  TESTING: 'testing',
  PRODUCTION: 'production'
}

const CURRENT_ENV = process.env.NODE_ENV === 'production' ? ENV_TYPES.PRODUCTION : ENV_TYPES.DEVELOPMENT

// 环境配置
const ENV_CONFIG = {
  [ENV_TYPES.DEVELOPMENT]: {
    API_BASE_URL: 'https://dev-api.weautotools.com',
    API_VERSION: '/v1',
    API_TIMEOUT: 10000,
    DEBUG: true,
    LOG_LEVEL: 'debug'
  },
  [ENV_TYPES.TESTING]: {
    API_BASE_URL: 'https://test-api.weautotools.com',
    API_VERSION: '/v1',
    API_TIMEOUT: 8000,
    DEBUG: true,
    LOG_LEVEL: 'info'
  },
  [ENV_TYPES.PRODUCTION]: {
    API_BASE_URL: 'https://api.weautotools.com',
    API_VERSION: '/v1',
    API_TIMEOUT: 10000,
    DEBUG: false,
    LOG_LEVEL: 'error'
  }
}

// 获取当前环境配置
export const getCurrentEnvConfig = () => {
  return ENV_CONFIG[CURRENT_ENV] || ENV_CONFIG[ENV_TYPES.DEVELOPMENT]
}

// 获取API基础URL
export const getApiBaseUrl = () => {
  const config = getCurrentEnvConfig()
  return `${config.API_BASE_URL}${config.API_VERSION}`
}

// 获取完整API URL
export const getApiUrl = (endpoint) => {
  return `${getApiBaseUrl()}${endpoint}`
}

// 获取配置项
export const getConfig = (key) => {
  const config = getCurrentEnvConfig()
  return config[key]
}

// 是否为开发环境
export const isDevelopment = () => {
  return CURRENT_ENV === ENV_TYPES.DEVELOPMENT
}

// 是否为生产环境
export const isProduction = () => {
  return CURRENT_ENV === ENV_TYPES.PRODUCTION
}

// 导出当前环境
export const currentEnv = CURRENT_ENV

// 导出默认配置
export default {
  ENV_TYPES,
  currentEnv,
  getCurrentEnvConfig,
  getApiBaseUrl,
  getApiUrl,
  getConfig,
  isDevelopment,
  isProduction
}
