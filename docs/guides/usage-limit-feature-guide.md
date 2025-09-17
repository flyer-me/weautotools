# WeAutoTools 使用限制功能开发指南

## 概述

WeAutoTools 实现了统一的使用限制功能，支持后端API工具和前端纯客户端工具的使用次数控制。通过差异化的限制策略，引导匿名用户注册登录。

**版本**: v1.0.2  
**更新时间**: 2025-09-12

## 功能特性

### 限制类型
- **每日限制**: 每天可使用的总次数
- **每小时限制**: 每小时可使用的次数
- **用户类型**: 匿名用户 vs 登录用户

## 架构设计

### 技术栈
- **后端**: Spring Boot 3 + MyBatis + Redis
- **前端**: Vue.js 3 + uni-app
- **数据库**: PostgreSQL
- **缓存**: Redis

### 系统架构
```
[前端工具] --预检查--> [UsageLimitController] --查询--> [UsageLimitService]
     |                                                           |
     |                                                           v
     |                                                    [Redis计数器]
     |                                                           |
     |                                                           v
     v                                                    [PostgreSQL]
[执行工具逻辑]
     |
     v
[报告使用] --记录--> [UsageLimitController] --更新--> [Redis + DB]
```

## 实现方案

### 1. 后端实现

#### 数据库设计
```sql
-- 使用限制配置表
CREATE TABLE tool_usage_limits (
    id BIGSERIAL PRIMARY KEY,
    tool_type VARCHAR(50) NOT NULL,     -- QR_CODE, IMAGE_PROCESS, DATA_CONVERT
    tool_name VARCHAR(100) NOT NULL,    -- 具体工具名称
    user_type VARCHAR(20) NOT NULL,     -- ANONYMOUS, LOGIN
    limit_type VARCHAR(20) NOT NULL,    -- DAILY, HOURLY
    limit_count INTEGER NOT NULL,       -- 限制次数
    enabled BOOLEAN DEFAULT true
);

-- 使用记录表
CREATE TABLE tool_usage_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,                     -- 登录用户ID (可选)
    user_identifier VARCHAR(200),       -- 用户标识 (IP哈希/设备指纹)
    tool_name VARCHAR(100) NOT NULL,    -- 工具名称
    usage_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 配置数据示例
```sql
-- 前端工具限制配置 (使用-frontend后缀区分)
INSERT INTO tool_usage_limits (tool_type, tool_name, user_type, limit_type, limit_count) VALUES
('QR_CODE', 'qr-generate-frontend', 'ANONYMOUS', 'DAILY', 10),
('QR_CODE', 'qr-generate-frontend', 'LOGIN', 'DAILY', 50),
('IMAGE_PROCESS', 'image-process-frontend', 'ANONYMOUS', 'DAILY', 5),
('DATA_CONVERT', 'data-convert-frontend', 'ANONYMOUS', 'DAILY', 8);

-- 后端API工具限制配置
INSERT INTO tool_usage_limits (tool_type, tool_name, user_type, limit_type, limit_count) VALUES
('QR_CODE', 'qr-generate', 'ANONYMOUS', 'DAILY', 10),
('QR_CODE', 'qr-generate', 'LOGIN', 'DAILY', 50);
```

#### 核心服务实现

**UsageLimitService.java**
```java
@Service
public class UsageLimitServiceImpl implements UsageLimitService {
    
    // 检查是否超出限制
    public boolean isExceededLimit(String userIdentifier, String toolName, UserType userType);
    
    // 记录使用
    public void recordUsage(String userIdentifier, String toolName, UserType userType, 
                          Long userId, String ipAddress, String userAgent);
    
    // 获取剩余次数
    public int getRemainingUsage(String userIdentifier, String toolName, UserType userType);
    
    // 前端工具批量记录 (新增)
    public void recordFrontendToolUsage(String userIdentifier, String toolName, 
                                       UserType userType, int usageCount);
}
```

**UsageLimitController.java**
```java
@RestController
@RequestMapping("/api/usage-limits")
public class UsageLimitController {
    
