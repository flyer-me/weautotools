# WeAutoTools 前端

基于 UniApp + Vue3 的自动化工具平台

## 快速开始

```bash
npm install
npm run dev:mp-weixin
```
frontend/src/
├── api/                    # API接口管理
│   └── index.js           # 统一的API接口定义
├── components/            # 通用组件
├── composables/          # 组合式API
├── constants/            # 常量定义
│   └── index.js         # 项目常量（状态、路由等）
├── mock/                 # 模拟数据
│   └── index.js         # 统一的模拟数据管理
├── pages/               # 页面组件
│   ├── index/           # 首页
│   ├── category/        # 分类页
│   ├── message/         # 消息页
│   ├── user/            # 用户页
│   ├── search/          # 搜索页
│   └── chat-detail/     # 聊天详情
├── static/              # 静态资源
├── utils/               # 工具函数
│   ├── router.js        # 路由管理
│   └── orderStatus.js   # 订单状态配置
├── App.vue              # 应用根组件
├── main.js              # 应用入口
├── pages.json           # 页面配置
└── uni.scss             # 全局样式
```
