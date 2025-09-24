<template>
  <view class="format-selector">
    <view class="format-group">
      <text class="format-label">源格式</text>
      <picker
        :range="formatOptions"
        range-key="label"
        :value="fromFormatIndex"
        @change="handleFromFormatChange"
      >
        <view class="picker-display">
          {{ formatOptions[fromFormatIndex].label }}
          <uni-icons type="arrowdown" size="14" color="#666" />
        </view>
      </picker>
    </view>
    
    <view class="convert-arrow">
      <uni-icons type="arrowright" size="24" color="#007aff" />
    </view>
    
    <view class="format-group">
      <text class="format-label">目标格式</text>
      <picker
        :range="formatOptions"
        range-key="label"
        :value="toFormatIndex"
        @change="handleToFormatChange"
      >
        <view class="picker-display">
          {{ formatOptions[toFormatIndex].label }}
          <uni-icons type="arrowdown" size="14" color="#666" />
        </view>
      </picker>
    </view>
  </view>

  <!-- 快速转换按钮 -->
  <view class="quick-conversions">
    <button
      v-for="conversion in quickConversions"
      :key="`${conversion.from}-${conversion.to}`"
      class="quick-btn"
      :class="{ active: isCurrentConversion(conversion) }"
      @click="setConversion(conversion)"
    >
      {{ conversion.label }}
    </button>
  </view>
</template>

<script setup>
const props = defineProps({
  fromFormatIndex: {
    type: Number,
    required: true
  },
  toFormatIndex: {
    type: Number,
    required: true
  },
  formatOptions: {
    type: Array,
    required: true
  },
  quickConversions: {
    type: Array,
    required: true
  }
})

const emit = defineEmits([
  'update:fromFormatIndex',
  'update:toFormatIndex',
  'formatChange'
])

const handleFromFormatChange = (e) => {
  const index = e.detail.value
  emit('update:fromFormatIndex', index)
  emit('formatChange', {
    from: props.formatOptions[index].value,
    to: props.formatOptions[props.toFormatIndex].value
  })
}

const handleToFormatChange = (e) => {
  const index = e.detail.value
  emit('update:toFormatIndex', index)
  emit('formatChange', {
    from: props.formatOptions[props.fromFormatIndex].value,
    to: props.formatOptions[index].value
  })
}

const isCurrentConversion = (conversion) => {
  const fromFormat = props.formatOptions[props.fromFormatIndex].value
  const toFormat = props.formatOptions[props.toFormatIndex].value
  return conversion.from === fromFormat && conversion.to === toFormat
}

const setConversion = (conversion) => {
  const fromIndex = props.formatOptions.findIndex(f => f.value === conversion.from)
  const toIndex = props.formatOptions.findIndex(f => f.value === conversion.to)
  
  if (fromIndex !== -1) {
    emit('update:fromFormatIndex', fromIndex)
  }
  if (toIndex !== -1) {
    emit('update:toFormatIndex', toIndex)
  }
  
  emit('formatChange', {
    from: conversion.from,
    to: conversion.to
  })
}
</script>

<style lang="scss" scoped>
.format-selector {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  border: 2rpx solid #f0f0f0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

  .format-group {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;

    .format-label {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .picker-display {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 24rpx;
      background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
      border: 2rpx solid #e9ecef;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        border-color: #007aff;
        background: linear-gradient(135deg, #f0f8ff 0%, #fff 100%);
        transform: translateY(-1rpx);
        box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.1);
      }
    }
  }

  .convert-arrow {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60rpx;
    height: 60rpx;
    background: linear-gradient(135deg, #007AFF 0%, #0056b3 100%);
    border-radius: 50%;
    margin-top: 40rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.2);
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.1);
      box-shadow: 0 6rpx 16rpx rgba(0, 122, 255, 0.3);
    }
  }
}

.quick-conversions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 32rpx;

  .quick-btn {
    padding: 12rpx 20rpx;
    background: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 24rpx;
    font-size: 24rpx;
    color: #666;
    cursor: pointer;
    transition: all 0.3s ease;
    font-weight: 500;

    &.active {
      background: linear-gradient(135deg, #007AFF 0%, #0056b3 100%);
      border-color: #007AFF;
      color: white;
      transform: translateY(-1rpx);
      box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.2);
    }

    &:hover:not(.active) {
      border-color: #007AFF;
      background: #f0f8ff;
      color: #007AFF;
      transform: translateY(-1rpx);
      box-shadow: 0 2rpx 8rpx rgba(0, 122, 255, 0.1);
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .format-selector {
    flex-direction: column;
    gap: 20rpx;

    .convert-arrow {
      margin-top: 0;
      transform: rotate(90deg);
    }

    .format-group {
      width: 100%;

      .picker-display {
        padding: 16rpx 20rpx;
        font-size: 26rpx;
      }
    }
  }

  .quick-conversions {
    justify-content: center;

    .quick-btn {
      padding: 10rpx 16rpx;
      font-size: 22rpx;
    }
  }
}
</style>
