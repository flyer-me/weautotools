<!-- frontend/src/pages/logout-success/logout-success.vue -->
<template>
    <view>
        <text>您已成功注销。</text>
        <button @click="goToHome">返回首页</button>
    </view>
</template>

<script setup>
const goToHome = () => {
    uni.switchTab({ // 假设首页是Tab页
        url: '/pages/index/index'
    });
};
</script>