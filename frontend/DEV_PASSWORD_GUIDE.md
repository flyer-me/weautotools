# 开发者模式密码验证

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

```

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