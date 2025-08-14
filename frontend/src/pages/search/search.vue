<template>
  <view class="search-page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-input-wrapper">
        <uni-icons type="search" size="20" color="#999" />
        <input 
          class="search-input" 
          placeholder="搜索产品" 
          v-model="searchKeyword"
          @confirm="handleSearch"
          focus
        />
        <uni-icons 
          v-if="searchKeyword" 
          type="clear" 
          size="20" 
          color="#999" 
          @click="clearSearch"
        />
      </view>
      <text class="search-cancel" @click="handleCancel">取消</text>
    </view>

    <!-- 搜索建议 -->
    <view v-if="!hasSearched && searchSuggestions.length" class="search-suggestions">
      <view class="suggestion-title">搜索建议</view>
      <view class="suggestion-list">
        <text 
          v-for="suggestion in searchSuggestions" 
          :key="suggestion"
          class="suggestion-item"
          @click="selectSuggestion(suggestion)"
        >
          {{ suggestion }}
        </text>
      </view>
    </view>

    <!-- 热门搜索 -->
    <view v-if="!hasSearched" class="hot-search">
      <view class="hot-title">热门搜索</view>
      <view class="hot-list">
        <text 
          v-for="hot in hotSearchList" 
          :key="hot"
          class="hot-item"
          @click="selectSuggestion(hot)"
        >
          {{ hot }}
        </text>
      </view>
    </view>

    <!-- 搜索历史 -->
    <view v-if="!hasSearched && searchHistory.length" class="search-history">
      <view class="history-header">
        <text class="history-title">搜索历史</text>
        <uni-icons type="trash" size="20" color="#999" @click="clearHistory" />
      </view>
      <view class="history-list">
        <view 
          v-for="history in searchHistory" 
          :key="history"
          class="history-item"
          @click="selectSuggestion(history)"
        >
          <uni-icons type="clock" size="16" color="#999" />
          <text class="history-text">{{ history }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view v-if="hasSearched" class="search-results">
      <!-- 筛选栏 -->
      <view class="filter-bar">
        <view class="filter-item" @click="showSortModal">
          <text class="filter-text">{{ currentSort.name }}</text>
          <uni-icons type="arrowdown" size="16" />
        </view>
        <view class="filter-item" @click="showFilterModal">
          <text class="filter-text">筛选</text>
          <uni-icons type="settings" size="16" />
        </view>
      </view>

      <!-- 结果统计 -->
      <view class="result-stats">
        找到 {{ searchResults.length }} 个相关产品
      </view>

      <!-- 产品列表 -->
      <view class="goods-list">
        <view 
          v-for="goods in searchResults" 
          :key="goods.id"
          class="goods-item"
          @click="handleGoodsClick(goods)"
        >
          <image :src="goods.img" class="goods-img" mode="aspectFill" />
          <view class="goods-info">
            <view class="goods-title">{{ goods.title }}</view>
            <view class="goods-desc">{{ goods.desc }}</view>
            <view class="goods-tags">
              <text v-for="tag in goods.tags" :key="tag" class="goods-tag">{{ tag }}</text>
            </view>
            <view class="goods-price">
              <text class="price">￥{{ goods.price }}</text>
              <text class="origin-price">￥{{ goods.originPrice }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="searchResults.length === 0" class="empty-state">
        <uni-icons type="search" size="80" color="#ccc" />
        <text class="empty-text">没有找到相关产品</text>
        <text class="empty-tip">试试其他关键词吧</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 搜索关键词
const searchKeyword = ref('')
// 是否已搜索
const hasSearched = ref(false)

// 搜索建议
const searchSuggestions = ref([])

// 热门搜索
const hotSearchList = ref([
  'Excel自动化', 'Word处理', '网页抓取', '数据分析', 
  '办公助手', '批量处理', '自动填表', '文件管理'
])

// 搜索历史
const searchHistory = ref([
  'Excel表格处理', 'PDF转换工具', '数据清洗'
])

// 当前排序
const currentSort = ref({ name: '综合排序', value: 'default' })

// 搜索结果
const searchResults = ref([])

// 模拟搜索结果数据
const mockResults = [
  {
    id: 1,
    title: 'Excel自动化处理工具专业版',
    desc: '高效处理Excel表格，支持批量操作',
    img: '/static/goods1.png',
    price: 299,
    originPrice: 399,
    tags: ['热销', '专业版']
  },
  {
    id: 2,
    title: 'Word文档批量处理器',
    desc: '一键处理多个Word文档，提高办公效率',
    img: '/static/goods2.png',
    price: 199,
    originPrice: 299,
    tags: ['推荐', '批量']
  }
]

// 处理搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  
  console.log('搜索:', searchKeyword.value)
  hasSearched.value = true
  
  // 添加到搜索历史
  if (!searchHistory.value.includes(searchKeyword.value)) {
    searchHistory.value.unshift(searchKeyword.value)
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }
  }
  
  // 模拟搜索结果
  searchResults.value = mockResults.filter(item => 
    item.title.includes(searchKeyword.value) || 
    item.desc.includes(searchKeyword.value)
  )
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchSuggestions.value = []
}

