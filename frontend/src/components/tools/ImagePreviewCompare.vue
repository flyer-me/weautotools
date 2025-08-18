<template>
  <view class="image-preview-compare" v-if="originalImage">
    <!-- 预览模式切换 -->
    <view class="preview-controls">
      <view class="mode-switcher">
        <view
          class="mode-btn"
          :class="{ active: compareMode === 'slider' }"
          @click="setCompareMode('slider')"
        >
          <uni-icons type="crop" size="16" />
          <text>滑动对比</text>
        </view>
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

    <!-- 滑动对比模式 -->
    <view v-if="compareMode === 'slider'" class="slider-container">
      <view class="slider-wrapper" :class="{ zoomed: isZoomed }">
        <!-- 原始图片 (底层) -->
        <image 
          :src="originalImage" 
          class="slider-image original-layer"
          mode="aspectFit"
        />
        
        <!-- 处理后图片 (顶层，带遮罩) -->
        <view
          v-if="processedImage"
          class="processed-layer"
          :style="{ clipPath: `inset(0 0 0 ${sliderPosition}%)` }"
        >
          <image
            :src="processedImage"
            class="slider-image"
            mode="aspectFit"
          />
        </view>

        <!-- 滑动控制线 -->
        <view
          class="slider-divider"
          :style="{ left: sliderPosition + '%' }"
        >
          <!-- 顶部指示器 -->
          <view class="slider-indicator-top">
            <view class="indicator-arrow"></view>
          </view>
          <!-- 底部指示器 -->
          <view class="slider-indicator-bottom">
            <view class="indicator-arrow"></view>
            <text class="indicator-text">拖动下方滑块</text>
          </view>
        </view>
        
        <!-- 标签 -->
        <view class="slider-labels">
          <text class="label-original">原始</text>
          <text class="label-processed">处理后</text>
        </view>
      </view>
      
      <!-- 滑块控制 -->
      <view class="slider-control">
        <view class="slider-control-label">
          <text class="control-label">拖动滑块对比图片</text>
          <text class="control-value">{{ Math.round(sliderPosition) }}%</text>
        </view>
        <view class="slider-track">
          <slider
            :value="sliderPosition"
            :min="0"
            :max="100"
            activeColor="#007AFF"
            backgroundColor="#E5E5E5"
            block-size="24"
            @change="handleSliderChange"
            @changing="handleSliderChanging"
          />
        </view>
        <view class="slider-labels-bottom">
          <text class="slider-label-left">原始图片</text>
          <text class="slider-label-right">处理后</text>
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
const compareMode = ref('slider') // 'side-by-side' | 'slider'
const isZoomed = ref(false)
const showInfo = ref(false)
const sliderPosition = ref(50) // 滑动对比位置 (0-100)

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

const handleSliderChange = (e) => {
  sliderPosition.value = e.detail.value
}

const handleSliderChanging = (e) => {
  sliderPosition.value = e.detail.value
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
}

.preview-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #f8f9fa;
  border-bottom: 2rpx solid #e9ecef;
}

.mode-switcher {
  display: flex;
  background: #fff;
  border-radius: 8rpx;
  overflow: hidden;
  border: 2rpx solid #e9ecef;
}

.mode-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  font-size: 24rpx;
  color: #666;
  background: #fff;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &.active {
    background: #007AFF;
    color: #fff;
  }
  
  &:not(:last-child) {
    border-right: 2rpx solid #e9ecef;
  }
}

.preview-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  background: #fff;
  border: 2rpx solid #e9ecef;
  border-radius: 8rpx;
  color: #666;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    background: #f8f9fa;
    border-color: #007AFF;
    color: #007AFF;
  }
}

.image-info-panel {
  padding: 20rpx 24rpx;
  background: #f8f9fa;
  border-bottom: 2rpx solid #e9ecef;

  .info-section {
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .info-title {
      display: block;
      font-size: 26rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 12rpx;
    }

    .info-details {
      display: flex;
      flex-wrap: wrap;
      gap: 20rpx;

      .info-item {
        font-size: 24rpx;
        color: #666;
        background: #fff;
        padding: 8rpx 16rpx;
        border-radius: 6rpx;
        border: 1rpx solid #e9ecef;
      }
    }
  }
}

