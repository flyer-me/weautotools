<template>
  <ToolContainer
    title="图片处理工具"
    description="图片压缩、尺寸调整、添加水印"
    :showProgress="isProcessing"
    :progress="progress"
    :progressStatus="progressStatus"
    :progressMessage="progressMessage"
    :showResult="results.length > 0"
    :results="results"
    :error="errorMessage"
    @download="handleDownload"
    @download-all="handleDownloadAll"
    @clear="handleClear"
    @preview="handlePreview"
  >
    <!-- 文件上传区域 -->
    <FileUploader
      ref="fileUploaderRef"
      accept="image/*"
      :multiple="true"
      :maxCount="50"
      uploadTitle="选择图片"
      uploadDesc="支持 JPG、PNG、GIF、WebP 格式"
      acceptText="图片格式"
      :validateFormat="validateImageFormat"
      @change="handleFileChange"
      @error="handleFileError"
    />

    <!-- 图片预览区域 -->
    <view v-if="selectedFiles.length > 0 && previewData.originalImage" class="preview-section">
      <ImagePreviewCompare
        :originalImage="previewData.originalImage"
        :processedImage="previewData.processedImage"
        :originalInfo="previewData.originalInfo"
        :processedInfo="previewData.processedInfo"
        :isProcessing="previewData.isProcessing"
        @modeChange="handlePreviewModeChange"
        @zoom="handlePreviewZoom"
        @info="handlePreviewInfo"
      />
    </view>

    <!-- 处理选项 -->
    <view class="process-options">
      <view class="section-title">处理选项</view>
      
      <!-- 处理类型选择 -->
      <view class="option-group">
        <view class="option-label">处理类型</view>
        <view class="process-types">
          <view
            v-for="type in processTypes"
            :key="type.value"
            class="type-option"
            :class="{ active: selectedTypes.includes(type.value) }"
            @click="handleTypeToggle(type.value)"
          >
            <checkbox
              :value="type.value"
              :checked="selectedTypes.includes(type.value)"
              @click.stop
            />
            <text class="type-name">{{ type.name }}</text>
            <text class="type-desc">{{ type.description }}</text>
          </view>
        </view>
      </view>

      <!-- 压缩设置 -->
      <view v-if="selectedTypes.includes('compress')" class="option-group">
        <view class="option-label">压缩设置</view>

        <!-- 压缩模式选择 -->
        <view class="compression-mode-selector">
          <text class="mode-title">压缩方式</text>
          <view class="mode-options">
            <view
              v-for="(mode, index) in compressionModes"
              :key="index"
              class="mode-option"
              :class="{ active: selectedCompressionModeIndex === index }"
              @click="handleCompressionModeChange({ detail: { value: index } })"
            >
              <view class="mode-icon">
                <uni-icons :type="mode.icon" size="20" />
              </view>
              <text class="mode-name">{{ mode.label }}</text>
              <text class="mode-desc">{{ mode.description }}</text>
            </view>
          </view>
        </view>

        <!-- 质量优先模式 -->
        <view v-if="compressOptions.mode === 'quality'" class="compress-quality-section">
          <!-- 质量预设 -->
          <view class="quality-presets-container">
            <text class="preset-label">质量预设</text>
            <view class="quality-presets">
              <view
                v-for="(preset, index) in qualityPresets"
                :key="index"
                class="preset-btn"
                :class="{ active: selectedQualityIndex === index }"
                @click="handleQualityPresetClick(index)"
              >
                <text class="preset-name">{{ preset.name }}</text>
                <text class="preset-desc">{{ preset.description }}</text>
              </view>
            </view>
          </view>

          <!-- 精确质量调整 -->
          <view class="quality-slider-container">
            <view class="slider-header">
              <text class="slider-label">精确调整</text>
              <text class="slider-value">{{ compressOptions.quality }}%</text>
            </view>
            <view class="slider-wrapper">
              <slider
                :value="compressOptions.quality"
                :min="10"
                :max="100"
                :step="1"
                activeColor="#007AFF"
                backgroundColor="#E5E5E5"
                block-size="24"
                @change="handleQualitySliderChange"
                @changing="handleQualitySliderChanging"
              />
            </view>
          </view>
        </view>

        <!-- 大小优先模式 -->
        <view v-if="compressOptions.mode === 'size'" class="compress-size-section">
          <view class="setting-item">
            <text class="setting-label">目标文件大小</text>
            <view class="target-size-input">
              <input
                v-model.number="compressOptions.targetSize"
                type="number"
                class="number-input"
                placeholder="500"
                @input="handleTargetSizeChange"
              />
              <picker
                :range="sizeUnits"
                range-key="label"
                :value="selectedSizeUnitIndex"
                @change="handleSizeUnitChange"
              >
                <view class="unit-picker">
                  {{ sizeUnits[selectedSizeUnitIndex].label }}
                  <uni-icons type="arrowdown" size="12" color="#666" />
                </view>
              </picker>
            </view>
          </view>

          <!-- 目标大小预设 -->
          <view class="setting-item">
            <text class="setting-label">常用大小</text>
            <view class="size-presets">
              <view
                v-for="preset in sizePresets"
                :key="preset.value"
                class="preset-btn size-preset"
                @click="handleSizePresetClick(preset)"
              >
                {{ preset.label }}
              </view>
            </view>
          </view>
        </view>



        <!-- 压缩预览信息 -->
        <view v-if="selectedFiles.length > 0" class="compress-preview">
          <view class="preview-title">压缩预览</view>
          <view class="preview-stats">
            <view class="stat-item">
              <text class="stat-label">原始大小</text>
              <text class="stat-value">{{ formatTotalSize(selectedFiles) }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">预估压缩后</text>
              <text class="stat-value">{{ estimatedCompressedSize }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">压缩比例</text>
              <text class="stat-value compression-ratio">{{ estimatedCompressionRatio }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 尺寸限制设置 -->
      <view v-if="selectedTypes.includes('compress')" class="option-group">
        <view class="option-label">尺寸限制</view>

        <view class="dimension-limit-container">
          <view class="dimension-inputs">
            <view class="dimension-input">
              <text class="dimension-label">最大宽度</text>
              <input
                v-model.number="compressOptions.maxWidth"
                type="number"
                class="number-input"
                placeholder="1920"
              />
              <text class="unit">px</text>
            </view>
            <view class="dimension-input">
              <text class="dimension-label">最大高度</text>
              <input
                v-model.number="compressOptions.maxHeight"
                type="number"
                class="number-input"
                placeholder="1080"
              />
              <text class="unit">px</text>
            </view>
          </view>

          <view class="dimension-presets">
            <text class="preset-label">常用尺寸</text>
            <view class="preset-buttons">
              <view
                v-for="preset in dimensionPresets"
                :key="preset.name"
                class="preset-btn dimension-preset"
                @click="handleDimensionPresetClick(preset)"
              >
                {{ preset.name }}
              </view>
            </view>
          </view>

          <view class="dimension-note">
            <uni-icons type="info" size="16" color="#666" />
            <text class="note-text">设置最大尺寸可以进一步减小文件大小，超出尺寸的图片将被等比缩放</text>
          </view>
        </view>
      </view>



      <!-- 尺寸调整设置 -->
      <view v-if="selectedTypes.includes('resize')" class="option-group">
        <view class="option-label">尺寸调整</view>
        
        <view class="setting-item">
          <text class="setting-label">预设尺寸</text>
          <picker
            :range="presetSizes"
            range-key="name"
            :value="selectedSizeIndex"
            @change="handleSizeChange"
          >
            <view class="picker-display">
              {{ presetSizes[selectedSizeIndex].name }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>
        
        <view v-if="selectedSizeIndex > 0" class="setting-item">
          <text class="setting-label">缩放模式</text>
          <picker
            :range="resizeModes"
            range-key="label"
            :value="selectedResizeModeIndex"
            @change="handleResizeModeChange"
          >
            <view class="picker-display">
              {{ resizeModes[selectedResizeModeIndex].label }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>
        
        <view v-if="selectedSizeIndex === 0" class="custom-size">
          <view class="size-inputs">
            <view class="size-input-group">
              <text class="size-label">宽度</text>
              <input
                v-model.number="customSize.width"
                type="number"
                class="size-input"
                placeholder="宽度"
              />
            </view>
            <text class="size-separator">×</text>
            <view class="size-input-group">
              <text class="size-label">高度</text>
              <input
                v-model.number="customSize.height"
                type="number"
                class="size-input"
                placeholder="高度"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 水印设置 -->
      <view v-if="selectedTypes.includes('watermark')" class="option-group">
        <view class="option-label">水印设置</view>
        
        <view class="setting-item">
          <text class="setting-label">水印类型</text>
          <picker
            :range="watermarkTypes"
            range-key="label"
            :value="selectedWatermarkTypeIndex"
            @change="handleWatermarkTypeChange"
          >
            <view class="picker-display">
              {{ watermarkTypes[selectedWatermarkTypeIndex].label }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>
        
        <!-- 文字水印设置 -->
        <view v-if="watermarkOptions.type === 'text'" class="watermark-settings">
          <view class="setting-item">
            <text class="setting-label">水印文字</text>
            <input
              v-model="watermarkOptions.text"
              class="text-input"
              placeholder="请输入水印文字"
            />
          </view>
          
          <view class="setting-item">
            <text class="setting-label">字体大小</text>
            <input
              v-model.number="watermarkOptions.fontSize"
              type="number"
              class="number-input"
              placeholder="24"
            />
            <text class="unit">px</text>
          </view>
          
          <view class="setting-item">
            <text class="setting-label">位置</text>
            <picker
              :range="watermarkPositions"
              range-key="label"
              :value="selectedWatermarkPositionIndex"
              @change="handleWatermarkPositionChange"
            >
              <view class="picker-display">
                {{ watermarkPositions[selectedWatermarkPositionIndex].label }}
                <uni-icons type="arrowdown" size="14" color="#666" />
              </view>
            </picker>
          </view>
        </view>
      </view>


    </view>

    <!-- 处理按钮 -->
    <view class="action-section">
      <button
        class="action-btn primary-btn"
        :disabled="!canProcess || isProcessing"
        @click="handleProcess"
      >
        <uni-icons 
          v-if="!isProcessing" 
          type="gear" 
          size="16" 
          color="white" 
        />
        <uni-icons 
          v-else 
          type="spinner-cycle" 
          size="16" 
          color="white" 
        />
        <text>{{ isProcessing ? '处理中...' : '开始处理' }}</text>
      </button>

      <button
        class="action-btn secondary-btn"
        :disabled="isProcessing"
        @click="handleReset"
      >
        <uni-icons type="refresh" size="16" color="#666" />
        <text>重置</text>
      </button>
    </view>
  </ToolContainer>

  <!-- 处理统计信息 -->
  <view v-if="results.length > 0" class="statistics-section">
    <view class="statistics-header">
      <text class="statistics-title">处理统计</text>
      <view class="statistics-summary">
        <text class="summary-text">共处理 {{ statistics.total }} 张图片</text>
        <text class="summary-success">成功 {{ statistics.successful }}</text>
        <text v-if="statistics.failed > 0" class="summary-failed">失败 {{ statistics.failed }}</text>
      </view>
    </view>

    <view class="statistics-details">
      <view class="stat-card">
        <view class="stat-icon">
          <uni-icons type="image" size="24" color="#007AFF" />
        </view>
        <view class="stat-content">
          <text class="stat-label">原始大小</text>
          <text class="stat-value">{{ statistics.originalSize }}</text>
        </view>
      </view>

      <view class="stat-card">
        <view class="stat-icon">
          <uni-icons type="download" size="24" color="#28a745" />
        </view>
        <view class="stat-content">
          <text class="stat-label">处理后大小</text>
          <text class="stat-value">{{ statistics.processedSize }}</text>
        </view>
      </view>

      <view class="stat-card">
        <view class="stat-icon">
          <uni-icons type="checkmarkempty" size="24" color="#17a2b8" />
        </view>
        <view class="stat-content">
          <text class="stat-label">压缩比例</text>
          <text class="stat-value compression-ratio">{{ statistics.compressionRatio }}</text>
        </view>
      </view>

      <view class="stat-card">
        <view class="stat-icon">
          <uni-icons type="star" size="24" color="#ffc107" />
        </view>
        <view class="stat-content">
          <text class="stat-label">成功率</text>
          <text class="stat-value success-rate">{{ statistics.successRate }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import ToolContainer from '@/components/tools/ToolContainer.vue'
import FileUploader from '@/components/tools/FileUploader.vue'
import ImagePreviewCompare from '@/components/tools/ImagePreviewCompare.vue'
import { ImageProcessor } from '@/tools/image/processor.js'
import { ProgressTracker } from '@/tools/base/ProgressTracker.js'

// 响应式数据
const fileUploaderRef = ref(null)
const selectedFiles = ref([])

const selectedTypes = ref(['compress'])
const selectedQualityIndex = ref(1) // 标准质量
const selectedSizeIndex = ref(0) // 原始尺寸
const selectedResizeModeIndex = ref(1) // contain
const selectedWatermarkTypeIndex = ref(0) // 文字
const selectedWatermarkPositionIndex = ref(3) // 右下角
const selectedCompressionModeIndex = ref(0) // 大小优先
const selectedSizeUnitIndex = ref(1) // KB

const compressOptions = ref({
  mode: 'size', // 'quality' | 'size'
  quality: 80, // 0-100
  maxWidth: 1920,
  maxHeight: 1080,
  targetSize: 500, // 目标文件大小数值
  targetSizeBytes: 500 * 1024 // 目标文件大小字节数
})

const customSize = ref({
  width: null,
  height: null
})

const watermarkOptions = ref({
  type: 'text',
  text: '© WeAutoTools',
  fontSize: 24,
  position: 'bottom-right',
  color: 'rgba(255, 255, 255, 0.8)'
})



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

const isProcessing = ref(false)
const progress = ref(0)
const progressStatus = ref('normal')
const progressMessage = ref('')
const results = ref([])
const statistics = ref({})
const errorMessage = ref('')

// 工具实例
const imageProcessor = new ImageProcessor()

// 配置选项
const processTypes = [
  { value: 'compress', name: '压缩', description: '减小文件大小' },
  { value: 'resize', name: '尺寸调整', description: '调整图片尺寸' },
  { value: 'watermark', name: '添加水印', description: '添加文字或图片水印' }
]

const qualityPresets = imageProcessor.getQualityPresets()
const presetSizes = imageProcessor.getPresetSizes()
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

// 常用尺寸预设
const dimensionPresets = [
  { name: '4K', width: 3840, height: 2160 },
  { name: '2K', width: 2560, height: 1440 },
  { name: '1080P', width: 1920, height: 1080 },
  { name: '720P', width: 1280, height: 720 },
  { name: '无限制', width: null, height: null }
]

const resizeModes = [
  { value: 'exact', label: '精确尺寸', description: '强制调整到指定尺寸' },
  { value: 'contain', label: '等比缩放(包含)', description: '保持比例，完整显示' },
  { value: 'cover', label: '等比缩放(覆盖)', description: '保持比例，填满尺寸' }
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
  return selectedFiles.value.length > 0 && selectedTypes.value.length > 0
})



// 估算压缩后的总大小
const estimatedCompressedSize = computed(() => {
  if (!selectedFiles.value.length || !selectedTypes.value.includes('compress')) {
    return '0 B'
  }

  const totalOriginalSize = selectedFiles.value.reduce((sum, file) => sum + file.size, 0)
  let estimatedSize

  if (compressOptions.value.mode === 'quality') {
    // 基于质量估算
    const compressionRatio = compressOptions.value.quality / 100
    estimatedSize = totalOriginalSize * compressionRatio
  } else {
    // 基于目标大小
    estimatedSize = compressOptions.value.targetSizeBytes * selectedFiles.value.length
  }

  return imageProcessor.formatFileSize(estimatedSize)
})

// 估算压缩比例
const estimatedCompressionRatio = computed(() => {
  if (!selectedFiles.value.length || !selectedTypes.value.includes('compress')) {
    return 0
  }

  const totalOriginalSize = selectedFiles.value.reduce((sum, file) => sum + file.size, 0)
  let estimatedCompressedSize

  if (compressOptions.value.mode === 'quality') {
    const compressionRatio = compressOptions.value.quality / 100
    estimatedCompressedSize = totalOriginalSize * compressionRatio
  } else {
    estimatedCompressedSize = compressOptions.value.targetSizeBytes * selectedFiles.value.length
  }

  return imageProcessor.calculateCompressionRatio(totalOriginalSize, estimatedCompressedSize)
})

// 监听变化
watch(selectedQualityIndex, (newIndex) => {
  if (compressOptions.value.mode === 'quality') {
    compressOptions.value.quality = Math.round(qualityPresets[newIndex].value * 100)
  }
})

// 监听目标大小变化
watch(() => compressOptions.value.targetSize, () => {
  updateTargetSizeBytes()
})

watch(selectedSizeUnitIndex, () => {
  updateTargetSizeBytes()
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
const debouncedPreviewUpdate = debounce(() => {
  if (selectedFiles.value.length > 0) {
    generateProcessedPreview(selectedFiles.value[0])
  }
}, 300)

// 监听压缩设置变化，实时更新预览
watch(() => compressOptions.value.quality, () => {
  if (selectedFiles.value.length > 0 && compressOptions.value.mode === 'quality') {
    debouncedPreviewUpdate()
  }
})

watch(() => compressOptions.value.targetSizeBytes, () => {
  if (selectedFiles.value.length > 0 && compressOptions.value.mode === 'size') {
    debouncedPreviewUpdate()
  }
})

watch(() => compressOptions.value.mode, () => {
  if (selectedFiles.value.length > 0) {
    generateProcessedPreview(selectedFiles.value[0])
  }
})

watch(selectedWatermarkTypeIndex, (newIndex) => {
  watermarkOptions.value.type = watermarkTypes[newIndex].value
})

watch(selectedWatermarkPositionIndex, (newIndex) => {
  watermarkOptions.value.position = watermarkPositions[newIndex].value
})

// 方法
const validateImageFormat = (file) => {
  return imageProcessor.validateFormat(file)
}

const handleFileChange = (files) => {
  selectedFiles.value = files.filter(f => f.valid).map(f => f.file)

  // 生成第一个文件的预览
  if (selectedFiles.value.length > 0) {
    generatePreview(selectedFiles.value[0])
  } else {
    clearPreview()
  }
}

const handleFileError = (error) => {
  errorMessage.value = error
}

const handleTypeToggle = (value) => {
  const index = selectedTypes.value.indexOf(value)

  if (index > -1) {
    // 移除选中的类型
    selectedTypes.value = selectedTypes.value.filter(type => type !== value)
  } else {
    // 添加新的类型
    selectedTypes.value = [...selectedTypes.value, value]
  }
}

// 压缩模式切换
const handleCompressionModeChange = (e) => {
  selectedCompressionModeIndex.value = e.detail.value
  compressOptions.value.mode = compressionModes[e.detail.value].value
}

// 质量预设点击
const handleQualityPresetClick = (index) => {
  selectedQualityIndex.value = index
  compressOptions.value.quality = Math.round(qualityPresets[index].value * 100)
}

// 质量滑块变化
const handleQualitySliderChange = (e) => {
  compressOptions.value.quality = e.detail.value
  // 更新对应的预设索引
  updateQualityPresetIndex(e.detail.value)
}

const handleQualitySliderChanging = (e) => {
  compressOptions.value.quality = e.detail.value
}

// 更新质量预设索引
const updateQualityPresetIndex = (quality) => {
  const qualityValue = quality / 100
  let closestIndex = 0
  let minDiff = Math.abs(qualityPresets[0].value - qualityValue)

  qualityPresets.forEach((preset, index) => {
    const diff = Math.abs(preset.value - qualityValue)
    if (diff < minDiff) {
      minDiff = diff
      closestIndex = index
    }
  })

  selectedQualityIndex.value = closestIndex
}

// 目标大小变化
const handleTargetSizeChange = () => {
  updateTargetSizeBytes()
}

// 大小单位变化
const handleSizeUnitChange = (e) => {
  selectedSizeUnitIndex.value = e.detail.value
  updateTargetSizeBytes()
}

// 更新目标大小字节数
const updateTargetSizeBytes = () => {
  const size = compressOptions.value.targetSize || 0
  const unit = sizeUnits[selectedSizeUnitIndex.value]
  compressOptions.value.targetSizeBytes = size * unit.value
}

// 大小预设点击
const handleSizePresetClick = (preset) => {
  compressOptions.value.targetSize = preset.value
  const unitIndex = sizeUnits.findIndex(unit => unit.label === preset.unit)
  if (unitIndex !== -1) {
    selectedSizeUnitIndex.value = unitIndex
  }
  updateTargetSizeBytes()
}

// 尺寸预设点击
const handleDimensionPresetClick = (preset) => {
  compressOptions.value.maxWidth = preset.width
  compressOptions.value.maxHeight = preset.height
}



// 格式化总文件大小
const formatTotalSize = (files) => {
  const totalSize = files.reduce((sum, file) => sum + file.size, 0)
  return imageProcessor.formatFileSize(totalSize)
}

// 预览相关方法
const handlePreviewModeChange = (mode) => {
  previewSettings.value.mode = mode
}

const handlePreviewZoom = (isZoomed) => {
  previewSettings.value.isZoomed = isZoomed
}

const handlePreviewInfo = (showInfo) => {
  previewSettings.value.showInfo = showInfo
}

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

    // 根据当前设置生成处理后的预览
    await generateProcessedPreview(file)

  } catch (error) {
    console.error('预览生成失败:', error)
  } finally {
    previewData.value.isProcessing = false
  }
}

// 生成处理后的预览
const generateProcessedPreview = async (file) => {
  if (!selectedTypes.value.includes('compress')) {
    // 如果不包含压缩，直接使用原图
    previewData.value.processedImage = previewData.value.originalImage
    previewData.value.processedInfo = previewData.value.originalInfo
    return
  }

  try {
    let processedFile = file

    // 应用压缩设置
    if (compressOptions.value.mode === 'quality') {
      processedFile = await imageProcessor.compressImage(processedFile, {
        quality: compressOptions.value.quality / 100,
        maxWidth: compressOptions.value.maxWidth,
        maxHeight: compressOptions.value.maxHeight
      })
    } else {
      processedFile = await imageProcessor.compressToTargetSize(
        processedFile,
        compressOptions.value.targetSizeBytes,
        {
          maxWidth: compressOptions.value.maxWidth,
          maxHeight: compressOptions.value.maxHeight
        }
      )
    }

    // 设置处理后的图片
    const processedUrl = URL.createObjectURL(processedFile)
    previewData.value.processedImage = processedUrl
    previewData.value.processedInfo = await imageProcessor.getImageInfo(processedFile)

  } catch (error) {
    console.error('处理预览生成失败:', error)
    previewData.value.processedImage = previewData.value.originalImage
    previewData.value.processedInfo = previewData.value.originalInfo
  }
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



const handleSizeChange = (e) => {
  selectedSizeIndex.value = e.detail.value
}

const handleResizeModeChange = (e) => {
  selectedResizeModeIndex.value = e.detail.value
}

const handleWatermarkTypeChange = (e) => {
  selectedWatermarkTypeIndex.value = e.detail.value
}

const handleWatermarkPositionChange = (e) => {
  selectedWatermarkPositionIndex.value = e.detail.value
}

const handleProcess = async () => {
  if (!canProcess.value || isProcessing.value) return
  
  isProcessing.value = true
  progress.value = 0
  progressStatus.value = 'running'
  errorMessage.value = ''
  results.value = []
  
  try {
    const tracker = new ProgressTracker(selectedFiles.value.length, (status) => {
      progress.value = status.percentage
      progressMessage.value = `正在处理第 ${status.current}/${status.total} 张图片...`
    })
    
    tracker.start()
    
    const processResults = await imageProcessor.processBatch(
      selectedFiles.value,
      async (file) => {
        return await processImage(file)
      },
      (progress) => {
        tracker.setCurrent(progress.completed, `正在处理: ${progress.currentFile}`)
      }
    )
    
    results.value = await Promise.all(processResults.map(async (result) => {
      // 安全地获取文件名 - result.file 现在是真正的 File 对象
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
        extension = 'jpg' // 默认扩展名
      }

      const processedName = `${originalName}_done.${extension}`

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
    
  } catch (error) {
    progressStatus.value = 'error'
    progressMessage.value = '处理失败'
    errorMessage.value = error.message
  } finally {
    isProcessing.value = false
  }
}

const processImage = async (file) => {
  let processedFile = file
  
  // 按顺序执行各种处理
  for (const type of selectedTypes.value) {
    switch (type) {
      case 'compress':
        if (compressOptions.value.mode === 'quality') {
          // 质量优先模式
          processedFile = await imageProcessor.compressImage(processedFile, {
            quality: compressOptions.value.quality / 100,
            maxWidth: compressOptions.value.maxWidth,
            maxHeight: compressOptions.value.maxHeight
          })
        } else {
          // 大小优先模式
          processedFile = await imageProcessor.compressToTargetSize(
            processedFile,
            compressOptions.value.targetSizeBytes,
            {
              maxWidth: compressOptions.value.maxWidth,
              maxHeight: compressOptions.value.maxHeight
            }
          )
        }
        break

      case 'resize':
        if (selectedSizeIndex.value > 0) {
          const size = presetSizes[selectedSizeIndex.value]
          const mode = resizeModes[selectedResizeModeIndex.value].value
          processedFile = await imageProcessor.resizeImage(processedFile, {
            width: size.width,
            height: size.height,
            mode
          })
        } else if (customSize.value.width && customSize.value.height) {
          const mode = resizeModes[selectedResizeModeIndex.value].value
          processedFile = await imageProcessor.resizeImage(processedFile, {
            width: customSize.value.width,
            height: customSize.value.height,
            mode
          })
        }
        break

      case 'watermark':
        processedFile = await imageProcessor.addWatermark(processedFile, watermarkOptions.value)
        break
    }
  }
  
  return processedFile
}

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

const handleDownload = (result) => {
  if (result.result) {
    const filename = result.file.processedName || result.file.name
    imageProcessor.downloadFile(result.result, filename)
  }
}

const handleDownloadAll = async () => {
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

const handleClear = () => {
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

  // 清理预览数据
  clearPreview()

  if (fileUploaderRef.value) {
    fileUploaderRef.value.clearFiles()
  }

  selectedFiles.value = []
}

const handleReset = () => {
  selectedTypes.value = ['compress']
  selectedQualityIndex.value = 1
  selectedSizeIndex.value = 0
  selectedResizeModeIndex.value = 1
  selectedWatermarkTypeIndex.value = 0
  selectedWatermarkPositionIndex.value = 3

  compressOptions.value = {
    mode: 'size',
    quality: 80,
    maxWidth: 1920,
    maxHeight: 1080,
    targetSize: 500,
    targetSizeBytes: 500 * 1024
  }

  customSize.value = {
    width: null,
    height: null
  }

  watermarkOptions.value = {
    type: 'text',
    text: '© WeAutoTools',
    fontSize: 24,
    position: 'bottom-right',
    color: 'rgba(255, 255, 255, 0.8)'
  }

  handleClear()
}

const handlePreview = (result) => {
  if (result.result) {
    const url = URL.createObjectURL(result.result)
    uni.previewImage({
      urls: [url],
      current: 0
    })
  }
}

const formatFileSize = (size) => {
  if (!size) return '0B'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)}MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
}
</script>

<style lang="scss" scoped>
.process-options {
  margin: 32rpx 0;

  .section-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    padding: 0 8rpx;
  }
}

.option-group {
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid #f0f0f0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

  &:last-child {
    margin-bottom: 0;
  }

  .option-label {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    padding-bottom: 12rpx;
    border-bottom: 2rpx solid #f0f0f0;
  }
}

.process-types {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280rpx, 1fr));
  gap: 16rpx;

  .type-option {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: white;
    border: 2rpx solid #e9ecef;
    border-radius: 16rpx;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

    &.active {
      border-color: #007aff;
      background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
      box-shadow: 0 4rpx 16rpx rgba(0, 122, 255, 0.15);
    }

    &:hover {
      border-color: #007aff;
      transform: translateY(-2rpx);
      box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.08);
    }

    checkbox {
      margin-right: 16rpx;
    }

    .type-name {
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
      margin-right: 12rpx;
    }

    .type-desc {
      font-size: 22rpx;
      color: #666;
      flex: 1;
    }
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 16rpx 0;

  &:last-child {
    margin-bottom: 0;
  }

  .setting-label {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    min-width: 140rpx;
  }

  .picker-display {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 20rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #333;
    min-width: 200rpx;
    transition: all 0.3s ease;

    &:hover {
      border-color: #007aff;
      background: #fff;
    }
  }

  .number-input,
  .text-input {
    padding: 16rpx 20rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #333;
    min-width: 150rpx;
    transition: all 0.3s ease;

    &:focus {
      border-color: #007aff;
      background: #fff;
      box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
    }
  }

  .unit {
    font-size: 24rpx;
    color: #666;
    margin-left: 12rpx;
    font-weight: 500;
  }
}

.custom-size {
  margin-top: 20rpx;
  
  .size-inputs {
    display: flex;
    align-items: center;
    gap: 20rpx;
    
    .size-input-group {
      display: flex;
      flex-direction: column;
      gap: 10rpx;
      
      .size-label {
        font-size: 24rpx;
        color: #666;
      }
      
      .size-input {
        padding: 15rpx;
        background: white;
        border: 2rpx solid #ddd;
        border-radius: 8rpx;
        font-size: 26rpx;
        width: 120rpx;
        text-align: center;
      }
    }
    
    .size-separator {
      font-size: 28rpx;
      color: #666;
      margin-top: 30rpx;
    }
  }
}

.watermark-settings {
  margin-top: 20rpx;
  padding: 20rpx;
  background: white;
  border-radius: 12rpx;
  border: 2rpx solid #eee;
}



.action-section {
  display: flex;
  gap: 20rpx;
  margin: 30rpx 0;
  
  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    padding: 25rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.primary-btn {
      background: #007aff;
      color: white;
      
      &:hover {
        background: #0056b3;
      }
      
      &:disabled {
        background: #ccc;
        cursor: not-allowed;
      }
    }
    
    &.secondary-btn {
      background: #f8f9fa;
      color: #666;
      border: 2rpx solid #dee2e6;
      
      &:hover {
        background: #e9ecef;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .process-options {
    padding: 20rpx;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15rpx;
    
    .picker-display,
    .number-input,
    .text-input {
      width: 100%;
      min-width: auto;
    }
  }
  
  .size-inputs {
    flex-direction: column;
    
    .size-separator {
      margin: 0;
      transform: rotate(90deg);
    }
  }
}

/* 压缩设置样式 */
.compress-quality-section,
.compress-size-section {
  margin-top: 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 12rpx;
  border: 2rpx solid #f0f0f0;
}

.compression-mode-selector {
  margin-bottom: 32rpx;

  .mode-title {
    display: block;
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }

  .mode-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
  }

  .mode-option {
    position: relative;
    padding: 28rpx 24rpx;
    background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
    border: 3rpx solid #e9ecef;
    border-radius: 16rpx;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4rpx;
      background: transparent;
      transition: all 0.3s ease;
    }

    &.active {
      background: linear-gradient(135deg, #007AFF 0%, #0056b3 100%);
      border-color: #007AFF;
      color: white;
      transform: translateY(-2rpx);
      box-shadow: 0 8rpx 24rpx rgba(0, 122, 255, 0.25);

      &::before {
        background: linear-gradient(90deg, #00d4ff 0%, #007AFF 100%);
      }

      .mode-icon {
        opacity: 1;
        transform: scale(1.1);
      }
    }

    &:hover:not(.active) {
      border-color: #007AFF;
      background: linear-gradient(135deg, #fff 0%, #f0f8ff 100%);
      transform: translateY(-1rpx);
      box-shadow: 0 6rpx 16rpx rgba(0, 122, 255, 0.1);

      .mode-icon {
        transform: scale(1.05);
      }
    }

    .mode-icon {
      margin-bottom: 12rpx;
      opacity: 0.8;
      transition: all 0.3s ease;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 48rpx;
      height: 48rpx;
      margin: 0 auto 12rpx;
      background: rgba(0, 122, 255, 0.1);
      border-radius: 12rpx;
    }

    &.active .mode-icon {
      background: rgba(255, 255, 255, 0.2);
    }

    .mode-name {
      display: block;
      font-size: 28rpx;
      font-weight: 700;
      margin-bottom: 8rpx;
      letter-spacing: 0.5rpx;
    }

    .mode-desc {
      display: block;
      font-size: 22rpx;
      opacity: 0.8;
      line-height: 1.4;
      font-weight: 400;
    }
  }
}

/* 尺寸限制设置样式 */
.dimension-limit-container {
  .dimension-inputs {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;
    margin-bottom: 24rpx;

    .dimension-input {
      display: flex;
      align-items: center;
      gap: 12rpx;
      padding: 20rpx;
      background: #f8f9fa;
      border-radius: 12rpx;
      border: 2rpx solid #e9ecef;
      transition: all 0.3s ease;

      &:focus-within {
        border-color: #007AFF;
        background: #fff;
        box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
      }

      .dimension-label {
        font-size: 24rpx;
        color: #666;
        font-weight: 500;
        white-space: nowrap;
      }

      .number-input {
        flex: 1;
        padding: 12rpx 16rpx;
        background: #fff;
        border: 2rpx solid #dee2e6;
        border-radius: 8rpx;
        font-size: 26rpx;
        color: #333;
        text-align: center;

        &:focus {
          border-color: #007AFF;
          box-shadow: 0 0 0 2rpx rgba(0, 122, 255, 0.1);
        }
      }

      .unit {
        font-size: 22rpx;
        color: #666;
        font-weight: 500;
      }
    }
  }

  .dimension-presets {
    margin-bottom: 24rpx;

    .preset-label {
      display: block;
      font-size: 26rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 16rpx;
    }

    .preset-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;

      .dimension-preset {
        padding: 12rpx 20rpx;
        background: #f8f9fa;
        border: 2rpx solid #e9ecef;
        border-radius: 20rpx;
        font-size: 24rpx;
        color: #666;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: #007AFF;
          background: #f0f8ff;
          color: #007AFF;
        }

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }

  .dimension-note {
    display: flex;
    align-items: flex-start;
    gap: 8rpx;
    padding: 16rpx;
    background: rgba(0, 122, 255, 0.05);
    border-radius: 12rpx;
    border: 1rpx solid rgba(0, 122, 255, 0.1);

    .note-text {
      font-size: 22rpx;
      color: #666;
      line-height: 1.4;
    }
  }
}

.quality-presets-container {
  margin-bottom: 32rpx;

  .preset-label {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 16rpx;
  }

  .quality-presets {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12rpx;
  }

  .preset-btn {
    padding: 16rpx 12rpx;
    background: #f8f9fa;
    border-radius: 12rpx;
    border: 2rpx solid #e9ecef;
    text-align: center;
    transition: all 0.3s ease;
    cursor: pointer;

    &.active {
      background: #007AFF;
      border-color: #007AFF;
      color: white;
    }

    .preset-name {
      display: block;
      font-size: 26rpx;
      font-weight: 500;
      margin-bottom: 4rpx;
    }

    .preset-desc {
      display: block;
      font-size: 20rpx;
      opacity: 0.8;
    }
  }
}

.quality-slider-container {
  .slider-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    .slider-label {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
    }

    .slider-value {
      font-size: 28rpx;
      color: #007AFF;
      font-weight: 600;
      background: rgba(0, 122, 255, 0.1);
      padding: 6rpx 16rpx;
      border-radius: 16rpx;
    }
  }

  .slider-wrapper {
    padding: 16rpx 0;
  }
}

.size-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 16rpx;

  .preset-btn {
    padding: 12rpx 24rpx;
    background: #f5f5f5;
    border-radius: 20rpx;
    font-size: 24rpx;
    color: #666;
    border: 2rpx solid transparent;
    transition: all 0.3s ease;
    min-width: 120rpx;
    text-align: center;

    &.active {
      background: #007AFF;
      color: white;
      border-color: #007AFF;
    }
  }
}

.slider-container {
  margin: 16rpx 0;

  .slider-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 8rpx;

    .slider-label {
      font-size: 22rpx;
      color: #999;
    }
  }
}

.quality-description {
  font-size: 22rpx;
  color: #666;
  margin-top: 8rpx;
  text-align: center;
}

.target-size-input {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;

  .number-input {
    flex: 1;
  }

  .unit-picker {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 16rpx;
    background: #f5f5f5;
    border-radius: 8rpx;
    font-size: 24rpx;
    color: #333;
    min-width: 80rpx;
    justify-content: center;
  }
}

.dimension-inputs {
  display: flex;
  gap: 20rpx;
  flex: 1;

  .dimension-input {
    display: flex;
    align-items: center;
    gap: 8rpx;
    flex: 1;

    .dimension-label {
      font-size: 22rpx;
      color: #666;
      white-space: nowrap;
    }

    .number-input.small {
      min-width: 100rpx;
    }
  }
}

.compress-preview {
  margin-top: 32rpx;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border: 2rpx solid #e9ecef;

  .preview-title {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    margin-bottom: 20rpx;
  }

  .preview-stats {
    display: flex;
    justify-content: space-between;
    gap: 20rpx;

    .stat-item {
      text-align: center;
      flex: 1;

      .stat-label {
        display: block;
        font-size: 22rpx;
        color: #666;
        margin-bottom: 8rpx;
      }

      .stat-value {
        display: block;
        font-size: 26rpx;
        font-weight: 500;
        color: #333;

        &.compression-ratio {
          color: #28a745;
        }
      }
    }
  }
}

/* 预览区域样式 */
.preview-section {
  margin: 32rpx 0;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border: 2rpx solid #f0f0f0;
}

@media (max-width: 750rpx) {
  .quality-presets,
  .size-presets {
    gap: 12rpx;
  }

  .preset-btn {
    padding: 10rpx 20rpx;
    font-size: 22rpx;
  }

  .dimension-limit-container {
    .dimension-inputs {
      grid-template-columns: 1fr;
      gap: 16rpx;

      .dimension-input {
        padding: 16rpx;

        .dimension-label {
          font-size: 22rpx;
        }

        .number-input {
          padding: 10rpx 12rpx;
          font-size: 24rpx;
        }
      }
    }

    .dimension-presets {
      .preset-buttons {
        gap: 10rpx;

        .dimension-preset {
          padding: 10rpx 16rpx;
          font-size: 22rpx;
        }
      }
    }

    .dimension-note {
      padding: 12rpx;

      .note-text {
        font-size: 20rpx;
      }
    }
  }

  .target-size-input {
    flex-direction: column;
    align-items: stretch;
    gap: 12rpx;
  }

  .preview-stats {
    flex-direction: column;
    gap: 16rpx;
  }

  .preview-section {
    margin: 24rpx 0;
    border-radius: 12rpx;
  }
}

/* 统计信息样式 */
.statistics-section {
  margin: 32rpx 0;
  padding: 32rpx;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 20rpx;
  border: 2rpx solid #dee2e6;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);

  .statistics-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding-bottom: 16rpx;
    border-bottom: 2rpx solid #dee2e6;

    .statistics-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .statistics-summary {
      display: flex;
      gap: 16rpx;
      align-items: center;

      .summary-text {
        font-size: 24rpx;
        color: #666;
      }

      .summary-success {
        font-size: 22rpx;
        color: #28a745;
        background: rgba(40, 167, 69, 0.1);
        padding: 4rpx 12rpx;
        border-radius: 12rpx;
        font-weight: 500;
      }

      .summary-failed {
        font-size: 22rpx;
        color: #dc3545;
        background: rgba(220, 53, 69, 0.1);
        padding: 4rpx 12rpx;
        border-radius: 12rpx;
        font-weight: 500;
      }
    }
  }

  .statistics-details {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
    gap: 20rpx;
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 20rpx;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 16rpx;
    border: 2rpx solid rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10rpx);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.95);
      transform: translateY(-2rpx);
      box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48rpx;
      height: 48rpx;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 12rpx;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    }

    .stat-content {
      flex: 1;

      .stat-label {
        display: block;
        font-size: 22rpx;
        color: #666;
        margin-bottom: 4rpx;
      }

      .stat-value {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;

        &.compression-ratio {
          color: #17a2b8;
        }

        &.success-rate {
          color: #ffc107;
        }
      }
    }
  }
}

@media (max-width: 750rpx) {
  .statistics-section {
    margin: 24rpx 0;
    padding: 24rpx;

    .statistics-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12rpx;

      .statistics-summary {
        flex-wrap: wrap;
        gap: 12rpx;
      }
    }

    .statistics-details {
      grid-template-columns: repeat(2, 1fr);
      gap: 16rpx;
    }

    .stat-card {
      padding: 16rpx;
      gap: 12rpx;

      .stat-icon {
        width: 40rpx;
        height: 40rpx;
      }

      .stat-content {
        .stat-value {
          font-size: 24rpx;
        }
      }
    }
  }
}
</style>
