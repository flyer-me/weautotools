<template>
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
            @click="downloadResult(result, index)"
          >
            <uni-icons type="download" size="14" color="white" />
          </button>
        </view>
      </view>
    </view>
    
    <view class="batch-actions">
      <button class="action-btn primary-btn" @click="downloadAll">
        <uni-icons type="download" size="16" color="white" />
        <text>下载全部</text>
      </button>
      <button class="action-btn secondary-btn" @click="clearResults">
        <uni-icons type="clear" size="16" color="#666" />
        <text>清空结果</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  batchResults: {
    type: Array,
    required: true
  }
})

const emit = defineEmits([
  'downloadResult',
  'downloadAll',
  'clearResults'
])

const batchStats = computed(() => {
  const successful = props.batchResults.filter(r => r.success).length
  const failed = props.batchResults.filter(r => !r.success).length
  return { successful, failed }
})

const downloadResult = (result, index) => {
  emit('downloadResult', result, index)
}

const downloadAll = () => {
  emit('downloadAll')
}

const clearResults = () => {
  emit('clearResults')
}
</script>

<style lang="scss" scoped>
.batch-results {
  margin-top: 32rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid #f0f0f0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

  .results-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    padding-bottom: 16rpx;
    border-bottom: 2rpx solid #f0f0f0;

    .results-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .results-stats {
      display: flex;
      gap: 16rpx;

      .stat-item {
        font-size: 24rpx;
        font-weight: 600;
        padding: 6rpx 12rpx;
        border-radius: 12rpx;

        &.success {
          color: #28a745;
          background: rgba(40, 167, 69, 0.1);
        }

        &.error {
          color: #dc3545;
          background: rgba(220, 53, 69, 0.1);
        }
      }
    }
  }

  .results-list {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    margin-bottom: 24rpx;

    .result-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16rpx 20rpx;
      border-radius: 12rpx;
      border: 2rpx solid #f0f0f0;
      transition: all 0.3s ease;

      &.success {
        background: linear-gradient(135deg, rgba(40, 167, 69, 0.05) 0%, rgba(40, 167, 69, 0.02) 100%);
        border-color: rgba(40, 167, 69, 0.2);

        &:hover {
          background: linear-gradient(135deg, rgba(40, 167, 69, 0.08) 0%, rgba(40, 167, 69, 0.04) 100%);
          transform: translateY(-1rpx);
          box-shadow: 0 4rpx 12rpx rgba(40, 167, 69, 0.1);
        }
      }

      &.error {
        background: linear-gradient(135deg, rgba(220, 53, 69, 0.05) 0%, rgba(220, 53, 69, 0.02) 100%);
        border-color: rgba(220, 53, 69, 0.2);

        &:hover {
          background: linear-gradient(135deg, rgba(220, 53, 69, 0.08) 0%, rgba(220, 53, 69, 0.04) 100%);
          transform: translateY(-1rpx);
          box-shadow: 0 4rpx 12rpx rgba(220, 53, 69, 0.1);
        }
      }

      .result-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;

        .result-filename {
          font-size: 26rpx;
          font-weight: 600;
          color: #333;
        }

        .result-status {
          font-size: 22rpx;
          color: #666;
        }
      }

      .result-actions {
        display: flex;
        gap: 8rpx;

        .mini-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 36rpx;
          height: 36rpx;
          border-radius: 8rpx;
          border: none;
          cursor: pointer;
          transition: all 0.3s ease;

          &.download-btn {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);

            &:hover {
              background: linear-gradient(135deg, #218838 0%, #1ea085 100%);
              transform: scale(1.1);
              box-shadow: 0 4rpx 12rpx rgba(40, 167, 69, 0.3);
            }
          }
        }
      }
    }
  }

  .batch-actions {
    display: flex;
    gap: 16rpx;
    justify-content: center;

    .action-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      border-radius: 12rpx;
      font-size: 26rpx;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      border: none;

      &.primary-btn {
        background: linear-gradient(135deg, #007AFF 0%, #0056b3 100%);
        color: white;

        &:hover {
          background: linear-gradient(135deg, #0056b3 0%, #004085 100%);
          transform: translateY(-2rpx);
          box-shadow: 0 6rpx 16rpx rgba(0, 122, 255, 0.3);
        }
      }

      &.secondary-btn {
        background: #f8f9fa;
        color: #666;
        border: 2rpx solid #dee2e6;

        &:hover {
          background: #e9ecef;
          border-color: #adb5bd;
          transform: translateY(-1rpx);
          box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .batch-results {
    padding: 20rpx;

    .results-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12rpx;

      .results-stats {
        width: 100%;
        justify-content: space-between;

        .stat-item {
          font-size: 22rpx;
          padding: 4rpx 10rpx;
        }
      }
    }

    .results-list {
      .result-item {
        padding: 14rpx 16rpx;

        .result-info {
          .result-filename {
            font-size: 24rpx;
          }

          .result-status {
            font-size: 20rpx;
          }
        }

        .result-actions {
          .mini-btn {
            width: 32rpx;
            height: 32rpx;
          }
        }
      }
    }

    .batch-actions {
      flex-direction: column;
      gap: 12rpx;

      .action-btn {
        padding: 14rpx 20rpx;
        font-size: 24rpx;
        justify-content: center;
      }
    }
  }
}
</style>
