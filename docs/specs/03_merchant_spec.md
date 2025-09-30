# 商家相关功能规范（初稿）

## 概述
为商家提供入驻、商品/服务上架、定价与计费配置、库存管理（可选）、订单管理与结算对账功能。商家需通过平台认证并配置结算信息与计费规则。

## 主要角色与权限
- 商家（Merchant）：上架商品、配置计费方式、查看订单、发起结算申请
- 商家运营（Merchant-Operator）：商家内部用来管理商品和订单的账号
- 平台管理员（Admin）：审核商家入驻、强制下架、调整结算策略、查看财务报表

## 核心用例
- 商家入驻申请与审核（P0）
- 商品/服务上架与下架（P0）
- 定价与计费方式配置（按次、按量、包月、免费试用）（P0）
- 库存管理（若适用，P1）
- 商家账单与结算申请（P1）
- 订单查看、退款处理与售后（P1）

## 数据模型要点（概要）
- merchant_account：id, name, status, contact, settlement_info, created_at
- product_item：id, merchant_id, title, description, price, billing_type, status, stock
- pricing_rule：支持 JSON 格式规则体（阶梯、包月、按量计费等）
- merchant_statement / settlement_request：结算周期、金额、状态

## API 概要
- POST /api/v1/merchants/apply — 商家入驻申请
- GET /api/v1/merchants/{id} — 商家信息
- POST /api/v1/merchants/{id}/products — 新建商品
- PUT /api/v1/merchants/{id}/products/{pid} — 更新商品
- POST /api/v1/merchants/{id}/products/{pid}/publish — 上架
- POST /api/v1/merchants/{id}/products/{pid}/unpublish — 下架
- GET /api/v1/merchants/{id}/orders — 商家查看订单
- GET /api/v1/admin/merchants — 平台查看/审核商家

## 计费配置（接口）
- 商品层支持配置 `billing_type` 与 `pricing_rule`，`pricing_rule` 使用统一的 JSON schema（将在计费规则文档中详细说明）

## 结算与对账
- 结算周期：建议支持按月/按周
- 结算单生成：在结算周期结束后，平台根据订单实际收入（扣除平台佣金/手续费）生成结算单，商家可发起提现（或平台自动打款）
- 对账：商家可下载对账单 CSV，平台提供对账异常申诉流程

## 验收要点
- 商家能完成入驻申请并通过审核后上架商品
- 商品上架/下架流程正常，订单能在商家端查询
- 计费规则能被正确解析并在下单时生效

## 后续细化项
- pricing_rule JSON Schema
- SQL DDL 草案
- OpenAPI 定义
- 计费模拟器与测试用例
