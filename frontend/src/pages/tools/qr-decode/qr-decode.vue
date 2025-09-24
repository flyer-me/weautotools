<template>
  <ToolContainer
    title="二维码识别器"
    description="上传图片或使用摄像头识别二维码内容"
    :showProgress="isDecoding"
    :progress="progress"
    :progressStatus="progressStatus"
    :progressMessage="progressMessage"
    :showResult="results.length > 0"
    :results="results"
    :statistics="statistics"
    :error="errorMessage"
    @download="handleDownload"
    @clear="handleClear"
  >
    <!-- 上传区域 -->
    <FileUploader
      ref="fileUploaderRef"
      accept="image/*"
      :multiple="true"
      :maxCount="20"
      uploadTitle="选择图片"
      uploadDesc="支持 JPG、PNG、GIF、WebP 格式"
      acceptText="图片格式"
      :validateFormat="validateImageFormat"
      @change="handleFileChange"
      @error="handleFileError"
    />

    <!-- 摄像头扫描区域 (H5端) -->
    <view class="camera-section">
      <view class="section-title">摄像头扫描</view>
      
      <view v-if="!isCameraActive" class="camera-controls">
        <button
          class="action-btn primary-btn"
          @click="startCamera"
          :disabled="isDecoding"
        >
          <uni-icons type="videocam" size="16" color="white" />
          <text>启动摄像头</text>
        </button>
      </view>
      
      <view v-else class="camera-container">
        <video
          ref="videoRef"
          class="camera-video"
          autoplay
          playsinline
          muted
        ></video>
        
        <view class="camera-overlay">
          <view class="scan-frame"></view>
          <text class="scan-hint">将二维码对准扫描框</text>
        </view>
        
        <view class="camera-controls">
          <button
            class="action-btn secondary-btn"
            @click="stopCamera"
          >
            <uni-icons type="close" size="16" color="#666" />
            <text>停止扫描</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 识别结果详情 -->
    <view v-if="selectedResult" class="result-detail">
      <view class="detail-title">识别详情</view>
      
      <view class="detail-content">
        <!-- 内容类型 -->
        <view class="detail-item">
          <text class="detail-label">内容类型:</text>
          <text class="detail-value">{{ selectedResult.analysis?.description || '普通文本' }}</text>
        </view>
        
        <!-- 识别内容 -->
        <view class="detail-item">
          <text class="detail-label">识别内容:</text>
          <view class="content-display">
            <text class="content-text" selectable>{{ selectedResult.data }}</text>
            <button class="copy-btn" @click="copyContent(selectedResult.data)">
              <uni-icons type="copy" size="14" color="#007aff" />
            </button>
          </view>
        </view>
        
        <!-- 特殊内容解析 -->
        <view v-if="selectedResult.analysis?.type === 'url'" class="detail-item">
          <text class="detail-label">操作:</text>
          <button class="action-link" @click="openUrl(selectedResult.data)">
            <uni-icons type="arrowright" size="14" color="#007aff" />
            <text>打开链接</text>
          </button>
        </view>
        
        <view v-if="selectedResult.analysis?.type === 'wifi'" class="detail-item">
          <text class="detail-label">WiFi信息:</text>
          <view class="wifi-info">
            <text>网络名称: {{ selectedResult.analysis.wifi?.ssid }}</text>
            <text>安全类型: {{ selectedResult.analysis.wifi?.security }}</text>
            <text v-if="selectedResult.analysis.wifi?.password">
              密码: {{ selectedResult.analysis.wifi.password }}
            </text>
          </view>
        </view>
        
        <!-- 技术信息 -->
        <view class="detail-item">
          <text class="detail-label">技术信息:</text>
          <view class="tech-info">
            <text v-if="selectedResult.version">版本: {{ selectedResult.version }}</text>
            <text v-if="selectedResult.errorCorrectionLevel">
              纠错级别: {{ selectedResult.errorCorrectionLevel }}
            </text>
          </view>
        </view>
      </view>
      
      <view class="detail-actions">
        <button class="action-btn secondary-btn" @click="selectedResult = null">
          <uni-icons type="close" size="16" color="#666" />
          <text>关闭</text>
        </button>
      </view>
    </view>
  </ToolContainer>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import ToolContainer from '@/features/tools/shared/components/ToolContainer.vue'
import FileUploader from '@/features/tools/shared/components/FileUploader.vue'
import { QRDecoder } from '@/features/tools/qrcode/decoder.js'
import { ProgressTracker } from '@/features/tools/base/ProgressTracker.js'
import { useUsageLimit } from '@/composables/useUsageLimit'

// 响应式数据
const fileUploaderRef = ref(null)
const videoRef = ref(null)

const isDecoding = ref(false)
const progress = ref(0)
const progressStatus = ref('normal')
const progressMessage = ref('')
const results = ref([])
const statistics = ref({})
const errorMessage = ref('')
const selectedResult = ref(null)

const isCameraActive = ref(false)
const cameraStream = ref(null)

