<template>
  <view class="file-uploader">
    <!-- 上传区域 -->
    <view 
      class="upload-area" 
      :class="{ 'drag-over': isDragOver, 'disabled': disabled }"
      @click="handleChooseFiles"
      @dragover.prevent="handleDragOver"
      @dragleave.prevent="handleDragLeave"
      @drop.prevent="handleDrop"
    >
      <view class="upload-icon">
        <uni-icons 
          :type="uploadIcon" 
          :size="iconSize" 
          :color="iconColor" 
        />
      </view>
      <view class="upload-text">
        <text class="upload-title">{{ uploadTitle }}</text>
        <text class="upload-desc" v-if="uploadDesc">{{ uploadDesc }}</text>
      </view>
    </view>
    
    <!-- 文件列表 -->
    <view v-if="fileList.length" class="file-list">
      <view class="file-list-header">
        <text class="file-count">已选择 {{ fileList.length }} 个文件</text>
        <text class="total-size">总大小: {{ formatTotalSize() }}</text>
      </view>
      
      <view 
        v-for="(file, index) in fileList" 
        :key="index"
        class="file-item"
        :class="{ 'invalid': !file.valid }"
      >
        <view class="file-icon">
          <uni-icons 
            :type="getFileIcon(file)" 
            size="24" 
            :color="file.valid ? '#007aff' : '#ff4757'" 
          />
        </view>
        
        <view class="file-info">
          <text class="file-name">{{ file.name }}</text>
          <text class="file-size">{{ formatFileSize(file.size) }}</text>
          <text v-if="!file.valid" class="file-error">{{ file.error }}</text>
        </view>
        
        <view class="file-actions">
          <uni-icons 
            type="clear" 
            size="20" 
            color="#ff4757"
            @click="removeFile(index)"
          />
        </view>
      </view>
    </view>
    
    <!-- 上传限制提示 -->
    <view v-if="showLimits" class="upload-limits">
      <text class="limit-text">
        支持格式: {{ acceptText }}
      </text>
      <text class="limit-text" v-if="maxSize">
        单文件最大: {{ formatFileSize(maxSize) }}
      </text>
      <text class="limit-text" v-if="maxCount > 1">
        最多选择: {{ maxCount }} 个文件
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, Ref } from 'vue'
import { chooseFiles, SelectedFile } from '@/utils/fileUtils'

interface ManagedFile {
  name: string;
  size: number;
  file?: File;
  path: string;
  type: string;
  valid: boolean;
  error: string;
}

