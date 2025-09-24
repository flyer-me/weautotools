<template>
  <view class="example-page">
    <view class="settings-section">
      <view class="section-title">重构后的组件示例</view>
      
      <!-- 使用通用设置组件 -->
      <SettingGroup 
        label="选择模式" 
        required
        hint="这是一个必填的选择项"
      >
        <OptionPicker
          v-model="selectedMode"
          :options="modeOptions"
          rangeKey="label"
          placeholder="请选择模式"
        />
      </SettingGroup>
      
      <!-- 使用预设按钮组件 -->
      <SettingGroup label="质量预设">
        <PresetButtons
          v-model="selectedQuality"
          :presets="qualityPresets"
          valueKey="value"
          labelKey="name"
        />
      </SettingGroup>
      
      <!-- 使用表单输入组件 -->
      <SettingGroup label="目标大小" optional>
        <FormInput
          v-model="targetSize"
          type="number-with-unit"
          unit="KB"
          placeholder="500"
          :rules="[validateSize]"
        />
      </SettingGroup>
      
      <!-- 使用多选预设按钮 -->
      <SettingGroup label="处理类型">
        <PresetButtons
          v-model="selectedTypes"
          :presets="processTypes"
          :multiple="true"
          valueKey="value"
          labelKey="name"
        />
      </SettingGroup>
    </view>
    
    <!-- 操作按钮 -->
    <view class="action-section">
      <button class="btn btn-primary btn-lg" @click="handleProcess">
        <uni-icons type="gear" size="16" color="white" />
        <text>开始处理</text>
      </button>
      
      <button class="btn btn-secondary btn-lg" @click="handleReset">
        <uni-icons type="refresh" size="16" color="#666" />
        <text>重置</text>
      </button>
    </view>
    
    <!-- 结果展示 -->
    <view v-if="result" class="option-group">
      <view class="option-label">处理结果</view>
      <view class="result-content">
        <text>选择的模式: {{ modeOptions[selectedMode]?.label }}</text>
        <text>质量预设: {{ selectedQuality }}</text>
        <text>目标大小: {{ targetSize }}KB</text>
        <text>处理类型: {{ selectedTypes.join(', ') }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import SettingGroup from '@/features/tools/shared/components/SettingGroup.vue'
import OptionPicker from '@/features/tools/shared/components/OptionPicker.vue'
import PresetButtons from '@/features/tools/shared/components/PresetButtons.vue'
import FormInput from '@/features/tools/shared/components/FormInput.vue'

// 响应式数据
const selectedMode = ref(0)
const selectedQuality = ref(0.8)
const targetSize = ref(500)
const selectedTypes = ref(['compress'])
const result = ref(null)

// 选项数据
const modeOptions = [
  { label: '质量优先', value: 'quality' },
  { label: '大小优先', value: 'size' },
  { label: '无损压缩', value: 'lossless' }
]

const qualityPresets = [
  { name: '高质量', value: 0.9, description: '质量优先' },
  { name: '标准', value: 0.8, description: '平衡模式' },
  { name: '压缩', value: 0.6, description: '大小优先' }
]

const processTypes = [
  { value: 'compress', name: '压缩', description: '减小文件大小' },
  { value: 'resize', name: '尺寸调整', description: '调整图片尺寸' },
  { value: 'watermark', name: '添加水印', description: '添加文字或图片水印' }
]

// 验证函数
const validateSize = (value) => {
  if (!value || value <= 0) {
    return '请输入有效的大小'
  }
  if (value > 10000) {
    return '大小不能超过10MB'
  }
  return true
}

// 事件处理
const handleProcess = () => {
  result.value = {
    mode: modeOptions[selectedMode.value],
    quality: selectedQuality.value,
    size: targetSize.value,
    types: selectedTypes.value
  }
  
  uni.showToast({
    title: '处理完成',
    icon: 'success'
  })
}

const handleReset = () => {
  selectedMode.value = 0
  selectedQuality.value = 0.8
  targetSize.value = 500
  selectedTypes.value = ['compress']
  result.value = null
  
  uni.showToast({
    title: '已重置',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/tools-common.scss';

.example-page {
  padding: $spacing-lg;
  background: $bg-secondary;
  min-height: 100vh;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $bg-light;
  border-radius: $radius-md;
  
  text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

// 响应式设计
@media (max-width: 750rpx) {
  .example-page {
    padding: $spacing-md;
  }
}
</style>
