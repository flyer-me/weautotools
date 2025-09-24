<template>
  <view class="form-input-container">
    <view v-if="label" class="input-label">
      <text>{{ label }}</text>
      <text v-if="required" class="required">*</text>
      <text v-if="optional" class="optional">（可选）</text>
    </view>
    
    <!-- 文本输入 -->
    <input
      v-if="type === 'text' || type === 'number' || type === 'password'"
      v-model="inputValue"
      :type="type"
      :placeholder="placeholder"
      :maxlength="maxlength"
      :disabled="disabled"
      :class="['text-input', { error: hasError }]"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
    />
    
    <!-- 文本域 -->
    <textarea
      v-else-if="type === 'textarea'"
      v-model="inputValue"
      :placeholder="placeholder"
      :maxlength="maxlength"
      :disabled="disabled"
      :auto-height="autoHeight"
      :class="['textarea-input', { large, error: hasError }]"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
    />
    
    <!-- 数字输入（带单位） -->
    <view v-else-if="type === 'number-with-unit'" class="number-unit-input">
      <input
        v-model.number="inputValue"
        type="number"
        :placeholder="placeholder"
        :disabled="disabled"
        :class="['text-input', { error: hasError }]"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      <text v-if="unit" class="unit">{{ unit }}</text>
    </view>
    
    <!-- 提示信息 -->
    <view v-if="showHint" class="input-hint">
      <!-- 字符计数 -->
      <text v-if="showCharCount" class="char-count">
        {{ inputValue?.length || 0 }}{{ maxlength ? `/${maxlength}` : '' }}
      </text>
      
      <!-- 自定义提示 -->
      <text v-if="hint" class="hint-text">{{ hint }}</text>
      
      <!-- 错误信息 -->
      <text v-if="errorMessage" class="error-text">{{ errorMessage }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text',
    validator: (value) => ['text', 'number', 'password', 'textarea', 'number-with-unit'].includes(value)
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  optional: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  maxlength: {
    type: Number,
    default: null
  },
  unit: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  },
  errorMessage: {
    type: String,
    default: ''
  },
  showCharCount: {
    type: Boolean,
    default: false
  },
  autoHeight: {
    type: Boolean,
    default: true
  },
  large: {
    type: Boolean,
    default: false
  },
  rules: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'input', 'blur', 'focus', 'validate'])

const isFocused = ref(false)
const validationError = ref('')

const inputValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const hasError = computed(() => {
  return !!(props.errorMessage || validationError.value)
})

const showHint = computed(() => {
  return props.showCharCount || props.hint || hasError.value
})

const handleInput = (e) => {
  const value = e.detail.value
  emit('input', value)
  validateInput(value)
}

const handleBlur = (e) => {
  isFocused.value = false
  emit('blur', e.detail.value)
  validateInput(e.detail.value)
}

const handleFocus = (e) => {
  isFocused.value = true
  emit('focus', e.detail.value)
}

const validateInput = (value) => {
  validationError.value = ''
  
  // 必填验证
  if (props.required && (!value || value.toString().trim() === '')) {
    validationError.value = '此字段为必填项'
    emit('validate', { valid: false, error: validationError.value })
    return false
  }
  
  // 自定义规则验证
  for (const rule of props.rules) {
    if (typeof rule === 'function') {
      const result = rule(value)
      if (result !== true) {
        validationError.value = result || '输入格式不正确'
        emit('validate', { valid: false, error: validationError.value })
        return false
      }
    } else if (rule.pattern && !rule.pattern.test(value)) {
      validationError.value = rule.message || '输入格式不正确'
      emit('validate', { valid: false, error: validationError.value })
      return false
    }
  }
  
  emit('validate', { valid: true, error: '' })
  return true
}

// 暴露验证方法
defineExpose({
  validate: () => validateInput(inputValue.value),
  focus: () => {
    // 这里可以添加聚焦逻辑
  },
  blur: () => {
    // 这里可以添加失焦逻辑
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.form-input-container {
  margin-bottom: $spacing-md;
  
  .input-label {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;
    
    .required {
      color: $danger-color;
      margin-left: 5rpx;
    }
    
    .optional {
      color: $text-muted;
      font-size: $font-sm;
      margin-left: 10rpx;
      font-weight: normal;
    }
  }
  
  .text-input, .textarea-input {
    width: 100%;
    padding: $spacing-md;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    font-size: $font-md;
    line-height: 1.5;
    background: $bg-primary;
    transition: border-color 0.3s ease, box-shadow 0.3s ease;
    
    &:focus {
      border-color: $primary-color;
      box-shadow: 0 0 0 4rpx rgba(0, 122, 255, 0.1);
    }
    
    &.error {
      border-color: $danger-color;
      
      &:focus {
        box-shadow: 0 0 0 4rpx rgba(220, 53, 69, 0.1);
      }
    }
    
    &:disabled {
      background: $bg-light;
      color: $text-muted;
      cursor: not-allowed;
    }
  }
  
  .text-input {
    min-height: 80rpx;
  }
  
  .textarea-input {
    min-height: 120rpx;
    resize: vertical;
    
    &.large {
      min-height: 200rpx;
    }
  }
  
  .number-unit-input {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    
    .text-input {
      flex: 1;
    }
    
    .unit {
      font-size: $font-sm;
      color: $text-secondary;
      font-weight: 500;
      white-space: nowrap;
    }
  }
  
  .input-hint {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: $spacing-xs;
    
    .char-count {
      font-size: $font-sm;
      color: $text-muted;
    }
    
    .hint-text {
      font-size: $font-sm;
      color: $text-secondary;
    }
    
    .error-text {
      font-size: $font-sm;
      color: $danger-color;
    }
  }
}
</style>
