# 数据模型与数据库迁移脚本草案（初稿）

## 概述
本节提供核心 ER 要素与主要表 DDL 片段，覆盖用户、商家、商品、订单、流水、充值与结算相关表。

> 说明：金额字段建议以分为单位的 bigint 存储；时间字段使用 timestamptz；所有表包含 created_at/updated_at/deleted/version 字段用于审计与乐观锁。

## 主要表 DDL 片段

1. wallet_account
```sql
CREATE TABLE wallet_account (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  balance BIGINT NOT NULL DEFAULT 0,
  frozen_amount BIGINT NOT NULL DEFAULT 0,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);
```

2. wallet_transaction
```sql
CREATE TABLE wallet_transaction (
  id BIGSERIAL PRIMARY KEY,
  wallet_id BIGINT NOT NULL REFERENCES wallet_account(id),
  user_id BIGINT NOT NULL,
  tx_type VARCHAR(32) NOT NULL,
  amount BIGINT NOT NULL,
  balance_after BIGINT NOT NULL,
  related_order_id BIGINT,
  biz_no VARCHAR(128),
  status VARCHAR(32) NOT NULL,
  remark TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
```

3. recharge_order
```sql
CREATE TABLE recharge_order (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount BIGINT NOT NULL,
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  payment_method VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  third_party_txn VARCHAR(128),
  idempotency_key VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
```

4. payment_order (消费/下单)
```sql
CREATE TABLE payment_order (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  merchant_id BIGINT,
  total_amount BIGINT NOT NULL,
  pay_method VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  refunded_amount BIGINT DEFAULT 0,
  idempotency_key VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  paid_at TIMESTAMP WITH TIME ZONE
);
```

5. refund_order
```sql
CREATE TABLE refund_order (
  id BIGSERIAL PRIMARY KEY,
  related_order_id BIGINT NOT NULL,
  amount BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  idempotency_key VARCHAR(128),
  third_party_txn VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
```

6. merchant_account
```sql
CREATE TABLE merchant_account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  contact JSONB,
  settlement_info JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
```

7. product_item
```sql
CREATE TABLE product_item (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchant_account(id),
  title VARCHAR(255) NOT NULL,
  description TEXT,
  price BIGINT NOT NULL,
  billing_type VARCHAR(32) NOT NULL,
  pricing_rule JSONB,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  stock BIGINT DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
```

8. merchant_statement
```sql
CREATE TABLE merchant_statement (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  total_income BIGINT NOT NULL,
  platform_commission BIGINT NOT NULL,
  payable_amount BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
```

## 后续
- 生成 Flyway/Liquibase 的 migration 文件
- 补充索引、外键约束和分区策略（如需要）
- 为关键金额字段添加 CHECK 约束（>=0）与触发器（审计）
