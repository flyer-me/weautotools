<template>
  <view class="empty-state">
    <view class="empty-icon">
      <uni-icons 
        :type="iconType" 
        :size="iconSize" 
        :color="iconColor" 
      />
    </view>
    <text class="empty-title">{{ title }}</text>
    <text v-if="description" class="empty-description">{{ description }}</text>
    <button 
      v-if="showButton" 
      class="empty-button" 
      @click="handleButtonClick"
    >
      {{ buttonText }}
    </button>
  </view>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    type: {
      type: String,
      default: 'default', // default | search | cart | order | network | data
      validator: (value) => ['default', 'search', 'cart', 'order', 'network', 'data'].includes(value)
    },
    title: {
      type: String,
      default: ''
    },
    description: {
      type: String,
      default: ''
    },
    showButton: {
      type: Boolean,
      default: false
    },
    buttonText: {
      type: String,
      default: '重试'
    },
    iconSize: {
      type: [String, Number],
      default: 80
    }
  },
  computed: {
    iconType() {
      const iconMap = {
        default: 'info-filled',
        search: 'search',
        cart: 'cart',
        order: 'list',
        network: 'wifi-off',
        data: 'folder-add'
      }
      return iconMap[this.type] || iconMap.default
    },
    iconColor() {
      return '#ccc'
    },
    computedTitle() {
      if (this.title) return this.title
      
      const titleMap = {
        default: '暂无数据',
        search: '没有找到相关内容',
        cart: '购物车是空的',
        order: '暂无订单',
        network: '网络连接失败',
        data: '暂无数据'
      }
      return titleMap[this.type] || titleMap.default
    },
    computedDescription() {
      if (this.description) return this.description
      
      const descMap = {
        default: '暂时没有相关数据',
        search: '试试其他关键词吧',
        cart: '快去挑选心仪的商品吧',
        order: '您还没有任何订单',
        network: '请检查网络连接后重试',
        data: '暂时没有相关数据'
      }
      return descMap[this.type] || descMap.default
    }
  },
  methods: {
    handleButtonClick() {
      this.$emit('button-click')
    }
  }
}
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
  text-align: center;
}

.empty-icon {
  margin-bottom: 32rpx;
  opacity: 0.6;
}

.empty-title {
  font-size: 32rpx;
  color: #666;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.empty-description {
  font-size: 26rpx;
  color: #999;
  line-height: 1.5;
  margin-bottom: 48rpx;
}

.empty-button {
  background: #007aff;
  color: #fff;
  border: none;
  border-radius: 48rpx;
  padding: 24rpx 48rpx;
  font-size: 28rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: #0056cc;
    transform: translateY(-2rpx);
  }
  
  &:active {
    transform: translateY(0);
  }
}
</style>
