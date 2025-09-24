## 🔴 红点徽章

### 功能特点

1. **统一管理**: 使用 `useBadge` 组合式API统一管理所有红点状态
2. **智能显示**: 支持数字徽章和小红点两种显示模式

### 使用方式

#### 1. 在组件中使用红点

```javascript
import { useGlobalBadge } from '@/composables/useBadge'

export default {
  setup() {
    const { tabBarBadges, getBadgeCount } = useGlobalBadge()
    
    return {
      tabBarBadges,
      getBadgeCount
    }
  }
}
```

#### 2. 更新红点数量

```javascript
const { updateMessageBadge, updateOrderBadge } = useGlobalBadge()

// 更新消息红点
updateMessageBadge('chat', 5)

// 更新订单红点
updateOrderBadge('pending', 3)
```

#### 3. 清除红点

```javascript
const { clearBadge } = useGlobalBadge()

// 清除所有消息红点
clearBadge('message')

// 清除特定类型的红点
clearBadge('message', 'chat')
```

### 红点类型

#### 消息红点
- `total`: 总未读消息数
- `chat`: 聊天消息未读数
- `system`: 系统消息未读数
- `service`: 客服消息未读数
- `order`: 订单消息未读数

#### 订单红点
- `pending`: 待付款订单数
- `processing`: 待交付订单数
- `shipping`: 待收货订单数
- `refunding`: 退款中订单数

##  样式规范

### 红点样式

```scss
.tab-badge {
  position: absolute;
  top: -8rpx;
  right: -12rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 18rpx;
  font-weight: bold;
  border-radius: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  animation: badge-pulse 2s infinite;
  
  &--dot {
    min-width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    animation: dot-pulse 2s infinite;
  }
}
```

### 动画效果

```scss
@keyframes badge-pulse {
  0%, 100% { transform: scale(0.9); }
  50% { transform: scale(1); }
}

@keyframes dot-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}
```

##  开发指南

### 添加新的红点类型

1. 在 `constants/index.js` 中定义新的常量
2. 在 `useBadge.js` 中添加对应的状态管理
3. 在相关组件中使用新的红点类型
