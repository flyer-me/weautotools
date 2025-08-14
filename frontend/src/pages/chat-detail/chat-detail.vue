<template>
  <view class="chat-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-left" @click="handleBack">
        <uni-icons type="arrowleft" size="20" color="#333" />
      </view>
      <view class="navbar-center">
        <text class="navbar-title">{{ chatInfo.name }}</text>
        <text class="navbar-subtitle">{{ chatInfo.status }}</text>
      </view>
      <view class="navbar-right" @click="handleMore">
        <uni-icons type="more-filled" size="20" color="#333" />
      </view>
    </view>

    <!-- 聊天消息列表 -->
    <scroll-view
      class="chat-messages"
      scroll-y
      :scroll-top="scrollTop"
      scroll-with-animation
      @scrolltoupper="handleLoadHistory"
    >
      <!-- 历史消息加载提示 -->
      <view v-if="loadingHistory" class="loading-history">
        <uni-icons type="spinner-cycle" size="16" color="#999" />
        <text class="loading-text">加载历史消息...</text>
      </view>

      <!-- 消息列表 -->
      <view
        v-for="message in messages"
        :key="message.id"
        :class="['message-item', message.isSelf ? 'message-self' : 'message-other']"
      >
        <image
          v-if="!message.isSelf"
          :src="message.avatar"
          class="message-avatar"
          mode="aspectFill"
        />
        <view class="message-bubble">
          <view v-if="message.type === 'text'" class="message-text">{{ message.content }}</view>
          <view v-else-if="message.type === 'image'" class="message-image">
            <image :src="message.content" mode="aspectFit" @click="previewImage(message.content)" />
          </view>
          <view v-else-if="message.type === 'goods'" class="message-goods" @click="handleGoodsClick(message.goods)">
            <image :src="message.goods.img" class="goods-img" mode="aspectFill" />
            <view class="goods-info">
              <text class="goods-title">{{ message.goods.title }}</text>
              <text class="goods-price">￥{{ message.goods.price }}</text>
            </view>
          </view>
          <text class="message-time">{{ formatMessageTime(message.time) }}</text>
        </view>
        <image
          v-if="message.isSelf"
          :src="message.avatar"
          class="message-avatar"
          mode="aspectFill"
        />
      </view>
    </scroll-view>

    <!-- 输入栏 -->
    <view class="chat-input-bar">
      <view class="input-tools">
        <uni-icons type="mic" size="24" color="#666" @click="handleVoiceInput" />
        <uni-icons type="image" size="24" color="#666" @click="handleImagePicker" />
        <uni-icons type="plus" size="24" color="#666" @click="handleMoreTools" />
      </view>
      <input
        class="chat-input"
        v-model="inputText"
        placeholder="说点什么..."
        @confirm="handleSendMessage"
        :focus="inputFocus"
      />
      <view class="send-btn" :class="{ active: inputText.trim() }" @click="handleSendMessage">
        发送
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { navigate } from '@/utils/router.js'
import { getFinalFeatureState, showFeatureDisabledToast } from '@/config/features'

// 页面参数
const chatInfo = ref({
  id: '',
  name: '',
  avatar: '',
  status: '在线'
})

// 消息列表
const messages = ref([])
const inputText = ref('')
const inputFocus = ref(false)
const scrollTop = ref(0)
const loadingHistory = ref(false)

// 模拟聊天数据
const mockMessages = [
  {
    id: 1,
    content: '您好，请问这个Excel自动化脚本支持哪些功能？',
    time: new Date().getTime() - 600000,
    isSelf: true,
    avatar: '/static/my-avatar.png',
    type: 'text'
  },
  {
    id: 2,
    content: '您好！这个脚本支持数据清洗、图表生成、批量处理等功能，非常实用',
    time: new Date().getTime() - 580000,
    isSelf: false,
    avatar: '/static/avatar1.png',
    type: 'text'
  },
  {
    id: 3,
    content: '还有演示视频吗？',
    time: new Date().getTime() - 560000,
    isSelf: true,
    avatar: '/static/my-avatar.png',
    type: 'text'
  },
  {
    id: 4,
    content: '当然有，我发个产品链接给您看看',
    time: new Date().getTime() - 540000,
    isSelf: false,
    avatar: '/static/avatar1.png',
    type: 'text'
  },
  {
    id: 5,
    content: '',
    time: new Date().getTime() - 520000,
    isSelf: false,
    avatar: '/static/avatar1.png',
    type: 'goods',
    goods: {
      id: 1,
      title: 'Excel数据处理自动化脚本',
      price: 299,
      img: '/static/goods1.png'
    }
  }
]

// 页面加载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  
  chatInfo.value = {
    id: options.id || '1',
    name: options.name || '自动化专家',
    avatar: '/static/avatar1.png',
    status: '在线'
  }
  
  // 加载消息
  loadMessages()
})

// 加载消息
const loadMessages = () => {
  messages.value = mockMessages
  scrollToBottom()
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = 99999
  })
}

// 格式化消息时间
const formatMessageTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 事件处理
const handleBack = () => {
  navigate.back()
}

const handleMore = () => {
  uni.showActionSheet({
    itemList: ['查看资料', '清空聊天记录', '举报'],
    success: (res) => {
      console.log('选择了:', res.tapIndex)
    }
  })
}

const handleLoadHistory = () => {
  if (loadingHistory.value) return
  
  loadingHistory.value = true
  setTimeout(() => {
    loadingHistory.value = false
    console.log('加载历史消息')
  }, 1000)
}

