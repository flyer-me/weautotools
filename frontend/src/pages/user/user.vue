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

    <!-- 我的订单 -->
    <view class="section-card">
      <view class="section-title-row">
        <text class="section-title">我的订单</text>
        <view class="section-link" @click="handleAllOrdersClick">全部订单 <uni-icons type="arrowright" size="16" /></view>
      </view>
      <view class="order-status-row">
        <view
          class="order-status-item"
          v-for="item in orderStatus"
          :key="item.status"
          @click="handleOrderStatusClick(item)"
        >
          <view class="icon-badge">
            <uni-icons :type="item.icon" size="32" />
            <view v-if="item.count > 0" class="badge">{{ item.count }}</view>
          </view>
          <text class="order-status-text">{{ item.text }}</text>
        </view>
      </view>
    </view>

    <!-- 其他信息卡片 -->
    <view class="section-card">
      <view class="info-row" @click="handleAddressClick">
        <text>收货地址</text>
        <uni-icons type="arrowright" size="18" />
      </view>
      <view class="info-row" @click="handleCouponClick">
        <text>优惠券</text>
        <view class="info-value">{{ userStats.couponCount }} <uni-icons type="arrowright" size="18" /></view>
      </view>
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

    <view class="version">当前版本 develop</view>

    <!-- 自定义 TabBar -->
    <TabBar :current="'pages/user/user'" />
  </view>
</template>

<script setup>
import TabBar from '@/components/TabBar.vue'
import { navigate } from '@/utils/router.js'
import { getUserOrderStatusConfig } from '@/utils/orderStatus.js'
import { ref, computed, onMounted } from 'vue'
import { useOrder } from '@/composables/useOrder'
import { useGlobalBadge } from '@/composables/useBadge'

// 使用订单管理Hook
const { getOrderStatusCounts } = useOrder()

// 使用全局徽章Hook
const { badgeState } = useGlobalBadge()

// 用户信息
const userInfo = ref({
  name: 'TDesign',
  desc: '自动化专家 · VIP会员'
})

// 用户统计数据
const userStats = ref({
  couponCount: 10,
  points: 2580
})

// 订单状态数据 - 动态计算数量
const orderStatus = computed(() => {
  const statusConfig = getUserOrderStatusConfig()
  return statusConfig.map(item => ({
    ...item,
    count: badgeState.order[item.status] || 0
  }))
})

// 事件处理函数
const handleProfileClick = () => {
  navigate.toProfile()
}

const handleAllOrdersClick = () => {
  navigate.toOrderList()
}

const handleOrderStatusClick = (item) => {
  navigate.toOrderList(item.status)
}

const handleAddressClick = () => {
  navigate.toAddress()
}

const handleCouponClick = () => {
  navigate.toCoupon()
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
    content: '-',
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

// 页面加载时初始化订单状态
onMounted(async () => {
  await getOrderStatusCounts()
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

</style>
