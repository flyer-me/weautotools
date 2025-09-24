<template>
  <view class="resize-settings">
    <!-- 尺寸限制设置 -->
    <view class="resize-limit-section">
      <view class="dimension-limit-container">
        <!-- 尺寸输入 -->
        <view class="dimension-inputs">
          <view class="dimension-input">
            <text class="dimension-label">最大宽度</text>
            <view class="dimension-inline">
              <input
                v-model.number="options.maxWidth"
                type="number"
                class="number-input"
                placeholder="留空表示不限制"
                @input="handleDimensionChange"
              />
              <text class="unit">px</text>
            </view>
          </view>
          <view class="dimension-input">
            <text class="dimension-label">最大高度</text>
            <view class="dimension-inline">
              <input
                v-model.number="options.maxHeight"
                type="number"
                class="number-input"
                placeholder="留空表示不限制"
                @input="handleDimensionChange"
              />
              <text class="unit">px</text>
            </view>
          </view>
        </view>

        <!-- 缩放模式选择 -->
        <view class="setting-item">
          <text class="setting-label">缩放方式</text>
          <picker
            :range="resizeModes"
            range-key="label"
            :value="selectedResizeModeIndex"
            @change="handleResizeModeChange"
          >
            <view class="picker-display">
              {{ resizeModes[selectedResizeModeIndex].label }}
              <uni-icons type="arrowdown" size="14" color="#666" />
            </view>
          </picker>
        </view>

        <!-- 常用尺寸预设 -->
        <view class="dimension-presets">
          <text class="preset-label">常用尺寸</text>
          <view class="preset-buttons">
            <view
              class="preset-btn dimension-preset"
              :class="{ active: !options.maxWidth && !options.maxHeight }"
              @click="handleDimensionPresetClick({ name: '原始尺寸', width: null, height: null })"
            >
              原始尺寸
            </view>
            <view
              v-for="preset in dimensionPresets.slice(0, -1)"
              :key="preset.name"
              class="preset-btn dimension-preset"
              :class="{ active: options.maxWidth === preset.width && options.maxHeight === preset.height }"
              @click="handleDimensionPresetClick(preset)"
            >
              {{ preset.name }}
            </view>
          </view>
        </view>

        <!-- 说明信息 -->
        <view class="dimension-note">
          <uni-icons type="info" size="16" color="#666" />
          <text class="note-text">设置图片的最大尺寸限制。超出限制的图片会根据所选缩放方式进行调整，留空表示该方向不限制</text>
        </view>
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
  selectedResizeModeIndex: {
    type: Number,
    default: 0
  },
  resizeModes: {
    type: Array,
    required: true
  },
  dimensionPresets: {
    type: Array,
    required: true
  }
})

const emit = defineEmits([
  'update:options',
  'update:selectedResizeModeIndex',
  'dimensionChange',
  'modeChange'
])

const handleDimensionChange = () => {
  emit('dimensionChange')
}

const handleResizeModeChange = (e) => {
  emit('update:selectedResizeModeIndex', e.detail.value)
  emit('modeChange', props.resizeModes[e.detail.value].value)
}

const handleDimensionPresetClick = (preset) => {
  const newOptions = {
    ...props.options,
    maxWidth: preset.width,
    maxHeight: preset.height
  }
  emit('update:options', newOptions)
  emit('dimensionChange')
}
</script>

<style lang="scss" scoped>
.resize-settings {
  .resize-limit-section {
    margin-top: 24rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 12rpx;
    border: 2rpx solid #f0f0f0;
  }

  .dimension-inputs {
    display: flex;
    gap: 24rpx;
    margin-bottom: 24rpx;
  }

  .dimension-input {
    display: flex;
    align-items: center;
    gap: 12rpx;
    flex: 1;
  }

  .dimension-label {
    font-size: 26rpx;
    color: #333;
    min-width: 140rpx;
    font-weight: 500;
  }

  .dimension-inline {
    display: flex;
    align-items: center;
    flex: 1;
  }

  .number-input {
    flex: 1;
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
      font-size: 24rpx;
    }
  }

  .unit {
    font-size: 24rpx;
    color: #666;
    margin-left: 12rpx;
    font-weight: 500;
  }

  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;
    padding: 16rpx 0;

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
  }

  .dimension-presets {
    margin-bottom: 24rpx;

    .preset-label {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .preset-buttons {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160rpx, 1fr));
      gap: 12rpx;

      .dimension-preset {
        padding: 16rpx 20rpx;
        background: #f8f9fa;
        border: 2rpx solid #e9ecef;
        border-radius: 12rpx;
        text-align: center;
        font-size: 24rpx;
        cursor: pointer;
        transition: all 0.3s ease;
        font-weight: 500;

        &.active {
          background: #007AFF;
          border-color: #007AFF;
          color: white;
          transform: translateY(-1rpx);
          box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.2);
        }

        &:hover:not(.active) {
          border-color: #007AFF;
          background: #f0f8ff;
          transform: translateY(-1rpx);
          box-shadow: 0 2rpx 8rpx rgba(0, 122, 255, 0.1);
        }
      }
    }
  }

  .dimension-note {
    display: flex;
    align-items: flex-start;
    gap: 12rpx;
    padding: 16rpx;
    background: rgba(0, 122, 255, 0.05);
    border-radius: 12rpx;
    border: 1rpx solid rgba(0, 122, 255, 0.1);

    .note-text {
      font-size: 24rpx;
      color: #666;
      line-height: 1.4;
    }
  }
}

@media (max-width: 750rpx) {
  .resize-settings {
    .dimension-inputs {
      flex-direction: column;
      gap: 16rpx;
    }

    .dimension-input {
      flex-direction: column;
      align-items: flex-start;
      gap: 8rpx;

      .dimension-label {
        min-width: auto;
      }

      .dimension-inline {
        width: 100%;
      }
    }

    .setting-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 15rpx;

      .picker-display {
        width: 100%;
        min-width: auto;
      }
    }

    .preset-buttons {
      grid-template-columns: repeat(2, 1fr);

      .dimension-preset {
        padding: 12rpx 16rpx;
        font-size: 22rpx;
      }
    }
  }
}
</style>
