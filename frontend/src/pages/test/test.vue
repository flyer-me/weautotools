<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">工具功能测试</text>
      <text class="test-desc">验证各个工具的基本功能</text>
    </view>
    
    <!-- 测试控制 -->
    <view class="test-controls">
      <button 
        class="test-btn primary-btn"
        :disabled="isRunning"
        @click="runTests"
      >
        <uni-icons 
          v-if="!isRunning" 
          type="play" 
          size="16" 
          color="white" 
        />
        <uni-icons 
          v-else 
          type="spinner-cycle" 
          size="16" 
          color="white" 
        />
        <text>{{ isRunning ? '测试中...' : '运行测试' }}</text>
      </button>
      
      <button
        class="test-btn secondary-btn"
        :disabled="isRunning"
        @click="runPerformanceTest"
      >
        <uni-icons type="flash" size="16" color="#666" />
        <text>性能测试</text>
      </button>

      <button
        class="test-btn secondary-btn"
        :disabled="isRunning"
        @click="runQRTests"
      >
        <uni-icons type="scan" size="16" color="#666" />
        <text>二维码测试</text>
      </button>
      
      <button 
        class="test-btn secondary-btn"
        @click="checkMemory"
      >
        <uni-icons type="gear" size="16" color="#666" />
        <text>内存检查</text>
      </button>
      
      <button 
        class="test-btn secondary-btn"
        @click="clearResults"
      >
        <uni-icons type="clear" size="16" color="#666" />
        <text>清空结果</text>
      </button>
    </view>
    
    <!-- 测试结果 -->
    <view v-if="testResults.length > 0" class="test-results">
      <view class="results-header">
        <text class="results-title">测试结果</text>
        <view class="results-summary">
          <text class="summary-item success">通过: {{ successCount }}</text>
          <text class="summary-item error">失败: {{ failCount }}</text>
          <text class="summary-item">成功率: {{ successRate }}%</text>
        </view>
      </view>
      
      <view class="results-list">
        <view 
          v-for="(result, index) in testResults" 
          :key="index"
          class="result-item"
          :class="{ success: result.success, error: !result.success }"
        >
          <view class="result-icon">
            <uni-icons 
              :type="result.success ? 'checkmarkempty' : 'close'" 
              size="20" 
              :color="result.success ? '#28a745' : '#dc3545'" 
            />
          </view>
          
          <view class="result-info">
            <text class="result-name">{{ result.name }}</text>
            <text v-if="result.error" class="result-error">{{ result.error }}</text>
          </view>
          
          <view v-if="result.duration" class="result-duration">
            <text>{{ result.duration }}ms</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 性能测试结果 -->
    <view v-if="performanceResults" class="performance-results">
      <view class="performance-header">
        <text class="performance-title">性能测试结果</text>
      </view>
      
      <view class="performance-metrics">
        <view class="metric-item">
          <text class="metric-label">总耗时</text>
          <text class="metric-value">{{ performanceResults.totalTime?.toFixed(2) }}ms</text>
        </view>
        
        <view class="metric-item">
          <text class="metric-label">批量处理耗时</text>
          <text class="metric-value">{{ performanceResults.batchTime?.toFixed(2) }}ms</text>
        </view>
        
        <view class="metric-item">
          <text class="metric-label">平均耗时</text>
          <text class="metric-value">{{ performanceResults.avgTime?.toFixed(2) }}ms/个</text>
        </view>
        
        <view class="metric-item">
          <text class="metric-label">处理速度</text>
          <text class="metric-value">{{ performanceResults.throughput }}</text>
        </view>
      </view>
    </view>
    
    <!-- 内存使用情况 -->
    <view v-if="memoryInfo" class="memory-info">
      <view class="memory-header">
        <text class="memory-title">内存使用情况</text>
      </view>
      
      <view class="memory-metrics">
        <view class="memory-item">
          <text class="memory-label">已使用</text>
          <text class="memory-value">{{ formatMemory(memoryInfo.used) }}</text>
        </view>
        
        <view class="memory-item">
          <text class="memory-label">总分配</text>
          <text class="memory-value">{{ formatMemory(memoryInfo.total) }}</text>
        </view>
        
        <view class="memory-item">
          <text class="memory-label">限制</text>
          <text class="memory-value">{{ formatMemory(memoryInfo.limit) }}</text>
        </view>
      </view>
    </view>
    
    <!-- 日志输出 -->
    <view v-if="logs.length > 0" class="test-logs">
      <view class="logs-header">
        <text class="logs-title">测试日志</text>
        <button class="clear-logs-btn" @click="clearLogs">
          <uni-icons type="clear" size="14" color="#666" />
          <text>清空</text>
        </button>
      </view>
      
      <view class="logs-content">
        <text 
          v-for="(log, index) in logs" 
          :key="index"
          class="log-item"
          :class="log.type"
        >
          {{ log.message }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

// 响应式数据
const isRunning = ref(false)
const testResults = ref([])
const performanceResults = ref(null)
const memoryInfo = ref(null)
const logs = ref([])

// 计算属性
const successCount = computed(() => testResults.value.filter(r => r.success).length)
const failCount = computed(() => testResults.value.filter(r => !r.success).length)
const successRate = computed(() => {
  if (testResults.value.length === 0) return 0
  return ((successCount.value / testResults.value.length) * 100).toFixed(1)
})

// 方法
const addLog = (message, type = 'info') => {
  logs.value.push({
    message: `[${new Date().toLocaleTimeString()}] ${message}`,
    type,
    timestamp: Date.now()
  })
}

const runTests = async () => {
  if (isRunning.value) return
  
  isRunning.value = true
  testResults.value = []
  addLog('开始运行功能测试...', 'info')
  
  try {
    // 动态导入测试工具
    const { runAllTests } = await import('@/utils/testUtils.js')
    
    // 重写console方法来捕获日志
    const originalLog = console.log
    const originalError = console.error
    
    console.log = (...args) => {
      addLog(args.join(' '), 'info')
      originalLog(...args)
    }
    
    console.error = (...args) => {
      addLog(args.join(' '), 'error')
      originalError(...args)
    }
    
    // 运行测试
    const results = await runAllTests()
    
    // 恢复console方法
    console.log = originalLog
    console.error = originalError
    
    testResults.value = results.results
    addLog(`测试完成: ${results.successful}/${results.total} 通过`, 'success')
    
  } catch (error) {
    addLog(`测试失败: ${error.message}`, 'error')
    console.error('测试运行失败:', error)
  } finally {
    isRunning.value = false
  }
}

const runPerformanceTest = async () => {
  addLog('开始性能测试...', 'info')

  try {
    const { performanceTest } = await import('@/utils/testUtils.js')

    const results = await performanceTest()
    performanceResults.value = results

    if (results) {
      addLog(`性能测试完成: 总耗时 ${results.totalTime.toFixed(2)}ms`, 'success')
    } else {
      addLog('性能测试失败', 'error')
    }
  } catch (error) {
    addLog(`性能测试失败: ${error.message}`, 'error')
  }
}

const runQRTests = async () => {
  addLog('开始二维码专项测试...', 'info')

  try {
    const { runAllQRTests } = await import('@/utils/qrTestUtils.js')

    // 重写console方法来捕获日志
    const originalLog = console.log
    const originalError = console.error
    const originalWarn = console.warn

    console.log = (...args) => {
      addLog(args.join(' '), 'info')
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

    const results = await runAllQRTests()

    // 恢复console方法
    console.log = originalLog
    console.error = originalError
    console.warn = originalWarn

    // 更新测试结果
    testResults.value = results.results.map(r => ({
      name: `二维码-${r.name}`,
      success: r.success,
      error: r.message
    }))

    addLog(`二维码测试完成: ${results.successful}/${results.total} 通过`, 'success')

  } catch (error) {
    addLog(`二维码测试失败: ${error.message}`, 'error')
  }
}

const checkMemory = async () => {
  try {
    const { memoryTest } = await import('@/utils/testUtils.js')
    
    const memory = memoryTest()
    memoryInfo.value = memory
    
    if (memory) {
      addLog(`内存检查完成: 已使用 ${formatMemory(memory.used)}`, 'info')
    } else {
      addLog('当前环境不支持内存监控', 'warning')
    }
  } catch (error) {
    addLog(`内存检查失败: ${error.message}`, 'error')
  }
}

const clearResults = () => {
  testResults.value = []
  performanceResults.value = null
  memoryInfo.value = null
  addLog('测试结果已清空', 'info')
}

const clearLogs = () => {
  logs.value = []
}

const formatMemory = (bytes) => {
  if (!bytes) return '0 MB'
  const mb = bytes / 1024 / 1024
  return `${mb.toFixed(2)} MB`
}

// 页面加载时检查内存
import { onMounted } from 'vue'

onMounted(() => {
  addLog('测试页面已加载', 'info')
  checkMemory()
})
</script>

<style lang="scss" scoped>
.test-page {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.test-header {
  text-align: center;
  margin-bottom: 30rpx;
  
  .test-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .test-desc {
    font-size: 28rpx;
    color: #666;
  }
}

.test-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  margin-bottom: 30rpx;
  
  .test-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 20rpx 30rpx;
    border-radius: 12rpx;
    font-size: 26rpx;
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
      background: white;
      color: #666;
      border: 2rpx solid #ddd;
      
      &:hover {
        background: #f8f9fa;
      }
    }
  }
}

