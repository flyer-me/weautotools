<template>
  <view class="compression-settings">
    <!-- 压缩模式选择 -->
    <view class="compression-mode-selector">
      <text class="mode-title">压缩方式</text>
      <view class="mode-options">
        <view
          v-for="(mode, index) in compressionModes"
          :key="index"
          class="mode-option"
          :class="{ active: selectedModeIndex === index }"
          @click="handleModeChange(index)"
        >
          <view class="mode-icon">
            <uni-icons :type="mode.icon" size="20" />
          </view>
          <text class="mode-name">{{ mode.label }}</text>
          <text class="mode-desc">{{ mode.description }}</text>
        </view>
      </view>
    </view>

    <!-- 质量优先模式 -->
    <view v-if="options.mode === 'quality'" class="compress-quality-section">
      <!-- 质量预设 -->
      <view class="quality-presets-container">
        <text class="preset-label">质量预设</text>
        <view class="quality-presets">
          <view
            v-for="(preset, index) in qualityPresets"
            :key="index"
            class="preset-btn"
            :class="{ active: selectedQualityIndex === index }"
            @click="handleQualityPresetClick(index)"
          >
            <text class="preset-name">{{ preset.name }}</text>
            <text class="preset-desc">{{ preset.description }}</text>
          </view>
        </view>
      </view>

      <!-- 精确质量调整 -->
      <view class="quality-slider-container">
        <view class="slider-header">
          <text class="slider-label">精确调整</text>
          <text class="slider-value">{{ options.quality }}%</text>
        </view>
        <view class="slider-wrapper">
          <slider
            :value="options.quality"
            :min="10"
            :max="100"
            :step="1"
            activeColor="#007AFF"
            backgroundColor="#E5E5E5"
            block-size="24"
            @change="handleQualitySliderChange"
            @changing="handleQualitySliderChanging"
          />
        </view>
      </view>
    </view>

    <!-- 大小优先模式 -->
    <view v-if="options.mode === 'size'" class="compress-size-section">
      <view class="setting-item">
        <text class="setting-label">目标文件大小</text>
        <view class="target-size-input">
          <input
            v-model.number="options.targetSize"
            type="number"
            class="number-input"
            placeholder="500"
            @input="handleTargetSizeChange"
          />
          <picker
            :range="sizeUnits"
            range-key="label"
            :value="selectedSizeUnitIndex"
            @change="handleSizeUnitChange"
          >
            <view class="unit-picker">
              {{ sizeUnits[selectedSizeUnitIndex].label }}
              <uni-icons type="arrowdown" size="12" color="#666" />
            </view>
          </picker>
        </view>
      </view>

      <!-- 目标大小预设 -->
      <view class="setting-item">
        <text class="setting-label">常用大小</text>
        <view class="size-presets">
          <view class="preset-buttons">
            <view
              v-for="preset in sizePresets"
              :key="preset.value"
              class="preset-btn size-preset"
              @click="handleSizePresetClick(preset)"
            >
              {{ preset.label }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 无损压缩模式 -->
    <view v-if="options.mode === 'lossless'" class="compress-lossless-section">
      <view class="lossless-info">
        <view class="info-icon">
          <uni-icons type="star" size="24" color="#007AFF" />
        </view>
        <view class="info-content">
          <text class="info-title">无损压缩模式</text>
          <text class="info-desc">保持原始图片质量，仅通过优化编码和尺寸调整来减小文件大小</text>
        </view>
      </view>

      <view class="lossless-features">
        <view class="feature-item">
          <uni-icons type="checkmarkempty" size="16" color="#28a745" />
          <text class="feature-text">保持100%图片质量</text>
        </view>
        <view class="feature-item">
          <uni-icons type="checkmarkempty" size="16" color="#28a745" />
          <text class="feature-text">优化文件编码结构</text>
        </view>
        <view class="feature-item">
          <uni-icons type="checkmarkempty" size="16" color="#28a745" />
          <text class="feature-text">智能格式选择</text>
        </view>
        <view class="feature-item">
          <uni-icons type="checkmarkempty" size="16" color="#28a745" />
          <text class="feature-text">支持尺寸限制</text>
        </view>
      </view>

      <view class="lossless-note">
        <uni-icons type="info" size="16" color="#666" />
        <text class="note-text">无损压缩的效果取决于原始图片的格式和内容，通常可以减小5%-30%的文件大小</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, watch } from 'vue'

