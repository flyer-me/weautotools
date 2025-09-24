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
      <view class="format-selector">
        <view class="format-group">
          <text class="format-label">源格式</text>
          <picker
            :range="formatOptions"
            range-key="label"
            :value="fromFormatIndex"
            @change="handleFromFormatChange"
          >
            <view class="picker-display">
              {{ formatOptions[fromFormatIndex].label }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>
        
        <view class="convert-arrow">
          <uni-icons type="arrowright" size="24" color="#007aff" />
        </view>
        
        <view class="format-group">
          <text class="format-label">目标格式</text>
          <picker
            :range="formatOptions"
            range-key="label"
            :value="toFormatIndex"
            @change="handleToFormatChange"
          >
            <view class="picker-display">
              {{ formatOptions[toFormatIndex].label }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>
      </view>
      
      <!-- 快速转换按钮 -->
      <view class="quick-conversions">
        <button
          v-for="conversion in quickConversions"
          :key="`${conversion.from}-${conversion.to}`"
          class="quick-btn"
          :class="{ active: isCurrentConversion(conversion) }"
          @click="setConversion(conversion)"
        >
          {{ conversion.label }}
        </button>
      </view>
    </view>

    <!-- 输入输出区域 -->
    <view class="io-container">
      <!-- 输入区域 -->
      <view class="io-section">
        <view class="section-header">
          <text class="section-title">输入 ({{ formatOptions[fromFormatIndex].label }})</text>
          <view class="section-actions">
            <button class="action-btn" @click="validateInput">
              <uni-icons type="checkmarkempty" size="14" color="#28a745" />
              <text>验证</text>
            </button>
            <button class="action-btn" @click="beautifyInput">
              <uni-icons type="compose" size="14" color="#007aff" />
              <text>美化</text>
            </button>
            <button class="action-btn" @click="clearInput">
              <uni-icons type="clear" size="14" color="#dc3545" />
              <text>清空</text>
            </button>
          </view>
        </view>
        
        <textarea
          v-model="inputContent"
          class="content-textarea"
          :placeholder="getInputPlaceholder()"
          :maxlength="-1"
          @input="handleInputChange"
        />

        <!-- 格式提示 -->
        <view v-if="!inputContent.trim()" class="format-hint">
          <view class="hint-title">
            <uni-icons type="info" size="14" color="#007aff" />
            <text>{{ formatOptions[fromFormatIndex].label }} 格式说明</text>
          </view>
          <view class="hint-content">{{ getFormatHint() }}</view>
        </view>

        <!-- 验证结果 -->
        <view v-if="validationResult" class="validation-result">
          <view 
            class="validation-message"
            :class="{ success: validationResult.valid, error: !validationResult.valid }"
          >
            <uni-icons 
              :type="validationResult.valid ? 'checkmarkempty' : 'close'" 
              size="16" 
              :color="validationResult.valid ? '#28a745' : '#dc3545'" 
            />
            <text>{{ validationResult.message }}</text>
          </view>
        </view>
      </view>

      <!-- 输出区域 -->
      <view class="io-section">
        <view class="section-header">
          <text class="section-title">输出 ({{ formatOptions[toFormatIndex].label }})</text>
          <view class="section-actions">
            <button 
              class="action-btn" 
              @click="copyOutput"
              :disabled="!outputContent"
            >
              <uni-icons type="copy" size="14" color="#007aff" />
              <text>复制</text>
            </button>
            <button 
              class="action-btn" 
              @click="downloadOutput"
              :disabled="!outputContent"
            >
              <uni-icons type="download" size="14" color="#28a745" />
              <text>下载</text>
            </button>
          </view>
        </view>
        
        <textarea
          v-model="outputContent"
          class="content-textarea"
          placeholder="转换结果将显示在这里..."
          :maxlength="-1"
          readonly
        />
      </view>
    </view>

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
    <view v-if="batchResults.length > 0" class="batch-results">
      <view class="results-header">
        <text class="results-title">批量转换结果</text>
        <view class="results-stats">
          <text class="stat-item success">成功: {{ batchStats.successful }}</text>
          <text class="stat-item error">失败: {{ batchStats.failed }}</text>
        </view>
      </view>
      
      <view class="results-list">
        <view 
          v-for="(result, index) in batchResults" 
          :key="index"
          class="result-item"
          :class="{ success: result.success, error: !result.success }"
        >
          <view class="result-info">
            <text class="result-filename">{{ result.input.name }}</text>
            <text class="result-status">
              {{ result.success ? '转换成功' : result.error }}
            </text>
          </view>
          
          <view v-if="result.success" class="result-actions">
            <button 
              class="mini-btn download-btn"
              @click="downloadBatchResult(result, index)"
            >
              <uni-icons type="download" size="14" color="white" />
            </button>
          </view>
        </view>
      </view>
      
      <view class="batch-actions">
        <button class="action-btn primary-btn" @click="downloadAllResults">
          <uni-icons type="download" size="16" color="white" />
          <text>下载全部</text>
        </button>
        <button class="action-btn secondary-btn" @click="clearBatchResults">
          <uni-icons type="clear" size="16" color="#666" />
          <text>清空结果</text>
        </button>
      </view>
    </view>
  </ToolContainer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import ToolContainer from '@/features/tools/shared/components/ToolContainer.vue'
import FileUploader from '@/features/tools/shared/components/FileUploader.vue'
import { DataConverter } from '@/features/tools/data/converter.js'
import { ProgressTracker } from '@/features/tools/base/ProgressTracker.js'
import { useUsageLimit } from '@/composables/useUsageLimit'

// 响应式数据
const fileUploaderRef = ref(null)

const inputContent = ref('')
const outputContent = ref('')
const fromFormatIndex = ref(0) // JSON
const toFormatIndex = ref(1) // YAML
const validationResult = ref(null)

const isConverting = ref(false)
const progress = ref(0)
const progressStatus = ref('normal')
const progressMessage = ref('')
const errorMessage = ref('')

const batchResults = ref([])

// 工具实例
const dataConverter = new DataConverter()

// 使用限制相关
const {
  useFrontendTool
} = useUsageLimit()

// 配置选项
const formatOptions = [
  { value: 'json', label: 'JSON' },
  { value: 'yaml', label: 'YAML' },
  { value: 'xml', label: 'XML' }
]

const quickConversions = dataConverter.getSupportedConversions()

// 计算属性
const canConvert = computed(() => {
  return inputContent.value.trim().length > 0 && 
         fromFormatIndex.value !== toFormatIndex.value
})

const batchStats = computed(() => {
  const successful = batchResults.value.filter(r => r.success).length
  const failed = batchResults.value.filter(r => !r.success).length
  return { successful, failed }
})

// 监听输入变化，自动检测格式
watch(inputContent, (newContent) => {
  if (newContent.trim()) {
    const detectedFormat = dataConverter.detectFormat(newContent)
    const formatIndex = formatOptions.findIndex(f => f.value === detectedFormat)
    if (formatIndex !== -1 && formatIndex !== fromFormatIndex.value) {
      fromFormatIndex.value = formatIndex
    }
  }
  
  // 清除之前的验证结果
  validationResult.value = null
}, { debounce: 300 })

// 方法
const handleFromFormatChange = (e) => {
  fromFormatIndex.value = e.detail.value
  validationResult.value = null
}

const handleToFormatChange = (e) => {
  toFormatIndex.value = e.detail.value
}

const isCurrentConversion = (conversion) => {
  const fromFormat = formatOptions[fromFormatIndex.value].value
  const toFormat = formatOptions[toFormatIndex.value].value
  return conversion.from === fromFormat && conversion.to === toFormat
}

const setConversion = (conversion) => {
  const fromIndex = formatOptions.findIndex(f => f.value === conversion.from)
  const toIndex = formatOptions.findIndex(f => f.value === conversion.to)
  
  if (fromIndex !== -1) fromFormatIndex.value = fromIndex
  if (toIndex !== -1) toFormatIndex.value = toIndex
}

const handleInputChange = () => {
  validationResult.value = null
  outputContent.value = ''
}

const getInputPlaceholder = () => {
  const format = formatOptions[fromFormatIndex.value].value
  const examples = {
    json: '请输入 JSON 数据，例如：\n{\n  "name": "示例",\n  "value": 123\n}',
    yaml: '请输入 YAML 数据，例如：\nname: 示例\nvalue: 123\nlist:\n  - 项目1\n  - 项目2',
    xml: '请输入 XML 数据，例如：\n<?xml version="1.0"?>\n<root>\n  <name>示例</name>\n  <value>123</value>\n</root>'
  }
  return examples[format] || '请输入要转换的数据...'
}

const getFormatHint = () => {
  const format = formatOptions[fromFormatIndex.value].value
  const hints = {
    json: 'JSON 是一种轻量级的数据交换格式，使用键值对表示数据，注意字符串需要双引号包围。',
    yaml: 'YAML 是一种人类可读的数据序列化标准，使用缩进表示层级关系，注意缩进必须使用空格。',
    xml: 'XML 是一种标记语言，使用标签包围数据，每个开始标签都必须有对应的结束标签。'
  }
  return hints[format] || ''
}

const validateInput = async () => {
  if (!inputContent.value.trim()) {
    uni.showToast({
      title: '请输入内容',
      icon: 'none'
    })
    return
  }
  
  const format = formatOptions[fromFormatIndex.value].value
  
  try {
    let result
    switch (format) {
      case 'json':
        result = dataConverter.validateJson(inputContent.value)
        break
      case 'yaml':
        result = dataConverter.validateYaml(inputContent.value)
        break
      case 'xml':
        result = await dataConverter.validateXml(inputContent.value)
        break
    }
    
    validationResult.value = result

    const toastTitle = result.valid
      ? `${format.toUpperCase()} 格式正确 ✓`
      : `${format.toUpperCase()} 格式错误: ${result.message}`

    uni.showToast({
      title: toastTitle,
      icon: result.valid ? 'success' : 'none',
      duration: result.valid ? 1500 : 3000
    })
  } catch (error) {
    validationResult.value = {
      valid: false,
      message: error.message
    }
  }
}

const beautifyInput = () => {
  if (!inputContent.value.trim()) return
  
  const format = formatOptions[fromFormatIndex.value].value
  
  try {
    let beautified
    switch (format) {
      case 'json':
        beautified = dataConverter.beautifyJson(inputContent.value)
        break
      case 'yaml':
        beautified = dataConverter.beautifyYaml(inputContent.value)
        break
      default:
        uni.showToast({
          title: `${format.toUpperCase()} 格式暂不支持美化功能`,
          icon: 'none',
          duration: 2000
        })
        return
    }

    const originalLength = inputContent.value.length
    inputContent.value = beautified
    const newLength = beautified.length

    uni.showToast({
      title: `${format.toUpperCase()} 美化完成 (${originalLength} → ${newLength} 字符)`,
      icon: 'success',
      duration: 2000
    })
  } catch (error) {
    uni.showToast({
      title: `美化失败: ${error.message}`,
      icon: 'none',
      duration: 3000
    })
  }
}

const clearInput = () => {
  inputContent.value = ''
  outputContent.value = ''
  validationResult.value = null
}

const handleConvert = async () => {
  if (!canConvert.value || isConverting.value) return
  
  // 1. 预检查使用限制
  const usageResult = await useFrontendTool('data-convert', 1)
  if (!usageResult.canUse) {
    return // 已显示限制提示
  }
  
  isConverting.value = true
  progressStatus.value = 'running'
  progressMessage.value = '正在转换...'
  errorMessage.value = ''
  
  try {
    const fromFormat = formatOptions[fromFormatIndex.value].value
    const toFormat = formatOptions[toFormatIndex.value].value

    // 先验证输入格式
    progressMessage.value = '验证输入格式...'
    let validation
    switch (fromFormat) {
      case 'json':
        validation = dataConverter.validateJson(inputContent.value)
        break
      case 'yaml':
        validation = dataConverter.validateYaml(inputContent.value)
        break
      case 'xml':
        validation = await dataConverter.validateXml(inputContent.value)
        break
    }

    if (!validation.valid) {
      throw new Error(`输入格式错误: ${validation.message}`)
    }

    // 执行转换
    progressMessage.value = `正在转换 ${fromFormat.toUpperCase()} → ${toFormat.toUpperCase()}...`
    const result = await dataConverter.convert(inputContent.value, fromFormat, toFormat)
    outputContent.value = result

    progressStatus.value = 'completed'
    progressMessage.value = `转换完成 (${inputContent.value.length} → ${result.length} 字符)`
    
    // 3. 成功后报告使用
    await usageResult.reportUsage(1)

    uni.showToast({
      title: `${fromFormat.toUpperCase()} → ${toFormat.toUpperCase()} 转换成功`,
      icon: 'success',
      duration: 2000
    })
  } catch (error) {
    progressStatus.value = 'error'
    progressMessage.value = '转换失败'

    // 提供更详细的错误信息
    let userFriendlyMessage = error.message
    if (error.message.includes('JSON')) {
      userFriendlyMessage = 'JSON 格式错误，请检查语法是否正确'
    } else if (error.message.includes('YAML')) {
      userFriendlyMessage = 'YAML 格式错误，请检查缩进和语法'
    } else if (error.message.includes('XML')) {
      userFriendlyMessage = 'XML 格式错误，请检查标签是否正确闭合'
    }

    errorMessage.value = userFriendlyMessage
    // 失败时不记录使用次数

    uni.showToast({
      title: userFriendlyMessage,
      icon: 'none',
      duration: 3000
    })
  } finally {
    isConverting.value = false
  }
}

const copyOutput = () => {
  if (!outputContent.value) return
  
  // 检查是否在浏览器环境
  if (typeof navigator !== 'undefined' && navigator.clipboard) {
    navigator.clipboard.writeText(outputContent.value).then(() => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    }).catch(() => {
      fallbackCopy(outputContent.value)
    })
  } else if (typeof uni !== 'undefined') {
    uni.setClipboardData({
      data: outputContent.value,
      success: () => {
        uni.showToast({
          title: '已复制到剪贴板',
          icon: 'success'
        })
      }
    })
  } else {
    fallbackCopy(outputContent.value)
  }
}

const fallbackCopy = (content) => {
  const textArea = document.createElement('textarea')
  textArea.value = content
  document.body.appendChild(textArea)
  textArea.select()
  document.execCommand('copy')
  document.body.removeChild(textArea)
  
  uni.showToast({
    title: '已复制到剪贴板',
    icon: 'success'
  })
}

const downloadOutput = () => {
  if (!outputContent.value) return
  
  const format = formatOptions[toFormatIndex.value].value
  const filename = `converted_${Date.now()}`
  
  dataConverter.downloadResult(outputContent.value, filename, format)
}

// 文件处理方法
const validateFileFormat = (file) => {
  const supportedExts = ['.json', '.xml', '.yaml', '.yml', '.txt']
  return supportedExts.some(ext => file.name.toLowerCase().endsWith(ext))
}

const handleFileChange = async (files) => {
  if (files.length === 0) return
  
  const validFiles = files.filter(f => f.valid)
  if (validFiles.length === 0) {
    uni.showToast({
      title: '没有有效的数据文件',
      icon: 'none'
    })
    return
  }
  
  await convertFiles(validFiles)
}

const handleFileError = (error) => {
  errorMessage.value = error
}

const convertFiles = async (files) => {
  if (isConverting.value) return
  
  // 1. 预检查使用限制
  const usageResult = await useFrontendTool('data-convert', files.length)
  if (!usageResult.canUse) {
    return // 已显示限制提示
  }
  
  isConverting.value = true
  progress.value = 0
  progressStatus.value = 'running'
  errorMessage.value = ''
  batchResults.value = []
  
  try {
    const fromFormat = formatOptions[fromFormatIndex.value].value
    const toFormat = formatOptions[toFormatIndex.value].value
    
    // 读取文件内容
    const inputs = await Promise.all(
      files.map(async (fileItem) => {
        const content = await readFileContent(fileItem.file)
        return {
          name: fileItem.file.name,
          content: content
        }
      })
    )
    
    const tracker = new ProgressTracker(inputs.length, (status) => {
      progress.value = status.percentage
      progressMessage.value = `正在转换第 ${status.current}/${status.total} 个文件...`
    })
    
    tracker.start()
    
    const results = await dataConverter.convertBatch(
      inputs, 
      fromFormat, 
      toFormat, 
      {}, 
      (progress) => {
        tracker.setCurrent(progress.completed, `正在处理: ${progress.currentItem}`)
      }
    )
    
    batchResults.value = results
    
    progressStatus.value = 'completed'
    progressMessage.value = '批量转换完成'
    
    tracker.complete()
    
    // 3. 成功后报告使用
    const successfulCount = batchStats.value.successful
    await usageResult.reportUsage(successfulCount)
    
  } catch (error) {
    progressStatus.value = 'error'
    progressMessage.value = '批量转换失败'
    errorMessage.value = error.message
    // 失败时不记录使用次数
  } finally {
    isConverting.value = false
  }
}

const readFileContent = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsText(file)
  })
}

