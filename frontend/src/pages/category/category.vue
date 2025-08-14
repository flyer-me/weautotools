<template>
  <view class="category-page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-container">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#999" class="search-icon" />
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索工具功能..."
            placeholder-style="color: #999"
            @input="handleSearchInput"
            @confirm="handleSearchConfirm"
            @focus="handleSearchFocus"
            @blur="handleSearchBlur"
          />
          <view
            v-if="searchKeyword"
            class="search-clear"
            @click="clearSearch"
          >
            <uni-icons type="clear" size="16" color="#999" />
          </view>
        </view>
      </view>

      <!-- 搜索建议 -->
      <view v-if="showSearchSuggestions && searchSuggestions.length" class="search-suggestions">
        <view
          v-for="suggestion in searchSuggestions"
          :key="suggestion.id"
          class="suggestion-item"
          @click="handleSuggestionClick(suggestion)"
        >
          <uni-icons type="search" size="14" color="#666" />
          <text class="suggestion-text">{{ suggestion.name }}</text>
          <text class="suggestion-category">{{ suggestion.categoryName }}</text>
        </view>
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content" :class="{ 'search-mode': isSearchMode }">
      <!-- 搜索结果 -->
      <view v-if="isSearchMode" class="search-results">
        <view v-if="searchResults.length" class="search-results-header">
          <text class="results-count">找到 {{ searchResults.length }} 个相关工具</text>
        </view>

        <view v-if="searchResults.length" class="search-results-grid">
          <view
            v-for="item in searchResults"
            :key="item.id"
            class="search-result-item"
            @click="handleCategoryClick(item)"
          >
            <CategoryIcon
              :name="item.name"
              type="tool"
              size="md"
              color="primary"
            />
            <view class="result-info">
              <view class="result-name">{{ item.name }}</view>
              <view class="result-desc">{{ item.desc }}</view>
              <view class="result-category">{{ item.categoryName }}</view>
            </view>
          </view>
        </view>

        <!-- 搜索无结果 -->
        <view v-else class="search-empty">
          <uni-icons type="search" size="60" color="#ccc" />
          <text class="empty-text">未找到相关工具</text>
          <text class="empty-tip">试试其他关键词</text>
        </view>
      </view>

      <!-- 分类浏览模式 -->
      <view v-else class="category-browse">
        <view class="category-sidebar">
          <view
            v-for="(item, idx) in categories"
            :key="item.name"
            :class="['sidebar-item', { active: idx === activeSidebar }]"
            @click="activeSidebar = idx"
          >
            <uni-icons :type="item.icon" size="20" class="sidebar-icon" />
            <text class="sidebar-text">{{ item.name }}</text>
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
              <CategoryIcon
                :name="sub.name"
                type="tool"
                size="lg"
                color="primary"
              />
              <view class="category-label">{{ sub.name }}</view>
              <view class="category-desc">{{ sub.desc }}</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 自定义 TabBar -->
    <TabBar :current="'pages/category/category'" />
  </view>
</template>

<script setup>
import TabBar from '@/components/TabBar.vue'
import CategoryIcon from '@/components/CategoryIcon.vue'
import { ref, computed, watch, nextTick } from 'vue'

