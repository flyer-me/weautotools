<!-- frontend/src/pages/login-failed/login-failed.vue -->
<template>
    <view>
        <text>登录失败，请重试。</text>
        <button @click="goToUser">返回</button>
    </view>
</template>

<script setup>
const goToUser = () => {
    uni.reLaunch({
        url: '/pages/user/user'
    });
};
</script>