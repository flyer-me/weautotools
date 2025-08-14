/**
 * 功能开关配置
 * 用于控制应用中各种功能的启用/禁用状态
 * 便于符合微信个人开发者审核要求，同时保证功能可快速恢复
 */

// 功能开关配置
export const FEATURE_FLAGS = {
  // 支付相关功能
  PAYMENT: {
    enabled: false,  // 是否启用支付功能
    reason: '微信个人开发者限制 - 不允许支付功能'
  },
  
  // 交易相关功能
  TRADING: {
    enabled: false,  // 是否启用交易功能（下单、购买等）
    reason: '微信个人开发者限制 - 不允许交易功能'
  },
  
  // 用户间沟通功能
  USER_CHAT: {
    enabled: false,  // 是否启用用户间聊天功能
    reason: '微信个人开发者限制 - 不允许用户间沟通'
  },
  
  // 系统客服功能（保留）
  SYSTEM_SERVICE: {
    enabled: true,   // 保留系统客服功能
    reason: '符合微信个人开发者要求'
  },
  
  // 商品展示功能（保留）
  GOODS_DISPLAY: {
    enabled: true,   // 保留商品展示功能
    reason: '符合微信个人开发者要求'
  },
  
  // 订单查看功能（保留，但禁用操作）
  ORDER_VIEW: {
    enabled: true,   // 保留订单查看功能
    reason: '符合微信个人开发者要求'
  },
  
  // 订单操作功能
  ORDER_ACTIONS: {
    enabled: false,  // 禁用订单操作功能（支付、取消、确认等）
    reason: '微信个人开发者限制 - 不允许交易操作'
  }
}

// 检查功能是否启用
export const isFeatureEnabled = (featureName) => {
  const feature = FEATURE_FLAGS[featureName]
  return feature ? feature.enabled : false
}

// 获取功能禁用原因
export const getFeatureDisabledReason = (featureName) => {
  const feature = FEATURE_FLAGS[featureName]
  return feature ? feature.reason : '功能未定义'
}

// 显示功能禁用提示
export const showFeatureDisabledToast = (featureName) => {
  const reason = getFeatureDisabledReason(featureName)
  uni.showToast({
    title: '功能暂不可用',
    icon: 'none',
    duration: 2000
  })
  console.log(`功能 ${featureName} 已禁用: ${reason}`)
}

// 页面路由控制
export const DISABLED_ROUTES = [
  '/pages/payment/payment',      // 支付页面
  '/pages/order-confirm/order-confirm',  // 订单确认页面
  '/pages/review/review'         // 评价页面
]

// 检查路由是否被禁用
export const isRouteDisabled = (route) => {
  return DISABLED_ROUTES.includes(route)
}

// 处理被禁用的路由跳转
export const handleDisabledRoute = (route) => {
  console.log(`路由 ${route} 已被禁用`)
  showFeatureDisabledToast('TRADING')
  return false
}

// 开发模式配置（用于开发时快速切换）
export const DEV_MODE = {
  // 是否为开发模式
  enabled: false,

  // 开发模式下的功能开关覆盖（禁用限制功能）
  overrides: {
    PAYMENT: { enabled: false },
    TRADING: { enabled: false },
    USER_CHAT: { enabled: false },
    ORDER_ACTIONS: { enabled: false }
  },

  // 密码验证配置
  auth: {
    // 是否已通过密码验证
    authenticated: false,
    // 验证时间戳
    authTime: null,
    // 验证有效期（毫秒）24小时
    authExpiry: 24 * 60 * 60 * 1000
  }
}

// 获取最终的功能状态（考虑开发模式）
export const getFinalFeatureState = (featureName) => {
  // 开发模式下，如果功能在覆盖列表中，则使用覆盖状态
  if (DEV_MODE.enabled && DEV_MODE.auth.authenticated && isDevModeValid() && DEV_MODE.overrides[featureName]) {
    return DEV_MODE.overrides[featureName].enabled
  }
  // 否则使用默认的功能状态
  return isFeatureEnabled(featureName)
}

