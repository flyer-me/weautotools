/**
 * 文件处理工具函数
 * 提供跨平台的文件选择和处理功能
 */

interface ChooseFilesOptions {
  /** 最多可以选择的文件数量 */
  count?: number;
  /** 文件类型，默认'file' */
  type?: string;
  /** 接受的文件类型，例如 'image/*' */
  accept?: string;
  /** 是否多选 */
  multiple?: boolean;
}

export interface SelectedFile {
  /** 文件路径 */
  path: string;
  /** 文件对象 */
  file: File;
  /** 文件大小 */
  size: number;
  /** 文件名 */
  name: string;
  /** 文件类型 */
  type?: string;
}

interface TempFile extends Omit<SelectedFile, 'type'> {
  // 兼容旧版类型
}

interface ChooseFilesResult {
  /** 选择的文件列表 */
  tempFiles: TempFile[];
}

interface ChooseImagesOptions {
  /** 最多可以选择的图片数量 */
  count?: number;
  /** 所选的图片的尺寸 */
  sizeType?: Array<'original' | 'compressed'>;
  /** 选择图片的来源 */
  sourceType?: Array<'album' | 'camera'>;
}

interface ChooseImagesResult {
  /** 图片的本地文件路径列表 */
  tempFilePaths: string[];
  /** 图片的本地文件列表 */
  tempFiles: Array<{
    /** 本地文件路径 */
    path: string;
    /** 本地文件大小，单位B */
    size: number;
  }>;
}

interface FileInfo {
  /** 文件大小，单位B */
  size: number;
}

declare const uni: any;
declare const plus: any;

/**
 * 选择文件
 * @param options 选择选项
 * @returns 文件选择结果
 */
export function chooseFiles(options: ChooseFilesOptions = {}): Promise<{ tempFiles: SelectedFile[] }> {
  const {
    count = 9,
    type = 'file',
    accept = '*/*',
    multiple = true
  } = options;

  return new Promise((resolve, reject) => {
    // 检查是否在浏览器环境
    if (typeof document !== 'undefined') {
      // H5环境
      const input = document.createElement('input');
      input.type = 'file';
      input.multiple = multiple;
      input.accept = accept;

      input.onchange = (e: Event) => {
        const target = e.target as HTMLInputElement;
        const files = Array.from(target.files || []);
        resolve({
          tempFiles: files.map(file => ({
            path: file.name,
            file,
            size: file.size,
            name: file.name,
            type: file.type
          }))
        });
      };

      input.onerror = () => reject(new Error('文件选择失败'));
      input.click();
    } else if (typeof uni !== 'undefined') {
      // 小程序或APP环境
      uni.chooseMessageFile({
        count,
        type,
        success: resolve,
        fail: reject
      });
    } else {
      reject(new Error('当前环境不支持文件选择'));
    }
  });
}

/**
 * 选择图片文件
 * @param options 选择选项
 * @returns 图片选择结果
 */
export function chooseImages(options: ChooseImagesOptions = {}): Promise<ChooseImagesResult> {
  const {
    count = 9,
    sizeType = ['original', 'compressed'],
    sourceType = ['album', 'camera']
  } = options;

  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType,
      sourceType,
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 获取文件信息
 * @param filePath 文件路径
 * @returns 文件信息
 */
export function getFileInfo(filePath: string): Promise<FileInfo> {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      uni.getFileInfo({
        filePath,
        success: resolve,
        fail: reject
      });
    } else {
      // H5端文件信息已在选择时获取
      resolve({ size: 0 });
    }
  });
}

/**
 * 读取文件内容
 * @param filePath 文件路径或File对象
 * @param encoding 编码方式
 * @returns 文件内容
 */
export function readFile(filePath: string | File, encoding: string = 'utf8'): Promise<string | ArrayBuffer> {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined' && typeof filePath === 'string') {
      const fs = uni.getFileSystemManager();
      fs.readFile({
        filePath,
        encoding,
        success: (res: { data: string | ArrayBuffer }) => resolve(res.data),
        fail: reject
      });
    } else if (filePath instanceof File) {
      // H5端使用FileReader
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target?.result as string | ArrayBuffer);
      reader.onerror = reject;

      if (encoding === 'base64') {
        reader.readAsDataURL(filePath);
      } else {
        reader.readAsText(filePath, encoding);
      }
    } else {
      reject(new Error('H5端需要传入File对象'));
    }
  });
}

/**
 * 写入文件
 * @param filePath 文件路径
 * @param data 文件数据
 * @param encoding 编码方式
 * @returns 写入结果
 */
export function writeFile(filePath: string, data: any, encoding: string = 'utf8'): Promise<void> {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager();
      fs.writeFile({
        filePath,
        data,
        encoding,
        success: resolve,
        fail: reject
      });
    } else {
      // H5端不支持直接写入文件系统
      reject(new Error('H5端不支持写入文件'));
    }
  });
}

/**
 * 保存文件到相册
 * @param filePath 文件路径
 * @returns 保存结果
 */
export function saveImageToPhotosAlbum(filePath: string): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.saveImageToPhotosAlbum({
      filePath,
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 预览图片
 * @param urls 图片URL数组
 * @param current 当前显示图片索引或URL
 * @returns 预览结果
 */
export function previewImage(urls: string[], current: number | string = 0): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.previewImage({
      urls,
      current: typeof current === 'number' ? current : urls.indexOf(current),
      success: resolve,
      fail: reject
    });
  });
}

/**
 * 获取临时文件路径
 * @param fileName 文件名
 * @returns 临时文件路径
 */
export function getTempFilePath(fileName: string): string {
  if (typeof uni !== 'undefined' && uni.env) {
    return `${uni.env.USER_DATA_PATH}/${fileName}`;
  } else if (typeof plus !== 'undefined') {
    return `${plus.io.PRIVATE_DOC}/${fileName}`;
  } else {
    return URL.createObjectURL(new Blob([], { type: 'application/octet-stream' }));
  }
}

/**
 * 检查文件是否存在
 * @param filePath 文件路径
 * @returns 检查结果
 */
export function fileExists(filePath: string): Promise<boolean> {
  return new Promise((resolve) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager();
      fs.access({
        path: filePath,
        success: () => resolve(true),
        fail: () => resolve(false)
      });
    } else {
      // H5端默认返回false
      resolve(false);
    }
  });
}

/**
 * 删除文件
 * @param filePath 文件路径
 * @returns 删除结果
 */
export function removeFile(filePath: string): Promise<void> {
  return new Promise((resolve, reject) => {
    if (typeof uni !== 'undefined') {
      const fs = uni.getFileSystemManager();
      fs.unlink({
        filePath,
        success: resolve,
        fail: reject
      });
    } else {
      // H5端不支持直接删除文件
      reject(new Error('H5端不支持删除文件'));
    }
  });
}
