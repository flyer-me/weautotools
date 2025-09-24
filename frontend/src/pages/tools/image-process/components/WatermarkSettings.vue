<template>
  <view class="watermark-settings">
    <view class="setting-item">
      <text class="setting-label">水印类型</text>
      <picker
        :range="watermarkTypes"
        range-key="label"
        :value="selectedWatermarkTypeIndex"
        @change="handleWatermarkTypeChange"
      >
        <view class="picker-display">
          {{ watermarkTypes[selectedWatermarkTypeIndex].label }}
          <uni-icons type="arrowdown" size="14" color="#666" />
        </view>
      </picker>
    </view>
    
    <!-- 文字水印设置 -->
    <view v-if="options.type === 'text'" class="text-watermark-settings">
      <view class="setting-item">
        <text class="setting-label">水印文字</text>
        <input
          v-model="options.text"
          class="text-input"
          placeholder="请输入水印文字"
          @input="handleTextChange"
        />
      </view>
      
      <view class="setting-item">
        <text class="setting-label">字体大小</text>
        <view class="font-size-input">
          <input
            v-model.number="options.fontSize"
            type="number"
            class="number-input"
            placeholder="24"
            @input="handleFontSizeChange"
          />
          <text class="unit">px</text>
        </view>
      </view>
      
      <view class="setting-item">
        <text class="setting-label">位置</text>
        <picker
          :range="watermarkPositions"
          range-key="label"
          :value="selectedWatermarkPositionIndex"
          @change="handleWatermarkPositionChange"
        >
          <view class="picker-display">
            {{ watermarkPositions[selectedWatermarkPositionIndex].label }}
            <uni-icons type="arrowdown" size="14" color="#666" />
          </view>
        </picker>
      </view>

      <!-- 水印预览 -->
      <view class="watermark-preview">
        <text class="preview-label">预览效果</text>
        <view class="preview-container">
          <view class="preview-box">
            <view 
              class="preview-watermark"
              :class="`position-${options.position}`"
              :style="{ fontSize: options.fontSize + 'rpx' }"
            >
              {{ options.text || '© WeAutoTools' }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 图片水印设置 -->
    <view v-if="options.type === 'image'" class="image-watermark-settings">
      <view class="setting-item">
        <text class="setting-label">水印图片</text>
        <button class="upload-btn" @click="handleImageUpload">
          <uni-icons type="image" size="16" color="#666" />
          <text>选择图片</text>
        </button>
      </view>

      <view class="setting-item">
        <text class="setting-label">位置</text>
        <picker
          :range="watermarkPositions"
          range-key="label"
          :value="selectedWatermarkPositionIndex"
          @change="handleWatermarkPositionChange"
        >
          <view class="picker-display">
            {{ watermarkPositions[selectedWatermarkPositionIndex].label }}
            <uni-icons type="arrowdown" size="14" color="#666" />
          </view>
        </picker>
      </view>

      <view class="setting-note">
        <uni-icons type="info" size="16" color="#666" />
        <text class="note-text">图片水印功能暂未实现，敬请期待</text>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  options: {
    type: Object,
    required: true
  },
  selectedWatermarkTypeIndex: {
    type: Number,
    default: 0
  },
  selectedWatermarkPositionIndex: {
    type: Number,
    default: 3
  },
  watermarkTypes: {
    type: Array,
    required: true
  },
  watermarkPositions: {
    type: Array,
    required: true
  }
})

const emit = defineEmits([
  'update:options',
  'update:selectedWatermarkTypeIndex',
  'update:selectedWatermarkPositionIndex',
  'textChange',
  'fontSizeChange',
  'positionChange',
  'typeChange'
])

const handleWatermarkTypeChange = (e) => {
  const index = e.detail.value
  emit('update:selectedWatermarkTypeIndex', index)
  
  const newOptions = {
    ...props.options,
    type: props.watermarkTypes[index].value
  }
  emit('update:options', newOptions)
  emit('typeChange', props.watermarkTypes[index].value)
}

const handleTextChange = () => {
  emit('textChange')
}

const handleFontSizeChange = () => {
  emit('fontSizeChange')
}

const handleWatermarkPositionChange = (e) => {
  const index = e.detail.value
  emit('update:selectedWatermarkPositionIndex', index)
  
  const newOptions = {
    ...props.options,
    position: props.watermarkPositions[index].value
  }
  emit('update:options', newOptions)
  emit('positionChange', props.watermarkPositions[index].value)
}

