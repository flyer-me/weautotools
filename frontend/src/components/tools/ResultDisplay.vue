<template>
  <view class="result-display" v-if="results.length || Object.keys(statistics).length">
    <!-- 统计信息 -->
    <view v-if="Object.keys(statistics).length" class="statistics">
      <view class="statistics-title">处理统计</view>
      <view class="statistics-grid">
        <view v-if="statistics.total !== undefined" class="stat-item">
          <text class="stat-label">总数</text>
          <text class="stat-value">{{ statistics.total }}</text>
        </view>
        
        <view v-if="statistics.successful !== undefined" class="stat-item success">
          <text class="stat-label">成功</text>
          <text class="stat-value">{{ statistics.successful }}</text>
        </view>
        
        <view v-if="statistics.failed !== undefined" class="stat-item error">
          <text class="stat-label">失败</text>
          <text class="stat-value">{{ statistics.failed }}</text>
        </view>
        
        <view v-if="statistics.successRate" class="stat-item">
          <text class="stat-label">成功率</text>
          <text class="stat-value">{{ statistics.successRate }}</text>
        </view>
        
        <view v-if="statistics.originalSize" class="stat-item">
          <text class="stat-label">原始大小</text>
          <text class="stat-value">{{ statistics.originalSize }}</text>
        </view>
        
        <view v-if="statistics.processedSize" class="stat-item">
          <text class="stat-label">处理后大小</text>
          <text class="stat-value">{{ statistics.processedSize }}</text>
        </view>
        
        <view v-if="statistics.compressionRatio" class="stat-item">
          <text class="stat-label">压缩率</text>
          <text class="stat-value">{{ statistics.compressionRatio }}</text>
        </view>
      </view>
    </view>
    
    <!-- 操作按钮 -->
    <view v-if="results.length" class="result-actions">
      <button 
        class="action-btn primary-btn"
        @click="handleDownloadAll"
        :disabled="!hasSuccessResults"
      >
        <uni-icons type="download" size="16" color="white" />
        <text>下载全部</text>
      </button>
      
      <button 
        class="action-btn secondary-btn"
        @click="handleClear"
      >
        <uni-icons type="clear" size="16" color="#666" />
        <text>清空结果</text>
      </button>
    </view>
    
    <!-- 结果列表 -->
    <view v-if="results.length" class="result-list">
      <view class="result-list-header">
        <text class="result-count">处理结果 ({{ results.length }})</text>
      </view>
      
      <view 
        v-for="(result, index) in results" 
        :key="index"
        class="result-item"
        :class="{ 'success': result.success, 'error': !result.success }"
      >
        <!-- 文件信息 -->
        <view class="result-info">
          <!-- 缩略图或图标 -->
          <view class="result-thumbnail">
            <image
              v-if="result.success && result.thumbnail"
              :src="result.thumbnail"
              class="thumbnail-image"
              mode="aspectFill"
            />
            <view v-else class="result-icon">
              <uni-icons
                :type="getResultIcon(result)"
                size="24"
                :color="result.success ? '#28a745' : '#dc3545'"
              />
            </view>
          </view>

          <view class="result-details">
            <!-- 原始文件名 -->
            <text class="result-filename original">{{ result.file?.name || '未知文件' }}</text>

            <!-- 处理后文件名 -->
            <text v-if="result.success && result.file?.processedName" class="result-filename processed">
              → {{ result.file.processedName }}
            </text>

            <text class="result-status">
              {{ result.success ? '处理成功' : (result.error || '处理失败') }}
            </text>

            <!-- 成功结果的额外信息 -->
            <view v-if="result.success && result.result" class="result-meta">
              <text v-if="result.originalSize && result.compressedSize" class="meta-item">
                {{ formatFileSize(result.originalSize) }} → {{ formatFileSize(result.compressedSize) }}
              </text>
              <text v-if="result.compressionRatio" class="meta-item">
                压缩率: {{ result.compressionRatio }}
              </text>
            </view>
          </view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="result-actions-item">
          <button 
            v-if="result.success && result.result"
            class="mini-btn download-btn"
            @click="handleDownload(result, index)"
          >
            <uni-icons type="download" size="14" color="white" />
          </button>
          
          <button 
            v-if="result.success && canPreview(result)"
            class="mini-btn preview-btn"
            @click="handlePreview(result, index)"
          >
            <uni-icons type="eye" size="14" color="white" />
          </button>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-if="!results.length && !Object.keys(statistics).length" class="empty-state">
      <uni-icons type="info" size="48" color="#ccc" />
      <text class="empty-text">暂无处理结果</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  results: {
    type: Array,
    default: () => []
  },
  statistics: {
    type: Object,
    default: () => ({})
  },
  allowPreview: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['download', 'download-all', 'clear', 'preview'])

// 计算属性
const hasSuccessResults = computed(() => {
  return props.results.some(result => result.success && result.result)
})

// 事件处理
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

