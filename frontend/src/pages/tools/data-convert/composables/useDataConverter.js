import { ref, computed, watch } from 'vue'
import { DataConverter } from '@/features/tools/data/converter.js'
import { ProgressTracker } from '@/features/tools/base/ProgressTracker.js'
import { useUsageLimit } from '@/composables/useUsageLimit'

export function useDataConverter() {
  // 响应式状态
  const inputContent = ref('')
  const outputContent = ref('')
  const fromFormatIndex = ref(0) // JSON
  const toFormatIndex = ref(1) // YAML
  const validationResult = ref(null)

  const isConverting = ref(false)
  const progress = ref(0)
  const progressStatus = ref('normal')
  const progressMessage = ref('')
  const errorMessage = ref('')

  const batchResults = ref([])

  // 工具实例
  const dataConverter = new DataConverter()

  // 使用限制
  const { useFrontendTool } = useUsageLimit()

  // 配置选项
  const formatOptions = [
    { value: 'json', label: 'JSON' },
    { value: 'yaml', label: 'YAML' },
    { value: 'xml', label: 'XML' }
  ]

  const quickConversions = dataConverter.getSupportedConversions()

  // 计算属性
  const canConvert = computed(() => {
    return inputContent.value.trim().length > 0 && 
           fromFormatIndex.value !== toFormatIndex.value
  })

  const batchStats = computed(() => {
    const successful = batchResults.value.filter(r => r.success).length
    const failed = batchResults.value.filter(r => !r.success).length
    return { successful, failed }
  })

  // 获取输入占位符
  const getInputPlaceholder = () => {
    const format = formatOptions[fromFormatIndex.value].value
    const placeholders = {
      json: '请输入 JSON 数据，例如：\n{\n  "name": "示例",\n  "value": 123\n}',
      yaml: '请输入 YAML 数据，例如：\nname: 示例\nvalue: 123',
      xml: '请输入 XML 数据，例如：\n<root>\n  <name>示例</name>\n  <value>123</value>\n</root>'
    }
    return placeholders[format] || '请输入要转换的数据...'
  }

  // 获取格式提示
  const getFormatHint = () => {
    const format = formatOptions[fromFormatIndex.value].value
    const hints = {
      json: 'JSON (JavaScript Object Notation) 是一种轻量级的数据交换格式。使用键值对表示数据，支持字符串、数字、布尔值、数组和对象。',
      yaml: 'YAML (YAML Ain\'t Markup Language) 是一种人类可读的数据序列化标准。使用缩进表示层级关系，语法简洁。',
      xml: 'XML (eXtensible Markup Language) 是一种标记语言。使用标签包围数据，支持属性和嵌套结构。'
    }
    return hints[format] || ''
  }

  // 监听输入变化，自动检测格式
  watch(inputContent, (newContent) => {
    if (newContent.trim()) {
      const detectedFormat = dataConverter.detectFormat(newContent)
      if (detectedFormat) {
        const detectedIndex = formatOptions.findIndex(f => f.value === detectedFormat)
        if (detectedIndex !== -1 && detectedIndex !== fromFormatIndex.value) {
          fromFormatIndex.value = detectedIndex
        }
      }
    }
    // 清除之前的验证结果
    validationResult.value = null
  })

  // 验证输入
  const validateInput = async () => {
    if (!inputContent.value.trim()) {
      validationResult.value = {
        valid: false,
        message: '请输入要验证的数据'
      }
      return
    }

    try {
      const format = formatOptions[fromFormatIndex.value].value
      const result = await dataConverter.validate(inputContent.value, format)
      validationResult.value = result
    } catch (error) {
      validationResult.value = {
        valid: false,
        message: `验证失败: ${error.message}`
      }
    }
  }

  // 美化输入
  const beautifyInput = async () => {
    if (!inputContent.value.trim()) {
      uni.showToast({
        title: '请先输入数据',
        icon: 'none'
      })
      return
    }

    try {
      const format = formatOptions[fromFormatIndex.value].value
      const beautified = await dataConverter.beautify(inputContent.value, format)
      inputContent.value = beautified
      
      uni.showToast({
        title: '美化成功',
        icon: 'success'
      })
    } catch (error) {
      uni.showToast({
        title: `美化失败: ${error.message}`,
        icon: 'none'
      })
    }
  }

  // 转换数据
  const convertData = async () => {
    if (!canConvert.value || isConverting.value) return

    // 检查使用限制
    const usageResult = await useFrontendTool('data-convert', 1)
    if (!usageResult.canUse) {
      return
    }

    isConverting.value = true
    progress.value = 0
    progressStatus.value = 'running'
    errorMessage.value = ''

    try {
      const fromFormat = formatOptions[fromFormatIndex.value].value
      const toFormat = formatOptions[toFormatIndex.value].value

      progressMessage.value = '正在转换数据...'
      progress.value = 50

      const result = await dataConverter.convert(inputContent.value, fromFormat, toFormat)
      
      outputContent.value = result
      progressStatus.value = 'completed'
      progressMessage.value = '转换完成'
      progress.value = 100

      // 报告使用
      await usageResult.reportUsage(1)

      uni.showToast({
        title: '转换成功',
        icon: 'success'
      })

    } catch (error) {
      progressStatus.value = 'error'
      progressMessage.value = '转换失败'
      errorMessage.value = error.message
      
      uni.showToast({
        title: `转换失败: ${error.message}`,
        icon: 'none'
      })
    } finally {
      isConverting.value = false
    }
  }

  // 复制输出
  const copyOutput = () => {
    if (!outputContent.value) return

    uni.setClipboardData({
      data: outputContent.value,
      success: () => {
        uni.showToast({
          title: '复制成功',
          icon: 'success'
        })
      },
      fail: () => {
        uni.showToast({
          title: '复制失败',
          icon: 'none'
        })
      }
    })
  }

  // 下载输出
  const downloadOutput = () => {
    if (!outputContent.value) return

    try {
      const format = formatOptions[toFormatIndex.value].value
      const filename = `converted_data.${format}`
      const blob = new Blob([outputContent.value], { type: 'text/plain' })
      
      dataConverter.downloadFile(blob, filename)
      
      uni.showToast({
        title: '下载成功',
        icon: 'success'
      })
    } catch (error) {
      uni.showToast({
        title: `下载失败: ${error.message}`,
        icon: 'none'
      })
    }
  }

  // 批量转换文件
  const convertBatch = async (files) => {
    if (!files || files.length === 0) return

    // 检查使用限制
    const usageResult = await useFrontendTool('data-convert', files.length)
    if (!usageResult.canUse) {
      return
    }

    isConverting.value = true
    progress.value = 0
    progressStatus.value = 'running'
    errorMessage.value = ''
    batchResults.value = []

    try {
      const tracker = new ProgressTracker(files.length, (status) => {
        progress.value = status.percentage
        progressMessage.value = `正在转换第 ${status.current}/${status.total} 个文件...`
      })

      tracker.start()

      const fromFormat = formatOptions[fromFormatIndex.value].value
      const toFormat = formatOptions[toFormatIndex.value].value

      const results = []
      for (let i = 0; i < files.length; i++) {
        const file = files[i]
        try {
          const content = await readFileContent(file)
          const converted = await dataConverter.convert(content, fromFormat, toFormat)
          
          results.push({
            success: true,
            input: file,
            output: converted,
            error: null
          })
        } catch (error) {
          results.push({
            success: false,
            input: file,
            output: null,
            error: error.message
          })
        }
        
        tracker.setCurrent(i + 1, `转换完成: ${file.name}`)
      }

      batchResults.value = results
      progressStatus.value = 'completed'
      progressMessage.value = '批量转换完成'
      tracker.complete()

      // 报告使用
      const successfulCount = results.filter(r => r.success).length
      await usageResult.reportUsage(successfulCount)

    } catch (error) {
      progressStatus.value = 'error'
      progressMessage.value = '批量转换失败'
      errorMessage.value = error.message
    } finally {
      isConverting.value = false
    }
  }

  // 读取文件内容
  const readFileContent = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = (e) => resolve(e.target.result)
      reader.onerror = () => reject(new Error('文件读取失败'))
      reader.readAsText(file)
    })
  }

  // 下载批量结果
  const downloadBatchResult = (result, index) => {
    if (!result.success || !result.output) return

    try {
      const format = formatOptions[toFormatIndex.value].value
      const originalName = result.input.name.replace(/\.[^/.]+$/, '')
      const filename = `${originalName}_converted.${format}`
      const blob = new Blob([result.output], { type: 'text/plain' })
      
      dataConverter.downloadFile(blob, filename)
    } catch (error) {
      uni.showToast({
        title: `下载失败: ${error.message}`,
        icon: 'none'
      })
    }
  }

  // 下载所有结果
  const downloadAllResults = async () => {
    const successfulResults = batchResults.value.filter(r => r.success)
    if (successfulResults.length === 0) return

    try {
      const format = formatOptions[toFormatIndex.value].value
      const files = successfulResults.map(result => {
        const originalName = result.input.name.replace(/\.[^/.]+$/, '')
        const filename = `${originalName}_converted.${format}`
        return {
          data: new Blob([result.output], { type: 'text/plain' }),
          filename: filename,
          mimeType: 'text/plain'
        }
      })

      await dataConverter.downloadBatch(files, 'converted_files.zip')
    } catch (error) {
      uni.showToast({
        title: `批量下载失败: ${error.message}`,
        icon: 'none'
      })
    }
  }

  // 清空结果
  const clearResults = () => {
    inputContent.value = ''
    outputContent.value = ''
    validationResult.value = null
    batchResults.value = []
    errorMessage.value = ''
    progress.value = 0
    progressStatus.value = 'normal'
    progressMessage.value = ''
  }

  // 验证文件格式
  const validateFileFormat = (file) => {
    const allowedExtensions = ['.json', '.xml', '.yaml', '.yml', '.txt']
    const fileName = file.name.toLowerCase()
    return allowedExtensions.some(ext => fileName.endsWith(ext))
  }

  return {
    // 状态
    inputContent,
    outputContent,
    fromFormatIndex,
    toFormatIndex,
    validationResult,
    isConverting,
    progress,
    progressStatus,
    progressMessage,
    errorMessage,
    batchResults,

    // 配置
    formatOptions,
    quickConversions,

    // 计算属性
    canConvert,
    batchStats,

    // 方法
    getInputPlaceholder,
    getFormatHint,
    validateInput,
    beautifyInput,
    convertData,
    copyOutput,
    downloadOutput,
    convertBatch,
    downloadBatchResult,
    downloadAllResults,
    clearResults,
    validateFileFormat
  }
}
