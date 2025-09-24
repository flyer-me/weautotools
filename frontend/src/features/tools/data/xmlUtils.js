/**
 * XML 工具类（迁移版）
 * 提供浏览器兼容的 XML 处理功能
 */

/** 验证 XML 格式 */
export function validateXml(xmlString) {
  return new Promise((resolve) => {
    try {
      if (typeof DOMParser !== 'undefined') {
        const parser = new DOMParser()
        const doc = parser.parseFromString(xmlString, 'text/xml')
        const parseError = doc.querySelector('parsererror')
        if (parseError) {
          resolve({ valid: false, message: `XML格式错误: ${parseError.textContent || '解析失败'}` })
        } else {
          resolve({ valid: true, message: 'XML格式正确' })
        }
        return
      }
      if (isValidXmlStructure(xmlString)) {
        resolve({ valid: true, message: 'XML格式正确' })
      } else {
        resolve({ valid: false, message: 'XML格式错误: 标签结构不正确' })
      }
    } catch (error) {
      resolve({ valid: false, message: `XML格式错误: ${error.message}` })
    }
  })
}

function isValidXmlStructure(xmlString) {
  try {
    const cleanXml = xmlString.replace(/<!--[\s\S]*?-->/g, '').replace(/<\?xml[^>]*\?>/g, '').trim()
    if (!cleanXml.startsWith('<') || !cleanXml.endsWith('>')) {
      return false
    }
    const openTags = []
    const tagRegex = /<\/?([a-zA-Z][a-zA-Z0-9]*)[^>]*>/g
    let match
    while ((match = tagRegex.exec(cleanXml)) !== null) {
      const tagName = match[1]
      const isClosing = match[0].startsWith('</')
      const isSelfClosing = match[0].endsWith('/>')
      if (isSelfClosing) continue
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
      const jsonObj = xmlNodeToObject(doc.documentElement)
      resolve(JSON.stringify(jsonObj, null, 2))
    } catch (error) {
      reject(new Error(`XML转JSON失败: ${error.message}`))
    }
  })
}

function xmlNodeToObject(node) {
  const obj = {}
  if (node.attributes && node.attributes.length > 0) {
    for (let i = 0; i < node.attributes.length; i++) {
      const attr = node.attributes[i]
      obj[`@${attr.name}`] = attr.value
    }
  }
  const children = Array.from(node.childNodes)
  const textContent = node.textContent?.trim()
  if (children.length === 0) {
    return textContent || ''
  }
  const elementChildren = children.filter(child => child.nodeType === 1)
  const textNodes = children.filter(child => child.nodeType === 3 && child.textContent.trim())
  if (elementChildren.length === 0 && textNodes.length > 0) {
    return textContent
  }
  for (const child of elementChildren) {
    const childName = child.tagName
    const childObj = xmlNodeToObject(child)
    if (obj[childName]) {
      if (!Array.isArray(obj[childName])) {
        obj[childName] = [obj[childName]]
      }
      obj[childName].push(childObj)
    } else {
      obj[childName] = childObj
    }
  }
  if (textContent && elementChildren.length > 0) {
    obj['#text'] = textContent
  }
  return obj
}

export function safeXmlToJson(xmlString, xml2jsParser) {
  return new Promise(async (resolve, reject) => {
    try {
      if (typeof DOMParser !== 'undefined') {
        try {
          const result = await xmlToJsonWithDOMParser(xmlString)
          resolve(result)
          return
        } catch (domError) {
          console.warn('DOMParser 转换失败，回退到 xml2js:', domError.message)
        }
      }
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