const props = defineProps({
  options: {
    type: Object,
    required: true
  },
  selectedModeIndex: {
    type: Number,
    default: 0
  },
  selectedQualityIndex: {
    type: Number,
    default: 1
  },
  selectedSizeUnitIndex: {
    type: Number,
    default: 1
  },
  compressionModes: {
    type: Array,
    required: true
  },
  qualityPresets: {
    type: Array,
    required: true
  },
  sizeUnits: {
    type: Array,
    required: true
  },
  sizePresets: {
    type: Array,
    required: true
  }
})

const emit = defineEmits([
  'update:options',
  'update:selectedModeIndex',
  'update:selectedQualityIndex',
  'update:selectedSizeUnitIndex',
  'modeChange',
  'qualityChange',
  'targetSizeChange'
])

const handleModeChange = (index) => {
  emit('update:selectedModeIndex', index)
  const newOptions = { ...props.options, mode: props.compressionModes[index].value }
  emit('update:options', newOptions)
  emit('modeChange', props.compressionModes[index].value)
}

const handleQualityPresetClick = (index) => {
  emit('update:selectedQualityIndex', index)
  const quality = Math.round(props.qualityPresets[index].value * 100)
  const newOptions = { ...props.options, quality }
  emit('update:options', newOptions)
  emit('qualityChange', quality)
}

const handleQualitySliderChange = (e) => {
  const quality = e.detail.value
  const newOptions = { ...props.options, quality }
  emit('update:options', newOptions)
  emit('qualityChange', quality)
  
  // 更新对应的预设索引
  updateQualityPresetIndex(quality)
}

const handleQualitySliderChanging = (e) => {
  const quality = e.detail.value
  const newOptions = { ...props.options, quality }
  emit('update:options', newOptions)
}

const updateQualityPresetIndex = (quality) => {
  const qualityValue = quality / 100
  let closestIndex = 0
  let minDiff = Math.abs(props.qualityPresets[0].value - qualityValue)

  props.qualityPresets.forEach((preset, index) => {
    const diff = Math.abs(preset.value - qualityValue)
    if (diff < minDiff) {
      minDiff = diff
      closestIndex = index
    }
  })

  emit('update:selectedQualityIndex', closestIndex)
}

const handleTargetSizeChange = () => {
  updateTargetSizeBytes()
  emit('targetSizeChange')
}

const handleSizeUnitChange = (e) => {
  emit('update:selectedSizeUnitIndex', e.detail.value)
  updateTargetSizeBytes()
  emit('targetSizeChange')
}

const updateTargetSizeBytes = () => {
  const size = props.options.targetSize || 0
  const unit = props.sizeUnits[props.selectedSizeUnitIndex]
  const targetSizeBytes = size * unit.value
  const newOptions = { ...props.options, targetSizeBytes }
  emit('update:options', newOptions)
}

const handleSizePresetClick = (preset) => {
  const unitIndex = props.sizeUnits.findIndex(unit => unit.label === preset.unit)
  if (unitIndex !== -1) {
    emit('update:selectedSizeUnitIndex', unitIndex)
  }
  
  const newOptions = { ...props.options, targetSize: preset.value }
  emit('update:options', newOptions)
  updateTargetSizeBytes()
  emit('targetSizeChange')
}
</script>

