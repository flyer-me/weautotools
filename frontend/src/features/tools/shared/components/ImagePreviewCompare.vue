<template>
  <view class="image-preview-compare" v-if="originalImage">
    <!-- 预览模式切换 -->
    <view class="preview-controls">
      <view class="mode-switcher">
        <view
          class="mode-btn"
          :class="{ active: compareMode === 'side-by-side' }"
          @click="setCompareMode('side-by-side')"
        >
          <uni-icons type="columns" size="16" />
          <text>详细对比</text>
        </view>
      </view>
      
      <view class="preview-actions">
        <view class="action-btn" @click="toggleZoom">
          <uni-icons :type="isZoomed ? 'search-minus' : 'search-plus'" size="16" />
        </view>
        <view class="action-btn" @click="toggleInfo">
          <uni-icons type="info" size="16" />
        </view>
      </view>
    </view>

    <!-- 图片信息面板 -->
    <view v-if="showInfo" class="image-info-panel">
      <view class="info-section">
        <text class="info-title">原始图片</text>
        <view class="info-details">
          <text class="info-item">尺寸: {{ originalInfo.width }}×{{ originalInfo.height }}</text>
          <text class="info-item">大小: {{ formatFileSize(originalInfo.size) }}</text>
          <text class="info-item">格式: {{ originalInfo.type }}</text>
        </view>
      </view>
      
      <view v-if="processedImage" class="info-section">
        <text class="info-title">处理后</text>
        <view class="info-details">
          <text class="info-item">尺寸: {{ processedInfo.width }}×{{ processedInfo.height }}</text>
          <text class="info-item">大小: {{ formatFileSize(processedInfo.size) }}</text>
          <text class="info-item">压缩率: {{ compressionRatio }}%</text>
        </view>
      </view>
    </view>

    <!-- 左右对比模式 -->
    <view v-if="compareMode === 'side-by-side'" class="side-by-side-container">
      <view class="image-container original" @click="openImageDetail('original')">
        <view class="image-label">原始图片</view>
        <image
          :src="originalImage"
          class="preview-image"
          :class="{ zoomed: isZoomed }"
          mode="aspectFit"
          @load="handleOriginalLoad"
        />
      </view>

      <view class="image-container processed" @click="openImageDetail('processed')">
        <view class="image-label">
          {{ processedImage ? '处理后' : '预览中...' }}
        </view>
        <image
          v-if="processedImage"
          :src="processedImage"
          class="preview-image"
          :class="{ zoomed: isZoomed }"
          mode="aspectFit"
          @load="handleProcessedLoad"
        />
        <view v-else class="preview-placeholder">
          <uni-icons type="image" size="48" color="#ccc" />
          <text>等待处理...</text>
        </view>
      </view>
    </view>


    <!-- 加载状态 -->
    <view v-if="isProcessing" class="processing-overlay">
      <view class="processing-content">
        <uni-icons type="spinner-cycle" size="32" />
        <text>正在处理...</text>
      </view>
    </view>

    <!-- 图片详情弹窗 -->
    <view v-if="showImageDetail" class="image-detail-modal" @click="closeImageDetail">
      <view class="modal-content" @click.stop>
        <!-- 关闭按钮 -->
        <view class="modal-close" @click="closeImageDetail">
          <uni-icons type="close" size="24" />
        </view>

        <!-- 图片信息浮动面板 -->
        <view class="floating-info-panel" :class="{ collapsed: !showFloatingInfo }">
          <view class="info-header" @click="toggleFloatingInfo">
            <text class="info-title">{{ detailImageType === 'original' ? '原始图片' : '处理后图片' }}</text>
            <uni-icons :type="showFloatingInfo ? 'up' : 'down'" size="16" />
          </view>

          <view v-if="showFloatingInfo" class="info-content">
            <view class="info-item">
              <text class="label">尺寸</text>
              <text class="value">{{ detailImageInfo.width }}×{{ detailImageInfo.height }}</text>
            </view>
            <view class="info-item">
              <text class="label">大小</text>
              <text class="value">{{ formatFileSize(detailImageInfo.size) }}</text>
            </view>
            <view class="info-item">
              <text class="label">格式</text>
              <text class="value">{{ detailImageInfo.type || 'Unknown' }}</text>
            </view>
            <view v-if="detailImageType === 'processed' && compressionRatio > 0" class="info-item">
              <text class="label">压缩率</text>
              <text class="value compression-highlight">{{ compressionRatio }}%</text>
            </view>
          </view>
        </view>

        <!-- 图片展示区域 -->
        <view class="detail-image-container">
          <image
            :src="detailImageSrc"
            class="detail-image"
            :style="{ transform: `scale(${detailZoom}) translate(${detailTranslateX}px, ${detailTranslateY}px)` }"
            mode="aspectFit"
            @touchstart="handleTouchStart"
            @touchmove="handleTouchMove"
            @touchend="handleTouchEnd"
          />
        </view>

        <!-- 缩放控制 -->
        <view class="zoom-controls">
          <view class="zoom-btn" @click="zoomOut">
            <uni-icons type="minus" size="16" />
          </view>
          <text class="zoom-text">{{ Math.round(detailZoom * 100) }}%</text>
          <view class="zoom-btn" @click="zoomIn">
            <uni-icons type="plus" size="16" />
          </view>
          <view class="zoom-btn" @click="resetZoom">
            <uni-icons type="refresh" size="16" />
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