const downloadBatchResult = (result, index) => {
  const format = formatOptions[toFormatIndex.value].value
  const originalName = result.input.name.replace(/\.[^/.]+$/, '')
  const filename = `${originalName}_converted`
  
  dataConverter.downloadResult(result.result, filename, format)
}

const downloadAllResults = () => {
  const successResults = batchResults.value.filter(r => r.success)
  
  successResults.forEach((result, index) => {
    setTimeout(() => {
      downloadBatchResult(result, index)
    }, index * 500)
  })
}

const clearBatchResults = () => {
  batchResults.value = []
  if (fileUploaderRef.value) {
    fileUploaderRef.value.clearFiles()
  }
}

const handleClear = () => {
  inputContent.value = ''
  outputContent.value = ''
  validationResult.value = null
  errorMessage.value = ''
  progress.value = 0
  progressStatus.value = 'normal'
  progressMessage.value = ''
  clearBatchResults()
}
</script>

<style lang="scss" scoped>
.conversion-settings {
  margin-bottom: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
}

.format-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
  
  .format-group {
    flex: 1;
    
    .format-label {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 15rpx;
    }
    
    .picker-display {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx;
      background: white;
      border: 2rpx solid #ddd;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }
  
  .convert-arrow {
    margin: 0 30rpx;
    padding-top: 40rpx;
  }
}

.quick-conversions {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  
  .quick-btn {
    padding: 15rpx 25rpx;
    background: white;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 24rpx;
    color: #666;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.active {
      background: #007aff;
      color: white;
      border-color: #007aff;
    }
    
    &:hover {
      border-color: #007aff;
    }
  }
}

