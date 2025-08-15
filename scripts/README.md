# Scripts 目录

本目录包含项目相关的脚本和配置文件。

## 目录结构

```
scripts/
├── README.md                 # 本文件
├── database/                 # 数据库相关脚本
│   ├── init/                # 数据库初始化脚本
│   ├── migration/           # 数据库迁移脚本
│   └── seed/                # 测试数据脚本
├── environment/             # 环境配置文件
│   ├── .env.example        # 环境变量示例文件
│   ├── .env.dev            # 开发环境配置
│   ├── .env.prod           # 生产环境配置
│   └── database.properties # 数据库配置
├── deployment/              # 部署相关脚本
│   ├── docker/             # Docker相关文件
│   └── kubernetes/         # K8s相关文件
└── tools/                   # 工具脚本
    ├── build.sh            # 构建脚本
    ├── start.sh            # 启动脚本
    └── backup.sh           # 备份脚本
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

### 部署
```bash
# 使用Docker部署
cd scripts/deployment/docker
docker-compose up -d
```