// 检查功能是否可以点击（开发模式下显示但禁用）
export const isFeatureClickable = (featureName) => {
  // 如果功能本身是启用的，则可以点击
  if (isFeatureEnabled(featureName)) {
    return true
  }

  // 如果在开发模式下且功能被禁用，则不可点击但显示
  if (DEV_MODE.enabled && DEV_MODE.auth.authenticated && isDevModeValid() && DEV_MODE.overrides[featureName]) {
    return false
  }

  return false
}

// 处理被禁用功能的点击
export const handleDisabledFeatureClick = (featureName) => {
  const feature = FEATURE_FLAGS[featureName]
  const reason = feature ? feature.reason : '功能未定义'

  uni.showModal({
    title: '功能限制',
    content: `${reason}\n\n当前未开放此功能。`,
    showCancel: false,
    confirmText: '我知道了'
  })
}

// 生成本地密码（当前日期8位数字）
export const generateLocalPassword = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}${month}${day}`
}

// 验证开发者密码
export const verifyDevPassword = async (password) => {
  try {
    // 首先尝试API验证
    const response = await uni.request({
      url: 'https://api.weautomarket.com/v1/dev/verify',
      method: 'POST',
      data: { password },
      timeout: 3000,
      header: {
        'Content-Type': 'application/json'
      }
    })

    if (response.statusCode === 200 && response.data?.success) {
      return { success: true, source: 'api' }
    }
  } catch (error) {
    console.log('API验证失败，使用本地验证:', error)
  }

  // API不可用时使用本地密码验证
  const localPassword = generateLocalPassword()
  if (password === localPassword) {
    return { success: true, source: 'local' }
  }

  return { success: false, source: 'none' }
}

// 启用开发模式
export const enableDevMode = async (password) => {
  const result = await verifyDevPassword(password)

  if (result.success) {
    DEV_MODE.enabled = true
    DEV_MODE.auth.authenticated = true
    DEV_MODE.auth.authTime = Date.now()

    // 保存到本地存储
    uni.setStorageSync('dev_mode_auth', {
      authenticated: true,
      authTime: DEV_MODE.auth.authTime,
      source: result.source
    })

    return { success: true, source: result.source }
  }

  return { success: false, message: '密码错误' }
}

// 检查开发模式是否有效
export const isDevModeValid = () => {
  if (!DEV_MODE.auth.authenticated || !DEV_MODE.auth.authTime) {
    return false
  }

  const now = Date.now()
  const elapsed = now - DEV_MODE.auth.authTime

  if (elapsed > DEV_MODE.auth.authExpiry) {
    // 验证已过期
    disableDevMode()
    return false
  }

  return true
}

// 禁用开发模式
export const disableDevMode = () => {
  DEV_MODE.enabled = false
  DEV_MODE.auth.authenticated = false
  DEV_MODE.auth.authTime = null

  // 清除本地存储
  uni.removeStorageSync('dev_mode_auth')
}

// 初始化开发模式状态（从本地存储恢复）
export const initDevModeFromStorage = () => {
  try {
    const stored = uni.getStorageSync('dev_mode_auth')
    if (stored && stored.authenticated && stored.authTime) {
      const now = Date.now()
      const elapsed = now - stored.authTime

      if (elapsed <= DEV_MODE.auth.authExpiry) {
        DEV_MODE.enabled = true
        DEV_MODE.auth.authenticated = true
        DEV_MODE.auth.authTime = stored.authTime
        console.log('开发模式已从本地存储恢复')
      } else {
        // 已过期，清除存储
        uni.removeStorageSync('dev_mode_auth')
      }
    }
  } catch (error) {
    console.error('恢复开发模式状态失败:', error)
  }
}

export default {
  FEATURE_FLAGS,
  isFeatureEnabled,
  getFeatureDisabledReason,
  showFeatureDisabledToast,
  DISABLED_ROUTES,
  isRouteDisabled,
  handleDisabledRoute,
  DEV_MODE,
  getFinalFeatureState,
  isFeatureClickable,
  handleDisabledFeatureClick,
  generateLocalPassword,
  verifyDevPassword,
  enableDevMode,
  isDevModeValid,
  disableDevMode,
  initDevModeFromStorage
}
