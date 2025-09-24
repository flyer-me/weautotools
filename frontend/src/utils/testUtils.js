/**
 * 娴嬭瘯宸ュ叿鍑芥暟
 * 鐢ㄤ簬楠岃瘉鍚勪釜宸ュ叿鐨勫熀鏈姛鑳?
 */

import { QRGenerator } from '@/features/tools/qrcode/generator.js'
import { QRDecoder } from '@/features/tools/qrcode/decoder.js'
import { DataConverter } from '@/features/tools/data/converter.js'
import { ImageProcessor } from '@/features/tools/image/processor.js'

/**
 * 娴嬭瘯浜岀淮鐮佺敓鎴愬姛鑳?
 */
export async function testQRGenerator() {
  console.log('馃И 娴嬭瘯浜岀淮鐮佺敓鎴愬姛鑳?..')
  
  try {
    const generator = new QRGenerator()
    
    // 娴嬭瘯鍩烘湰鐢熸垚
    const result = await generator.generate('https://weautotools.com', {
      width: 256,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    
    console.log('鉁?浜岀淮鐮佺敓鎴愭垚鍔?', {
      size: result.size,
      width: result.width,
      height: result.height
    })
    
    return true
  } catch (error) {
    console.error('鉂?浜岀淮鐮佺敓鎴愬け璐?', error.message)
    return false
  }
}

/**
 * 娴嬭瘯鏁版嵁鏍煎紡杞崲鍔熻兘
 */
export async function testDataConverter() {
  console.log('馃И 娴嬭瘯鏁版嵁鏍煎紡杞崲鍔熻兘...')
  
  try {
    const converter = new DataConverter()
    
    // 娴嬭瘯JSON杞琘AML
    const jsonData = '{"name": "WeAutoTools", "version": "1.0.0", "tools": ["qrcode", "image", "pdf"]}'
    const yamlResult = converter.jsonToYaml(jsonData)
    
    console.log('鉁?JSON杞琘AML鎴愬姛:', yamlResult.substring(0, 50) + '...')
    
    // 娴嬭瘯YAML杞琂SON
    const jsonResult = converter.yamlToJson(yamlResult)
    const parsedResult = JSON.parse(jsonResult)
    
    console.log('鉁?YAML杞琂SON鎴愬姛:', parsedResult.name)
    
    // 娴嬭瘯鏍煎紡楠岃瘉
    const validation = converter.validateJson(jsonData)
    console.log('鉁?JSON鏍煎紡楠岃瘉:', validation.valid ? '閫氳繃' : '澶辫触')
    
    return true
  } catch (error) {
    console.error('鉂?鏁版嵁鏍煎紡杞崲澶辫触:', error.message)
    return false
  }
}

/**
 * 娴嬭瘯鍥剧墖澶勭悊鍔熻兘锛堟ā鎷燂級
 */
export async function testImageProcessor() {
  console.log('馃И 娴嬭瘯鍥剧墖澶勭悊鍔熻兘...')
  
  try {
    const processor = new ImageProcessor()
    
    // 娴嬭瘯鏀寔鐨勬牸寮?
    const formats = processor.getSupportedOutputFormats()
    console.log('鉁?鏀寔鐨勮緭鍑烘牸寮?', formats.map(f => f.label).join(', '))
    
    // 娴嬭瘯棰勮灏哄
    const sizes = processor.getPresetSizes()
    console.log('鉁?棰勮灏哄鏁伴噺:', sizes.length)
    
    // 娴嬭瘯璐ㄩ噺棰勮
    const qualities = processor.getQualityPresets()
    console.log('鉁?璐ㄩ噺棰勮鏁伴噺:', qualities.length)
    
    return true
  } catch (error) {
    console.error('鉂?鍥剧墖澶勭悊鍣ㄥ垵濮嬪寲澶辫触:', error.message)
    return false
  }
}

/**
 * 娴嬭瘯鏂囦欢宸ュ叿鍑芥暟
 */
export async function testFileUtils() {
  console.log('馃И 娴嬭瘯鏂囦欢宸ュ叿鍑芥暟...')
  
  try {
    // 娴嬭瘯鏂囦欢澶у皬鏍煎紡鍖?
    const { FileProcessor } = await import('@/features/tools/base/FileProcessor.js')
    const processor = new FileProcessor()
    
    const sizes = [1024, 1024 * 1024, 1024 * 1024 * 1024]
    const formatted = sizes.map(size => processor.formatFileSize(size))
    
    console.log('鉁?鏂囦欢澶у皬鏍煎紡鍖?', formatted.join(', '))
    
    // 娴嬭瘯鏂囦欢鍚嶇敓鎴?
    const filename = processor.generateFileName('test.jpg', '_processed')
    console.log('鉁?鏂囦欢鍚嶇敓鎴?', filename)
    
    return true
  } catch (error) {
    console.error('鉂?鏂囦欢宸ュ叿娴嬭瘯澶辫触:', error.message)
    return false
  }
}

/**
 * 娴嬭瘯杩涘害璺熻釜鍣?
 */
export async function testProgressTracker() {
  console.log('馃И 娴嬭瘯杩涘害璺熻釜鍣?..')
  
  try {
    const { ProgressTracker } = await import('@/features/tools/base/ProgressTracker.js')
    
    const tracker = new ProgressTracker(10, (status) => {
      console.log(`杩涘害: ${status.percentage}% - ${status.message}`)
    })
    
    tracker.start()
    
    // 妯℃嫙杩涘害鏇存柊
    for (let i = 1; i <= 10; i++) {
      tracker.increment(1, `澶勭悊绗?${i} 椤筦)
      await new Promise(resolve => setTimeout(resolve, 100))
    }
    
    tracker.complete('娴嬭瘯瀹屾垚')
    
    console.log('鉁?杩涘害璺熻釜鍣ㄦ祴璇曞畬鎴?)
    return true
  } catch (error) {
    console.error('鉂?杩涘害璺熻釜鍣ㄦ祴璇曞け璐?', error.message)
    return false
  }
}

/**
 * 杩愯鎵€鏈夋祴璇?
 */
export async function runAllTests() {
  console.log('馃殌 寮€濮嬭繍琛屾墍鏈夋祴璇?..')
  
  const tests = [
    { name: '浜岀淮鐮佺敓鎴?, test: testQRGenerator },
    { name: '鏁版嵁鏍煎紡杞崲', test: testDataConverter },
    { name: '鍥剧墖澶勭悊', test: testImageProcessor },
    { name: '鏂囦欢宸ュ叿', test: testFileUtils },
    { name: '杩涘害璺熻釜', test: testProgressTracker }
  ]
  
  const results = []
  
  for (const { name, test } of tests) {
    try {
      const result = await test()
      results.push({ name, success: result })
    } catch (error) {
      console.error(`鉂?${name}娴嬭瘯寮傚父:`, error.message)
      results.push({ name, success: false, error: error.message })
    }
  }
  
  // 杈撳嚭娴嬭瘯缁撴灉
  console.log('\n馃搳 娴嬭瘯缁撴灉姹囨€?')
  console.log('=' * 50)
  
  const successful = results.filter(r => r.success)
  const failed = results.filter(r => !r.success)
  
  successful.forEach(r => {
    console.log(`鉁?${r.name}: 閫氳繃`)
  })
  
  failed.forEach(r => {
    console.log(`鉂?${r.name}: 澶辫触 ${r.error ? `(${r.error})` : ''}`)
  })
  
  console.log(`\n鎬昏: ${results.length} 椤规祴璇昤)
  console.log(`閫氳繃: ${successful.length} 椤筦)
  console.log(`澶辫触: ${failed.length} 椤筦)
  console.log(`鎴愬姛鐜? ${((successful.length / results.length) * 100).toFixed(1)}%`)
  
  return {
    total: results.length,
    successful: successful.length,
    failed: failed.length,
    results
  }
}

/**
 * 鎬ц兘娴嬭瘯
 */
export async function performanceTest() {
  console.log('鈿?寮€濮嬫€ц兘娴嬭瘯...')
  
  const startTime = performance.now()
  
  try {
    // 娴嬭瘯浜岀淮鐮佹壒閲忕敓鎴愭€ц兘
    const generator = new QRGenerator()
    const texts = Array.from({ length: 10 }, (_, i) => `Test QR Code ${i + 1}`)
    
    const batchStartTime = performance.now()
    const results = await generator.generateBatch(texts)
    const batchEndTime = performance.now()
    
    const batchTime = batchEndTime - batchStartTime
    const avgTime = batchTime / texts.length
    
    console.log(`鉁?鎵归噺鐢熸垚 ${texts.length} 涓簩缁寸爜`)
    console.log(`鎬昏€楁椂: ${batchTime.toFixed(2)}ms`)
    console.log(`骞冲潎鑰楁椂: ${avgTime.toFixed(2)}ms/涓猔)
    
    const endTime = performance.now()
    const totalTime = endTime - startTime
    
    console.log(`\n鈿?鎬ц兘娴嬭瘯瀹屾垚锛屾€昏€楁椂: ${totalTime.toFixed(2)}ms`)
    
    return {
      totalTime,
      batchTime,
      avgTime,
      throughput: (texts.length / (batchTime / 1000)).toFixed(2) + ' QR/绉?
    }
  } catch (error) {
    console.error('鉂?鎬ц兘娴嬭瘯澶辫触:', error.message)
    return null
  }
}

/**
 * 鍐呭瓨浣跨敤娴嬭瘯
 */
export function memoryTest() {
  if (typeof performance !== 'undefined' && performance.memory) {
    const memory = performance.memory
    
    console.log('馃捑 鍐呭瓨浣跨敤鎯呭喌:')
    console.log(`宸蹭娇鐢? ${(memory.usedJSHeapSize / 1024 / 1024).toFixed(2)} MB`)
    console.log(`鎬诲垎閰? ${(memory.totalJSHeapSize / 1024 / 1024).toFixed(2)} MB`)
    console.log(`闄愬埗: ${(memory.jsHeapSizeLimit / 1024 / 1024).toFixed(2)} MB`)
    
    return {
      used: memory.usedJSHeapSize,
      total: memory.totalJSHeapSize,
      limit: memory.jsHeapSizeLimit
    }
  } else {
    console.log('馃捑 褰撳墠鐜涓嶆敮鎸佸唴瀛樼洃鎺?)
    return null
  }
}

