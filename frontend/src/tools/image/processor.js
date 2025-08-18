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

      // 如果是无损压缩模式
      if (options.lossless) {
        this.losslessCompress(file, finalOptions).then(resolve).catch(reject)
      } else {
        new Compressor(file, finalOptions)
      }
    })
  }

  /**
   * 无损压缩图片
   * @param {File} file 图片文件
   * @param {Object} options 压缩选项
   * @returns {Promise} 压缩结果
   */
  async losslessCompress(file, options = {}) {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()

      img.onload = () => {
        let { width, height } = img
        const { maxWidth, maxHeight } = options

        // 只进行尺寸调整，不降低质量
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

        // 使用高质量的图像渲染
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, width, height)

        // 根据原始格式选择最佳的无损格式
        let outputFormat = 'image/png' // PNG默认无损
        let quality = 1.0 // 无损质量

        // 如果原始是JPEG且尺寸没有变化，尝试保持原格式
        if (file.type === 'image/jpeg' && width === img.width && height === img.height) {
          outputFormat = 'image/jpeg'
          quality = 1.0 // JPEG的最高质量
        }

        canvas.toBlob(
          (blob) => {
            if (blob) {
              // 如果无损压缩后文件更大，返回原文件
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

  /**
   * 按目标文件大小压缩图片（优化版）
   * @param {File} file 图片文件
   * @param {number} targetSize 目标文件大小（字节）
   * @param {Object} options 其他选项
   * @returns {Promise} 压缩结果
   */
  async compressToTargetSize(file, targetSize, options = {}) {
    // 如果目标大小大于等于原文件大小，直接返回原文件
    if (targetSize >= file.size) {
      return file
    }

    // 首先使用预估算法获得初始质量
    const initialQuality = await this.estimateQualityForTargetSize(file, targetSize, options)

    const maxAttempts = 8 // 增加尝试次数以提高精度
    let currentQuality = initialQuality
    let minQuality = 0.05
    let maxQuality = 1.0
    let bestResult = null
    let bestDiff = Infinity

    // 设置更宽松的容差范围
    const tolerance = 0.15 // ±15%

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
      try {
        const compressedFile = await this.compressImage(file, {
          ...options,
          quality: currentQuality
        })

        const sizeDiff = Math.abs(compressedFile.size - targetSize)
        const sizeRatio = compressedFile.size / targetSize

        // 如果找到更接近目标大小的结果，保存它
        if (sizeDiff < bestDiff) {
          bestDiff = sizeDiff
          bestResult = compressedFile
        }

        // 如果文件大小在目标范围内，返回结果
        if (sizeRatio >= (1 - tolerance) && sizeRatio <= (1 + tolerance)) {
          return compressedFile
        }

        // 智能调整质量参数
        if (compressedFile.size > targetSize) {
          maxQuality = currentQuality
          // 如果超出太多，更激进地降低质量
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
          // 如果太小，适度提高质量
          const undershoot = targetSize / compressedFile.size
          if (undershoot > 2.0) {
            currentQuality = (currentQuality + maxQuality) / 2
          } else if (undershoot > 1.5) {
            currentQuality = currentQuality + (maxQuality - currentQuality) * 0.4
          } else {
            currentQuality = currentQuality + (maxQuality - currentQuality) * 0.2
          }
        }

        // 如果质量范围太小，停止尝试
        if (maxQuality - minQuality < 0.01) {
          break
        }

        // 防止质量参数超出范围
        currentQuality = Math.max(minQuality, Math.min(maxQuality, currentQuality))

      } catch (error) {
        console.warn(`压缩尝试 ${attempt + 1} 失败:`, error)
        // 如果压缩失败，降低质量重试
        currentQuality *= 0.7
        if (currentQuality < 0.05) {
          throw error
        }
      }
    }

    // 返回最接近目标大小的结果，如果没有更好的结果则返回原文件
    return bestResult || file
  }

  /**
   * 估算达到目标大小所需的质量参数
   * @param {File} file 原始文件
   * @param {number} targetSize 目标大小
   * @param {Object} options 其他选项
   * @returns {Promise<number>} 估算的质量参数
   */
  async estimateQualityForTargetSize(file, targetSize, options = {}) {
    try {
      const targetRatio = targetSize / file.size

      // 基于文件类型和大小的启发式估算
      let estimatedQuality

      if (file.type.includes('png')) {
        // PNG文件通常压缩率较低，特别是经过尺寸调整的PNG
        if (targetRatio < 0.3) {
          estimatedQuality = Math.pow(targetRatio, 0.6) * 0.9
        } else {
          estimatedQuality = Math.sqrt(targetRatio) * 1.1
        }
      } else if (file.type.includes('jpeg') || file.type.includes('jpg')) {
        // JPEG文件压缩效果较好
        if (targetRatio < 0.2) {
          estimatedQuality = Math.pow(targetRatio, 0.5) * 0.8
        } else {
          estimatedQuality = Math.pow(targetRatio, 0.7) * 1.0
        }
      } else {
        // 其他格式使用通用估算
        estimatedQuality = Math.pow(targetRatio, 0.75)
      }

      // 考虑文件大小的影响 - 大文件通常需要更低的质量
      if (file.size > 5 * 1024 * 1024) { // 5MB以上
        estimatedQuality *= 0.85
      } else if (file.size > 2 * 1024 * 1024) { // 2MB以上
        estimatedQuality *= 0.9
      }

      // 考虑目标大小的影响 - 非常小的目标大小需要更激进的压缩
      if (targetSize < 100 * 1024) { // 100KB以下
        estimatedQuality *= 0.8
      } else if (targetSize < 500 * 1024) { // 500KB以下
        estimatedQuality *= 0.9
      }

      // 限制在合理范围内
      return Math.max(0.05, Math.min(0.9, estimatedQuality))
    } catch (error) {
      console.warn('质量预估失败:', error)
      // 如果估算失败，返回较低的质量以确保能达到目标大小
      return 0.4
    }
  }

  /**
   * 估算压缩后的文件大小（改进版）
   * @param {File} file 原始文件
   * @param {number} quality 压缩质量 (0-1)
   * @param {Object} options 其他选项
   * @returns {Promise<number>} 估算的文件大小
   */
  async estimateCompressedSize(file, quality, options = {}) {
    try {
      // 使用改进的预估算法
      const estimatedSize = await this.advancedSizeEstimation(file, quality, options)
      return estimatedSize
    } catch (error) {
      // 如果预估失败，使用基础算法
      return this.basicSizeEstimation(file, quality)
    }
  }

  /**
   * 高级文件大小预估算法
   * 基于图片特征和压缩特性的拟合函数
   * @param {File} file 原始文件
   * @param {number} quality 压缩质量 (0-1)
   * @param {Object} options 其他选项
   * @returns {Promise<number>} 估算的文件大小
   */
  async advancedSizeEstimation(file, quality, options = {}) {
    // 获取图片基本信息
    const imageInfo = await this.getImageInfo(file)
    const { width, height, type } = imageInfo
    const pixelCount = width * height

    // 计算图片复杂度因子
    const complexityFactor = await this.calculateImageComplexity(file)

    // 根据图片格式确定基础压缩系数
    const formatCoefficients = this.getFormatCompressionCoefficients(type)

    // 质量参数的非线性映射
    const qualityFactor = this.calculateQualityFactor(quality)

    // 尺寸因子（大图片压缩效果更好）
    const sizeFactor = this.calculateSizeFactor(pixelCount)

    // 综合计算预估大小
    let estimatedSize = file.size * qualityFactor * formatCoefficients.base * complexityFactor * sizeFactor

    // 应用尺寸限制的影响
    if (options.maxWidth || options.maxHeight) {
      const resizeFactor = this.calculateResizeFactor(width, height, options.maxWidth, options.maxHeight)
      estimatedSize *= resizeFactor
    }

    // 确保最小值和最大值合理
    estimatedSize = Math.max(estimatedSize, file.size * 0.05) // 最小5%
    estimatedSize = Math.min(estimatedSize, file.size * 0.95) // 最大95%

    return Math.floor(estimatedSize)
  }

  /**
   * 基础文件大小预估算法（备用）
   * @param {File} file 原始文件
   * @param {number} quality 压缩质量 (0-1)
   * @returns {number} 估算的文件大小
   */
  basicSizeEstimation(file, quality) {
    // 改进的线性估算，考虑质量参数的非线性特性
    const qualityFactor = Math.pow(quality, 0.8) // 非线性映射
    const formatFactor = file.type.includes('png') ? 0.7 : 0.6 // PNG通常压缩率较低
    return Math.floor(file.size * qualityFactor * formatFactor)
  }

  /**
   * 计算图片复杂度因子
   * @param {File} file 图片文件
   * @returns {Promise<number>} 复杂度因子 (0.5-1.5)
   */
  async calculateImageComplexity(file) {
    return new Promise((resolve) => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      const img = new Image()

      img.onload = () => {
        // 缩放到小尺寸进行分析
        const sampleSize = 100
        canvas.width = sampleSize
        canvas.height = sampleSize
        ctx.drawImage(img, 0, 0, sampleSize, sampleSize)

        try {
          const imageData = ctx.getImageData(0, 0, sampleSize, sampleSize)
          const data = imageData.data

          // 计算颜色变化程度（边缘检测简化版）
          let totalVariation = 0
          let colorVariance = 0
          const colors = []

          for (let i = 0; i < data.length; i += 4) {
            const r = data[i]
            const g = data[i + 1]
            const b = data[i + 2]

            colors.push([r, g, b])

            // 计算与相邻像素的差异
            if (i > 0) {
              const prevR = data[i - 4]
              const prevG = data[i - 3]
              const prevB = data[i - 2]
              totalVariation += Math.abs(r - prevR) + Math.abs(g - prevG) + Math.abs(b - prevB)
            }
          }

          // 计算颜色多样性
          const uniqueColors = new Set(colors.map(c => `${c[0]},${c[1]},${c[2]}`)).size
          const colorDiversity = uniqueColors / (sampleSize * sampleSize)

          // 归一化变化程度
          const normalizedVariation = totalVariation / (data.length * 255 * 3)

          // 综合复杂度因子
          const complexityFactor = 0.5 + (normalizedVariation * 0.3) + (colorDiversity * 0.7)

          resolve(Math.min(Math.max(complexityFactor, 0.5), 1.5))
        } catch (error) {
          // 如果分析失败，返回中等复杂度
          resolve(1.0)
        }
      }

      img.onerror = () => resolve(1.0)
      img.src = URL.createObjectURL(file)
    })
  }

  /**
   * 获取格式压缩系数
   * @param {string} mimeType 图片MIME类型
   * @returns {Object} 压缩系数配置
   */
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

  /**
   * 计算质量因子（非线性映射）
   * @param {number} quality 质量参数 (0-1)
   * @returns {number} 质量因子
   */
  calculateQualityFactor(quality) {
    // 使用指数函数模拟JPEG压缩的非线性特性
    // 高质量时文件大小增长更快
    return Math.pow(quality, 0.7) * 0.9 + 0.1
  }

  /**
   * 计算尺寸因子
   * @param {number} pixelCount 像素总数
   * @returns {number} 尺寸因子
   */
  calculateSizeFactor(pixelCount) {
    // 大图片通常有更好的压缩效果
    const megaPixels = pixelCount / (1024 * 1024)
    if (megaPixels > 10) return 0.85      // 超大图片
    if (megaPixels > 5) return 0.9        // 大图片
    if (megaPixels > 2) return 0.95       // 中等图片
    if (megaPixels > 0.5) return 1.0      // 小图片
    return 1.1                            // 很小的图片
  }

  /**
   * 计算尺寸调整因子
   * @param {number} originalWidth 原始宽度
   * @param {number} originalHeight 原始高度
   * @param {number} maxWidth 最大宽度
   * @param {number} maxHeight 最大高度
   * @returns {number} 调整因子
   */
  calculateResizeFactor(originalWidth, originalHeight, maxWidth, maxHeight) {
    if (!maxWidth && !maxHeight) return 1.0

    const widthRatio = maxWidth ? maxWidth / originalWidth : 1
    const heightRatio = maxHeight ? maxHeight / originalHeight : 1
    const resizeRatio = Math.min(widthRatio, heightRatio, 1) // 不放大

    // 尺寸调整的影响是平方关系
    return resizeRatio * resizeRatio
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

        // 使用高质量渲染
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, newWidth, newHeight)

        // 智能选择输出格式和质量
        let format = 'jpeg'  // 默认使用JPEG，更适合后续压缩
        let quality = 0.92   // 高质量但不是无损，为后续压缩留出空间

        // 如果后续不需要压缩，使用无损质量
        if (options.preserveQuality) {
          if (file.type === 'image/png' || file.type === 'image/gif') {
            format = 'png'  // 保持PNG格式的无损特性
            quality = 1.0
          } else {
            quality = 0.98  // JPEG接近无损质量
          }
        }

        // 如果用户明确要求保持原格式
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

  /**
   * 获取质量预设
   * @returns {Array} 质量列表
   */
  getQualityPresets() {
    return [
      { index: 0, name: '高质量', value: 0.9, description: '质量优先' },
      { index: 1, name: '标准', value: 0.8, description: '平衡模式' },
      { index: 2, name: '压缩', value: 0.6, description: '大小优先' }
    ]
  }

  /**
   * 获取压缩模式选项
   * @returns {Array} 压缩模式列表
   */
  getCompressionModes() {
    return [
      {
        index: 0,
        value: 'size',
        label: '大小优先',
        description: '设置目标文件大小',
        icon: 'resize'
      },
      {
        index: 1,
        value: 'quality',
        label: '质量优先',
        description: '调整质量参数',
        icon: 'tune'
      },
      {
        index: 2,
        value: 'lossless',
        label: '无损压缩',
        description: '保持原始质量',
        icon: 'star'
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
