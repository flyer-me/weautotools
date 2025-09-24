<template>
  <view class="setting-group" :class="{ vertical: vertical }">
    <view class="setting-label">
      <text>{{ label }}</text>
      <text v-if="required" class="required">*</text>
      <text v-if="optional" class="optional">（可选）</text>
    </view>
    
    <view class="setting-control">
      <slot />
    </view>
    
    <view v-if="hint" class="setting-hint">
      <uni-icons type="info" size="14" color="#666" />
      <text class="hint-text">{{ hint }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  required: {
    type: Boolean,
    default: false
  },
  optional: {
    type: Boolean,
    default: false
  },
  hint: {
    type: String,
    default: ''
  },
  vertical: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.setting-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &.vertical {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-sm;
    
    .setting-control {
      width: 100%;
    }
  }
  
  .setting-label {
    display: flex;
    align-items: center;
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;
    min-width: 140rpx;
    
    .required {
      color: $danger-color;
      margin-left: 5rpx;
    }
    
    .optional {
      color: $text-muted;
      font-size: $font-sm;
      margin-left: 5rpx;
      font-weight: normal;
    }
  }
  
  .setting-control {
    flex: 1;
    max-width: 300rpx;
  }
  
  .setting-hint {
    display: flex;
    align-items: flex-start;
    gap: $spacing-xs;
    margin-top: $spacing-xs;
    padding: $spacing-xs $spacing-sm;
    background: rgba(0, 122, 255, 0.05);
    border-radius: $radius-sm;
    border: 1rpx solid rgba(0, 122, 255, 0.1);
    
    .hint-text {
      font-size: $font-xs;
      color: $text-secondary;
      line-height: 1.4;
    }
  }
}

// 响应式设计
@media (max-width: 750rpx) {
  .setting-group:not(.vertical) {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-sm;

    .setting-control {
      width: 100%;
      max-width: none;
    }
  }
}
</style>
