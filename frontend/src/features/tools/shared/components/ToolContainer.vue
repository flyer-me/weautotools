<template>
  <view class="tool-container">
    <!-- 工具头部 -->
    <view class="tool-header">
      <view class="tool-title">{{ title }}</view>
      <view class="tool-desc" v-if="description">{{ description }}</view>
    </view>
    
    <!-- 工具内容区域 -->
    <view class="tool-content">
      <slot></slot>
    </view>
    
    <!-- 进度条 -->
    <ProgressBar 
      v-if="showProgress" 
      :percentage="progress" 
      :status="progressStatus"
      :message="progressMessage"
      :details="progressDetails"
    />
    
    <!-- 结果展示 -->
    <ResultDisplay 
      v-if="showResult" 
      :results="results"
      :statistics="statistics"
      @download="handleDownload"
      @download-all="handleDownloadAll"
      @clear="handleClear"
      @preview="handlePreview"
    />
    
    <!-- 错误提示 -->
    <view v-if="error" class="error-message">
      <uni-icons type="info" color="#ff4757" size="20" />
      <text class="error-text">{{ error }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import ProgressBar from './ProgressBar.vue'
import ResultDisplay from './ResultDisplay.vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  showProgress: {
    type: Boolean,
    default: false
  },
  progress: {
    type: Number,
    default: 0
  },
  progressStatus: {
    type: String,
    default: 'normal'
  },
  progressMessage: {
    type: String,
    default: ''
  },
  progressDetails: {
    type: Object,
    default: () => ({})
  },
  showResult: {
    type: Boolean,
    default: false
  },
  results: {
    type: Array,
    default: () => []
  },
  statistics: {
    type: Object,
    default: () => ({})
  },
  error: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'download',
  'download-all', 
  'clear',
  'preview'
])

const handleDownload = (result, index) => {
  emit('download', result, index)
}

const handleDownloadAll = () => {
  emit('download-all')
}

const handleClear = () => {
  emit('clear')
}

const handlePreview = (result, index) => {
  emit('preview', result, index)
}
</script>

<style lang="scss" scoped>
.tool-container {
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  margin: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.tool-header {
  margin-bottom: 30rpx;
  text-align: center;
  
  .tool-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .tool-desc {
    font-size: 28rpx;
    color: #666;
    line-height: 1.5;
  }
}

.tool-content {
  margin-bottom: 30rpx;
}

.error-message {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff5f5;
  border: 2rpx solid #fed7d7;
  border-radius: 12rpx;
  margin-top: 20rpx;
  
  .error-text {
    margin-left: 10rpx;
    color: #e53e3e;
    font-size: 28rpx;
    flex: 1;
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .tool-container {
    margin: 10rpx;
    padding: 15rpx;
  }
  
  .tool-header {
    .tool-title {
      font-size: 32rpx;
    }
    
    .tool-desc {
      font-size: 26rpx;
    }
  }
}
</style>
