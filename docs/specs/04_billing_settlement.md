# 计费规则引擎与结算设计（初稿）

## 目标
设计一个可配置、可扩展的计费规则引擎，支持常见计费模式：按次、按量、阶梯计费、包月/订阅、促销折扣与优惠券。提供结算与对账能力，支持商家与平台分成规则。

## 计费模型概览
计费规则应用层次：
- 平台全局（平台费用/佣金）
- 商家级（商家可配置默认计费政策）
- 商品级（最优先级，商品可配置 pricing_rule）

计费类型：
- FIXED（固定单价/按次）
- USAGE（按量计费，例如按实际耗用量计价）
- TIERED（阶梯计费：不同数量段不同单价或不同计费方式）
- SUBSCRIPTION（按周期订阅/包月）
- FREE（免费或试用）

## pricing_rule JSON Schema（示例）
- type: "FIXED" | "USAGE" | "TIERED" | "SUBSCRIPTION" | "FREE"
- currency: "CNY"
- tiers: 可选数组，定义阶梯（min_unit, max_unit, unit_price）
- billing_cycle: 可选（用于 SUBSCRIPTION，如 monthly、weekly）
- trial_period_days: 可选

示例（阶梯计费）:
{
  "type": "TIERED",
  "currency": "CNY",
  "tiers": [
    {"min_unit": 0, "max_unit": 100, "unit_price": 100},
    {"min_unit": 101, "max_unit": 500, "unit_price": 80},
    {"min_unit": 501, "max_unit": null, "unit_price": 50}
  ]
}

## 计费流程（下单/结算）
1. 下单时从商品中读取 pricing_rule，结合订单实际指标（数量/时长/流量）计算金额
2. 应用促销与优惠券（若有），计算最终应付金额
3. 若使用钱包支付：扣款并生成流水；若第三方支付：创建支付单并等待回调
4. 订单完成后，记录计费明细供结算使用

## 结算流程
- 结算周期结束后（按商家设置，如月结），系统汇总周期内已确认收入（不含退款/待处理订单），计算平台佣金与应结金额
- 生成结算单（merchant_statement），变更结算状态（PENDING -> APPROVED -> PAID）
- 提供结算单导出和争议处理接口

## 分成与手续费
- 平台佣金可配置为固定比例或固定金额
- 手续费处理：第三方支付手续费由平台承担或按约定分摊（可配置）

## 可扩展性与实现建议
- 计费规则解析器：采用策略模式，将 JSON rule 映射到不同计费策略类
- 规则测试工具：提供计费模拟器供商家预览计费效果
- 支持规则版本化与生效时间
- 所有计费计算记录明细以便审计与重算

## 验收要点
- 支持 FIXED/USAGE/TIERED/SUBSCRIPTION 四种计费方式并通过样例用例验证
- 生成结算单正确反映平台佣金与应结金额
- 计费模拟器输出与实际计费保持一致

## 后续细化项
- pricing_rule JSON Schema 规范化
- 计费引擎代码接口定义（Java 接口/服务）
- 计费边界条件与精度规则（货币小数/进位）
- 自动化测试用例（包含复杂阶梯与订阅续费）
