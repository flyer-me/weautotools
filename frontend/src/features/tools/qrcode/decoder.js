/**
 * 二维码识别器（迁移）
 * 基于 jsQR 实现二维码识别
 */

import jsQR from 'jsqr'
import { FileProcessor } from '../base/FileProcessor.js'

export class QRDecoder extends FileProcessor {
  constructor() {
    super()
    this.supportedFormats = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  }

  /** 从文件识别二维码 */
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
      return { success: true, file, data: result.data, location: result.location, version: result.version, errorCorrectionLevel: result.errorCorrectionLevel, maskPattern: result.maskPattern, binaryData: result.binaryData }
    } catch (error) {
      throw error
    }
  }

  /** 批量识别 */
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
        results.push({ ...result, index: i })
      } catch (error) {
        results.push({ success: false, file, index: i, error: error.message })
      }
      if (onProgress) {
        onProgress({ completed: i + 1, total, percentage: Math.round(((i + 1) / total) * 100), currentFile: file.name })
      }
      if (i < files.length - 1) {
        await this.delay(10)
      }
    }
    return results
  }

  /** 从 URL 识别 */
  async decodeFromUrl(imageUrl) {
    try {
      const img = await this.loadImage(imageUrl)
      const imageData = this.getImageDataFromImage(img)
      const result = this.decodeFromImageData(imageData)
      return { success: true, url: imageUrl, data: result.data, location: result.location, version: result.version, errorCorrectionLevel: result.errorCorrectionLevel }
    } catch (error) {
      return { success: false, url: imageUrl, error: error.message }
    }
  }

  /** 从 Canvas 识别 */
  decodeFromCanvas(canvas) {
    try {
      const ctx = canvas.getContext('2d')
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const result = this.decodeFromImageData(imageData)
      return { success: true, data: result.data, location: result.location, version: result.version }
    } catch (error) {
      return { success: false, error: error.message }
    }
  }

  /** 从 ImageData 识别 */
  decodeFromImageData(imageData) {
    const attempts = [ { inversionAttempts: 'dontInvert' }, { inversionAttempts: 'onlyInvert' }, { inversionAttempts: 'attemptBoth' } ]
    for (const options of attempts) {
      try {
        const code = jsQR(imageData.data, imageData.width, imageData.height, options)
        if (code) {
          return { data: code.data, location: code.location, version: code.version, errorCorrectionLevel: code.errorCorrectionLevel, maskPattern: code.maskPattern, binaryData: code.binaryData }
        }
      } catch (error) {
        // ignore and try next
      }
    }
    throw new Error('未识别到二维码')
  }

  /** 获取文件 ImageData */
  getImageData(file) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        try {
          const imageData = this.getImageDataFromImage(img)
          resolve(imageData)
        } catch (error) {
          reject(error)
        }
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.crossOrigin = 'anonymous'
      img.src = URL.createObjectURL(file)
    })
  }

  /** 从 Image 获取 ImageData */
  getImageDataFromImage(img) {
    if (typeof document === 'undefined') {
      throw new Error('当前环境不支持Canvas操作')
    }
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    if (!ctx) throw new Error('无法创建Canvas上下文')
    canvas.width = img.naturalWidth || img.width
    canvas.height = img.naturalHeight || img.height
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    if (!imageData || !imageData.data) throw new Error('无法获取图像数据')
    return imageData
  }

  /** 加载图片 */
  loadImage(src) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => resolve(img)
      img.onerror = () => reject(new Error('图片加载失败'))
      img.crossOrigin = 'anonymous'
      img.src = src
    })
  }

  /** 预处理图像（可选） */
  preprocessImage(imageData) {
    const data = new Uint8ClampedArray(imageData.data)
    for (let i = 0; i < data.length; i += 4) {
      const gray = Math.round(0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2])
      data[i] = gray
      data[i + 1] = gray
      data[i + 2] = gray
    }
    this.enhanceContrast(data)
    return new ImageData(data, imageData.width, imageData.height)
  }

  enhanceContrast(data) {
    const factor = 1.5
    for (let i = 0; i < data.length; i += 4) {
      data[i] = Math.min(255, Math.max(0, factor * (data[i] - 128) + 128))
      data[i + 1] = Math.min(255, Math.max(0, factor * (data[i + 1] - 128) + 128))
      data[i + 2] = Math.min(255, Math.max(0, factor * (data[i + 2] - 128) + 128))
    }
  }

  /** 内容类型分析 */
  analyzeContent(data) {
    const analysis = { type: 'text', description: '普通文本', data }
    if (/^https?:\/\//i.test(data)) {
      analysis.type = 'url'; analysis.description = '网址链接'
    } else if (/^mailto:/i.test(data)) {
      analysis.type = 'email'; analysis.description = '邮箱地址'; analysis.email = data.replace(/^mailto:/i, '')
    } else if (/^tel:/i.test(data)) {
      analysis.type = 'phone'; analysis.description = '电话号码'; analysis.phone = data.replace(/^tel:/i, '')
    } else if (/^WIFI:/i.test(data)) {
      analysis.type = 'wifi'; analysis.description = 'WiFi配置'; analysis.wifi = this.parseWiFiData(data)
    } else if (/^geo:/i.test(data)) {
      analysis.type = 'location'; analysis.description = '地理位置'; analysis.location = this.parseLocationData(data)
    }
    return analysis
  }

  parseWiFiData(data) {
    const match = data.match(/WIFI:T:([^;]*);S:([^;]*);P:([^;]*);H:([^;]*);?/i)
    if (match) {
      return { security: match[1], ssid: match[2], password: match[3], hidden: match[4] === 'true' }
    }
    return null
  }

  parseLocationData(data) {
    const match = data.match(/geo:([^,]+),([^,?]+)/i)
    if (match) {
      return { latitude: parseFloat(match[1]), longitude: parseFloat(match[2]) }
    }
    return null
  }

  delay(ms) { return new Promise(resolve => setTimeout(resolve, ms)) }
}
