# Scripts 目录

本目录包含项目相关的脚本和配置文件。

## 目录结构

```
scripts/
├── README.md                    # 本文件
├── database/                    # 数据库相关脚本
│   ├── init/                   # 数据库初始化脚本
│   ├── migration/              # 数据库迁移脚本
│   └── seed/                   # 测试数据脚本
├── env/                # 环境配置文件
│   ├── .env.example           # 环境变量示例文件
│   ├── .env.dev               # 开发环境配置
│   └── .env.prod              # 生产环境配置
├── deploy/                 # 部署相关脚本
│   └── docker/                # Docker相关文件
│       ├── docker-compose.yml # Docker Compose配置文件
│       └── postgres/          # PostgreSQL初始化脚本
└── online-judge-shells/        # 在线评测相关脚本
```

## 使用说明

### 数据库初始化
```bash
# 执行数据库初始化脚本
psql -h localhost -U weautotools -d weautotools -f scripts/database/init/create_tables.sql
```

### 环境配置
```bash
# 复制环境配置文件
cp scripts/environment/.env.example .env
# 根据实际情况修改配置
```

### 容器化部署
```bash
# 使用Docker部署PostgreSQL和Redis
cd scripts/deployment/docker
docker-compose up -d
```