const categories = ref([
  {
    name: 'PDF与文档',
    icon: 'paperclip',
    sub: [
      {
        name: 'PDF转换',
        icon: 'refresh',
        desc: 'PDF与其他格式互转',
        keywords: ['PDF', '转换', 'Word', 'Excel', 'PPT', '图片'],
        route: '/pages/tools/pdf-convert'
      },
      {
        name: 'PDF压缩',
        icon: 'download',
        desc: '减小PDF文件大小',
        keywords: ['PDF', '压缩', '优化', '文件大小'],
        route: '/pages/tools/pdf-compress'
      },
      {
        name: 'PDF合并',
        icon: 'plus',
        desc: '多个PDF文件合并',
        keywords: ['PDF', '合并', '拼接', '组合'],
        route: '/pages/tools/pdf-merge'
      },
      {
        name: 'PDF拆分',
        icon: 'minus',
        desc: '拆分PDF为多个文件',
        keywords: ['PDF', '拆分', '分割', '提取'],
        route: '/pages/tools/pdf-split'
      },
      {
        name: '图片转PDF',
        icon: 'image',
        desc: '图片批量转换为PDF',
        keywords: ['图片', 'PDF', '转换', '批量', 'JPG', 'PNG'],
        route: '/pages/tools/image-to-pdf'
      },
      {
        name: 'OCR文字识别',
        icon: 'eye',
        desc: '图片文字识别提取',
        keywords: ['OCR', '文字识别', '图片', '文本提取'],
        route: '/pages/tools/ocr'
      },
    ],
  },
  {
    name: '图片工具',
    icon: 'images',
    sub: [
      {
        name: '图片压缩',
        icon: 'download-filled',
        desc: '无损/有损压缩图片',
        keywords: ['图片', '压缩', '优化', '无损', '有损'],
        route: '/pages/tools/image-compress'
      },
      {
        name: '格式转换',
        icon: 'refresh-filled',
        desc: '图片格式互相转换',
        keywords: ['图片', '格式', '转换', 'JPG', 'PNG', 'WebP', 'GIF'],
        route: '/pages/tools/image-convert'
      },
      {
        name: '批量加水印',
        icon: 'color',
        desc: '批量添加文字/图片水印',
        keywords: ['水印', '批量', '文字', '图片', '版权'],
        route: '/pages/tools/watermark'
      },
      {
        name: '批量重命名',
        icon: 'compose',
        desc: '图片文件批量重命名',
        keywords: ['重命名', '批量', '文件名', '规则'],
        route: '/pages/tools/batch-rename'
      },
    ],
  },
  {
    name: '文件转换',
    icon: 'loop',
    sub: [
      {
        name: '文档格式转换',
        icon: 'paperclip',
        desc: 'Word/Excel/PPT格式互转',
        keywords: ['文档', '转换', 'Word', 'Excel', 'PPT', 'TXT'],
        route: '/pages/tools/doc-convert'
      },
      {
        name: '电子书转换',
        icon: 'contact',
        desc: 'PDF/EPUB/MOBI格式互转',
        keywords: ['电子书', 'PDF', 'EPUB', 'MOBI', '转换'],
        route: '/pages/tools/ebook-convert'
      },
    ],
  },
  {
    name: '数据工具',
    icon: 'bars',
    sub: [
      {
        name: 'JSON转换',
        icon: 'gear',
        desc: 'JSON/XML/YAML格式互转',
        keywords: ['JSON', 'XML', 'YAML', '数据', '转换'],
        route: '/pages/tools/json-convert'
      },
    ],
  },
  {
    name: '二维码工具',
    icon: 'scan',
    sub: [
      {
        name: '二维码生成',
        icon: 'plus-filled',
        desc: '生成各种类型二维码',
        keywords: ['二维码', '生成', 'QR', '链接', '文本'],
        route: '/pages/tools/qr-generate'
      },
      {
        name: '二维码识别',
        icon: 'search',
        desc: '识别解析二维码内容',
        keywords: ['二维码', '识别', '解析', '扫描'],
        route: '/pages/tools/qr-decode'
      },
    ],
  },
])

const activeSidebar = ref(0)

// 搜索相关状态
const searchKeyword = ref('')
const isSearchMode = ref(false)
const showSearchSuggestions = ref(false)
const searchResults = ref([])
const searchSuggestions = ref([])

// 创建所有工具的扁平化列表，用于搜索
const allTools = computed(() => {
  const tools = []
  categories.value.forEach((category, categoryIndex) => {
    category.sub.forEach((tool, toolIndex) => {
      tools.push({
        ...tool,
        id: `${categoryIndex}-${toolIndex}`,
        categoryName: category.name,
        categoryIndex,
        toolIndex
      })
    })
  })
  return tools
})

// 防抖搜索定时器
let searchTimer = null

// 搜索输入处理（添加防抖）
const handleSearchInput = (e) => {
  const value = e.detail.value.trim()
  searchKeyword.value = value

  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }

  if (value) {
    isSearchMode.value = true
    // 防抖处理，300ms后执行搜索
    searchTimer = setTimeout(() => {
      try {
        performSearch(value)
        generateSearchSuggestions(value)
      } catch (error) {
        console.error('搜索出错:', error)
        uni.showToast({
          title: '搜索出错，请重试',
          icon: 'none'
        })
      }
    }, 300)
  } else {
    isSearchMode.value = false
    searchResults.value = []
    searchSuggestions.value = []
    showSearchSuggestions.value = false
  }
}

