<template>
  <view class="dev-settings">
    <view class="header">
      <text class="title">开发者设置</text>
      <text class="subtitle">仅用于开发测试，生产环境请关闭</text>
    </view>

    <!-- 验证状态 -->
    <view class="section-card">
      <view class="section-title">验证状态</view>
      <view class="auth-status">
        <view class="status-item">
          <text class="status-label">验证状态</text>
          <text :class="['status-value', isAuthenticated ? 'success' : 'error']">
            {{ isAuthenticated ? '已验证' : '未验证' }}
          </text>
        </view>
        <view v-if="isAuthenticated" class="status-item">
          <text class="status-label">验证时间</text>
          <text class="status-value">{{ authTimeText }}</text>
        </view>
        <view v-if="isAuthenticated" class="status-item">
          <text class="status-label">剩余时间</text>
          <text class="status-value">{{ remainingTimeText }}</text>
        </view>
      </view>
      <view class="auth-actions">
        <button
          v-if="!isAuthenticated"
          class="btn-auth"
          @click="showPasswordModal = true"
        >
          重新验证
        </button>
        <button
          v-else
          class="btn-logout"
          @click="handleLogout"
        >
          退出开发模式
        </button>
      </view>
    </view>

    <!-- 开发模式开关 -->
    <view class="section-card" v-if="isAuthenticated">
      <view class="section-title">开发模式</view>
      <view class="setting-item">
        <text class="setting-label">启用开发模式</text>
        <switch
          :checked="devModeEnabled"
          @change="handleDevModeChange"
          color="#007aff"
        />
      </view>
      <view class="setting-desc">
        开启后将禁用限制功能，点击可查看提示信息
      </view>
    </view>

    <!-- 功能开关状态 -->
    <view class="section-card">
      <view class="section-title">功能开关状态</view>
      <view class="feature-list">
        <view 
          v-for="(feature, key) in featureFlags" 
          :key="key"
          class="feature-item"
        >
          <view class="feature-info">
            <text class="feature-name">{{ getFeatureName(key) }}</text>
            <text class="feature-reason">{{ feature.reason }}</text>
          </view>
          <view class="feature-status">
            <text
              :class="['status-text', feature.enabled ? 'enabled' : 'disabled']"
            >
              {{ feature.enabled ? '启用' : '禁用' }}
            </text>
            <text v-if="!feature.enabled" class="dev-mode-hint">
              当前未开放
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 当前状态 -->
    <view class="section-card">
      <view class="section-title">当前生效状态</view>
      <view class="current-status">
        <view 
          v-for="(feature, key) in featureFlags" 
          :key="key"
          class="status-item"
        >
          <text class="status-name">{{ getFeatureName(key) }}</text>
          <text 
            :class="['status-value', getFinalFeatureState(key) ? 'active' : 'inactive']"
          >
            {{ getFinalFeatureState(key) ? '可用' : '不可用' }}
          </text>
        </view>
      </view>
    </view>

    <!-- 重置按钮 -->
    <view class="actions" v-if="isAuthenticated">
      <button class="btn-reset" @click="handleReset">重置为生产模式</button>
    </view>

    <!-- 开发者密码验证弹窗 -->
    <DevPasswordModal
      :visible="showPasswordModal"
      @close="showPasswordModal = false"
      @success="handleAuthSuccess"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import DevPasswordModal from '@/components/DevPasswordModal.vue'
import {
  FEATURE_FLAGS,
  DEV_MODE,
  getFinalFeatureState,
  isDevModeValid,
  disableDevMode
} from '@/config/features'

// 开发模式状态
const devModeEnabled = ref(DEV_MODE.enabled)
const showPasswordModal = ref(false)
const currentTime = ref(Date.now())

// 功能开关
const featureFlags = computed(() => FEATURE_FLAGS)

// 验证状态
const isAuthenticated = computed(() => {
  return DEV_MODE.auth.authenticated && isDevModeValid()
})

// 验证时间文本
const authTimeText = computed(() => {
  if (!DEV_MODE.auth.authTime) return ''
  return new Date(DEV_MODE.auth.authTime).toLocaleString()
})

// 剩余时间文本
const remainingTimeText = computed(() => {
  if (!DEV_MODE.auth.authTime) return ''

  const elapsed = currentTime.value - DEV_MODE.auth.authTime
  const remaining = DEV_MODE.auth.authExpiry - elapsed

  if (remaining <= 0) return '已过期'

  const hours = Math.floor(remaining / (1000 * 60 * 60))
  const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))

  return `${hours}小时${minutes}分钟`
})

