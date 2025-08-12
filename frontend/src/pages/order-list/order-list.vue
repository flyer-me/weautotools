<template>
  <view class="order-list-page">
    <!-- 状态筛选栏 -->
    <view class="status-tabs">
      <view 
        v-for="(tab, index) in statusTabs" 
        :key="tab.value"
        :class="['status-tab', { active: currentStatus === tab.value }]"
        @click="switchStatus(tab.value)"
      >
        {{ tab.label }}
        <view v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list">
      <view v-if="loading" class="loading-container">
        <LoadingState type="skeleton" :count="3" />
      </view>

      <view v-else-if="orderList.length === 0" class="empty-container">
        <EmptyState 
          type="order" 
          title="暂无订单" 
          description="您还没有订单记录"
          :show-button="true"
          button-text="去浏览"
          @button-click="goShopping"
        />
      </view>

      <view v-else>
        <view 
          v-for="order in orderList" 
          :key="order.id"
          class="order-item"
          @click="goOrderDetail(order.id)"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <text class="order-number">订单号：{{ order.orderNo }}</text>
            <text :class="['order-status', `status-${order.status}`]">
              {{ getStatusText(order.status) }}
            </text>
          </view>

          <!-- 商品列表 -->
          <view class="goods-list">
            <view 
              v-for="goods in order.goodsList" 
              :key="goods.id"
              class="goods-item"
            >
              <image :src="goods.img" class="goods-img" mode="aspectFill" />
              <view class="goods-info">
                <text class="goods-title">{{ goods.title }}</text>
                <text class="goods-spec">{{ goods.spec }}</text>
                <view class="goods-price-qty">
                  <text class="goods-price">￥{{ goods.price }}</text>
                  <text class="goods-qty">x{{ goods.quantity }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 订单信息 -->
          <view class="order-info">
            <text class="order-time">{{ order.createTime }}</text>
            <text class="order-total">共{{ order.totalQuantity }}件商品 合计：￥{{ order.totalAmount }}</text>
          </view>

          <!-- 操作按钮 -->
          <view class="order-actions">
            <button 
              v-for="action in getOrderActions(order.status)" 
              :key="action.type"
              :class="['action-btn', `btn-${action.type}`]"
              @click.stop="handleOrderAction(order, action.type)"
            >
              {{ action.text }}
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import LoadingState from '@/components/LoadingState.vue'
import EmptyState from '@/components/EmptyState.vue'
import {
  getOrderListStatusTabs,
  getStatusText,
  getOrderActions,
  ORDER_STATUS
} from '@/utils/orderStatus.js'

// 状态标签 - 使用统一配置
const statusTabs = ref([])

// 模拟订单统计数据
const mockOrderCounts = {
  pending: 2,
  processing: 1,
  shipping: 3,
  refund: 0,
  completed: 5  // 评价状态
}

// 初始化状态标签
const initStatusTabs = () => {
  const tabs = getOrderListStatusTabs()
  statusTabs.value = tabs.map(tab => ({
    ...tab,
    count: tab.value === 'all' ? 0 : (mockOrderCounts[tab.value] || 0)
  }))
}

// 当前状态
const currentStatus = ref('all')
// 加载状态
const loading = ref(false)
// 订单列表
const orderList = ref([])

// 模拟订单数据
const mockOrders = [
  {
    id: 1,
    orderNo: '202401150001',
    status: 'pending',
    createTime: '2024-01-15 14:30:00',
    totalAmount: 598,
    totalQuantity: 2,
    goodsList: [
      {
        id: 1,
        title: 'Excel自动化处理工具专业版',
        spec: '专业版 · 单用户授权',
        price: 299,
        quantity: 1,
        img: '/static/goods1.png'
      },
      {
        id: 2,
        title: 'Word文档批量处理器',
        spec: '标准版 · 单用户授权',
        price: 299,
        quantity: 1,
        img: '/static/goods2.png'
      }
    ]
  },
  {
    id: 2,
    orderNo: '202401140002',
    status: 'shipping',
    createTime: '2024-01-14 10:15:00',
    totalAmount: 199,
    totalQuantity: 1,
    goodsList: [
      {
        id: 3,
        title: '网页数据抓取工具',
        spec: '基础版 · 单用户授权',
        price: 199,
        quantity: 1,
        img: '/static/goods1.png'
      }
    ]
  }
]

// 注意：getStatusText 和 getOrderActions 现在从 orderStatus.js 导入

// 切换状态
const switchStatus = (status) => {
  currentStatus.value = status
  loadOrderList()
}

// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  
  // 模拟API请求
  setTimeout(() => {
    if (currentStatus.value === 'all') {
      orderList.value = mockOrders
    } else {
      orderList.value = mockOrders.filter(order => order.status === currentStatus.value)
    }
    loading.value = false
  }, 500)
}

