/**
 * 文件处理基础类
 * 提供统一的文件处理接口和平台适配
 */

export class FileProcessor {
  constructor() {
    this.supportedFormats = []
    this.maxFileSize = 50 * 1024 * 1024 // 50MB
  }

  /**
   * 验证文件格式
   * @param {File} file 文件对象
   * @returns {boolean} 是否支持
   */
  validateFormat(file) {
    if (!this.supportedFormats.length) return true
    
    const fileType = file.type || this.getFileTypeFromName(file.name)
    return this.supportedFormats.some(format => 
      fileType.includes(format) || file.name.toLowerCase().endsWith(format)
    )
  }

  /**
   * 验证文件大小
   * @param {File} file 文件对象
   * @returns {boolean} 是否符合大小限制
   */
  validateSize(file) {
    return file.size <= this.maxFileSize
  }

  /**
   * 从文件名获取文件类型
   * @param {string} fileName 文件名
   * @returns {string} 文件类型
   */
  getFileTypeFromName(fileName) {
    const ext = fileName.split('.').pop().toLowerCase()
    const typeMap = {
      'pdf': 'application/pdf',
      'jpg': 'image/jpeg',
      'jpeg': 'image/jpeg',
      'png': 'image/png',
      'gif': 'image/gif',
      'webp': 'image/webp',
      'json': 'application/json',
      'xml': 'application/xml',
      'yaml': 'application/yaml',
      'yml': 'application/yaml'
    }
    return typeMap[ext] || 'application/octet-stream'
  }

  /**
   * 读取文件内容
   * @param {File} file 文件对象
   * @param {string} readAs 读取方式: 'text', 'dataURL', 'arrayBuffer'
   * @returns {Promise} 文件内容
   */
  readFile(file, readAs = 'dataURL') {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      
      reader.onload = (e) => resolve(e.target.result)
      reader.onerror = () => reject(new Error('文件读取失败'))
      
      switch (readAs) {
        case 'text':
          reader.readAsText(file)
          break
        case 'arrayBuffer':
          reader.readAsArrayBuffer(file)
          break
        case 'dataURL':
        default:
          reader.readAsDataURL(file)
          break
      }
    })
  }

  /**
   * 创建下载链接
   * @param {Blob} blob 文件数据
   * @param {string} filename 文件名
   */
  downloadFile(blob, filename) {
    // 检查是否在浏览器环境
    if (typeof document !== 'undefined' && typeof URL !== 'undefined') {
      // H5环境
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      a.click()
      URL.revokeObjectURL(url)
    } else if (typeof uni !== 'undefined') {
      // 小程序需要先保存到临时文件
      const fs = uni.getFileSystemManager()
      const filePath = `${uni.env.USER_DATA_PATH}/${filename}`

      // 将blob转换为ArrayBuffer
      blob.arrayBuffer().then(buffer => {
        fs.writeFile({
          filePath,
          data: buffer,
          success: () => {
            uni.saveFile({
              tempFilePath: filePath,
              success: () => {
                uni.showToast({ title: '保存成功' })
              },
              fail: () => {
                uni.showToast({ title: '保存失败', icon: 'none' })
              }
            })
          },
          fail: () => {
            uni.showToast({ title: '文件处理失败', icon: 'none' })
          }
        })
      })
    } else {
      console.error('当前环境不支持文件下载')
    }
  }

  /**
   * 格式化文件大小
   * @param {number} size 文件大小（字节）
   * @returns {string} 格式化后的大小
   */
  formatFileSize(size) {
    if (size < 1024) return `${size}B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
    if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)}MB`
    return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
  }

  /**
   * 生成唯一文件名
   * @param {string} originalName 原始文件名
   * @param {string} suffix 后缀
   * @returns {string} 新文件名
   */
  generateFileName(originalName, suffix = '') {
    const timestamp = Date.now()
    const ext = originalName.split('.').pop()
    const name = originalName.replace(`.${ext}`, '')
    return `${name}${suffix}_${timestamp}.${ext}`
  }
}
