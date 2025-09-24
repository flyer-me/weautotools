/**
 * 浜岀淮鐮佹祴璇曞伐鍏?
 * 鐢ㄤ簬娴嬭瘯浜岀淮鐮佺敓鎴愬拰璇嗗埆鍔熻兘
 */

import { QRGenerator } from '@/features/tools/qrcode/generator.js'
import { QRDecoder } from '@/features/tools/qrcode/decoder.js'

/**
 * 娴嬭瘯浜岀淮鐮佺敓鎴愬拰璇嗗埆鐨勫畬鏁存祦绋?
 */
export async function testQRCodeFlow() {
  console.log('馃И 寮€濮嬫祴璇曚簩缁寸爜鐢熸垚鍜岃瘑鍒祦绋?..')
  
  try {
    // 1. 鐢熸垚浜岀淮鐮?
    console.log('1. 鐢熸垚浜岀淮鐮?..')
    const generator = new QRGenerator()
    const testText = 'https://weautotools.com/test'
    
    const qrResult = await generator.generate(testText, {
      width: 256,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    
    console.log('鉁?浜岀淮鐮佺敓鎴愭垚鍔?', {
      text: qrResult.text,
      size: qrResult.size,
      width: qrResult.width,
      height: qrResult.height
    })
    
    // 2. 灏嗙敓鎴愮殑浜岀淮鐮佽浆鎹负File瀵硅薄
    console.log('2. 杞崲涓篎ile瀵硅薄...')
    const file = new File([qrResult.blob], 'test-qr.png', { type: 'image/png' })
    console.log('鉁?File瀵硅薄鍒涘缓鎴愬姛:', file.name, file.size)
    
    // 3. 璇嗗埆浜岀淮鐮?
    console.log('3. 璇嗗埆浜岀淮鐮?..')
    const decoder = new QRDecoder()
    
    const decodeResult = await decoder.decodeFromFile(file)
    
    console.log('鉁?浜岀淮鐮佽瘑鍒垚鍔?', {
      success: decodeResult.success,
      data: decodeResult.data,
      originalText: testText,
      match: decodeResult.data === testText
    })
    
    // 4. 楠岃瘉缁撴灉
    if (decodeResult.success && decodeResult.data === testText) {
      console.log('馃帀 娴嬭瘯瀹屽叏鎴愬姛锛佺敓鎴愬拰璇嗗埆鐨勫唴瀹逛竴鑷?)
      return {
        success: true,
        message: '浜岀淮鐮佺敓鎴愬拰璇嗗埆娴嬭瘯閫氳繃',
        originalText: testText,
        decodedText: decodeResult.data,
        qrSize: qrResult.size
      }
    } else {
      console.error('鉂?娴嬭瘯澶辫触锛氳瘑鍒粨鏋滀笌鍘熷鍐呭涓嶅尮閰?)
      return {
        success: false,
        message: '璇嗗埆缁撴灉涓庡師濮嬪唴瀹逛笉鍖归厤',
        originalText: testText,
        decodedText: decodeResult.data
      }
    }
    
  } catch (error) {
    console.error('鉂?娴嬭瘯澶辫触:', error.message)
    return {
      success: false,
      message: error.message,
      error: error
    }
  }
}

/**
 * 娴嬭瘯jsQR搴撴槸鍚︽甯稿伐浣?
 */
export async function testJsQRLibrary() {
  console.log('馃И 娴嬭瘯jsQR搴?..')
  
  try {
    // 鍔ㄦ€佸鍏sQR
    const jsQR = (await import('jsqr')).default
    
    if (!jsQR) {
      throw new Error('jsQR搴撳鍏ュけ璐?)
    }
    
    console.log('鉁?jsQR搴撳鍏ユ垚鍔?)
    
    // 鍒涘缓涓€涓畝鍗曠殑娴嬭瘯鍥惧儚鏁版嵁
    const width = 100
    const height = 100
    const data = new Uint8ClampedArray(width * height * 4)
    
    // 濉厖鐧借壊鑳屾櫙
    for (let i = 0; i < data.length; i += 4) {
      data[i] = 255     // R
      data[i + 1] = 255 // G
      data[i + 2] = 255 // B
      data[i + 3] = 255 // A
    }
    
    // 灏濊瘯璇嗗埆锛堝簲璇ュけ璐ワ紝鍥犱负娌℃湁浜岀淮鐮侊級
    const result = jsQR(data, width, height)
    
    if (result === null) {
      console.log('鉁?jsQR搴撳伐浣滄甯革紙姝ｇ‘杩斿洖null锛?)
      return {
        success: true,
        message: 'jsQR搴撳伐浣滄甯?
      }
    } else {
      console.warn('鈿狅笍 jsQR搴撹繑鍥炰簡鎰忓缁撴灉')
      return {
        success: true,
        message: 'jsQR搴撳伐浣滐紝浣嗚繑鍥炰簡鎰忓缁撴灉',
        result: result
      }
    }
    
  } catch (error) {
    console.error('鉂?jsQR搴撴祴璇曞け璐?', error.message)
    return {
      success: false,
      message: error.message,
      error: error
    }
  }
}

/**
 * 娴嬭瘯Canvas鍜孖mageData鍔熻兘
 */
export function testCanvasSupport() {
  console.log('馃И 娴嬭瘯Canvas鏀寔...')
  
  try {
    // 妫€鏌anvas鏀寔
    if (typeof document === 'undefined') {
      throw new Error('褰撳墠鐜涓嶆敮鎸乨ocument瀵硅薄')
    }
    
    const canvas = document.createElement('canvas')
    if (!canvas) {
      throw new Error('鏃犳硶鍒涘缓Canvas鍏冪礌')
    }
    
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      throw new Error('鏃犳硶鑾峰彇Canvas 2D涓婁笅鏂?)
    }
    
    // 娴嬭瘯鍩烘湰Canvas鎿嶄綔
    canvas.width = 100
    canvas.height = 100
    ctx.fillStyle = '#ff0000'
    ctx.fillRect(0, 0, 50, 50)
    
    // 娴嬭瘯ImageData
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    if (!imageData || !imageData.data) {
      throw new Error('鏃犳硶鑾峰彇ImageData')
    }
    
    console.log('鉁?Canvas鏀寔娴嬭瘯閫氳繃:', {
      canvasSize: `${canvas.width}x${canvas.height}`,
      imageDataSize: `${imageData.width}x${imageData.height}`,
      dataLength: imageData.data.length
    })
    
    return {
      success: true,
      message: 'Canvas鍔熻兘姝ｅ父',
      canvasSupported: true,
      imageDataSupported: true
    }
    
  } catch (error) {
    console.error('鉂?Canvas鏀寔娴嬭瘯澶辫触:', error.message)
    return {
      success: false,
      message: error.message,
      error: error
    }
  }
}

/**
 * 杩愯鎵€鏈変簩缁寸爜鐩稿叧娴嬭瘯
 */
export async function runAllQRTests() {
  console.log('馃殌 寮€濮嬭繍琛屾墍鏈変簩缁寸爜娴嬭瘯...')
  
  const results = []
  
  // 1. 娴嬭瘯Canvas鏀寔
  const canvasTest = testCanvasSupport()
  results.push({ name: 'Canvas鏀寔', ...canvasTest })
  
  // 2. 娴嬭瘯jsQR搴?
  const jsqrTest = await testJsQRLibrary()
  results.push({ name: 'jsQR搴?, ...jsqrTest })
  
  // 3. 娴嬭瘯瀹屾暣娴佺▼锛堝彧鏈夊墠闈㈢殑娴嬭瘯閮介€氳繃鎵嶆墽琛岋級
  if (canvasTest.success && jsqrTest.success) {
    const flowTest = await testQRCodeFlow()
    results.push({ name: '瀹屾暣娴佺▼', ...flowTest })
  } else {
    results.push({ 
      name: '瀹屾暣娴佺▼', 
      success: false, 
      message: '璺宠繃娴嬭瘯锛堝墠缃潯浠朵笉婊¤冻锛? 
    })
  }
  
  // 杈撳嚭娴嬭瘯缁撴灉
  console.log('\n馃搳 浜岀淮鐮佹祴璇曠粨鏋滄眹鎬?')
  console.log('=' * 50)
  
  const successful = results.filter(r => r.success)
  const failed = results.filter(r => !r.success)
  
  successful.forEach(r => {
    console.log(`鉁?${r.name}: 閫氳繃`)
  })
  
  failed.forEach(r => {
    console.log(`鉂?${r.name}: 澶辫触 - ${r.message}`)
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