.test-results,
.performance-results,
.memory-info {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.results-header,
.performance-header,
.memory-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  
  .results-title,
  .performance-title,
  .memory-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .results-summary {
    display: flex;
    gap: 20rpx;
    
    .summary-item {
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
  .result-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    border-radius: 12rpx;
    margin-bottom: 15rpx;
    
    &.success {
      background: #f8fff9;
      border: 2rpx solid #d4edda;
    }
    
    &.error {
      background: #fff8f8;
      border: 2rpx solid #f8d7da;
    }
    
    .result-icon {
      margin-right: 15rpx;
    }
    
    .result-info {
      flex: 1;
      
      .result-name {
        display: block;
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 5rpx;
      }
      
      .result-error {
        font-size: 24rpx;
        color: #dc3545;
      }
    }
    
    .result-duration {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.performance-metrics,
.memory-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
  gap: 20rpx;
  
  .metric-item,
  .memory-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 12rpx;
    
    .metric-label,
    .memory-label {
      font-size: 24rpx;
      color: #666;
      margin-bottom: 8rpx;
    }
    
    .metric-value,
    .memory-value {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
  }
}

.test-logs {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  
  .logs-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;
    
    .logs-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .clear-logs-btn {
      display: flex;
      align-items: center;
      gap: 5rpx;
      padding: 10rpx 15rpx;
      background: #f8f9fa;
      border: 2rpx solid #ddd;
      border-radius: 8rpx;
      font-size: 22rpx;
      color: #666;
      cursor: pointer;
    }
  }
  
  .logs-content {
    max-height: 400rpx;
    overflow-y: auto;
    background: #f8f9fa;
    border-radius: 8rpx;
    padding: 20rpx;
    
    .log-item {
      display: block;
      font-size: 24rpx;
      line-height: 1.5;
      margin-bottom: 8rpx;
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
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .test-controls {
    .test-btn {
      flex: 1;
      min-width: 0;
    }
  }
  
  .performance-metrics,
  .memory-metrics {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15rpx;
  }
}
</style>
