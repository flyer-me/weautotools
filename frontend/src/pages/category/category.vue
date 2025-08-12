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
          @click="handleCategoryClick(sub)"
        >
          <image :src="sub.img" mode="aspectFit" class="category-img" />
          <view class="category-label">{{ sub.name }}</view>
          <view class="category-desc">{{ sub.desc }}</view>
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
      { name: 'Word生成', img: '/static/category/word-generate.png', desc: '自动生成Word文档' },
      { name: 'Word转换', img: '/static/category/word-convert.png', desc: '格式转换工具' },
      { name: 'Word压缩', img: '/static/category/word-compress.png', desc: '文档压缩优化' },
      { name: 'Word合并', img: '/static/category/word-merge.png', desc: '多文档合并' },
      { name: 'Word拆分', img: '/static/category/word-split.png', desc: '文档拆分工具' },
      { name: 'Word模板', img: '/static/category/word-template.png', desc: '模板库管理' },
    ],
  },
  {
    name: 'Excel',
    sub: [
      { name: 'Excel处理', img: '/static/category/excel-process.png', desc: '数据处理工具' },
      { name: 'Excel图表', img: '/static/category/excel-chart.png', desc: '图表生成器' },
      { name: 'Excel公式', img: '/static/category/excel-formula.png', desc: '公式助手' },
      { name: 'Excel导入', img: '/static/category/excel-import.png', desc: '数据导入工具' },
      { name: 'Excel导出', img: '/static/category/excel-export.png', desc: '数据导出工具' },
      { name: 'Excel清洗', img: '/static/category/excel-clean.png', desc: '数据清洗工具' },
    ],
  },
  {
    name: '浏览器',
    sub: [
      { name: '网页抓取', img: '/static/category/web-scrape.png', desc: '网页数据抓取' },
      { name: '表单填写', img: '/static/category/web-form.png', desc: '自动表单填写' },
      { name: '页面监控', img: '/static/category/web-monitor.png', desc: '网页变化监控' },
      { name: '批量下载', img: '/static/category/web-download.png', desc: '批量文件下载' },
      { name: '自动登录', img: '/static/category/web-login.png', desc: '网站自动登录' },
      { name: '数据采集', img: '/static/category/web-collect.png', desc: '网页数据采集' },
    ],
  },
  {
    name: '文件操作',
    sub: [
      { name: '批量重命名', img: '/static/category/file-rename.png', desc: '文件批量重命名' },
      { name: '文件分类', img: '/static/category/file-classify.png', desc: '智能文件分类' },
      { name: '重复清理', img: '/static/category/file-duplicate.png', desc: '重复文件清理' },
      { name: '格式转换', img: '/static/category/file-convert.png', desc: '文件格式转换' },
      { name: '批量压缩', img: '/static/category/file-compress.png', desc: '文件批量压缩' },
      { name: '文件同步', img: '/static/category/file-sync.png', desc: '文件同步工具' },
    ],
  },
])

const activeSidebar = ref(0)

// 处理分类点击事件
const handleCategoryClick = (category) => {
  console.log('点击分类:', category)
  // 这里可以跳转到对应的商品列表页面
  uni.showToast({
    title: `点击了${category.name}`,
    icon: 'none'
  })
  // 实际项目中可以这样跳转：
  // uni.navigateTo({
  //   url: `/pages/goods-list/goods-list?category=${category.name}`
  // })
}
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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160rpx, 1fr));
  gap: 24rpx;
  padding-right: 32rpx;
}
.category-item {
  width: 160rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    background: #f8f9fa;
    transform: translateY(-2rpx);
  }

  &:active {
    transform: translateY(0);
    background: #e9ecef;
  }
}

.category-img {
  width: 96rpx;
  height: 96rpx;
  margin-bottom: 12rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  border: 2rpx solid #eee;
}

.category-label {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 4rpx;
  text-align: center;
}

.category-desc {
  font-size: 22rpx;
  color: #888;
  text-align: center;
  line-height: 1.3;
}
</style>
