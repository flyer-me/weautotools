# 开发者模式安全配置

## 基本配置

- **触发方式**：连续点击版本号5次
- **密码格式**：当前日期8位数字（如：20240115）
- **验证方式**：API优先，本地备用
- **有效期**：24小时

## 密码生成

```javascript
// 生成当日密码
export const generateLocalPassword = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}${month}${day}`
}
```

## 功能限制处理

```javascript
// 禁用功能点击处理
export const handleDisabledFeatureClick = (featureName) => {
  const feature = FEATURE_FLAGS[featureName]
  const reason = feature ? feature.reason : '功能未定义'
  
  uni.showModal({
    title: '功能限制',
    content: `${reason}\n\n当前未开放此功能。`,
    showCancel: false,
    confirmText: '我知道了'
  })
}
```

## 安全原则

- 不在UI中显示密码规则
- 禁用功能统一显示"当前未开放"
- 验证状态自动过期清理
