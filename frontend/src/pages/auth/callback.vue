<script setup>
import { onMounted } from 'vue';
import userManager from '../../services/authService';

onMounted(async () => {
    try {
        const user = await userManager.signinRedirectCallback();
        const returnUrl = user.state || '/';
        window.location.replace(returnUrl);
        console.log('登录回调成功，用户信息:', user);

        // 登录成功后，决定重定向到哪里 -旧版
        const returnPath = user.state?.returnPath || '/pages/category/category';

        // uni-app 中，tabBar 页面必须使用 switchTab
        const tabBarPages = [
            '/pages/category/category',
            '/pages/message/message',
            '/pages/user/user'
        ];

        if (tabBarPages.includes(returnPath)) {
            uni.switchTab({ url: returnPath });
        } else {
            uni.reLaunch({ url: returnPath });
        }

    } catch (error) {
        console.error('登录回调处理失败:', error);
        uni.reLaunch({ url: '/pages/auth/login-failed' });
    }
});
</script>
<template>
    <view>
        <text>正在登录，请稍候...</text>
    </view>
</template>
