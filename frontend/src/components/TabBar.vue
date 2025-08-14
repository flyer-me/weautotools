<template>
  <view class="tab-bar">
    <view
      v-for="item in tabs"
      :key="item.pagePath"
      class="tab-bar-item"
      :class="{ active: current === item.pagePath }"
      @click="switchTab(item.pagePath)"
    >
      <view class="tab-bar-icon" :class="{ active: current === item.pagePath }">
        <uni-icons :type="item.icon" size="24"/>
        <view
          v-if="getBadgeCount(item.pagePath) > 0"
          class="tab-badge"
          :class="{ 'tab-badge--dot': shouldShowDot(item.pagePath) }"
        >
          <text v-if="!shouldShowDot(item.pagePath)" class="badge-text">
            {{ formatBadgeText(getBadgeCount(item.pagePath)) }}
          </text>
        </view>
      </view>
      <text class="tab-bar-text" :class="{ active: current === item.pagePath }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
import { TAB_BAR_CONFIG } from '@/constants'
import { useGlobalBadge } from '@/composables/useBadge'

export default {
  name: 'TabBar',
  props: {
    current: {
      type: String,
      required: true
    }
  },
  setup() {
    const { tabBarBadges } = useGlobalBadge()

    return {
      tabBarBadges
    }
  },
  data() {
    return {
      tabs: TAB_BAR_CONFIG
    }
  },
  methods: {
    switchTab(pagePath) {
      if (this.current !== pagePath) {
        uni.switchTab({ url: '/' + pagePath })
      }
    },
    getBadgeCount(pagePath) {
      if (pagePath === 'pages/user/user') {
        return this.tabBarBadges.user || 0
      }
      return 0
    },
    shouldShowDot(pagePath) {
      const count = this.getBadgeCount(pagePath)
      // 当数量为1时显示小红点，否则显示数字
      return count === 1
    },
    formatBadgeText(count) {
      if (count > 99) {
        return '99+'
      }
      return count.toString()
    }
  }
}
</script>

<style lang="scss" scoped>
.tab-bar {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 100rpx;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  border-top: 1rpx solid #eee;
  z-index: 999;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1);
}
.tab-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0;
  color: #888;
  transition: color 0.3s ease;
}
.tab-bar-item.active {
  color: #007aff;
}
.tab-bar-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
  color: inherit;
}

.tab-bar-icon.active {
  color: #007aff;
}

.tab-bar-text {
  font-size: 22rpx;
  line-height: 1.2;
  color: inherit;
}

.tab-bar-text.active {
  color: #007aff;
}

.tab-badge {
  position: absolute;
  top: -8rpx;
  right: -12rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 18rpx;
  font-weight: bold;
  border-radius: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6rpx;
  border: 2rpx solid #fff;
  box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.4);
  transform: scale(0.9);
  z-index: 10;
  animation: badge-pulse 2s infinite;

  &--dot {
    min-width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    padding: 0;
    top: -6rpx;
    right: -6rpx;
    transform: scale(1);
    animation: dot-pulse 2s infinite;
  }

  .badge-text {
    font-size: 18rpx;
    line-height: 1;
    font-weight: 600;
  }
}

@keyframes badge-pulse {
  0%, 100% {
    transform: scale(0.9);
  }
  50% {
    transform: scale(1);
  }
}

@keyframes dot-pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}
</style>
