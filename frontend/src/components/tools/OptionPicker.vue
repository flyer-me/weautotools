<template>
  <picker
    :range="options"
    :range-key="rangeKey"
    :value="modelValue"
    @change="handleChange"
  >
    <view class="picker-display" :class="{ disabled }">
      <text>{{ displayText }}</text>
      <uni-icons type="arrowdown" size="14" color="#666" />
    </view>
  </picker>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  options: {
    type: Array,
    required: true
  },
  rangeKey: {
    type: String,
    default: 'label'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '请选择'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const displayText = computed(() => {
  if (props.options.length === 0) return props.placeholder
  const option = props.options[props.modelValue]
  if (!option) return props.placeholder
  return props.rangeKey ? option[props.rangeKey] : option
})

const handleChange = (e) => {
  const value = e.detail.value
  emit('update:modelValue', value)
  emit('change', {
    value,
    option: props.options[value]
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.picker-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-md;
  background: $bg-primary;
  border: 2rpx solid $border-light;
  border-radius: $radius-md;
  font-size: $font-md;
  color: $text-primary;
  min-width: 200rpx;
  transition: all 0.3s ease;

  &:hover:not(.disabled) {
    border-color: $primary-color;
    background: $bg-primary;
  }
  
  &.disabled {
    background: $bg-light;
    color: $text-muted;
    cursor: not-allowed;
  }
}
</style>
