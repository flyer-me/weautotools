/**
 * 二维码识别器
 * 基于jsQR库实现二维码识别功能
 */

import jsQR from 'jsqr'
import { FileProcessor } from '../base/FileProcessor.js'

export class QRDecoder extends FileProcessor {
  constructor() {
    super()
    this.supportedFormats = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  }

  /**
   * 从文件识别二维码
   * @param {File} file 图片文件
   * @returns {Promise} 识别结果
   */
  async decodeFromFile(file) {
    if (!this.validateFormat(file)) {
      throw new Error('不支持的图片格式')
    }

    if (!this.validateSize(file)) {
      throw new Error('图片文件过大')
    }

    try {
      const imageData = await this.getImageData(file)
      const result = this.decodeFromImageData(imageData)

      return {
        success: true,
        file,
        data: result.data,
        location: result.location,
        version: result.version,
        errorCorrectionLevel: result.errorCorrectionLevel,
        maskPattern: result.maskPattern,
        binaryData: result.binaryData
      }
    } catch (error) {
      console.error('二维码识别失败:', error)
      throw error // 重新抛出错误，让上层处理
    }
  }

  /**
   * 批量识别二维码
   * @param {Array} files 图片文件列表
   * @param {Function} onProgress 进度回调
   * @returns {Promise} 批量识别结果
   */
  async decodeBatch(files, onProgress = null) {
    if (!Array.isArray(files) || files.length === 0) {
      throw new Error('请提供有效的文件列表')
    }

    const results = []
    const total = files.length
    
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      
      try {
        const result = await this.decodeFromFile(file)
        results.push({ 
          ...result,
          index: i
        })
      } catch (error) {
        results.push({ 
          success: false, 
          file,
          index: i,
          error: error.message 
        })
      }
      
      // 更新进度
      if (onProgress) {
        onProgress({
          completed: i + 1,
          total,
          percentage: Math.round(((i + 1) / total) * 100),
          currentFile: file.name
        })
      }
      
      // 短暂延迟，避免阻塞UI
      if (i < files.length - 1) {
        await this.delay(10)
      }
    }