// 工具函数
const getResultIcon = (result) => {
  if (!result.success) return 'close'
  
  const fileType = result.file?.type || ''
  if (fileType.includes('image')) return 'image'
  if (fileType.includes('pdf')) return 'paperclip'
  return 'checkmarkempty'
}

const canPreview = (result) => {
  if (!props.allowPreview || !result.success) return false
  
  const fileType = result.file?.type || ''
  return fileType.includes('image') || fileType.includes('pdf')
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
.result-display {
  margin-top: 30rpx;
}

.statistics {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  
  .statistics-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    text-align: center;
  }
  
  .statistics-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
    gap: 20rpx;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx;
      background: white;
      border-radius: 12rpx;
      border: 2rpx solid #eee;
      
      &.success {
        border-color: #28a745;
        background: #f8fff9;
      }
      
      &.error {
        border-color: #dc3545;
        background: #fff8f8;
      }
      
      .stat-label {
        font-size: 24rpx;
        color: #666;
        margin-bottom: 8rpx;
      }
      
      .stat-value {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }
}

.result-actions {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 24rpx 36rpx;
    border-radius: 16rpx;
    font-size: 28rpx;
    font-weight: 500;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

    &:hover {
      transform: translateY(-2rpx);
      box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.15);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    }

    &.primary-btn {
      background: linear-gradient(135deg, #007aff 0%, #0056b3 100%);
      color: white;

      &:hover {
        background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
      }

      &:disabled {
        background: #ccc;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
      }
    }

    &.secondary-btn {
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      color: #666;
      border: 2rpx solid #dee2e6;

      &:hover {
        background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
        border-color: #adb5bd;
      }
    }
  }
}

.result-list {
  .result-list-header {
    padding: 20rpx 0;
    border-bottom: 2rpx solid #eee;
    margin-bottom: 20rpx;
    
    .result-count {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }
  }
  
  .result-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 25rpx;
    background: #fff;
    border-radius: 12rpx;
    border: 2rpx solid #eee;
    margin-bottom: 15rpx;
    
    &.success {
      border-color: #28a745;
      background: #f8fff9;
    }
    
    &.error {
      border-color: #dc3545;
      background: #fff8f8;
    }
  }
}

.result-info {
  display: flex;
  align-items: center;
  flex: 1;

  .result-thumbnail {
    margin-right: 20rpx;
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
    overflow: hidden;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    display: flex;
    align-items: center;
    justify-content: center;

    .thumbnail-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .result-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
    }
  }

  .result-details {
    flex: 1;

    .result-filename {
      display: block;
      font-size: 26rpx;
      font-weight: 500;
      margin-bottom: 4rpx;
      word-break: break-all;
      line-height: 1.3;

      &.original {
        color: #666;
        font-size: 24rpx;
      }

      &.processed {
        color: #007AFF;
        font-weight: 600;
        margin-bottom: 6rpx;
      }
    }

    .result-status {
      display: block;
      font-size: 22rpx;
      color: #666;
      margin-bottom: 6rpx;
    }

    .result-meta {
      .meta-item {
        display: inline-block;
        font-size: 20rpx;
        color: #999;
        margin-right: 16rpx;
        background: rgba(0, 0, 0, 0.05);
        padding: 2rpx 8rpx;
        border-radius: 6rpx;
      }
    }
  }
}

.result-actions-item {
  display: flex;
  gap: 12rpx;

  .mini-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 12rpx;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);

    &:hover {
      transform: translateY(-2rpx);
      box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.2);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
    }

    &.download-btn {
      background: linear-gradient(135deg, #007aff 0%, #0056b3 100%);

      &:hover {
        background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
      }
    }

    &.preview-btn {
      background: linear-gradient(135deg, #28a745 0%, #1e7e34 100%);

      &:hover {
        background: linear-gradient(135deg, #1e7e34 0%, #155724 100%);
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  
  .empty-text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #999;
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .statistics {
    padding: 20rpx;
    
    .statistics-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 15rpx;
      
      .stat-item {
        padding: 15rpx;
        
        .stat-label {
          font-size: 22rpx;
        }
        
        .stat-value {
          font-size: 26rpx;
        }
      }
    }
  }
  
  .result-actions {
    .action-btn {
      padding: 15rpx 20rpx;
      font-size: 26rpx;
    }
  }
  
  .result-item {
    padding: 20rpx;

    .result-info {
      .result-thumbnail {
        width: 64rpx;
        height: 64rpx;
        margin-right: 16rpx;
      }
    }

    .result-details {
      .result-filename {
        font-size: 24rpx;

        &.original {
          font-size: 22rpx;
        }

        &.processed {
          font-size: 24rpx;
        }
      }

      .result-status {
        font-size: 20rpx;
      }

      .result-meta {
        .meta-item {
          font-size: 18rpx;
          margin-right: 12rpx;
        }
      }
    }
  }
}
</style>
