<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  },
}
</script>

<style lang="scss">
/*每个页面公共css */

/* 全局重置样式 */
page {
  background-color: $uni-bg-color-light;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  line-height: 1.6;
}

/* 通用工具类 */
.flex {
  display: flex;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

/* 文字颜色工具类 */
.text-primary {
  color: $uni-color-primary;
}

.text-secondary {
  color: $uni-color-secondary;
}

.text-success {
  color: $uni-color-success;
}

.text-warning {
  color: $uni-color-warning;
}

.text-error {
  color: $uni-color-error;
}

.text-muted {
  color: $uni-text-color-grey;
}

/* 背景色工具类 */
.bg-primary {
  background-color: $uni-color-primary;
}

.bg-secondary {
  background-color: $uni-color-secondary;
}

.bg-white {
  background-color: $uni-bg-color;
}

.bg-grey {
  background-color: $uni-bg-color-grey;
}

/* 间距工具类 */
.m-xs { margin: $uni-spacing-xs; }
.m-sm { margin: $uni-spacing-sm; }
.m-md { margin: $uni-spacing-md; }
.m-lg { margin: $uni-spacing-lg; }
.m-xl { margin: $uni-spacing-xl; }

.mt-xs { margin-top: $uni-spacing-xs; }
.mt-sm { margin-top: $uni-spacing-sm; }
.mt-md { margin-top: $uni-spacing-md; }
.mt-lg { margin-top: $uni-spacing-lg; }
.mt-xl { margin-top: $uni-spacing-xl; }

.mb-xs { margin-bottom: $uni-spacing-xs; }
.mb-sm { margin-bottom: $uni-spacing-sm; }
.mb-md { margin-bottom: $uni-spacing-md; }
.mb-lg { margin-bottom: $uni-spacing-lg; }
.mb-xl { margin-bottom: $uni-spacing-xl; }

.p-xs { padding: $uni-spacing-xs; }
.p-sm { padding: $uni-spacing-sm; }
.p-md { padding: $uni-spacing-md; }
.p-lg { padding: $uni-spacing-lg; }
.p-xl { padding: $uni-spacing-xl; }

/* 圆角工具类 */
.rounded-sm {
  border-radius: $uni-border-radius-sm;
}

.rounded {
  border-radius: $uni-border-radius-base;
}

.rounded-lg {
  border-radius: $uni-border-radius-lg;
}

.rounded-circle {
  border-radius: $uni-border-radius-circle;
}

/* 阴影工具类 */
.shadow-sm {
  box-shadow: $uni-box-shadow-sm;
}

.shadow {
  box-shadow: $uni-box-shadow-base;
}

.shadow-lg {
  box-shadow: $uni-box-shadow-lg;
}

/* 动画工具类 */
.transition {
  transition: all $uni-animation-duration-base $uni-animation-timing-function;
}

.transition-fast {
  transition: all $uni-animation-duration-fast $uni-animation-timing-function;
}

.transition-slow {
  transition: all $uni-animation-duration-slow $uni-animation-timing-function;
}

/* 响应式工具类 */
.hidden {
  display: none;
}

.visible {
  display: block;
}

/* 按钮基础样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 16rpx 32rpx;
  border-radius: $uni-border-radius-base;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all $uni-animation-duration-base $uni-animation-timing-function;

  &:active {
    transform: translateY(1rpx);
  }

  &.btn-primary {
    background-color: $uni-color-primary;
    color: #fff;

    &:hover {
      background-color: $uni-color-link-hover;
    }
  }

  &.btn-secondary {
    background-color: $uni-color-secondary;
    color: #fff;

    &:hover {
      opacity: 0.9;
    }
  }

  &.btn-outline {
    background-color: transparent;
    border: 2rpx solid $uni-color-primary;
    color: $uni-color-primary;

    &:hover {
      background-color: $uni-color-primary;
      color: #fff;
    }
  }

  &.btn-ghost {
    background-color: transparent;
    color: $uni-color-primary;

    &:hover {
      background-color: $uni-bg-color-active;
    }
  }

  &.btn-sm {
    padding: 12rpx 24rpx;
    font-size: 24rpx;
  }

  &.btn-lg {
    padding: 20rpx 40rpx;
    font-size: 32rpx;
  }

  &.btn-block {
    width: 100%;
  }

  &.btn-disabled {
    opacity: 0.6;
    cursor: not-allowed;

    &:hover {
      transform: none;
    }
  }
}

/* 卡片基础样式 */
.card {
  background-color: $uni-bg-color;
  border-radius: $uni-border-radius-base;
  box-shadow: $uni-box-shadow-sm;
  overflow: hidden;

  &.card-bordered {
    border: 1rpx solid $uni-border-color-light;
    box-shadow: none;
  }
}

/* 输入框基础样式 */
.input {
  padding: 16rpx 24rpx;
  border: 2rpx solid $uni-border-color;
  border-radius: $uni-border-radius-base;
  font-size: 28rpx;
  background-color: $uni-bg-color;
  transition: all $uni-animation-duration-base $uni-animation-timing-function;

  &:focus {
    border-color: $uni-color-primary;
    box-shadow: 0 0 0 4rpx rgba(24, 144, 255, 0.2);
  }

  &.input-error {
    border-color: $uni-color-error;

    &:focus {
      box-shadow: 0 0 0 4rpx rgba(255, 77, 79, 0.2);
    }
  }
}
</style>