// 功能名称映射
const featureNameMap = {
  PAYMENT: '支付功能',
  TRADING: '交易功能',
  USER_CHAT: '用户间聊天',
  SYSTEM_SERVICE: '系统客服',
  GOODS_DISPLAY: '产品展示',
  ORDER_VIEW: '订单查看',
  ORDER_ACTIONS: '订单操作'
}

// 获取功能名称
const getFeatureName = (key) => {
  return featureNameMap[key] || key
}

// 处理开发模式切换
const handleDevModeChange = (e) => {
  devModeEnabled.value = e.detail.value
  DEV_MODE.enabled = e.detail.value
  
  uni.showToast({
    title: e.detail.value ? '开发模式已启用' : '开发模式已关闭',
    icon: 'success'
  })
}

// 处理验证成功
const handleAuthSuccess = (result) => {
  console.log('开发者模式验证成功:', result)
  devModeEnabled.value = DEV_MODE.enabled
}

// 处理退出开发模式
const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出开发者模式吗？',
    success: (res) => {
      if (res.confirm) {
        disableDevMode()
        devModeEnabled.value = false

        uni.showToast({
          title: '已退出开发者模式',
          icon: 'success'
        })
      }
    }
  })
}

// 重置为生产模式
const handleReset = () => {
  uni.showModal({
    title: '确认重置',
    content: '确定要重置为生产模式吗？这将关闭所有受限功能。',
    success: (res) => {
      if (res.confirm) {
        devModeEnabled.value = false
        DEV_MODE.enabled = false

        uni.showToast({
          title: '已重置为生产模式',
          icon: 'success'
        })
      }
    }
  })
}

// 定时器更新当前时间
let timeInterval = null

onMounted(() => {
  // 每分钟更新一次时间，用于计算剩余时间
  timeInterval = setInterval(() => {
    currentTime.value = Date.now()
  }, 60000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style lang="scss" scoped>
.dev-settings {
  background: #f5f5f5;
  min-height: 100vh;
  padding: 24rpx;
}

.header {
  text-align: center;
  margin-bottom: 32rpx;
  
  .title {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 8rpx;
  }
  
  .subtitle {
    font-size: 24rpx;
    color: #999;
  }
}

.section-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
  
  .setting-label {
    font-size: 28rpx;
    color: #333;
  }
}

.setting-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}

.feature-list, .current-status {
  .feature-item, .status-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
}

.feature-info {
  flex: 1;
  
  .feature-name, .status-name {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 4rpx;
  }
  
  .feature-reason {
    font-size: 24rpx;
    color: #666;
  }
}

.feature-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;

  .status-text {
    font-size: 24rpx;
    padding: 4rpx 12rpx;
    border-radius: 12rpx;

    &.enabled {
      background: #f6ffed;
      color: #52c41a;
    }

    &.disabled {
      background: #fff2f0;
      color: #ff4d4f;
    }
  }

  .dev-mode-hint {
    font-size: 20rpx;
    color: #999;
    margin-top: 4rpx;
  }
}

.status-value {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  
  &.active {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  &.inactive {
    background: #f5f5f5;
    color: #999;
  }
}

.auth-status {
  .status-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .status-label {
      font-size: 28rpx;
      color: #333;
    }

    .status-value {
      font-size: 28rpx;

      &.success {
        color: #52c41a;
        font-weight: 500;
      }

      &.error {
        color: #ff4d4f;
        font-weight: 500;
      }
    }
  }
}

.auth-actions {
  margin-top: 24rpx;

  .btn-auth,
  .btn-logout {
    width: 100%;
    height: 80rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;
    cursor: pointer;
  }

  .btn-auth {
    background: #007aff;
    color: #fff;

    &:active {
      background: #0056cc;
    }
  }

  .btn-logout {
    background: #ff4d4f;
    color: #fff;

    &:active {
      background: #d9363e;
    }
  }
}

.actions {
  text-align: center;
  margin-top: 48rpx;

  .btn-reset {
    background: #ff4d4f;
    color: #fff;
    border: none;
    border-radius: 48rpx;
    padding: 24rpx 48rpx;
    font-size: 28rpx;
  }
}
</style>