const handleSendMessage = () => {
  if (!inputText.value.trim()) return

  // 检查是否为系统客服聊天
  const isSystemService = chatInfo.value.id === 'system' || chatInfo.value.name === '系统客服'

  // 如果不是系统客服且用户间聊天功能被禁用
  if (!isSystemService && !getFinalFeatureState('USER_CHAT')) {
    showFeatureDisabledToast('USER_CHAT')
    return
  }

  const newMessage = {
    id: Date.now(),
    content: inputText.value.trim(),
    time: new Date().getTime(),
    isSelf: true,
    avatar: '/static/my-avatar.png',
    type: 'text'
  }

  messages.value.push(newMessage)
  inputText.value = ''
  scrollToBottom()

  // 模拟对方回复（仅系统客服）
  if (isSystemService) {
    setTimeout(() => {
      const replyMessage = {
        id: Date.now() + 1,
        content: '您好，我是系统客服，有什么可以帮助您的吗？',
        time: new Date().getTime(),
        isSelf: false,
        avatar: chatInfo.value.avatar,
        type: 'text'
      }
      messages.value.push(replyMessage)
      scrollToBottom()
    }, 1000)
  }
}

const handleVoiceInput = () => {
  uni.showToast({
    title: '语音功能开发中',
    icon: 'none'
  })
}

const handleImagePicker = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      const newMessage = {
        id: Date.now(),
        content: tempFilePath,
        time: new Date().getTime(),
        isSelf: true,
        avatar: '/static/my-avatar.png',
        type: 'image'
      }
      messages.value.push(newMessage)
      scrollToBottom()
    }
  })
}

const handleMoreTools = () => {
  uni.showActionSheet({
    itemList: ['产品', '位置', '文件'],
    success: (res) => {
      console.log('选择了工具:', res.tapIndex)
    }
  })
}

const previewImage = (src) => {
  uni.previewImage({
    urls: [src]
  })
}

const handleGoodsClick = (goods) => {
  navigate.toGoodsDetail(goods.id)
}
</script>

<style lang="scss" scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $uni-bg-color-light;
}

.custom-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx $uni-spacing-md;
  background: #fff;
  border-bottom: 1rpx solid $uni-border-color-light;
  
  .navbar-left, .navbar-right {
    width: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .navbar-center {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .navbar-title {
      font-size: 32rpx;
      color: $uni-text-color;
      font-weight: 500;
    }
    
    .navbar-subtitle {
      font-size: 22rpx;
      color: $uni-text-color-grey;
      margin-top: 4rpx;
    }
  }
}

.chat-messages {
  flex: 1;
  padding: $uni-spacing-md;
}

.loading-history {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $uni-spacing-md;
  
  .loading-text {
    font-size: 24rpx;
    color: $uni-text-color-grey;
    margin-left: $uni-spacing-sm;
  }
}

.message-item {
  display: flex;
  margin-bottom: $uni-spacing-lg;
  
  &.message-self {
    flex-direction: row-reverse;
    
    .message-bubble {
      background: $uni-color-primary;
      color: #fff;
      margin-right: $uni-spacing-sm;
      margin-left: 120rpx;
    }
  }
  
  &.message-other {
    .message-bubble {
      background: #fff;
      color: $uni-text-color;
      margin-left: $uni-spacing-sm;
      margin-right: 120rpx;
    }
  }
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: $uni-border-radius-circle;
  background: $uni-bg-color-grey;
}

.message-bubble {
  max-width: 480rpx;
  padding: $uni-spacing-sm $uni-spacing-md;
  border-radius: $uni-border-radius-lg;
  position: relative;
  
  .message-text {
    font-size: 28rpx;
    line-height: 1.4;
    word-wrap: break-word;
  }
  
  .message-image {
    image {
      max-width: 400rpx;
      max-height: 400rpx;
      border-radius: $uni-border-radius-sm;
    }
  }
  
  .message-goods {
    display: flex;
    background: #fff;
    border-radius: $uni-border-radius-sm;
    overflow: hidden;
    border: 1rpx solid $uni-border-color-light;
    
    .goods-img {
      width: 120rpx;
      height: 120rpx;
    }
    
    .goods-info {
      flex: 1;
      padding: $uni-spacing-sm;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .goods-title {
        font-size: 26rpx;
        color: $uni-text-color;
        margin-bottom: $uni-spacing-xs;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .goods-price {
        font-size: 28rpx;
        color: $uni-color-error;
        font-weight: bold;
      }
    }
  }
  
  .message-time {
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.7);
    margin-top: $uni-spacing-xs;
    text-align: center;
  }
}

.message-other .message-bubble .message-time {
  color: $uni-text-color-placeholder;
}

.chat-input-bar {
  display: flex;
  align-items: center;
  padding: $uni-spacing-md;
  background: #fff;
  border-top: 1rpx solid $uni-border-color-light;
  
  .input-tools {
    display: flex;
    align-items: center;
    gap: $uni-spacing-md;
    margin-right: $uni-spacing-md;
  }
  
  .chat-input {
    flex: 1;
    background: $uni-bg-color-grey;
    border-radius: 40rpx;
    padding: 16rpx $uni-spacing-md;
    font-size: 28rpx;
    border: none;
    outline: none;
    max-height: 120rpx;
  }
  
  .send-btn {
    background: $uni-bg-color-grey;
    color: $uni-text-color-grey;
    padding: 16rpx $uni-spacing-lg;
    border-radius: 40rpx;
    font-size: 28rpx;
    margin-left: $uni-spacing-md;
    transition: all $uni-animation-duration-fast;
    
    &.active {
      background: $uni-color-primary;
      color: #fff;
    }
  }
}
</style>
