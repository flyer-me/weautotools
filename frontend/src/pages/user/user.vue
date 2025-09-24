<script setup lang="ts">
import { computed, ref } from 'vue';
import { useUser } from '@/composables/useUser';

const { user, login, logout } = useUser();
const showPasswordModal = ref(false);

const userStats = computed(() => {
  if (user.value && user.value.profile) {
    return {
      points: (user.value.profile as any).points || 0,
    };
  }
  return {
    points: 0,
  };
});


function handlePointsClick() {
  uni.navigateTo({ url: '/pages/points/points' }); // 假设有积分页面
}

function handleMessageClick() {
  uni.switchTab({ url: '/pages/message/message' });
}

function handleHelpClick() {
  uni.navigateTo({ url: '/pages/help/help' }); // 假设有帮助页面
}

function handleServiceClick() {
  uni.makePhoneCall({
    phoneNumber: '0000',
  });
}

let clickCount = 0;
let timer: ReturnType<typeof setTimeout> | null = null;

function handleVersionClick() {
  clickCount++;
  if (timer) {
    clearTimeout(timer);
  }
  timer = setTimeout(() => {
    if (clickCount >= 5) {
      showPasswordModal.value = true;
    }
    clickCount = 0;
  }, 300); // 300ms 内连续点击
}

function handleDevModeSuccess() {
  navigateToDevSettings();
}

function navigateTo(url: string) {
  uni.navigateTo({ url });
}

function navigateToDevSettings() {
  uni.navigateTo({ url: '/pages/dev-settings/dev-settings' });
}
</script>
<template>
  <view class="profile-container">

    <!-- START: 登录/注销逻辑 -->
    <!-- 用户已登录 -->
    <view v-if="user">
      <view class="profile-header">
        <image class="avatar" :src="user.profile.picture || '/static/my-avatar.png'" mode="aspectFill" />
        <view class="profile-info">
          <view class="profile-name">你好, {{ user.profile.name || user.profile.sub }}</view>
        </view>
      </view>
      <view class="section-card">
         <view class="info-row" @click="logout">
            <text style="color: red;">注销</text>
         </view>
      </view>
    </view>

    <!-- 用户未登录 -->
    <view v-else>
      <view class="profile-header" @click="login">
        <image class="avatar" src="/static/avatar.png" />
        <view class="profile-info">
          <view class="profile-name">点击登录</view>
          <view class="profile-desc">登录后体验完整功能</view>
        </view>
        <uni-icons type="arrowright" size="18" color="#666" />
      </view>
    </view>
    <!-- END: 登录/注销逻辑 -->

    <!-- 其他信息卡片 -->
    <view class="section-card" v-if="user">
      <view class="info-row" @click="handlePointsClick">
        <text>积分</text>
        <view class="info-value">{{ userStats.points }}</view>
      </view>
    </view>

    <view class="section-card">
      <view class="info-row" @click="handleMessageClick"  v-if="user">
        <text>消息</text>
        <uni-icons type="chat" size="18" />
      </view>
      <view class="info-row" @click="handleHelpClick">
        <text>帮助</text>
        <uni-icons type="arrowright" size="18" />
      </view>
      <view class="info-row" @click="handleServiceClick">
        <text>客服电话</text>
        <uni-icons type="headphones" size="18" />
      </view>
    </view>

    <view class="version" @click="handleVersionClick">当前版本 develop</view>

    <TabBar :current="'pages/user/user'" />
    <DevPasswordModal :visible="showPasswordModal" @close="showPasswordModal = false" @success="handleDevModeSuccess" />
  </view>
</template>

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

/* 错误状态样式 */
.error-container {
  background: #fff3cd;
  border: 1rpx solid #ffeaa7;
  border-radius: 12rpx;
  margin: 24rpx;
  padding: 32rpx;
  text-align: center;

  .error-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;

    .error-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #856404;
    }

    .error-desc {
      font-size: 26rpx;
      color: #856404;
      opacity: 0.8;
      line-height: 1.4;
    }

    .retry-btn {
      background: linear-gradient(120deg, #ffd6e0 0%, #f9e0ff 100%);
      color: #333;
      border: none;
      border-radius: 24rpx;
      padding: 16rpx 32rpx;
      font-size: 28rpx;
      font-weight: 500;
      margin-top: 16rpx;
      transition: all 0.3s ease;

      &:active {
        opacity: 0.8;
        transform: scale(0.98);
      }
    }
  }
}
</style>