// Props
const props = defineProps({
  originalImage: {
    type: String,
    default: ''
  },
  processedImage: {
    type: String,
    default: ''
  },
  originalInfo: {
    type: Object,
    default: () => ({})
  },
  processedInfo: {
    type: Object,
    default: () => ({})
  },
  isProcessing: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['modeChange', 'zoom', 'info'])

// 响应式数据
const compareMode = ref('side-by-side') // 'side-by-side'
const isZoomed = ref(false)
const showInfo = ref(false)

// 图片详情弹窗相关
const showImageDetail = ref(false)
const detailImageType = ref('original') // 'original' | 'processed'
const detailZoom = ref(1)
const detailTranslateX = ref(0)
const detailTranslateY = ref(0)
const showFloatingInfo = ref(true) // 控制浮动信息面板的展开/收起

// 触摸相关
const touchStartX = ref(0)
const touchStartY = ref(0)
const lastTouchDistance = ref(0)

// 计算属性
const compressionRatio = computed(() => {
  if (!props.originalInfo.size || !props.processedInfo.size) return 0
  return Math.round((1 - props.processedInfo.size / props.originalInfo.size) * 100)
})

const detailImageSrc = computed(() => {
  return detailImageType.value === 'original' ? props.originalImage : props.processedImage
})

const detailImageInfo = computed(() => {
  return detailImageType.value === 'original' ? props.originalInfo : props.processedInfo
})

// 方法
const setCompareMode = (mode) => {
  compareMode.value = mode
  emit('modeChange', mode)
}

const toggleZoom = () => {
  isZoomed.value = !isZoomed.value
  emit('zoom', isZoomed.value)
}

const toggleInfo = () => {
  showInfo.value = !showInfo.value
  emit('info', showInfo.value)
}


const handleOriginalLoad = (e) => {
  console.log('Original image loaded:', e)
}

const handleProcessedLoad = (e) => {
  console.log('Processed image loaded:', e)
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 图片详情相关方法
const openImageDetail = (type) => {
  if (type === 'processed' && !props.processedImage) return

  detailImageType.value = type
  showImageDetail.value = true
  resetZoom()
}

const closeImageDetail = () => {
  showImageDetail.value = false
  resetZoom()
}

const toggleFloatingInfo = () => {
  showFloatingInfo.value = !showFloatingInfo.value
}

const zoomIn = () => {
  detailZoom.value = Math.min(detailZoom.value * 1.2, 5)
}

const zoomOut = () => {
  detailZoom.value = Math.max(detailZoom.value / 1.2, 0.5)
}

const resetZoom = () => {
  detailZoom.value = 1
  detailTranslateX.value = 0
  detailTranslateY.value = 0
}

// 触摸事件处理
const handleTouchStart = (e) => {
  if (e.touches.length === 1) {
    touchStartX.value = e.touches[0].clientX - detailTranslateX.value
    touchStartY.value = e.touches[0].clientY - detailTranslateY.value
  } else if (e.touches.length === 2) {
    const distance = Math.sqrt(
      Math.pow(e.touches[0].clientX - e.touches[1].clientX, 2) +
      Math.pow(e.touches[0].clientY - e.touches[1].clientY, 2)
    )
    lastTouchDistance.value = distance
  }
}

const handleTouchMove = (e) => {
  e.preventDefault()

  if (e.touches.length === 1 && detailZoom.value > 1) {
    // 单指拖拽
    detailTranslateX.value = e.touches[0].clientX - touchStartX.value
    detailTranslateY.value = e.touches[0].clientY - touchStartY.value
  } else if (e.touches.length === 2) {
    // 双指缩放
    const distance = Math.sqrt(
      Math.pow(e.touches[0].clientX - e.touches[1].clientX, 2) +
      Math.pow(e.touches[0].clientY - e.touches[1].clientY, 2)
    )

    if (lastTouchDistance.value > 0) {
      const scale = distance / lastTouchDistance.value
      detailZoom.value = Math.max(0.5, Math.min(5, detailZoom.value * scale))
    }

    lastTouchDistance.value = distance
  }
}

const handleTouchEnd = () => {
  lastTouchDistance.value = 0
}

// 监听处理状态变化
watch(() => props.isProcessing, (newVal) => {
  if (!newVal && props.processedImage) {
    // 处理完成，可以添加一些动画效果
    nextTick(() => {
      console.log('Processing completed')
    })
  }
})
</script>

<style lang="scss" scoped>
.image-preview-compare {
  width: 100%;
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  // 预览控制区域
  .preview-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    background: #f8f9fa;
    border-bottom: 2rpx solid #e9ecef;

    .mode-switcher {
      display: flex;
      gap: 16rpx;

      .mode-btn {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 16rpx 24rpx;
        background: #fff;
        border: 2rpx solid #ddd;
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #666;
        cursor: pointer;
        transition: all 0.3s ease;

        &.active {
          background: #007AFF;
          color: white;
          border-color: #007AFF;
        }

        &:hover:not(.active) {
          border-color: #007AFF;
          color: #007AFF;
        }
      }
    }

    .preview-actions {
      display: flex;
      gap: 16rpx;

      .action-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 64rpx;
        height: 64rpx;
        background: #fff;
        border: 2rpx solid #ddd;
        border-radius: 8rpx;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: #007AFF;
          color: #007AFF;
        }
      }
    }
  }

  // 图片信息面板
  .image-info-panel {
    padding: 24rpx;
    background: #f8f9fa;
    border-bottom: 2rpx solid #e9ecef;

    .info-section {
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .info-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 16rpx;
      }

      .info-details {
        display: flex;
        flex-wrap: wrap;
        gap: 24rpx;

        .info-item {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }

  // 左右对比模式
  .side-by-side-container {
    display: flex;
    min-height: 400rpx;

    .image-container {
      flex: 1;
      position: relative;
      border-right: 2rpx solid #e9ecef;
      cursor: pointer;
      transition: all 0.3s ease;

      &:last-child {
        border-right: none;
      }

      &:hover {
        background: #f8f9fa;
      }

      .image-label {
        position: absolute;
        top: 16rpx;
        left: 16rpx;
        padding: 8rpx 16rpx;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        font-size: 22rpx;
        border-radius: 4rpx;
        z-index: 2;
      }

      .preview-image {
        width: 100%;
        height: 100%;
        min-height: 400rpx;
        object-fit: contain;
        transition: transform 0.3s ease;

        &.zoomed {
          transform: scale(1.2);
        }
      }

      .preview-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 400rpx;
        color: #ccc;
        font-size: 24rpx;
        gap: 16rpx;
      }
    }
  }


  // 加载状态
  .processing-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;

    .processing-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16rpx;
      font-size: 24rpx;
      color: #666;
    }
  }

  // 图片详情弹窗
  .image-detail-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.8);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;

    .modal-content {
      position: relative;
      width: 90%;
      height: 90%;
      background: #fff;
      border-radius: 12rpx;
      overflow: hidden;
      display: flex;
      flex-direction: column;

      .modal-close {
        position: absolute;
        top: 16rpx;
        right: 16rpx;
        width: 48rpx;
        height: 48rpx;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        z-index: 10;
        color: white;

        &:hover {
          background: rgba(0, 0, 0, 0.7);
        }
      }

      .floating-info-panel {
        position: absolute;
        top: 16rpx;
        left: 16rpx;
        background: rgba(255, 255, 255, 0.95);
        border-radius: 8rpx;
        z-index: 5;
        min-width: 200rpx;
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

        &.collapsed .info-content {
          display: none;
        }

        .info-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 16rpx;
          cursor: pointer;
          border-bottom: 2rpx solid #e9ecef;

          .info-title {
            font-size: 24rpx;
            font-weight: 600;
            color: #333;
          }
        }

        .info-content {
          padding: 16rpx;

          .info-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12rpx;
            font-size: 22rpx;

            &:last-child {
              margin-bottom: 0;
            }

            .label {
              color: #666;
            }

            .value {
              color: #333;
              font-weight: 500;

              &.compression-highlight {
                color: #28a745;
                font-weight: 600;
              }
            }
          }
        }
      }

      .detail-image-container {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        background: #f8f9fa;

        .detail-image {
          max-width: 100%;
          max-height: 100%;
          object-fit: contain;
          transition: transform 0.3s ease;
          cursor: grab;

          &:active {
            cursor: grabbing;
          }
        }
      }

      .zoom-controls {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 16rpx;
        padding: 16rpx;
        background: #f8f9fa;
        border-top: 2rpx solid #e9ecef;

        .zoom-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 48rpx;
          height: 48rpx;
          background: #fff;
          border: 2rpx solid #ddd;
          border-radius: 8rpx;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            border-color: #007AFF;
            color: #007AFF;
          }
        }

        .zoom-text {
          font-size: 24rpx;
          color: #666;
          min-width: 80rpx;
          text-align: center;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 750rpx) {
  .image-preview-compare {
    .preview-controls {
      flex-direction: column;
      gap: 16rpx;
      align-items: stretch;

      .mode-switcher {
        justify-content: center;
      }

      .preview-actions {
        justify-content: center;
      }
    }

    .side-by-side-container {
      flex-direction: column;

      .image-container {
        border-right: none;
        border-bottom: 2rpx solid #e9ecef;

        &:last-child {
          border-bottom: none;
        }
      }
    }

    .image-detail-modal .modal-content {
      width: 95%;
      height: 95%;

      .floating-info-panel {
        position: relative;
        top: 0;
        left: 0;
        margin: 16rpx;
        width: calc(100% - 32rpx);
      }
    }
  }
}
</style>