    return results
  }

  /**
   * 从图片URL识别二维码
   * @param {string} imageUrl 图片URL
   * @returns {Promise} 识别结果
   */
  async decodeFromUrl(imageUrl) {
    try {
      const img = await this.loadImage(imageUrl)
      const imageData = this.getImageDataFromImage(img)
      const result = this.decodeFromImageData(imageData)
      
      return {
        success: true,
        url: imageUrl,
        data: result.data,
        location: result.location,
        version: result.version,
        errorCorrectionLevel: result.errorCorrectionLevel
      }
    } catch (error) {
      return {
        success: false,
        url: imageUrl,
        error: error.message
      }
    }
  }

  /**
   * 从Canvas识别二维码
   * @param {HTMLCanvasElement} canvas Canvas元素
   * @returns {Promise} 识别结果
   */
  decodeFromCanvas(canvas) {
    try {
      const ctx = canvas.getContext('2d')
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const result = this.decodeFromImageData(imageData)
      
      return {
        success: true,
        data: result.data,
        location: result.location,
        version: result.version
      }
    } catch (error) {
      return {
        success: false,
        error: error.message
      }
    }
  }

  /**
   * 从ImageData识别二维码
   * @param {ImageData} imageData 图像数据
   * @returns {Object} 识别结果
   */
  decodeFromImageData(imageData) {
    console.log('开始识别二维码，图像尺寸:', imageData.width, 'x', imageData.height)

    // 尝试多种识别参数
    const attempts = [
      { inversionAttempts: "dontInvert" },
      { inversionAttempts: "onlyInvert" },
      { inversionAttempts: "attemptBoth" }
    ]

    for (const options of attempts) {
      try {
        const code = jsQR(imageData.data, imageData.width, imageData.height, options)

        if (code) {
          console.log('二维码识别成功:', code.data)
          return {
            data: code.data,
            location: code.location,
            version: code.version,
            errorCorrectionLevel: code.errorCorrectionLevel,
            maskPattern: code.maskPattern,
            binaryData: code.binaryData
          }
        }
      } catch (error) {
        console.warn('识别尝试失败:', error.message)
      }
    }

    throw new Error('未识别到二维码')
  }

  /**
   * 摄像头实时扫描 (H5端)
   * @param {HTMLVideoElement} videoElement 视频元素
   * @param {Function} onDetected 检测到二维码的回调
   * @param {Object} options 扫描选项
   * @returns {Promise} 媒体流
   */
  async startCameraScanning(videoElement, onDetected, options = {}) {
    const {
      facingMode = 'environment', // 后置摄像头
      width = 640,
      height = 480,
      frameRate = 30
    } = options

    // 检查是否支持摄像头
    if (typeof navigator === 'undefined' || !navigator.mediaDevices) {
      throw new Error('当前平台不支持摄像头扫描')
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode,
          width: { ideal: width },
          height: { ideal: height },
          frameRate: { ideal: frameRate }
        }
      })

      videoElement.srcObject = stream
      videoElement.play()

      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      let scanning = true

      const scanFrame = () => {
        if (!scanning) return

        if (videoElement.readyState === videoElement.HAVE_ENOUGH_DATA) {
          canvas.width = videoElement.videoWidth
          canvas.height = videoElement.videoHeight
          ctx.drawImage(videoElement, 0, 0)

          const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)

          try {
            const result = this.decodeFromImageData(imageData)
            onDetected(result.data, result)
            return // 停止扫描
          } catch (error) {
            // 继续扫描
          }
        }

        requestAnimationFrame(scanFrame)
      }

      scanFrame()

      // 返回停止扫描的方法
      return {
        stream,
        stop: () => {
          scanning = false
          stream.getTracks().forEach(track => track.stop())
        }
      }
    } catch (error) {
      throw new Error(`摄像头访问失败: ${error.message}`)
    }
  }

  /**
   * 获取图片的ImageData
   * @param {File} file 图片文件
   * @returns {Promise} ImageData
   */
  getImageData(file) {
    return new Promise((resolve, reject) => {
      console.log('开始加载图片:', file.name, '大小:', file.size)

      const img = new Image()
      img.onload = () => {
        try {
          console.log('图片加载成功，尺寸:', img.width, 'x', img.height)
          const imageData = this.getImageDataFromImage(img)
          console.log('ImageData创建成功，尺寸:', imageData.width, 'x', imageData.height)
          resolve(imageData)
        } catch (error) {
          console.error('ImageData创建失败:', error)
          reject(error)
        }
      }
      img.onerror = (error) => {
        console.error('图片加载失败:', error)
        reject(new Error('图片加载失败'))
      }

      // 设置跨域属性
      img.crossOrigin = 'anonymous'
      img.src = URL.createObjectURL(file)
    })
  }

  /**
   * 从Image对象获取ImageData
   * @param {HTMLImageElement} img 图片对象
   * @returns {ImageData} 图像数据
   */
  getImageDataFromImage(img) {
    // 检查是否在浏览器环境
    if (typeof document === 'undefined') {
      throw new Error('当前环境不支持Canvas操作')
    }

    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    if (!ctx) {
      throw new Error('无法创建Canvas上下文')
    }

    // 设置Canvas尺寸
    canvas.width = img.naturalWidth || img.width
    canvas.height = img.naturalHeight || img.height

    console.log('Canvas尺寸设置为:', canvas.width, 'x', canvas.height)

    // 清除Canvas并绘制图片
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height)

    // 获取ImageData
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)

    if (!imageData || !imageData.data) {
      throw new Error('无法获取图像数据')
    }

    return imageData
  }

  /**
   * 加载图片
   * @param {string} src 图片源
   * @returns {Promise} Image对象
   */
  loadImage(src) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => resolve(img)
      img.onerror = () => reject(new Error('图片加载失败'))
      img.crossOrigin = 'anonymous' // 允许跨域
      img.src = src
    })
  }

  /**
   * 预处理图片（增强识别率）
   * @param {ImageData} imageData 原始图像数据
   * @returns {ImageData} 处理后的图像数据
   */
  preprocessImage(imageData) {
    const data = new Uint8ClampedArray(imageData.data)
    
    // 转换为灰度图
    for (let i = 0; i < data.length; i += 4) {
      const gray = Math.round(0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2])
      data[i] = gray     // R
      data[i + 1] = gray // G
      data[i + 2] = gray // B
      // data[i + 3] 保持不变 (Alpha)
    }
    
    // 增强对比度
    this.enhanceContrast(data)
    
    return new ImageData(data, imageData.width, imageData.height)
  }

  /**
   * 增强对比度
   * @param {Uint8ClampedArray} data 图像数据
   */
  enhanceContrast(data) {
    const factor = 1.5 // 对比度因子
    
    for (let i = 0; i < data.length; i += 4) {
      data[i] = Math.min(255, Math.max(0, factor * (data[i] - 128) + 128))
      data[i + 1] = Math.min(255, Math.max(0, factor * (data[i + 1] - 128) + 128))
      data[i + 2] = Math.min(255, Math.max(0, factor * (data[i + 2] - 128) + 128))
    }
  }

  /**
   * 分析二维码内容类型
   * @param {string} data 二维码数据
   * @returns {Object} 内容分析结果
   */
  analyzeContent(data) {
    const analysis = {
      type: 'text',
      description: '普通文本',
      data
    }
    
    // URL检测
    if (/^https?:\/\//i.test(data)) {
      analysis.type = 'url'
      analysis.description = '网址链接'
    }
    // 邮箱检测
    else if (/^mailto:/i.test(data)) {
      analysis.type = 'email'
      analysis.description = '邮箱地址'
      analysis.email = data.replace(/^mailto:/i, '')
    }
    // 电话检测
    else if (/^tel:/i.test(data)) {
      analysis.type = 'phone'
      analysis.description = '电话号码'
      analysis.phone = data.replace(/^tel:/i, '')
    }
    // WiFi检测
    else if (/^WIFI:/i.test(data)) {
      analysis.type = 'wifi'
      analysis.description = 'WiFi配置'
      analysis.wifi = this.parseWiFiData(data)
    }
    // 地理位置检测
    else if (/^geo:/i.test(data)) {
      analysis.type = 'location'
      analysis.description = '地理位置'
      analysis.location = this.parseLocationData(data)
    }
    
    return analysis
  }

  /**
   * 解析WiFi数据
   * @param {string} data WiFi二维码数据
   * @returns {Object} WiFi信息
   */
  parseWiFiData(data) {
    const match = data.match(/WIFI:T:([^;]*);S:([^;]*);P:([^;]*);H:([^;]*);?/i)
    if (match) {
      return {
        security: match[1],
        ssid: match[2],
        password: match[3],
        hidden: match[4] === 'true'
      }
    }
    return null
  }

  /**
   * 解析地理位置数据
   * @param {string} data 地理位置二维码数据
   * @returns {Object} 位置信息
   */
  parseLocationData(data) {
    const match = data.match(/geo:([^,]+),([^,?]+)/i)
    if (match) {
      return {
        latitude: parseFloat(match[1]),
        longitude: parseFloat(match[2])
      }
    }
    return null
  }

  /**
   * 延迟函数
   * @param {number} ms 延迟毫秒数
   * @returns {Promise}
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}
