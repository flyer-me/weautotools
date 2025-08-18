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
