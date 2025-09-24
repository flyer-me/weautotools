<template>
  <view class="preset-buttons-container">
    <view v-if="label" class="preset-label">{{ label }}</view>
    <view class="preset-buttons" :class="{ vertical }">
      <view
        v-for="(preset, index) in presets"
        :key="getPresetKey(preset, index)"
        class="preset-btn"
        :class="{ 
          active: isActive(preset, index),
          disabled: preset.disabled 
        }"
        @click="handleClick(preset, index)"
      >
        <uni-icons 
          v-if="preset.icon" 
          :type="preset.icon" 
          size="16" 
          :color="isActive(preset, index) ? '#fff' : '#666'" 
        />
        <text>{{ getPresetLabel(preset) }}</text>
        <text v-if="preset.description" class="preset-desc">{{ preset.description }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  presets: {
    type: Array,
    required: true
  },
  modelValue: {
    type: [String, Number, Object],
    default: null
  },
  label: {
    type: String,
    default: ''
  },
  vertical: {
    type: Boolean,
    default: false
  },
  valueKey: {
    type: String,
    default: 'value'
  },
  labelKey: {
    type: String,
    default: 'label'
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const getPresetKey = (preset, index) => {
  return preset.key || preset[props.valueKey] || index
}

const getPresetLabel = (preset) => {
  return preset[props.labelKey] || preset.label || preset.name || preset
}

const isActive = (preset, index) => {
  if (props.multiple) {
    const values = Array.isArray(props.modelValue) ? props.modelValue : []
    const value = preset[props.valueKey] !== undefined ? preset[props.valueKey] : preset
    return values.includes(value)
  } else {
    const value = preset[props.valueKey] !== undefined ? preset[props.valueKey] : preset
    return props.modelValue === value
  }
}

const handleClick = (preset, index) => {
  if (preset.disabled) return
  
  const value = preset[props.valueKey] !== undefined ? preset[props.valueKey] : preset
  
  if (props.multiple) {
    const values = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    const valueIndex = values.indexOf(value)
    
    if (valueIndex > -1) {
      values.splice(valueIndex, 1)
    } else {
      values.push(value)
    }
    
    emit('update:modelValue', values)
    emit('change', { value: values, preset, index })
  } else {
    emit('update:modelValue', value)
    emit('change', { value, preset, index })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.preset-buttons-container {
  .preset-label {
    display: block;
    font-size: $font-md;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: $spacing-sm;
  }
  
  .preset-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    
    &.vertical {
      flex-direction: column;
      
      .preset-btn {
        width: 100%;
      }
    }
    
    .preset-btn {
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      padding: $spacing-sm $spacing-md;
      background: $bg-secondary;
      border: 2rpx solid $border-light;
      border-radius: $radius-xl;
      font-size: $font-sm;
      color: $text-secondary;
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
      
      &:hover:not(.disabled) {
        border-color: $primary-color;
        background: lighten($primary-color, 45%);
        color: $primary-color;
        transform: translateY(-1rpx);
      }
      
      &:active:not(.disabled) {
        transform: scale(0.95);
      }
      
      &.active {
        background: $primary-color;
        color: white;
        border-color: $primary-color;
        box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.3);
        
        .preset-desc {
          color: rgba(255, 255, 255, 0.8);
        }
      }
      
      &.disabled {
        background: $bg-light;
        color: $text-muted;
        border-color: $border-lighter;
        cursor: not-allowed;
        opacity: 0.6;
        
        &:hover {
          transform: none;
        }
      }
      
      .preset-desc {
        font-size: $font-xs;
        color: $text-muted;
        margin-left: $spacing-xs;
      }
    }
  }
}

// 响应式设计
@media (max-width: 750rpx) {
  .preset-buttons-container {
    .preset-buttons:not(.vertical) {
      .preset-btn {
        flex: 1;
        min-width: 0;
        text-align: center;
        justify-content: center;
        
        .preset-desc {
          display: none;
        }
      }
    }
  }
}
</style>