// 工具实例
const qrDecoder = new QRDecoder()

// 使用限制相关
const {
  useFrontendTool
} = useUsageLimit()

// 方法
const validateImageFormat = (file) => {
  const supportedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  return supportedTypes.includes(file.type)
}

const handleFileChange = async (files) => {
  if (files.length === 0) return
  
  const validFiles = files.filter(f => f.valid).map(f => f.file)
  if (validFiles.length === 0) {
    uni.showToast({
      title: '没有有效的图片文件',
      icon: 'none'
    })
    return
  }
  
  await decodeFiles(validFiles)
}

const handleFileError = (error) => {
  errorMessage.value = error
}

const decodeFiles = async (files) => {
  if (isDecoding.value) return

  // 1. 预检查使用限制
  const usageResult = await useFrontendTool('qr-decode', files.length)
  if (!usageResult.canUse) {
    return // 已显示限制提示
  }

  isDecoding.value = true
  progress.value = 0
  progressStatus.value = 'running'
  errorMessage.value = ''
  results.value = []

  try {
    const tracker = new ProgressTracker(files.length, (status) => {
      progress.value = status.percentage
      progressMessage.value = `正在识别第 ${status.current}/${status.total} 张图片...`
    })

    tracker.start()

    console.log('开始批量识别二维码，文件数量:', files.length)

    const decodeResults = await qrDecoder.decodeBatch(files, (progress) => {
      tracker.setCurrent(progress.completed, `正在处理: ${progress.currentFile}`)
    })

    console.log('批量识别完成，结果:', decodeResults)

    // 处理结果
    results.value = decodeResults.map(result => {
      console.log('处理识别结果:', result)

      const processedResult = {
        success: result.success,
        file: { name: result.file.name, type: result.file.type },
        result: result.success ? result.data : null, // ResultDisplay期望的result属性
        data: result.data,
        error: result.error,
        location: result.location,
        version: result.version,
        errorCorrectionLevel: result.errorCorrectionLevel,
        originalSize: result.file.size || 0,
        compressedSize: result.success ? (result.data ? result.data.length : 0) : 0
      }

      // 分析内容类型
      if (result.success && result.data) {
        processedResult.analysis = qrDecoder.analyzeContent(result.data)
        console.log('内容分析结果:', processedResult.analysis)
      }

      return processedResult
    })

    console.log('最终处理结果:', results.value)

    progressStatus.value = 'completed'
    progressMessage.value = '识别完成'

    // 计算统计信息
    updateStatistics()

    tracker.complete()

    // 如果有成功的结果，自动显示第一个
    const firstSuccess = results.value.find(r => r.success)
    if (firstSuccess) {
      selectedResult.value = firstSuccess
    }
    
    // 3. 成功后报告使用
    const successfulCount = results.value.filter(r => r.success).length
    await usageResult.reportUsage(successfulCount)

  } catch (error) {
    console.error('批量识别失败:', error)
    progressStatus.value = 'error'
    progressMessage.value = '识别失败'
    errorMessage.value = error.message
    // 失败时不记录使用次数
  } finally {
    isDecoding.value = false
  }
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

// 摄像头相关方法
const startCamera = async () => {
  try {
    const scanner = await qrDecoder.startCameraScanning(
      videoRef.value,
      (data, result) => {
        // 识别成功
        handleCameraResult(data, result)
        stopCamera()
      }
    )

    cameraStream.value = scanner
    isCameraActive.value = true

  } catch (error) {
    uni.showToast({
      title: error.message,
      icon: 'none'
    })
  }
}

const stopCamera = () => {
  if (cameraStream.value) {
    cameraStream.value.stop()
    cameraStream.value = null
  }
  isCameraActive.value = false
}

const handleCameraResult = async (data, result) => {
  // 1. 检查使用限制
  const usageResult = await useFrontendTool('qr-decode', 1)
  if (!usageResult.canUse) {
    return // 已显示限制提示
  }
  
  const cameraResult = {
    success: true,
    file: { name: 'camera_scan.jpg', type: 'image/jpeg' },
    data: data,
    location: result.location,
    version: result.version,
    errorCorrectionLevel: result.errorCorrectionLevel,
    analysis: qrDecoder.analyzeContent(data)
  }
  
  results.value = [cameraResult]
  updateStatistics()
  
  // 自动显示详情
  selectedResult.value = cameraResult
  
  // 2. 成功后报告使用
  await usageResult.reportUsage(1)
  
  uni.showToast({
    title: '识别成功',
    icon: 'success'
  })
}

// 结果处理方法
const handleDownload = (result, index) => {
  // 导出识别结果为文本文件
  const content = `二维码识别结果

文件名: ${result.file.name}
内容类型: ${result.analysis?.description || '普通文本'}
识别内容: ${result.data}
识别时间: ${new Date().toLocaleString()}

${result.version ? `版本: ${result.version}` : ''}
${result.errorCorrectionLevel ? `纠错级别: ${result.errorCorrectionLevel}` : ''}
${result.location ? `位置信息: ${JSON.stringify(result.location, null, 2)}` : ''}
`

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const filename = `qr_result_${result.file.name.replace(/\.[^/.]+$/, '')}_${Date.now()}.txt`

  qrDecoder.downloadFile(blob, filename)
}

const handleClear = () => {
  results.value = []
  statistics.value = {}
  errorMessage.value = ''
  selectedResult.value = null
  progress.value = 0
  progressStatus.value = 'normal'
  progressMessage.value = ''
  
  // 清空文件上传器
  if (fileUploaderRef.value) {
    fileUploaderRef.value.clearFiles()
  }
}

// 内容操作方法
const copyContent = (content) => {
  // 检查是否在浏览器环境
  if (typeof navigator !== 'undefined' && navigator.clipboard) {
    navigator.clipboard.writeText(content).then(() => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    }).catch(() => {
      fallbackCopy(content)
    })
  } else if (typeof uni !== 'undefined') {
    uni.setClipboardData({
      data: content,
      success: () => {
        uni.showToast({
          title: '已复制到剪贴板',
          icon: 'success'
        })
      }
    })
  } else {
    fallbackCopy(content)
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

const openUrl = (url) => {
  // 检查是否在浏览器环境
  if (typeof window !== 'undefined') {
    window.open(url, '_blank')
  } else if (typeof uni !== 'undefined') {
    uni.showModal({
      title: '打开链接',
      content: `是否要打开链接: ${url}`,
      success: (res) => {
        if (res.confirm) {
          // 小程序中复制链接让用户手动打开
          uni.setClipboardData({
            data: url,
            success: () => {
              uni.showToast({
                title: '链接已复制',
                icon: 'success'
              })
            }
          })
        }
      }
    })
  } else {
    console.log('打开链接:', url)
  }
}

// 页面卸载时清理摄像头
import { onUnmounted } from 'vue'

onUnmounted(() => {
  stopCamera()
})
</script>

<style lang="scss" scoped>
.camera-section {
  margin: 30rpx 0;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    text-align: center;
  }
}

.camera-controls {
  display: flex;
  justify-content: center;
  gap: 20rpx;
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 20rpx 30rpx;
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

.camera-container {
  position: relative;
  margin-bottom: 20rpx;
  
  .camera-video {
    width: 100%;
    height: 400rpx;
    border-radius: 12rpx;
    background: #000;
  }
  
  .camera-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    pointer-events: none;
    
    .scan-frame {
      width: 200rpx;
      height: 200rpx;
      border: 4rpx solid #007aff;
      border-radius: 12rpx;
      position: relative;
      
      &::before,
      &::after {
        content: '';
        position: absolute;
        width: 40rpx;
        height: 40rpx;
        border: 6rpx solid #007aff;
      }
      
      &::before {
        top: -6rpx;
        left: -6rpx;
        border-right: none;
        border-bottom: none;
      }
      
      &::after {
        bottom: -6rpx;
        right: -6rpx;
        border-left: none;
        border-top: none;
      }
    }
    
    .scan-hint {
      margin-top: 30rpx;
      color: white;
      font-size: 26rpx;
      text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
    }
  }
}

.result-detail {
  margin-top: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  
  .detail-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 30rpx;
    text-align: center;
  }
  
  .detail-content {
    margin-bottom: 30rpx;
  }
  
  .detail-item {
    margin-bottom: 25rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .detail-label {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 10rpx;
    }
    
    .detail-value {
      font-size: 28rpx;
      color: #333;
    }
    
    .content-display {
      display: flex;
      align-items: flex-start;
      gap: 15rpx;
      
      .content-text {
        flex: 1;
        font-size: 28rpx;
        color: #333;
        line-height: 1.5;
        word-break: break-all;
        padding: 20rpx;
        background: white;
        border-radius: 8rpx;
        border: 2rpx solid #ddd;
      }
      
      .copy-btn {
        padding: 20rpx;
        background: #f8f9fa;
        border: 2rpx solid #dee2e6;
        border-radius: 8rpx;
        cursor: pointer;
        
        &:hover {
          background: #e9ecef;
        }
      }
    }
    
    .action-link {
      display: flex;
      align-items: center;
      gap: 10rpx;
      padding: 15rpx 20rpx;
      background: #e3f2fd;
      color: #007aff;
      border: 2rpx solid #bbdefb;
      border-radius: 8rpx;
      font-size: 26rpx;
      cursor: pointer;
      
      &:hover {
        background: #bbdefb;
      }
    }
    
    .wifi-info,
    .tech-info {
      padding: 20rpx;
      background: white;
      border-radius: 8rpx;
      border: 2rpx solid #ddd;
      
      text {
        display: block;
        font-size: 26rpx;
        color: #333;
        margin-bottom: 8rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
  
  .detail-actions {
    display: flex;
    justify-content: center;
  }
}
</style>
