# 前端集成指南

##  完整的OAuth2流程

### 1. 用户注册

**前端调用**：
```javascript
// 注册用户
const registerUser = async (userData) => {
  try {
    const response = await fetch('/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        registerType: 'PHONE', // 或 'EMAIL'
        mobile: '13800138000', // 手机号
        email: null, // 邮箱（如果使用邮箱注册）
        password: 'password123',
        nickname: '用户昵称'
      })
    });
    
    const result = await response.json();
    console.log('注册结果:', result);
    // 返回: { "code": 200, "message": "注册成功，请使用OAuth2端点获取令牌", "data": null }
    
    return result;
  } catch (error) {
    console.error('注册失败:', error);
    throw error;
  }
};
```

**返回结果**：
```json
{
  "code": 200,
  "message": "注册成功，请使用OAuth2端点获取令牌",
  "data": null
}
```

### 2. 用户登录

**前端调用**：
```javascript
// 登录用户
const loginUser = async (username, password) => {
  try {
    const response = await fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username, // 手机号或邮箱
        password: password
      })
    });
    
    const result = await response.json();
    console.log('登录结果:', result);
    // 返回: { "code": 200, "message": "登录成功，请使用OAuth2端点获取令牌", "data": null }
    
    return result;
  } catch (error) {
    console.error('登录失败:', error);
    throw error;
  }
};
```

**返回结果**：
```json
{
  "code": 200,
  "message": "登录成功，请使用OAuth2端点获取令牌",
  "data": null
}
```

### 3. 获取OAuth2授权URL

**前端调用**：
```javascript
// 获取授权URL
const getAuthorizeUrl = async () => {
  try {
    const response = await fetch('/auth/oauth2/authorize-url');
    const result = await response.json();
    console.log('授权URL:', result.data);
    // 返回: "http://localhost:8080/oauth2/authorize?response_type=code&client_id=weautotools-client&..."
    
    return result.data;
  } catch (error) {
    console.error('获取授权URL失败:', error);
    throw error;
  }
};
```

### 4. OAuth2授权流程

**前端实现**：
```javascript
// 完整的OAuth2授权流程
const performOAuth2Login = async () => {
  try {
    // 1. 获取授权URL
    const authorizeUrl = await getAuthorizeUrl();
    
    // 2. 重定向到授权页面
    window.location.href = authorizeUrl;
    
    // 3. 用户授权后，会重定向到回调URL
    // 回调URL: http://localhost:8080/callback?code=AUTH_CODE&state=random_state
    
  } catch (error) {
    console.error('OAuth2授权失败:', error);
    throw error;
  }
};

// 处理回调
const handleCallback = async (code, state) => {
  try {
    // 4. 使用授权码获取令牌
    const tokenResponse = await fetch('/oauth2/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + btoa('weautotools-client:weautotools-secret')
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        code: code,
        redirect_uri: 'http://localhost:8080/callback'
      })
    });
    
    const tokenResult = await tokenResponse.json();
    console.log('令牌结果:', tokenResult);
    // 返回: { "access_token": "...", "token_type": "Bearer", "expires_in": 7200, "refresh_token": "..." }
    
    // 5. 保存令牌
    localStorage.setItem('access_token', tokenResult.access_token);
    localStorage.setItem('refresh_token', tokenResult.refresh_token);
    
    return tokenResult;
  } catch (error) {
    console.error('获取令牌失败:', error);
    throw error;
  }
};
```

### 5. 使用令牌访问API

**前端实现**：
```javascript
// 使用令牌访问受保护的API
const callProtectedAPI = async (url, options = {}) => {
  const accessToken = localStorage.getItem('access_token');
  
  if (!accessToken) {
    throw new Error('未找到访问令牌');
  }
  
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  if (response.status === 401) {
    // 令牌过期，尝试刷新
    await refreshToken();
    // 重新调用API
    return callProtectedAPI(url, options);
  }
  
  return response.json();
};

// 刷新令牌
const refreshToken = async () => {
  const refreshToken = localStorage.getItem('refresh_token');
  
  if (!refreshToken) {
    throw new Error('未找到刷新令牌');
  }
  
  const response = await fetch('/oauth2/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic ' + btoa('weautotools-client:weautotools-secret')
    },
    body: new URLSearchParams({
      grant_type: 'refresh_token',
      refresh_token: refreshToken
    })
  });
  
  const result = await response.json();
  
  // 更新令牌
  localStorage.setItem('access_token', result.access_token);
  if (result.refresh_token) {
    localStorage.setItem('refresh_token', result.refresh_token);
  }
  
  return result;
};
```

### 6. 用户登出

**前端调用**：
```javascript
// 登出用户
const logoutUser = async () => {
  try {
    const accessToken = localStorage.getItem('access_token');
    
    if (accessToken) {
      // 撤销令牌
      await fetch('/oauth2/revoke', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'Authorization': 'Basic ' + btoa('weautotools-client:weautotools-secret')
        },
        body: new URLSearchParams({
          token: accessToken,
          token_type_hint: 'access_token'
        })
      });
    }
    
    // 清除本地存储
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    
    console.log('登出成功');
  } catch (error) {
    console.error('登出失败:', error);
    throw error;
  }
};
```

##  完整的登录流程示例

```javascript
// 完整的登录流程
const completeLoginFlow = async (username, password) => {
  try {
    // 1. 用户登录验证
    await loginUser(username, password);
    
    // 2. 执行OAuth2授权
    await performOAuth2Login();
    
    // 3. 在回调页面处理授权码
    // 4. 获取访问令牌
    // 5. 保存令牌并跳转到主页面
    
  } catch (error) {
    console.error('登录流程失败:', error);
    throw error;
  }
};
```

##  总结

1. **注册**：`POST /auth/register` - 创建用户
2. **登录**：`POST /auth/login` - 验证用户
3. **授权**：`GET /auth/oauth2/authorize-url` - 获取授权URL
4. **令牌**：`POST /oauth2/token` - 获取访问令牌
5. **API调用**：使用Bearer令牌访问受保护的API
6. **刷新**：`POST /oauth2/token` - 刷新令牌
7. **登出**：`POST /oauth2/revoke` - 撤销令牌
