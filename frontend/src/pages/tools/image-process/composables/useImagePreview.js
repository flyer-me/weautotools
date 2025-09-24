import { ref, computed } from 'vue'
import { ImageProcessor } from '@/features/tools/image/processor.js'

export function useImagePreview() {
  // 预览数据
  const previewData = ref({
    originalImage: '',
    processedImage: '',
    originalInfo: {},
    processedInfo: {},
    isProcessing: false
  })

  // 预览设置
  const previewSettings = ref({
    mode: 'side-by-side', // 'side-by-side' | 'slider'
    isZoomed: false,
    showInfo: false,
    autoPreview: true // 是否自动生成预览
  })

  // 工具实例
  const imageProcessor = new ImageProcessor()

  // 预览提示文本：仅展示 压缩/水印，不展示 尺寸调整
  const previewHint = computed(() => {
    return (selectedTypes) => {
      const shown = []
      if (selectedTypes.includes('compress')) shown.push('压缩')
      if (selectedTypes.includes('watermark')) shown.push('水印')
      const shownText = shown.length ? shown.join('、') : '无'
      const hideResize = selectedTypes.includes('resize') ? '；不展示：尺寸调整' : ''
      return `预览仅展示：${shownText}${hideResize}`
    }
  })

  // 防抖函数
  const debounce = (func, wait) => {
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

  // 防抖的预览更新函数
  const debouncedPreviewUpdate = debounce((file, options) => {
    if (file) {
      generateProcessedPreview(file, options)
    }
  }, 300)

  // 生成预览
  const generatePreview = async (file) => {
    if (!file || !previewSettings.value.autoPreview) return

    try {
      // 设置原始图片
      const originalUrl = URL.createObjectURL(file)
      previewData.value.originalImage = originalUrl
      previewData.value.originalInfo = await imageProcessor.getImageInfo(file)

      // 开始处理预览
      previewData.value.isProcessing = true
      previewData.value.processedImage = ''

    } catch (error) {
      console.error('预览生成失败:', error)
    } finally {
      previewData.value.isProcessing = false
    }
  }

  // 生成处理后的预览（仅应用 压缩/水印，不应用 尺寸调整）
  const generateProcessedPreview = async (file, options) => {
    if (!file || !options) return

    try {
      previewData.value.isProcessing = true
      let previewFile = file

      // 先应用水印（如启用）
      if (options.selectedTypes.includes('watermark')) {
        previewFile = await imageProcessor.addWatermark(previewFile, options.watermarkOptions)
      }

      // 再应用压缩（如启用）
      if (options.selectedTypes.includes('compress')) {
        if (options.compressOptions.mode === 'quality') {
          previewFile = await imageProcessor.compressImage(previewFile, {
            quality: options.compressOptions.quality / 100
          })
        } else if (options.compressOptions.mode === 'size') {
          previewFile = await imageProcessor.compressToTargetSize(
            previewFile,
            options.compressOptions.targetSizeBytes
          )
        } else if (options.compressOptions.mode === 'lossless') {
          previewFile = await imageProcessor.compressImage(previewFile, {
            lossless: true
          })
        }
      }

      // 若都未启用，则展示原图
      const processedUrl = URL.createObjectURL(previewFile)
      previewData.value.processedImage = processedUrl
      previewData.value.processedInfo = await imageProcessor.getImageInfo(previewFile)

    } catch (error) {
      console.error('处理预览生成失败:', error)
      previewData.value.processedImage = previewData.value.originalImage
      previewData.value.processedInfo = previewData.value.originalInfo
    } finally {
      previewData.value.isProcessing = false
    }
  }

  // 更新预览（防抖）
  const updatePreview = (file, options) => {
    debouncedPreviewUpdate(file, options)
  }

  // 清理预览数据
  const clearPreview = () => {
    if (previewData.value.originalImage) {
      URL.revokeObjectURL(previewData.value.originalImage)
    }
    if (previewData.value.processedImage && previewData.value.processedImage !== previewData.value.originalImage) {
      URL.revokeObjectURL(previewData.value.processedImage)
    }

    previewData.value = {
      originalImage: '',
      processedImage: '',
      originalInfo: {},
      processedInfo: {},
      isProcessing: false
    }
  }

  // 预览相关事件处理
  const handlePreviewModeChange = (mode) => {
    previewSettings.value.mode = mode
  }

  const handlePreviewZoom = (isZoomed) => {
    previewSettings.value.isZoomed = isZoomed
  }

  const handlePreviewInfo = (showInfo) => {
    previewSettings.value.showInfo = showInfo
  }

  // 预览图片
  const previewImage = (result) => {
    if (result.result) {
      const url = URL.createObjectURL(result.result)
      uni.previewImage({
        urls: [url],
        current: 0
      })
    }
  }

  return {
    // 状态
    previewData,
    previewSettings,
    previewHint,

    // 方法
    generatePreview,
    generateProcessedPreview,
    updatePreview,
    clearPreview,
    handlePreviewModeChange,
    handlePreviewZoom,
    handlePreviewInfo,
    previewImage,

    // 工具
    debouncedPreviewUpdate
  }
}
