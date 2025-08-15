<template>
  <view class="debug-page">
    <view class="debug-header">
      <text class="debug-title">二维码调试工具</text>
      <text class="debug-desc">测试二维码生成和识别功能</text>
    </view>
    
    <!-- 生成测试二维码 -->
    <view class="debug-section">
      <view class="section-title">1. 生成测试二维码</view>
      <view class="input-group">
        <input 
          v-model="testText" 
          class="text-input" 
          placeholder="输入要生成二维码的文本"
        />
        <button class="generate-btn" @click="generateTestQR">生成</button>
      </view>
      
      <view v-if="generatedQR" class="qr-preview">
        <image 
          :src="generatedQR.dataURL" 
          class="qr-image"
          mode="aspectFit"
          @click="downloadQR"
        />
        <text class="qr-info">点击下载二维码图片</text>
      </view>
    </view>
    
    <!-- 上传识别测试 -->
    <view class="debug-section">
      <view class="section-title">2. 上传图片识别测试</view>
      <view class="upload-area" @click="selectImage">
        <uni-icons type="image" size="48" color="#ccc" />
        <text class="upload-text">点击选择图片</text>
      </view>
      
      <view v-if="selectedImage" class="image-preview">
        <image 
          :src="selectedImageURL" 
          class="preview-image"
          mode="aspectFit"
        />
        <button class="decode-btn" @click="decodeImage">识别二维码</button>
      </view>
    </view>
    
    <!-- 识别结果 -->
    <view v-if="decodeResult" class="debug-section">
      <view class="section-title">3. 识别结果</view>
      <view class="result-container" :class="{ success: decodeResult.success, error: !decodeResult.success }">
        <view v-if="decodeResult.success" class="success-result">
          <text class="result-label">识别成功！</text>
          <text class="result-content">{{ decodeResult.data }}</text>
          <view v-if="decodeResult.analysis" class="result-analysis">
            <text class="analysis-label">内容类型:</text>
            <text class="analysis-value">{{ decodeResult.analysis.description }}</text>
          </view>
        </view>
        
        <view v-else class="error-result">
          <text class="result-label">识别失败</text>
          <text class="result-error">{{ decodeResult.error }}</text>
        </view>
      </view>
    </view>
    
    <!-- 调试日志 -->
    <view class="debug-section">
      <view class="section-title">4. 调试日志</view>
      <view class="log-container">
        <text 
          v-for="(log, index) in logs" 
          :key="index"
          class="log-item"
          :class="log.type"
        >
          {{ log.message }}
        </text>
      </view>
      <button class="clear-logs-btn" @click="clearLogs">清空日志</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { QRGenerator } from '@/tools/qrcode/generator.js'
import { QRDecoder } from '@/tools/qrcode/decoder.js'

// 响应式数据
const testText = ref('https://weautotools.com/test')
const generatedQR = ref(null)
const selectedImage = ref(null)
const selectedImageURL = ref('')
const decodeResult = ref(null)
const logs = ref([])

// 工具实例
const qrGenerator = new QRGenerator()
const qrDecoder = new QRDecoder()

// 方法
const addLog = (message, type = 'info') => {
  logs.value.push({
    message: `[${new Date().toLocaleTimeString()}] ${message}`,
    type,
    timestamp: Date.now()
  })
}

const generateTestQR = async () => {
  if (!testText.value.trim()) {
    addLog('请输入文本内容', 'error')
    return
  }
  
  try {
    addLog('开始生成二维码...', 'info')
    
    const result = await qrGenerator.generate(testText.value, {
      width: 256,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    
    generatedQR.value = result
    addLog(`二维码生成成功: ${result.width}x${result.height}, ${result.size}字节`, 'success')
    
  } catch (error) {
    addLog(`二维码生成失败: ${error.message}`, 'error')
  }
}

const downloadQR = () => {
  if (generatedQR.value) {
    qrGenerator.download(generatedQR.value, 'test-qr.png')
    addLog('二维码已下载', 'info')
  }
}

const selectImage = () => {
  // 创建文件输入元素
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      selectedImage.value = file
      selectedImageURL.value = URL.createObjectURL(file)
      addLog(`图片已选择: ${file.name}, ${file.size}字节`, 'info')
      
      // 清除之前的识别结果
      decodeResult.value = null
    }
  }
  
  input.click()
}

