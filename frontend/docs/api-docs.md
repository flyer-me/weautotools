# WeAutoTools 前端 API 文档

本文档根据前端代码库中的 API 调用自动生成，旨在为后端开发人员提供一个清晰、准确的接口实现指南。

所有 API 请求的 Base URL 由环境配置决定。所有需要授权的接口都应通过请求头传递 `Authorization: Bearer <access_token>` 进行认证。

---

## 1. 认证授权 (Authentication & Authorization)

前端使用 `oidc-client-ts` 库，通过标准的 OpenID Connect (OIDC) / OAuth 2.0 授权码流程进行认证。后端 Spring Authorization Server 需要为此提供标准的 OIDC 端点。

### OIDC 客户端配置

-   **Client ID:** `weautotools-frontend-client`
-   **Response Type:** `code`
-   **Scopes:** `openid profile api.read api.write`
-   **Redirect URI (登录回调):** `http://localhost:5173/pages/auth/callback`
-   **Post Logout Redirect URI (登出回调):** `http://localhost:5173/pages/auth/logout-success`

### 所需 OIDC/OAuth2 端点

后端需要暴露标准的 OIDC Provider 端点，通常包括：
-   `/.well-known/openid-configuration`
-   Authorization Endpoint
-   Token Endpoint
-   UserInfo Endpoint
-   End Session Endpoint (用于登出)

---

## 2. 用户 (User)

### 2.1 获取当前用户信息

-   **Endpoint:** `GET /user/info`
-   **描述:** 获取当前登录用户的详细信息，如用户名、头像、ID 等。
-   **认证:** 需要

### 2.2 更新用户信息

-   **Endpoint:** `PUT /user/info`
-   **描述:** 更新当前登录用户的信息。
-   **认证:** 需要
-   **请求体 (Request Body):**
    ```json
    {
      "nickname": "新的昵称",
      "avatarUrl": "http://example.com/new-avatar.png"
    }
    ```

### 2.3 获取用户统计数据

-   **Endpoint:** `GET /user/stats`
-   **描述:** 获取用户的统计数据，用于“我的”页面显示，例如订单数、消息数等。
-   **认证:** 需要

---

## 3. 订单 (Orders)

### 3.1 获取订单列表

-   **Endpoint:** `GET /orders`
-   **描述:** 获取用户的订单列表，支持按状态筛选和分页。
-   **认证:** 需要
-   **查询参数 (Query Parameters):**
    -   `status` (string, optional): 订单状态 (e.g., `PENDING`, `PAID`, `COMPLETED`)。
    -   `page` (number, optional): 页码。
    -   `pageSize` (number, optional): 每页数量。

### 3.2 创建订单

-   **Endpoint:** `POST /orders`
-   **描述:** 创建一个新的订单。
-   **认证:** 需要
-   **请求体 (Request Body):**
    ```json
    {
      "productId": "prod_123",
      "quantity": 2,
      "addressId": "addr_456"
    }
    ```

### 3.3 获取订单详情

-   **Endpoint:** `GET /orders/{orderId}`
-   **描述:** 根据订单 ID 获取单个订单的详细信息。
-   **认证:** 需要
-   **路径参数 (Path Parameters):**
    -   `orderId` (string, required): 订单的唯一标识符。

### 3.4 更新订单状态

-   **Endpoint:** `PUT /orders/{orderId}/status`
-   **描述:** 更新指定订单的状态。这是一个通用端点，具体的状态变更由其他专用端点（如取消、确认收货）调用。
-   **认证:** 需要
-   **请求体 (Request Body):**
    ```json
    {
      "status": "COMPLETED"
    }
    ```

### 3.5 取消订单

-   **Endpoint:** `PUT /orders/{orderId}/cancel`
-   **描述:** 取消一个未支付或未处理的订单。
-   **认证:** 需要

### 3.6 确认收货

-   **Endpoint:** `PUT /orders/{orderId}/confirm`
-   **描述:** 用户确认收到货物。
-   **认证:** 需要

### 3.7 申请退款

-   **Endpoint:** `PUT /orders/{orderId}/refund`
-   **描述:** 用户对已支付订单申请退款。
-   **认证:** 需要
-   **请求体 (Request Body):**
    ```json
    {
      "reason": "商品有瑕疵"
    }
    ```

### 3.8 删除订单

-   **Endpoint:** `DELETE /orders/{orderId}`
-   **描述:** 用户删除一个已完成或已取消的订单（逻辑删除）。
-   **认证:** 需要

### 3.9 支付订单

-   **Endpoint:** `POST /orders/{orderId}/pay`
-   **描述:** 为待支付订单获取支付参数或发起支付。
-   **认证:** 需要
-   **请求体 (Request Body):**
    ```json
    {
      "paymentMethod": "wechat_pay",
      "paymentChannel": "app"
    }
    ```

