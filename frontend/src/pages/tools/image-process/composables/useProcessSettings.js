import { ref, computed } from 'vue'
import { ImageProcessor } from '@/features/tools/image/processor.js'

export function useProcessSettings() {
  // 工具实例
  const imageProcessor = new ImageProcessor()

  // 选择的处理类型
  const selectedTypes = ref(['compress'])
  
  // 索引状态
  const selectedQualityIndex = ref(1) // 标准质量
  const selectedResizeModeIndex = ref(0) // 保持比例缩放
  const selectedWatermarkTypeIndex = ref(0) // 文字
  const selectedWatermarkPositionIndex = ref(3) // 右下角
  const selectedCompressionModeIndex = ref(0) // 大小优先
  const selectedSizeUnitIndex = ref(1) // KB

  // 压缩选项
  const compressOptions = ref({
    mode: 'size', // 'quality' | 'size' | 'lossless'
    quality: 80, // 0-100
    targetSize: 500, // 目标文件大小数值
    targetSizeBytes: 500 * 1024 // 目标文件大小字节数
  })

  // 尺寸调整选项
  const resizeOptions = ref({
    maxWidth: null, // 最大宽度，null表示不限制
    maxHeight: null // 最大高度，null表示不限制
  })

  // 水印选项
  const watermarkOptions = ref({
    type: 'text',
    text: '© WeAutoTools',
    fontSize: 24,
    position: 'bottom-right',
    color: 'rgba(255, 255, 255, 0.8)'
  })

  // 配置选项
  const processTypes = [
    { value: 'compress', name: '压缩', description: '减小文件大小' },
    { value: 'resize', name: '尺寸调整', description: '调整图片尺寸' },
    { value: 'watermark', name: '添加水印', description: '添加文字或图片水印' }
  ]

  const qualityPresets = imageProcessor.getQualityPresets()
  const compressionModes = imageProcessor.getCompressionModes()

  // 文件大小单位
  const sizeUnits = [
    { label: 'B', value: 1 },
    { label: 'KB', value: 1024 },
    { label: 'MB', value: 1024 * 1024 }
  ]

  // 常用文件大小预设
  const sizePresets = [
    { label: '100KB', value: 100, unit: 'KB' },
    { label: '500KB', value: 500, unit: 'KB' },
    { label: '1MB', value: 1, unit: 'MB' },
    { label: '2MB', value: 2, unit: 'MB' },
    { label: '5MB', value: 5, unit: 'MB' }
  ]

  // 常用尺寸预设（中国常用尺寸）
  const dimensionPresets = [
    { name: '1寸证件照', width: 295, height: 413 },
    { name: '2寸证件照', width: 413, height: 579 },
    { name: '小2寸证件照', width: 413, height: 531 },
    { name: '微信头像', width: 640, height: 640 },
    { name: '朋友圈封面', width: 1080, height: 608 },
    { name: '720P', width: 1280, height: 720 },
    { name: '1080P', width: 1920, height: 1080 },
    { name: '淘宝主图', width: 800, height: 800 },
    { name: '公众号封面', width: 900, height: 500 },
    { name: '无限制', width: null, height: null }
  ]

  const resizeModes = [
    { value: 'contain', label: '保持比例缩放', description: '等比例缩放，不会变形，可能有留白' },
    { value: 'cover', label: '填满尺寸缩放', description: '等比例缩放填满，不会变形，可能会裁剪' },
    { value: 'exact', label: '强制拉伸', description: '强制调整到指定尺寸，可能会变形' }
  ]

  const watermarkTypes = [
    { value: 'text', label: '文字水印' },
    { value: 'image', label: '图片水印' }
  ]

  const watermarkPositions = [
    { value: 'top-left', label: '左上角' },
    { value: 'top-right', label: '右上角' },
    { value: 'bottom-left', label: '左下角' },
    { value: 'bottom-right', label: '右下角' },
    { value: 'center', label: '居中' }
  ]

  // 计算属性
  const canProcess = computed(() => {
    return selectedTypes.value.length > 0
  })

  // 更新目标大小字节数
  const updateTargetSizeBytes = () => {
    const size = compressOptions.value.targetSize || 0
    const unit = sizeUnits[selectedSizeUnitIndex.value]
    compressOptions.value.targetSizeBytes = size * unit.value
  }

  // 重置所有设置
  const resetSettings = () => {
    selectedTypes.value = ['compress']
    selectedQualityIndex.value = 1
    selectedResizeModeIndex.value = 0
    selectedWatermarkTypeIndex.value = 0
    selectedWatermarkPositionIndex.value = 3
    selectedCompressionModeIndex.value = 0
    selectedSizeUnitIndex.value = 1

    compressOptions.value = {
      mode: 'size',
      quality: 80,
      targetSize: 500,
      targetSizeBytes: 500 * 1024
    }

    resizeOptions.value = {
      maxWidth: null,
      maxHeight: null
    }

    watermarkOptions.value = {
      type: 'text',
      text: '© WeAutoTools',
      fontSize: 24,
      position: 'bottom-right',
      color: 'rgba(255, 255, 255, 0.8)'
    }
  }

  // 获取当前处理选项（用于传递给处理器）
  const getProcessOptions = () => {
    return {
      selectedTypes: selectedTypes.value,
      compressOptions: compressOptions.value,
      resizeOptions: resizeOptions.value,
      watermarkOptions: watermarkOptions.value,
      selectedResizeModeIndex: selectedResizeModeIndex.value,
      resizeModes: resizeModes
    }
  }

  return {
    // 状态
    selectedTypes,
    selectedQualityIndex,
    selectedResizeModeIndex,
    selectedWatermarkTypeIndex,
    selectedWatermarkPositionIndex,
    selectedCompressionModeIndex,
    selectedSizeUnitIndex,
    compressOptions,
    resizeOptions,
    watermarkOptions,

    // 配置
    processTypes,
    qualityPresets,
    compressionModes,
    sizeUnits,
    sizePresets,
    dimensionPresets,
    resizeModes,
    watermarkTypes,
    watermarkPositions,

    // 计算属性
    canProcess,

    // 方法
    updateTargetSizeBytes,
    resetSettings,
    getProcessOptions
  }
}
