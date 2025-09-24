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

    for (let i = 0; i < files.length; i += this.batchSize) {
      const batch = files.slice(i, i + this.batchSize)

      const batchPromises = batch.map(async (file, index) => {
        try {
          const result = await processor(file, i + index)
          completed++

          if (onProgress) {
            onProgress({
              completed,
              total,
              percentage: Math.round((completed / total) * 100),
              currentFile: file.name
            })
          }

          return { success: true, file, result, index: i + index }
        } catch (error) {
          completed++

          if (onProgress) {
            onProgress({
              completed,
              total,
              percentage: Math.round((completed / total) * 100),
              currentFile: file.name,
              error: error.message
            })
          }

          return { success: false, file, error: error.message, index: i + index }
        }
      })

      const batchResults = await Promise.all(batchPromises)
      results.push(...batchResults)

      if (i + this.batchSize < files.length) {
        await this.delay(100)
      }
    }

    return results
  }

  /**
   * 逐个下载文件集合
   */
  downloadIndividually(results) {
    results.forEach((result, index) => {
      if (result.success && result.result) {
        const filename = this.generateFileName(result.file.name, `_processed_${index + 1}`)
        setTimeout(() => {
          this.downloadFile(result.result, filename)
        }, index * 500)
      }
    })
  }

  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  /**
   * 汇总统计
   */
  getStatistics(results) {
    const successful = results.filter(r => r.success)
    const failed = results.filter(r => !r.success)
    const originalSize = results.reduce((sum, r) => sum + (r.file.size || 0), 0)
    const processedSize = successful.reduce((sum, r) => sum + (r.result?.size || 0), 0)

    return {
      total: results.length,
      successful: successful.length,
      failed: failed.length,
      successRate: `${((successful.length / results.length) * 100).toFixed(1)}%`,
      originalSize: this.formatFileSize(originalSize),
      processedSize: this.formatFileSize(processedSize),
      compressionRatio: originalSize > 0 ? `${(((originalSize - processedSize) / originalSize) * 100).toFixed(1)}%` : '0%'
    }
  }
}
