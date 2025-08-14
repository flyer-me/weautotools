<template>
  <view class="goods-detail">
    <!-- 产品图片轮播 -->
    <swiper class="goods-swiper" indicator-dots circular>
      <swiper-item v-for="(img, idx) in goodsInfo.images" :key="idx">
        <image :src="img" class="goods-image" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 产品基本信息 -->
    <view class="goods-info-card">
      <view class="goods-price-row">
        <text class="current-price">￥{{ goodsInfo.price }}</text>
        <text class="origin-price">￥{{ goodsInfo.originPrice }}</text>
        <view class="goods-tags">
          <text v-for="tag in goodsInfo.tags" :key="tag" class="goods-tag">{{ tag }}</text>
        </view>
      </view>
      <view class="goods-title">{{ goodsInfo.title }}</view>
      <view class="goods-subtitle">{{ goodsInfo.subtitle }}</view>
      
      <!-- 产品规格选择 -->
      <view class="goods-specs">
        <view class="spec-item" v-for="spec in goodsInfo.specs" :key="spec.name">
          <text class="spec-name">{{ spec.name }}</text>
          <view class="spec-options">
            <text 
              v-for="option in spec.options" 
              :key="option"
              :class="['spec-option', { active: selectedSpecs[spec.name] === option }]"
              @click="selectSpec(spec.name, option)"
            >
              {{ option }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 产品详情 -->
    <view class="goods-detail-card">
      <view class="card-title">产品详情</view>
      <view class="detail-content">
        <text class="detail-text">{{ goodsInfo.description }}</text>
        <view class="detail-features">
          <view v-for="feature in goodsInfo.features" :key="feature" class="feature-item">
            <uni-icons type="checkmarkempty" size="16" color="#52c41a" />
            <text class="feature-text">{{ feature }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 用户评价 -->
    <view class="reviews-card">
      <view class="card-title">
        用户评价 ({{ goodsInfo.reviewCount }})
        <view class="review-rating">
          <uni-rate :value="goodsInfo.rating" readonly size="16" />
          <text class="rating-text">{{ goodsInfo.rating }}</text>
        </view>
      </view>
      <view class="review-list">
        <view v-for="review in goodsInfo.reviews" :key="review.id" class="review-item">
          <view class="review-header">
            <image :src="review.avatar" class="review-avatar" />
            <view class="review-user">
              <text class="review-name">{{ review.username }}</text>
              <uni-rate :value="review.rating" readonly size="12" />
            </view>
            <text class="review-time">{{ review.time }}</text>
          </view>
          <text class="review-content">{{ review.content }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <view class="action-left">
        <view class="action-item" @click="handleFavorite">
          <uni-icons :type="isFavorite ? 'heart-filled' : 'heart'" size="24" :color="isFavorite ? '#ff4d4f' : '#666'" />
          <text class="action-text">收藏</text>
        </view>
        <view class="action-item" @click="handleShare">
          <uni-icons type="redo" size="24" color="#666" />
          <text class="action-text">分享</text>
        </view>
      </view>
      <view class="action-right">
        <!-- 正常模式下根据功能开关显示 -->
        <button
          v-if="!DEV_MODE.enabled && getFinalFeatureState('TRADING')"
          class="btn-buy"
          @click="handleBuyNow"
        >
          立即购买
        </button>

        <!-- 开发模式下显示但禁用 -->
        <button
          v-else-if="DEV_MODE.enabled && DEV_MODE.auth.authenticated"
          class="btn-dev-disabled"
          @click="handleDisabledFeatureClick('TRADING')"
        >
          立即购买 (开发模式)
        </button>

        <!-- 功能完全不可用 -->
        <button
          v-else
          class="btn-disabled"
          @click="showFeatureDisabledToast('TRADING')"
        >
          功能暂不可用
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFinalFeatureState, showFeatureDisabledToast, isFeatureClickable, handleDisabledFeatureClick, DEV_MODE } from '@/config/features'

// 产品信息
const goodsInfo = ref({
  id: 1,
  title: 'Excel自动化处理工具',
  subtitle: '高效办公，一键搞定复杂表格处理',
  price: 299,
  originPrice: 399,
  tags: ['热销', '限时优惠'],
  images: [
    '/static/goods1.png',
    '/static/goods2.png',
    '/static/goods1.png'
  ],
  specs: [
    {
      name: '版本',
      options: ['基础版', '专业版', '企业版']
    },
    {
      name: '授权',
      options: ['单用户', '多用户']
    }
  ],
  description: '这是一款功能强大的Excel自动化处理工具，能够帮助用户快速处理各种复杂的表格操作，提高工作效率。支持批量数据处理、图表生成、公式计算等多种功能。',
  features: [
    '支持批量数据处理',
    '一键生成各类图表',
    '智能公式计算',
    '多格式文件导入导出',
    '定时任务执行',
    '云端数据同步'
  ],
  rating: 4.8,
  reviewCount: 128,
  reviews: [
    {
      id: 1,
      username: '用户***123',
      avatar: '/static/avatar.png',
      rating: 5,
      time: '2024-01-15',
      content: '非常好用的工具，大大提高了我的工作效率，强烈推荐！'
    },
    {
      id: 2,
      username: '办公***达人',
      avatar: '/static/avatar.png',
      rating: 4,
      time: '2024-01-10',
      content: '功能很全面，操作简单，值得购买。'
    }
  ]
})

// 选中的规格
const selectedSpecs = ref({
  '版本': '基础版',
  '授权': '单用户'
})

// 是否收藏
const isFavorite = ref(false)

// 选择规格
const selectSpec = (specName, option) => {
  selectedSpecs.value[specName] = option
}

// 收藏
const handleFavorite = () => {
  isFavorite.value = !isFavorite.value
  uni.showToast({
    title: isFavorite.value ? '已收藏' : '已取消收藏',
    icon: 'none'
  })
}

// 分享
const handleShare = () => {
  uni.share({
    provider: 'weixin',
    type: 0,
    title: goodsInfo.value.title,
    summary: goodsInfo.value.subtitle,
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    }
  })
}



// 立即购买
const handleBuyNow = () => {
  console.log('立即购买', selectedSpecs.value)
  // 检查交易功能是否启用
  if (!getFinalFeatureState('TRADING')) {
    showFeatureDisabledToast('TRADING')
    return
  }
  uni.navigateTo({
    url: `/pages/order-confirm/order-confirm?goodsId=${goodsInfo.value.id}`
  })
}

// 获取产品详情
onMounted(() => {
  // 这里可以根据页面参数获取产品详情
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const goodsId = currentPage.options.id
  console.log('产品ID:', goodsId)
  // 实际项目中这里会调用API获取产品详情
})
</script>

<style lang="scss" scoped>
.goods-detail {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.goods-swiper {
  width: 100%;
  height: 600rpx;
  background: #fff;
}

.goods-image {
  width: 100%;
  height: 600rpx;
}

.goods-info-card {
  background: #fff;
  margin-top: 16rpx;
  padding: 32rpx;
}

.goods-price-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.current-price {
  font-size: 48rpx;
  color: #ff4d4f;
  font-weight: bold;
  margin-right: 16rpx;
}

.origin-price {
  font-size: 28rpx;
  color: #999;
  text-decoration: line-through;
  margin-right: 16rpx;
}

.goods-tags {
  margin-left: auto;
}

.goods-tag {
  font-size: 22rpx;
  color: #ff4d4f;
  background: #fff0f0;
  border-radius: 8rpx;
  padding: 4rpx 12rpx;
  margin-left: 8rpx;
}

.goods-title {
  font-size: 36rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.goods-subtitle {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 32rpx;
}

.goods-specs {
  .spec-item {
    margin-bottom: 24rpx;
    
    .spec-name {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 16rpx;
      display: block;
    }
    
    .spec-options {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
    }
    
    .spec-option {
      padding: 12rpx 24rpx;
      border: 2rpx solid #ddd;
      border-radius: 8rpx;
      font-size: 26rpx;
      color: #666;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &.active {
        border-color: #ff4d4f;
        color: #ff4d4f;
        background: #fff0f0;
      }
    }
  }
}

.goods-detail-card, .reviews-card {
  background: #fff;
  margin-top: 16rpx;
  padding: 32rpx;
}

.card-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 24rpx;
}

.detail-features {
  .feature-item {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
    
    .feature-text {
      font-size: 26rpx;
      color: #666;
      margin-left: 8rpx;
    }
  }
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.rating-text {
  font-size: 24rpx;
  color: #666;
}

.review-list {
  .review-item {
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.review-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.review-user {
  flex: 1;
  
  .review-name {
    font-size: 26rpx;
    color: #333;
    display: block;
    margin-bottom: 4rpx;
  }
}

.review-time {
  font-size: 22rpx;
  color: #999;
}

.review-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.bottom-actions {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 100rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  z-index: 999;
}

.action-left {
  display: flex;
  gap: 32rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  
  .action-text {
    font-size: 20rpx;
    color: #666;
    margin-top: 4rpx;
  }
}

.action-right {
  margin-left: auto;
  display: flex;
  gap: 16rpx;
}

.btn-buy {
  padding: 16rpx 32rpx;
  border-radius: 48rpx;
  font-size: 28rpx;
  border: none;
  cursor: pointer;
  background: #ff4d4f;
  color: #fff;
  flex: 1;
}

.btn-disabled {
  padding: 16rpx 32rpx;
  border-radius: 48rpx;
  font-size: 28rpx;
  border: none;
  cursor: not-allowed;
  background: #d9d9d9;
  color: #999;
  flex: 1;
}

.btn-dev-disabled {
  padding: 16rpx 32rpx;
  border-radius: 48rpx;
  font-size: 28rpx;
  border: 2rpx solid #ff4d4f;
  cursor: pointer;
  background: #fff2f0;
  color: #ff4d4f;
  flex: 1;
  position: relative;

  &:active {
    background: #ffe7e7;
  }
}
</style>