    // 统一的限制检查接口 (支持前端和后端工具)
    @GetMapping("/check")
    public Result<UsageLimitCheckResponse> checkUsageLimit(@RequestParam String toolName) {
        // 自动识别用户类型 (JWT Token 或 IP地址)
        // 检查剩余次数
        // 记录一次使用 (仅对API调用)
        // 返回检查结果
    }
}
```

### 2. 前端实现

#### 核心Composable

**useUsageLimit.js**
```javascript
export function useUsageLimit() {
    // 前端工具使用检查和记录
    const useFrontendTool = async (toolName, batchSize = 1) => {
        // 1. 预检查剩余次数
        const frontendToolName = toolName.endsWith('-frontend') ? toolName : toolName + '-frontend'
        const response = await get('/api/usage-limits/check', { toolName: frontendToolName })
        
        if (data.isExceeded || remaining < batchSize) {
            showFrontendToolLimit(frontendToolName, `剩余次数不足`)
            return { canUse: false, reportUsage: () => {} }
        }
        
        // 2. 返回可用状态和回调函数
        return {
            canUse: true,
            remaining: remaining,
            reportUsage: async (actualCount = batchSize) => {
                // 通过多次调用检查API来记录使用
                for (let i = 0; i < actualCount; i++) {
                    await get('/api/usage-limits/check', { toolName: frontendToolName })
                }
            }
        }
    }
    
    return { useFrontendTool, /* 其他现有方法... */ }
}
```

#### 工具集成示例

**前端工具使用模式**
```javascript
// 在各个前端工具中的标准集成方式
const handleProcess = async () => {
    // 1. 预检查使用限制
    const usageResult = await useFrontendTool('tool-name', batchSize)
    if (!usageResult.canUse) {
        return // 已显示限制提示
    }
    
    try {
        // 2. 执行前端逻辑
        await executeToolLogic()
        
        // 3. 成功后报告使用
        await usageResult.reportUsage(successfulCount)
    } catch (error) {
        // 失败时不记录使用次数
        console.error('操作失败:', error)
    }
}
```

### 3. 用户体验设计

#### 限制提示
```javascript
const showFrontendToolLimit = (toolName, message) => {
    const toolDisplayNames = {
        'qr-generate-frontend': '二维码生成',
        'image-process-frontend': '图片处理',
        'data-convert-frontend': '数据转换'
    }
    
    uni.showModal({
        title: `${toolDisplayNames[toolName] || '工具'}使用限制`,
        content: message,
        showCancel: true,
        cancelText: '知道了',
        confirmText: '登录获取更多',
        success: (res) => {
            if (res.confirm) {
                uni.navigateTo({ url: '/pages/auth/login' })
            }
        }
    })
}
```

## 工具分类与限制策略

### 后端API工具
**特点**: 需要调用后端API进行业务处理
**限制方式**: AOP切面拦截 + @UsageLimit注解
**工具示例**: 
- `click-counter` - 点击计数器

**实现方式**:
```java
@PostMapping("/action")
@UsageLimit(toolName = "tool-name", message = "使用次数已达限制")
public Result<String> performAction() {
    // 业务逻辑
}
```

### 前端纯客户端工具
**特点**: 完全在前端执行，无需后端业务逻辑
**限制方式**: 预检查 + 后验证
**工具示例**:
- `qr-generate-frontend` - 二维码生成
- `qr-decode-frontend` - 二维码识别  
- `image-process-frontend` - 图片处理
- `data-convert-frontend` - 数据转换

**实现方式**: 前端集成 `useFrontendTool()` 方法

## 部署和配置

### 1. 数据库迁移
```bash
# 执行迁移脚本

```

### 2. Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

### 3. 限制策略配置
通过开发者设置页面 `/pages/dev-settings/dev-settings.vue` 可以动态调整限制配置。

## 监控和统计

### 使用统计查询
```sql
-- 前端工具使用统计
SELECT tool_name, COUNT(*) as usage_count
FROM tool_usage_records 
WHERE tool_name LIKE '%-frontend'
  AND usage_time >= CURRENT_DATE
GROUP BY tool_name;

-- 用户转化分析
SELECT 
    SUM(CASE WHEN user_id IS NULL THEN 1 ELSE 0 END) as anonymous_usage,
    SUM(CASE WHEN user_id IS NOT NULL THEN 1 ELSE 0 END) as login_usage
FROM tool_usage_records 
WHERE usage_time >= CURRENT_DATE;
```

### Redis键格式
```
limit:{userIdentifier}:{toolName}:{timeType}
示例: limit:anonymous:192.168.1.1:qr-generate-frontend:daily
```

## 扩展新工具

### 后端API工具
1. 在Controller方法上添加 `@UsageLimit` 注解
2. 在数据库中添加限制配置
3. 无需修改其他代码

### 前端工具
1. 在Vue组件中导入 `useUsageLimit`
2. 在处理方法中调用 `useFrontendTool()`
3. 在数据库中添加 `-frontend` 后缀的限制配置
4. 在 `showFrontendToolLimit()` 中添加工具显示名称

### 配置模板
```sql
-- 新增工具限制配置模板
INSERT INTO tool_usage_limits (tool_type, tool_name, user_type, limit_type, limit_count) VALUES
('TOOL_TYPE', 'new-tool-frontend', 'ANONYMOUS', 'DAILY', 5),
('TOOL_TYPE', 'new-tool-frontend', 'LOGIN', 'DAILY', 25);
```

## 最佳实践

### 开发建议
1. **复用现有组件**: 优先使用已有的Controller和Service
2. **统一命名规范**: 前端工具使用 `-frontend` 后缀
3. **精确计费**: 只记录实际成功的操作次数
4. **优雅降级**: 网络异常时允许有限使用
5. **用户引导**: 提供清晰的限制说明和登录入口

### 性能优化
1. **预检查机制**: 避免无效的操作执行
2. **批量处理**: 减少API调用次数
3. **Redis缓存**: 快速的计数器操作
4. **异步记录**: 不阻塞用户操作

### 安全考虑
1. **IP地址哈希**: 保护用户隐私
2. **JWT验证**: 安全的用户身份识别
3. **限制绕过防护**: 前端和后端双重验证
4. **异常处理**: 防止因限制检查导致功能中断

---

本文档描述了WeAutoTools平台使用限制功能的完整实现方案。该功能已通过全面测试，可用于生产环境部署。