.io-container {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
  
  .io-section {
    flex: 1;
    
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 15rpx;
      
      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
      
      .section-actions {
        display: flex;
        gap: 10rpx;
        
        .action-btn {
          display: flex;
          align-items: center;
          gap: 5rpx;
          padding: 10rpx 15rpx;
          background: #f8f9fa;
          border: 2rpx solid #dee2e6;
          border-radius: 6rpx;
          font-size: 22rpx;
          cursor: pointer;
          
          &:hover {
            background: #e9ecef;
          }
          
          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
        }
      }
    }
    
    .content-textarea {
      width: 100%;
      height: 400rpx;
      padding: 20rpx;
      border: 2rpx solid #ddd;
      border-radius: 12rpx;
      font-size: 26rpx;
      font-family: 'Courier New', monospace;
      line-height: 1.5;
      resize: vertical;
      
      &:focus {
        border-color: #007aff;
      }
      
      &[readonly] {
        background: #f8f9fa;
        color: #666;
      }
    }
  }
}

.format-hint {
  margin-top: 15rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 12rpx;

  .hint-title {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 26rpx;
    font-weight: 500;
    color: #007aff;
    margin-bottom: 10rpx;
  }

  .hint-content {
    font-size: 24rpx;
    color: #666;
    line-height: 1.5;
  }
}

