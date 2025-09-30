# 顾客相关功能规范（初稿）

## 概述
为顾客提供账户体系、钱包（余额/冻结）、充值、消费、退款、流水查询、积分与等级等功能。强调幂等、并发安全、审计与对账能力。与现有 OAuth2 身份系统集成。

## 核心用例
- 注册/登录（OAuth2）
- 充值（第三方/线下/平台）与回调处理
- 下单消费（钱包支付/混合支付）
- 退款（全额/部分）
- 查询余额与流水（分页、筛选）
- 积分累计与等级变更（后续迭代）

## 关键数据模型（概要）
- wallet_account：user_id, balance, frozen_amount, version
- wallet_transaction：金额、类型、关联订单、biz_no、状态、balance_after
- recharge_order / payment_order / refund_order：订单状态、idempotency_key、第三方交易号
- user_points / user_level：积分、等级

## API 概要
- GET /api/v1/wallet — 查询余额
- POST /api/v1/payments/recharge — 创建充值订单
- POST /api/v1/payments/recharge/callback — 第三方回调处理
- POST /api/v1/orders — 创建支付订单（支持 WALLET/MIXED）
- GET /api/v1/wallet/transactions — 查询流水
- POST /api/v1/refunds — 发起退款

## 一致性与安全
- 写类操作要求 Idempotency-Key 支持
- 并发更新使用乐观锁或 SELECT FOR UPDATE
- 所有变更写入审计日志（前后余额、操作者、时间）

## 验收要点
- 幂等测试：重复相同 idempotency 请求不产生多次扣款或入账
- 并发测试：并发扣款不会导致余额负值
- 回调测试：模拟第三方回调，账务正确

## 后续细化项
- SQL DDL 草案
- OpenAPI 定义
- 自动化测试（并发、回归）
- 积分兑换与会员规则
