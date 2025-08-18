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

  /**
   * 压缩单张图片
   * @param {File} file 图片文件
   * @param {Object} options 压缩选项
   * @returns {Promise} 压缩结果
   */
  compressImage(file, options = {}) {
    return new Promise((resolve, reject) => {
      const defaultOptions = {
        quality: 0.8,
        maxWidth: 1920,
        maxHeight: 1080,
        convertSize: 5000000, // 5MB以上转换为JPEG
        success: resolve,
        error: reject
      }

      const finalOptions = { ...defaultOptions, ...options }

      new Compressor(file, finalOptions)
    })
  }

  /**
   * 按目标文件大小压缩图片
   * @param {File} file 图片文件
   * @param {number} targetSize 目标文件大小（字节）
   * @param {Object} options 其他选项
   * @returns {Promise} 压缩结果
   */
  async compressToTargetSize(file, targetSize, options = {}) {
    const maxAttempts = 8
    let currentQuality = 0.9
    let minQuality = 0.1
    let maxQuality = 1.0
    let bestResult = null
    let bestDiff = Infinity

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
      try {
        const compressedFile = await this.compressImage(file, {
          ...options,
          quality: currentQuality
        })

        const sizeDiff = Math.abs(compressedFile.size - targetSize)

        // 如果找到更接近目标大小的结果，保存它
        if (sizeDiff < bestDiff) {
          bestDiff = sizeDiff
          bestResult = compressedFile
        }

        // 如果文件大小在目标范围内（±5%），返回结果
        if (compressedFile.size <= targetSize * 1.05 && compressedFile.size >= targetSize * 0.95) {
          return compressedFile
        }

        // 调整质量参数
        if (compressedFile.size > targetSize) {
          maxQuality = currentQuality
          currentQuality = (minQuality + currentQuality) / 2
        } else {
          minQuality = currentQuality
          currentQuality = (currentQuality + maxQuality) / 2
        }

        // 如果质量范围太小，停止尝试
        if (maxQuality - minQuality < 0.01) {
          break
        }
      } catch (error) {
        // 如果压缩失败，降低质量重试
        currentQuality *= 0.8
        if (currentQuality < 0.1) {
          throw error
        }
      }
    }

    // 返回最接近目标大小的结果
    return bestResult || file
  }

  /**
   * 估算压缩后的文件大小
   * @param {File} file 原始文件
   * @param {number} quality 压缩质量 (0-1)
   * @param {Object} options 其他选项
   * @returns {Promise<number>} 估算的文件大小
   */
  async estimateCompressedSize(file, quality, options = {}) {
    try {
      const compressed = await this.compressImage(file, {
        ...options,
        quality: quality
      })
      return compressed.size
    } catch (error) {
      // 如果压缩失败，返回原始大小的估算值
      return Math.floor(file.size * quality)
    }
  }

  /**
   * 转换图片格式
   * @param {File} file 图片文件
   * @param {string} targetFormat 目标格式 (jpeg, png, webp)
   * @param {Object} options 转换选项
   * @returns {Promise} 转换结果
   */
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

  /**
   * 调整图片尺寸
   * @param {File} file 图片文件
   * @param {Object} dimensions 尺寸设置
   * @param {Object} options 其他选项
   * @returns {Promise} 调整结果
   */
  resizeImage(file, dimensions, options = {}) {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()

      img.onload = () => {
        const { width, height, mode = 'contain' } = dimensions
        
        let newWidth, newHeight
        
        if (mode === 'exact') {
          // 精确尺寸
          newWidth = width
          newHeight = height
        } else if (mode === 'contain') {
          // 等比缩放，保持在指定尺寸内
          const ratio = Math.min(width / img.width, height / img.height)
          newWidth = img.width * ratio
          newHeight = img.height * ratio
        } else if (mode === 'cover') {
          // 等比缩放，填满指定尺寸
          const ratio = Math.max(width / img.width, height / img.height)
          newWidth = img.width * ratio
          newHeight = img.height * ratio
        }

        canvas.width = newWidth
        canvas.height = newHeight
        
        // 设置背景色（用于透明图片）
        if (options.backgroundColor) {
          ctx.fillStyle = options.backgroundColor
          ctx.fillRect(0, 0, newWidth, newHeight)
        }

        ctx.drawImage(img, 0, 0, newWidth, newHeight)

        const quality = options.quality || 0.9
        const format = options.format || 'png'
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

  /**
   * 添加水印
   * @param {File} file 图片文件
   * @param {Object} watermark 水印设置
   * @param {Object} options 其他选项
   * @returns {Promise} 添加水印后的图片
   */
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

  /**
   * 添加文字水印
   * @param {CanvasRenderingContext2D} ctx Canvas上下文
   * @param {Object} watermark 水印设置
   * @param {number} canvasWidth 画布宽度
   * @param {number} canvasHeight 画布高度
   */
  addTextWatermark(ctx, watermark, canvasWidth, canvasHeight) {
    const {
      text,
      fontSize = 24,
      fontFamily = 'Arial',
      color = 'rgba(255, 255, 255, 0.8)',
      position = 'bottom-right',
      margin = 20,
      rotation = 0
    } = watermark

    ctx.save()
    
    // 设置字体
    ctx.font = `${fontSize}px ${fontFamily}`
    ctx.fillStyle = color
    ctx.textBaseline = 'bottom'

    // 计算文字尺寸
    const textMetrics = ctx.measureText(text)
    const textWidth = textMetrics.width
    const textHeight = fontSize

    // 计算位置
    let x, y
    switch (position) {
      case 'top-left':
        x = margin
        y = margin + textHeight
        break
      case 'top-right':
        x = canvasWidth - textWidth - margin
        y = margin + textHeight
        break
      case 'bottom-left':
        x = margin
        y = canvasHeight - margin
        break
      case 'bottom-right':
        x = canvasWidth - textWidth - margin
        y = canvasHeight - margin
        break
      case 'center':
        x = (canvasWidth - textWidth) / 2
        y = (canvasHeight + textHeight) / 2
        break
      default:
        x = canvasWidth - textWidth - margin
        y = canvasHeight - margin
    }

    // 应用旋转
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

  /**
   * 添加图片水印
   * @param {CanvasRenderingContext2D} ctx Canvas上下文
   * @param {Object} watermark 水印设置
   * @param {number} canvasWidth 画布宽度
   * @param {number} canvasHeight 画布高度
   * @returns {Promise}
   */
  addImageWatermark(ctx, watermark, canvasWidth, canvasHeight) {
    return new Promise((resolve, reject) => {
      const {
        image,
        width = 100,
        height = 100,
        position = 'bottom-right',
        margin = 20,
        opacity = 0.8
      } = watermark

      const watermarkImg = new Image()
      
      watermarkImg.onload = () => {
        ctx.save()
        ctx.globalAlpha = opacity

        // 计算位置
        let x, y
        switch (position) {
          case 'top-left':
            x = margin
            y = margin
            break
          case 'top-right':
            x = canvasWidth - width - margin
            y = margin
            break
          case 'bottom-left':
            x = margin
            y = canvasHeight - height - margin
            break
          case 'bottom-right':
            x = canvasWidth - width - margin
            y = canvasHeight - height - margin
            break
          case 'center':
            x = (canvasWidth - width) / 2
            y = (canvasHeight - height) / 2
            break
          default:
            x = canvasWidth - width - margin
            y = canvasHeight - height - margin
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

  /**
   * 批量重命名
   * @param {Array} files 文件列表
   * @param {Object} renameOptions 重命名选项
   * @returns {Array} 重命名后的文件信息
   */
  batchRename(files, renameOptions) {
    const {
      pattern = '{name}_{index}',
      startIndex = 1,
      prefix = '',
      suffix = '',
      extension = 'keep' // keep, jpg, png, webp
    } = renameOptions

    return files.map((file, index) => {
      const originalName = file.name.replace(/\.[^/.]+$/, '')
      const originalExt = file.name.split('.').pop().toLowerCase()
      
      let newName = pattern
        .replace('{name}', originalName)
        .replace('{index}', (startIndex + index).toString().padStart(3, '0'))
        .replace('{prefix}', prefix)
        .replace('{suffix}', suffix)

      // 处理扩展名
      let newExt
      if (extension === 'keep') {
        newExt = originalExt
      } else {
        newExt = extension
      }

      return {
        originalFile: file,
        originalName: file.name,
        newName: `${prefix}${newName}${suffix}.${newExt}`,
        index: index
      }
    })
  }

  /**
   * 获取图片信息
   * @param {File} file 图片文件
   * @returns {Promise} 图片信息
   */
  getImageInfo(file) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      
      img.onload = () => {
        resolve({
          width: img.width,
          height: img.height,
          size: file.size,
          type: file.type,
          name: file.name,
          aspectRatio: (img.width / img.height).toFixed(2)
        })
      }

      img.onerror = () => reject(new Error('图片信息获取失败'))
      img.src = URL.createObjectURL(file)
    })
  }

  /**
   * 获取支持的输出格式
   * @returns {Array} 格式列表
   */
  getSupportedOutputFormats() {
    return [
      { value: 'jpeg', label: 'JPEG', description: '适合照片，文件较小' },
      { value: 'png', label: 'PNG', description: '支持透明，质量高' },
      { value: 'webp', label: 'WebP', description: '现代格式，压缩率高' }
    ]
  }

  /**
   * 获取预设尺寸
   * @returns {Array} 尺寸列表
   */
  getPresetSizes() {
    return [
      { name: '原始尺寸', width: 0, height: 0 },
      { name: '4K (3840×2160)', width: 3840, height: 2160 },
      { name: '2K (2560×1440)', width: 2560, height: 1440 },
      { name: 'Full HD (1920×1080)', width: 1920, height: 1080 },
      { name: 'HD (1280×720)', width: 1280, height: 720 },
      { name: '移动端 (750×1334)', width: 750, height: 1334 },
      { name: '正方形 (1080×1080)', width: 1080, height: 1080 }
    ]
  }

  /**
   * 获取质量预设
   * @returns {Array} 质量列表
   */
  getQualityPresets() {
    return [
      { name: '高质量', value: 0.9, description: '质量优先' },
      { name: '标准', value: 0.8, description: '平衡模式' },
      { name: '压缩', value: 0.6, description: '大小优先' }
    ]
  }

  /**
   * 获取压缩模式选项
   * @returns {Array} 压缩模式列表
   */
  getCompressionModes() {
    return [
      {
        value: 'size',
        label: '大小优先',
        description: '设置目标文件大小',
        icon: 'resize'
      },
      {
        value: 'quality',
        label: '质量优先',
        description: '调整质量参数',
        icon: 'tune'
      }
    ]
  }

  /**
   * 格式化文件大小显示
   * @param {number} bytes 字节数
   * @returns {string} 格式化的大小字符串
   */
  formatFileSize(bytes) {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * 计算压缩比例
   * @param {number} originalSize 原始大小
   * @param {number} compressedSize 压缩后大小
   * @returns {number} 压缩比例（百分比）
   */
  calculateCompressionRatio(originalSize, compressedSize) {
    if (originalSize === 0) return 0
    return Math.round((1 - compressedSize / originalSize) * 100)
  }
}
