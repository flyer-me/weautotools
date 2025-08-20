# 功能开关配置

## 配置文件
`src/config/features.js`

## 当前状态

| 功能 | 状态 | 说明 |
|------|------|------|
| 支付功能 (PAYMENT) | ❌ 禁用 | 微信个人开发者限制 |
| 交易功能 (TRADING) | ❌ 禁用 | 微信个人开发者限制 |
| 用户间聊天 (USER_CHAT) | ❌ 禁用 | 微信个人开发者限制 |
| 系统客服 (SYSTEM_SERVICE) | ✅ 启用 | 允许 |
| 产品展示 (GOODS_DISPLAY) | ✅ 启用 | 允许 |
| 订单查看 (ORDER_VIEW) | ✅ 启用 | 允许 |
| 订单操作 (ORDER_ACTIONS) | ❌ 禁用 | 微信个人开发者限制 |

## 技术实现

### 功能检查
```javascript
import { getFinalFeatureState } from '@/config/features'

if (getFinalFeatureState('PAYMENT')) {
  // 执行支付相关逻辑
}
```

### 条件渲染
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

## 快速恢复

修改 `src/config/features.js` 中对应功能的 `enabled` 属性为 `true`

```javascript
export const FEATURE_FLAGS = {
  PAYMENT: {
    enabled: true,  // 启用功能
    reason: '功能已恢复'
  }
}
```
