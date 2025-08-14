<template>
  <view 
    :class="['category-icon', `category-icon--${size}`, `category-icon--${theme}`]"
    :style="iconWrapperStyle"
    @click="handleClick"
  >
    <uni-icons 
      :type="iconType" 
      :size="iconSize" 
      :color="iconColor"
    />
  </view>
</template>

<script>
import { 
  getCategoryIcon, 
  getToolIcon, 
  getIconColor, 
  getIconSize,
  getIconTheme,
  recordIconUsage 
} from '@/constants/icons'

export default {
  name: 'CategoryIcon',
  props: {
    // 图标名称或类型
    name: {
      type: String,
      required: true
    },
    // 图标类型：category | tool | custom
    type: {
      type: String,
      default: 'tool',
      validator: (value) => ['category', 'tool', 'custom'].includes(value)
    },
    // 图标尺寸
    size: {
      type: String,
      default: 'md',
      validator: (value) => ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].includes(value)
    },
    // 图标颜色
    color: {
      type: String,
      default: 'primary'
    },
    // 主题
    theme: {
      type: String,
      default: 'light',
      validator: (value) => ['light', 'dark'].includes(value)
    },
    // 是否显示背景
    showBackground: {
      type: Boolean,
      default: true
    },
    // 是否可点击
    clickable: {
      type: Boolean,
      default: false
    },
    // 自定义样式
    customStyle: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    // 获取图标类型
    iconType() {
      let icon = this.name
      
      if (this.type === 'category') {
        icon = getCategoryIcon(this.name)
      } else if (this.type === 'tool') {
        icon = getToolIcon(this.name)
      }
      
      // 记录图标使用
      recordIconUsage(icon)
      
      return icon
    },
    
    // 获取图标尺寸
    iconSize() {
      return getIconSize(this.size)
    },
    
    // 获取图标颜色
    iconColor() {
      return getIconColor(this.color)
    },
    
    // 获取主题样式
    themeStyle() {
      return getIconTheme(this.theme)
    },
    
    // 图标容器样式
    iconWrapperStyle() {
      const baseStyle = {
        ...this.customStyle
      }
      
      if (this.showBackground) {
        baseStyle.backgroundColor = this.themeStyle.background
        baseStyle.borderColor = this.themeStyle.border
      }
      
      return baseStyle
    }
  },
  methods: {
    handleClick() {
      if (this.clickable) {
        this.$emit('click', {
          name: this.name,
          type: this.type,
          iconType: this.iconType
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.category-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  
  &--xs {
    width: 48rpx;
    height: 48rpx;
    border-radius: 8rpx;
  }
  
  &--sm {
    width: 64rpx;
    height: 64rpx;
    border-radius: 10rpx;
  }
  
  &--md {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
  }
  
  &--lg {
    width: 96rpx;
    height: 96rpx;
    border-radius: 16rpx;
  }
  
  &--xl {
    width: 128rpx;
    height: 128rpx;
    border-radius: 20rpx;
  }
  
  &--xxl {
    width: 160rpx;
    height: 160rpx;
    border-radius: 24rpx;
  }
  
  // 主题样式
  &--light {
    background-color: #f0f8ff;
    border-color: #e6f4ff;
  }
  
  &--dark {
    background-color: #1c1c1e;
    border-color: #38383a;
  }
  
  // 可点击状态
  &.clickable {
    cursor: pointer;
    
    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.2);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  // 悬停效果
  &:hover {
    &--light {
      background-color: #e6f4ff;
      border-color: #bae0ff;
    }
    
    &--dark {
      background-color: #2c2c2e;
      border-color: #48484a;
    }
  }
}

// 动画效果
@keyframes iconPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.category-icon.pulse {
  animation: iconPulse 1s ease-in-out infinite;
}

// 响应式设计
@media (max-width: 750rpx) {
  .category-icon {
    &--lg {
      width: 80rpx;
      height: 80rpx;
    }
    
    &--xl {
      width: 96rpx;
      height: 96rpx;
    }
    
    &--xxl {
      width: 128rpx;
      height: 128rpx;
    }
  }
}
</style>
