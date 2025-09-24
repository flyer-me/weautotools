<template>
  <ToolContainer
    title="数据格式转换"
    description="支持JSON、XML、YAML格式互转和验证"
    :showProgress="isConverting"
    :progress="progress"
    :progressStatus="progressStatus"
    :progressMessage="progressMessage"
    :showResult="false"
    :error="errorMessage"
    @clear="handleClear"
  >
    <!-- 转换设置 -->
    <view class="conversion-settings">
      <FormatSelector
        v-model:fromFormatIndex="fromFormatIndex"
        v-model:toFormatIndex="toFormatIndex"
        :formatOptions="formatOptions"
        :quickConversions="quickConversions"
        @formatChange="handleFormatChange"
      />
    </view>

    <!-- 输入输出区域 -->
    <DataEditor
      v-model:inputContent="inputContent"
      v-model:outputContent="outputContent"
      :fromFormatIndex="fromFormatIndex"
      :toFormatIndex="toFormatIndex"
      :formatOptions="formatOptions"
      :validationResult="validationResult"
      :inputPlaceholder="getInputPlaceholder()"
      :formatHint="getFormatHint()"
      @inputChange="handleInputChange"
      @validate="handleValidate"
      @beautify="handleBeautify"
      @clear="handleClearInput"
      @copy="handleCopy"
      @download="handleDownload"
    />

    <!-- 转换按钮 -->
    <view class="convert-section">
      <button
        class="convert-btn"
        :disabled="!canConvert || isConverting"
        @click="handleConvert"
      >
        <uni-icons 
          v-if="!isConverting" 
          type="refresh" 
          size="16" 
          color="white" 
        />
        <uni-icons 
          v-else 
          type="spinner-cycle" 
          size="16" 
          color="white" 
        />
        <text>{{ isConverting ? '转换中...' : '开始转换' }}</text>
      </button>
    </view>

    <!-- 文件上传区域 -->
    <view class="file-section">
      <view class="section-title">文件转换</view>
      <FileUploader
        ref="fileUploaderRef"
        accept=".json,.xml,.yaml,.yml,.txt"
        :multiple="true"
        :maxCount="10"
        uploadTitle="选择数据文件"
        uploadDesc="支持 JSON、XML、YAML 格式文件"
        acceptText="数据格式文件"
        :validateFormat="validateFileFormat"
        @change="handleFileChange"
        @error="handleFileError"
      />
    </view>

    <!-- 批量转换结果 -->
    <BatchResults
      :batchResults="batchResults"
      @downloadResult="handleDownloadBatchResult"
      @downloadAll="handleDownloadAllResults"
      @clearResults="handleClearBatchResults"
    />
  </ToolContainer>
</template>

<script setup>
import { ref } from 'vue'
import ToolContainer from '@/features/tools/shared/components/ToolContainer.vue'
import FileUploader from '@/features/tools/shared/components/FileUploader.vue'
import FormatSelector from './components/FormatSelector.vue'
import DataEditor from './components/DataEditor.vue'
import BatchResults from './components/BatchResults.vue'

// 导入 composable
import { useDataConverter } from './composables/useDataConverter.js'

// 使用 composable
const {
  inputContent,
  outputContent,
  fromFormatIndex,
  toFormatIndex,
  validationResult,
  isConverting,
  progress,
  progressStatus,
  progressMessage,
  errorMessage,
  batchResults,
  formatOptions,
  quickConversions,
  canConvert,
  batchStats,
  getInputPlaceholder,
  getFormatHint,
  validateInput,
  beautifyInput,
  convertData,
  copyOutput,
  downloadOutput,
  convertBatch,
  downloadBatchResult,
  downloadAllResults,
  clearResults,
  validateFileFormat
} = useDataConverter()

// 本地状态
const fileUploaderRef = ref(null)

// 事件处理
const handleFormatChange = (formats) => {
  // 格式变化时的处理逻辑
  console.log('格式变化:', formats)
}

const handleInputChange = () => {
  // 输入变化时的处理逻辑
}

const handleValidate = async () => {
  await validateInput()
}

const handleBeautify = async () => {
  await beautifyInput()
}

const handleClearInput = () => {
  inputContent.value = ''
  outputContent.value = ''
  validationResult.value = null
}

const handleCopy = () => {
  copyOutput()
}

const handleDownload = () => {
  downloadOutput()
}

const handleConvert = async () => {
  await convertData()
}

const handleFileChange = async (files) => {
  const validFiles = files.filter(f => f.valid).map(f => f.file)
  if (validFiles.length > 0) {
    await convertBatch(validFiles)
  }
}

const handleFileError = (error) => {
  uni.showToast({
    title: error,
    icon: 'none'
  })
}

const handleDownloadBatchResult = (result, index) => {
  downloadBatchResult(result, index)
}

const handleDownloadAllResults = async () => {
  await downloadAllResults()
}

const handleClearBatchResults = () => {
  batchResults.value = []
}

const handleClear = () => {
  clearResults()
  
  if (fileUploaderRef.value) {
    fileUploaderRef.value.clearFiles()
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.conversion-settings {
  margin-bottom: 32rpx;
}

.convert-section {
  display: flex;
  justify-content: center;
  margin: 32rpx 0;

  .convert-btn {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 20rpx 40rpx;
    background: linear-gradient(135deg, #007AFF 0%, #0056b3 100%);
    color: white;
    border: none;
    border-radius: 12rpx;
    font-size: 28rpx;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.2);

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
      transform: translateY(-2rpx);
      box-shadow: 0 6rpx 16rpx rgba(0, 122, 255, 0.3);
    }

    &:disabled {
      background: #ccc;
      cursor: not-allowed;
      transform: none;
      box-shadow: none;
    }
  }
}

.file-section {
  margin-top: 40rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid #f0f0f0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    padding-bottom: 12rpx;
    border-bottom: 2rpx solid #f0f0f0;
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .convert-section {
    .convert-btn {
      padding: 16rpx 32rpx;
      font-size: 26rpx;
    }
  }

  .file-section {
    padding: 20rpx;
    margin-top: 32rpx;

    .section-title {
      font-size: 28rpx;
    }
  }
}
</style>