// 处理订单操作
const handleOrderAction = (order, actionType) => {
  console.log('订单操作:', order.id, actionType)
  
  switch (actionType) {
    case 'pay':
      uni.navigateTo({
        url: `/pages/payment/payment?orderId=${order.id}`
      })
      break
    case 'cancel':
      uni.showModal({
        title: '确认取消',
        content: '确定要取消这个订单吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '订单已取消', icon: 'success' })
            loadOrderList()
          }
        }
      })
      break
    case 'confirm':
      uni.showModal({
        title: '确认收货',
        content: '确认已收到货吗？',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '确认收货成功', icon: 'success' })
            loadOrderList()
          }
        }
      })
      break
    case 'review':
      uni.navigateTo({
        url: `/pages/review/review?orderId=${order.id}`
      })
      break
    case 'rebuy':
      uni.showToast({ title: '已加入购物车', icon: 'success' })
      break
    default:
      uni.showToast({ title: `${actionType}功能开发中`, icon: 'none' })
  }
}

// 跳转订单详情
const goOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order-detail/order-detail?id=${orderId}`
  })
}

// 去购物
const goShopping = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}

// 页面加载
onMounted(() => {
  // 初始化状态标签
  initStatusTabs()

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const status = currentPage.options.status

  if (status && status !== 'all') {
    currentStatus.value = status
  }

  loadOrderList()
})
</script>

<style lang="scss" scoped>
.order-list-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.status-tabs {
  background: #fff;
  display: flex;
  padding: 0 24rpx;
  border-bottom: 1rpx solid #eee;
}

.status-tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #666;
  position: relative;
  cursor: pointer;
  
  &.active {
    color: #007aff;
    font-weight: bold;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 4rpx;
      background: #007aff;
      border-radius: 2rpx;
    }
  }
}

.tab-badge {
  position: absolute;
  top: 8rpx;
  right: 20%;
  background: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  border-radius: 50%;
  min-width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: scale(0.8);
}

.order-list {
  padding: 24rpx;
}

.loading-container, .empty-container {
  background: #fff;
  border-radius: 12rpx;
}

.order-item {
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  padding: 24rpx;
  cursor: pointer;
  
  &:active {
    background: #f8f9fa;
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-number {
  font-size: 26rpx;
  color: #666;
}

.order-status {
  font-size: 26rpx;
  font-weight: bold;

  &.status-pending {
    color: #faad14;
  }

  &.status-processing {
    color: #1890ff;
  }

  &.status-shipping {
    color: #52c41a;
  }

  &.status-completed {
    color: #666;
  }

  &.status-refund {
    color: #722ed1;
  }
}

.goods-list {
  margin-bottom: 24rpx;
}

.goods-item {
  display: flex;
  margin-bottom: 16rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.goods-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-title {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goods-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.goods-price-qty {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 28rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.goods-qty {
  font-size: 24rpx;
  color: #666;
}

.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.order-total {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  font-size: 24rpx;
  border: 1rpx solid #d9d9d9;
  background: #fff;
  color: #666;
  cursor: pointer;
  
  &.btn-pay {
    background: #ff4d4f;
    color: #fff;
    border-color: #ff4d4f;
  }
  
  &.btn-confirm {
    background: #52c41a;
    color: #fff;
    border-color: #52c41a;
  }
  
  &:active {
    opacity: 0.8;
  }
}
</style>