const props = defineProps({
  accept: {
    type: String,
    default: '*'
  },
  multiple: {
    type: Boolean,
    default: true
  },
  maxCount: {
    type: Number,
    default: 9
  },
  maxSize: {
    type: Number,
    default: 50 * 1024 * 1024 // 50MB
  },
  uploadTitle: {
    type: String,
    default: '选择文件'
  },
  uploadDesc: {
    type: String,
    default: '点击选择或拖拽文件到此处'
  },
  acceptText: {
    type: String,
    default: '所有格式'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showLimits: {
    type: Boolean,
    default: true
  },
  validateFormat: {
    type: Function,
    default: () => true
  },
  autoValidate: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['change', 'error', 'exceed-limit'])

const fileList: Ref<ManagedFile[]> = ref([])
const isDragOver = ref(false)

// 计算属性
const uploadIcon = computed(() => {
  if (props.disabled) return 'close'
  return isDragOver.value ? 'cloud-download' : 'cloud-upload'
})

const iconSize = computed(() => {
  return isDragOver.value ? 52 : 48
})

const iconColor = computed(() => {
  if (props.disabled) return '#ccc'
  return isDragOver.value ? '#28a745' : '#007aff'
})

// 选择文件
const handleChooseFiles = async () => {
  if (props.disabled) return
  
  try {
    const result = await chooseFiles({
      count: props.maxCount,
      accept: props.accept,
      multiple: props.multiple
    })
    
    processFiles(result.tempFiles)
  } catch (error: any) {
    emit('error', error.message || '文件选择失败')
    uni.showToast({
      title: '文件选择失败',
      icon: 'none'
    })
  }
}

// 处理文件
const processFiles = (files: SelectedFile[]) => {
  const newFiles: ManagedFile[] = files.map(item => {
    const file: ManagedFile = {
      name: item.file?.name || item.path.split('/').pop() || 'unknown',
      size: item.size || 0,
      file: item.file,
      path: item.path,
      type: item.file?.type || getFileTypeFromName(item.file?.name || item.path),
      valid: true,
      error: ''
    }
    
    // 验证文件
    if (props.autoValidate) {
      validateFile(file)
    }
    
    return file
  })
  
  // 检查数量限制
  const totalCount = fileList.value.length + newFiles.length
  if (totalCount > props.maxCount) {
    emit('exceed-limit', {
      type: 'count',
      current: totalCount,
      limit: props.maxCount
    })
    
    uni.showToast({
      title: `最多只能选择${props.maxCount}个文件`,
      icon: 'none'
    })
    return
  }
  
  fileList.value.push(...newFiles)
  emit('change', fileList.value)
}

// 验证单个文件
const validateFile = (file: ManagedFile) => {
  // 大小验证
  if (file.size > props.maxSize) {
    file.valid = false
    file.error = '文件过大'
    return
  }
  
  // 格式验证
  if (!props.validateFormat(file)) {
    file.valid = false
    file.error = '不支持的格式'
    return
  }
}

// 移除文件
const removeFile = (index: number) => {
  fileList.value.splice(index, 1)
  emit('change', fileList.value)
}

// 清空文件列表
const clearFiles = () => {
  fileList.value = []
  emit('change', fileList.value)
}

// 拖拽处理
const handleDragOver = (e: DragEvent) => {
  if (props.disabled) return
  isDragOver.value = true
}

const handleDragLeave = (e: DragEvent) => {
  isDragOver.value = false
}

const handleDrop = (e: DragEvent) => {
  if (props.disabled) return
  
  isDragOver.value = false
  
  // #ifdef H5
  if (e.dataTransfer) {
    const files = Array.from(e.dataTransfer.files)
    const tempFiles: SelectedFile[] = files.map(file => ({
      file,
      path: file.name,
      name: file.name,
      size: file.size
    }))
    
    processFiles(tempFiles)
  }
  // #endif
}

// 工具函数
const formatFileSize = (size: number) => {
  if (size < 1024) return `${size}B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)}KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(1)}MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)}GB`
}

const formatTotalSize = () => {
  const total = fileList.value.reduce((sum, file) => sum + file.size, 0)
  return formatFileSize(total)
}

const getFileIcon = (file: ManagedFile) => {
  if (!file.valid) return 'close'
  
  const type = file.type.toLowerCase()
  if (type.includes('image')) return 'image'
  if (type.includes('pdf')) return 'paperclip'
  if (type.includes('json') || type.includes('xml') || type.includes('yaml')) return 'list'
  return 'doc'
}

const getFileTypeFromName = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  const typeMap: { [key: string]: string } = {
    'pdf': 'application/pdf',
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'png': 'image/png',
    'gif': 'image/gif',
    'webp': 'image/webp',
    'json': 'application/json',
    'xml': 'application/xml',
    'yaml': 'application/yaml',
    'yml': 'application/yaml'
  }
  return typeMap[ext] || 'application/octet-stream'
}

// 暴露方法给父组件
defineExpose({
  clearFiles,
  getValidFiles: () => fileList.value.filter(f => f.valid),
  getAllFiles: () => fileList.value
})
</script>

<style lang="scss" scoped>
.file-uploader {
  width: 100%;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
  border: 4rpx dashed #ddd;
  border-radius: 16rpx;
  background: #fafafa;
  transition: all 0.3s ease;
  cursor: pointer;
  
  &:hover {
    border-color: #007aff;
    background: #f0f8ff;
  }
  
  &.drag-over {
    border-color: #28a745;
    background: #f0fff4;
  }
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    
    &:hover {
      border-color: #ddd;
      background: #fafafa;
    }
  }
}

.upload-icon {
  margin-bottom: 20rpx;
}

.upload-text {
  text-align: center;
  
  .upload-title {
    display: block;
    font-size: 32rpx;
    color: #333;
    font-weight: 500;
    margin-bottom: 10rpx;
  }
  
  .upload-desc {
    display: block;
    font-size: 26rpx;
    color: #666;
  }
}

.file-list {
  margin-top: 30rpx;
}

.file-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #eee;
  margin-bottom: 20rpx;
  
  .file-count {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
  }
  
  .total-size {
    font-size: 26rpx;
    color: #666;
  }
}

.file-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  margin-bottom: 15rpx;
  
  &.invalid {
    background: #fff5f5;
    border: 2rpx solid #fed7d7;
  }
}

.file-icon {
  margin-right: 20rpx;
}

.file-info {
  flex: 1;
  
  .file-name {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 5rpx;
    word-break: break-all;
  }
  
  .file-size {
    display: block;
    font-size: 24rpx;
    color: #666;
  }
  
  .file-error {
    display: block;
    font-size: 24rpx;
    color: #e53e3e;
    margin-top: 5rpx;
  }
}

.file-actions {
  margin-left: 20rpx;
  cursor: pointer;
}

.upload-limits {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  
  .limit-text {
    display: block;
    font-size: 24rpx;
    color: #666;
    margin-bottom: 5rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}
</style>
