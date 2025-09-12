<template>
  <ToolContainer
    title="二维码生成器"
    description="输入文本内容，快速生成二维码"
    :showProgress="isGenerating"
    :progress="progress"
    :progressStatus="progressStatus"
    :progressMessage="progressMessage"
    :showResult="results.length > 0"
    :results="results"
    :statistics="statistics"
    :error="errorMessage"
    @download="handleDownload"
    @download-all="handleDownloadAll"
    @clear="handleClear"
    @preview="handlePreview"
  >
    <!-- 使用限制提示 -->
    <view v-if="showUsageHint" :class="['usage-hint', usageHintClass]">
      <uni-icons type="info" size="16" :color="usageHintClass === 'usage-hint-danger' ? '#ff4757' : '#3742fa'" />
      <text class="hint-text">{{ usageHintText }}</text>
      <text v-if="showLoginHint" class="login-hint" @click="handleLoginHint">登录获取更多</text>
    </view>

    <!-- 输入区域 -->
    <view class="input-section">
      <!-- 文本输入 -->
      <view class="input-group">
        <view class="input-label">
          <text>文本内容</text>
          <text class="required">*</text>
        </view>
        <textarea
          v-model="inputText"
          class="text-input"
          placeholder="请输入要生成二维码的文本内容..."
          :maxlength="2000"
          auto-height
        />
        <view class="input-hint">
          <text class="char-count">{{ inputText.length }}/2000</text>
        </view>
      </view>

      <!-- 批量输入 -->
      <view class="input-group">
        <view class="input-label">
          <text>批量生成</text>
          <text class="optional">（可选）</text>
        </view>
        <textarea
          v-model="batchText"
          class="text-input batch-input"
          placeholder="每行一个文本，支持批量生成多个二维码..."
          auto-height
        />
        <view class="input-hint">
          <text>{{ batchLines.length }} 行文本</text>
        </view>
      </view>
    </view>

    <!-- 设置区域 -->
    <view class="settings-section">
      <view class="section-title">生成设置</view>
      
      <!-- 尺寸设置 -->
      <view class="setting-group">
        <view class="setting-label">尺寸大小</view>
        <picker
          :range="sizeOptions"
          range-key="label"
          :value="selectedSizeIndex"
          @change="handleSizeChange"
        >
          <view class="picker-display">
            {{ sizeOptions[selectedSizeIndex].label }}
            <uni-icons type="arrowdown" size="14" color="#666" />
          </view>
        </picker>
      </view>

      <!-- 纠错级别 -->
      <view class="setting-group">
        <view class="setting-label">纠错级别</view>
        <picker
          :range="errorLevels"
          range-key="label"
          :value="selectedErrorIndex"
          @change="handleErrorLevelChange"
        >
          <view class="picker-display">
            {{ errorLevels[selectedErrorIndex].label }}
            <uni-icons type="arrowdown" size="14" color="#666" />
          </view>
        </picker>
      </view>

      <!-- 颜色设置 -->
      <view class="setting-group">
        <view class="setting-label">前景色</view>
        <view class="color-picker" @click="showColorPicker = true">
          <view class="color-preview" :style="{ backgroundColor: foregroundColor }"></view>
          <text class="color-text">{{ foregroundColor }}</text>
        </view>
      </view>

      <view class="setting-group">
        <view class="setting-label">背景色</view>
        <view class="color-picker" @click="showBgColorPicker = true">
          <view class="color-preview" :style="{ backgroundColor: backgroundColor }"></view>
          <text class="color-text">{{ backgroundColor }}</text>
        </view>
      </view>

      <!-- 边距设置 -->
      <view class="setting-group">
        <view class="setting-label">边距</view>
        <slider
          :value="margin"
          :min="0"
          :max="10"
          :step="1"
          @change="handleMarginChange"
          activeColor="#007aff"
        />
        <text class="slider-value">{{ margin }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <button
        class="action-btn primary-btn"
        :disabled="!canGenerate || isGenerating"
        @click="handleGenerate"
      >
        <uni-icons v-if="!isGenerating" type="scan" size="16" color="white" />
        <uni-icons v-else type="spinner-cycle" size="16" color="white" />
        <text>{{ isGenerating ? '生成中...' : '生成二维码' }}</text>
      </button>

      <button
        class="action-btn secondary-btn"
        :disabled="isGenerating"
        @click="handleReset"
      >
        <uni-icons type="refresh" size="16" color="#666" />
        <text>重置</text>
      </button>
    </view>

    <!-- 预览区域 -->
    <view v-if="previewResult" class="preview-section">
      <view class="preview-title">预览</view>
      <view class="preview-container">
        <image
          :src="previewResult.dataURL"
          class="preview-image"
          mode="aspectFit"
          @click="handlePreviewClick"
        />
        <view class="preview-info">
          <text class="preview-text">{{ previewResult.text }}</text>
          <text class="preview-size">{{ formatFileSize(previewResult.size) }}</text>
        </view>
      </view>
    </view>
  </ToolContainer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import ToolContainer from '@/components/tools/ToolContainer.vue'
import { QRGenerator } from '@/tools/qrcode/generator.js'
import { ProgressTracker } from '@/tools/base/ProgressTracker.js'
import { useUsageLimit } from '@/composables/useUsageLimit'

// 响应式数据
const inputText = ref('')
const batchText = ref('')
const selectedSizeIndex = ref(1) // 默认256
const selectedErrorIndex = ref(1) // 默认M级别
const foregroundColor = ref('#000000')
const backgroundColor = ref('#FFFFFF')
const margin = ref(2)

const isGenerating = ref(false)
const progress = ref(0)
const progressStatus = ref('normal')
const progressMessage = ref('')
const results = ref([])
const statistics = ref({})
const errorMessage = ref('')
const previewResult = ref(null)

// 使用限制相关
const {
  remainingUsage,
  isLimited,
  userType,
  loading,
  showUsageHint,
  usageHintText,
  usageHintClass,
  showLoginHint,
  useFrontendTool
} = useUsageLimit()

// 工具实例
const qrGenerator = new QRGenerator()

// 配置选项
const sizeOptions = qrGenerator.getPresetSizes()
const errorLevels = qrGenerator.getErrorCorrectionLevels()

// 计算属性
const batchLines = computed(() => {
  return batchText.value
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
})

const canGenerate = computed(() => {
  return inputText.value.trim().length > 0 || batchLines.value.length > 0
})

const isBatchMode = computed(() => {
  return batchLines.value.length > 1
})

// 监听输入变化，实时预览
watch([inputText, selectedSizeIndex, selectedErrorIndex, foregroundColor, backgroundColor, margin], 
  async () => {
    if (inputText.value.trim() && !isGenerating.value) {
      await generatePreview()
    }
  }, 
  { debounce: 500 }
)

// 方法
const handleSizeChange = (e) => {
  selectedSizeIndex.value = e.detail.value
}

const handleErrorLevelChange = (e) => {
  selectedErrorIndex.value = e.detail.value
}

const handleMarginChange = (e) => {
  margin.value = e.detail.value
}

const generatePreview = async () => {
  if (!inputText.value.trim()) {
    previewResult.value = null
    return
  }

  try {
    const options = getGenerateOptions()
    const result = await qrGenerator.generate(inputText.value.trim(), options)
    previewResult.value = result
  } catch (error) {
    console.error('预览生成失败:', error)
  }
}

const getGenerateOptions = () => {
  return {
    width: sizeOptions[selectedSizeIndex.value].value,
    margin: margin.value,
    color: {
      dark: foregroundColor.value,
      light: backgroundColor.value
    },
    errorCorrectionLevel: errorLevels[selectedErrorIndex.value].value
  }
}

const handleGenerate = async () => {
  if (!canGenerate.value || isGenerating.value) return

  // 1. 预检查使用限制
  const batchSize = isBatchMode.value ? batchLines.value.length : 1
  const usageResult = await useFrontendTool('qr-generate', batchSize)
  if (!usageResult.canUse) {
    return // 已显示限制提示
  }

  isGenerating.value = true
  progress.value = 0
  progressStatus.value = 'running'
  errorMessage.value = ''
  results.value = []

  try {
    const options = getGenerateOptions()
    
    if (isBatchMode.value) {
      // 批量生成
      await generateBatch(batchLines.value, options)
    } else {
      // 单个生成
      await generateSingle(inputText.value.trim(), options)
    }
    
    progressStatus.value = 'completed'
    progressMessage.value = '生成完成'
    
    // 计算统计信息
    updateStatistics()
    
    // 3. 成功后报告使用
    await usageResult.reportUsage(batchSize)
    
  } catch (error) {
    progressStatus.value = 'error'
    progressMessage.value = '生成失败'
    errorMessage.value = error.message
    // 失败时不记录使用次数
    console.error('二维码生成失败:', error)
  } finally {
    isGenerating.value = false
  }
}

const generateSingle = async (text, options) => {
  progressMessage.value = '正在生成二维码...'
  
  const result = await qrGenerator.generate(text, options)
  
  results.value = [{
    success: true,
    file: { name: `qrcode_${Date.now()}.png`, type: 'image/png' },
    result: result.blob,
    originalSize: text.length,
    compressedSize: result.size,
    text: result.text,
    dataURL: result.dataURL
  }]
  
  progress.value = 100
}

const generateBatch = async (textList, options) => {
  const tracker = new ProgressTracker(textList.length, (status) => {
    progress.value = status.percentage
    progressMessage.value = `正在生成第 ${status.current}/${status.total} 个二维码...`
  })
  
  tracker.start()
  
  const batchResults = await qrGenerator.generateBatch(textList, options, (progress) => {
    tracker.setCurrent(progress.completed, `正在处理: ${progress.currentText}`)
  })
  
  results.value = batchResults.map(result => ({
    success: result.success,
    file: { name: `qrcode_${result.index + 1}.png`, type: 'image/png' },
    result: result.success ? result.blob : null,
    originalSize: result.text?.length || 0,
    compressedSize: result.success ? result.size : 0,
    text: result.text,
    dataURL: result.success ? result.dataURL : null,
    error: result.error
  }))
  
  tracker.complete()
}

const updateStatistics = () => {
  const successful = results.value.filter(r => r.success)
  const failed = results.value.filter(r => !r.success)
  
  statistics.value = {
    total: results.value.length,
    successful: successful.length,
    failed: failed.length,
    successRate: `${((successful.length / results.value.length) * 100).toFixed(1)}%`
  }
}

const handleDownload = (result, index) => {
  if (result.result) {
    const filename = `qrcode_${index + 1}_${Date.now()}.png`
    qrGenerator.downloadFile(result.result, filename)
  }
}

const handleDownloadAll = async () => {
  try {
    await qrGenerator.downloadBatch(results.value)
  } catch (error) {
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
}

const handleClear = () => {
  results.value = []
  statistics.value = {}
  errorMessage.value = ''
  progress.value = 0
  progressStatus.value = 'normal'
  progressMessage.value = ''
}

const handleReset = () => {
  inputText.value = ''
  batchText.value = ''
  selectedSizeIndex.value = 1
  selectedErrorIndex.value = 1
  foregroundColor.value = '#000000'
  backgroundColor.value = '#FFFFFF'
  margin.value = 2
  previewResult.value = null
  handleClear()
}

const handlePreview = (result, index) => {
  if (result.dataURL) {
    uni.previewImage({
      urls: [result.dataURL],
      current: 0
    })
  }
}

const handlePreviewClick = () => {
  if (previewResult.value?.dataURL) {
    uni.previewImage({
      urls: [previewResult.value.dataURL],
      current: 0
    })
  }
}

const formatFileSize = (size) => {
  if (!size) return '0B'
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  return `${(size / (1024 * 1024)).toFixed(1)}MB`
}

// 处理登录提示点击
const handleLoginHint = () => {
  uni.navigateTo({
    url: '/pages/auth/login',
    fail: () => {
      uni.showToast({
        title: '请通过登录获取更多使用次数',
        icon: 'none',
        duration: 3000
      })
    }
  })
}

// 页面加载时生成预览
import { onMounted } from 'vue'

onMounted(async () => {
  // 设置默认文本用于演示
  inputText.value = 'https://weautotools.com'
})
</script>

<style lang="scss" scoped>
.input-section {
  margin-bottom: 30rpx;
}

.input-group {
  margin-bottom: 30rpx;
  
  .input-label {
    display: flex;
    align-items: center;
    margin-bottom: 15rpx;
    font-size: 28rpx;
    color: #333;
    
    .required {
      color: #ff4757;
      margin-left: 5rpx;
    }
    
    .optional {
      color: #999;
      font-size: 24rpx;
      margin-left: 10rpx;
    }
  }
  
  .text-input {
    width: 100%;
    min-height: 120rpx;
    padding: 20rpx;
    border: 2rpx solid #ddd;
    border-radius: 12rpx;
    font-size: 28rpx;
    line-height: 1.5;
    
    &.batch-input {
      min-height: 200rpx;
    }
    
    &:focus {
      border-color: #007aff;
    }
  }
  
  .input-hint {
    margin-top: 10rpx;
    text-align: right;
    
    .char-count {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.settings-section {
  margin-bottom: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 30rpx;
  }
}

.setting-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 25rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .setting-label {
    font-size: 28rpx;
    color: #333;
    min-width: 120rpx;
  }
  
  .picker-display {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 15rpx 20rpx;
    background: white;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    min-width: 200rpx;
    justify-content: space-between;
  }
  
  .color-picker {
    display: flex;
    align-items: center;
    gap: 15rpx;
    padding: 10rpx 15rpx;
    background: white;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    cursor: pointer;
    
    .color-preview {
      width: 40rpx;
      height: 40rpx;
      border-radius: 6rpx;
      border: 2rpx solid #ddd;
    }
    
    .color-text {
      font-size: 26rpx;
      color: #333;
    }
  }
  
  .slider-value {
    font-size: 26rpx;
    color: #333;
    min-width: 40rpx;
    text-align: center;
  }
}

.action-section {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
  
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

.preview-section {
  margin-top: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  
  .preview-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    text-align: center;
  }
  
  .preview-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .preview-image {
      width: 200rpx;
      height: 200rpx;
      border: 2rpx solid #ddd;
      border-radius: 12rpx;
      margin-bottom: 20rpx;
      cursor: pointer;
    }
    
    .preview-info {
      text-align: center;
      
      .preview-text {
        display: block;
        font-size: 26rpx;
        color: #333;
        margin-bottom: 10rpx;
        word-break: break-all;
      }
      
      .preview-size {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

// 使用限制提示样式
.usage-hint {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  margin-bottom: 30rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
  
  &.usage-hint-info {
    background: #e6f7ff;
    border: 1rpx solid #91d5ff;
  }
  
  &.usage-hint-warning {
    background: #fff7e6;
    border: 1rpx solid #ffd591;
  }
  
  &.usage-hint-danger {
    background: #fff2f0;
    border: 1rpx solid #ffccc7;
  }
  
  .hint-text {
    flex: 1;
    color: #333;
  }
  
  .login-hint {
    color: #007aff;
    text-decoration: underline;
    cursor: pointer;
    
    &:active {
      opacity: 0.7;
    }
  }
}
</style>
