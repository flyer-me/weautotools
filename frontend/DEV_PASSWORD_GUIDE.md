# 开发者模式密码验证指南

## 功能概述

为了保护开发者功能，现在进入开发者模式需要密码验证。系统采用双重验证机制：
1. **API验证**：优先通过远程API验证密码
2. **本地验证**：API不可用时使用本地密码规则

## 密码验证流程

### 1. 触发验证
- 在用户页面连续点击版本号5次
- 弹出密码输入对话框

### 2. 密码验证
- 系统首先尝试调用API验证密码
- API地址：`https://api.weautomarket.com/v1/dev/verify`
- 请求方法：POST
- 请求体：`{ "password": "用户输入的密码" }`

### 3. 本地密码规则
当API不可用时，使用本地密码规则：
- 格式：当前日期8位数字
- 示例：如果今天是2024年1月15日，密码为：`20240115`

## 验证成功后

### 1. 状态保存
- 验证状态保存到本地存储
- 有效期：24小时
- 超时后需要重新验证

### 2. 功能启用
- 自动跳转到开发者设置页面
- 可以查看和修改功能开关状态
- 可以启用/禁用开发模式

## 开发者设置页面功能

### 1. 验证状态显示
- 当前验证状态（已验证/未验证）
- 验证时间
- 剩余有效时间

### 2. 开发模式控制
- 启用/禁用开发模式开关
- 查看所有功能开关状态
- 查看当前生效状态

### 3. 安全操作
- 退出开发者模式
- 重置为生产模式

## API接口规范

### 验证接口
```
POST https://api.weautomarket.com/v1/dev/verify
Content-Type: application/json

{
  "password": "用户输入的密码"
}
```

### 成功响应
```json
{
  "success": true,
  "message": "验证成功"
}
```

### 失败响应
```json
{
  "success": false,
  "message": "密码错误"
}
```

## 本地密码生成规则

```javascript
// 生成当前日期的本地密码
function generateLocalPassword() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}${month}${day}`
}

// 示例
// 2024年1月15日 -> 20240115
// 2024年12月31日 -> 20241231
```

## 安全特性

### 1. 超时机制
- 验证有效期：24小时
- 超时后自动清除验证状态
- 需要重新输入密码验证

### 2. 本地存储加密
- 验证状态保存在本地存储中
- 包含时间戳用于验证有效期

### 3. API优先策略
- 优先使用远程API验证
- 确保密码的安全性和可控性
- 本地密码仅作为备用方案

## 使用示例

### 1. 正常验证流程
1. 用户页面连续点击版本号5次
2. 弹出密码输入框
3. 输入正确密码（API验证或本地密码）
4. 验证成功，跳转到开发者设置页面
5. 启用开发模式，功能状态可查看

### 2. 密码错误处理
1. 输入错误密码
2. 显示错误提示
3. 清空输入框，重新聚焦
4. 可以重新输入

### 3. 验证过期处理
1. 24小时后验证自动过期
2. 开发模式自动禁用
3. 需要重新验证密码

## 开发调试

### 1. 查看当前本地密码
在浏览器控制台执行：
```javascript
import { generateLocalPassword } from '@/config/features'
console.log('当前本地密码:', generateLocalPassword())
```

### 2. 手动清除验证状态
```javascript
uni.removeStorageSync('dev_mode_auth')
```

### 3. 检查验证状态
```javascript
const stored = uni.getStorageSync('dev_mode_auth')
console.log('验证状态:', stored)
```

## 注意事项

1. **生产环境**：确保API接口在生产环境中正常工作
2. **密码安全**：不要在代码中硬编码密码
3. **时区问题**：本地密码基于设备本地时间生成
4. **网络问题**：API不可用时会自动降级到本地验证
5. **存储清理**：应用卸载时会自动清除验证状态
