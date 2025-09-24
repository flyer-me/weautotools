<template>
  <view class="message-container">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        placeholder="搜索聊天记录"
        v-model="searchKeyword"
        @input="handleSearch"
      />
      <view class="search-icon">
        <uni-icons type="search" size="20" color="#999" />
      </view>
    </view>

    <!-- 消息分类Tab -->
    <view class="message-tabs">
      <view
        v-for="(tab, idx) in messageTabs"
        :key="tab.key"
        :class="['tab-item', { active: idx === activeTab }]"
        @click="activeTab = idx"
      >
        {{ tab.name }}
        <view v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</view>
        <view v-if="idx === activeTab" class="tab-underline"></view>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="message-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
      @scrolltolower="handleLoadMore"
    >
      <MessageItem
        v-for="item in filteredMessages"
        :key="item.id"
        :message="item"
        @click="handleMessageClick"
      />

      <!-- 空状态 -->
      <view v-if="filteredMessages.length === 0" class="empty-state">
        <uni-icons type="chat" size="80" color="#ddd" />
        <text class="empty-text">暂无消息</text>
        <text class="empty-desc">暂时没有新消息</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="hasMore && filteredMessages.length > 0" class="load-more">
        <uni-icons type="spinner-cycle" size="20" color="#999" />
        <text class="load-more-text">加载中...</text>
      </view>
    </scroll-view>

    <!-- 自定义 TabBar -->
    <TabBar :current="'pages/message/message'" />
  </view>
</template>

<script setup>
import TabBar from '@/components/TabBar.vue'
import MessageItem from '@/components/MessageItem.vue'
import { ref, computed, onMounted } from 'vue'
import { useMessage } from '@/composables/useMessage'
import { MESSAGE_TYPE, MESSAGE_TYPE_TEXT } from '@/constants'
import { getFinalFeatureState } from '@/config/featurests'

// 使用消息管理Hook
const {
  messages,
  loading,
  refreshing,
  hasMore,
  messagesByType,
  unreadCounts,
  getMessageList,
  markAsRead,
  markAllAsRead,
  refreshMessages
} = useMessage()

// 搜索关键词
const searchKeyword = ref('')

// 消息分类Tab - 动态计算未读数量
const messageTabs = computed(() => {
  const tabs = [
    { key: 'all', name: '全部', count: unreadCounts.value.total },
    { key: MESSAGE_TYPE.SYSTEM, name: MESSAGE_TYPE_TEXT[MESSAGE_TYPE.SYSTEM], count: unreadCounts.value[MESSAGE_TYPE.SYSTEM] },
    { key: MESSAGE_TYPE.SERVICE, name: MESSAGE_TYPE_TEXT[MESSAGE_TYPE.SERVICE], count: unreadCounts.value[MESSAGE_TYPE.SERVICE] }
  ]

  // 如果用户间聊天功能启用，则显示聊天Tab
  if (getFinalFeatureState('USER_CHAT')) {
    tabs.splice(1, 0, { key: MESSAGE_TYPE.CHAT, name: MESSAGE_TYPE_TEXT[MESSAGE_TYPE.CHAT], count: unreadCounts.value[MESSAGE_TYPE.CHAT] })
  }

  return tabs
})

const activeTab = ref(0)

