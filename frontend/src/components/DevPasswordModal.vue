<template>
  <view v-if="visible" class="modal-overlay" @click="handleOverlayClick">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="modal-title">开发者模式验证</text>
        <view class="close-btn" @click="handleClose">
          <uni-icons type="close" size="20" color="#666" />
        </view>
      </view>
      
      <view class="modal-body">
        <view class="input-group">
          <text class="input-label">请输入开发者密码</text>
          <input
            class="password-input"
            type="password"
            v-model="password"
            placeholder="请输入密码"
            :focus="inputFocus"
            @confirm="handleSubmit"
            maxlength="20"
          />
        </view>
        
        <view v-if="error" class="error-message">
          <uni-icons type="info" size="16" color="#ff4d4f" />
          <text class="error-text">{{ error }}</text>
        </view>
      </view>
      
      <view class="modal-footer">
        <button class="btn-cancel" @click="handleClose">取消</button>
        <button 
          class="btn-confirm" 
          :class="{ loading: loading }"
          @click="handleSubmit"
          :disabled="loading || !password.trim()"
        >
          <uni-icons v-if="loading" type="spinner-cycle" size="16" color="#fff" />
          <text>{{ loading ? '验证中...' : '确认' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { enableDevMode } from '@/config/features'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close', 'success'])

// 响应式数据
const password = ref('')
const loading = ref(false)
const error = ref('')
const inputFocus = ref(false)

// 计算属性已移除以提高安全性

// 监听弹窗显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 弹窗打开时重置状态并聚焦输入框
    password.value = ''
    error.value = ''
    loading.value = false
    setTimeout(() => {
      inputFocus.value = true
    }, 100)
  } else {
    inputFocus.value = false
  }
})

// 处理遮罩层点击
const handleOverlayClick = () => {
  if (!loading.value) {
    handleClose()
  }
}

// 处理关闭
const handleClose = () => {
  if (!loading.value) {
    emit('close')
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!password.value.trim() || loading.value) {
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const result = await enableDevMode(password.value.trim())
    
    if (result.success) {
      uni.showToast({
        title: `验证成功 (${result.source === 'api' ? 'API' : '本地'})`,
        icon: 'success'
      })
      
      emit('success', result)
      emit('close')
    } else {
      error.value = result.message || '密码错误'
      
      // 清空密码输入框
      password.value = ''
      
      // 重新聚焦
      setTimeout(() => {
        inputFocus.value = true
      }, 100)
    }
  } catch (err) {
    console.error('开发者模式验证失败:', err)
    error.value = '验证失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 32rpx;
}

.modal-content {
  background: #fff;
  border-radius: 16rpx;
  width: 100%;
  max-width: 600rpx;
  max-height: 80vh;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50rpx) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .modal-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .close-btn {
    padding: 8rpx;
    cursor: pointer;
  }
}

.modal-body {
  padding: 32rpx;
}

.input-group {
  margin-bottom: 24rpx;
  
  .input-label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 16rpx;
  }
  
  .password-input {
    width: 100%;
    height: 88rpx;
    padding: 0 24rpx;
    border: 2rpx solid #e0e0e0;
    border-radius: 12rpx;
    font-size: 28rpx;
    background: #fff;
    
    &:focus {
      border-color: #007aff;
      outline: none;
    }
  }
}

.hint-text {
  background: #f8f9fa;
  padding: 24rpx;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  
  .hint-line {
    display: block;
    font-size: 24rpx;
    color: #666;
    line-height: 1.5;
    margin-bottom: 8rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .hint-example {
    color: #007aff;
    font-weight: 500;
  }
}

.error-message {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 20rpx;
  background: #fff2f0;
  border: 1rpx solid #ffccc7;
  border-radius: 8rpx;
  
  .error-text {
    font-size: 24rpx;
    color: #ff4d4f;
  }
}

.modal-footer {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx 32rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .btn-cancel,
  .btn-confirm {
    flex: 1;
    height: 80rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
  }
  
  .btn-cancel {
    background: #f5f5f5;
    color: #666;
    
    &:active {
      background: #e8e8e8;
    }
  }
  
  .btn-confirm {
    background: #007aff;
    color: #fff;
    
    &:active {
      background: #0056cc;
    }
    
    &:disabled {
      background: #d9d9d9;
      color: #999;
      cursor: not-allowed;
    }
    
    &.loading {
      background: #0056cc;
    }
  }
}
</style>