.validation-result {
  margin-top: 15rpx;
  
  .validation-message {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 15rpx;
    border-radius: 8rpx;
    font-size: 24rpx;
    
    &.success {
      background: #d4edda;
      color: #155724;
      border: 2rpx solid #c3e6cb;
    }
    
    &.error {
      background: #f8d7da;
      color: #721c24;
      border: 2rpx solid #f5c6cb;
    }
  }
}

.convert-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30rpx;
  
  .convert-btn {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 25rpx 50rpx;
    background: #007aff;
    color: white;
    border: none;
    border-radius: 12rpx;
    font-size: 28rpx;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background: #0056b3;
    }
    
    &:disabled {
      background: #ccc;
      cursor: not-allowed;
    }
  }
}

.file-section {
  margin-bottom: 30rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.batch-results {
  margin-top: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  
  .results-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .results-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .results-stats {
      display: flex;
      gap: 20rpx;
      
      .stat-item {
        font-size: 24rpx;
        
        &.success {
          color: #28a745;
        }
        
        &.error {
          color: #dc3545;
        }
      }
    }
  }
  
  .results-list {
    margin-bottom: 30rpx;
    
    .result-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx;
      background: white;
      border-radius: 12rpx;
      border: 2rpx solid #ddd;
      margin-bottom: 15rpx;
      
      &.success {
        border-color: #28a745;
        background: #f8fff9;
      }
      
      &.error {
        border-color: #dc3545;
        background: #fff8f8;
      }
      
      .result-info {
        flex: 1;
        
        .result-filename {
          display: block;
          font-size: 26rpx;
          color: #333;
          font-weight: 500;
          margin-bottom: 5rpx;
        }
        
        .result-status {
          font-size: 22rpx;
          color: #666;
        }
      }
      
      .result-actions {
        .mini-btn {
          width: 60rpx;
          height: 60rpx;
          border-radius: 8rpx;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.download-btn {
            background: #007aff;
          }
        }
      }
    }
  }
  
  .batch-actions {
    display: flex;
    gap: 20rpx;
    justify-content: center;
    
    .action-btn {
      display: flex;
      align-items: center;
      gap: 10rpx;
      padding: 20rpx 30rpx;
      border-radius: 12rpx;
      font-size: 26rpx;
      border: none;
      cursor: pointer;
      
      &.primary-btn {
        background: #007aff;
        color: white;
      }
      
      &.secondary-btn {
        background: #f8f9fa;
        color: #666;
        border: 2rpx solid #dee2e6;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .io-container {
    flex-direction: column;
    
    .io-section {
      .content-textarea {
        height: 300rpx;
      }
    }
  }
  
  .format-selector {
    flex-direction: column;
    gap: 20rpx;
    
    .convert-arrow {
      margin: 0;
      padding: 0;
      transform: rotate(90deg);
    }
  }
}
</style>