// 取消搜索
const handleCancel = () => {
  uni.navigateBack()
}

// 选择建议
const selectSuggestion = (suggestion) => {
  searchKeyword.value = suggestion
  handleSearch()
}

// 清空历史
const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
      }
    }
  })
}

// 显示排序弹窗
const showSortModal = () => {
  const sortOptions = ['综合排序', '价格从低到高', '价格从高到低', '销量优先']
  uni.showActionSheet({
    itemList: sortOptions,
    success: (res) => {
      currentSort.value = {
        name: sortOptions[res.tapIndex],
        value: ['default', 'price_asc', 'price_desc', 'sales'][res.tapIndex]
      }
      // 这里可以重新排序搜索结果
    }
  })
}

// 显示筛选弹窗
const showFilterModal = () => {
  uni.showToast({
    title: '筛选功能开发中',
    icon: 'none'
  })
}

// 产品点击
const handleGoodsClick = (goods) => {
  uni.navigateTo({
    url: `/pages/goods-detail/goods-detail?id=${goods.id}`
  })
}

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const keyword = currentPage.options.keyword
  
  if (keyword) {
    searchKeyword.value = decodeURIComponent(keyword)
    handleSearch()
  }
})
</script>

<style lang="scss" scoped>
.search-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.search-header {
  background: #fff;
  padding: 16rpx 24rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #eee;
}

.search-input-wrapper {
  flex: 1;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 16rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  border: none;
  outline: none;
  background: transparent;
}

.search-cancel {
  font-size: 28rpx;
  color: #666;
  margin-left: 24rpx;
  cursor: pointer;
}

.search-suggestions, .hot-search, .search-history {
  background: #fff;
  margin-top: 16rpx;
  padding: 32rpx 24rpx;
}

.suggestion-title, .hot-title, .history-title {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 24rpx;
}

.suggestion-list, .hot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.suggestion-item, .hot-item {
  padding: 12rpx 24rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: #666;
  cursor: pointer;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.history-list {
  .history-item {
    display: flex;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    cursor: pointer;
    
    &:last-child {
      border-bottom: none;
    }
    
    .history-text {
      font-size: 28rpx;
      color: #666;
      margin-left: 12rpx;
    }
  }
}

.search-results {
  .filter-bar {
    background: #fff;
    padding: 24rpx;
    display: flex;
    gap: 32rpx;
    border-bottom: 1rpx solid #eee;
    
    .filter-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      cursor: pointer;
      
      .filter-text {
        font-size: 26rpx;
        color: #666;
      }
    }
  }
  
  .result-stats {
    padding: 24rpx;
    font-size: 24rpx;
    color: #999;
  }
}

.goods-list {
  padding: 0 24rpx;
  
  .goods-item {
    background: #fff;
    border-radius: 12rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;
    display: flex;
    cursor: pointer;
    
    .goods-img {
      width: 160rpx;
      height: 160rpx;
      border-radius: 8rpx;
      margin-right: 24rpx;
    }
    
    .goods-info {
      flex: 1;
      
      .goods-title {
        font-size: 28rpx;
        color: #333;
        font-weight: bold;
        margin-bottom: 8rpx;
      }
      
      .goods-desc {
        font-size: 24rpx;
        color: #666;
        margin-bottom: 12rpx;
      }
      
      .goods-tags {
        margin-bottom: 16rpx;
        
        .goods-tag {
          font-size: 20rpx;
          color: #ff4d4f;
          background: #fff0f0;
          border-radius: 6rpx;
          padding: 2rpx 8rpx;
          margin-right: 8rpx;
        }
      }
      
      .goods-price {
        display: flex;
        align-items: center;
        
        .price {
          font-size: 32rpx;
          color: #ff4d4f;
          font-weight: bold;
          margin-right: 12rpx;
        }
        
        .origin-price {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
        }
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
  
  .empty-text {
    font-size: 32rpx;
    color: #666;
    margin: 24rpx 0 8rpx 0;
  }
  
  .empty-tip {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