const handleImageUpload = () => {
  uni.showToast({
    title: '图片水印功能开发中',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.watermark-settings {
  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;
    padding: 16rpx 0;

    &:last-child {
      margin-bottom: 0;
    }

    .setting-label {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      min-width: 140rpx;
    }

    .picker-display {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16rpx 20rpx;
      background: #f8f9fa;
      border: 2rpx solid #e9ecef;
      border-radius: 12rpx;
      font-size: 26rpx;
      color: #333;
      min-width: 200rpx;
      transition: all 0.3s ease;

      &:hover {
        border-color: #007aff;
        background: #fff;
      }
    }

    .text-input {
      flex: 1;
      max-width: 300rpx;
      padding: 16rpx 20rpx;
      background: #f8f9fa;
      border: 2rpx solid #e9ecef;
      border-radius: 12rpx;
      font-size: 26rpx;
      color: #333;
      transition: all 0.3s ease;

      &:focus {
        border-color: #007aff;
        background: #fff;
        box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
      }

      &::placeholder {
        color: #999;
      }
    }

    .upload-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 20rpx;
      background: #f8f9fa;
      border: 2rpx solid #e9ecef;
      border-radius: 12rpx;
      font-size: 26rpx;
      color: #666;
      transition: all 0.3s ease;

      &:hover {
        border-color: #007aff;
        background: #f0f8ff;
        color: #007aff;
      }
    }
  }

  .font-size-input {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .number-input {
      width: 120rpx;
      padding: 16rpx 20rpx;
      background: #f8f9fa;
      border: 2rpx solid #e9ecef;
      border-radius: 12rpx;
      font-size: 26rpx;
      color: #333;
      text-align: center;
      transition: all 0.3s ease;

      &:focus {
        border-color: #007aff;
        background: #fff;
        box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
      }
    }

    .unit {
      font-size: 24rpx;
      color: #666;
      font-weight: 500;
    }
  }

  .text-watermark-settings {
    margin-top: 20rpx;
    padding: 20rpx;
    background: #fff;
    border-radius: 12rpx;
    border: 2rpx solid #f0f0f0;
  }

  .image-watermark-settings {
    margin-top: 20rpx;
    padding: 20rpx;
    background: #fff;
    border-radius: 12rpx;
    border: 2rpx solid #f0f0f0;
  }

  .watermark-preview {
    margin-top: 24rpx;
    padding-top: 24rpx;
    border-top: 2rpx solid #f0f0f0;

    .preview-label {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .preview-container {
      .preview-box {
        position: relative;
        width: 100%;
        height: 200rpx;
        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
        border: 2rpx dashed #dee2e6;
        border-radius: 12rpx;
        overflow: hidden;

        .preview-watermark {
          position: absolute;
          color: rgba(255, 255, 255, 0.8);
          font-weight: 600;
          text-shadow: 2rpx 2rpx 4rpx rgba(0, 0, 0, 0.5);
          white-space: nowrap;
          pointer-events: none;

          &.position-top-left {
            top: 20rpx;
            left: 20rpx;
          }

          &.position-top-right {
            top: 20rpx;
            right: 20rpx;
          }

          &.position-bottom-left {
            bottom: 20rpx;
            left: 20rpx;
          }

          &.position-bottom-right {
            bottom: 20rpx;
            right: 20rpx;
          }

          &.position-center {
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
          }
        }
      }
    }
  }

  .setting-note {
    display: flex;
    align-items: flex-start;
    gap: 12rpx;
    padding: 16rpx;
    background: rgba(255, 193, 7, 0.05);
    border-radius: 12rpx;
    border: 1rpx solid rgba(255, 193, 7, 0.1);
    margin-top: 20rpx;

    .note-text {
      font-size: 24rpx;
      color: #666;
      line-height: 1.4;
    }
  }
}

@media (max-width: 750rpx) {
  .watermark-settings {
    .setting-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 15rpx;

      .setting-label {
        min-width: auto;
      }

      .picker-display,
      .text-input {
        width: 100%;
        min-width: auto;
        max-width: none;
      }

      .upload-btn {
        width: 100%;
        justify-content: center;
      }
    }

    .font-size-input {
      width: 100%;
      justify-content: flex-start;

      .number-input {
        flex: 1;
        max-width: 200rpx;
      }
    }

    .preview-box {
      height: 160rpx;

      .preview-watermark {
        font-size: 24rpx !important;
      }
    }
  }
}
</style>
