/**
 * 文件处理工具函数
 * 提供跨平台的文件选择和处理功能
 */

/**
 * 选择文件
 * @param {Object} options 选择选项
 * @returns {Promise} 文件选择结果
 */
export function chooseFiles(options = {}) {
  const {
    count = 9,
    type = 'file',
    accept = '*/*',
    multiple = true
  } = options

  return new Promise((resolve, reject) => {
    // 检查是否在浏览器环境
    if (typeof document !== 'undefined') {
      // H5环境
      const input = document.createElement('input')
      input.type = 'file'
      input.multiple = multiple
      input.accept = accept

      input.onchange = (e) => {
        const files = Array.from(e.target.files)
        resolve({
          tempFiles: files.map(file => ({
            path: file.name,
            file,
            size: file.size,
            name: file.name
          }))
        })
      }

      input.onerror = () => reject(new Error('文件选择失败'))
      input.click()
    } else if (typeof uni !== 'undefined') {
      // 小程序或APP环境
      uni.chooseMessageFile({
        count,
        type,
        success: resolve,
        fail: reject
      })
    } else {
      reject(new Error('当前环境不支持文件选择'))
    }
  })
}

/**
 * 选择图片文件
 * @param {Object} options 选择选项
 * @returns {Promise} 图片选择结果
 */
export function chooseImages(options = {}) {
  const {
    count = 9,
    sizeType = ['original', 'compressed'],
    sourceType = ['album', 'camera']
  } = options

  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType,
      sourceType,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 获取文件信息
 * @param {string} filePath 文件路径
 * @returns {Promise} 文件信息
 */
export function getFileInfo(filePath) {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      uni.getFileInfo({
        filePath,
        success: resolve,
        fail: reject
      })
    } else {
      // H5端文件信息已在选择时获取
      resolve({ size: 0 })
    }
  })
}

/**
 * 读取文件内容
 * @param {string} filePath 文件路径
 * @param {string} encoding 编码方式
 * @returns {Promise} 文件内容
 */
export function readFile(filePath, encoding = 'utf8') {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager()
      fs.readFile({
        filePath,
        encoding,
        success: (res) => resolve(res.data),
        fail: reject
      })
    } else {
      // H5端使用FileReader
      if (filePath instanceof File) {
        const reader = new FileReader()
        reader.onload = (e) => resolve(e.target.result)
        reader.onerror = reject

        if (encoding === 'base64') {
          reader.readAsDataURL(filePath)
        } else {
          reader.readAsText(filePath, encoding)
        }
      } else {
        reject(new Error('H5端需要传入File对象'))
      }
    }
  })
}

/**
 * 写入文件
 * @param {string} filePath 文件路径
 * @param {*} data 文件数据
 * @param {string} encoding 编码方式
 * @returns {Promise} 写入结果
 */
export function writeFile(filePath, data, encoding = 'utf8') {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager()
      fs.writeFile({
        filePath,
        data,
        encoding,
        success: resolve,
        fail: reject
      })
    } else {
      // H5端不支持直接写入文件系统
      reject(new Error('H5端不支持写入文件'))
    }
  })
}

/**
 * 保存文件到相册
 * @param {string} filePath 文件路径
 * @returns {Promise} 保存结果
 */
export function saveImageToPhotosAlbum(filePath) {
  return new Promise((resolve, reject) => {
    uni.saveImageToPhotosAlbum({
      filePath,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 预览图片
 * @param {Array} urls 图片URL数组
 * @param {number} current 当前显示图片索引
 * @returns {Promise} 预览结果
 */
export function previewImage(urls, current = 0) {
  return new Promise((resolve, reject) => {
    uni.previewImage({
      urls,
      current: typeof current === 'number' ? current : urls.indexOf(current),
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 获取临时文件路径
 * @param {string} fileName 文件名
 * @returns {string} 临时文件路径
 */
export function getTempFilePath(fileName) {
  if (typeof uni !== 'undefined' && uni.env) {
    return `${uni.env.USER_DATA_PATH}/${fileName}`
  } else if (typeof plus !== 'undefined') {
    return `${plus.io.PRIVATE_DOC}/${fileName}`
  } else {
    return fileName
  }
}

/**
 * 检查文件是否存在
 * @param {string} filePath 文件路径
 * @returns {Promise} 检查结果
 */
export function fileExists(filePath) {
  return new Promise((resolve) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager()
      fs.access({
        path: filePath,
        success: () => resolve(true),
        fail: () => resolve(false)
      })
    } else {
      resolve(false) // H5端无法检查文件系统
    }
  })
}

/**
 * 删除文件
 * @param {string} filePath 文件路径
 * @returns {Promise} 删除结果
 */
export function removeFile(filePath) {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager()
      fs.unlink({
        filePath,
        success: resolve,
        fail: reject
      })
    } else {
      resolve() // H5端无需删除
    }
  })
}
