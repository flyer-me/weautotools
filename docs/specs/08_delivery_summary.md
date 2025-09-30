# 交付规范化文档（汇总）

本汇总将先前编写的规范文档链接并说明下一步工作项，供开发/测试/运维参考。

## 已完成文档（路径相对于仓库根目录）
1. 高层产品范围与优先级：`docs/specs/01_product_scope.md`
2. 顾客相关功能规范：`docs/specs/02_customer_spec.md`
3. 商家相关功能规范：`docs/specs/03_merchant_spec.md`
4. 计费规则与结算设计：`docs/specs/04_billing_settlement.md`
5. 数据模型与 DDL 草案：`docs/specs/05_data_model.md`
6. API 设计与 OpenAPI 草稿：`docs/specs/06_api_openapi.md`
7. 验收测试与上线计划：`docs/specs/07_release_acceptance.md`

## 建议的下一步（可分阶段执行）
- 将文本 API 草稿转换为 OpenAPI YAML 并生成客户端/Mock
- 实现 Flyway/Liquibase migration，并在 dev/staging 运行验证
- 编写关键业务单元与集成测试（充值、下单、退款、并发场景）
- 实现计费规则引擎基础实现与计费模拟器
- 准备运维/监控仪表盘（Prometheus 指标与 Grafana 仪表）

## 交付内容与负责人建议
- 产品：确认计费规则边界与佣金策略
- 后端：实现钱包/流水/订单 API 与 migration
- 前端：集成支付流程与 Mock 支付回调
- 测试：并发与幂等自动化测试
- 运维：配置监控、告警与备份策略

## 变更管理
- 所有 schema 变更通过 migration 工具执行，并写明回滚脚本
- 计费规则做版本管理以支持回滚
