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
      </view>
      <text class="tab-bar-text" :class="{ active: current === item.pagePath }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'TabBar',
  props: {
    current: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      tabs: [
        {
          pagePath: 'pages/index/index',
          text: '首页',
          icon: 'home-filled'
        },
        {
          pagePath: 'pages/category/category',
          text: '分类',
          icon: 'list'
        },
        {
          pagePath: 'pages/user/user',
          text: '我的',
          icon: 'person'
        }
      ]
    }
  },
  methods: {
    switchTab(pagePath) {
      if (this.current !== pagePath) {
        uni.switchTab({ url: '/' + pagePath })
      }
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
</style>
