# 环境配置目录

## 文件说明

- `.env.example` - 环境变量示例文件，包含所有可配置项
- `.env.dev` - 开发环境配置
- `.env.prod` - 生产环境配置
- `database.properties` - 数据库连接配置

## 使用方法

1. 复制对应环境的配置文件到项目根目录
2. 重命名为 `.env`
3. 根据实际情况修改配置值

```bash
# 开发环境
cp scripts/environment/.env.dev .env

# 生产环境
cp scripts/environment/.env.prod .env
```

## 安全注意事项

- 不要将包含敏感信息的 `.env` 文件提交到版本控制
- 生产环境密码请使用强密码
- 定期更换数据库密码