<style lang="scss" scoped>
.compression-settings {
  .compression-mode-selector {
    margin-bottom: 32rpx;

    .mode-title {
      display: block;
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 20rpx;
    }

    .mode-options {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20rpx;
    }

    .mode-option {
      position: relative;
      padding: 28rpx 24rpx;
      background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
      border: 3rpx solid #e9ecef;
      border-radius: 16rpx;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4rpx;
        background: transparent;
        transition: all 0.3s ease;
      }

      &.active {
        background: linear-gradient(135deg, #007AFF 0%, #0056b3 100%);
        border-color: #007AFF;
        color: white;
        transform: translateY(-2rpx);
        box-shadow: 0 8rpx 24rpx rgba(0, 122, 255, 0.25);

        &::before {
          background: linear-gradient(90deg, #00d4ff 0%, #007AFF 100%);
        }

        .mode-icon {
          opacity: 1;
          transform: scale(1.1);
          background: rgba(255, 255, 255, 0.2);
        }
      }

      &:hover:not(.active) {
        border-color: #007AFF;
        background: linear-gradient(135deg, #fff 0%, #f0f8ff 100%);
        transform: translateY(-1rpx);
        box-shadow: 0 6rpx 16rpx rgba(0, 122, 255, 0.1);

        .mode-icon {
          transform: scale(1.05);
        }
      }

      .mode-icon {
        margin-bottom: 12rpx;
        opacity: 0.8;
        transition: all 0.3s ease;
        display: flex;
        justify-content: center;
        align-items: center;
        width: 48rpx;
        height: 48rpx;
        margin: 0 auto 12rpx;
        background: rgba(0, 122, 255, 0.1);
        border-radius: 12rpx;
      }

      .mode-name {
        display: block;
        font-size: 28rpx;
        font-weight: 700;
        margin-bottom: 8rpx;
        letter-spacing: 0.5rpx;
      }

      .mode-desc {
        display: block;
        font-size: 22rpx;
        opacity: 0.8;
        line-height: 1.4;
        font-weight: 400;
      }
    }
  }

  .compress-quality-section,
  .compress-size-section,
  .compress-lossless-section {
    margin-top: 24rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 12rpx;
    border: 2rpx solid #f0f0f0;
  }

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
  }

  .target-size-input {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .number-input {
    padding: 16rpx 20rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #333;
    min-width: 150rpx;
    transition: all 0.3s ease;

    &:focus {
      border-color: #007aff;
      background: #fff;
      box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
    }
  }

  .unit-picker {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 16rpx 20rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #333;
  }

  .quality-presets-container {
    margin-bottom: 24rpx;

    .preset-label {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .quality-presets {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
      gap: 12rpx;

      .preset-btn {
        padding: 16rpx 20rpx;
        background: #f8f9fa;
        border: 2rpx solid #e9ecef;
        border-radius: 12rpx;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;

        &.active {
          background: #007AFF;
          border-color: #007AFF;
          color: white;
        }

        &:hover:not(.active) {
          border-color: #007AFF;
          background: #f0f8ff;
        }

        .preset-name {
          display: block;
          font-size: 26rpx;
          font-weight: 600;
          margin-bottom: 4rpx;
        }

        .preset-desc {
          display: block;
          font-size: 22rpx;
          opacity: 0.8;
        }
      }
    }
  }

  .quality-slider-container {
    .slider-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .slider-label {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }

      .slider-value {
        font-size: 28rpx;
        font-weight: 700;
        color: #007AFF;
      }
    }

    .slider-wrapper {
      padding: 0 20rpx;
    }
  }

  .size-presets {
    .preset-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;

      .size-preset {
        padding: 12rpx 20rpx;
        background: #f8f9fa;
        border: 2rpx solid #e9ecef;
        border-radius: 8rpx;
        font-size: 24rpx;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: #007AFF;
          background: #f0f8ff;
        }
      }
    }
  }

  .lossless-info {
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
    padding: 20rpx;
    background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
    border-radius: 12rpx;
    border: 2rpx solid rgba(0, 122, 255, 0.1);
    margin-bottom: 20rpx;

    .info-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48rpx;
      height: 48rpx;
      background: rgba(0, 122, 255, 0.1);
      border-radius: 12rpx;
      flex-shrink: 0;
    }

    .info-content {
      flex: 1;

      .info-title {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #007AFF;
        margin-bottom: 8rpx;
      }

      .info-desc {
        display: block;
        font-size: 24rpx;
        color: #666;
        line-height: 1.4;
      }
    }
  }

  .lossless-features {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12rpx;
    margin-bottom: 20rpx;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 16rpx;
      background: rgba(40, 167, 69, 0.05);
      border-radius: 8rpx;
      border: 1rpx solid rgba(40, 167, 69, 0.1);

      .feature-text {
        font-size: 22rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }

  .lossless-note {
    display: flex;
    align-items: flex-start;
    gap: 8rpx;
    padding: 12rpx;
    background: rgba(255, 193, 7, 0.05);
    border-radius: 12rpx;
    border: 1rpx solid rgba(255, 193, 7, 0.1);

    .note-text {
      font-size: 22rpx;
      color: #666;
      line-height: 1.4;
    }
  }
}

@media (max-width: 750rpx) {
  .compression-settings {
    .mode-options {
      grid-template-columns: 1fr;
    }

    .quality-presets {
      grid-template-columns: 1fr;
    }

    .lossless-features {
      grid-template-columns: 1fr;
    }

    .setting-item {
      flex-direction: column;
      align-items: flex-start;
      gap: 15rpx;

      .number-input {
        width: 100%;
        min-width: auto;
      }
    }
  }
}
</style>
