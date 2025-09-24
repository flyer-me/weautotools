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
      <view class="preview-hint">
        <uni-icons type="info" size="16" color="#666" />
        <text class="hint-text">{{ previewHint(selectedTypes) }}</text>
      </view>
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
    <ImageProcessOptions
      v-model:selectedTypes="selectedTypes"
      v-model:compressOptions="compressOptions"
      v-model:resizeOptions="resizeOptions"
      v-model:watermarkOptions="watermarkOptions"
      v-model:selectedCompressionModeIndex="selectedCompressionModeIndex"
      v-model:selectedQualityIndex="selectedQualityIndex"
      v-model:selectedSizeUnitIndex="selectedSizeUnitIndex"
      v-model:selectedResizeModeIndex="selectedResizeModeIndex"
      v-model:selectedWatermarkTypeIndex="selectedWatermarkTypeIndex"
      v-model:selectedWatermarkPositionIndex="selectedWatermarkPositionIndex"
      :processTypes="processTypes"
      :compressionModes="compressionModes"
      :qualityPresets="qualityPresets"
      :sizeUnits="sizeUnits"
      :sizePresets="sizePresets"
      :resizeModes="resizeModes"
      :dimensionPresets="dimensionPresets"
      :watermarkTypes="watermarkTypes"
      :watermarkPositions="watermarkPositions"
      @typeToggle="handleTypeToggle"
      @compressionModeChange="handleCompressionModeChange"
      @qualityChange="handleQualityChange"
      @targetSizeChange="handleTargetSizeChange"
      @dimensionChange="handleDimensionChange"
      @resizeModeChange="handleResizeModeChange"
      @watermarkTextChange="handleWatermarkTextChange"
      @watermarkFontSizeChange="handleWatermarkFontSizeChange"
      @watermarkPositionChange="handleWatermarkPositionChange"
      @watermarkTypeChange="handleWatermarkTypeChange"
    />

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
import { ref, watch } from 'vue'
import ToolContainer from '@/features/tools/shared/components/ToolContainer.vue'
import FileUploader from '@/features/tools/shared/components/FileUploader.vue'
import ImagePreviewCompare from '@/features/tools/shared/components/ImagePreviewCompare.vue'
import ImageProcessOptions from './components/ImageProcessOptions.vue'

// 导入 composables
import { useImageProcessor } from './composables/useImageProcessor.js'
import { useImagePreview } from './composables/useImagePreview.js'
import { useProcessSettings } from './composables/useProcessSettings.js'

// 使用 composables
const {
  isProcessing,
  progress,
  progressStatus,
  progressMessage,
  results,
  statistics,
  errorMessage,
  canProcess,
  processBatch,
  downloadFile,
  downloadAll,
  clearResults,
  validateImageFormat
} = useImageProcessor()

const {
  previewData,
  previewSettings,
  previewHint,
  generatePreview,
  updatePreview,
  clearPreview,
  handlePreviewModeChange,
  handlePreviewZoom,
  handlePreviewInfo,
  previewImage
} = useImagePreview()

const {
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
  processTypes,
  qualityPresets,
  compressionModes,
  sizeUnits,
  sizePresets,
  dimensionPresets,
  resizeModes,
  watermarkTypes,
  watermarkPositions,
  updateTargetSizeBytes,
  resetSettings,
  getProcessOptions
} = useProcessSettings()

// 本地状态
const fileUploaderRef = ref(null)
const selectedFiles = ref([])

// 文件处理
const handleFileChange = (files) => {
  selectedFiles.value = files.filter(f => f.valid).map(f => f.file)

  // 生成第一个文件的预览
  if (selectedFiles.value.length > 0) {
    generatePreview(selectedFiles.value[0])
    updatePreview(selectedFiles.value[0], getProcessOptions())
  } else {
    clearPreview()
  }
}

const handleFileError = (error) => {
  errorMessage.value = error
}

