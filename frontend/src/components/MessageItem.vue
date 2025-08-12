<template>
  <view class="message-item" @click="handleClick">
    <view class="message-avatar">
      <image :src="message.avatar" class="avatar-img" mode="aspectFill" />
      <view v-if="message.unread > 0" class="unread-badge">
        {{ message.unread > 99 ? '99+' : message.unread }}
      </view>
    </view>
    <view class="message-content">
      <view class="message-header">
        <text class="sender-name">{{ message.senderName }}</text>
        <text class="message-time">{{ formatTime(message.time) }}</text>
      </view>
      <view class="message-preview">
        <text class="preview-text" :class="{ unread: message.unread > 0 }">
          {{ message.lastMessage }}
        </text>
        <view v-if="message.type === 'system'" class="message-type-tag">系统</view>
        <view v-else-if="message.type === 'service'" class="message-type-tag service">客服</view>
      </view>
    </view>
    <view v-if="message.unread > 0" class="unread-dot"></view>
  </view>
</template>

<script>
export default {
  name: 'MessageItem',
  props: {
    message: {
      type: Object,
      required: true
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.message)
    },
    formatTime(timestamp) {
      const now = new Date().getTime()
      const diff = now - timestamp
      const minute = 60 * 1000
      const hour = 60 * minute
      const day = 24 * hour

      if (diff < minute) {
        return '刚刚'
      } else if (diff < hour) {
        return `${Math.floor(diff / minute)}分钟前`
      } else if (diff < day) {
        return `${Math.floor(diff / hour)}小时前`
      } else if (diff < 7 * day) {
        return `${Math.floor(diff / day)}天前`
      } else {
        return new Date(timestamp).toLocaleDateString()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.message-item {
  display: flex;
  align-items: center;
  padding: $uni-spacing-lg $uni-spacing-md;
  border-bottom: 1rpx solid $uni-border-color-light;
  position: relative;
  transition: background-color $uni-animation-duration-fast;
  
  &:active {
    background: $uni-bg-color-hover;
  }
  
  &:last-child {
    border-bottom: none;
  }
}

.message-avatar {
  position: relative;
  margin-right: $uni-spacing-sm;
  
  .avatar-img {
    width: 96rpx;
    height: 96rpx;
    border-radius: $uni-border-radius-circle;
    background: $uni-bg-color-grey;
  }
  
  .unread-badge {
    position: absolute;
    top: -8rpx;
    right: -8rpx;
    background: $uni-color-error;
    color: #fff;
    font-size: 20rpx;
    border-radius: 50%;
    min-width: 32rpx;
    height: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 8rpx;
    border: 2rpx solid #fff;
    transform: scale(0.8);
  }
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
  
  .sender-name {
    font-size: 32rpx;
    color: $uni-text-color;
    font-weight: 500;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .message-time {
    font-size: 24rpx;
    color: $uni-text-color-grey;
    margin-left: $uni-spacing-sm;
    flex-shrink: 0;
  }
}

.message-preview {
  display: flex;
  align-items: center;
  
  .preview-text {
    font-size: 28rpx;
    color: $uni-text-color-grey;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    
    &.unread {
      color: $uni-text-color;
      font-weight: 500;
    }
  }
  
  .message-type-tag {
    background: $uni-color-primary;
    color: #fff;
    font-size: 20rpx;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
    margin-left: $uni-spacing-sm;
    
    &.service {
      background: $uni-color-warning;
    }
  }
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background: $uni-color-error;
  border-radius: 50%;
  margin-left: $uni-spacing-sm;
}
</style>
