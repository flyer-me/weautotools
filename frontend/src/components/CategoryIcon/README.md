# CategoryIcon 组件使用指南

## 概述

CategoryIcon 是专为Category页面设计的图标组件。基于 uni-icons。

## 基础用法

### 工具图标
```vue
<CategoryIcon 
  name="PDF转换" 
  type="tool" 
  size="md" 
  color="primary"
/>
```

### 分类图标
```vue
<CategoryIcon 
  name="PDF与文档" 
  type="category" 
  size="lg" 
  color="primary"
/>
```

### 自定义图标
```vue
<CategoryIcon 
  name="search" 
  type="custom" 
  size="sm" 
  color="gray"
/>
```

## Props 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | - | 图标名称或类型（必填） |
| type | String | 'tool' | 图标类型：category/tool/custom |
| size | String | 'md' | 图标尺寸：xs/sm/md/lg/xl/xxl |
| color | String | 'primary' | 图标颜色：primary/secondary/success/warning/error/info/gray |
| theme | String | 'light' | 主题：light/dark |
| showBackground | Boolean | true | 是否显示背景 |
| clickable | Boolean | false | 是否可点击 |
| customStyle | Object | {} | 自定义样式 |

## 尺寸规格

| 尺寸 | 像素值 | 容器大小 | 使用场景 |
|------|--------|----------|----------|
| xs | 16px | 48rpx | 小图标、状态指示 |
| sm | 20px | 64rpx | 列表项图标 |
| md | 24px | 80rpx | 搜索结果图标 |
| lg | 32px | 96rpx | 分类工具图标 |
| xl | 48px | 128rpx | 大型展示图标 |
| xxl | 64px | 160rpx | 特大展示图标 |

## 颜色主题

### 预设颜色
- **primary**: #007aff (主色调)
- **secondary**: #5856d6 (次要色)
- **success**: #34c759 (成功色)
- **warning**: #ff9500 (警告色)
- **error**: #ff3b30 (错误色)
- **info**: #5ac8fa (信息色)
- **gray**: #8e8e93 (灰色)

### 主题模式
- **light**: 浅色主题，适合日间使用
- **dark**: 深色主题，适合夜间使用

## 图标映射

### 分类图标
- PDF与文档 → paperclip
- 图片工具 → images
- 文件转换 → loop
- 数据工具 → bars
- 二维码工具 → scan

### 工具图标
- PDF转换 → refresh
- PDF压缩 → download
- PDF合并 → plus
- PDF拆分 → minus
- 图片转PDF → image
- OCR文字识别 → eye
- 图片压缩 → download-filled
- 格式转换 → refresh-filled
- 批量加水印 → color
- 批量重命名 → compose
- 文档格式转换 → paperclip
- 电子书转换 → contact
- JSON转换 → gear
- 二维码生成 → plus-filled
- 二维码识别 → search

## 高级用法

### 可点击图标
```vue
<CategoryIcon 
  name="PDF转换" 
  type="tool" 
  :clickable="true"
  @click="handleIconClick"
/>
```

### 自定义样式
```vue
<CategoryIcon 
  name="PDF转换" 
  type="tool" 
  :custom-style="{
    backgroundColor: '#f0f0f0',
    borderRadius: '20rpx'
  }"
/>
```

### 无背景图标
```vue
<CategoryIcon 
  name="PDF转换" 
  type="tool" 
  :show-background="false"
/>
```

## 事件

### @click
图标点击事件（需要设置 clickable 为 true）

```vue
<CategoryIcon 
  name="PDF转换" 
  type="tool" 
  :clickable="true"
  @click="handleClick"
/>

<script>
export default {
  methods: {
    handleClick(data) {
      console.log('点击图标:', data)
      // data: { name, type, iconType }
    }
  }
}
</script>
```

## 样式定制

### CSS 变量
```scss
.category-icon {
  --icon-bg-color: #f0f8ff;
  --icon-border-color: #e6f4ff;
  --icon-hover-bg: #e6f4ff;
  --icon-hover-border: #bae0ff;
}
```

### 自定义动画
```scss
.category-icon.custom-animation {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: rotate(360deg) scale(1.2);
  }
}
```

## 扩展图标

### 添加新图标
1. 在 `constants/icons.js` 中添加图标映射
2. 确保使用 uni-icons 支持的图标类型
3. 更新类型定义文件

### 自定义图标库
如需使用自定义图标，可以通过 fontFamily 属性：

```vue
<uni-icons 
  fontFamily="CustomFont" 
  :size="24"
>
  {{'\ue601'}}
</uni-icons>
```