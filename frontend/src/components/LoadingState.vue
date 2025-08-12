<template>
  <view :class="['loading-state', `loading-state--${type}`]">
    <view v-if="type === 'spinner'" class="spinner-container">
      <view class="spinner"></view>
      <text v-if="text" class="loading-text">{{ text }}</text>
    </view>
    
    <view v-else-if="type === 'skeleton'" class="skeleton-container">
      <view v-for="n in count" :key="n" class="skeleton-item">
        <view class="skeleton-avatar"></view>
        <view class="skeleton-content">
          <view class="skeleton-line skeleton-line--title"></view>
          <view class="skeleton-line skeleton-line--subtitle"></view>
          <view class="skeleton-line skeleton-line--price"></view>
        </view>
      </view>
    </view>
    
    <view v-else-if="type === 'dots'" class="dots-container">
      <view class="dot dot1"></view>
      <view class="dot dot2"></view>
      <view class="dot dot3"></view>
      <text v-if="text" class="loading-text">{{ text }}</text>
    </view>
    
    <view v-else class="default-loading">
      <uni-icons type="spinner-cycle" size="32" color="#007aff" />
      <text v-if="text" class="loading-text">{{ text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'LoadingState',
  props: {
    type: {
      type: String,
      default: 'spinner', // spinner | skeleton | dots | default
      validator: (value) => ['spinner', 'skeleton', 'dots', 'default'].includes(value)
    },
    text: {
      type: String,
      default: '加载中...'
    },
    count: {
      type: Number,
      default: 3 // skeleton 模式下的数量
    }
  }
}
</script>

<style lang="scss" scoped>
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60rpx 0;
}

// 旋转加载器
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #007aff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 骨架屏
.loading-state--skeleton {
  padding: 0;
  align-items: stretch;
  flex-direction: column;
}

.skeleton-container {
  padding: 24rpx;
}

.skeleton-item {
  display: flex;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.skeleton-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.skeleton-line {
  height: 32rpx;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  
  &--title {
    width: 80%;
    margin-bottom: 16rpx;
  }
  
  &--subtitle {
    width: 60%;
    height: 24rpx;
    margin-bottom: 16rpx;
  }
  
  &--price {
    width: 40%;
    height: 28rpx;
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

// 点状加载器
.dots-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.dots-container > .dot {
  display: inline-block;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #007aff;
  margin: 0 8rpx;
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

.dot1 {
  animation-delay: -0.32s;
}

.dot2 {
  animation-delay: -0.16s;
}

.dot3 {
  animation-delay: 0s;
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

// 默认加载器
.default-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

// 通用文本样式
.loading-text {
  font-size: 26rpx;
  color: #666;
  text-align: center;
}
</style>
