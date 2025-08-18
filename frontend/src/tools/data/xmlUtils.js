/**
 * XML 工具类
 * 提供浏览器兼容的 XML 处理功能
 */

/**
 * 验证 XML 格式
 * @param {string} xmlString XML字符串
 * @returns {Promise<Object>} 验证结果
 */
export function validateXml(xmlString) {
  return new Promise((resolve) => {
    try {
      // 优先使用浏览器原生的 DOMParser
      if (typeof DOMParser !== 'undefined') {
        const parser = new DOMParser()
        const doc = parser.parseFromString(xmlString, 'text/xml')
        const parseError = doc.querySelector('parsererror')
        
        if (parseError) {
          resolve({ 
            valid: false, 
            message: `XML格式错误: ${parseError.textContent || '解析失败'}`
          })
        } else {
          resolve({ valid: true, message: 'XML格式正确' })
        }
        return
      }
      
      // 如果没有 DOMParser，使用简单的正则验证
      if (isValidXmlStructure(xmlString)) {
        resolve({ valid: true, message: 'XML格式正确' })
      } else {
        resolve({ 
          valid: false, 
          message: 'XML格式错误: 标签结构不正确'
        })
      }
    } catch (error) {
      resolve({ 
        valid: false, 
        message: `XML格式错误: ${error.message}`
      })
    }
  })
}

/**
 * 简单的 XML 结构验证
 * @param {string} xmlString XML字符串
 * @returns {boolean} 是否有效
 */
function isValidXmlStructure(xmlString) {
  try {
    // 移除注释和声明
    const cleanXml = xmlString
      .replace(/<!--[\s\S]*?-->/g, '')
      .replace(/<\?xml[^>]*\?>/g, '')
      .trim()
    
    // 检查是否有根元素
    if (!cleanXml.startsWith('<') || !cleanXml.endsWith('>')) {
      return false
    }
    
    // 简单的标签匹配检查
    const openTags = []
    const tagRegex = /<\/?([a-zA-Z][a-zA-Z0-9]*)[^>]*>/g
    let match
    
    while ((match = tagRegex.exec(cleanXml)) !== null) {
      const tagName = match[1]
      const isClosing = match[0].startsWith('</')
      const isSelfClosing = match[0].endsWith('/>')
      
      if (isSelfClosing) {
        continue
      }
      
      if (isClosing) {
        if (openTags.length === 0 || openTags.pop() !== tagName) {
          return false
        }
      } else {
        openTags.push(tagName)
      }
    }
    
    return openTags.length === 0
  } catch (error) {
    return false
  }
}

/**
 * 使用 DOMParser 将 XML 转换为 JSON
 * @param {string} xmlString XML字符串
 * @returns {Promise<string>} JSON字符串
 */
export function xmlToJsonWithDOMParser(xmlString) {
  return new Promise((resolve, reject) => {
    try {
      if (typeof DOMParser === 'undefined') {
        reject(new Error('DOMParser 不可用'))
        return
      }
      
      const parser = new DOMParser()
      const doc = parser.parseFromString(xmlString, 'text/xml')
      const parseError = doc.querySelector('parsererror')
      
      if (parseError) {
        reject(new Error(`XML解析失败: ${parseError.textContent}`))
        return
      }
      
      // 将 DOM 转换为 JSON 对象
      const jsonObj = xmlNodeToObject(doc.documentElement)
      resolve(JSON.stringify(jsonObj, null, 2))
    } catch (error) {
      reject(new Error(`XML转JSON失败: ${error.message}`))
    }
  })
}

/**
 * 将 XML 节点转换为 JavaScript 对象
 * @param {Element} node XML节点
 * @returns {Object} JavaScript对象
 */
function xmlNodeToObject(node) {
  const obj = {}
  
  // 处理属性
  if (node.attributes && node.attributes.length > 0) {
    for (let i = 0; i < node.attributes.length; i++) {
      const attr = node.attributes[i]
      obj[`@${attr.name}`] = attr.value
    }
  }
  
  // 处理子节点
  const children = Array.from(node.childNodes)
  const textContent = node.textContent?.trim()
  
  if (children.length === 0) {
    return textContent || ''
  }
  
  const elementChildren = children.filter(child => child.nodeType === 1) // Element nodes
  const textNodes = children.filter(child => child.nodeType === 3 && child.textContent.trim()) // Text nodes
  
  if (elementChildren.length === 0 && textNodes.length > 0) {
    // 只有文本内容
    return textContent
  }
  
  // 处理元素子节点
  for (const child of elementChildren) {
    const childName = child.tagName
    const childObj = xmlNodeToObject(child)
    
    if (obj[childName]) {
      // 如果已存在同名元素，转换为数组
      if (!Array.isArray(obj[childName])) {
        obj[childName] = [obj[childName]]
      }
      obj[childName].push(childObj)
    } else {
      obj[childName] = childObj
    }
  }
  
  // 如果有文本内容且有子元素，添加文本内容
  if (textContent && elementChildren.length > 0) {
    obj['#text'] = textContent
  }
  
  return obj
}

/**
 * 安全的 XML 转 JSON 转换
 * 优先使用 DOMParser，回退到 xml2js
 * @param {string} xmlString XML字符串
 * @param {Function} xml2jsParser xml2js 解析函数
 * @returns {Promise<string>} JSON字符串
 */
export function safeXmlToJson(xmlString, xml2jsParser) {
  return new Promise(async (resolve, reject) => {
    try {
      // 首先尝试使用 DOMParser
      if (typeof DOMParser !== 'undefined') {
        try {
          const result = await xmlToJsonWithDOMParser(xmlString)
          resolve(result)
          return
        } catch (domError) {
          console.warn('DOMParser 转换失败，回退到 xml2js:', domError.message)
        }
      }
      
      // 回退到 xml2js
      if (xml2jsParser) {
        xml2jsParser(xmlString, (err, result) => {
          if (err) {
            reject(new Error(`XML转JSON失败: ${err.message}`))
          } else {
            resolve(JSON.stringify(result, null, 2))
          }
        })
      } else {
        reject(new Error('无可用的 XML 解析器'))
      }
    } catch (error) {
      reject(new Error(`XML转JSON失败: ${error.message}`))
    }
  })
}
