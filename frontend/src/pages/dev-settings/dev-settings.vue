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

    <!-- 使用限制配置管理 -->
    <view class="section-card" v-if="isAuthenticated">
      <view class="section-title">使用限制配置管理</view>
      
      <view class="limit-config-controls">
        <button 
          class="btn-refresh" 
          @click="refreshLimitConfigs"
          :disabled="limitLoading"
        >
          {{ limitLoading ? '加载中...' : '刷新配置' }}
        </button>
        <button 
          class="btn-save" 
          @click="saveLimitConfigs"
          :disabled="limitLoading"
        >
          保存配置
        </button>
      </view>
      
      <view class="limit-config-list" v-if="limitConfigs.length > 0">
        <view 
          v-for="(config, index) in limitConfigs" 
          :key="config.id"
          class="config-item"
        >
          <view class="config-info">
            <text class="tool-name">{{ config.toolName }}</text>
            <text class="user-type">{{ getUserTypeText(config.userType) }}</text>
            <text class="limit-type">{{ getLimitTypeText(config.limitType) }}</text>
          </view>
          <view class="config-controls">
            <input 
              v-model.number="config.limitCount" 
              type="number" 
              class="limit-input"
              placeholder="限制次数"
            />
            <switch
              :checked="config.enabled"
              @change="handleConfigEnabledChange(index, $event)"
              color="#007aff"
              style="transform: scale(0.8);"
            />
          </view>
        </view>
      </view>
      
      <view v-else-if="!limitLoading" class="empty-config">
        <text>暂无限制配置数据</text>
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
import { useUsageLimitAdmin } from '@/composables/useUsageLimit'

// 开发模式状态
const devModeEnabled = ref(DEV_MODE.enabled)
const showPasswordModal = ref(false)
const currentTime = ref(Date.now())

// 使用限制配置管理
const { configs: limitConfigs, loading: limitLoading, fetchConfigs, batchUpdateConfigs } = useUsageLimitAdmin()

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

// 获取用户类型文本
const getUserTypeText = (userType) => {
  return userType === 'ANONYMOUS' ? '匿名用户' : '登录用户'
}

// 获取限制类型文本
const getLimitTypeText = (limitType) => {
  const typeMap = {
    'DAILY': '每日',
    'HOURLY': '每小时',
    'TOTAL': '总计'
  }
  return typeMap[limitType] || limitType
}

// 处理配置启用状态变化
const handleConfigEnabledChange = (index, e) => {
  limitConfigs.value[index].enabled = e.detail.value
}

// 刷新限制配置
const refreshLimitConfigs = async () => {
  try {
    await fetchConfigs()
    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('刷新配置失败:', error)
  }
}

// 保存限制配置
const saveLimitConfigs = async () => {
  try {
    const success = await batchUpdateConfigs(limitConfigs.value)
    if (success) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('保存配置失败:', error)
  }
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
  
  // 在开发者模式下初始化限制配置
  if (isAuthenticated.value) {
    fetchConfigs()
  }
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

// 使用限制配置样式
.limit-config-controls {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
  
  .btn-refresh,
  .btn-save {
    flex: 1;
    height: 64rpx;
    border-radius: 8rpx;
    font-size: 26rpx;
    border: none;
    
    &:disabled {
      opacity: 0.6;
    }
  }
  
  .btn-refresh {
    background: #f0f0f0;
    color: #333;
    
    &:active:not(:disabled) {
      background: #e0e0e0;
    }
  }
  
  .btn-save {
    background: #007aff;
    color: #fff;
    
    &:active:not(:disabled) {
      background: #0056cc;
    }
  }
}

.limit-config-list {
  .config-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .config-info {
      flex: 1;
      
      .tool-name {
        display: block;
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 8rpx;
      }
      
      .user-type,
      .limit-type {
        font-size: 24rpx;
        color: #666;
        margin-right: 16rpx;
      }
    }
    
    .config-controls {
      display: flex;
      align-items: center;
      gap: 16rpx;
      
      .limit-input {
        width: 120rpx;
        height: 56rpx;
        padding: 8rpx 12rpx;
        border: 1rpx solid #ddd;
        border-radius: 6rpx;
        font-size: 24rpx;
        text-align: center;
      }
    }
  }
}

.empty-config {
  text-align: center;
  padding: 48rpx 0;
  color: #999;
  font-size: 26rpx;
}
</style>
