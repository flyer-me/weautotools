<template>
  <view 
    :class="['goods-card', layoutClass]"
    @click="handleClick"
  >
    <image :src="goods.img" class="goods-img" mode="aspectFill" />
    <view class="goods-info">
      <view class="goods-title">{{ goods.title }}</view>
      <view v-if="goods.desc && layout === 'horizontal'" class="goods-desc">{{ goods.desc }}</view>
      <view class="goods-tags" v-if="goods.tags && goods.tags.length">
        <text v-for="tag in goods.tags" :key="tag" class="goods-tag">{{ tag }}</text>
      </view>
      <view class="goods-price">
        <text class="price">￥{{ goods.price }}</text>
        <text v-if="goods.originPrice" class="origin-price">￥{{ goods.originPrice }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'GoodsCard',
  props: {
    goods: {
      type: Object,
      required: true
    },
    layout: {
      type: String,
      default: 'vertical', // vertical | horizontal
      validator: (value) => ['vertical', 'horizontal'].includes(value)
    },

  },
  computed: {
    layoutClass() {
      return `goods-card--${this.layout}`
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.goods)
    }
  }
}
</script>

<style lang="scss" scoped>
.goods-card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  }
  
  &:active {
    transform: translateY(0);
  }
}

// 垂直布局（网格布局）
.goods-card--vertical {
  .goods-img {
    width: 100%;
    height: 320rpx;
    object-fit: cover;
  }
  
  .goods-info {
    padding: 18rpx 16rpx 12rpx 16rpx;
  }
  
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
}

// 水平布局（列表布局）
.goods-card--horizontal {
  display: flex;
  padding: 24rpx;
  
  .goods-img {
    width: 160rpx;
    height: 160rpx;
    border-radius: 8rpx;
    margin-right: 24rpx;
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
    font-weight: bold;
    margin-bottom: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-clamp: 2;
  }
  
  .goods-desc {
    font-size: 24rpx;
    color: #666;
    margin-bottom: 12rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
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
  

}
</style>
