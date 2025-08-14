/**
 * 统一Toast提示工具
 * 提供统一的消息提示方法，包含成功、错误、警告、加载等类型
 */

// Toast类型常量
export const TOAST_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'none',
  INFO: 'none',
  LOADING: 'loading'
}

// 默认配置
const DEFAULT_CONFIG = {
  duration: 2000,
  mask: false,
  position: 'center'
}

/**
 * 显示成功提示
 * @param {string} title 提示内容
 * @param {Object} options 配置选项
 */
export const showSuccess = (title, options = {}) => {
  uni.showToast({
    title,
    icon: TOAST_TYPES.SUCCESS,
    ...DEFAULT_CONFIG,
    ...options
  })
}

/**
 * 显示错误提示
 * @param {string} title 提示内容
 * @param {Object} options 配置选项
 */
export const showError = (title, options = {}) => {
  uni.showToast({
    title,
    icon: TOAST_TYPES.ERROR,
    ...DEFAULT_CONFIG,
    ...options
  })
}

/**
 * 显示警告提示
 * @param {string} title 提示内容
 * @param {Object} options 配置选项
 */
export const showWarning = (title, options = {}) => {
  uni.showToast({
    title,
    icon: TOAST_TYPES.WARNING,
    ...DEFAULT_CONFIG,
    ...options
  })
}

/**
 * 显示信息提示
 * @param {string} title 提示内容
 * @param {Object} options 配置选项
 */
export const showInfo = (title, options = {}) => {
  uni.showToast({
    title,
    icon: TOAST_TYPES.INFO,
    ...DEFAULT_CONFIG,
    ...options
  })
}

/**
 * 显示加载提示
 * @param {string} title 提示内容
 * @param {Object} options 配置选项
 */
export const showLoading = (title = '加载中...', options = {}) => {
  uni.showLoading({
    title,
    mask: true,
    ...options
  })
}

/**
 * 隐藏加载提示
 */
export const hideLoading = () => {
  uni.hideLoading()
}

/**
 * 隐藏Toast提示
 */
export const hideToast = () => {
  uni.hideToast()
}

/**
 * 显示模态对话框
 * @param {string} title 标题
 * @param {string} content 内容
 * @param {Object} options 配置选项
 * @returns {Promise} 用户操作结果
 */
export const showModal = (title, content, options = {}) => {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      showCancel: true,
      confirmText: '确定',
      cancelText: '取消',
      ...options,
      success: (res) => {
        resolve(res.confirm)
      },
      fail: () => {
        resolve(false)
      }
    })
  })
}

/**
 * 显示确认对话框
 * @param {string} content 内容
 * @param {string} title 标题
 * @param {Object} options 配置选项
 * @returns {Promise} 用户操作结果
 */
export const showConfirm = (content, title = '提示', options = {}) => {
  return showModal(title, content, options)
}

/**
 * 显示警告对话框
 * @param {string} content 内容
 * @param {string} title 标题
 * @param {Object} options 配置选项
 * @returns {Promise} 用户操作结果
 */
export const showAlert = (content, title = '警告', options = {}) => {
  return showModal(title, content, {
    showCancel: false,
    confirmText: '我知道了',
    ...options
  })
}

/**
 * 显示操作菜单
 * @param {Array} itemList 菜单项列表
 * @param {Object} options 配置选项
 * @returns {Promise} 用户选择结果
 */
export const showActionSheet = (itemList, options = {}) => {
  return new Promise((resolve, reject) => {
    uni.showActionSheet({
      itemList,
      ...options,
      success: (res) => {
        resolve(res.tapIndex)
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}

/**
 * 通用Toast方法
 * @param {string} title 提示内容
 * @param {string} type 提示类型
 * @param {Object} options 配置选项
 */
export const toast = (title, type = TOAST_TYPES.INFO, options = {}) => {
  switch (type) {
    case TOAST_TYPES.SUCCESS:
      showSuccess(title, options)
      break
    case TOAST_TYPES.ERROR:
      showError(title, options)
      break
    case TOAST_TYPES.WARNING:
      showWarning(title, options)
      break
    case TOAST_TYPES.LOADING:
      showLoading(title, options)
      break
    default:
      showInfo(title, options)
  }
}

// 导出默认方法
export default {
  showSuccess,
  showError,
  showWarning,
  showInfo,
  showLoading,
  hideLoading,
  hideToast,
  showModal,
  showConfirm,
  showAlert,
  showActionSheet,
  toast,
  TOAST_TYPES
}
