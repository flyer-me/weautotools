<template>
  <view class="profile-container">
    <!-- 头部信息 -->
    <view class="profile-header" @click="handleProfileClick">
      <image class="avatar" src="/static/avatar.png" />
      <view class="profile-info">
        <view class="profile-name">{{ userInfo.name }} <uni-icons type="medal" size="20" color="#FFD700" /></view>
        <view class="profile-desc">{{ userInfo.desc }}</view>
      </view>
      <uni-icons type="arrowright" size="18" color="#666" />
    </view>

    <!-- 我的订单 - 已隐藏 -->
    <!-- 订单功能已隐藏以符合微信个人开发者要求 -->

    <!-- 其他信息卡片 -->
    <view class="section-card">
      <!-- 收货地址和优惠券已隐藏以符合微信个人开发者要求 -->
      <view class="info-row" @click="handlePointsClick">
        <text>积分</text>
        <view class="info-value">{{ userStats.points }}</view>
      </view>
    </view>

    <view class="section-card">
      <view class="info-row" @click="handleHelpClick">
        <text>帮助中心</text>
        <uni-icons type="arrowright" size="18" />
      </view>
      <view class="info-row" @click="handleServiceClick">
        <text>客服电话</text>
        <uni-icons type="headphones" size="18" />
      </view>
    </view>

    <view class="version" @click="handleVersionClick">当前版本 develop</view>

    <!-- 自定义 TabBar -->
    <TabBar :current="'pages/user/user'" />

    <!-- 开发者密码验证弹窗 -->
    <DevPasswordModal
      :visible="showPasswordModal"
      @close="showPasswordModal = false"
      @success="handleDevModeSuccess"
    />
  </view>
</template>

<script setup>
import TabBar from '@/components/TabBar.vue'
import DevPasswordModal from '@/components/DevPasswordModal.vue'
import { navigate } from '@/utils/router.js'
import { ref, onMounted } from 'vue'
import { initDevModeFromStorage } from '@/config/features'

// 用户信息
const userInfo = ref({
  name: 'TDesign',
  desc: '自动化专家'
})

// 用户统计数据
const userStats = ref({
  points: 2580
})

// 事件处理函数
const handleProfileClick = () => {
  navigate.toProfile()
}

const handlePointsClick = () => {
  navigate.toPoints()
}

const handleHelpClick = () => {
  navigate.toHelp()
}

const handleServiceClick = () => {
  uni.showModal({
    title: '客服电话',
    content: '功能受限',
    showCancel: true,
    cancelText: '取消',
    confirmText: '拨打',
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({
          phoneNumber: '-'
        })
      }
    }
  })
}

// 版本点击计数器（用于开发者设置入口）
const versionClickCount = ref(0)
const showPasswordModal = ref(false)

// 处理版本点击
const handleVersionClick = () => {
  versionClickCount.value++

  if (versionClickCount.value >= 5) {
    versionClickCount.value = 0
    showPasswordModal.value = true
  }

  // 3秒后重置计数
  setTimeout(() => {
    versionClickCount.value = 0
  }, 3000)
}

// 处理开发者模式验证成功
const handleDevModeSuccess = (result) => {
  console.log('开发者模式已启用:', result)

  // 跳转到开发者设置页面
  uni.navigateTo({
    url: '/pages/dev-settings/dev-settings'
  })
}

// 页面加载时初始化
onMounted(() => {
  // 初始化开发模式状态
  initDevModeFromStorage()
})

</script>

<style lang="scss" scoped>
.profile-container {
  background: #fafbfc;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.profile-header {
  display: flex;
  align-items: center;
  padding: 48rpx 32rpx 24rpx 32rpx;
  background: linear-gradient(120deg, #ffd6e0 0%, #f9e0ff 100%);
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    opacity: 0.8;
  }

  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: #fff;
    margin-right: 24rpx;
    border: 3rpx solid rgba(255, 255, 255, 0.8);
  }

  .profile-info {
    display: flex;
    flex-direction: column;
    flex: 1;

    .profile-name {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin-bottom: 8rpx;
    }

    .profile-desc {
      font-size: 24rpx;
      color: #666;
      opacity: 0.8;
    }
  }
}
.section-card {
  background: #fff;
  border-radius: 18rpx;
  margin: 24rpx 24rpx 0 24rpx;
  padding: 24rpx 0 0 0;
  box-shadow: 0 2rpx 8rpx #f3f3f3;
}
.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx 16rpx 32rpx;
  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #222;
  }
  .section-link {
    font-size: 24rpx;
    color: #888;
    display: flex;
    align-items: center;
    gap: 4rpx;
  }
}
.order-status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16rpx 24rpx 16rpx;
  .order-status-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    .icon-badge {
      position: relative;
      margin-bottom: 12rpx;
      .badge {
        position: absolute;
        top: 0;
        left: 100%;
        background: #e60012;
        color: #fff;
        font-size: 16rpx;
        border-radius: 50%;
        min-width: 24rpx;
        height: 24rpx;
        padding: 0 4rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2rpx solid #fff;
        transform: scale(0.8);
      }
    }
    .order-status-text {
      font-size: 22rpx;
      color: #666;
      margin-top: 4rpx;
    }
  }
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  font-size: 28rpx;
  color: #222;
  border-bottom: 1rpx solid #f3f3f3;
  .info-value {
    color: #888;
    display: flex;
    align-items: center;
    gap: 4rpx;
  }
  &:last-child {
    border-bottom: none;
  }
}
.version {
  text-align: center;
  color: #bbb;
  font-size: 24rpx;
  margin: 48rpx 0 0 0;
}

.disabled-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx 16rpx;
  gap: 12rpx;

  .disabled-text {
    color: #999;
    font-size: 28rpx;
  }
}

</style>
