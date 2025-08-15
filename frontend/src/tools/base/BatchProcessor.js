/**
 * 批量处理基础类
 * 提供批量文件处理的通用功能
 */

import { FileProcessor } from './FileProcessor.js'

export class BatchProcessor extends FileProcessor {
  constructor() {
    super()
    this.batchSize = 5 // 批量处理大小
    this.concurrency = 3 // 并发数量
  }

  /**
   * 批量处理文件
   * @param {Array} files 文件列表
   * @param {Function} processor 处理函数
   * @param {Function} onProgress 进度回调
   * @returns {Promise} 处理结果
   */
  async processBatch(files, processor, onProgress) {
    const results = []
    const total = files.length
    let completed = 0

    // 分批处理
    for (let i = 0; i < files.length; i += this.batchSize) {
      const batch = files.slice(i, i + this.batchSize)
      
      // 并发处理当前批次
      const batchPromises = batch.map(async (file, index) => {
        try {
          const result = await processor(file, i + index)
          completed++
          
          // 更新进度
          if (onProgress) {
            onProgress({
              completed,
              total,
              percentage: Math.round((completed / total) * 100),
              currentFile: file.name
            })
          }
          
          return {
            success: true,
            file,
            result,
            index: i + index
          }
        } catch (error) {
          completed++
          
          // 更新进度
          if (onProgress) {
            onProgress({
              completed,
              total,
              percentage: Math.round((completed / total) * 100),
              currentFile: file.name,
              error: error.message
            })
          }
          
          return {
            success: false,
            file,
            error: error.message,
            index: i + index
          }
        }
      })

      // 等待当前批次完成
      const batchResults = await Promise.all(batchPromises)
      results.push(...batchResults)

      // 短暂延迟，避免过度占用资源
      if (i + this.batchSize < files.length) {
        await this.delay(100)
      }
    }

    return results
  }

  /**
   * 验证批量文件
   * @param {Array} files 文件列表
   * @returns {Object} 验证结果
   */
  validateBatch(files) {
    const validFiles = []
    const invalidFiles = []

    files.forEach(file => {
      const formatValid = this.validateFormat(file)
      const sizeValid = this.validateSize(file)

      if (formatValid && sizeValid) {
        validFiles.push(file)
      } else {
        invalidFiles.push({
          file,
          reasons: [
            !formatValid && '不支持的文件格式',
            !sizeValid && '文件大小超出限制'
          ].filter(Boolean)
        })
      }
    })

    return {
      valid: validFiles,
      invalid: invalidFiles,
      totalSize: validFiles.reduce((sum, file) => sum + file.size, 0)
    }
  }

  /**
   * 创建批量下载
   * @param {Array} results 处理结果
   * @param {string} zipName 压缩包名称
   */
  async downloadBatch(results, zipName = 'batch_results.zip') {
    // #ifdef H5
    // H5端可以使用JSZip创建压缩包
    try {
      const JSZip = await import('jszip')
      const zip = new JSZip.default()

      results.forEach((result, index) => {
        if (result.success && result.result) {
          const filename = this.generateFileName(
            result.file.name, 
            `_processed_${index + 1}`
          )
          zip.file(filename, result.result)
        }
      })

      const content = await zip.generateAsync({ type: 'blob' })
      this.downloadFile(content, zipName)
    } catch (error) {
      // 降级处理：逐个下载
      this.downloadIndividually(results)
    }
    // #endif

    // #ifdef MP-WEIXIN
    // 小程序端逐个保存
    this.downloadIndividually(results)
    // #endif
  }

  /**
   * 逐个下载文件
   * @param {Array} results 处理结果
   */
  downloadIndividually(results) {
    results.forEach((result, index) => {
      if (result.success && result.result) {
        const filename = this.generateFileName(
          result.file.name, 
          `_processed_${index + 1}`
        )
        
        setTimeout(() => {
          this.downloadFile(result.result, filename)
        }, index * 500) // 延迟下载，避免浏览器阻止
      }
    })
  }

  /**
   * 延迟函数
   * @param {number} ms 延迟毫秒数
   * @returns {Promise}
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  /**
   * 获取批量处理统计信息
   * @param {Array} results 处理结果
   * @returns {Object} 统计信息
   */
  getStatistics(results) {
    const successful = results.filter(r => r.success)
    const failed = results.filter(r => !r.success)
    
    const originalSize = results.reduce((sum, r) => sum + r.file.size, 0)
    const processedSize = successful.reduce((sum, r) => {
      return sum + (r.result?.size || 0)
    }, 0)

    return {
      total: results.length,
      successful: successful.length,
      failed: failed.length,
      successRate: `${((successful.length / results.length) * 100).toFixed(1)}%`,
      originalSize: this.formatFileSize(originalSize),
      processedSize: this.formatFileSize(processedSize),
      compressionRatio: originalSize > 0 ? 
        `${(((originalSize - processedSize) / originalSize) * 100).toFixed(1)}%` : '0%'
    }
  }
}
