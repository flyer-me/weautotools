/**
 * 进度跟踪器
 * 提供统一的进度管理和反馈
 */

export class ProgressTracker {
  constructor(total = 0, onUpdate = null) {
    this.total = total
    this.current = 0
    this.onUpdate = onUpdate
    this.startTime = null
    this.status = 'idle' // idle, running, completed, error
    this.message = ''
    this.details = {}
  }

  /**
   * 开始进度跟踪
   * @param {number} total 总数量
   */
  start(total = null) {
    if (total !== null) {
      this.total = total
    }
    this.current = 0
    this.startTime = Date.now()
    this.status = 'running'
    this.message = '开始处理...'
    this.notifyUpdate()
  }

  /**
   * 增加进度
   * @param {number} increment 增加量
   * @param {string} message 状态消息
   * @param {Object} details 详细信息
   */
  increment(increment = 1, message = '', details = {}) {
    this.current = Math.min(this.current + increment, this.total)
    
    if (message) {
      this.message = message
    }
    
    this.details = { ...this.details, ...details }
    this.notifyUpdate()
  }

  /**
   * 设置当前进度
   * @param {number} current 当前进度
   * @param {string} message 状态消息
   * @param {Object} details 详细信息
   */
  setCurrent(current, message = '', details = {}) {
    this.current = Math.min(Math.max(current, 0), this.total)
    
    if (message) {
      this.message = message
    }
    
    this.details = { ...this.details, ...details }
    this.notifyUpdate()
  }

  /**
   * 完成进度
   * @param {string} message 完成消息
   */
  complete(message = '处理完成') {
    this.current = this.total
    this.status = 'completed'
    this.message = message
    this.notifyUpdate()
  }

  /**
   * 设置错误状态
   * @param {string} message 错误消息
   * @param {Object} details 错误详情
   */
  error(message = '处理失败', details = {}) {
    this.status = 'error'
    this.message = message
    this.details = { ...this.details, ...details }
    this.notifyUpdate()
  }

  /**
   * 重置进度
   */
  reset() {
    this.current = 0
    this.startTime = null
    this.status = 'idle'
    this.message = ''
    this.details = {}
    this.notifyUpdate()
  }

  /**
   * 获取进度百分比
   * @returns {number} 百分比 (0-100)
   */
  getPercentage() {
    if (this.total === 0) return 0
    return Math.round((this.current / this.total) * 100)
  }

  /**
   * 获取剩余时间估算
   * @returns {string} 剩余时间描述
   */
  getEstimatedTimeRemaining() {
    if (!this.startTime || this.current === 0 || this.status !== 'running') {
      return '计算中...'
    }

    const elapsed = Date.now() - this.startTime
    const rate = this.current / elapsed // 每毫秒处理数量
    const remaining = this.total - this.current
    const estimatedMs = remaining / rate

    return this.formatDuration(estimatedMs)
  }

  /**
   * 获取已用时间
   * @returns {string} 已用时间描述
   */
  getElapsedTime() {
    if (!this.startTime) return '0秒'
    
    const elapsed = Date.now() - this.startTime
    return this.formatDuration(elapsed)
  }

  /**
   * 获取处理速度
   * @returns {string} 处理速度描述
   */
  getProcessingRate() {
    if (!this.startTime || this.current === 0) {
      return '0/秒'
    }

    const elapsed = (Date.now() - this.startTime) / 1000 // 转换为秒
    const rate = this.current / elapsed
    
    if (rate < 1) {
      return `${(rate * 60).toFixed(1)}/分钟`
    }
    
    return `${rate.toFixed(1)}/秒`
  }

  /**
   * 获取完整状态信息
   * @returns {Object} 状态信息
   */
  getStatus() {
    return {
      current: this.current,
      total: this.total,
      percentage: this.getPercentage(),
      status: this.status,
      message: this.message,
      details: this.details,
      elapsedTime: this.getElapsedTime(),
      estimatedTimeRemaining: this.getEstimatedTimeRemaining(),
      processingRate: this.getProcessingRate()
    }
  }

  /**
   * 通知更新
   */
  notifyUpdate() {
    if (this.onUpdate && typeof this.onUpdate === 'function') {
      this.onUpdate(this.getStatus())
    }
  }

  /**
   * 格式化持续时间
   * @param {number} ms 毫秒数
   * @returns {string} 格式化后的时间
   */
  formatDuration(ms) {
    if (ms < 1000) return '不到1秒'
    
    const seconds = Math.floor(ms / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    
    if (hours > 0) {
      return `${hours}小时${minutes % 60}分钟`
    } else if (minutes > 0) {
      return `${minutes}分钟${seconds % 60}秒`
    } else {
      return `${seconds}秒`
    }
  }

  /**
   * 设置更新回调
   * @param {Function} callback 回调函数
   */
  setUpdateCallback(callback) {
    this.onUpdate = callback
  }
}
