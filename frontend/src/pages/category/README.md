# 工具分类页面

## 功能概述

提供自动化工具的分类浏览和搜索功能。

## 工具分类

### PDF与文档处理
- PDF转换、压缩、合并、拆分
- 图片转PDF、OCR文字识别

### 图片工具
- 图片压缩、格式转换
- 批量加水印、批量重命名

### 文件转换
- 文档格式转换（Word/Excel/PPT）
- 电子书转换（PDF/EPUB/MOBI）

### 数据工具
- JSON/XML/YAML格式互转

### 二维码工具
- 二维码生成和识别

## 技术特性

- 🔍 实时搜索（300ms防抖）
- 📱 响应式设计
- 🚀 性能优化（懒加载、虚拟滚动）

## 开发指南

### 添加新工具
在 `categories` 数组中添加工具配置：
```javascript
{
  name: '工具名称',
  img: '/static/category/tool-icon.png',
  desc: '工具描述',
  keywords: ['关键词1', '关键词2'],
  route: '/pages/tools/tool-page'
}
```
