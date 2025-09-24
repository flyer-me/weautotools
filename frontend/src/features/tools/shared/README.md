# features/tools/shared

存放工具域的通用能力：仅在工具内复用、但不适合放到全局 `src/components/`、`src/composables/` 的内容。

- components/ 工具域通用组件（如 ToolContainer、ResultDisplay、SettingGroup 等）
- composables/ 组合式函数（如 useFeatureGuard、useToolRunner）
- types.(ts|js) 可选：工具域的通用类型

命名规范：
- 目录与文件使用 kebab-case
- 组件文件名使用 kebab-case，但组件名（SFC 内）用 PascalCase
- 组合式函数以 `use` 前缀命名
