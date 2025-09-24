import { ref, computed } from 'vue'
import { ImageProcessor } from '@/features/tools/image/processor.js'
import { ProgressTracker } from '@/features/tools/base/ProgressTracker.js'
import { useUsageLimit } from '@/composables/useUsageLimit'

export function useImageProcessor() {
  // 响应式状态
  const isProcessing = ref(false)
  const progress = ref(0)
  const progressStatus = ref('normal')
  const progressMessage = ref('')
  const results = ref([])
  const statistics = ref({})
  const errorMessage = ref('')

  // 工具实例
  const imageProcessor = new ImageProcessor()

  // 使用限制
  const { useFrontendTool } = useUsageLimit()

  // 计算属性
  const canProcess = computed(() => {
    return results.value.length === 0 && !isProcessing.value
  })

  // 处理图片批次
  const processBatch = async (files, processOptions) => {
    if (!files || files.length === 0) {
      throw new Error('没有选择文件')
    }

    // 预检查使用限制
    const usageResult = await useFrontendTool('image-process', files.length)
    if (!usageResult.canUse) {
      return // 已显示限制提示
    }

    isProcessing.value = true
    progress.value = 0
    progressStatus.value = 'running'
    errorMessage.value = ''
    results.value = []

    try {
      const tracker = new ProgressTracker(files.length, (status) => {
        progress.value = status.percentage
        progressMessage.value = `正在处理第 ${status.current}/${status.total} 张图片...`
      })

      tracker.start()

      const processResults = await imageProcessor.processBatch(
        files,
        async (file) => {
          return await processImage(file, processOptions)
        },
        (progress) => {
          tracker.setCurrent(progress.completed, `正在处理: ${progress.currentFile}`)
        }
      )

      results.value = await Promise.all(processResults.map(async (result) => {
        // 安全地获取文件名
        const originalFile = result.file
        const fileName = originalFile?.name || 'unknown_file.jpg'

        // 提取文件名和扩展名
        let originalName, extension
        if (fileName.includes('.')) {
          const lastDotIndex = fileName.lastIndexOf('.')
          originalName = fileName.substring(0, lastDotIndex)
          extension = fileName.substring(lastDotIndex + 1)
        } else {
          originalName = fileName
          extension = 'jpg'
        }

        // 根据实际结果 MIME 决定最终扩展名
        const mime = result.success ? (result.result?.type || originalFile?.type) : (originalFile?.type || '')
        const mimeToExt = {
          'image/jpeg': 'jpg',
          'image/jpg': 'jpg',
          'image/png': 'png',
          'image/webp': 'webp',
          'image/gif': 'gif'
        }
        const finalExt = mimeToExt[mime] || extension
        const processedName = `${originalName}_done.${finalExt}`

        let thumbnail = null
        if (result.success && result.result) {
          try {
            thumbnail = URL.createObjectURL(result.result)
          } catch (error) {
            console.warn('Failed to create thumbnail:', error)
          }
        }

        return {
          success: result.success,
          file: {
            name: fileName,
            type: originalFile?.type || 'image/jpeg',
            processedName: processedName
          },
          result: result.success ? result.result : null,
          thumbnail: thumbnail,
          originalSize: originalFile?.size || 0,
          compressedSize: result.success ? (result.result?.size || 0) : 0,
          error: result.error
        }
      }))

      progressStatus.value = 'completed'
      progressMessage.value = '处理完成'

      // 计算统计信息
      updateStatistics()

      tracker.complete()

      // 成功后报告使用
      const successfulCount = results.value.filter(r => r.success).length
      await usageResult.reportUsage(successfulCount)

    } catch (error) {
      progressStatus.value = 'error'
      progressMessage.value = '处理失败'
      errorMessage.value = error.message
      console.error('批量处理失败:', error)
    } finally {
      isProcessing.value = false
    }
  }

  // 处理单个图片
  const processImage = async (file, options) => {
    let processedFile = file

    // 按优化顺序执行各种处理：尺寸调整 → 水印 → 压缩
    if (options.selectedTypes.includes('resize')) {
      processedFile = await processResize(processedFile, options)
    }

    if (options.selectedTypes.includes('watermark')) {
      processedFile = await processWatermark(processedFile, options)
    }

    if (options.selectedTypes.includes('compress')) {
      processedFile = await processCompress(processedFile, options)
    }

    return processedFile
  }

  // 尺寸调整处理
  const processResize = async (file, options) => {
    const { resizeOptions, selectedResizeModeIndex, resizeModes, selectedTypes } = options

    if (resizeOptions.maxWidth || resizeOptions.maxHeight) {
      const imageInfo = await imageProcessor.getImageInfo(file)
      const maxWidth = resizeOptions.maxWidth || imageInfo.width
      const maxHeight = resizeOptions.maxHeight || imageInfo.height
      const mode = resizeModes[selectedResizeModeIndex].value

      // 检查是否需要调整
      const needResize = (resizeOptions.maxWidth && imageInfo.width > resizeOptions.maxWidth) ||
                       (resizeOptions.maxHeight && imageInfo.height > resizeOptions.maxHeight)

      if (needResize) {
        // 判断是否后续需要压缩，如果需要压缩则不使用无损质量
        const willCompress = selectedTypes.includes('compress')

        return await imageProcessor.resizeImage(file, {
          width: maxWidth,
          height: maxHeight,
          mode: mode
        }, {
          preserveQuality: !willCompress,
          keepOriginalFormat: false
        })
      }
    }

    return file
  }

  // 水印处理
  const processWatermark = async (file, options) => {
    return await imageProcessor.addWatermark(file, options.watermarkOptions)
  }

  // 压缩处理
  const processCompress = async (file, options) => {
    const { compressOptions } = options

    if (compressOptions.mode === 'quality') {
      return await imageProcessor.compressImage(file, {
        mimeType: 'image/jpeg',
        quality: compressOptions.quality / 100
      })
    } else if (compressOptions.mode === 'size') {
      return await imageProcessor.compressToTargetSize(
        file,
        compressOptions.targetSizeBytes,
        { mimeType: 'image/jpeg' }
      )
    } else if (compressOptions.mode === 'lossless') {
      return await imageProcessor.compressImage(file, {
        lossless: true
      })
    }

    return file
  }

  // 更新统计信息
  const updateStatistics = () => {
    const successful = results.value.filter(r => r.success)
    const failed = results.value.filter(r => !r.success)
    
    const originalSize = results.value.reduce((sum, r) => sum + r.originalSize, 0)
    const processedSize = successful.reduce((sum, r) => sum + r.compressedSize, 0)
    
    statistics.value = {
      total: results.value.length,
      successful: successful.length,
      failed: failed.length,
      successRate: `${((successful.length / results.value.length) * 100).toFixed(1)}%`,
      originalSize: formatFileSize(originalSize),
      processedSize: formatFileSize(processedSize),
      compressionRatio: originalSize > 0 ? 
        `${(((originalSize - processedSize) / originalSize) * 100).toFixed(1)}%` : '0%'
    }
  }

  // 格式化文件大小
  const formatFileSize = (size) => {
    if (!size) return '0B'
    if (size < 1024) return `${size}B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
    if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)}MB`
    return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
  }

  // 下载单个文件
  const downloadFile = (result) => {
    if (result.result) {
      const filename = result.file.processedName || result.file.name
      imageProcessor.downloadFile(result.result, filename)
    }
  }

  // 批量下载
  const downloadAll = async () => {
    try {
      const files = results.value
        .filter(r => r.success)
        .map((result) => {
          return {
            data: result.result,
            filename: result.file.processedName || result.file.name,
            mimeType: result.result.type
          }
        })

      await imageProcessor.downloadBatch(files, 'processed_images.zip')
    } catch (error) {
      uni.showToast({
        title: error.message,
        icon: 'none'
      })
    }
  }

  // 清理结果
  const clearResults = () => {
    // 清理缩略图URL
    results.value.forEach(result => {
      if (result.thumbnail) {
        URL.revokeObjectURL(result.thumbnail)
      }
    })

    results.value = []
    statistics.value = {}
    errorMessage.value = ''
    progress.value = 0
    progressStatus.value = 'normal'
    progressMessage.value = ''
  }

  // 验证图片格式
  const validateImageFormat = (file) => {
    return imageProcessor.validateFormat(file)
  }

  return {
    // 状态
    isProcessing,
    progress,
    progressStatus,
    progressMessage,
    results,
    statistics,
    errorMessage,
    canProcess,

    // 方法
    processBatch,
    downloadFile,
    downloadAll,
    clearResults,
    validateImageFormat,
    formatFileSize,

    // 工具实例
    imageProcessor
  }
}
