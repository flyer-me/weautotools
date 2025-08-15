# WeAutoTools 客户端工具开发文档

## 项目状态
- ✅ **核心功能已完成**: 二维码工具、数据转换、图片处理
- 📦 **可投入生产使用**

## 技术栈
```bash
# 核心依赖
npm install qrcode jsqr js-yaml xml2js compressorjs
```

## 已实现功能

### 1. 二维码工具
- **生成器**: `/pages/tools/qr-generate/qr-generate`
- **识别器**: `/pages/tools/qr-decode/qr-decode`
- **调试页面**: `/pages/qr-debug/qr-debug`

### 2. 数据格式转换
- **路径**: `/pages/tools/data-convert/data-convert`
- **支持**: JSON ↔ XML ↔ YAML

### 3. 图片处理
- **路径**: `/pages/tools/image-process/image-process`
- **功能**: 压缩、转换、水印、重命名

### 4. 测试工具
- **功能测试**: `/pages/test/test`
- **性能监控**: 内存使用、处理速度

## 核心架构

### 基础类
```javascript
// 文件处理基类
import { FileProcessor } from '@/tools/base/FileProcessor.js'

// 批量处理框架
import { BatchProcessor } from '@/tools/base/BatchProcessor.js'

// 进度跟踪
import { ProgressTracker } from '@/tools/base/ProgressTracker.js'
```

### 工具类示例
```javascript
// 二维码生成
import { QRGenerator } from '@/tools/qrcode/generator.js'
const generator = new QRGenerator()
const result = await generator.generate('text', options)

// 数据转换
import { DataConverter } from '@/tools/data/converter.js'
const converter = new DataConverter()
const yaml = converter.jsonToYaml(jsonString)
```

## 待开发功能

### PDF处理工具
**路径**: `/pages/tools/pdf-process`
**依赖**: `pdf-lib`, `jsPDF`
**功能**: 合并、拆分、压缩、加水印

```javascript
// 实现示例
import { PDFProcessor } from '@/tools/pdf/processor.js'
const processor = new PDFProcessor()
await processor.mergePDFs(files)
```

### 音视频处理
**路径**: `/pages/tools/media-process`
**依赖**: `FFmpeg.wasm`
**功能**: 格式转换、压缩、剪辑

### 文档处理
**路径**: `/pages/tools/doc-process`
**依赖**: `mammoth.js`, `xlsx`
**功能**: Word/Excel转换、预览

## 开发规范

### 新工具开发流程
1. 创建工具类 `src/tools/category/tool.js`
2. 继承 `FileProcessor` 基类
3. 创建页面 `src/pages/tools/tool-name/`
4. 使用 `ToolContainer` 组件
5. 添加路由和分类配置

### 工具类模板
```javascript
export class NewTool extends FileProcessor {
  constructor() {
    super()
    this.supportedFormats = ['format1', 'format2']
  }
  
  async process(file, options) {
    // 处理逻辑
  }
  
  async processBatch(files, options, onProgress) {
    // 批量处理
  }
}
```

### 页面模板
```vue
<template>
  <ToolContainer
    title="工具名称"
    :showProgress="isProcessing"
    :results="results"
    @download="handleDownload"
  >
    <!-- 工具界面 -->
  </ToolContainer>
</template>
```

## 部署配置

### 构建命令
```bash
npm run build:h5      # H5版本
npm run build:mp-weixin  # 微信小程序
npm run build:app     # APP版本
```

### 环境要求
- Node.js >= 16
- 现代浏览器（支持ES2020、Canvas API）
- 小程序基础库 >= 2.0

## 问题排查

### 常见问题
1. **文件上传失败**: 检查文件格式和大小限制
2. **处理超时**: 优化算法或增加进度反馈
3. **内存溢出**: 使用流式处理大文件
4. **兼容性问题**: 检查浏览器API支持

### 调试工具
- 功能测试页面: `/pages/test/test`
- 二维码调试: `/pages/qr-debug/qr-debug`
- 浏览器开发者工具

---

*文档最后更新: 2024年*
