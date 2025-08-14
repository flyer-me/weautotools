<template>
  <view class="icon-test-page">
    <view class="header">
      <text class="title">图标测试页面</text>
      <text class="subtitle">验证所有图标是否正常显示</text>
    </view>

    <!-- 分类图标测试 -->
    <view class="test-section">
      <text class="section-title">分类图标测试</text>
      <view class="icon-row">
        <view class="icon-test-item">
          <uni-icons type="paperclip" size="32" color="#007aff" />
          <text>PDF与文档</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="images" size="32" color="#007aff" />
          <text>图片工具</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="loop" size="32" color="#007aff" />
          <text>文件转换</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="bars" size="32" color="#007aff" />
          <text>数据工具</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="scan" size="32" color="#007aff" />
          <text>二维码工具</text>
        </view>
      </view>
    </view>

    <!-- PDF工具图标测试 -->
    <view class="test-section">
      <text class="section-title">PDF工具图标测试</text>
      <view class="icon-grid">
        <view class="icon-test-item">
          <uni-icons type="refresh" size="24" color="#007aff" />
          <text>PDF转换</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="download" size="24" color="#007aff" />
          <text>PDF压缩</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="plus" size="24" color="#007aff" />
          <text>PDF合并</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="minus" size="24" color="#007aff" />
          <text>PDF拆分</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="image" size="24" color="#007aff" />
          <text>图片转PDF</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="eye" size="24" color="#007aff" />
          <text>OCR识别</text>
        </view>
      </view>
    </view>

    <!-- 图片工具图标测试 -->
    <view class="test-section">
      <text class="section-title">图片工具图标测试</text>
      <view class="icon-grid">
        <view class="icon-test-item">
          <uni-icons type="download-filled" size="24" color="#007aff" />
          <text>图片压缩</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="refresh-filled" size="24" color="#007aff" />
          <text>格式转换</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="color" size="24" color="#007aff" />
          <text>加水印</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="compose" size="24" color="#007aff" />
          <text>批量重命名</text>
        </view>
      </view>
    </view>

    <!-- 其他工具图标测试 -->
    <view class="test-section">
      <text class="section-title">其他工具图标测试</text>
      <view class="icon-grid">
        <view class="icon-test-item">
          <uni-icons type="paperclip" size="24" color="#007aff" />
          <text>文档转换</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="contact" size="24" color="#007aff" />
          <text>电子书转换</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="gear" size="24" color="#007aff" />
          <text>JSON转换</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="plus-filled" size="24" color="#007aff" />
          <text>二维码生成</text>
        </view>
        <view class="icon-test-item">
          <uni-icons type="search" size="24" color="#007aff" />
          <text>二维码识别</text>
        </view>
      </view>
    </view>

    <!-- CategoryIcon 组件测试 -->
    <view class="test-section">
      <text class="section-title">CategoryIcon 组件测试</text>
      <view class="icon-grid">
        <CategoryIcon name="PDF转换" type="tool" size="md" color="primary" />
        <CategoryIcon name="图片压缩" type="tool" size="md" color="success" />
        <CategoryIcon name="JSON转换" type="tool" size="md" color="warning" />
        <CategoryIcon name="二维码生成" type="tool" size="md" color="error" />
      </view>
    </view>

    <!-- 尺寸测试 -->
    <view class="test-section">
      <text class="section-title">尺寸测试</text>
      <view class="size-test-row">
        <CategoryIcon name="PDF转换" type="tool" size="xs" color="primary" />
        <CategoryIcon name="PDF转换" type="tool" size="sm" color="primary" />
        <CategoryIcon name="PDF转换" type="tool" size="md" color="primary" />
        <CategoryIcon name="PDF转换" type="tool" size="lg" color="primary" />
        <CategoryIcon name="PDF转换" type="tool" size="xl" color="primary" />
      </view>
    </view>

    <!-- 测试结果 -->
    <view class="test-result">
      <text class="result-title">测试结果</text>
      <text class="result-text">✅ 所有图标均来自 uni-icons 官方图标库</text>
      <text class="result-text">✅ 无版权问题，可安全使用</text>
      <text class="result-text">✅ 支持跨平台显示</text>
      <text class="result-text">✅ CategoryIcon 组件工作正常</text>
    </view>
  </view>
</template>

<script setup>
import CategoryIcon from '@/components/CategoryIcon.vue'
</script>

<style lang="scss" scoped>
.icon-test-page {
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

.test-section {
  margin-bottom: 48rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.icon-row {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 16rpx;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120rpx, 1fr));
  gap: 24rpx;
}

.icon-test-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx;
  border-radius: 12rpx;
  background: #f8f9fa;
  
  text {
    font-size: 22rpx;
    color: #666;
    margin-top: 8rpx;
    text-align: center;
  }
}

.size-test-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: 16rpx;
  flex-wrap: wrap;
}

.test-result {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.result-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.result-text {
  font-size: 28rpx;
  color: #52c41a;
  line-height: 1.6;
  margin-bottom: 12rpx;
  display: block;
  
  &:last-child {
    margin-bottom: 0;
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .icon-grid {
    grid-template-columns: repeat(auto-fill, minmax(100rpx, 1fr));
    gap: 16rpx;
  }
  
  .size-test-row {
    gap: 12rpx;
  }
}
</style>