.side-by-side-container {
  display: flex;
  min-height: 400rpx;
  gap: 16rpx; /* 减少图片间距 */
  padding: 16rpx;

  .image-container {
    flex: 1;
    position: relative;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
    border: 2rpx solid #f0f0f0;
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
      border-color: #007AFF;
      transform: translateY(-2rpx);
    }

    .image-label {
      padding: 12rpx 16rpx;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      font-size: 24rpx;
      color: #666;
      text-align: center;
      font-weight: 500;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 1rpx;
        background: linear-gradient(90deg, transparent, #e9ecef, transparent);
      }
    }

    .preview-image {
      flex: 1;
      width: 100%;
      min-height: 300rpx;
      object-fit: contain;
      transition: transform 0.3s ease;
      background: #fafafa;

      &.zoomed {
        transform: scale(1.2);
      }

      &:hover {
        transform: scale(1.02);
      }
    }

    .preview-placeholder {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 16rpx;
      color: #999;
      font-size: 24rpx;
      background: #fafafa;

      .uni-icons {
        opacity: 0.5;
      }
    }

    /* 添加点击提示 */
    &::before {
      content: '点击查看详情';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0, 122, 255, 0.9);
      color: white;
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      font-size: 22rpx;
      opacity: 0;
      transition: opacity 0.3s ease;
      pointer-events: none;
      z-index: 10;
      backdrop-filter: blur(4rpx);
    }

    &:hover::before {
      opacity: 1;
    }
  }
}

.slider-container {
  padding: 24rpx;

  .slider-wrapper {
    position: relative;
    width: 100%;
    min-height: 400rpx;
    border-radius: 8rpx;
    overflow: hidden;
    background: #f8f9fa;
    transition: transform 0.3s ease;

    &.zoomed {
      transform: scale(1.2);
    }

    .slider-image {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }

    .original-layer {
      position: absolute;
      top: 0;
      left: 0;
      z-index: 1;
    }

    .processed-layer {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 2;
      overflow: hidden;

      .slider-image {
        width: 100%;
        height: 100%;
      }
    }

    .slider-divider {
      position: absolute;
      top: 0;
      bottom: 0;
      width: 3rpx;
      background: linear-gradient(180deg,
        rgba(255, 255, 255, 0.8) 0%,
        rgba(255, 255, 255, 0.6) 50%,
        rgba(255, 255, 255, 0.8) 100%);
      z-index: 3;
      cursor: ew-resize;
      box-shadow: 0 0 6rpx rgba(0, 0, 0, 0.2);
      border-left: 1rpx solid rgba(0, 0, 0, 0.1);
      border-right: 1rpx solid rgba(0, 0, 0, 0.1);

      .slider-indicator-top {
        position: absolute;
        top: 20rpx;
        left: 50%;
        transform: translateX(-50%);

        .indicator-arrow {
          width: 0;
          height: 0;
          border-left: 8rpx solid transparent;
          border-right: 8rpx solid transparent;
          border-bottom: 12rpx solid rgba(255, 255, 255, 0.9);
          filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.3));
        }
      }

      .slider-indicator-bottom {
        position: absolute;
        bottom: 20rpx;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;

        .indicator-arrow {
          width: 0;
          height: 0;
          border-left: 8rpx solid transparent;
          border-right: 8rpx solid transparent;
          border-top: 12rpx solid rgba(255, 255, 255, 0.9);
          filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.3));
        }

        .indicator-text {
          font-size: 20rpx;
          color: rgba(255, 255, 255, 0.9);
          background: rgba(0, 0, 0, 0.6);
          padding: 4rpx 8rpx;
          border-radius: 8rpx;
          white-space: nowrap;
          backdrop-filter: blur(4rpx);
          text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.5);
        }
      }
    }

    .slider-labels {
      position: absolute;
      top: 20rpx;
      left: 20rpx;
      right: 20rpx;
      display: flex;
      justify-content: space-between;
      z-index: 4;
      pointer-events: none;

      .label-original,
      .label-processed {
        padding: 8rpx 16rpx;
        background: rgba(0, 0, 0, 0.7);
        color: #fff;
        font-size: 22rpx;
        border-radius: 6rpx;
        backdrop-filter: blur(10rpx);
      }
    }
  }

  .slider-control {
    margin-top: 32rpx;
    padding: 24rpx;
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-radius: 12rpx;
    border: 2rpx solid #dee2e6;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

    .slider-control-label {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .control-label {
        font-size: 26rpx;
        color: #333;
        font-weight: 500;
      }

      .control-value {
        font-size: 24rpx;
        color: #007AFF;
        font-weight: 600;
        background: rgba(0, 122, 255, 0.1);
        padding: 4rpx 12rpx;
        border-radius: 12rpx;
      }
    }

    .slider-track {
      margin: 16rpx 0;
      padding: 8rpx 0;
    }

    .slider-labels-bottom {
      display: flex;
      justify-content: space-between;
      margin-top: 12rpx;

      .slider-label-left,
      .slider-label-right {
        font-size: 22rpx;
        color: #666;
        padding: 6rpx 12rpx;
        background: rgba(255, 255, 255, 0.8);
        border-radius: 8rpx;
        border: 1rpx solid #e9ecef;
      }

      .slider-label-left {
        color: #dc3545;
      }

      .slider-label-right {
        color: #28a745;
      }
    }
  }
}

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
  backdrop-filter: blur(4rpx);

  .processing-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    color: #666;
    font-size: 26rpx;

    .uni-icons {
      animation: spin 1s linear infinite;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 图片详情弹窗样式 */
.image-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8rpx);

  .modal-content {
    width: 95vw;
    height: 90vh;
    position: relative;
    border-radius: 16rpx;
    overflow: hidden;
    background: #000;
    box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.5);
  }

  /* 关闭按钮 */
  .modal-close {
    position: absolute;
    top: 24rpx;
    right: 24rpx;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.7);
    color: #fff;
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(10rpx);

    &:hover {
      background: rgba(0, 0, 0, 0.8);
      transform: scale(1.1);
    }
  }

  /* 浮动信息面板 */
  .floating-info-panel {
    position: absolute;
    top: 24rpx;
    left: 24rpx;
    z-index: 10;
    background: rgba(0, 0, 0, 0.8);
    border-radius: 12rpx;
    backdrop-filter: blur(10rpx);
    transition: all 0.3s ease;
    max-width: 400rpx;

    &.collapsed {
      .info-content {
        max-height: 0;
        padding: 0 20rpx;
        opacity: 0;
      }
    }

    .info-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16rpx 20rpx;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }

      .info-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
      }

      .uni-icons {
        color: #fff;
        transition: transform 0.3s ease;
      }
    }

    .info-content {
      max-height: 300rpx;
      overflow: hidden;
      padding: 0 20rpx 16rpx;
      transition: all 0.3s ease;
      opacity: 1;

      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8rpx 0;
        border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);

        &:last-child {
          border-bottom: none;
        }

        .label {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.8);
        }

        .value {
          font-size: 24rpx;
          color: #fff;
          font-weight: 500;

          &.compression-highlight {
            color: #28a745;
            background: rgba(40, 167, 69, 0.2);
            padding: 4rpx 8rpx;
            border-radius: 8rpx;
          }
        }
      }
    }
  }

  /* 图片展示区域 */
  .detail-image-container {
    width: 100%;
    height: 100%;
    position: relative;
    background: #000;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    .detail-image {
      max-width: 100%;
      max-height: 100%;
      transition: transform 0.3s ease;
      cursor: grab;

      &:active {
        cursor: grabbing;
      }
    }
  }

  /* 缩放控制 */
  .zoom-controls {
    position: absolute;
    bottom: 24rpx;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 16rpx;
    background: rgba(0, 0, 0, 0.8);
    padding: 12rpx 24rpx;
    border-radius: 32rpx;
    backdrop-filter: blur(10rpx);
    z-index: 10;

    .zoom-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48rpx;
      height: 48rpx;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      color: #fff;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.1);
      }
    }

    .zoom-text {
      color: #fff;
      font-size: 24rpx;
      font-weight: 500;
      min-width: 80rpx;
      text-align: center;
    }
  }
}