// 处理选项变化
const handleTypeToggle = (type) => {
  if (selectedFiles.value.length > 0) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleCompressionModeChange = (mode) => {
  if (selectedFiles.value.length > 0) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleQualityChange = (quality) => {
  if (selectedFiles.value.length > 0 && compressOptions.value.mode === 'quality') {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleTargetSizeChange = () => {
  if (selectedFiles.value.length > 0 && compressOptions.value.mode === 'size') {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleDimensionChange = () => {
  if (selectedFiles.value.length > 0 && selectedTypes.value.includes('resize')) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleResizeModeChange = (mode) => {
  if (selectedFiles.value.length > 0 && selectedTypes.value.includes('resize')) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleWatermarkTextChange = () => {
  if (selectedFiles.value.length > 0 && selectedTypes.value.includes('watermark')) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleWatermarkFontSizeChange = () => {
  if (selectedFiles.value.length > 0 && selectedTypes.value.includes('watermark')) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleWatermarkPositionChange = (position) => {
  if (selectedFiles.value.length > 0 && selectedTypes.value.includes('watermark')) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

const handleWatermarkTypeChange = (type) => {
  if (selectedFiles.value.length > 0 && selectedTypes.value.includes('watermark')) {
    updatePreview(selectedFiles.value[0], getProcessOptions())
  }
}

// 监听目标大小变化
watch(() => compressOptions.value.targetSize, () => {
  updateTargetSizeBytes()
})

watch(selectedSizeUnitIndex, () => {
  updateTargetSizeBytes()
})

// 处理操作
const handleProcess = async () => {
  if (!canProcess.value || isProcessing.value || selectedFiles.value.length === 0) return
  
  try {
    await processBatch(selectedFiles.value, getProcessOptions())
  } catch (error) {
    console.error('处理失败:', error)
  }
}

const handleReset = () => {
  resetSettings()
  handleClear()
}

const handleClear = () => {
  clearResults()
  clearPreview()

  if (fileUploaderRef.value) {
    fileUploaderRef.value.clearFiles()
  }

  selectedFiles.value = []
}

const handleDownload = (result) => {
  downloadFile(result)
}

const handleDownloadAll = async () => {
  await downloadAll()
}

const handlePreview = (result) => {
  previewImage(result)
}
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.preview-section {
  margin-bottom: 24rpx;

  .preview-hint {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 16rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 12rpx;
    color: #666;
    margin-bottom: 12rpx;

    .hint-text {
      font-size: 24rpx;
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

.statistics-section {
  margin-top: 40rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);

  .statistics-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;
    padding-bottom: 20rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .statistics-title {
      font-size: 36rpx;
      font-weight: 700;
      color: #333;
    }

    .statistics-summary {
      display: flex;
      gap: 20rpx;
      align-items: center;

      .summary-text {
        font-size: 26rpx;
        color: #666;
      }

      .summary-success {
        font-size: 26rpx;
        color: #28a745;
        font-weight: 600;
      }

      .summary-failed {
        font-size: 26rpx;
        color: #dc3545;
        font-weight: 600;
      }
    }
  }

  .statistics-details {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
    gap: 24rpx;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 24rpx;
      background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
      border-radius: 12rpx;
      border: 2rpx solid #f0f0f0;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2rpx);
        box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 56rpx;
        height: 56rpx;
        background: rgba(0, 122, 255, 0.1);
        border-radius: 12rpx;
        flex-shrink: 0;
      }

      .stat-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;

        .stat-label {
          font-size: 24rpx;
          color: #666;
          font-weight: 500;
        }

        .stat-value {
          font-size: 32rpx;
          color: #333;
          font-weight: 700;

          &.compression-ratio {
            color: #17a2b8;
          }

          &.success-rate {
            color: #28a745;
          }
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .statistics-section {
    padding: 24rpx;

    .statistics-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16rpx;

      .statistics-summary {
        flex-direction: column;
        align-items: flex-start;
        gap: 8rpx;
      }
    }

    .statistics-details {
      grid-template-columns: 1fr;
      gap: 16rpx;

      .stat-card {
        padding: 20rpx;

        .stat-icon {
          width: 48rpx;
          height: 48rpx;
        }

        .stat-content {
          .stat-value {
            font-size: 28rpx;
          }
        }
      }
    }
  }
}
</style>
