# WeAutoTools 前端

基于 UniApp + Vue3 的自动化工具平台

## 快速开始

```bash
npm install
npm run dev:mp-weixin
```

## 项目结构

```
frontend/src/
├── api/                    # API接口管理
│   ├── index.js            # 统一的API接口定义
│   ├── message.js          # 消息相关API
│   ├── order.js            # 订单相关API
│   ├── tools.js            # 工具相关API
│   └── user.js             # 用户相关API
├── components/             # 通用组件
│   ├── CategoryIcon.vue    # 分类图标组件
│   ├── DevPasswordModal.vue # 开发者密码模态框
│   ├── EmptyState.vue      # 空状态组件
│   ├── GoodsCard.vue       # 商品卡片组件
│   ├── LoadingState.vue    # 加载状态组件
│   ├── MessageItem.vue     # 消息项组件
│   └── TabBar.vue          # 底部导航栏组件
├── composables/            # 组合式API
│   ├── useBadge.js         # 徽章状态管理
│   ├── useMessage.js       # 消息状态管理
│   ├── useOrder.js         # 订单状态管理
│   ├── useUsageLimit.js    # 使用限制管理
│   └── useUser.ts          # 用户状态管理
├── config/                 # 配置文件
│   ├── env.ts              # 环境配置
│   ├── features.ts         # 功能开关配置
│   ├── index.ts            # 配置入口文件
│   └── SECURITY.md         # 安全配置说明
├── constants/              # 常量定义
│   ├── icons.js            # 图标常量
│   └── index.js            # 项目常量（状态、路由等）
├── features/               # 特性模块（按功能划分）
│   └── tools/              # 工具相关功能模块
│       ├── base/           # 基础工具类
│       ├── data/           # 数据处理工具
│       ├── image/          # 图片处理工具
│       ├── qrcode/         # 二维码工具
│       ├── shared/         # 工具模块共享组件和函数
│       └── README.md       # 工具模块说明文档
├── mock/                   # 模拟数据
│   └── index.js            # 统一的模拟数据管理
├── pages/                  # 页面组件
│   ├── index/              # 首页
│   ├── category/           # 分类页
│   ├── message/            # 消息页
│   ├── user/               # 用户页
│   ├── search/             # 搜索页
│   ├── chat-detail/        # 聊天详情
│   ├── dev-settings/       # 开发设置页
│   ├── goods-detail/       # 商品详情页
│   ├── order-list/         # 订单列表页
│   └── tools/              # 工具页面
├── router/                 # 路由配置
│   └── index.js            # 路由定义
├── services/               # 服务层
│   └── authService.ts      # 认证服务
├── static/                 # 静态资源
├── styles/                 # 样式文件
│   └── tools-common.scss   # 工具模块通用样式
├── types/                  # TypeScript 类型定义
│   └── category.ts         # 分类相关类型定义
├── utils/                  # 工具函数
│   ├── categoryUtils.ts    # 分类工具函数
│   ├── downloadUtils.js    # 下载工具函数
│   ├── errorHandler.js     # 错误处理工具
│   ├── fileUtils.ts        # 文件处理工具
│   ├── orderStatus.js      # 订单状态配置
│   ├── request.js          # 请求工具函数
│   ├── toast.js            # 提示工具函数
│   └── tools-common.js     # 工具模块通用函数
├── App.vue                 # 应用根组件
├── main.ts                 # 应用入口
├── pages.json              # 页面配置
└── uni.scss                # 全局样式
```