/**
 * 全局错误处理工具
 * 提供统一的错误处理机制，确保API不可用时应用仍能正常运行
 */

import { ref, computed, onUnmounted } from 'vue'

// 全局错误状态
const globalErrorState = ref({
  hasNetworkError: false,
  hasApiError: false,
  errorMessage: '',
  lastErrorTime: null
})

/**
 * 安全执行异步函数，带超时和错误处理
 * @param {Function} asyncFn 异步函数
 * @param {Object} options 配置选项
 * @returns {Promise} 执行结果
 */
export const safeExecute = async (asyncFn, options = {}) => {
  const {
    timeout = 5000,
    fallback = null,
    silent = false,
    retries = 0
  } = options

  let lastError = null
  
  for (let attempt = 0; attempt <= retries; attempt++) {
    try {
      // 使用 Promise.race 实现超时控制
      const result = await Promise.race([
        asyncFn(),
        new Promise((_, reject) => 
          setTimeout(() => reject(new Error('Operation timeout')), timeout)
        )
      ])
      
      // 成功执行，清除错误状态
      if (globalErrorState.value.hasNetworkError) {
        globalErrorState.value.hasNetworkError = false
        globalErrorState.value.errorMessage = ''
      }
      
      return result
    } catch (error) {
      lastError = error
      
      if (!silent) {
        console.warn(`执行失败 (尝试 ${attempt + 1}/${retries + 1}):`, error.message)
      }
      
      // 如果还有重试机会，等待一段时间后重试
      if (attempt < retries) {
        await new Promise(resolve => setTimeout(resolve, 1000 * (attempt + 1)))
      }
    }
  }
  
  // 所有尝试都失败了
  globalErrorState.value.hasNetworkError = true
  globalErrorState.value.errorMessage = lastError?.message || '网络连接异常'
  globalErrorState.value.lastErrorTime = Date.now()
  
  if (!silent) {
    console.error('执行最终失败:', lastError)
  }
  
  return fallback
}

/**
 * 安全的API调用包装器
 * @param {Function} apiCall API调用函数
 * @param {Object} options 配置选项
 * @returns {Promise} API调用结果
 */
export const safeApiCall = async (apiCall, options = {}) => {
  const {
    showError = false,
    fallbackData = null,
    timeout = 8000
  } = options

  return safeExecute(apiCall, {
    timeout,
    fallback: fallbackData,
    silent: !showError,
    retries: 1
  })
}

/**
 * 安全的初始化函数包装器
 * @param {Function} initFn 初始化函数
 * @param {Object} options 配置选项
 * @returns {Promise} 初始化结果
 */
export const safeInit = async (initFn, options = {}) => {
  const {
    essential = false,
    timeout = 3000
  } = options

  const result = await safeExecute(initFn, {
    timeout,
    fallback: null,
    silent: !essential,
    retries: essential ? 2 : 0
  })

  return result
}

/**
 * 创建错误边界组合式函数
 * @param {Object} options 配置选项
 * @returns {Object} 错误边界相关的响应式数据和方法
 */
export const useErrorBoundary = (options = {}) => {
  const {
    showGlobalError = true,
    autoRetry = true,
    retryInterval = 30000
  } = options

  const localError = ref(false)
  const isRetrying = ref(false)
  const retryCount = ref(0)

  // 检查是否有错误
  const hasError = computed(() => {
    return localError.value || (showGlobalError && globalErrorState.value.hasNetworkError)
  })

  // 错误信息
  const errorMessage = computed(() => {
    if (localError.value) return '页面加载异常'
    return globalErrorState.value.errorMessage || '网络连接异常'
  })

  // 设置本地错误
  const setError = (error) => {
    localError.value = true
    console.error('页面错误:', error)
  }

  // 清除本地错误
  const clearError = () => {
    localError.value = false
    retryCount.value = 0
  }

  // 重试连接
  const retry = async () => {
    if (isRetrying.value) return false

    isRetrying.value = true
    retryCount.value++

    try {
      // 这里可以添加具体的重试逻辑
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 清除错误状态
      clearError()
      globalErrorState.value.hasNetworkError = false
      globalErrorState.value.errorMessage = ''
      
      return true
    } catch (error) {
      console.error('重试失败:', error)
      return false
    } finally {
      isRetrying.value = false
    }
  }

  // 自动重试机制
  if (autoRetry) {
    const autoRetryTimer = setInterval(() => {
      if (hasError.value && !isRetrying.value && retryCount.value < 3) {
        retry()
      }
    }, retryInterval)

    // 清理定时器
    onUnmounted(() => {
      clearInterval(autoRetryTimer)
    })
  }

  return {
    hasError,
    errorMessage,
    isRetrying,
    retryCount,
    setError,
    clearError,
    retry
  }
}

/**
 * 网络状态检查
 * @returns {Promise<boolean>} 网络是否可用
 */
export const checkNetworkStatus = async () => {
  try {
    // 尝试发送一个简单的请求来检查网络状态
    const response = await fetch('data:text/plain;base64,', {
      method: 'HEAD',
      cache: 'no-cache'
    })
    return true
  } catch (error) {
    return false
  }
}

/**
 * 显示用户友好的错误提示
 * @param {string} message 错误信息
 * @param {Object} options 配置选项
 */
export const showUserFriendlyError = (message, options = {}) => {
  const {
    duration = 3000,
    showRetry = false
  } = options

  if (showRetry) {
    uni.showModal({
      title: '网络异常',
      content: message || '连接服务器失败，请检查网络连接',
      showCancel: true,
      cancelText: '稍后重试',
      confirmText: '立即重试',
      success: (res) => {
        if (res.confirm) {
          // 触发重试逻辑
          window.location.reload?.()
        }
      }
    })
  } else {
    uni.showToast({
      title: message || '网络连接异常',
      icon: 'none',
      duration
    })
  }
}

// 导出全局错误状态
export { globalErrorState }

// 默认导出
export default {
  safeExecute,
  safeApiCall,
  safeInit,
  useErrorBoundary,
  checkNetworkStatus,
  showUserFriendlyError,
  globalErrorState
}