// 执行搜索（优化性能）
const performSearch = (keyword) => {
  if (!keyword) {
    searchResults.value = []
    return
  }

  try {
    const lowerKeyword = keyword.toLowerCase()
    const startTime = performance.now()

    searchResults.value = allTools.value.filter(tool => {
      // 优先匹配工具名称（权重最高）
      if (tool.name.toLowerCase().includes(lowerKeyword)) return true
      // 匹配关键词（权重较高）
      if (tool.keywords && tool.keywords.some(k => k.toLowerCase().includes(lowerKeyword))) return true
      // 匹配描述（权重中等）
      if (tool.desc.toLowerCase().includes(lowerKeyword)) return true
      // 匹配分类名称（权重较低）
      if (tool.categoryName.toLowerCase().includes(lowerKeyword)) return true

      return false
    })

    // 按匹配度排序
    searchResults.value.sort((a, b) => {
      const aNameMatch = a.name.toLowerCase().includes(lowerKeyword)
      const bNameMatch = b.name.toLowerCase().includes(lowerKeyword)

      if (aNameMatch && !bNameMatch) return -1
      if (!aNameMatch && bNameMatch) return 1

      // 如果都匹配名称或都不匹配名称，按字母顺序排序
      return a.name.localeCompare(b.name)
    })

    const endTime = performance.now()
    console.log(`搜索耗时: ${endTime - startTime}ms, 结果数量: ${searchResults.value.length}`)

  } catch (error) {
    console.error('搜索执行出错:', error)
    searchResults.value = []
    throw error
  }
}

// 生成搜索建议
const generateSearchSuggestions = (keyword) => {
  if (!keyword || keyword.length < 2) {
    searchSuggestions.value = []
    showSearchSuggestions.value = false
    return
  }

  const lowerKeyword = keyword.toLowerCase()
  const suggestions = allTools.value
    .filter(tool =>
      tool.name.toLowerCase().includes(lowerKeyword) ||
      (tool.keywords && tool.keywords.some(k => k.toLowerCase().includes(lowerKeyword)))
    )
    .slice(0, 5) // 最多显示5个建议

  searchSuggestions.value = suggestions
  showSearchSuggestions.value = suggestions.length > 0
}

// 搜索确认
const handleSearchConfirm = () => {
  showSearchSuggestions.value = false
  if (searchKeyword.value.trim()) {
    performSearch(searchKeyword.value.trim())
  }
}

// 搜索获得焦点
const handleSearchFocus = () => {
  if (searchKeyword.value && searchSuggestions.value.length > 0) {
    showSearchSuggestions.value = true
  }
}

// 搜索失去焦点
const handleSearchBlur = () => {
  // 延迟隐藏建议，以便点击建议项
  setTimeout(() => {
    showSearchSuggestions.value = false
  }, 200)
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  isSearchMode.value = false
  searchResults.value = []
  searchSuggestions.value = []
  showSearchSuggestions.value = false
}

// 点击搜索建议
const handleSuggestionClick = (suggestion) => {
  searchKeyword.value = suggestion.name
  showSearchSuggestions.value = false
  performSearch(suggestion.name)
  // 直接跳转到该工具
  handleCategoryClick(suggestion)
}

// 处理分类点击事件
const handleCategoryClick = (category) => {
  console.log('点击分类:', category)

  try {
    // 记录用户行为（可用于分析）
    const clickData = {
      toolName: category.name,
      categoryName: category.categoryName || categories.value[activeSidebar.value]?.name,
      timestamp: new Date().toISOString(),
      searchKeyword: searchKeyword.value || null
    }
    console.log('用户点击工具:', clickData)

    // 如果有路由配置，直接跳转
    if (category.route) {
      uni.navigateTo({
        url: category.route,
        fail: (error) => {
          console.error('页面跳转失败:', error)
          uni.showToast({
            title: '功能开发中',
            icon: 'none'
          })
        }
      })
    } else {
      uni.showToast({
        title: `点击了${category.name}`,
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('处理点击事件出错:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}

// 监听搜索关键词变化
watch(searchKeyword, (newKeyword, oldKeyword) => {
  if (newKeyword !== oldKeyword) {
    console.log(`搜索关键词变化: "${oldKeyword}" -> "${newKeyword}"`)
  }
})

// 监听搜索模式变化
watch(isSearchMode, (newMode, oldMode) => {
  if (newMode !== oldMode) {
    console.log(`搜索模式变化: ${oldMode} -> ${newMode}`)
    if (!newMode) {
      // 退出搜索模式时清理数据
      nextTick(() => {
        searchResults.value = []
        searchSuggestions.value = []
        showSearchSuggestions.value = false
      })
    }
  }
})

// 组件卸载时清理定时器
const cleanup = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
}

// 页面卸载时清理
uni.$on('beforeDestroy', cleanup)
uni.$on('onUnload', cleanup)
</script>

<style lang="scss" scoped>
.category-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f8f9fa;
}

/* 搜索头部 */
.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1rpx solid #eee;
  padding: 24rpx 32rpx 16rpx;
}

.search-container {
  position: relative;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 24rpx;
  padding: 0 32rpx;
  height: 72rpx;
}

.search-icon {
  margin-right: 16rpx;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  background: transparent;
  border: none;
  outline: none;
}

.search-clear {
  margin-left: 16rpx;
  padding: 8rpx;
  flex-shrink: 0;
  cursor: pointer;
}

/* 搜索建议 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  margin-top: 8rpx;
  max-height: 400rpx;
  overflow-y: auto;
  z-index: 200;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f8f9fa;
  }

  &:last-child {
    border-bottom: none;
  }
}

.suggestion-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  margin-left: 16rpx;
}

.suggestion-category {
  font-size: 24rpx;
  color: #999;
  background: #f0f0f0;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;

  &.search-mode {
    flex-direction: column;
  }
}

/* 搜索结果 */
.search-results {
  flex: 1;
  padding: 32rpx;
  overflow-y: auto;
}

.search-results-header {
  margin-bottom: 32rpx;
}

.results-count {
  font-size: 28rpx;
  color: #666;
}

.search-results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300rpx, 1fr));
  gap: 24rpx;
}

