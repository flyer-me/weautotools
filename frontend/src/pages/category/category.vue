<template>
  <view class="category-page">
    <view class="category-sidebar">
      <view
        v-for="(item, idx) in categories"
        :key="item.name"
        :class="['sidebar-item', { active: idx === activeSidebar } ]"
        @click="activeSidebar = idx"
      >
        {{ item.name }}
      </view>
    </view>
    <view class="category-content">
      <view class="category-title">{{ categories[activeSidebar].name }}</view>
      <view class="category-grid">
        <view
          v-for="sub in categories[activeSidebar].sub"
          :key="sub.name"
          class="category-item"
        >
          <image :src="sub.img" mode="aspectFit" class="category-img" />
          <view class="category-label">{{ sub.name }}</view>
        </view>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <TabBar :current="'pages/category/category'" />
  </view>
</template>

<script setup>
import TabBar from '@/components/TabBar.vue'
import { ref } from 'vue'

const categories = ref([
  {
    name: 'Word',
    sub: [
      { name: 'Word生成', img: '/static/category/1.png' },
      { name: 'Word转换', img: '/static/category/2.png' },
      { name: 'Word压缩', img: '/static/category/3.png' },
    ],
  },
  { name: 'Excel', sub: [] },
  { name: '浏览器', sub: [] },
  { name: '文件操作', sub: [] },
])

const activeSidebar = ref(0)
</script>

<style lang="scss" scoped>
.category-page {
  display: flex;
  height: 100vh;
  background: #fff;
}
.category-sidebar {
  width: 120rpx;
  background: #f7f7f7;
  display: flex;
  flex-direction: column;
  padding-top: 40rpx;
}
.sidebar-item {
  padding: 24rpx 0;
  text-align: center;
  color: #666;
  font-size: 28rpx;
  background: #f7f7f7;
  border-left: 8rpx solid transparent;
  transition: background 0.2s;
}
.sidebar-item.active {
  color: #ff4d4f;
  background: #fff;
  border-left: 8rpx solid #ff4d4f;
  font-weight: bold;
}
.category-content {
  flex: 1;
  padding: 32rpx 0 0 32rpx;
}
.category-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 32rpx;
}
.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 40rpx 48rpx;
}
.category-item {
  width: 140rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.category-img {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 12rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
}
.category-label {
  font-size: 26rpx;
  color: #444;
}
</style>
