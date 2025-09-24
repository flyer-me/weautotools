# features/tools 模块（特性优先结构）

本目录用于承载“工具”域的全部前端代码（除页面 `src/pages/tools/` 外），遵循特性优先（feature-first）与 uni-app/Vue 社区通用命名规范。

## 目录结构建议

- shared/
  - components/ 仅被工具域复用的通用组件（如 ToolContainer、ResultDisplay 等）
  - composables/ 组合式函数（如 useFeatureGuard、useToolRunner 等）
  - types.(ts|js) 类型与接口（可选）
- <tool-name>/ 单个工具的模块目录（如 qr、image-process、data-convert）
  - index.(ts|js) 出口文件（barrel），对外暴露模块 API
  - service.(ts|js) 业务与编排逻辑，组合 api/ 与 utils/
  - components/ 该工具内部的私有组件
  - README.md 模块说明（可选）

> 页面层（`src/pages/tools/<tool>`）仅负责展示与交互，核心逻辑放在 `features/tools/` 中，页面通过 import 使用。

## 命名规范（与 Vue/uni-app 社区常见规范一致）

- 目录、文件：使用 kebab-case，例如 `image-process`、`data-convert`、`tool-container.vue`
- 组件名（SFC 内部）：使用 PascalCase，例如 `<ToolContainer />`
- 组合式函数：`useXxx`，如 `useFeatureGuard`
- 出口文件：每个目录提供 `index.(ts|js)` 作为 barrel 导出，简化 import
- 页面路由目录：继续使用 kebab-case，保持 uni-app 一致性

## 新增一个工具的流程

1. 在 `src/features/tools/` 下新建目录（kebab-case），如 `qr` 或 `image-process`。
2. 创建 `service.(ts|js)`，编写工具核心逻辑（可调用 `src/api/` 与 `src/utils/`）。
3. 创建 `index.(ts|js)`，对外导出该工具需要暴露的函数/常量/组件。
4. 如需 UI 片段，放入 `components/`；通用组件放到 `shared/components/`。
5. 在 `src/pages/tools/<tool>/` 创建页面 `.vue`，从 `features/tools/<tool>` 引入逻辑与组件。
6. 如需要功能开关管控，使用 `shared/composables/useFeatureGuard`（创建后），或直接使用 `src/config/features.js`。

## 与其他层的边界

- api/ 仅负责资源端点与 HTTP 调用（薄层）；不要把业务编排放在 api。
- services/ 适合跨域通用业务（与“工具”无关的全局服务），工具相关优先放在 `features/tools/` 中。
- components/ 放全局通用组件；仅工具域复用的组件放 `features/tools/shared/components/`。
