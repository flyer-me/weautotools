/**
 * 二维码生成器
 * 基于qrcode库实现二维码生成功能
 */

import QRCode from 'qrcode'
import { FileProcessor } from '../base/FileProcessor.js'

export class QRGenerator extends FileProcessor {
  constructor() {
    super()
    this.defaultOptions = {
      width: 256,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      },
      errorCorrectionLevel: 'M'
    }
  }

  /**
   * 生成单个二维码
   * @param {string} text 要编码的文本
   * @param {Object} options 生成选项
   * @returns {Promise} 生成结果
   */
  async generate(text, options = {}) {
    if (!text || typeof text !== 'string') {
      throw new Error('请输入有效的文本内容')
    }

    const finalOptions = { ...this.defaultOptions, ...options }
    
    try {
      // 生成DataURL格式
      const dataURL = await QRCode.toDataURL(text, finalOptions)
      
      // 生成Canvas (用于进一步处理)
      const canvas = document.createElement('canvas')
      await QRCode.toCanvas(canvas, text, finalOptions)
      
      // 转换为Blob
      const blob = await this.canvasToBlob(canvas)
      
      return {
        text,
        dataURL,
        canvas,
        blob,
        size: blob.size,
        width: canvas.width,
        height: canvas.height,
        options: finalOptions
      }
    } catch (error) {
      throw new Error(`二维码生成失败: ${error.message}`)
    }
  }

  /**
   * 批量生成二维码
   * @param {Array} textList 文本列表
   * @param {Object} options 生成选项
   * @param {Function} onProgress 进度回调
   * @returns {Promise} 批量生成结果
   */
  async generateBatch(textList, options = {}, onProgress = null) {
    if (!Array.isArray(textList) || textList.length === 0) {
      throw new Error('请提供有效的文本列表')
    }

    const results = []
    const total = textList.length
    
    for (let i = 0; i < textList.length; i++) {
      const text = textList[i]
      
      try {
        const result = await this.generate(text, options)
        results.push({ 
          success: true, 
          index: i,
          text,
          ...result 
        })
      } catch (error) {
        results.push({ 
          success: false, 
          index: i,
          text,
          error: error.message 
        })
      }
      
      // 更新进度
      if (onProgress) {
        onProgress({
          completed: i + 1,
          total,
          percentage: Math.round(((i + 1) / total) * 100),
          currentText: text
        })
      }
      
      // 短暂延迟，避免阻塞UI
      if (i < textList.length - 1) {
        await this.delay(10)
      }
    }

    return results
  }

  /**
   * 生成带Logo的二维码
   * @param {string} text 要编码的文本
   * @param {string|File} logo Logo图片
   * @param {Object} options 生成选项
   * @returns {Promise} 生成结果
   */
  async generateWithLogo(text, logo, options = {}) {
    const qrResult = await this.generate(text, options)
    
    try {
      const logoImage = await this.loadImage(logo)
      const canvas = qrResult.canvas
      const ctx = canvas.getContext('2d')
      
      // 计算Logo尺寸（不超过二维码的1/5）
      const logoSize = Math.min(canvas.width, canvas.height) / 5
      const logoX = (canvas.width - logoSize) / 2
      const logoY = (canvas.height - logoSize) / 2
      
      // 绘制白色背景
      ctx.fillStyle = '#FFFFFF'
      ctx.fillRect(logoX - 5, logoY - 5, logoSize + 10, logoSize + 10)
      
      // 绘制Logo
      ctx.drawImage(logoImage, logoX, logoY, logoSize, logoSize)
      
      // 更新结果
      const newDataURL = canvas.toDataURL()
      const newBlob = await this.canvasToBlob(canvas)
      
      return {
        ...qrResult,
        dataURL: newDataURL,
        blob: newBlob,
        size: newBlob.size,
        hasLogo: true
      }
    } catch (error) {
      throw new Error(`Logo处理失败: ${error.message}`)
    }
  }

  /**
   * 生成SVG格式二维码
   * @param {string} text 要编码的文本
   * @param {Object} options 生成选项
   * @returns {Promise} SVG字符串
   */
  async generateSVG(text, options = {}) {
    const finalOptions = { ...this.defaultOptions, ...options }
    
    try {
      const svgString = await QRCode.toString(text, {
        ...finalOptions,
        type: 'svg'
      })
      
      return {
        text,
        svg: svgString,
        type: 'svg',
        options: finalOptions
      }
    } catch (error) {
      throw new Error(`SVG二维码生成失败: ${error.message}`)
    }
  }

  /**
   * 获取支持的纠错级别
   * @returns {Array} 纠错级别列表
   */
  getErrorCorrectionLevels() {
    return [
      { value: 'L', label: '低 (~7%)', description: '适用于清洁环境' },
      { value: 'M', label: '中 (~15%)', description: '默认级别' },
      { value: 'Q', label: '四分位 (~25%)', description: '适用于一般环境' },
      { value: 'H', label: '高 (~30%)', description: '适用于恶劣环境' }
    ]
  }

  /**
   * 获取预设尺寸
   * @returns {Array} 尺寸列表
   */
  getPresetSizes() {
    return [
      { value: 128, label: '小 (128x128)' },
      { value: 256, label: '中 (256x256)' },
      { value: 512, label: '大 (512x512)' },
      { value: 1024, label: '超大 (1024x1024)' }
    ]
  }

  /**
   * 验证文本内容
   * @param {string} text 文本内容
   * @returns {Object} 验证结果
   */
  validateText(text) {
    if (!text) {
      return { valid: false, error: '请输入文本内容' }
    }
    
    if (typeof text !== 'string') {
      return { valid: false, error: '文本内容必须是字符串' }
    }
    
    // 检查长度限制（根据纠错级别）
    const maxLengths = {
      'L': 2953,
      'M': 2331,
      'Q': 1663,
      'H': 1273
    }
    
    if (text.length > maxLengths.L) {
      return { valid: false, error: '文本内容过长' }
    }
    
    return { valid: true }
  }

  /**
   * Canvas转Blob
   * @param {HTMLCanvasElement} canvas Canvas元素
   * @returns {Promise} Blob对象
   */
  canvasToBlob(canvas) {
    return new Promise(resolve => {
      canvas.toBlob(resolve, 'image/png')
    })
  }

  /**
   * 加载图片
   * @param {string|File} source 图片源
   * @returns {Promise} Image对象
   */
  loadImage(source) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => resolve(img)
      img.onerror = () => reject(new Error('图片加载失败'))
      
      if (source instanceof File) {
        img.src = URL.createObjectURL(source)
      } else {
        img.src = source
      }
    })
  }

  /**
   * 延迟函数
   * @param {number} ms 延迟毫秒数
   * @returns {Promise}
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  /**
   * 下载二维码
   * @param {Object} result 生成结果
   * @param {string} filename 文件名
   */
  download(result, filename) {
    if (!result.blob) {
      throw new Error('无效的二维码数据')
    }
    
    const finalFilename = filename || this.generateFileName('qrcode.png')
    this.downloadFile(result.blob, finalFilename)
  }

  /**
   * 批量下载二维码
   * @param {Array} results 批量生成结果
   * @param {string} zipName 压缩包名称
   */
  async downloadBatch(results, zipName = 'qrcodes.zip') {
    const successResults = results.filter(r => r.success)
    
    if (successResults.length === 0) {
      throw new Error('没有可下载的二维码')
    }
    
    const files = successResults.map((result, index) => ({
      data: result.blob,
      filename: `qrcode_${index + 1}_${Date.now()}.png`,
      mimeType: 'image/png'
    }))
    
    // 使用父类的批量下载方法
    await super.downloadBatch(files, zipName)
  }
}
