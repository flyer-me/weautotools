<template>
  <view class="icon-preview-page">
    <!-- 头部 -->
    <view class="header">
      <text class="title">图标预览</text>
      <text class="subtitle">Category 页面使用的所有图标</text>
    </view>

    <!-- 分类图标 -->
    <view class="section">
      <view class="section-title">分类图标</view>
      <view class="icon-grid">
        <view 
          v-for="(icon, name) in categoryIcons" 
          :key="name"
          class="icon-item"
          @click="copyIconName(icon)"
        >
          <CategoryIcon 
            :name="name" 
            type="category" 
            size="lg" 
            color="primary"
          />
          <text class="icon-name">{{ name }}</text>
          <text class="icon-type">{{ icon }}</text>
        </view>
      </view>
    </view>

    <!-- 工具图标 -->
    <view class="section">
      <view class="section-title">工具图标</view>
      <view class="icon-grid">
        <view 
          v-for="(icon, name) in toolIcons" 
          :key="name"
          class="icon-item"
          @click="copyIconName(icon)"
        >
          <CategoryIcon 
            :name="name" 
            type="tool" 
            size="md" 
            color="primary"
          />
          <text class="icon-name">{{ name }}</text>
          <text class="icon-type">{{ icon }}</text>
        </view>
      </view>
    </view>

    <!-- 状态图标 -->
    <view class="section">
      <view class="section-title">状态图标</view>
      <view class="icon-grid">
        <view 
          v-for="(icon, name) in statusIcons" 
          :key="name"
          class="icon-item"
          @click="copyIconName(icon)"
        >
          <view class="status-icon-wrapper">
            <uni-icons :type="icon" size="24" :color="getStatusColor(name)" />
          </view>
          <text class="icon-name">{{ name }}</text>
          <text class="icon-type">{{ icon }}</text>
        </view>
      </view>
    </view>

    <!-- 操作图标 -->
    <view class="section">
      <view class="section-title">操作图标</view>
      <view class="icon-grid">
        <view 
          v-for="(icon, name) in actionIcons" 
          :key="name"
          class="icon-item"
          @click="copyIconName(icon)"
        >
          <view class="action-icon-wrapper">
            <uni-icons :type="icon" size="20" color="#666" />
          </view>
          <text class="icon-name">{{ name }}</text>
          <text class="icon-type">{{ icon }}</text>
        </view>
      </view>
    </view>

    <!-- 颜色示例 -->
    <view class="section">
      <view class="section-title">颜色示例</view>
      <view class="color-grid">
        <view 
          v-for="(color, name) in iconColors" 
          :key="name"
          class="color-item"
        >
          <CategoryIcon 
            name="PDF转换" 
            type="tool" 
            size="md" 
            :color="name"
          />
          <text class="color-name">{{ name }}</text>
          <text class="color-value">{{ color }}</text>
        </view>
      </view>
    </view>

    <!-- 尺寸示例 -->
    <view class="section">
      <view class="section-title">尺寸示例</view>
      <view class="size-grid">
        <view 
          v-for="(size, name) in iconSizes" 
          :key="name"
          class="size-item"
        >
          <CategoryIcon 
            name="PDF转换" 
            type="tool" 
            :size="name" 
            color="primary"
          />
          <text class="size-name">{{ name }}</text>
          <text class="size-value">{{ size }}px</text>
        </view>
      </view>
    </view>

    <!-- 使用说明 -->
    <view class="section">
      <view class="section-title">使用说明</view>
      <view class="usage-info">
        <text class="usage-text">• 点击图标可复制图标名称</text>
        <text class="usage-text">• 所有图标基于 uni-icons，无版权问题</text>
        <text class="usage-text">• 支持自定义颜色、尺寸和主题</text>
        <text class="usage-text">• 响应式设计，适配各种屏幕</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import CategoryIcon from '@/components/CategoryIcon.vue'
import { 
  CATEGORY_ICONS, 
  TOOL_ICONS, 
  STATUS_ICONS, 
  ACTION_ICONS,
  ICON_COLORS,
  ICON_SIZES
} from '@/constants/icons'

// 图标数据
const categoryIcons = CATEGORY_ICONS
const toolIcons = TOOL_ICONS
const statusIcons = STATUS_ICONS
const actionIcons = ACTION_ICONS
const iconColors = ICON_COLORS
const iconSizes = ICON_SIZES

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    success: '#34c759',
    error: '#ff3b30',
    warning: '#ff9500',
    info: '#5ac8fa',
    loading: '#007aff'
  }
  return colorMap[status] || '#666'
}

// 复制图标名称
const copyIconName = (iconName) => {
  // 在小程序中，可以使用 uni.setClipboardData
  uni.setClipboardData({
    data: iconName,
    success: () => {
      uni.showToast({
        title: `已复制: ${iconName}`,
        icon: 'none'
      })
    }
  })
}
</script>

<style lang="scss" scoped>
.icon-preview-page {
  padding: 32rpx;
  background: #f8f9fa;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 48rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 28rpx;
  color: #666;
  display: block;
}

.section {
  margin-bottom: 64rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 32rpx;
  padding-left: 16rpx;
  border-left: 8rpx solid #007aff;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200rpx, 1fr));
  gap: 24rpx;
}

.icon-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  }

  &:active {
    transform: translateY(-2rpx);
  }
}

.status-icon-wrapper,
.action-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.icon-name {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
  margin: 16rpx 0 8rpx;
  text-align: center;
}

.icon-type {
  font-size: 20rpx;
  color: #999;
  font-family: 'Monaco', 'Consolas', monospace;
  text-align: center;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160rpx, 1fr));
  gap: 24rpx;
}

.color-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.color-name {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
  margin: 16rpx 0 8rpx;
}

.color-value {
  font-size: 20rpx;
  color: #999;
  font-family: 'Monaco', 'Consolas', monospace;
}

.size-grid {
  display: flex;
  align-items: flex-end;
  gap: 32rpx;
  flex-wrap: wrap;
  justify-content: center;
}

.size-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.size-name {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
  margin: 16rpx 0 8rpx;
}

.size-value {
  font-size: 20rpx;
  color: #999;
  font-family: 'Monaco', 'Consolas', monospace;
}

.usage-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.usage-text {
  display: block;
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(160rpx, 1fr));
    gap: 16rpx;
  }

  .color-grid {
    grid-template-columns: repeat(auto-fill, minmax(120rpx, 1fr));
    gap: 16rpx;
  }

  .size-grid {
    gap: 24rpx;
  }
}
</style>
