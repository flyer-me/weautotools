<template>
  <view class="image-process-options">
    <view class="section-title">处理选项</view>
    
    <!-- 处理类型选择 -->
    <view class="option-group">
      <view class="option-label">处理类型</view>
      <view class="process-types">
        <view
          v-for="type in processTypes"
          :key="type.value"
          class="type-option"
          :class="{ active: selectedTypes.includes(type.value) }"
          @click="handleTypeToggle(type.value)"
        >
          <view class="type-icon">
            <uni-icons
              :type="selectedTypes.includes(type.value) ? 'checkmarkempty' : 'circle'"
              :size="20"
              :color="selectedTypes.includes(type.value) ? '#007AFF' : '#ccc'"
            />
          </view>
          <view class="type-content">
            <text class="type-name">{{ type.name }}</text>
            <text class="type-desc">{{ type.description }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 压缩设置 -->
    <view v-if="selectedTypes.includes('compress')" class="option-group">
      <view class="option-label">压缩设置</view>
      <CompressionSettings
        :options="compressOptions"
        :selectedModeIndex="selectedCompressionModeIndex"
        :selectedQualityIndex="selectedQualityIndex"
        :selectedSizeUnitIndex="selectedSizeUnitIndex"
        :compressionModes="compressionModes"
        :qualityPresets="qualityPresets"
        :sizeUnits="sizeUnits"
        :sizePresets="sizePresets"
        @update:options="$emit('update:compressOptions', $event)"
        @update:selectedModeIndex="$emit('update:selectedCompressionModeIndex', $event)"
        @update:selectedQualityIndex="$emit('update:selectedQualityIndex', $event)"
        @update:selectedSizeUnitIndex="$emit('update:selectedSizeUnitIndex', $event)"
        @modeChange="handleCompressionModeChange"
        @qualityChange="handleQualityChange"
        @targetSizeChange="handleTargetSizeChange"
      />
    </view>

    <!-- 尺寸调整设置 -->
    <view v-if="selectedTypes.includes('resize')" class="option-group">
      <view class="option-label">尺寸调整</view>
      <ResizeSettings
        :options="resizeOptions"
        :selectedResizeModeIndex="selectedResizeModeIndex"
        :resizeModes="resizeModes"
        :dimensionPresets="dimensionPresets"
        @update:options="$emit('update:resizeOptions', $event)"
        @update:selectedResizeModeIndex="$emit('update:selectedResizeModeIndex', $event)"
        @dimensionChange="handleDimensionChange"
        @modeChange="handleResizeModeChange"
      />
    </view>

    <!-- 水印设置 -->
    <view v-if="selectedTypes.includes('watermark')" class="option-group">
      <view class="option-label">水印设置</view>
      <WatermarkSettings
        :options="watermarkOptions"
        :selectedWatermarkTypeIndex="selectedWatermarkTypeIndex"
        :selectedWatermarkPositionIndex="selectedWatermarkPositionIndex"
        :watermarkTypes="watermarkTypes"
        :watermarkPositions="watermarkPositions"
        @update:options="$emit('update:watermarkOptions', $event)"
        @update:selectedWatermarkTypeIndex="$emit('update:selectedWatermarkTypeIndex', $event)"
        @update:selectedWatermarkPositionIndex="$emit('update:selectedWatermarkPositionIndex', $event)"
        @textChange="handleWatermarkTextChange"
        @fontSizeChange="handleWatermarkFontSizeChange"
        @positionChange="handleWatermarkPositionChange"
        @typeChange="handleWatermarkTypeChange"
      />
    </view>
  </view>
</template>

<script setup>
import CompressionSettings from './CompressionSettings.vue'
import ResizeSettings from './ResizeSettings.vue'
import WatermarkSettings from './WatermarkSettings.vue'

const props = defineProps({
  selectedTypes: {
    type: Array,
    required: true
  },
  compressOptions: {
    type: Object,
    required: true
  },
  resizeOptions: {
    type: Object,
    required: true
  },
  watermarkOptions: {
    type: Object,
    required: true
  },
  selectedCompressionModeIndex: {
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
  selectedResizeModeIndex: {
    type: Number,
    default: 0
  },
  selectedWatermarkTypeIndex: {
    type: Number,
    default: 0
  },
  selectedWatermarkPositionIndex: {
    type: Number,
    default: 3
  },
  processTypes: {
    type: Array,
    required: true
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
  },
  resizeModes: {
    type: Array,
    required: true
  },
  dimensionPresets: {
    type: Array,
    required: true
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

// 定义组件可以发出的事件
const emit = defineEmits({
  // v-model 相关事件 (用于双向绑定)
  'update:selectedTypes': (value) => Array.isArray(value),
  'update:compressOptions': (value) => typeof value === 'object',
  'update:resizeOptions': (value) => typeof value === 'object',
  'update:watermarkOptions': (value) => typeof value === 'object',
  'update:selectedCompressionModeIndex': (value) => typeof value === 'number',
  'update:selectedQualityIndex': (value) => typeof value === 'number',
  'update:selectedSizeUnitIndex': (value) => typeof value === 'number',
  'update:selectedResizeModeIndex': (value) => typeof value === 'number',
  'update:selectedWatermarkTypeIndex': (value) => typeof value === 'number',
  'update:selectedWatermarkPositionIndex': (value) => typeof value === 'number',
  
  // 业务逻辑事件 (用于通知父组件状态变化)
  'typeToggle': (type) => typeof type === 'string',
  'compressionModeChange': (mode) => typeof mode === 'string',
  'qualityChange': (quality) => typeof quality === 'number',
  'targetSizeChange': () => true,
  'dimensionChange': () => true,
  'resizeModeChange': (mode) => typeof mode === 'string',
  'watermarkTextChange': () => true,
  'watermarkFontSizeChange': () => true,
  'watermarkPositionChange': (position) => typeof position === 'string',
  'watermarkTypeChange': (type) => typeof type === 'string'
})

const handleTypeToggle = (value) => {
  const index = props.selectedTypes.indexOf(value)
  let newSelectedTypes

  if (index > -1) {
    // 移除选中的类型
    newSelectedTypes = props.selectedTypes.filter(type => type !== value)
  } else {
    // 添加新的类型
    newSelectedTypes = [...props.selectedTypes, value]
  }

  emit('update:selectedTypes', newSelectedTypes)
  emit('typeToggle', value)
}

const handleCompressionModeChange = (mode) => {
  emit('compressionModeChange', mode)
}

const handleQualityChange = (quality) => {
  emit('qualityChange', quality)
}

const handleTargetSizeChange = () => {
  emit('targetSizeChange')
}

const handleDimensionChange = () => {
  emit('dimensionChange')
}

const handleResizeModeChange = (mode) => {
  emit('resizeModeChange', mode)
}

const handleWatermarkTextChange = () => {
  emit('watermarkTextChange')
}

const handleWatermarkFontSizeChange = () => {
  emit('watermarkFontSizeChange')
}

const handleWatermarkPositionChange = (position) => {
  emit('watermarkPositionChange', position)
}

const handleWatermarkTypeChange = (type) => {
  emit('watermarkTypeChange', type)
}
</script>

<style lang="scss" scoped>
.image-process-options {
  margin: 32rpx 0;

  .section-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    padding: 0 8rpx;
  }

  .option-group {
    margin-bottom: 32rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 16rpx;
    border: 2rpx solid #f0f0f0;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

    &:last-child {
      margin-bottom: 0;
    }

    .option-label {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 24rpx;
      padding-bottom: 12rpx;
      border-bottom: 2rpx solid #f0f0f0;
    }
  }

  .process-types {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280rpx, 1fr));
    gap: 16rpx;

    .type-option {
      display: flex;
      align-items: center;
      padding: 24rpx;
      background: white;
      border: 3rpx solid #e9ecef;
      border-radius: 16rpx;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
      position: relative;
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
        border-color: #007aff;
        background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
        box-shadow: 0 6rpx 20rpx rgba(0, 122, 255, 0.2);
        transform: translateY(-2rpx);

        &::before {
          background: linear-gradient(90deg, #007AFF 0%, #00d4ff 100%);
        }

        .type-icon {
          background: rgba(0, 122, 255, 0.1);
          transform: scale(1.1);
        }

        .type-name {
          color: #007AFF;
        }
      }

      &:hover:not(.active) {
        border-color: #007aff;
        background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
        transform: translateY(-1rpx);
        box-shadow: 0 4rpx 16rpx rgba(0, 122, 255, 0.1);

        .type-icon {
          transform: scale(1.05);
        }
      }

      .type-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48rpx;
        height: 48rpx;
        background: rgba(0, 0, 0, 0.05);
        border-radius: 12rpx;
        margin-right: 16rpx;
        transition: all 0.3s ease;
        flex-shrink: 0;
      }

      .type-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4rpx;
      }

      .type-name {
        font-size: 28rpx;
        color: #333;
        font-weight: 600;
        line-height: 1.2;
        transition: color 0.3s ease;
      }

      .type-desc {
        font-size: 22rpx;
        color: #666;
        line-height: 1.3;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .image-process-options {
    padding: 20rpx;

    .process-types {
      grid-template-columns: 1fr;
      gap: 12rpx;

      .type-option {
        padding: 20rpx;

        .type-icon {
          width: 40rpx;
          height: 40rpx;
          margin-right: 12rpx;
        }

        .type-name {
          font-size: 26rpx;
        }

        .type-desc {
          font-size: 20rpx;
        }
      }
    }
  }
}
</style>
