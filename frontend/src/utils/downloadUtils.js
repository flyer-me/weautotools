/**
 * 下载工具函数
 * 提供跨平台的文件下载功能
 */

/**
 * 下载文件
 * @param {Blob|ArrayBuffer|string} data 文件数据
 * @param {string} filename 文件名
 * @param {string} mimeType MIME类型
 */
export function downloadFile(data, filename, mimeType = 'application/octet-stream') {
  // 检查是否在浏览器环境
  if (typeof document !== 'undefined' && typeof URL !== 'undefined') {
    // H5环境
    let blob

    if (data instanceof Blob) {
      blob = data
    } else if (data instanceof ArrayBuffer) {
      blob = new Blob([data], { type: mimeType })
    } else if (typeof data === 'string') {
      // 如果是base64数据
      if (data.startsWith('data:')) {
        const link = document.createElement('a')
        link.href = data
        link.download = filename
        link.click()
        return
      } else {
        blob = new Blob([data], { type: mimeType })
      }
    } else {
      console.error('不支持的数据类型')
      return
    }

    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } else if (typeof uni !== 'undefined') {
    // 小程序或APP环境
    saveToWechatTemp(data, filename, mimeType)
  } else {
    console.error('当前环境不支持文件下载')
  }
}

/**
 * 微信小程序保存文件
 * @param {*} data 文件数据
 * @param {string} filename 文件名
 * @param {string} mimeType MIME类型
 */
function saveToWechatTemp(data, filename, mimeType) {
  const fs = uni.getFileSystemManager()
  const filePath = `${uni.env.USER_DATA_PATH}/${filename}`
  
  let writeData = data
  let encoding = 'utf8'
  
  // 处理不同类型的数据
  if (data instanceof ArrayBuffer) {
    writeData = data
    encoding = 'binary'
  } else if (data instanceof Blob) {
    // Blob需要转换为ArrayBuffer
    data.arrayBuffer().then(buffer => {
      fs.writeFile({
        filePath,
        data: buffer,
        encoding: 'binary',
        success: () => {
          saveFileToWechat(filePath, filename)
        },
        fail: (error) => {
          uni.showToast({
            title: '文件保存失败',
            icon: 'none'
          })
          console.error('文件写入失败:', error)
        }
      })
    })
    return
  }
  
  fs.writeFile({
    filePath,
    data: writeData,
    encoding,
    success: () => {
      saveFileToWechat(filePath, filename)
    },
    fail: (error) => {
      uni.showToast({
        title: '文件保存失败',
        icon: 'none'
      })
      console.error('文件写入失败:', error)
    }
  })
}

/**
 * 微信小程序保存文件到系统
 * @param {string} tempFilePath 临时文件路径
 * @param {string} filename 文件名
 */
function saveFileToWechat(tempFilePath, filename) {
  uni.saveFile({
    tempFilePath,
    success: (res) => {
      uni.showModal({
        title: '保存成功',
        content: `文件已保存到: ${res.savedFilePath}`,
        showCancel: false
      })
    },
    fail: (error) => {
      uni.showToast({
        title: '保存失败',
        icon: 'none'
      })
      console.error('文件保存失败:', error)
    }
  })
}

/**
 * APP端保存文件
 * @param {*} data 文件数据
 * @param {string} filename 文件名
 * @param {string} mimeType MIME类型
 */
function saveToAppStorage(data, filename, mimeType) {
  if (typeof plus !== 'undefined') {
    const filePath = `${plus.io.PRIVATE_DOC}/${filename}`

    plus.io.requestFileSystem(plus.io.PRIVATE_DOC, (fs) => {
      fs.root.getFile(filename, { create: true }, (fileEntry) => {
        fileEntry.createWriter((writer) => {
          writer.onwrite = () => {
            uni.showModal({
              title: '保存成功',
              content: `文件已保存到: ${filePath}`,
              showCancel: false
            })
          }

          writer.onerror = (error) => {
            uni.showToast({
              title: '保存失败',
              icon: 'none'
            })
            console.error('文件写入失败:', error)
          }

          if (data instanceof Blob) {
            writer.write(data)
          } else if (data instanceof ArrayBuffer) {
            writer.write(new Blob([data], { type: mimeType }))
          } else {
            writer.write(new Blob([data], { type: mimeType }))
          }
        })
      })
    })
  } else {
    console.error('当前环境不支持APP存储')
  }
}

/**
 * 下载图片到相册
 * @param {string} imageUrl 图片URL或base64
 * @param {string} filename 文件名（可选）
 */
export function downloadImageToAlbum(imageUrl, filename) {
  // 检查是否在浏览器环境
  if (typeof document !== 'undefined') {
    downloadFile(imageUrl, filename || 'image.png')
  } else if (typeof uni !== 'undefined') {
    if (imageUrl.startsWith('data:')) {
      // base64图片需要先保存为临时文件
      const fs = uni.getFileSystemManager()
      const tempPath = `${uni.env.USER_DATA_PATH}/temp_image_${Date.now()}.png`

      // 提取base64数据
      const base64Data = imageUrl.split(',')[1]
      const buffer = uni.base64ToArrayBuffer(base64Data)

      fs.writeFile({
        filePath: tempPath,
        data: buffer,
        encoding: 'binary',
        success: () => {
          uni.saveImageToPhotosAlbum({
            filePath: tempPath,
            success: () => {
              uni.showToast({
                title: '保存成功'
              })
            },
            fail: (error) => {
              uni.showToast({
                title: '保存失败',
                icon: 'none'
              })
              console.error('保存到相册失败:', error)
            }
          })
        },
        fail: (error) => {
          uni.showToast({
            title: '图片处理失败',
            icon: 'none'
          })
          console.error('临时文件创建失败:', error)
        }
      })
    } else {
      // 网络图片直接保存
      uni.saveImageToPhotosAlbum({
        filePath: imageUrl,
        success: () => {
          uni.showToast({
            title: '保存成功'
          })
        },
        fail: (error) => {
          uni.showToast({
            title: '保存失败',
            icon: 'none'
          })
          console.error('保存到相册失败:', error)
        }
      })
    }
  } else {
    console.error('当前环境不支持图片保存')
  }
}

/**
 * 批量下载文件
 * @param {Array} files 文件列表 [{data, filename, mimeType}]
 * @param {string} zipName 压缩包名称（H5端）
 */
export async function downloadBatch(files, zipName = 'batch_download.zip') {
  // 检查是否在浏览器环境
  if (typeof document !== 'undefined') {
    try {
      // 尝试使用JSZip创建压缩包
      const JSZip = (await import('jszip')).default
      const zip = new JSZip()

      files.forEach(file => {
        zip.file(file.filename, file.data)
      })

      const content = await zip.generateAsync({ type: 'blob' })
      downloadFile(content, zipName)
    } catch (error) {
      // 降级处理：逐个下载
      files.forEach((file, index) => {
        setTimeout(() => {
          downloadFile(file.data, file.filename, file.mimeType)
        }, index * 500)
      })
    }
  } else {
    // 小程序和APP端逐个保存
    files.forEach((file, index) => {
      setTimeout(() => {
        downloadFile(file.data, file.filename, file.mimeType)
      }, index * 1000)
    })
  }
}