.search-result-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);

    .result-icon-wrapper {
      background: #e6f4ff;
      border-color: #bae0ff;
      transform: scale(1.1);
    }
  }

  &:active {
    transform: translateY(0);

    .result-icon-wrapper {
      transform: scale(1.05);
    }
  }
}

.result-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  background: #f0f8ff;
  margin-right: 24rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #e6f4ff;
}

.result-info {
  flex: 1;
}

.result-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.result-desc {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 8rpx;
  line-height: 1.4;
}

.result-category {
  font-size: 22rpx;
  color: #999;
  background: #f0f0f0;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  display: inline-block;
}

/* 搜索无结果 */
.search-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;
  text-align: center;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin: 24rpx 0 12rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #999;
}

/* 分类浏览模式 */
.category-browse {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.category-sidebar {
  width: 160rpx;
  background: #fff;
  display: flex;
  flex-direction: column;
  border-right: 1rpx solid #eee;
  overflow-y: auto;
}

.sidebar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 16rpx;
  color: #666;
  font-size: 24rpx;
  background: #fff;
  border-left: 6rpx solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;
  min-height: 120rpx;
  justify-content: center;

  &:hover {
    background: #f8f9fa;
  }

  &.active {
    color: #007aff;
    background: #f0f8ff;
    border-left: 6rpx solid #007aff;
    font-weight: 500;

    .sidebar-icon {
      color: #007aff;
    }
  }
}

.sidebar-icon {
  margin-bottom: 8rpx;
  color: inherit;
  transition: color 0.3s ease;
}

.sidebar-text {
  text-align: center;
  line-height: 1.2;
  color: inherit;
}

.category-content {
  flex: 1;
  padding: 32rpx;
  overflow-y: auto;
  background: #f8f9fa;
}

.category-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 32rpx;
  color: #333;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200rpx, 1fr));
  gap: 24rpx;
}

.category-item {
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 24rpx;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);

    .category-icon-wrapper {
      background: #e6f4ff;
      border-color: #bae0ff;
      transform: scale(1.1);
    }
  }

  &:active {
    transform: translateY(-2rpx);

    .category-icon-wrapper {
      transform: scale(1.05);
    }
  }
}

.category-icon-wrapper {
  width: 96rpx;
  height: 96rpx;
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  background: #f0f8ff;
  border: 2rpx solid #e6f4ff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.category-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 8rpx;
  text-align: center;
  line-height: 1.2;
}

.category-desc {
  font-size: 24rpx;
  color: #666;
  text-align: center;
  line-height: 1.3;
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .search-header {
    padding: 20rpx 24rpx 12rpx;
  }

  .search-input-wrapper {
    height: 64rpx;
    padding: 0 24rpx;
  }

  .category-sidebar {
    width: 140rpx;
  }

  .sidebar-item {
    padding: 24rpx 12rpx;
    min-height: 100rpx;
  }

  .category-content {
    padding: 24rpx;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(160rpx, 1fr));
    gap: 16rpx;
  }

  .search-results-grid {
    grid-template-columns: 1fr;
    gap: 16rpx;
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-suggestions,
.search-results {
  animation: fadeIn 0.3s ease;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8rpx;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4rpx;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4rpx;

  &:hover {
    background: #a8a8a8;
  }
}
</style>