### 3.10 获取订单状态统计

-   **Endpoint:** `GET /orders/status-counts`
-   **描述:** 获取不同状态下的订单数量，用于在“我的”页面显示徽章（Badge）。
-   **认证:** 需要

### 3.11 获取订单总览统计

-   **Endpoint:** `GET /orders/stats`
-   **描述:** 获取订单相关的综合统计数据。
-   **认证:** 需要

---

## 4. 消息 (Messages)

### 4.1 获取消息列表

-   **Endpoint:** `GET /messages`
-   **描述:** 获取用户的消息列表，支持分页。
-   **认证:** 需要
-   **查询参数 (Query Parameters):**
    -   `page` (number, optional): 页码。
    -   `pageSize` (number, optional): 每页数量。

### 4.2 获取未读消息数

-   **Endpoint:** `GET /messages/unread-count`
-   **描述:** 获取总的未读消息数量，用于显示应用徽章。
-   **认证:** 需要

### 4.3 标记消息为已读

-   **Endpoint:** `PUT /messages/{messageId}/read`
-   **描述:** 将单条消息标记为已读。
-   **认证:** 需要
-   **路径参数 (Path Parameters):**
    -   `messageId` (string, required): 消息的唯一标识符。

### 4.4 获取聊天记录

-   **Endpoint:** `GET /chats/{chatId}/messages`
-   **描述:** 获取指定对话（聊天）的详细消息记录，支持分页。
-   **认证:** 需要
-   **路径参数 (Path Parameters):**
    -   `chatId` (string, required): 对话的唯一标识符。
-   **查询参数 (Query Parameters):**
    -   `cursor` (string, optional): 用于分页的游标。
    -   `limit` (number, optional): 每页数量。

---

## 5. 工具 (Tools)

### 5.1 获取工具列表

-   **Endpoint:** `GET /tools`
-   **描述:** 获取所有可用的工具列表，支持按分类筛选和分页。
-   **认证:** 可选
-   **查询参数 (Query Parameters):**
    -   `category` (string, optional): 工具分类。
    -   `page` (number, optional): 页码。

### 5.2 获取工具详情

-   **Endpoint:** `GET /tools/{toolId}`
-   **描述:** 获取单个工具的详细信息。
-   **认证:** 可选
-   **路径参数 (Path Parameters):**
    -   `toolId` (string, required): 工具的唯一标识符。

### 5.3 搜索工具

-   **Endpoint:** `GET /tools/search`
-   **描述:** 根据关键词搜索工具。
-   **认证:** 可选
-   **查询参数 (Query Parameters):**
    -   `keyword` (string, required): 搜索关键词。

### 5.4 获取工具分类

-   **Endpoint:** `GET /tools/categories`
-   **描述:** 获取所有工具的分类列表。
-   **认证:** 可选

### 5.5 获取工具使用记录

-   **Endpoint:** `GET /tools/records`
-   **描述:** 获取当前用户的工具使用历史记录。
-   **认证:** 需要
-   **查询参数 (Query Parameters):**
    -   `page` (number, optional): 页码。

---

## 6. 使用限制 (Usage Limits)

### 6.1 检查工具使用限制

-   **Endpoint:** `GET /api/usage-limits/check`
-   **描述:** 在用户使用某个工具前，检查其剩余使用次数。此端点应包含速率限制，并在每次调用时减少相应工具的可用次数。
-   **认证:** 可选 (通过 `Authorization` 头传递 Token 来识别用户，匿名用户则通过 IP 或设备 ID 识别)
-   **查询参数 (Query Parameters):**
    -   `toolName` (string, required): 工具的唯一名称/ID (e.g., `qr-generate-frontend`)。
-   **成功响应示例:**
    ```json
    {
      "code": 0,
      "message": "Success",
      "data": {
        "isExceeded": false,
        "remaining": 98,
        "userType": "AUTHENTICATED"
      }
    }
    ```

### 6.2 [Admin] 获取所有限制配置

-   **Endpoint:** `GET /api/usage-limits/configs`
-   **描述:** (管理端) 获取所有工具的使用限制配置。
-   **认证:** 需要 (管理员权限)

### 6.3 [Admin] 更新单个限制配置

-   **Endpoint:** `PUT /api/usage-limits/configs/{id}`
-   **描述:** (管理端) 更新指定 ID 的使用限制配置。
-   **认证:** 需要 (管理员权限)

### 6.4 [Admin] 批量更新限制配置

-   **Endpoint:** `PUT /api/usage-limits/configs/batch`
-   **描述:** (管理端) 批量更新多个使用限制配置。
-   **认证:** 需要 (管理员权限)
-   **请求体 (Request Body):**
    ```json
    [
      { "id": 1, "limit": 100, "userType": "ANONYMOUS" },
      { "id": 2, "limit": 1000, "userType": "AUTHENTICATED" }
    ]
    ```