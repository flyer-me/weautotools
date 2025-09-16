# Docker 容器化部署

本目录包含使用 Docker 部署 WeAutoMarket 应用的相关配置文件。

## 目录结构

```
docker/
├── docker-compose.yml     # Docker Compose 配置文件
├── postgres/              # PostgreSQL 相关文件
│   ├── init/             # 初始化脚本
│   └── Dockerfile        # PostgreSQL 镜像构建文件（如需要）
└── redis/                # Redis 相关文件
    └── redis.conf        # Redis 配置文件（如需要）
```

## 使用说明

### 启动所有服务

```bash
docker-compose up -d
```

### 停止所有服务

```bash
docker-compose down
```

### 查看服务状态

```bash
docker-compose ps
```