/**
 * 图片处理器
 * 提供图片压缩、格式转换、批量处理等功能
 */

import Compressor from 'compressorjs'
import { BatchProcessor } from '../base/BatchProcessor.js'

export class ImageProcessor extends BatchProcessor {
  constructor() {
    super()
    this.supportedFormats = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
    this.outputFormats = ['jpeg', 'png', 'webp']
  }

  /** 压缩单张图片 */
  compressImage(file, options = {}) {
    return new Promise((resolve, reject) => {
      const defaultOptions = {
        quality: 0.8,
        maxWidth: 1920,
        maxHeight: 1080,
        convertSize: 0,
        success: resolve,
        error: reject
      }
      const finalOptions = { ...defaultOptions, ...options }
      if (!options.lossless) {
        const isJpeg = (file.type && (file.type.includes('jpeg') || file.type.includes('jpg')))
        if (!finalOptions.mimeType) {
          finalOptions.mimeType = isJpeg ? 'image/jpeg' : 'image/jpeg'
        }
      }
      if (options.lossless) {
        this.losslessCompress(file, finalOptions).then(resolve).catch(reject)
      } else {
        new Compressor(file, finalOptions)
      }
    })
  }

  /** 无损压缩图片 */
  async losslessCompress(file, options = {}) {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()
      img.onload = () => {
        let { width, height } = img
        const { maxWidth, maxHeight } = options
        if (maxWidth && width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
        if (maxHeight && height > maxHeight) {
          width = (width * maxHeight) / height
          height = maxHeight
        }
        canvas.width = width
        canvas.height = height
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, width, height)
        let outputFormat = 'image/png'
        let quality = 1.0
        if (file.type === 'image/jpeg' && width === img.width && height === img.height) {
          outputFormat = 'image/jpeg'
          quality = 1.0
        }
        canvas.toBlob(
          (blob) => {
            if (blob) {
              if (blob.size > file.size) {
                resolve(file)
              } else {
                resolve(blob)
              }
            } else {
              reject(new Error('无损压缩失败'))
            }
          },
          outputFormat,
          quality
        )
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = URL.createObjectURL(file)
    })
  }

  /** 按目标大小压缩 */
  async compressToTargetSize(file, targetSize, options = {}) {
    if (targetSize >= file.size) {
      return file
    }
    const initialQuality = await this.estimateQualityForTargetSize(file, targetSize, options)
    const maxAttempts = 8
    let currentQuality = initialQuality
    let minQuality = 0.05
    let maxQuality = 1.0
    let bestResult = null
    let bestDiff = Infinity
    const tolerance = 0.15
    for (let attempt = 0; attempt < maxAttempts; attempt++) {
      try {
        const compressedFile = await this.compressImage(file, {
          ...options,
          mimeType: (options && options.mimeType) ? options.mimeType : 'image/jpeg',
          quality: currentQuality
        })
        const sizeDiff = Math.abs(compressedFile.size - targetSize)
        const sizeRatio = compressedFile.size / targetSize
        if (sizeDiff < bestDiff) {
          bestDiff = sizeDiff
          bestResult = compressedFile
        }
        if (sizeRatio >= (1 - tolerance) && sizeRatio <= (1 + tolerance)) {
          return compressedFile
        }
        if (compressedFile.size > targetSize) {
          maxQuality = currentQuality
          const overshoot = compressedFile.size / targetSize
          if (overshoot > 2.0) {
            currentQuality = (minQuality + currentQuality) / 2
          } else if (overshoot > 1.5) {
            currentQuality = minQuality + (currentQuality - minQuality) * 0.6
          } else {
            currentQuality = minQuality + (currentQuality - minQuality) * 0.8
          }
        } else {
          minQuality = currentQuality
          const undershoot = targetSize / compressedFile.size
          if (undershoot > 2.0) {
            currentQuality = (currentQuality + maxQuality) / 2
          } else if (undershoot > 1.5) {
            currentQuality = currentQuality + (maxQuality - currentQuality) * 0.4
          } else {
            currentQuality = currentQuality + (maxQuality - currentQuality) * 0.2
          }
        }
        if (maxQuality - minQuality < 0.01) {
          break
        }
        currentQuality = Math.max(minQuality, Math.min(maxQuality, currentQuality))
      } catch (error) {
        console.warn(`压缩尝试 ${attempt + 1} 失败:`, error)
        currentQuality *= 0.7
        if (currentQuality < 0.05) {
          throw error
        }
      }
    }
    return bestResult || file
  }

  /** 估算达到目标大小的质量 */
  async estimateQualityForTargetSize(file, targetSize, options = {}) {
    try {
      const targetRatio = targetSize / file.size
      let estimatedQuality
      if (file.type.includes('png')) {
        if (targetRatio < 0.3) {
          estimatedQuality = Math.pow(targetRatio, 0.6) * 0.9
        } else {
          estimatedQuality = Math.sqrt(targetRatio) * 1.1
        }
      } else if (file.type.includes('jpeg') || file.type.includes('jpg')) {
        if (targetRatio < 0.2) {
          estimatedQuality = Math.pow(targetRatio, 0.5) * 0.8
        } else {
          estimatedQuality = Math.pow(targetRatio, 0.7) * 1.0
        }
      } else {
        estimatedQuality = Math.pow(targetRatio, 0.75)
      }
      if (file.size > 5 * 1024 * 1024) {
        estimatedQuality *= 0.85
      } else if (file.size > 2 * 1024 * 1024) {
        estimatedQuality *= 0.9
      }
      if (targetSize < 100 * 1024) {
        estimatedQuality *= 0.8
      } else if (targetSize < 500 * 1024) {
        estimatedQuality *= 0.9
      }
      return Math.max(0.05, Math.min(0.9, estimatedQuality))
    } catch (error) {
      console.warn('质量预估失败:', error)
      return 0.4
    }
  }

  /** 估算压缩后大小 */
  async estimateCompressedSize(file, quality, options = {}) {
    try {
      const estimatedSize = await this.advancedSizeEstimation(file, quality, options)
      return estimatedSize
    } catch (error) {
      return this.basicSizeEstimation(file, quality)
    }
  }

  /** 高级估算 */
  async advancedSizeEstimation(file, quality, options = {}) {
    const imageInfo = await this.getImageInfo(file)
    const { width, height, type } = imageInfo
    const pixelCount = width * height
    const complexityFactor = await this.calculateImageComplexity(file)
    const formatCoefficients = this.getFormatCompressionCoefficients(type)
    const qualityFactor = this.calculateQualityFactor(quality)
    const sizeFactor = this.calculateSizeFactor(pixelCount)
    let estimatedSize = file.size * qualityFactor * formatCoefficients.base * complexityFactor * sizeFactor
    if (options.maxWidth || options.maxHeight) {
      const resizeFactor = this.calculateResizeFactor(width, height, options.maxWidth, options.maxHeight)
      estimatedSize *= resizeFactor
    }
    estimatedSize = Math.max(estimatedSize, file.size * 0.05)
    estimatedSize = Math.min(estimatedSize, file.size * 0.95)
    return Math.floor(estimatedSize)
  }

  /** 基础估算 */
  basicSizeEstimation(file, quality) {
    const qualityFactor = Math.pow(quality, 0.8)
    const formatFactor = file.type.includes('png') ? 0.7 : 0.6
    return Math.floor(file.size * qualityFactor * formatFactor)
  }

  /** 复杂度因子 */
  async calculateImageComplexity(file) {
    return new Promise((resolve) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()
      img.onload = () => {
        const sampleSize = 100
        canvas.width = sampleSize
        canvas.height = sampleSize
        ctx.drawImage(img, 0, 0, sampleSize, sampleSize)
        try {
          const imageData = ctx.getImageData(0, 0, sampleSize, sampleSize)
          const data = imageData.data
          let totalVariation = 0
          let colorVariance = 0
          const colors = []
          for (let i = 0; i < data.length; i += 4) {
            const r = data[i]
            const g = data[i + 1]
            const b = data[i + 2]
            colors.push([r, g, b])
            if (i > 0) {
              const prevR = data[i - 4]
              const prevG = data[i - 3]
              const prevB = data[i - 2]
              totalVariation += Math.abs(r - prevR) + Math.abs(g - prevG) + Math.abs(b - prevB)
            }
          }
          const uniqueColors = new Set(colors.map(c => `${c[0]},${c[1]},${c[2]}`)).size
          const colorDiversity = uniqueColors / (sampleSize * sampleSize)
          const normalizedVariation = totalVariation / (data.length * 255 * 3)
          const complexityFactor = 0.5 + (normalizedVariation * 0.3) + (colorDiversity * 0.7)
          resolve(Math.min(Math.max(complexityFactor, 0.5), 1.5))
        } catch (error) {
          resolve(1.0)
        }
      }
      img.onerror = () => resolve(1.0)
      img.src = URL.createObjectURL(file)
    })
  }

  /** 各格式压缩系数 */
  getFormatCompressionCoefficients(mimeType) {
    const coefficients = {
      'image/jpeg': { base: 0.6, quality: 0.8 },
      'image/jpg': { base: 0.6, quality: 0.8 },
      'image/png': { base: 0.75, quality: 0.9 },
      'image/webp': { base: 0.5, quality: 0.7 },
      'image/gif': { base: 0.8, quality: 0.95 }
    }
    return coefficients[mimeType] || coefficients['image/jpeg']
  }

  /** 质量因子 */
  calculateQualityFactor(quality) {
    return Math.pow(quality, 0.7) * 0.9 + 0.1
  }

  /** 尺寸因子 */
  calculateSizeFactor(pixelCount) {
    const megaPixels = pixelCount / (1024 * 1024)
    if (megaPixels > 10) return 0.85
    if (megaPixels > 5) return 0.9
    if (megaPixels > 2) return 0.95
    if (megaPixels > 0.5) return 1.0
    return 1.1
  }

  /** 尺寸调整因子 */
  calculateResizeFactor(originalWidth, originalHeight, maxWidth, maxHeight) {
    if (!maxWidth && !maxHeight) return 1.0
    const widthRatio = maxWidth ? maxWidth / originalWidth : 1
    const heightRatio = maxHeight ? maxHeight / originalHeight : 1
    const resizeRatio = Math.min(widthRatio, heightRatio, 1)
    return resizeRatio * resizeRatio
  }

  /** 格式转换 */
  convertFormat(file, targetFormat, options = {}) {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()
      img.onload = () => {
        canvas.width = img.width
        canvas.height = img.height
        ctx.drawImage(img, 0, 0)
        const quality = options.quality || 0.9
        const mimeType = `image/${targetFormat}`
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('格式转换失败'))
            }
          },
          mimeType,
          quality
        )
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = URL.createObjectURL(file)
    })
  }

  /** 尺寸调整 */
  resizeImage(file, dimensions, options = {}) {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()
      img.onload = () => {
        const { width, height, mode = 'contain' } = dimensions
        let newWidth, newHeight
        if (mode === 'exact') {
          newWidth = width
          newHeight = height
        } else if (mode === 'contain') {
          const ratio = Math.min(width / img.width, height / img.height)
          newWidth = img.width * ratio
          newHeight = img.height * ratio
        } else if (mode === 'cover') {
          const ratio = Math.max(width / img.width, height / img.height)
          newWidth = img.width * ratio
          newHeight = img.height * ratio
        }
        canvas.width = newWidth
        canvas.height = newHeight
        if (options.backgroundColor) {
          ctx.fillStyle = options.backgroundColor
          ctx.fillRect(0, 0, newWidth, newHeight)
        }
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, newWidth, newHeight)
        let format = 'jpeg'
        let quality = 0.92
        if (options.preserveQuality) {
          if (file.type === 'image/png' || file.type === 'image/gif') {
            format = 'png'
            quality = 1.0
          } else {
            quality = 0.98
          }
        }
        if (options.keepOriginalFormat) {
          if (file.type === 'image/png') {
            format = 'png'
            quality = options.preserveQuality ? 1.0 : 0.95
          } else if (file.type === 'image/jpeg') {
            format = 'jpeg'
            quality = options.preserveQuality ? 0.98 : 0.92
          }
        }
        const mimeType = `image/${format}`
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('尺寸调整失败'))
            }
          },
          mimeType,
          quality
        )
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = URL.createObjectURL(file)
    })
  }

  /** 添加水印 */
  addWatermark(file, watermark, options = {}) {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()
      img.onload = async () => {
        canvas.width = img.width
        canvas.height = img.height
        ctx.drawImage(img, 0, 0)
        try {
          if (watermark.type === 'text') {
            await this.addTextWatermark(ctx, watermark, canvas.width, canvas.height)
          } else if (watermark.type === 'image') {
            await this.addImageWatermark(ctx, watermark, canvas.width, canvas.height)
          }
          const quality = options.quality || 0.9
          const format = options.format || 'png'
          const mimeType = `image/${format}`
          canvas.toBlob(
            (blob) => {
              if (blob) {
                resolve(blob)
              } else {
                reject(new Error('水印添加失败'))
              }
            },
            mimeType,
            quality
          )
        } catch (error) {
          reject(error)
        }
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = URL.createObjectURL(file)
    })
  }

  addTextWatermark(ctx, watermark, canvasWidth, canvasHeight) {
    const { text, fontSize = 24, fontFamily = 'Arial', color = 'rgba(255, 255, 255, 0.8)', position = 'bottom-right', margin = 20, rotation = 0 } = watermark
    ctx.save()
    ctx.font = `${fontSize}px ${fontFamily}`
    ctx.fillStyle = color
    ctx.textBaseline = 'bottom'
    const textMetrics = ctx.measureText(text)
    const textWidth = textMetrics.width
    const textHeight = fontSize
    let x, y
    switch (position) {
      case 'top-left': x = margin; y = margin + textHeight; break
      case 'top-right': x = canvasWidth - textWidth - margin; y = margin + textHeight; break
      case 'bottom-left': x = margin; y = canvasHeight - margin; break
      case 'bottom-right': x = canvasWidth - textWidth - margin; y = canvasHeight - margin; break
      case 'center': x = (canvasWidth - textWidth) / 2; y = (canvasHeight + textHeight) / 2; break
      default: x = canvasWidth - textWidth - margin; y = canvasHeight - margin
    }
    if (rotation !== 0) {
      ctx.translate(x + textWidth / 2, y - textHeight / 2)
      ctx.rotate((rotation * Math.PI) / 180)
      ctx.translate(-(textWidth / 2), textHeight / 2)
      ctx.fillText(text, 0, 0)
    } else {
      ctx.fillText(text, x, y)
    }
    ctx.restore()
  }

  addImageWatermark(ctx, watermark, canvasWidth, canvasHeight) {
    return new Promise((resolve, reject) => {
      const { image, width = 100, height = 100, position = 'bottom-right', margin = 20, opacity = 0.8 } = watermark
      const watermarkImg = new Image()
      watermarkImg.onload = () => {
        ctx.save()
        ctx.globalAlpha = opacity
        let x, y
        switch (position) {
          case 'top-left': x = margin; y = margin; break
          case 'top-right': x = canvasWidth - width - margin; y = margin; break
          case 'bottom-left': x = margin; y = canvasHeight - height - margin; break
          case 'bottom-right': x = canvasWidth - width - margin; y = canvasHeight - height - margin; break
          case 'center': x = (canvasWidth - width) / 2; y = (canvasHeight - height) / 2; break
          default: x = canvasWidth - width - margin; y = canvasHeight - height - margin
        }
        ctx.drawImage(watermarkImg, x, y, width, height)
        ctx.restore()
        resolve()
      }
      watermarkImg.onerror = () => reject(new Error('水印图片加载失败'))
      if (image instanceof File) {
        watermarkImg.src = URL.createObjectURL(image)
      } else {
        watermarkImg.src = image
      }
    })
  }

  /** 批量重命名 */
  batchRename(files, renameOptions) {
    const { pattern = '{name}_{index}', startIndex = 1, prefix = '', suffix = '', extension = 'keep' } = renameOptions
    return files.map((file, index) => {
      const originalName = file.name.replace(/\.[^/.]+$/, '')
      const originalExt = file.name.split('.').pop().toLowerCase()
      let newName = pattern
        .replace('{name}', originalName)
        .replace('{index}', (startIndex + index).toString().padStart(3, '0'))
        .replace('{prefix}', prefix)
        .replace('{suffix}', suffix)
      let newExt
      if (extension === 'keep') {
        newExt = originalExt
      } else {
        newExt = extension
      }
      return { originalFile: file, originalName: file.name, newName: `${prefix}${newName}${suffix}.${newExt}`, index: index }
    })
  }

  /** 图片信息 */
  getImageInfo(file) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => {
        resolve({ width: img.width, height: img.height, size: file.size, type: file.type, name: file.name, aspectRatio: (img.width / img.height).toFixed(2) })
      }
      img.onerror = () => reject(new Error('图片信息获取失败'))
      img.src = URL.createObjectURL(file)
    })
  }

  /** 支持的输出格式 */
  getSupportedOutputFormats() {
    return [
      { value: 'jpeg', label: 'JPEG', description: '适合照片，文件较小' },
      { value: 'png', label: 'PNG', description: '支持透明，质量高' },
      { value: 'webp', label: 'WebP', description: '现代格式，压缩率高' }
    ]
  }

  /** 预设尺寸 */
  getPresetSizes() {
    return [
      { name: '原始尺寸', width: 0, height: 0 },
      { name: '1寸证件照 (295×413)', width: 295, height: 413 },
      { name: '2寸证件照 (413×579)', width: 413, height: 579 },
      { name: '小2寸证件照 (413×531)', width: 413, height: 531 },
      { name: '微信头像 (640×640)', width: 640, height: 640 },
      { name: '朋友圈封面 (1080×608)', width: 1080, height: 608 },
      { name: 'HD (1280×720)', width: 1280, height: 720 },
      { name: 'Full HD (1920×1080)', width: 1920, height: 1080 },
      { name: '淘宝主图 (800×800)', width: 800, height: 800 },
      { name: '公众号封面 (900×500)', width: 900, height: 500 },
      { name: '小红书封面 (1242×1660)', width: 1242, height: 1660 }
    ]
  }

  /** 质量预设 */
  getQualityPresets() {
    return [
      { index: 0, name: '高质量', value: 0.9, description: '质量优先' },
      { index: 1, name: '标准', value: 0.8, description: '平衡模式' },
      { index: 2, name: '压缩', value: 0.6, description: '大小优先' }
    ]
  }

  /** 压缩模式 */
  getCompressionModes() {
    return [
      { index: 0, value: 'size', label: '大小优先', description: '设置目标文件大小', icon: 'resize' },
      { index: 1, value: 'quality', label: '质量优先', description: '调整质量参数', icon: 'tune' },
      { index: 2, value: 'lossless', label: '无损压缩', description: '保持原始质量', icon: 'star' }
    ]
  }

  /** 格式化大小显示 */
  formatFileSize(bytes) {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /** 压缩比例 */
  calculateCompressionRatio(originalSize, compressedSize) {
    if (originalSize === 0) return 0
    return Math.round((1 - compressedSize / originalSize) * 100)
  }
}
