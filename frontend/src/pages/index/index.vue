
<template>
  <view class="home-container">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <input class="search-input" placeholder="立即查找" />
      <view class="search-icons">
        <uni-icons type="more-filled" size="24" />
        <uni-icons type="person" size="24" />
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper class="banner-swiper" circular indicator-dots autoplay>
      <swiper-item v-for="(item, idx) in banners" :key="idx">
        <image :src="item" class="banner-img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 分类Tab -->
    <view class="tab-bar">
      <view v-for="(tab, idx) in tabs" :key="tab" :class="['tab-item', {active: idx === activeTab}]" @tap="() => activeTab = idx">
        {{ tab }}
        <view v-if="idx === activeTab" class="tab-underline"></view>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="goods-list">
      <view v-for="(item, idx) in goodsList" :key="idx" class="goods-card">
        <image :src="item.img" class="goods-img" mode="aspectFill" />
        <view class="goods-info">
          <view class="goods-title">{{ item.title }}</view>
          <view class="goods-tags">
            <text v-for="tag in item.tags" :key="tag" class="goods-tag">{{ tag }}</text>
          </view>
          <view class="goods-price">
            <text class="price">￥{{ item.price }}</text>
            <text class="origin-price">￥{{ item.originPrice }}</text>
            <uni-icons type="cart" size="22" class="cart-icon" />
          </view>
        </view>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <TabBar :current="'pages/index/index'" />
  </view>
</template>


<script setup>
import TabBar from '@/components/TabBar.vue'
import { ref, computed } from 'vue'
const banners = [
  '/static/banner1.png',
  '/static/banner2.png',
]
const tabs = ['精选推荐', '办公助手', 'Web自动化', '人气榜']
const activeTab = ref(0)

const goodsMap = [
  [
    {
      img: '/static/goods1.png',
      title: '精选推荐1',
      tags: ['限时'],
      price: 298,
      originPrice: 400,
    },
    {
      img: '/static/goods2.png',
      title: '精选推荐2',
      tags: ['new'],
      price: 259,
      originPrice: 319,
    },
  ],
  [
    {
      img: '/static/goods1.png',
      title: '办公助手1',
      tags: ['热卖'],
      price: 199,
      originPrice: 299,
    },
    {
      img: '/static/goods2.png',
      title: '办公助手2',
      tags: ['推荐'],
      price: 159,
      originPrice: 219,
    },
  ],
  [
    {
      img: '/static/goods1.png',
      title: 'Web自动化1',
      tags: ['自动化'],
      price: 399,
      originPrice: 499,
    },
    {
      img: '/static/goods2.png',
      title: 'Web自动化2',
      tags: ['高效'],
      price: 359,
      originPrice: 419,
    },
  ],
  [
    {
      img: '/static/goods1.png',
      title: '人气榜1',
      tags: ['爆款'],
      price: 499,
      originPrice: 599,
    },
    {
      img: '/static/goods2.png',
      title: '人气榜2',
      tags: ['热销'],
      price: 459,
      originPrice: 519,
    },
  ],
]

const goodsList = computed(() => goodsMap[activeTab.value])
</script>


<style lang="scss" scoped>
.home-container {
  background: #fafbfc;
  min-height: 100vh;
  padding-bottom: 120rpx;
}
.search-bar {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx 0 24rpx;
  justify-content: space-between;
  .search-input {
    flex: 1;
    background: #f5f5f5;
    border-radius: 32rpx;
    padding: 16rpx 32rpx;
    font-size: 28rpx;
    border: none;
    outline: none;
  }
  .search-icons {
    display: flex;
    align-items: center;
    margin-left: 16rpx;
    gap: 16rpx;
  }
}
.banner-swiper {
  width: 100%;
  height: 320rpx;
  margin: 24rpx 0;
  .banner-img {
    width: 100%;
    height: 320rpx;
    border-radius: 24rpx;
  }
}
.tab-bar {
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  background: #fff;
  .tab-item {
    font-size: 32rpx;
    font-weight: 500;
    color: #888;
    margin-right: 48rpx;
    position: relative;
    &.active {
      color: #222;
      font-weight: bold;
    }
    .tab-underline {
      position: absolute;
      left: 0;
      bottom: -8rpx;
      width: 48rpx;
      height: 6rpx;
      background: #e60012;
      border-radius: 3rpx;
    }
  }
}
.goods-list {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
  padding: 32rpx 24rpx 0 24rpx;
  .goods-card {
    width: calc(50% - 12rpx);
    background: #fff;
    border-radius: 18rpx;
    overflow: hidden;
    margin-bottom: 32rpx;
    .goods-img {
      width: 100%;
      height: 320rpx;
      object-fit: cover;
    }
    .goods-info {
      padding: 18rpx 16rpx 12rpx 16rpx;
      .goods-title {
        font-size: 28rpx;
        color: #222;
        margin-bottom: 8rpx;
        height: 72rpx;
        overflow: hidden;
        text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
      }
      .goods-tags {
        margin-bottom: 8rpx;
        .goods-tag {
          font-size: 22rpx;
          color: #e60012;
          background: #fff0f0;
          border-radius: 8rpx;
          padding: 2rpx 12rpx;
          margin-right: 8rpx;
        }
      }
      .goods-price {
        display: flex;
        align-items: center;
        .price {
          color: #e60012;
          font-size: 32rpx;
          font-weight: bold;
        }
        .origin-price {
          color: #bbb;
          font-size: 24rpx;
          margin-left: 12rpx;
          text-decoration: line-through;
        }
        .cart-icon {
          margin-left: auto;
          color: #e60012;
        }
      }
    }
  }
}
</style>
