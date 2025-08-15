/**
 * 二维码测试工具
 * 用于测试二维码生成和识别功能
 */

import { QRGenerator } from '@/tools/qrcode/generator.js'
import { QRDecoder } from '@/tools/qrcode/decoder.js'

/**
 * 测试二维码生成和识别的完整流程
 */
export async function testQRCodeFlow() {
  console.log('🧪 开始测试二维码生成和识别流程...')
  
  try {
    // 1. 生成二维码
    console.log('1. 生成二维码...')
    const generator = new QRGenerator()
    const testText = 'https://weautotools.com/test'
    
    const qrResult = await generator.generate(testText, {
      width: 256,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    
    console.log('✅ 二维码生成成功:', {
      text: qrResult.text,
      size: qrResult.size,
      width: qrResult.width,
      height: qrResult.height
    })
    
    // 2. 将生成的二维码转换为File对象
    console.log('2. 转换为File对象...')
    const file = new File([qrResult.blob], 'test-qr.png', { type: 'image/png' })
    console.log('✅ File对象创建成功:', file.name, file.size)
    
    // 3. 识别二维码
    console.log('3. 识别二维码...')
    const decoder = new QRDecoder()
    
    const decodeResult = await decoder.decodeFromFile(file)
    
    console.log('✅ 二维码识别成功:', {
      success: decodeResult.success,
      data: decodeResult.data,
      originalText: testText,
      match: decodeResult.data === testText
    })
    
    // 4. 验证结果
    if (decodeResult.success && decodeResult.data === testText) {
      console.log('🎉 测试完全成功！生成和识别的内容一致')
      return {
        success: true,
        message: '二维码生成和识别测试通过',
        originalText: testText,
        decodedText: decodeResult.data,
        qrSize: qrResult.size
      }
    } else {
      console.error('❌ 测试失败：识别结果与原始内容不匹配')
      return {
        success: false,
        message: '识别结果与原始内容不匹配',
        originalText: testText,
        decodedText: decodeResult.data
      }
    }
    
  } catch (error) {
    console.error('❌ 测试失败:', error.message)
    return {
      success: false,
      message: error.message,
      error: error
    }
  }
}

/**
 * 测试jsQR库是否正常工作
 */
export async function testJsQRLibrary() {
  console.log('🧪 测试jsQR库...')
  
  try {
    // 动态导入jsQR
    const jsQR = (await import('jsqr')).default
    
    if (!jsQR) {
      throw new Error('jsQR库导入失败')
    }
    
    console.log('✅ jsQR库导入成功')
    
    // 创建一个简单的测试图像数据
    const width = 100
    const height = 100
    const data = new Uint8ClampedArray(width * height * 4)
    
    // 填充白色背景
    for (let i = 0; i < data.length; i += 4) {
      data[i] = 255     // R
      data[i + 1] = 255 // G
      data[i + 2] = 255 // B
      data[i + 3] = 255 // A
    }
    
    // 尝试识别（应该失败，因为没有二维码）
    const result = jsQR(data, width, height)
    
    if (result === null) {
      console.log('✅ jsQR库工作正常（正确返回null）')
      return {
        success: true,
        message: 'jsQR库工作正常'
      }
    } else {
      console.warn('⚠️ jsQR库返回了意外结果')
      return {
        success: true,
        message: 'jsQR库工作，但返回了意外结果',
        result: result
      }
    }
    
  } catch (error) {
    console.error('❌ jsQR库测试失败:', error.message)
    return {
      success: false,
      message: error.message,
      error: error
    }
  }
}

/**
 * 测试Canvas和ImageData功能
 */
export function testCanvasSupport() {
  console.log('🧪 测试Canvas支持...')
  
  try {
    // 检查Canvas支持
    if (typeof document === 'undefined') {
      throw new Error('当前环境不支持document对象')
    }
    
    const canvas = document.createElement('canvas')
    if (!canvas) {
      throw new Error('无法创建Canvas元素')
    }
    
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      throw new Error('无法获取Canvas 2D上下文')
    }
    
    // 测试基本Canvas操作
    canvas.width = 100
    canvas.height = 100
    ctx.fillStyle = '#ff0000'
    ctx.fillRect(0, 0, 50, 50)
    
    // 测试ImageData
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    if (!imageData || !imageData.data) {
      throw new Error('无法获取ImageData')
    }
    
    console.log('✅ Canvas支持测试通过:', {
      canvasSize: `${canvas.width}x${canvas.height}`,
      imageDataSize: `${imageData.width}x${imageData.height}`,
      dataLength: imageData.data.length
    })
    
    return {
      success: true,
      message: 'Canvas功能正常',
      canvasSupported: true,
      imageDataSupported: true
    }
    
  } catch (error) {
    console.error('❌ Canvas支持测试失败:', error.message)
    return {
      success: false,
      message: error.message,
      error: error
    }
  }
}

/**
 * 运行所有二维码相关测试
 */
export async function runAllQRTests() {
  console.log('🚀 开始运行所有二维码测试...')
  
  const results = []
  
  // 1. 测试Canvas支持
  const canvasTest = testCanvasSupport()
  results.push({ name: 'Canvas支持', ...canvasTest })
  
  // 2. 测试jsQR库
  const jsqrTest = await testJsQRLibrary()
  results.push({ name: 'jsQR库', ...jsqrTest })
  
  // 3. 测试完整流程（只有前面的测试都通过才执行）
  if (canvasTest.success && jsqrTest.success) {
    const flowTest = await testQRCodeFlow()
    results.push({ name: '完整流程', ...flowTest })
  } else {
    results.push({ 
      name: '完整流程', 
      success: false, 
      message: '跳过测试（前置条件不满足）' 
    })
  }
  
  // 输出测试结果
  console.log('\n📊 二维码测试结果汇总:')
  console.log('=' * 50)
  
  const successful = results.filter(r => r.success)
  const failed = results.filter(r => !r.success)
  
  successful.forEach(r => {
    console.log(`✅ ${r.name}: 通过`)
  })
  
  failed.forEach(r => {
    console.log(`❌ ${r.name}: 失败 - ${r.message}`)
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
