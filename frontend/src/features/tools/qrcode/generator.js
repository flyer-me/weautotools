/**
 * 二维码生成器（迁移）
 * 基于 qrcode 库实现二维码生成
 */

import QRCode from 'qrcode'
import { FileProcessor } from '../base/FileProcessor.js'

export class QRGenerator extends FileProcessor {
  constructor() {
    super()
    this.defaultOptions = {
      width: 256,
      margin: 2,
      color: { dark: '#000000', light: '#FFFFFF' },
      errorCorrectionLevel: 'M'
    }
  }

  async generate(text, options = {}) {
    if (!text || typeof text !== 'string') {
      throw new Error('请输入有效的文本内容')
    }
    const finalOptions = { ...this.defaultOptions, ...options }
    try {
      const dataURL = await QRCode.toDataURL(text, finalOptions)
      const canvas = document.createElement('canvas')
      await QRCode.toCanvas(canvas, text, finalOptions)
      const blob = await this.canvasToBlob(canvas)
      return { text, dataURL, canvas, blob, size: blob.size, width: canvas.width, height: canvas.height, options: finalOptions }
    } catch (error) {
      throw new Error(`二维码生成失败: ${error.message}`)
    }
  }

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
        results.push({ success: true, index: i, text, ...result })
      } catch (error) {
        results.push({ success: false, index: i, text, error: error.message })
      }
      if (onProgress) {
        onProgress({ completed: i + 1, total, percentage: Math.round(((i + 1) / total) * 100), currentText: text })
      }
      if (i < textList.length - 1) {
        await this.delay(10)
      }
    }
    return results
  }

  async generateWithLogo(text, logo, options = {}) {
    const qrResult = await this.generate(text, options)
    try {
      const logoImage = await this.loadImage(logo)
      const canvas = qrResult.canvas
      const ctx = canvas.getContext('2d')
      const logoSize = Math.min(canvas.width, canvas.height) / 5
      const logoX = (canvas.width - logoSize) / 2
      const logoY = (canvas.height - logoSize) / 2
      ctx.fillStyle = '#FFFFFF'
      ctx.fillRect(logoX - 5, logoY - 5, logoSize + 10, logoSize + 10)
      ctx.drawImage(logoImage, logoX, logoY, logoSize, logoSize)
      const newDataURL = canvas.toDataURL()
      const newBlob = await this.canvasToBlob(canvas)
      return { ...qrResult, dataURL: newDataURL, blob: newBlob, size: newBlob.size, hasLogo: true }
    } catch (error) {
      throw new Error(`Logo处理失败: ${error.message}`)
    }
  }

  async generateSVG(text, options = {}) {
    const finalOptions = { ...this.defaultOptions, ...options }
    try {
      const svgString = await QRCode.toString(text, { ...finalOptions, type: 'svg' })
      return { text, svg: svgString, type: 'svg', options: finalOptions }
    } catch (error) {
      throw new Error(`SVG二维码生成失败: ${error.message}`)
    }
  }

  getErrorCorrectionLevels() {
    return [
      { value: 'L', label: '低 (~7%)', description: '适用于清洁环境' },
      { value: 'M', label: '中 (~15%)', description: '默认级别' },
      { value: 'Q', label: '四分位 (~25%)', description: '适用于一般环境' },
      { value: 'H', label: '高 (~30%)', description: '适用于恶劣环境' }
    ]
  }

  getPresetSizes() {
    return [
      { value: 128, label: '小 (128x128)' },
      { value: 256, label: '中 (256x256)' },
      { value: 512, label: '大 (512x512)' },
      { value: 1024, label: '超大 (1024x1024)' }
    ]
  }

  validateText(text) {
    if (!text) return { valid: false, error: '请输入文本内容' }
    if (typeof text !== 'string') return { valid: false, error: '文本内容必须是字符串' }
    const maxLengths = { L: 2953, M: 2331, Q: 1663, H: 1273 }
    if (text.length > maxLengths.L) return { valid: false, error: '文本内容过长' }
    return { valid: true }
  }

  canvasToBlob(canvas) {
    return new Promise(resolve => { canvas.toBlob(resolve, 'image/png') })
  }

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

  delay(ms) { return new Promise(resolve => setTimeout(resolve, ms)) }

  download(result, filename) {
    if (!result.blob) throw new Error('无效的二维码数据')
    const finalFilename = filename || this.generateFileName('qrcode.png')
    this.downloadFile(result.blob, finalFilename)
  }

  async downloadBatch(results, zipName = 'qrcodes.zip') {
    const successResults = results.filter(r => r.success)
    if (successResults.length === 0) throw new Error('没有可下载的二维码')
    const files = successResults.map((result, index) => ({ data: result.blob, filename: `qrcode_${index + 1}_${Date.now()}.png`, mimeType: 'image/png' }))
    // 降级：逐个下载
    this.downloadIndividually(files)
  }
}
