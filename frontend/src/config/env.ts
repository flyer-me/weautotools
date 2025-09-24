/**
 * 环境配置管理
 * 统一管理不同环境下的配置信息
 */

// 定义配置对象的接口
interface EnvConfig {
  API_BASE_URL: string;
  AUTH_AUTHORITY_URL: string;
  API_VERSION: string;
  API_TIMEOUT: number;
  DEBUG: boolean;
  LOG_LEVEL: 'debug' | 'info' | 'warn' | 'error';
}

// 环境类型
export const ENV_TYPES = {
  DEVELOPMENT: 'development',
  TESTING: 'testing',
  PRODUCTION: 'production'
} as const; // 使用 as const 确保键和值是字面量类型

type EnvType = typeof ENV_TYPES[keyof typeof ENV_TYPES];

const CURRENT_ENV: EnvType = process.env.NODE_ENV === 'production' ? ENV_TYPES.PRODUCTION : ENV_TYPES.DEVELOPMENT;

// 环境配置，并应用 EnvConfig 类型
const ENV_CONFIG: Record<EnvType, EnvConfig> = {
  [ENV_TYPES.DEVELOPMENT]: {
    API_BASE_URL: 'http://localhost:8080/api',
    AUTH_AUTHORITY_URL: 'http://localhost:8080',
    API_VERSION: '/v1',
    API_TIMEOUT: 10000,
    DEBUG: true,
    LOG_LEVEL: 'debug'
  },
  [ENV_TYPES.TESTING]: {
    API_BASE_URL: 'https://test-api.weautotools.com/api',
    AUTH_AUTHORITY_URL: 'https://test-api.weautotools.com',
    API_VERSION: '/v1',
    API_TIMEOUT: 8000,
    DEBUG: true,
    LOG_LEVEL: 'info'
  },
  [ENV_TYPES.PRODUCTION]: {
    API_BASE_URL: 'https://api.weautotools.com/api',
    AUTH_AUTHORITY_URL: 'https://api.weautotools.com',
    API_VERSION: '/v1',
    API_TIMEOUT: 10000,
    DEBUG: false,
    LOG_LEVEL: 'error'
  }
};

// 获取当前环境配置
export const getCurrentEnvConfig = (): EnvConfig => {
  return ENV_CONFIG[CURRENT_ENV] || ENV_CONFIG[ENV_TYPES.DEVELOPMENT];
};

// 获取API基础URL
export const getApiBaseUrl = (): string => {
  const config = getCurrentEnvConfig();
  return `${config.API_BASE_URL}${config.API_VERSION}`;
};

// 获取完整API URL
export const getApiUrl = (endpoint: string): string => {
  return `${getApiBaseUrl()}${endpoint}`;
};

// 获取配置项，并为 key 添加类型约束
export const getConfig = <K extends keyof EnvConfig>(key: K): EnvConfig[K] => {
  const config = getCurrentEnvConfig();
  return config[key];
};

// 是否为开发环境
export const isDevelopment = (): boolean => {
  return CURRENT_ENV === ENV_TYPES.DEVELOPMENT;
};

// 是否为生产环境
export const isProduction = (): boolean => {
  return CURRENT_ENV === ENV_TYPES.PRODUCTION;
};

// 导出当前环境
export const currentEnv: EnvType = CURRENT_ENV;

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
};