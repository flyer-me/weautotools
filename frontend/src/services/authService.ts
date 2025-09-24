import { UserManager, WebStorageStateStore, UserManagerSettings } from 'oidc-client-ts';
import { getConfig } from '@/config/env';

// 显示提示信息
const showToast = (message: string) => {
    if (typeof uni !== 'undefined' && uni.showToast) {
        uni.showToast({
            title: message,
            icon: 'none',
            duration: 3000
        });
    } else {
        console.log('Toast:', message);
    }
};

const settings: UserManagerSettings = {
      // 你的Spring Authorization Server的地址
      authority: getConfig('AUTH_AUTHORITY_URL'),

      // 与后端配置中完全匹配的 client_id
      client_id: 'weautotools-frontend-client',

      // 登录成功后，授权服务器重定向回来的地址
      redirect_uri: 'http://localhost:5173/pages/auth/callback',

      // 注销后重定向回来的地址
      post_logout_redirect_uri: 'http://localhost:5173/pages/auth/logout-success',

      // 必须是 'code'
      response_type: 'code',

      // 与后端 RegisteredClient 中配置中完全匹配的 scope
      scope: 'openid profile api.read api.write',

      // 自动静默刷新令牌
      automaticSilentRenew: true,

      // 静默请求超时（秒）
      silentRequestTimeoutInSeconds: 10,

      // 用户存储
      userStore: new WebStorageStateStore({ store: window.localStorage }),
      
      // 调试模式
      monitorSession: false,
      
      // 登出时撤销访问令牌
      revokeTokensOnSignout: true,
      // 在静默续订时包含ID令牌
      includeIdTokenInSilentRenew: true
  };

  const userManager = new UserManager(settings);
  // 监听用户加载事件
  userManager.events.addUserLoaded(user => {
      console.log('OIDC Event: User loaded', user);
  });
  
  // 监听用户卸载（注销）事件
  userManager.events.addUserUnloaded(() => {
      console.log('OIDC Event: User unloaded');
  });
  
  // 监听访问令牌过期
  userManager.events.addAccessTokenExpiring(() => {
      console.log('OIDC Event: Access token expiring');
  });
  
  // 监听访问令牌过期已过期
  userManager.events.addAccessTokenExpired(() => {
      console.log('OIDC Event: Access token expired');
      showToast('登录已过期，请重新登录');
      userManager.signinRedirect();
  });
  
  // 监听静默续订错误
  userManager.events.addSilentRenewError(error => {
      console.error('OIDC Event: Silent renew error', error);
      showToast('自动续订会话失败，请重新登录');
      userManager.signinRedirect();
  });
  
  // 监听用户会话已终止
  userManager.events.addUserSessionChanged(() => {
      console.log('OIDC Event: User session changed');
  });
  
  // 监听用户已登录
  userManager.events.addUserSignedIn(() => {
      console.log('OIDC Event: User signed in');
  });
  
  // 监听用户已登出
  userManager.events.addUserSignedOut(() => {
      console.log('OIDC Event: User signed out');
  });

  export default userManager;