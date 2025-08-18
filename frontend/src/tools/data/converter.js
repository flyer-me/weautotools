/**
 * 数据格式转换器
 * 支持JSON、XML、YAML格式互转
 */

import yaml from 'js-yaml'
import { parseString as parseXML, Builder as XMLBuilder } from 'xml2js'
import { FileProcessor } from '../base/FileProcessor.js'
import { validateXml as validateXmlSafe, safeXmlToJson } from './xmlUtils.js'

export class DataConverter extends FileProcessor {
  constructor() {
    super()
    this.supportedFormats = ['application/json', 'application/xml', 'application/yaml', 'text/plain']
  }

  /**
   * JSON转YAML
   * @param {string} jsonString JSON字符串
   * @returns {string} YAML字符串
   */
  jsonToYaml(jsonString) {
    try {
      const jsonObj = JSON.parse(jsonString)
      return yaml.dump(jsonObj, { 
        indent: 2,
        lineWidth: -1,
        noRefs: true,
        sortKeys: false
      })
    } catch (error) {
      throw new Error(`JSON转YAML失败: ${error.message}`)
    }
  }

  /**
   * YAML转JSON
   * @param {string} yamlString YAML字符串
   * @returns {string} JSON字符串
   */
  yamlToJson(yamlString) {
    try {
      const jsonObj = yaml.load(yamlString)
      return JSON.stringify(jsonObj, null, 2)
    } catch (error) {
      throw new Error(`YAML转JSON失败: ${error.message}`)
    }
  }

  /**
   * JSON转XML
   * @param {string} jsonString JSON字符串
   * @param {Object} options 转换选项
   * @returns {string} XML字符串
   */
  jsonToXml(jsonString, options = {}) {
    try {
      const jsonObj = JSON.parse(jsonString)

      // 如果 JSON 对象没有根节点，添加一个
      const rootName = options.rootName || 'root'
      const dataToConvert = typeof jsonObj === 'object' && !Array.isArray(jsonObj) && Object.keys(jsonObj).length === 1
        ? jsonObj
        : { [rootName]: jsonObj }

      const builder = new XMLBuilder({
        renderOpts: {
          pretty: true,
          indent: '  ',
          newline: '\n'
        },
        xmldec: {
          version: '1.0',
          encoding: 'UTF-8',
          standalone: true
        },
        headless: false
      })

      return builder.buildObject(dataToConvert)
    } catch (error) {
      throw new Error(`JSON转XML失败: ${error.message}`)
    }
  }

  /**
   * XML转JSON
   * @param {string} xmlString XML字符串
   * @param {Object} options 转换选项
   * @returns {Promise<string>} JSON字符串
   */
  xmlToJson(xmlString, options = {}) {
    return new Promise(async (resolve, reject) => {
      try {
        // 使用安全的 XML 转 JSON 方法
        const parseOptions = {
          explicitArray: false,
          ignoreAttrs: options.ignoreAttrs || false,
          mergeAttrs: options.mergeAttrs || false,
          explicitRoot: options.explicitRoot !== false,
          trim: true,
          normalize: true,
          normalizeTags: false,
          attrkey: '@',
          charkey: '#text',
          ...options
        }

        // 创建 xml2js 解析器函数
        const xml2jsParser = (xmlStr, callback) => {
          parseXML(xmlStr, parseOptions, callback)
        }

        const result = await safeXmlToJson(xmlString, xml2jsParser)
        resolve(result)
      } catch (error) {
        reject(new Error(`XML转JSON失败: ${error.message}`))
      }
    })
  }

  /**
   * YAML转XML
   * @param {string} yamlString YAML字符串
   * @param {Object} options 转换选项
   * @returns {string} XML字符串
   */
  yamlToXml(yamlString, options = {}) {
    try {
      const jsonObj = yaml.load(yamlString)
      const jsonString = JSON.stringify(jsonObj)
      return this.jsonToXml(jsonString, options)
    } catch (error) {
      throw new Error(`YAML转XML失败: ${error.message}`)
    }
  }

  /**
   * XML转YAML
   * @param {string} xmlString XML字符串
   * @param {Object} options 转换选项
   * @returns {Promise<string>} YAML字符串
   */
  async xmlToYaml(xmlString, options = {}) {
    try {
      const jsonString = await this.xmlToJson(xmlString, options)
      const jsonObj = JSON.parse(jsonString)
      return yaml.dump(jsonObj, { 
        indent: 2,
        lineWidth: -1,
        noRefs: true
      })
    } catch (error) {
      throw new Error(`XML转YAML失败: ${error.message}`)
    }
  }

  /**
   * 格式验证
   */
  validateJson(jsonString) {
    try {
      JSON.parse(jsonString)
      return { valid: true, message: 'JSON格式正确' }
    } catch (error) {
      return { 
        valid: false, 
        message: `JSON格式错误: ${error.message}`,
        line: this.getErrorLine(error.message)
      }
    }
  }

  validateYaml(yamlString) {
    try {
      yaml.load(yamlString)
      return { valid: true, message: 'YAML格式正确' }
    } catch (error) {
      return { 
        valid: false, 
        message: `YAML格式错误: ${error.message}`,
        line: error.mark?.line
      }
    }
  }

