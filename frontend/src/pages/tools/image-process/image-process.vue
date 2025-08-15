<template>
  <ToolContainer
    title="图片处理工具"
    description="图片压缩、格式转换、批量加水印、批量重命名"
    :showProgress="isProcessing"
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

    <!-- 处理选项 -->
    <view class="process-options">
      <view class="section-title">处理选项</view>
      
      <!-- 处理类型选择 -->
      <view class="option-group">
        <view class="option-label">处理类型</view>
        <view class="process-types">
          <label
            v-for="type in processTypes"
            :key="type.value"
            class="type-option"
            :class="{ active: selectedTypes.includes(type.value) }"
          >
            <checkbox
              :value="type.value"
              :checked="selectedTypes.includes(type.value)"
              @change="handleTypeChange"
            />
            <text class="type-name">{{ type.name }}</text>
            <text class="type-desc">{{ type.description }}</text>
          </label>
        </view>
      </view>

      <!-- 压缩设置 -->
      <view v-if="selectedTypes.includes('compress')" class="option-group">
        <view class="option-label">压缩设置</view>
        
        <view class="setting-item">
          <text class="setting-label">质量</text>
          <picker
            :range="qualityPresets"
            range-key="name"
            :value="selectedQualityIndex"
            @change="handleQualityChange"
          >
            <view class="picker-display">
              {{ qualityPresets[selectedQualityIndex].name }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>
        
        <view class="setting-item">
          <text class="setting-label">最大宽度</text>
          <input
            v-model.number="compressOptions.maxWidth"
            type="number"
            class="number-input"
            placeholder="1920"
          />
          <text class="unit">px</text>
        </view>
        
        <view class="setting-item">
          <text class="setting-label">最大高度</text>
          <input
            v-model.number="compressOptions.maxHeight"
            type="number"
            class="number-input"
            placeholder="1080"
          />
          <text class="unit">px</text>
        </view>
      </view>

      <!-- 格式转换设置 -->
      <view v-if="selectedTypes.includes('convert')" class="option-group">
        <view class="option-label">格式转换</view>
        
        <view class="setting-item">
          <text class="setting-label">目标格式</text>
          <picker
            :range="outputFormats"
            range-key="label"
            :value="selectedFormatIndex"
            @change="handleFormatChange"
          >
            <view class="picker-display">
              {{ outputFormats[selectedFormatIndex].label }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
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

      <!-- 重命名设置 -->
      <view v-if="selectedTypes.includes('rename')" class="option-group">
        <view class="option-label">批量重命名</view>
        
        <view class="setting-item">
          <text class="setting-label">命名模式</text>
          <input
            v-model="renameOptions.pattern"
            class="text-input"
            placeholder="{name}_{index}"
          />
        </view>
        
        <view class="setting-item">
          <text class="setting-label">起始序号</text>
          <input
            v-model.number="renameOptions.startIndex"
            type="number"
            class="number-input"
            placeholder="1"
          />
        </view>
        
        <view class="rename-preview" v-if="selectedFiles.length > 0">
          <text class="preview-title">重命名预览</text>
          <view class="preview-list">
            <view
              v-for="(preview, index) in renamePreview.slice(0, 3)"
              :key="index"
              class="preview-item"
            >
              <text class="original-name">{{ preview.originalName }}</text>
              <uni-icons type="arrowright" size="12" color="#666" />
              <text class="new-name">{{ preview.newName }}</text>
            </view>
            <text v-if="renamePreview.length > 3" class="preview-more">
              还有 {{ renamePreview.length - 3 }} 个文件...
            </text>
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
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import ToolContainer from '@/components/tools/ToolContainer.vue'
import FileUploader from '@/components/tools/FileUploader.vue'
import { ImageProcessor } from '@/tools/image/processor.js'
import { ProgressTracker } from '@/tools/base/ProgressTracker.js'

// 响应式数据
const fileUploaderRef = ref(null)
const selectedFiles = ref([])

const selectedTypes = ref(['compress'])
const selectedQualityIndex = ref(1) // 标准质量
const selectedFormatIndex = ref(0) // JPEG
const selectedSizeIndex = ref(0) // 原始尺寸
const selectedResizeModeIndex = ref(1) // contain
const selectedWatermarkTypeIndex = ref(0) // 文字
const selectedWatermarkPositionIndex = ref(3) // 右下角

const compressOptions = ref({
  maxWidth: 1920,
  maxHeight: 1080
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

const renameOptions = ref({
  pattern: '{name}_{index}',
  startIndex: 1,
  prefix: '',
  suffix: ''
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
  { value: 'convert', name: '格式转换', description: '转换图片格式' },
  { value: 'resize', name: '尺寸调整', description: '调整图片尺寸' },
  { value: 'watermark', name: '添加水印', description: '批量添加水印' },
  { value: 'rename', name: '批量重命名', description: '重命名文件' }
]

const qualityPresets = imageProcessor.getQualityPresets()
const outputFormats = imageProcessor.getSupportedOutputFormats()
const presetSizes = imageProcessor.getPresetSizes()

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

const renamePreview = computed(() => {
  if (!selectedTypes.value.includes('rename') || selectedFiles.value.length === 0) {
    return []
  }
  
  return imageProcessor.batchRename(selectedFiles.value, renameOptions.value)
})

// 监听变化
watch(selectedQualityIndex, (newIndex) => {
  compressOptions.value.quality = qualityPresets[newIndex].value
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
}

const handleFileError = (error) => {
  errorMessage.value = error
}

const handleTypeChange = (e) => {
  const value = e.detail.value
  const index = selectedTypes.value.indexOf(value)
  
  if (index > -1) {
    selectedTypes.value.splice(index, 1)
  } else {
    selectedTypes.value.push(value)
  }
}

const handleQualityChange = (e) => {
  selectedQualityIndex.value = e.detail.value
}

const handleFormatChange = (e) => {
  selectedFormatIndex.value = e.detail.value
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
      selectedFiles.value.map(file => ({ file })),
      async (fileItem, index) => {
        return await processImage(fileItem.file, index)
      },
      (progress) => {
        tracker.setCurrent(progress.completed, `正在处理: ${progress.currentFile}`)
      }
    )
    
    results.value = processResults.map(result => ({
      success: result.success,
      file: { name: result.file.name, type: result.file.type },
      result: result.success ? result.result : null,
      originalSize: result.file.size,
      compressedSize: result.success ? result.result.size : 0,
      error: result.error
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

const processImage = async (file, index) => {
  let processedFile = file
  
  // 按顺序执行各种处理
  for (const type of selectedTypes.value) {
    switch (type) {
      case 'compress':
        processedFile = await imageProcessor.compressImage(processedFile, {
          quality: qualityPresets[selectedQualityIndex.value].value,
          maxWidth: compressOptions.value.maxWidth,
          maxHeight: compressOptions.value.maxHeight
        })
        break
        
      case 'convert':
        const targetFormat = outputFormats[selectedFormatIndex.value].value
        processedFile = await imageProcessor.convertFormat(processedFile, targetFormat)
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

const handleDownload = (result, index) => {
  if (result.result) {
    let filename = result.file.name
    
    // 如果包含重命名，使用新文件名
    if (selectedTypes.value.includes('rename') && renamePreview.value[index]) {
      filename = renamePreview.value[index].newName
    }
    
    imageProcessor.downloadFile(result.result, filename)
  }
}

const handleDownloadAll = async () => {
  try {
    const files = results.value
      .filter(r => r.success)
      .map((result, index) => {
        let filename = result.file.name
        
        if (selectedTypes.value.includes('rename') && renamePreview.value[index]) {
          filename = renamePreview.value[index].newName
        }
        
        return {
          data: result.result,
          filename: filename,
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
  results.value = []
  statistics.value = {}
  errorMessage.value = ''
  progress.value = 0
  progressStatus.value = 'normal'
  progressMessage.value = ''
  
  if (fileUploaderRef.value) {
    fileUploaderRef.value.clearFiles()
  }
  
  selectedFiles.value = []
}

const handleReset = () => {
  selectedTypes.value = ['compress']
  selectedQualityIndex.value = 1
  selectedFormatIndex.value = 0
  selectedSizeIndex.value = 0
  selectedResizeModeIndex.value = 1
  selectedWatermarkTypeIndex.value = 0
  selectedWatermarkPositionIndex.value = 3
  
  compressOptions.value = {
    maxWidth: 1920,
    maxHeight: 1080
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
  
  renameOptions.value = {
    pattern: '{name}_{index}',
    startIndex: 1,
    prefix: '',
    suffix: ''
  }
  
  handleClear()
}

const handlePreview = (result, index) => {
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
  margin: 30rpx 0;
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

.option-group {
  margin-bottom: 40rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .option-label {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.process-types {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
  
  .type-option {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background: white;
    border: 2rpx solid #ddd;
    border-radius: 12rpx;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.active {
      border-color: #007aff;
      background: #f0f8ff;
    }
    
    checkbox {
      margin-right: 15rpx;
    }
    
    .type-name {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      margin-right: 15rpx;
    }
    
    .type-desc {
      font-size: 24rpx;
      color: #666;
      flex: 1;
    }
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .setting-label {
    font-size: 26rpx;
    color: #333;
    min-width: 120rpx;
  }
  
  .picker-display {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15rpx 20rpx;
    background: white;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    min-width: 200rpx;
  }
  
  .number-input,
  .text-input {
    padding: 15rpx 20rpx;
    background: white;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    min-width: 150rpx;
    
    &:focus {
      border-color: #007aff;
    }
  }
  
  .unit {
    font-size: 24rpx;
    color: #666;
    margin-left: 10rpx;
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

.rename-preview {
  margin-top: 20rpx;
  padding: 20rpx;
  background: white;
  border-radius: 12rpx;
  border: 2rpx solid #eee;
  
  .preview-title {
    font-size: 26rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 15rpx;
  }
  
  .preview-list {
    .preview-item {
      display: flex;
      align-items: center;
      gap: 15rpx;
      margin-bottom: 10rpx;
      
      .original-name {
        font-size: 24rpx;
        color: #666;
        flex: 1;
      }
      
      .new-name {
        font-size: 24rpx;
        color: #007aff;
        flex: 1;
      }
    }
    
    .preview-more {
      font-size: 22rpx;
      color: #999;
      text-align: center;
      margin-top: 10rpx;
    }
  }
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
</style>
