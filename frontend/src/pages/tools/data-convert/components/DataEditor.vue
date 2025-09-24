<template>
  <view class="data-editor">
    <!-- 输入区域 -->
    <view class="editor-section">
      <view class="section-header">
        <text class="section-title">输入 ({{ formatOptions[fromFormatIndex].label }})</text>
        <view class="section-actions">
          <button class="action-btn" @click="validateInput">
            <uni-icons type="checkmarkempty" size="14" color="#28a745" />
            <text>验证</text>
          </button>
          <button class="action-btn" @click="beautifyInput">
            <uni-icons type="compose" size="14" color="#007aff" />
            <text>美化</text>
          </button>
          <button class="action-btn" @click="clearInput">
            <uni-icons type="clear" size="14" color="#dc3545" />
            <text>清空</text>
          </button>
        </view>
      </view>
      
      <textarea
        v-model="inputContent"
        class="content-textarea"
        :placeholder="inputPlaceholder"
        :maxlength="-1"
        @input="handleInputChange"
      />

      <!-- 格式提示 -->
      <view v-if="!inputContent.trim()" class="format-hint">
        <view class="hint-title">
          <uni-icons type="info" size="14" color="#007aff" />
          <text>{{ formatOptions[fromFormatIndex].label }} 格式说明</text>
        </view>
        <view class="hint-content">{{ formatHint }}</view>
      </view>

      <!-- 验证结果 -->
      <view v-if="validationResult" class="validation-result">
        <view 
          class="validation-message"
          :class="{ success: validationResult.valid, error: !validationResult.valid }"
        >
          <uni-icons 
            :type="validationResult.valid ? 'checkmarkempty' : 'close'" 
            size="16" 
            :color="validationResult.valid ? '#28a745' : '#dc3545'" 
          />
          <text>{{ validationResult.message }}</text>
        </view>
      </view>
    </view>

    <!-- 输出区域 -->
    <view class="editor-section">
      <view class="section-header">
        <text class="section-title">输出 ({{ formatOptions[toFormatIndex].label }})</text>
        <view class="section-actions">
          <button 
            class="action-btn" 
            @click="copyOutput"
            :disabled="!outputContent"
          >
            <uni-icons type="copy" size="14" color="#007aff" />
            <text>复制</text>
          </button>
          <button 
            class="action-btn" 
            @click="downloadOutput"
            :disabled="!outputContent"
          >
            <uni-icons type="download" size="14" color="#28a745" />
            <text>下载</text>
          </button>
        </view>
      </view>
      
      <textarea
        v-model="outputContent"
        class="content-textarea"
        placeholder="转换结果将显示在这里..."
        :maxlength="-1"
        readonly
      />
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  inputContent: {
    type: String,
    required: true
  },
  outputContent: {
    type: String,
    required: true
  },
  fromFormatIndex: {
    type: Number,
    required: true
  },
  toFormatIndex: {
    type: Number,
    required: true
  },
  formatOptions: {
    type: Array,
    required: true
  },
  validationResult: {
    type: Object,
    default: null
  },
  inputPlaceholder: {
    type: String,
    default: '请输入要转换的数据...'
  },
  formatHint: {
    type: String,
    default: ''
  }
})

// 定义组件可以发出的事件，包含类型验证
const emit = defineEmits({
  // v-model 相关事件
  'update:inputContent': (value) => typeof value === 'string',
  'update:outputContent': (value) => typeof value === 'string',
  
  // 用户操作事件
  'inputChange': () => true,      // 输入内容变化
  'validate': () => true,         // 验证数据
  'beautify': () => true,         // 美化格式
  'clear': () => true,           // 清空内容
  'copy': () => true,            // 复制输出
  'download': () => true         // 下载输出
})

const handleInputChange = () => {
  emit('inputChange')
}

const validateInput = () => {
  emit('validate')
}

const beautifyInput = () => {
  emit('beautify')
}

const clearInput = () => {
  emit('update:inputContent', '')
  emit('update:outputContent', '')
  emit('clear')
}

const copyOutput = () => {
  emit('copy')
}

const downloadOutput = () => {
  emit('download')
}
</script>

<style lang="scss" scoped>
.data-editor {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
  margin-bottom: 32rpx;

  .editor-section {
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 16rpx;
    border: 2rpx solid #f0f0f0;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
    overflow: hidden;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 24rpx;
      background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
      border-bottom: 2rpx solid #f0f0f0;

      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }

      .section-actions {
        display: flex;
        gap: 12rpx;

        .action-btn {
          display: flex;
          align-items: center;
          gap: 6rpx;
          padding: 8rpx 12rpx;
          background: transparent;
          border: 1rpx solid #e9ecef;
          border-radius: 6rpx;
          font-size: 22rpx;
          color: #666;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover:not(:disabled) {
            background: #f0f8ff;
            border-color: #007aff;
            color: #007aff;
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }
        }
      }
    }

    .content-textarea {
      flex: 1;
      min-height: 400rpx;
      padding: 24rpx;
      border: none;
      outline: none;
      font-size: 26rpx;
      line-height: 1.6;
      color: #333;
      background: transparent;
      resize: none;
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;

      &::placeholder {
        color: #999;
        font-style: italic;
      }

      &:focus {
        background: #fafbfc;
      }

      &[readonly] {
        background: #f8f9fa;
        color: #666;
      }
    }

    .format-hint {
      padding: 20rpx 24rpx;
      background: rgba(0, 122, 255, 0.05);
      border-top: 2rpx solid #f0f0f0;

      .hint-title {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 24rpx;
        font-weight: 600;
        color: #007aff;
        margin-bottom: 8rpx;
      }

      .hint-content {
        font-size: 22rpx;
        color: #666;
        line-height: 1.5;
      }
    }

    .validation-result {
      padding: 16rpx 24rpx;
      border-top: 2rpx solid #f0f0f0;

      .validation-message {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 24rpx;
        font-weight: 500;

        &.success {
          color: #28a745;
          background: rgba(40, 167, 69, 0.05);
          padding: 12rpx 16rpx;
          border-radius: 8rpx;
          border: 1rpx solid rgba(40, 167, 69, 0.1);
        }

        &.error {
          color: #dc3545;
          background: rgba(220, 53, 69, 0.05);
          padding: 12rpx 16rpx;
          border-radius: 8rpx;
          border: 1rpx solid rgba(220, 53, 69, 0.1);
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
  .data-editor {
    grid-template-columns: 1fr;
    gap: 20rpx;

    .editor-section {
      .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12rpx;
        padding: 16rpx 20rpx;

        .section-actions {
          width: 100%;
          justify-content: flex-end;

          .action-btn {
            padding: 6rpx 10rpx;
            font-size: 20rpx;
          }
        }
      }

      .content-textarea {
        min-height: 300rpx;
        padding: 20rpx;
        font-size: 24rpx;
      }

      .format-hint {
        padding: 16rpx 20rpx;

        .hint-title {
          font-size: 22rpx;
        }

        .hint-content {
          font-size: 20rpx;
        }
      }

      .validation-result {
        padding: 12rpx 20rpx;

        .validation-message {
          font-size: 22rpx;
        }
      }
    }
  }
}
</style>