  validateXml(xmlString) {
    // 直接使用安全的 XML 验证方法
    return validateXmlSafe(xmlString)
  }

  /**
   * 格式美化
   */
  beautifyJson(jsonString) {
    try {
      const jsonObj = JSON.parse(jsonString)
      return JSON.stringify(jsonObj, null, 2)
    } catch (error) {
      throw new Error(`JSON美化失败: ${error.message}`)
    }
  }

  beautifyYaml(yamlString) {
    try {
      const jsonObj = yaml.load(yamlString)
      return yaml.dump(jsonObj, { 
        indent: 2,
        lineWidth: -1,
        noRefs: true,
        sortKeys: false
      })
    } catch (error) {
      throw new Error(`YAML美化失败: ${error.message}`)
    }
  }

  /**
   * 压缩格式（移除空白）
   */
  minifyJson(jsonString) {
    try {
      const jsonObj = JSON.parse(jsonString)
      return JSON.stringify(jsonObj)
    } catch (error) {
      throw new Error(`JSON压缩失败: ${error.message}`)
    }
  }

  /**
   * 获取支持的转换类型
   */
  getSupportedConversions() {
    return [
      { from: 'json', to: 'yaml', label: 'JSON → YAML' },
      { from: 'json', to: 'xml', label: 'JSON → XML' },
      { from: 'yaml', to: 'json', label: 'YAML → JSON' },
      { from: 'yaml', to: 'xml', label: 'YAML → XML' },
      { from: 'xml', to: 'json', label: 'XML → JSON' },
      { from: 'xml', to: 'yaml', label: 'XML → YAML' }
    ]
  }

  /**
   * 统一转换接口
   * @param {string} input 输入内容
   * @param {string} fromFormat 源格式
   * @param {string} toFormat 目标格式
   * @param {Object} options 转换选项
   * @returns {Promise<string>} 转换结果
   */
  async convert(input, fromFormat, toFormat, options = {}) {
    if (fromFormat === toFormat) {
      return input
    }

    const conversionKey = `${fromFormat}To${toFormat.charAt(0).toUpperCase() + toFormat.slice(1)}`
    
    if (typeof this[conversionKey] === 'function') {
      return await this[conversionKey](input, options)
    } else {
      throw new Error(`不支持从 ${fromFormat} 转换到 ${toFormat}`)
    }
  }

  /**
   * 批量转换
   * @param {Array} inputs 输入数据列表
   * @param {string} fromFormat 源格式
   * @param {string} toFormat 目标格式
   * @param {Object} options 转换选项
   * @param {Function} onProgress 进度回调
   * @returns {Promise<Array>} 转换结果列表
   */
  async convertBatch(inputs, fromFormat, toFormat, options = {}, onProgress = null) {
    const results = []
    const total = inputs.length
    
    for (let i = 0; i < inputs.length; i++) {
      const input = inputs[i]
      
      try {
        const result = await this.convert(input.content, fromFormat, toFormat, options)
        results.push({
          success: true,
          input: input,
          result: result,
          originalSize: input.content.length,
          convertedSize: result.length
        })
      } catch (error) {
        results.push({
          success: false,
          input: input,
          error: error.message
        })
      }
      
      // 更新进度
      if (onProgress) {
        onProgress({
          completed: i + 1,
          total,
          percentage: Math.round(((i + 1) / total) * 100),
          currentItem: input.name || `项目 ${i + 1}`
        })
      }
    }
    
    return results
  }

  /**
   * 检测数据格式
   * @param {string} content 内容
   * @returns {string} 格式类型
   */
  detectFormat(content) {
    const trimmed = content.trim()
    
    // 检测JSON
    if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || 
        (trimmed.startsWith('[') && trimmed.endsWith(']'))) {
      try {
        JSON.parse(trimmed)
        return 'json'
      } catch (e) {
        // 继续检测其他格式
      }
    }
    
    // 检测XML
    if (trimmed.startsWith('<') && trimmed.includes('>')) {
      return 'xml'
    }
    
    // 检测YAML（简单检测）
    if (trimmed.includes(':') && !trimmed.includes('<') && !trimmed.includes('{')) {
      return 'yaml'
    }
    
    return 'unknown'
  }

  /**
   * 获取错误行号
   * @param {string} errorMessage 错误信息
   * @returns {number|null} 行号
   */
  getErrorLine(errorMessage) {
    const match = errorMessage.match(/line (\d+)/i)
    return match ? parseInt(match[1]) : null
  }

  /**
   * 下载转换结果
   * @param {string} content 内容
   * @param {string} filename 文件名
   * @param {string} format 格式
   */
  downloadResult(content, filename, format) {
    const mimeTypes = {
      json: 'application/json',
      xml: 'application/xml',
      yaml: 'application/yaml'
    }
    
    const extensions = {
      json: '.json',
      xml: '.xml',
      yaml: '.yaml'
    }
    
    const blob = new Blob([content], { 
      type: mimeTypes[format] || 'text/plain' 
    })
    
    const finalFilename = filename.includes('.') ? 
      filename : `${filename}${extensions[format] || '.txt'}`
    
    this.downloadFile(blob, finalFilename)
  }
}