@media (max-width: 750rpx) {
  .side-by-side-container {
    flex-direction: column;
    gap: 12rpx;
    padding: 12rpx;

    .image-container {
      &::before {
        font-size: 20rpx;
        padding: 6rpx 12rpx;
      }
    }
  }

  .preview-controls {
    flex-direction: column;
    gap: 16rpx;
    align-items: stretch;
  }

  .mode-switcher {
    justify-content: center;
  }

  .preview-actions {
    justify-content: center;
  }

  .image-info-panel {
    .info-details {
      flex-direction: column;
      gap: 12rpx;
    }
  }

  /* 移动端弹窗适配 */
  .image-detail-modal {
    .modal-content {
      width: 100vw;
      height: 100vh;
      border-radius: 0;
    }

    .modal-close {
      top: 16rpx;
      right: 16rpx;
      width: 56rpx;
      height: 56rpx;
    }

    .floating-info-panel {
      top: 16rpx;
      left: 16rpx;
      max-width: calc(100vw - 120rpx);

      .info-header {
        padding: 12rpx 16rpx;

        .info-title {
          font-size: 26rpx;
        }
      }

      .info-content {
        padding: 0 16rpx 12rpx;

        .info-item {
          padding: 6rpx 0;

          .label,
          .value {
            font-size: 22rpx;
          }
        }
      }
    }

    .zoom-controls {
      bottom: 16rpx;
      padding: 10rpx 20rpx;

      .zoom-btn {
        width: 40rpx;
        height: 40rpx;
      }

      .zoom-text {
        font-size: 22rpx;
        min-width: 60rpx;
      }
    }
  }
}
</style>
