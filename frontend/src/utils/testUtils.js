/**
 * 测试工具函数
 * 用于验证各个工具的基本功能
 */

import { QRGenerator } from '@/tools/qrcode/generator.js'
import { QRDecoder } from '@/tools/qrcode/decoder.js'
import { DataConverter } from '@/tools/data/converter.js'
import { ImageProcessor } from '@/tools/image/processor.js'

/**
 * 测试二维码生成功能
 */
export async function testQRGenerator() {
  console.log('🧪 测试二维码生成功能...')
  
  try {
    const generator = new QRGenerator()
    
    // 测试基本生成
    const result = await generator.generate('https://weautotools.com', {
      width: 256,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    
    console.log('✅ 二维码生成成功:', {
      size: result.size,
      width: result.width,
      height: result.height
    })
    
    return true
  } catch (error) {
    console.error('❌ 二维码生成失败:', error.message)
    return false
  }
}

/**
 * 测试数据格式转换功能
 */
export async function testDataConverter() {
  console.log('🧪 测试数据格式转换功能...')
  
  try {
    const converter = new DataConverter()
    
    // 测试JSON转YAML
    const jsonData = '{"name": "WeAutoTools", "version": "1.0.0", "tools": ["qrcode", "image", "pdf"]}'
    const yamlResult = converter.jsonToYaml(jsonData)
    
    console.log('✅ JSON转YAML成功:', yamlResult.substring(0, 50) + '...')
    
    // 测试YAML转JSON
    const jsonResult = converter.yamlToJson(yamlResult)
    const parsedResult = JSON.parse(jsonResult)
    
    console.log('✅ YAML转JSON成功:', parsedResult.name)
    
    // 测试格式验证
    const validation = converter.validateJson(jsonData)
    console.log('✅ JSON格式验证:', validation.valid ? '通过' : '失败')
    
    return true
  } catch (error) {
    console.error('❌ 数据格式转换失败:', error.message)
    return false
  }
}

/**
 * 测试图片处理功能（模拟）
 */
export async function testImageProcessor() {
  console.log('🧪 测试图片处理功能...')
  
  try {
    const processor = new ImageProcessor()
    
    // 测试支持的格式
    const formats = processor.getSupportedOutputFormats()
    console.log('✅ 支持的输出格式:', formats.map(f => f.label).join(', '))
    
    // 测试预设尺寸
    const sizes = processor.getPresetSizes()
    console.log('✅ 预设尺寸数量:', sizes.length)
    
    // 测试质量预设
    const qualities = processor.getQualityPresets()
    console.log('✅ 质量预设数量:', qualities.length)
    
    return true
  } catch (error) {
    console.error('❌ 图片处理器初始化失败:', error.message)
    return false
  }
}

/**
 * 测试文件工具函数
 */
export async function testFileUtils() {
  console.log('🧪 测试文件工具函数...')
  
  try {
    // 测试文件大小格式化
    const { FileProcessor } = await import('@/tools/base/FileProcessor.js')
    const processor = new FileProcessor()
    
    const sizes = [1024, 1024 * 1024, 1024 * 1024 * 1024]
    const formatted = sizes.map(size => processor.formatFileSize(size))
    
    console.log('✅ 文件大小格式化:', formatted.join(', '))
    
    // 测试文件名生成
    const filename = processor.generateFileName('test.jpg', '_processed')
    console.log('✅ 文件名生成:', filename)
    
    return true
  } catch (error) {
    console.error('❌ 文件工具测试失败:', error.message)
    return false
  }
}

/**
 * 测试进度跟踪器
 */
export async function testProgressTracker() {
  console.log('🧪 测试进度跟踪器...')
  
  try {
    const { ProgressTracker } = await import('@/tools/base/ProgressTracker.js')
    
    const tracker = new ProgressTracker(10, (status) => {
      console.log(`进度: ${status.percentage}% - ${status.message}`)
    })
    
    tracker.start()
    
    // 模拟进度更新
    for (let i = 1; i <= 10; i++) {
      tracker.increment(1, `处理第 ${i} 项`)
      await new Promise(resolve => setTimeout(resolve, 100))
    }
    
    tracker.complete('测试完成')
    
    console.log('✅ 进度跟踪器测试完成')
    return true
  } catch (error) {
    console.error('❌ 进度跟踪器测试失败:', error.message)
    return false
  }
}

/**
 * 运行所有测试
 */
export async function runAllTests() {
  console.log('🚀 开始运行所有测试...')
  
  const tests = [
    { name: '二维码生成', test: testQRGenerator },
    { name: '数据格式转换', test: testDataConverter },
    { name: '图片处理', test: testImageProcessor },
    { name: '文件工具', test: testFileUtils },
    { name: '进度跟踪', test: testProgressTracker }
  ]
  
  const results = []
  
  for (const { name, test } of tests) {
    try {
      const result = await test()
      results.push({ name, success: result })
    } catch (error) {
      console.error(`❌ ${name}测试异常:`, error.message)
      results.push({ name, success: false, error: error.message })
    }
  }
  
  // 输出测试结果
  console.log('\n📊 测试结果汇总:')
  console.log('=' * 50)
  
  const successful = results.filter(r => r.success)
  const failed = results.filter(r => !r.success)
  
  successful.forEach(r => {
    console.log(`✅ ${r.name}: 通过`)
  })
  
  failed.forEach(r => {
    console.log(`❌ ${r.name}: 失败 ${r.error ? `(${r.error})` : ''}`)
  })
  
  console.log(`\n总计: ${results.length} 项测试`)
  console.log(`通过: ${successful.length} 项`)
  console.log(`失败: ${failed.length} 项`)
  console.log(`成功率: ${((successful.length / results.length) * 100).toFixed(1)}%`)
  
  return {
    total: results.length,
    successful: successful.length,
    failed: failed.length,
    results
  }
}

/**
 * 性能测试
 */
export async function performanceTest() {
  console.log('⚡ 开始性能测试...')
  
  const startTime = performance.now()
  
  try {
    // 测试二维码批量生成性能
    const generator = new QRGenerator()
    const texts = Array.from({ length: 10 }, (_, i) => `Test QR Code ${i + 1}`)
    
    const batchStartTime = performance.now()
    const results = await generator.generateBatch(texts)
    const batchEndTime = performance.now()
    
    const batchTime = batchEndTime - batchStartTime
    const avgTime = batchTime / texts.length
    
    console.log(`✅ 批量生成 ${texts.length} 个二维码`)
    console.log(`总耗时: ${batchTime.toFixed(2)}ms`)
    console.log(`平均耗时: ${avgTime.toFixed(2)}ms/个`)
    
    const endTime = performance.now()
    const totalTime = endTime - startTime
    
    console.log(`\n⚡ 性能测试完成，总耗时: ${totalTime.toFixed(2)}ms`)
    
    return {
      totalTime,
      batchTime,
      avgTime,
      throughput: (texts.length / (batchTime / 1000)).toFixed(2) + ' QR/秒'
    }
  } catch (error) {
    console.error('❌ 性能测试失败:', error.message)
    return null
  }
}

/**
 * 内存使用测试
 */
export function memoryTest() {
  if (typeof performance !== 'undefined' && performance.memory) {
    const memory = performance.memory
    
    console.log('💾 内存使用情况:')
    console.log(`已使用: ${(memory.usedJSHeapSize / 1024 / 1024).toFixed(2)} MB`)
    console.log(`总分配: ${(memory.totalJSHeapSize / 1024 / 1024).toFixed(2)} MB`)
    console.log(`限制: ${(memory.jsHeapSizeLimit / 1024 / 1024).toFixed(2)} MB`)
    
    return {
      used: memory.usedJSHeapSize,
      total: memory.totalJSHeapSize,
      limit: memory.jsHeapSizeLimit
    }
  } else {
    console.log('💾 当前环境不支持内存监控')
    return null
  }
}
