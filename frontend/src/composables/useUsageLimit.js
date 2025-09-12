/**
 * 使用限制相关的组合式函数
 * 用于工具组件中的使用限制检查和提示
 */

import { ref, computed } from 'vue'
import { get } from '@/utils/request'

/**
 * 使用限制检查hook
 */
export function useUsageLimit() {
  const remainingUsage = ref(0)
  const isLimited = ref(false)
  const userType = ref('ANONYMOUS')
  const loading = ref(false)

  /**
   * 检查工具使用限制
   * @param {string} toolName 工具名称
   * @returns {Promise<boolean>} 是否可以使用
   */
  const checkUsageLimit = async (toolName) => {
    try {
      loading.value = true
      const response = await get('/api/usage-limits/check', { toolName })
      
      if (response.code === 0 && response.data) {
        const data = response.data
        isLimited.value = data.isExceeded
        remainingUsage.value = data.remaining || 0
        userType.value = data.userType || 'ANONYMOUS'
        
        return !data.isExceeded
      } else {
        // 检查失败时默认允许使用
        console.warn('使用限制检查失败:', response.message)
        return true
      }
    } catch (error) {
      console.error('检查使用限制异常:', error)
      // 异常情况下默认允许使用，避免影响用户体验
      return true
    } finally {
      loading.value = false
    }
  }

  /**
   * 显示使用提示
   */
  const showUsageHint = computed(() => {
    return !loading.value && (remainingUsage.value < 5 || userType.value === 'ANONYMOUS')
  })

  /**
   * 使用提示文本
   */
  const usageHintText = computed(() => {
    if (userType.value === 'ANONYMOUS') {
      return `今日剩余使用次数: ${remainingUsage.value}，登录后可获得更多使用次数`
    } else if (remainingUsage.value < 5) {
      return `今日剩余使用次数: ${remainingUsage.value}`
    }
    return ''
  })

  /**
   * 使用提示样式类
   */
  const usageHintClass = computed(() => {
    if (remainingUsage.value === 0) {
      return 'usage-hint-danger'
    } else if (remainingUsage.value < 3) {
      return 'usage-hint-warning'
    } else {
      return 'usage-hint-info'
    }
  })

  /**
   * 是否显示登录提示
   */
  const showLoginHint = computed(() => {
    return userType.value === 'ANONYMOUS'
  })

  /**
   * 前端工具使用检查和记录
   * @param {string} toolName 工具名称
   * @param {number} batchSize 批量处理大小
   * @returns {Promise<Object>} 使用结果对象
   */
  const useFrontendTool = async (toolName, batchSize = 1) => {
    try {
      loading.value = true
      
      // 1. 预检查剩余次数
      const frontendToolName = toolName.endsWith('-frontend') ? toolName : toolName + '-frontend'
      const response = await get('/api/usage-limits/check', { toolName: frontendToolName })
      
      if (response.code === 0 && response.data) {
        const data = response.data
        const remaining = data.remaining || 0
        userType.value = data.userType || 'ANONYMOUS'
        
        // 检查是否有足够的剩余次数
        if (data.isExceeded || remaining < batchSize) {
          isLimited.value = true
          remainingUsage.value = remaining
          
          showFrontendToolLimit(frontendToolName, 
            `剩余次数不足，需要${batchSize}次，剩余${remaining}次`)
          
          return {
            canUse: false,
            remaining: remaining,
            reportUsage: () => Promise.resolve()
          }
        }
        
        // 2. 返回可用状态和回调函数
        remainingUsage.value = remaining
        isLimited.value = false
        
        return {
          canUse: true,
          remaining: remaining,
          // 使用完成后调用此函数记录使用
          reportUsage: async (actualCount = batchSize) => {
            await reportFrontendToolUsage(frontendToolName, actualCount)
          }
        }
      } else {
        console.warn('使用限制检查失败:', response.message)
        return {
          canUse: true,
          remaining: 999,
          reportUsage: () => Promise.resolve()
        }
      }
    } catch (error) {
      console.error('前端工具使用检查失败:', error)
      // 异常情况下默认允许使用，避免影响用户体验
      return {
        canUse: true,
        remaining: 0,
        reportUsage: () => Promise.resolve()
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 报告前端工具使用情况
   * @param {string} toolName 工具名称
   * @param {number} usageCount 使用次数
   */
  const reportFrontendToolUsage = async (toolName, usageCount) => {
    try {
      // 通过多次调用检查API来记录使用（触发限制检查）
      for (let i = 0; i < usageCount; i++) {
        await get('/api/usage-limits/check', { toolName })
      }
      console.debug('成功记录前端工具使用:', toolName, usageCount)
    } catch (error) {
      console.error('报告工具使用失败:', error)
    }
  }

  /**
   * 显示前端工具限制提示
   * @param {string} toolName 工具名称
   * @param {string} message 提示消息
   */
  const showFrontendToolLimit = (toolName, message) => {
    const toolDisplayNames = {
      'qr-generate-frontend': '二维码生成',
      'qr-decode-frontend': '二维码识别',
      'image-process-frontend': '图片处理',
      'image-compress-frontend': '图片压缩',
      'data-convert-frontend': '数据转换'
    }
    
    uni.showModal({
      title: `${toolDisplayNames[toolName] || '工具'}使用限制`,
      content: message,
      showCancel: true,
      cancelText: '知道了',
      confirmText: '登录获取更多',
      success: (res) => {
        if (res.confirm) {
          // 引导登录
          uni.navigateTo({ 
            url: '/pages/auth/login',
            fail: () => {
              uni.showToast({
                title: '请通过微信登录获取更多使用次数',
                icon: 'none',
                duration: 3000
              })
            }
          })
        }
      }
    })
  }

  return {
    remainingUsage,
    isLimited,
    userType,
    loading,
    showUsageHint,
    usageHintText,
    usageHintClass,
    showLoginHint,
    checkUsageLimit,
    useFrontendTool,
    reportFrontendToolUsage
  }
}

/**
 * 工具使用限制管理hook（用于管理界面）
 */
export function useUsageLimitAdmin() {
  const configs = ref([])
  const loading = ref(false)

  /**
   * 获取所有限制配置
   */
  const fetchConfigs = async () => {
    try {
      loading.value = true
      const response = await get('/api/usage-limits/configs')
      
      if (response.code === 0 && response.data) {
        configs.value = response.data
      }
    } catch (error) {
      console.error('获取限制配置失败:', error)
      uni.showToast({
        title: '获取配置失败',
        icon: 'none'
      })
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新限制配置
   */
  const updateConfig = async (config) => {
    try {
      const response = await uni.request({
        url: '/api/usage-limits/configs/' + config.id,
        method: 'PUT',
        data: config
      })
      
      if (response.data.code === 0) {
        uni.showToast({
          title: '更新成功',
          icon: 'success'
        })
        await fetchConfigs() // 重新获取配置
        return true
      } else {
        uni.showToast({
          title: response.data.message || '更新失败',
          icon: 'none'
        })
        return false
      }
    } catch (error) {
      console.error('更新配置失败:', error)
      uni.showToast({
        title: '更新失败',
        icon: 'none'
      })
      return false
    }
  }

  /**
   * 批量更新配置
   */
  const batchUpdateConfigs = async (configList) => {
    try {
      loading.value = true
      const response = await uni.request({
        url: '/api/usage-limits/configs/batch',
        method: 'PUT',
        data: configList
      })
      
      if (response.data.code === 0) {
        uni.showToast({
          title: '批量更新成功',
          icon: 'success'
        })
        await fetchConfigs()
        return true
      } else {
        uni.showToast({
          title: response.data.message || '批量更新失败',
          icon: 'none'
        })
        return false
      }
    } catch (error) {
      console.error('批量更新失败:', error)
      uni.showToast({
        title: '批量更新失败',
        icon: 'none'
      })
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    configs,
    loading,
    fetchConfigs,
    updateConfig,
    batchUpdateConfigs
  }
}