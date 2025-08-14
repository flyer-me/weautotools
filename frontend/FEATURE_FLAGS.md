# 功能开关说明文档

## 概述

为了符合微信个人开发者的审核要求，本项目实现了功能开关机制，可以灵活控制各种功能的启用/禁用状态。

## 功能开关配置

配置文件位置：`src/config/features.js`

### 当前功能开关状态

| 功能 | 状态 | 说明 |
|------|------|------|
| 支付功能 (PAYMENT) | ❌ 禁用 | 微信个人开发者限制 - 不允许支付功能 |
| 交易功能 (TRADING) | ❌ 禁用 | 微信个人开发者限制 - 不允许交易功能 |
| 用户间聊天 (USER_CHAT) | ❌ 禁用 | 微信个人开发者限制 - 不允许用户间沟通 |
| 系统客服 (SYSTEM_SERVICE) | ✅ 启用 | 符合微信个人开发者要求 |
| 商品展示 (GOODS_DISPLAY) | ✅ 启用 | 符合微信个人开发者要求 |
| 订单查看 (ORDER_VIEW) | ✅ 启用 | 符合微信个人开发者要求 |
| 订单操作 (ORDER_ACTIONS) | ❌ 禁用 | 微信个人开发者限制 - 不允许交易操作 |

## 受影响的功能

### 1. 支付相关功能
- **影响页面**：商品详情页、订单列表页
- **具体表现**：
  - 商品详情页的"立即购买"按钮显示为"功能暂不可用"
  - 订单列表页的"立即支付"按钮被隐藏
  - 支付页面路由被拦截

### 2. 交易相关功能
- **影响页面**：首页、商品详情页、订单列表页
- **具体表现**：
  - 首页商品卡片价格显示为"仅供展示"
  - 商品详情页购买按钮被禁用
  - 订单操作按钮（取消、确认、评价等）被隐藏

### 3. 用户间沟通功能
- **影响页面**：消息页、聊天详情页
- **具体表现**：
  - 消息页面隐藏"聊天"分类Tab
  - 过滤掉用户间聊天消息，仅保留系统客服消息
  - 聊天页面限制用户间发送消息

### 4. 订单功能
- **影响页面**：用户页面、订单列表页
- **具体表现**：
  - 用户页面显示订单查看功能，但隐藏操作按钮
  - 订单列表页可查看订单，但无法进行操作

## 开发模式

### 启用开发模式
1. 在用户页面连续点击版本号5次
2. 输入开发者密码进行验证
3. 进入开发者设置页面
4. 开启"启用开发模式"开关

### 开发模式效果（已修改）
- **禁用限制功能**：开发模式下会禁用所有受限功能
- **保留功能列表**：功能按钮仍然显示，但标记为禁用状态
- **点击提示**：点击禁用的功能会显示限制原因
- **便于测试**：可以查看完整的功能列表和限制提示

### 开发模式密码验证
- **API验证优先**：首先尝试通过API验证密码
- **本地密码备用**：API不可用时使用本地密码（当前日期8位数字）
- **验证有效期**：24小时，过期后需重新验证

### 重置为生产模式
在开发者设置页面点击"重置为生产模式"按钮

## 快速恢复功能

### 方法一：修改配置文件
编辑 `src/config/features.js` 文件，将需要启用的功能的 `enabled` 属性设置为 `true`：

```javascript
export const FEATURE_FLAGS = {
  PAYMENT: {
    enabled: true,  // 改为 true 启用功能
    reason: '功能已恢复'
  },
  // ... 其他功能
}
```

### 方法二：修改开发模式配置
编辑 `src/config/features.js` 文件中的开发模式配置：

```javascript
export const DEV_MODE = {
  enabled: false,
  overrides: {
    PAYMENT: { enabled: true },     // 改为 true 启用支付功能
    TRADING: { enabled: true },     // 改为 true 启用交易功能
    USER_CHAT: { enabled: true },   // 改为 true 启用用户聊天
    ORDER_ACTIONS: { enabled: true } // 改为 true 启用订单操作
  }
}
```

### 方法三：完全禁用功能开关
将所有 `FEATURE_FLAGS` 中的功能设置为 `enabled: true`

## 技术实现

### 1. 功能检查
```javascript
import { getFinalFeatureState } from '@/config/features'

// 检查功能是否启用
if (getFinalFeatureState('PAYMENT')) {
  // 执行支付相关逻辑
}
```

### 2. 条件渲染
```vue
<template>
  <button v-if="getFinalFeatureState('TRADING')" @click="handleBuy">
    立即购买
  </button>
  <button v-else @click="showFeatureDisabledToast('TRADING')">
    功能暂不可用
  </button>
</template>
```

### 3. 路由拦截
```javascript
// 在 router.js 中自动拦截被禁用的路由
static navigateTo(url, params = {}, options = {}) {
  if (isRouteDisabled(url)) {
    return Promise.reject(handleDisabledRoute(url))
  }
  // ... 正常跳转逻辑
}
```

## 注意事项

1. **生产环境**：确保 `DEV_MODE.enabled` 为 `false`
2. **审核前检查**：确认所有受限功能均已正确隐藏
3. **功能恢复**：审核通过后可通过修改配置文件快速恢复功能
4. **测试验证**：使用开发模式验证功能恢复的正确性

## 相关文件

- `src/config/features.js` - 功能开关配置
- `src/pages/dev-settings/dev-settings.vue` - 开发者设置页面
- `src/utils/router.js` - 路由拦截逻辑
- `frontend/FEATURE_FLAGS.md` - 本说明文档

## 联系方式

如有问题，请联系开发团队。
