<template>
  <view class="progress-container" v-if="visible">
    <!-- 进度信息 -->
    <view class="progress-info">
      <view class="progress-message">{{ message || defaultMessage }}</view>
      <view class="progress-percentage">{{ percentage }}%</view>
    </view>
    
    <!-- 进度条 -->
    <view class="progress-bar">
      <view 
        class="progress-fill" 
        :class="[`status-${status}`]"
        :style="{ width: `${percentage}%` }"
      ></view>
    </view>
    
    <!-- 详细信息 -->
    <view v-if="showDetails && details" class="progress-details">
      <view v-if="details.current !== undefined && details.total !== undefined" class="detail-item">
        <text class="detail-label">进度:</text>
        <text class="detail-value">{{ details.current }} / {{ details.total }}</text>
      </view>
      
      <view v-if="details.elapsedTime" class="detail-item">
        <text class="detail-label">已用时间:</text>
        <text class="detail-value">{{ details.elapsedTime }}</text>
      </view>
      
      <view v-if="details.estimatedTimeRemaining && status === 'running'" class="detail-item">
        <text class="detail-label">预计剩余:</text>
        <text class="detail-value">{{ details.estimatedTimeRemaining }}</text>
      </view>
      
      <view v-if="details.processingRate" class="detail-item">
        <text class="detail-label">处理速度:</text>
        <text class="detail-value">{{ details.processingRate }}</text>
      </view>
      
      <view v-if="details.currentFile" class="detail-item">
        <text class="detail-label">当前文件:</text>
        <text class="detail-value current-file">{{ details.currentFile }}</text>
      </view>
    </view>
    
    <!-- 操作按钮 -->
    <view v-if="showActions" class="progress-actions">
      <button 
        v-if="status === 'running' && allowCancel" 
        class="action-btn cancel-btn"
        @click="handleCancel"
      >
        取消
      </button>
      
      <button 
        v-if="status === 'completed'" 
        class="action-btn success-btn"
        @click="handleComplete"
      >
        完成
      </button>
      
      <button 
        v-if="status === 'error'" 
        class="action-btn retry-btn"
        @click="handleRetry"
      >
        重试
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percentage: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 100
  },
  status: {
    type: String,
    default: 'normal',
    validator: (value) => ['normal', 'running', 'completed', 'error', 'warning'].includes(value)
  },
  message: {
    type: String,
    default: ''
  },
  details: {
    type: Object,
    default: () => ({})
  },
  showDetails: {
    type: Boolean,
    default: true
  },
  showActions: {
    type: Boolean,
    default: true
  },
  allowCancel: {
    type: Boolean,
    default: true
  },
  visible: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['cancel', 'complete', 'retry'])

// 计算属性
const defaultMessage = computed(() => {
  switch (props.status) {
    case 'running':
      return '正在处理中...'
    case 'completed':
      return '处理完成'
    case 'error':
      return '处理失败'
    case 'warning':
      return '处理警告'
    default:
      return '准备中...'
  }
})

// 事件处理
const handleCancel = () => {
  emit('cancel')
}

const handleComplete = () => {
  emit('complete')
}

const handleRetry = () => {
  emit('retry')
}
</script>

<style lang="scss" scoped>
.progress-container {
  padding: 30rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid #eee;
  margin: 20rpx 0;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .progress-message {
    font-size: 28rpx;
    color: #333;
    flex: 1;
  }
  
  .progress-percentage {
    font-size: 28rpx;
    font-weight: 600;
    color: #007aff;
  }
}

.progress-bar {
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  
  .progress-fill {
    height: 100%;
    border-radius: 6rpx;
    transition: width 0.3s ease;
    
    &.status-normal {
      background: #007aff;
    }
    
    &.status-running {
      background: linear-gradient(90deg, #007aff, #40a9ff);
      animation: progress-shine 2s infinite;
    }
    
    &.status-completed {
      background: #28a745;
    }
    
    &.status-error {
      background: #dc3545;
    }
    
    &.status-warning {
      background: #ffc107;
    }
  }
}

@keyframes progress-shine {
  0% {
    background-position: -100% 0;
  }
  100% {
    background-position: 100% 0;
  }
}

.progress-details {
  margin-bottom: 20rpx;
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .detail-label {
      font-size: 26rpx;
      color: #666;
      min-width: 120rpx;
    }
    
    .detail-value {
      font-size: 26rpx;
      color: #333;
      flex: 1;
      text-align: right;
      
      &.current-file {
        word-break: break-all;
        text-align: left;
        margin-left: 20rpx;
      }
    }
  }
}

.progress-actions {
  display: flex;
  justify-content: center;
  gap: 20rpx;
  
  .action-btn {
    padding: 15rpx 30rpx;
    border-radius: 8rpx;
    font-size: 26rpx;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.cancel-btn {
      background: #6c757d;
      color: white;
      
      &:hover {
        background: #5a6268;
      }
    }
    
    &.success-btn {
      background: #28a745;
      color: white;
      
      &:hover {
        background: #218838;
      }
    }
    
    &.retry-btn {
      background: #007aff;
      color: white;
      
      &:hover {
        background: #0056b3;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .progress-container {
    padding: 20rpx;
  }
  
  .progress-info {
    .progress-message {
      font-size: 26rpx;
    }
    
    .progress-percentage {
      font-size: 26rpx;
    }
  }
  
  .progress-details {
    .detail-item {
      .detail-label,
      .detail-value {
        font-size: 24rpx;
      }
    }
  }
  
  .progress-actions {
    .action-btn {
      padding: 12rpx 24rpx;
      font-size: 24rpx;
    }
  }
}
</style>
