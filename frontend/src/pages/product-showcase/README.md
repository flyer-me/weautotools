# 产品展示页面 (Product Showcase)

## 📋 页面说明

这是一个产品展示页面，原名为 `index.vue`，因微信小程序个人开发者审核规则而暂时隐藏。该页面主要用于展示平台的自动化工具产品。

## 🚫 隐藏原因

根据微信小程序个人开发者审核要求：
- 个人开发者不允许涉及商业交易功能
- 不能显示价格信息和购买按钮
- 需要避免商城类页面布局

## 🎯 页面功能

### 主要功能
1. **产品展示** - 展示各类自动化工具
2. **分类浏览** - 按类别查看不同工具
3. **搜索功能** - 快速查找所需工具
4. **轮播推荐** - 展示精选内容

### 功能开关适配
- 使用 `getFinalFeatureState('TRADING')` 控制价格显示
- 交易功能禁用时显示"仅供展示"
- 保持页面布局完整性

## 🔧 技术实现

### 组件结构
```
product-showcase.vue
├── 搜索栏 (search-bar)
├── 轮播图 (banner-swiper)  
├── 分类标签 (tab-bar)
├── 产品列表 (goods-list)
└── 底部导航 (TabBar)
```

### 数据结构
```javascript
// 产品数据
const goodsMap = [
  [精选推荐],
  [办公助手], 
  [Web自动化],
  [人气榜]
]

// 轮播数据
const banners = [
  { img, title, url }
]
```

## 📱 页面配置

### pages.json 配置
```json
{
  "path": "pages/product-showcase/product-showcase",
  "style": {
    "navigationBarTitleText": "产品展示",
    "enablePullDownRefresh": false
  }
}
```

### 路由配置
```javascript
// constants/index.js
PRODUCT_SHOWCASE: '/pages/product-showcase/product-showcase'

// utils/router.js  
toProductShowcase: () => Router.navigateTo(ROUTES.PRODUCT_SHOWCASE)
```

## 🎨 样式特点

### 设计风格
- 简洁现代的卡片式布局
- 响应式网格系统
- 统一的视觉规范

### 关键样式
- 搜索栏：圆角输入框 + 图标按钮
- 轮播图：自动播放 + 指示器
- 产品卡片：图片 + 信息 + 标签
- 分类标签：下划线指示器

## 🔄 状态管理

### 响应式数据
```javascript
const searchKeyword = ref('')     // 搜索关键词
const activeTab = ref(0)          // 当前分类
const goodsList = computed(...)   // 当前产品列表
```

### 事件处理
- `handleSearchFocus` - 搜索框聚焦
- `handleGoodsClick` - 产品点击
- `handleBannerClick` - 轮播图点击

## 🚀 使用方式

### 开发环境
```bash
# 在 pages.json 中添加页面配置
# 在路由中添加跳转方法
# 在功能开关中控制显示状态
```

### 生产环境
```bash
# 通过功能开关控制页面访问
# 确保符合审核要求
# 隐藏商业化元素
```

## ⚠️ 注意事项

### 审核合规
1. **价格显示** - 根据功能开关控制
2. **交易按钮** - 完全隐藏或禁用
3. **商业用词** - 避免使用"购买"、"下单"等

### 功能限制
1. **支付功能** - 完全禁用
2. **订单功能** - 仅展示，不可操作
3. **用户交互** - 限制商业化交互

### 开发建议
1. **保持代码** - 便于功能恢复
2. **文档完善** - 记录隐藏原因
3. **测试覆盖** - 确保功能完整

## 📝 更新日志

### v1.0.0 (2024-01-15)
- 创建产品展示页面
- 实现基础功能模块
- 添加功能开关支持

### v1.1.0 (当前)
- 重命名为 product-showcase
- 添加审核合规说明
- 完善文档和注释

## 🔮 未来规划

### 短期计划
- 优化页面性能
- 增强用户体验
- 完善错误处理

### 长期规划
- 支持更多产品类型
- 增加个性化推荐
- 集成数据分析

当审核政策允许时，可以快速恢复该页面的完整功能。
