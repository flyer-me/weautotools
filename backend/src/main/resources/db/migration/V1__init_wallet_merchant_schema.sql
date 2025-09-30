
CREATE TABLE wallet_account (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  balance BIGINT NOT NULL DEFAULT 0 CHECK (balance >= 0),
  frozen_amount BIGINT NOT NULL DEFAULT 0 CHECK (frozen_amount >= 0),
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_wallet_account_user_id ON wallet_account(user_id);

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
CREATE INDEX idx_wallet_transaction_user_id ON wallet_transaction(user_id);
CREATE INDEX idx_wallet_transaction_tx_type ON wallet_transaction(tx_type);
CREATE UNIQUE INDEX uidx_wallet_transaction_biz_no ON wallet_transaction(biz_no) WHERE biz_no IS NOT NULL;

CREATE TABLE recharge_order (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount BIGINT NOT NULL CHECK (amount > 0),
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  payment_method VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  third_party_txn VARCHAR(128),
  idempotency_key VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX uidx_recharge_order_idempotency_key ON recharge_order(idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE TABLE payment_order (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  merchant_id BIGINT,
  total_amount BIGINT NOT NULL CHECK (total_amount >= 0),
  pay_method VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  refunded_amount BIGINT DEFAULT 0,
  idempotency_key VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  paid_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX uidx_payment_order_idempotency_key ON payment_order(idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE TABLE refund_order (
  id BIGSERIAL PRIMARY KEY,
  related_order_id BIGINT NOT NULL,
  amount BIGINT NOT NULL CHECK (amount > 0),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  idempotency_key VARCHAR(128),
  third_party_txn VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX uidx_refund_order_idempotency_key ON refund_order(idempotency_key) WHERE idempotency_key IS NOT NULL;

CREATE TABLE merchant_account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  contact JSONB,
  settlement_info JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);

CREATE TABLE product_item (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchant_account(id),
  title VARCHAR(255) NOT NULL,
  description TEXT,
  price BIGINT NOT NULL CHECK (price >= 0),
  billing_type VARCHAR(32) NOT NULL,
  pricing_rule JSONB,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  stock BIGINT DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);

CREATE TABLE merchant_statement (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchant_account(id),
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  total_income BIGINT NOT NULL,
  platform_commission BIGINT NOT NULL,
  payable_amount BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
-- Enable pgcrypto for gen_random_uuid (used for UUID primary keys)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Wallet Account (use UUID primary keys)
CREATE TABLE wallet_account (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id BIGINT NOT NULL UNIQUE,
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  balance BIGINT NOT NULL DEFAULT 0 CHECK (balance >= 0),
  frozen_amount BIGINT NOT NULL DEFAULT 0 CHECK (frozen_amount >= 0),
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);
CREATE INDEX idx_wallet_account_user_id ON wallet_account(user_id);

-- Wallet Transaction
CREATE TABLE wallet_transaction (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  wallet_id UUID NOT NULL REFERENCES wallet_account(id) ON DELETE CASCADE,
  user_id BIGINT NOT NULL,
  tx_type VARCHAR(32) NOT NULL,
  amount BIGINT NOT NULL,
  balance_after BIGINT NOT NULL,
  related_order_id UUID,
  biz_no VARCHAR(128),
  status VARCHAR(32) NOT NULL,
  remark TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_wallet_transaction_user_id ON wallet_transaction(user_id);
CREATE INDEX idx_wallet_transaction_tx_type ON wallet_transaction(tx_type);
CREATE UNIQUE INDEX uidx_wallet_transaction_biz_no ON wallet_transaction(biz_no) WHERE biz_no IS NOT NULL;

-- Recharge Order
CREATE TABLE recharge_order (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id BIGINT NOT NULL,
  amount BIGINT NOT NULL CHECK (amount > 0),
  currency VARCHAR(8) NOT NULL DEFAULT 'CNY',
  payment_method VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  third_party_txn VARCHAR(128),
  idempotency_key VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX uidx_recharge_order_idempotency_key ON recharge_order(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Payment Order
CREATE TABLE payment_order (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id BIGINT NOT NULL,
  merchant_id UUID,
  total_amount BIGINT NOT NULL CHECK (total_amount >= 0),
  pay_method VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  refunded_amount BIGINT DEFAULT 0,
  idempotency_key VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  paid_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX uidx_payment_order_idempotency_key ON payment_order(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Refund Order
CREATE TABLE refund_order (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  related_order_id UUID NOT NULL,
  amount BIGINT NOT NULL CHECK (amount > 0),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  idempotency_key VARCHAR(128),
  third_party_txn VARCHAR(128),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  completed_at TIMESTAMP WITH TIME ZONE
);
CREATE UNIQUE INDEX uidx_refund_order_idempotency_key ON refund_order(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Merchant Account
CREATE TABLE merchant_account (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  contact JSONB,
  settlement_info JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);

-- Product Item
CREATE TABLE product_item (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id UUID NOT NULL REFERENCES merchant_account(id) ON DELETE CASCADE,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  price BIGINT NOT NULL CHECK (price >= 0),
  billing_type VARCHAR(32) NOT NULL,
  pricing_rule JSONB,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  stock BIGINT DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  deleted SMALLINT DEFAULT 0
);

-- Merchant Statement
CREATE TABLE merchant_statement (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_id UUID NOT NULL REFERENCES merchant_account(id) ON DELETE CASCADE,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  total_income BIGINT NOT NULL,
  platform_commission BIGINT NOT NULL,
  payable_amount BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
