# API 设计与 OpenAPI 草稿（初稿）

## 说明
本节以文本方式列出关键 API（顾客/商家/支付/计费/结算）。后续可基于此生成 OpenAPI (YAML/JSON)。所有响应遵循统一 `Result<T>` 结构，错误码详见末尾。

## 认证
- 使用 OAuth2 Bearer Token
- 管理端接口需额外角色校验（ROLE_ADMIN / ROLE_MERCHANT）

## 顾客相关
### GET /api/v1/wallet
- 描述：查询钱包余额
- 权限：用户
- 返回：{ balance, frozen_amount, currency }

### POST /api/v1/payments/recharge
- 描述：创建充值订单
- 权限：用户
- 请求体：{ amount, currency, payment_method, idempotency_key, callback_url }
- 返回：{ recharge_order_id, payment_payload }

### POST /api/v1/payments/recharge/callback
- 描述：第三方回调处理
- 权限：公开（需签名验证）
- 返回：第三方指定格式或 200

### POST /api/v1/orders
- 描述：创建支付订单
- 权限：用户
- 请求体：{ items, total_amount, pay_method, wallet_amount, idempotency_key }
- 返回：{ order_id, status, payment_payload? }

### GET /api/v1/wallet/transactions
- 描述：查询流水，支持分页与筛选
- 权限：用户

### POST /api/v1/refunds
- 描述：发起退款
- 权限：用户或商家（具体视场景）

## 商家相关
### POST /api/v1/merchants/apply
- 描述：商家入驻申请
- 权限：公开登录

### POST /api/v1/merchants/{id}/products
- 描述：创建商品
- 权限：商家
- 请求体包含 pricing_rule JSON

### POST /api/v1/merchants/{id}/products/{pid}/publish
- 描述：上架商品
- 权限：商家

### GET /api/v1/merchants/{id}/orders
- 描述：商家查询订单
- 权限：商家

## 管理端
### GET /api/v1/admin/merchants
- 描述：平台查看/审核商家
- 权限：管理员

### GET /api/v1/admin/finance/merchant_statements
- 描述：导出结算单/对账
- 权限：管理员/财务


## 后续
- 基于此生成 OpenAPI YAML，并与前端对接 mock server
- 为关键接口设计示例请求/响应与示例数据
