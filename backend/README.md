# WeAutoTools Backend

自动化工具平台后端服务

## 技术栈

- **Java**: OpenJDK 17 (LTS)
- **框架**: Spring Boot 3.5.4
- **数据库**: PostgreSQL
- **ORM**: MyBatis-plus 3.5.13
- **构建工具**: Maven

## 项目结构

```
src/main/java/com/flyerme/weautotools/
├── WeAutoToolsApplication.java     # 主启动类
├── common/                         # 通用类
│   ├── BaseEntity.java            # 基础实体类
│   ├── Result.java                # 统一响应结果
│   └── ResultCode.java            # 响应状态码枚举
├── config/                        # 配置类
│   └── MyBatisConfig.java         # MyBatis配置
├── controller/                    # 控制器层
│   └── HealthController.java      # 健康检查控制器
├── dto/                          # 数据传输对象
├── entity/                       # 实体类
├── exception/                    # 异常处理
│   ├── BusinessException.java    # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── mapper/                       # MyBatis Mapper接口
├── repository/                   # JPA Repository接口
├── service/                      # 业务逻辑层
│   └── impl/                     # 业务逻辑实现
└── util/                         # 工具类
```

## 配置说明

### 数据库配置

项目配置了PostgreSQL数据库，默认配置：
- 数据库名: `weautotools`
- 用户名: `weautotools` (可通过环境变量 `DB_USERNAME` 覆盖)
- 密码: `weautotools123` (可通过环境变量 `DB_PASSWORD` 覆盖)
- 端口: `5432`

### 环境配置

支持多环境配置：
- `dev`: 开发环境 (默认)
- `prod`: 生产环境

### 启动端口

- 开发环境: `8080`
- 上下文路径: `/api`

## 快速开始

### 1. 环境要求

- JDK 17+ (推荐使用LTS版本)
- PostgreSQL 12+
- Maven 3.6+

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE weautotools;

-- 创建用户
CREATE USER weautotools WITH PASSWORD 'weautotools123';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE weautotools TO weautotools;
```

### 3. 启动应用

```bash
# 使用Maven Wrapper
./mvnw spring-boot:run

# 或指定环境
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. 验证启动

访问健康检查接口：
- http://localhost:8080/api/health
- http://localhost:8080/api/health/hello

## 开发指南

### 代码规范

1. 所有实体类继承 `BaseEntity`，自动包含 id、创建时间、更新时间、逻辑删除等字段
2. 使用 `Result<T>` 作为统一响应格式
3. 业务异常使用 `BusinessException`
4. 使用 `@Valid` 进行参数校验
5. 遵循 RESTful API 设计规范

### 数据库操作

项目使用 MyBatis 作为唯一ORM框架：
- 简单 CRUD 操作使用 MyBatis 注解
- 复杂查询使用 MyBatis XML 映射文件
- 动态SQL使用 MyBatis 脚本标签

### 配置理念

项目遵循 **"约定优于配置"** 原则：
- 尽量使用 Spring Boot 默认配置
- 只配置必要的业务相关参数
- 减少复杂的自定义配置
- 依赖 Spring Boot 自动配置机制

### 点击计数功能

项目包含完整的点击计数功能示例：

#### API接口

1. **创建计数器**
   ```
   POST /api/click-counter
   Content-Type: application/json

   {
     "counterName": "test_counter",
     "description": "测试计数器",
     "enabled": true
   }
   ```

2. **点击计数**
   ```
   POST /api/click-counter/{id}/click
   POST /api/click-counter/name/{counterName}/click
   ```

3. **查询计数器**
   ```
   GET /api/click-counter                    # 获取所有计数器
   GET /api/click-counter/{id}               # 根据ID获取
   GET /api/click-counter/name/{counterName} # 根据名称获取
   GET /api/click-counter/enabled            # 获取启用的计数器
   GET /api/click-counter/statistics         # 获取统计信息
   ```

4. **管理计数器**
   ```
   PUT /api/click-counter/{id}     # 更新计数器
   DELETE /api/click-counter/{id}  # 删除计数器
   POST /api/click-counter/{id}/reset  # 重置计数器
   ```

5. **高级查询**
   ```
   GET /api/click-counter/page?page=1&size=10  # 分页查询
   GET /api/click-counter/search?enabled=true&counterName=test  # 条件查询
   GET /api/click-counter/top?limit=5  # 获取点击数最多的计数器
   ```

#### 数据库表结构

```sql
CREATE TABLE click_counter (
    id BIGSERIAL PRIMARY KEY,
    counter_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    click_count BIGINT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT true,
    last_click_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);
```

### 监控端点

Spring Boot Actuator 端点：
- `/api/actuator/health` - 健康检查
- `/api/actuator/info` - 应用信息
- `/api/actuator/metrics` - 指标信息

## 构建部署

```bash
# 编译
./mvnw clean compile

# 打包
./mvnw clean package

# 跳过测试打包
./mvnw clean package -DskipTests
```

生成的 JAR 文件位于 `target/` 目录下。
