# API 文档

本文档根据 `frontend/src/api/index.js` 文件生成，包含了前端应用中定义的所有 API 接口。

### 基础路径

所有 API 的基础路径都由底层 HTTP-Client (`@/utils/request`) 配置。

---

### 用户 (User)

#### 1. 获取用户信息

-   **函数名:** `getUserInfo()`
-   **方法:** `GET`
-   **路径:** `/user/info`
-   **参数:** 无

#### 2. 更新用户信息

-   **函数名:** `updateUserInfo(data)`
-   **方法:** `PUT`
-   **路径:** `/user/info`
-   **参数:**
    -   `data` (Object): 需要更新的用户信息。

#### 3. 获取用户统计数据

-   **函数名:** `getUserStats()`
-   **方法:** `GET`
-   **路径:** `/user/stats`
-   **参数:** 无

---

### 消息 (Message)

#### 1. 获取消息列表

-   **函数名:** `getMessageList(params)`
-   **方法:** `GET`
-   **路径:** `/messages`
-   **参数:**
    -   `params` (Object): 查询参数，例如分页、筛选等。

#### 2. 获取未读消息数量

-   **函数名:** `getUnreadCount()`
-   **方法:** `GET`
-   **路径:** `/messages/unread-count`
-   **参数:** 无

#### 3. 标记消息为已读

-   **函数名:** `markAsRead(messageId)`
-   **方法:** `PUT`
-   **路径:** `/messages/{messageId}/read`
-   **参数:**
    -   `messageId` (String|Number): 消息 ID。

#### 4. 获取聊天记录

-   **函数名:** `getChatHistory(chatId, params)`
-   **方法:** `GET`
-   **路径:** `/chats/{chatId}/messages`
-   **参数:**
    -   `chatId` (String|Number): 聊天会话 ID。
    -   `params` (Object): 查询参数，例如分页。

---

### 订单 (Order)

#### 1. 获取订单列表

-   **函数名:** `getOrderList(params)`
-   **方法:** `GET`
-   **路径:** `/orders`
-   **参数:**
    -   `params` (Object): 查询参数，例如分页、状态筛选。

#### 2. 获取订单详情

-   **函数名:** `getOrderDetail(orderId)`
-   **方法:** `GET`
-   **路径:** `/orders/{orderId}`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。

#### 3. 获取订单统计

-   **函数名:** `getOrderStats()`
-   **方法:** `GET`
-   **路径:** `/orders/stats`
-   **参数:** 无

#### 4. 获取订单状态统计

-   **函数名:** `getOrderStatusCounts()`
-   **方法:** `GET`
-   **路径:** `/orders/status-counts`
-   **描述:** 用于用户页面徽章显示。
-   **参数:** 无

#### 5. 创建订单

-   **函数名:** `createOrder(data)`
-   **方法:** `POST`
-   **路径:** `/orders`
-   **参数:**
    -   `data` (Object): 创建订单所需的数据。

#### 6. 更新订单状态

-   **函数名:** `updateOrderStatus(orderId, status)`
-   **方法:** `PUT`
-   **路径:** `/orders/{orderId}/status`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。
    -   `status` (String): 新的订单状态。

#### 7. 取消订单

-   **函数名:** `cancelOrder(orderId)`
-   **方法:** `PUT`
-   **路径:** `/orders/{orderId}/cancel`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。

#### 8. 确认收货

-   **函数名:** `confirmOrder(orderId)`
-   **方法:** `PUT`
-   **路径:** `/orders/{orderId}/confirm`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。

#### 9. 申请退款

-   **函数名:** `refundOrder(orderId, reason)`
-   **方法:** `PUT`
-   **路径:** `/orders/{orderId}/refund`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。
    -   `reason` (String): 退款原因。

#### 10. 删除订单

-   **函数名:** `deleteOrder(orderId)`
-   **方法:** `DELETE`
-   **路径:** `/orders/{orderId}`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。

#### 11. 支付订单

-   **函数名:** `payOrder(orderId, paymentData)`
-   **方法:** `POST`
-   **路径:** `/orders/{orderId}/pay`
-   **参数:**
    -   `orderId` (String|Number): 订单 ID。
    -   `paymentData` (Object): 支付所需的数据。

---

### 工具 (Tools)

#### 1. 获取工具列表

-   **函数名:** `getToolsList(params)`
-   **方法:** `GET`
-   **路径:** `/tools`
-   **参数:**
    -   `params` (Object): 查询参数，例如分页、分类。

#### 2. 获取工具详情

-   **函数名:** `getToolDetail(toolId)`
-   **方法:** `GET`
-   **路径:** `/tools/{toolId}`
-   **参数:**
    -   `toolId` (String|Number): 工具 ID。

#### 3. 搜索工具

-   **函数名:** `searchTools(keyword, params)`
-   **方法:** `GET`
-   **路径:** `/tools/search`
-   **参数:**
    -   `keyword` (String): 搜索关键词。
    -   `params` (Object): 其他查询参数。

#### 4. 获取工具分类

-   **函数名:** `getToolCategories()`
-   **方法:** `GET`
-   **路径:** `/tools/categories`
-   **参数:** 无

#### 5. 获取工具使用记录

-   **函数名:** `getToolRecords(params)`
-   **方法:** `GET`
-   **路径:** `/tools/records`
-   **参数:**
    -   `params` (Object): 查询参数。