const decodeImage = async () => {
  if (!selectedImage.value) {
    addLog('请先选择图片', 'error')
    return
  }
  
  try {
    addLog('开始识别二维码...', 'info')
    
    // 重写console方法来捕获调试信息
    const originalLog = console.log
    const originalError = console.error
    const originalWarn = console.warn
    
    console.log = (...args) => {
      addLog(args.join(' '), 'debug')
      originalLog(...args)
    }
    
    console.error = (...args) => {
      addLog(args.join(' '), 'error')
      originalError(...args)
    }
    
    console.warn = (...args) => {
      addLog(args.join(' '), 'warning')
      originalWarn(...args)
    }
    
    const result = await qrDecoder.decodeFromFile(selectedImage.value)
    
    // 恢复console方法
    console.log = originalLog
    console.error = originalError
    console.warn = originalWarn
    
    // 分析内容类型
    if (result.success && result.data) {
      result.analysis = qrDecoder.analyzeContent(result.data)
    }
    
    decodeResult.value = result
    
    if (result.success) {
      addLog(`识别成功: ${result.data}`, 'success')
    } else {
      addLog(`识别失败: ${result.error}`, 'error')
    }
    
  } catch (error) {
    addLog(`识别异常: ${error.message}`, 'error')
    decodeResult.value = {
      success: false,
      error: error.message
    }
  }
}

const clearLogs = () => {
  logs.value = []
}

// 页面加载时的初始化
import { onMounted } from 'vue'

onMounted(() => {
  addLog('二维码调试工具已加载', 'info')
})
</script>

<style lang="scss" scoped>
.debug-page {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.debug-header {
  text-align: center;
  margin-bottom: 30rpx;
  
  .debug-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .debug-desc {
    font-size: 28rpx;
    color: #666;
  }
}

.debug-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

.input-group {
  display: flex;
  gap: 15rpx;
  margin-bottom: 20rpx;
  
  .text-input {
    flex: 1;
    padding: 20rpx;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 28rpx;
  }
  
  .generate-btn {
    padding: 20rpx 30rpx;
    background: #007aff;
    color: white;
    border: none;
    border-radius: 8rpx;
    font-size: 28rpx;
    cursor: pointer;
  }
}

.qr-preview {
  text-align: center;
  
  .qr-image {
    width: 200rpx;
    height: 200rpx;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    margin-bottom: 15rpx;
    cursor: pointer;
  }
  
  .qr-info {
    display: block;
    font-size: 24rpx;
    color: #666;
  }
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
  border: 4rpx dashed #ddd;
  border-radius: 16rpx;
  background: #fafafa;
  cursor: pointer;
  margin-bottom: 20rpx;
  
  &:hover {
    border-color: #007aff;
    background: #f0f8ff;
  }
  
  .upload-text {
    margin-top: 15rpx;
    font-size: 28rpx;
    color: #666;
  }
}

.image-preview {
  text-align: center;
  
  .preview-image {
    width: 300rpx;
    height: 300rpx;
    border: 2rpx solid #ddd;
    border-radius: 8rpx;
    margin-bottom: 20rpx;
  }
  
  .decode-btn {
    padding: 20rpx 40rpx;
    background: #28a745;
    color: white;
    border: none;
    border-radius: 8rpx;
    font-size: 28rpx;
    cursor: pointer;
  }
}

.result-container {
  padding: 30rpx;
  border-radius: 12rpx;
  
  &.success {
    background: #f8fff9;
    border: 2rpx solid #d4edda;
  }
  
  &.error {
    background: #fff8f8;
    border: 2rpx solid #f8d7da;
  }
  
  .result-label {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    margin-bottom: 15rpx;
  }
  
  .success-result .result-label {
    color: #28a745;
  }
  
  .error-result .result-label {
    color: #dc3545;
  }
  
  .result-content {
    display: block;
    font-size: 26rpx;
    color: #333;
    word-break: break-all;
    margin-bottom: 15rpx;
    padding: 20rpx;
    background: white;
    border-radius: 8rpx;
  }
  
  .result-error {
    font-size: 26rpx;
    color: #dc3545;
  }
  
  .result-analysis {
    display: flex;
    align-items: center;
    gap: 15rpx;
    
    .analysis-label {
      font-size: 24rpx;
      color: #666;
    }
    
    .analysis-value {
      font-size: 24rpx;
      color: #007aff;
      font-weight: 500;
    }
  }
}

.log-container {
  max-height: 300rpx;
  overflow-y: auto;
  background: #f8f9fa;
  border-radius: 8rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  
  .log-item {
    display: block;
    font-size: 22rpx;
    line-height: 1.5;
    margin-bottom: 5rpx;
    font-family: 'Courier New', monospace;
    
    &.info {
      color: #333;
    }
    
    &.success {
      color: #28a745;
    }
    
    &.error {
      color: #dc3545;
    }
    
    &.warning {
      color: #ffc107;
    }
    
    &.debug {
      color: #6c757d;
    }
  }
}

.clear-logs-btn {
  padding: 15rpx 25rpx;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
  cursor: pointer;
}
</style>