// 过滤消息列表
const filteredMessages = computed(() => {
  const currentTab = messageTabs.value[activeTab.value]
  let filtered = currentTab.key === 'all'
    ? messagesByType.value.all
    : messagesByType.value[currentTab.key] || []

  // 根据功能开关过滤用户间聊天消息
  if (!getFinalFeatureState('USER_CHAT')) {
    filtered = filtered.filter(msg =>
      msg.type !== MESSAGE_TYPE.CHAT ||
      msg.senderName === '系统客服' ||
      msg.senderName.includes('客服')
    )
  }

  // 根据搜索关键词过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(msg =>
      msg.senderName.toLowerCase().includes(keyword) ||
      msg.lastMessage.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

// formatTime 函数已在 MessageItem 组件中实现

// 事件处理
const handleSearch = () => {
  console.log('搜索消息:', searchKeyword.value)
}

const handleRefresh = async () => {
  await refreshMessages()
  uni.showToast({
    title: '刷新成功',
    icon: 'success'
  })
}

const handleLoadMore = () => {
  if (!hasMore.value) return
  console.log('加载更多消息')
  // 在实际项目中，这里应该调用API加载更多消息
}

const handleMessageClick = async (message) => {
  console.log('点击消息:', message)

  // 标记消息为已读
  if (!message.isRead && message.unreadCount > 0) {
    await markAsRead(message.id, message.type)
  }

  // 跳转到聊天详情页面
  uni.navigateTo({
    url: `/pages/chat-detail/chat-detail?id=${message.id}&name=${message.senderName}`
  })
}

// 页面加载时初始化数据
onMounted(async () => {
  await getMessageList()
})

// 更新消息数量
const updateMessageCounts = () => {
  const counts = {
    all: messages.value.reduce((sum, msg) => sum + msg.unread, 0),
    chat: messages.value.filter(msg => msg.type === 'chat').reduce((sum, msg) => sum + msg.unread, 0),
    system: messages.value.filter(msg => msg.type === 'system').reduce((sum, msg) => sum + msg.unread, 0),
    service: messages.value.filter(msg => msg.type === 'service').reduce((sum, msg) => sum + msg.unread, 0)
  }
  
  messageTabs.value.forEach(tab => {
    tab.count = counts[tab.key] || 0
  })
}
</script>

<style lang="scss" scoped>
.message-container {
  background: $uni-bg-color-light;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.search-bar {
  position: relative;
  padding: $uni-spacing-lg $uni-spacing-md $uni-spacing-md $uni-spacing-md;
  background: #fff;
  
  .search-input {
    width: 100%;
    background: $uni-bg-color-grey;
    border-radius: 32rpx;
    padding: 16rpx 48rpx 16rpx 32rpx;
    font-size: 28rpx;
    border: none;
    outline: none;
    color: $uni-text-color;
    
    &::placeholder {
      color: $uni-text-color-placeholder;
    }
  }
  
  .search-icon {
    position: absolute;
    right: 40rpx;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.message-tabs {
  display: flex;
  align-items: center;
  padding: 0 $uni-spacing-md;
  background: #fff;
  border-bottom: 1rpx solid $uni-border-color-light;
  
  .tab-item {
    font-size: 30rpx;
    font-weight: 500;
    color: $uni-text-color-grey;
    margin-right: 48rpx;
    position: relative;
    padding: 24rpx 0;
    display: flex;
    align-items: center;
    gap: 8rpx;
    
    &.active {
      color: $uni-text-color;
      font-weight: bold;
    }
    
    .tab-badge {
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
      transform: scale(0.8);
    }
    
    .tab-underline {
      position: absolute;
      left: 0;
      bottom: 0;
      width: 48rpx;
      height: 6rpx;
      background: $uni-color-secondary;
      border-radius: 3rpx;
    }
  }
}

.message-list {
  height: calc(100vh - 200rpx);
  background: #fff;
  margin-top: 16rpx;
}

// MessageItem 组件的样式已在组件内部定义

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $uni-spacing-md;
  
  .empty-text {
    font-size: 32rpx;
    color: $uni-text-color-grey;
    margin-top: $uni-spacing-md;
    margin-bottom: $uni-spacing-sm;
  }
  
  .empty-desc {
    font-size: 24rpx;
    color: $uni-text-color-placeholder;
  }
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $uni-spacing-lg;
  
  .load-more-text {
    font-size: 24rpx;
    color: $uni-text-color-grey;
    margin-left: $uni-spacing-sm;
  }
}
</style>
