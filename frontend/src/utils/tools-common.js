/**
 * 工具页面通用函数库
 * 提供常用的工具函数，避免重复代码
 */

/**
 * 格式化文件大小
 * @param {number} size 文件大小（字节）
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(size) {
  if (!size || size === 0) return '0B'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)}MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
}

/**
 * 计算压缩比例
 * @param {number} originalSize 原始大小
 * @param {number} compressedSize 压缩后大小
 * @returns {string} 压缩比例百分比
 */
export function calculateCompressionRatio(originalSize, compressedSize) {
  if (!originalSize || originalSize === 0) return '0%'
  const ratio = ((originalSize - compressedSize) / originalSize) * 100
  return `${Math.max(0, ratio).toFixed(1)}%`
}

/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} wait 等待时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

/**
 * 节流函数
 * @param {Function} func 要节流的函数
 * @param {number} limit 时间间隔（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit) {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 深拷贝对象
 * @param {any} obj 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
}

/**
 * 生成唯一ID
 * @param {string} prefix 前缀
 * @returns {string} 唯一ID
 */
export function generateId(prefix = 'id') {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 下载文件
 * @param {Blob|File} file 文件对象
 * @param {string} filename 文件名
 */
export function downloadFile(file, filename) {
  try {
    const url = URL.createObjectURL(file)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.style.display = 'none'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载文件失败:', error)
    uni.showToast({
      title: '下载失败',
      icon: 'none'
    })
  }
}

/**
 * 批量下载文件（打包为ZIP）
 * @param {Array} files 文件数组 [{data: Blob, filename: string}]
 * @param {string} zipName ZIP文件名
 */
export async function downloadBatch(files, zipName = 'files.zip') {
  try {
    // 动态导入JSZip
    const JSZip = (await import('jszip')).default
    const zip = new JSZip()
    
    // 添加文件到ZIP
    files.forEach(file => {
      zip.file(file.filename, file.data)
    })
    
    // 生成ZIP文件
    const zipBlob = await zip.generateAsync({ type: 'blob' })
    downloadFile(zipBlob, zipName)
    
  } catch (error) {
    console.error('批量下载失败:', error)
    uni.showToast({
      title: '批量下载失败',
      icon: 'none'
    })
  }
}

/**
 * 复制文本到剪贴板
 * @param {string} text 要复制的文本
 * @returns {Promise<boolean>} 是否成功
 */
export async function copyToClipboard(text) {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
      return true
    } else {
      // 降级方案
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      const result = document.execCommand('copy')
      document.body.removeChild(textArea)
      return result
    }
  } catch (error) {
    console.error('复制失败:', error)
    return false
  }
}

/**
 * 验证文件类型
 * @param {File} file 文件对象
 * @param {Array<string>} allowedTypes 允许的MIME类型
 * @returns {boolean} 是否有效
 */
export function validateFileType(file, allowedTypes) {
  if (!file || !allowedTypes || allowedTypes.length === 0) return false
  return allowedTypes.some(type => {
    if (type.endsWith('/*')) {
      return file.type.startsWith(type.slice(0, -1))
    }
    return file.type === type
  })
}

/**
 * 验证文件大小
 * @param {File} file 文件对象
 * @param {number} maxSize 最大大小（字节）
 * @returns {boolean} 是否有效
 */
export function validateFileSize(file, maxSize) {
  if (!file || !maxSize) return false
  return file.size <= maxSize
}

/**
 * 格式化时间
 * @param {Date|number} date 日期对象或时间戳
 * @param {string} format 格式字符串
 * @returns {string} 格式化后的时间
 */
export function formatTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 显示成功提示
 * @param {string} message 提示信息
 */
export function showSuccess(message) {
  uni.showToast({
    title: message,
    icon: 'success',
    duration: 2000
  })
}

/**
 * 显示错误提示
 * @param {string} message 错误信息
 */
export function showError(message) {
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 3000
  })
}

/**
 * 显示加载提示
 * @param {string} message 加载信息
 */
export function showLoading(message = '加载中...') {
  uni.showLoading({
    title: message,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 确认对话框
 * @param {string} message 确认信息
 * @param {string} title 标题
 * @returns {Promise<boolean>} 用户是否确认
 */
export function confirm(message, title = '确认') {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content: message,
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
 * 获取文件扩展名
 * @param {string} filename 文件名
 * @returns {string} 扩展名（不含点）
 */
export function getFileExtension(filename) {
  if (!filename) return ''
  const lastDotIndex = filename.lastIndexOf('.')
  return lastDotIndex > -1 ? filename.substring(lastDotIndex + 1).toLowerCase() : ''
}

/**
 * 获取文件名（不含扩展名）
 * @param {string} filename 文件名
 * @returns {string} 文件名（不含扩展名）
 */
export function getFileNameWithoutExtension(filename) {
  if (!filename) return ''
  const lastDotIndex = filename.lastIndexOf('.')
  return lastDotIndex > -1 ? filename.substring(0, lastDotIndex) : filename
}
