# WeAutoTools - 自动化工具平台

##  项目简介
WeAutoTools 是一个自动化工具平台。
用户可在平台浏览、使用各种自动化工具（如Excel处理、批量操作、网页自动化等），提升工作效率。
该项目使用 **uniapp前端 + Spring 后端**构建。

---

##  核心功能
- **用户管理**：微信登录，用户权限管理
- **工具展示**：分类浏览、搜索查找各类自动化工具
- **工具使用**：
  - 在线工具使用和演示
  - 工具说明文档和使用指南
  - 用户反馈和评价
- **CI/CD**：
  - GitHub Actions 自动构建 + 部署
  - Docker 镜像构建
  - 测试 + 代码质量检查
- **后台管理**：
  - 数据统计

---

##  架构设计

### 技术栈
| 模块 | 方案 |
|------|------|
| 前端 | uniapp |
| 后端 | Spring Boot 3 + MyBatis |
| 数据库 | PostgreSQL |
| 缓存 | Redis |
| 队列 | RabbitMQ |
| 容器化 | Docker + Kubernetes |
| CI/CD | GitHub Actions |
| 监控 | Prometheus + Grafana |
| RPA | Selenium + DrissionPage / Python3 / VBA / C# |

---

## API 文档
API 使用 RESTful 设计，并通过 Swagger 3.0 生成文档。  

## 快速启动
```bash
# 1. 克隆仓库
git clone https://github.com/flyer-me/weautotools.git
cd weautotools

# 2. 启动后端
cd backend
mvn clean install
docker-compose up -d db redis mq
java -jar target/backend.jar

# 3. 启动小程序
cd frontend
npm install
npm run dev:mp-weixin
```