-- 使用限制功能相关数据表创建脚本
-- WeAutoTools v1.0.2
-- 创建时间: 2025-09-12

-- 工具使用限制配置表
CREATE TABLE IF NOT EXISTS tool_usage_limits (
    id BIGSERIAL PRIMARY KEY,
    tool_type VARCHAR(50) NOT NULL,          -- 工具类型 (QR_CODE, IMAGE_PROCESS, DATA_CONVERT等)
    tool_name VARCHAR(100) NOT NULL,         -- 工具名称
    user_type VARCHAR(20) NOT NULL,          -- 用户类型 (ANONYMOUS, LOGIN)
    limit_type VARCHAR(20) NOT NULL,         -- 限制类型 (DAILY, HOURLY, TOTAL)
    limit_count INTEGER NOT NULL DEFAULT 0,  -- 限制次数
    enabled BOOLEAN NOT NULL DEFAULT true,   -- 是否启用
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,      -- 逻辑删除标记
    version INTEGER NOT NULL DEFAULT 0       -- 乐观锁版本号
);

-- 工具使用记录表
CREATE TABLE IF NOT EXISTS tool_usage_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,                          -- 用户ID (登录用户，匿名用户为NULL)
    user_identifier VARCHAR(200) NOT NULL,   -- 用户标识 (IP哈希或设备指纹)
    tool_type VARCHAR(50) NOT NULL,          -- 工具类型
    tool_name VARCHAR(100) NOT NULL,         -- 工具名称
    usage_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 使用时间
    ip_address INET,                         -- IP地址
    user_agent TEXT,                         -- 用户代理
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,      -- 逻辑删除标记
    version INTEGER NOT NULL DEFAULT 0       -- 乐观锁版本号
);

-- 创建索引
-- tool_usage_limits 表索引
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_tool_name ON tool_usage_limits(tool_name);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_tool_type ON tool_usage_limits(tool_type);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_user_type ON tool_usage_limits(user_type);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_enabled ON tool_usage_limits(enabled);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_deleted ON tool_usage_limits(deleted);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_composite ON tool_usage_limits(tool_name, user_type, enabled, deleted);

-- tool_usage_records 表索引
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_user_id ON tool_usage_records(user_id);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_user_identifier ON tool_usage_records(user_identifier);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_tool_name ON tool_usage_records(tool_name);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_usage_time ON tool_usage_records(usage_time);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_ip_address ON tool_usage_records(ip_address);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_deleted ON tool_usage_records(deleted);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_composite ON tool_usage_records(user_identifier, tool_name, usage_time);

-- 添加表注释
COMMENT ON TABLE tool_usage_limits IS '工具使用限制配置表';
COMMENT ON COLUMN tool_usage_limits.id IS '主键ID';
COMMENT ON COLUMN tool_usage_limits.tool_type IS '工具类型';
COMMENT ON COLUMN tool_usage_limits.tool_name IS '工具名称';
COMMENT ON COLUMN tool_usage_limits.user_type IS '用户类型 (ANONYMOUS:匿名用户, LOGIN:登录用户)';
COMMENT ON COLUMN tool_usage_limits.limit_type IS '限制类型 (DAILY:每日, HOURLY:每小时, TOTAL:总计)';
COMMENT ON COLUMN tool_usage_limits.limit_count IS '限制次数';
COMMENT ON COLUMN tool_usage_limits.enabled IS '是否启用';

COMMENT ON TABLE tool_usage_records IS '工具使用记录表';
COMMENT ON COLUMN tool_usage_records.id IS '主键ID';
COMMENT ON COLUMN tool_usage_records.user_id IS '用户ID (登录用户)';
COMMENT ON COLUMN tool_usage_records.user_identifier IS '用户标识 (IP哈希或设备指纹)';
COMMENT ON COLUMN tool_usage_records.tool_type IS '工具类型';
COMMENT ON COLUMN tool_usage_records.tool_name IS '工具名称';
COMMENT ON COLUMN tool_usage_records.usage_time IS '使用时间';
COMMENT ON COLUMN tool_usage_records.ip_address IS 'IP地址';
COMMENT ON COLUMN tool_usage_records.user_agent IS '用户代理';

-- 插入默认限制配置数据
INSERT INTO tool_usage_limits (tool_type, tool_name, user_type, limit_type, limit_count) VALUES
-- 二维码工具限制
('QR_CODE', 'qr-generate', 'ANONYMOUS', 'DAILY', 10),
('QR_CODE', 'qr-generate', 'ANONYMOUS', 'HOURLY', 3),
('QR_CODE', 'qr-generate', 'LOGIN', 'DAILY', 50),
('QR_CODE', 'qr-generate', 'LOGIN', 'HOURLY', 10),
('QR_CODE', 'qr-decode', 'ANONYMOUS', 'DAILY', 10),
('QR_CODE', 'qr-decode', 'ANONYMOUS', 'HOURLY', 3),
('QR_CODE', 'qr-decode', 'LOGIN', 'DAILY', 50),
('QR_CODE', 'qr-decode', 'LOGIN', 'HOURLY', 10),

-- 图片处理工具限制
('IMAGE_PROCESS', 'image-compress', 'ANONYMOUS', 'DAILY', 5),
('IMAGE_PROCESS', 'image-compress', 'ANONYMOUS', 'HOURLY', 2),
('IMAGE_PROCESS', 'image-compress', 'LOGIN', 'DAILY', 30),
('IMAGE_PROCESS', 'image-compress', 'LOGIN', 'HOURLY', 8),
('IMAGE_PROCESS', 'image-format-convert', 'ANONYMOUS', 'DAILY', 5),
('IMAGE_PROCESS', 'image-format-convert', 'ANONYMOUS', 'HOURLY', 2),
('IMAGE_PROCESS', 'image-format-convert', 'LOGIN', 'DAILY', 30),
('IMAGE_PROCESS', 'image-format-convert', 'LOGIN', 'HOURLY', 8),

-- 数据转换工具限制
('DATA_CONVERT', 'data-convert', 'ANONYMOUS', 'DAILY', 8),
('DATA_CONVERT', 'data-convert', 'ANONYMOUS', 'HOURLY', 3),
('DATA_CONVERT', 'data-convert', 'LOGIN', 'DAILY', 40),
('DATA_CONVERT', 'data-convert', 'LOGIN', 'HOURLY', 10),

-- 前端工具限制 (使用-frontend后缀区分)
-- 前端二维码生成工具
('QR_CODE', 'qr-generate-frontend', 'ANONYMOUS', 'DAILY', 10),
('QR_CODE', 'qr-generate-frontend', 'ANONYMOUS', 'HOURLY', 3),
('QR_CODE', 'qr-generate-frontend', 'LOGIN', 'DAILY', 50),
('QR_CODE', 'qr-generate-frontend', 'LOGIN', 'HOURLY', 10),

-- 前端二维码识别工具
('QR_CODE', 'qr-decode-frontend', 'ANONYMOUS', 'DAILY', 10),
('QR_CODE', 'qr-decode-frontend', 'ANONYMOUS', 'HOURLY', 3),
('QR_CODE', 'qr-decode-frontend', 'LOGIN', 'DAILY', 50),
('QR_CODE', 'qr-decode-frontend', 'LOGIN', 'HOURLY', 10),

-- 前端图片处理工具
('IMAGE_PROCESS', 'image-process-frontend', 'ANONYMOUS', 'DAILY', 5),
('IMAGE_PROCESS', 'image-process-frontend', 'ANONYMOUS', 'HOURLY', 2),
('IMAGE_PROCESS', 'image-process-frontend', 'LOGIN', 'DAILY', 30),
('IMAGE_PROCESS', 'image-process-frontend', 'LOGIN', 'HOURLY', 8),

-- 前端图片压缩工具
('IMAGE_PROCESS', 'image-compress-frontend', 'ANONYMOUS', 'DAILY', 5),
('IMAGE_PROCESS', 'image-compress-frontend', 'ANONYMOUS', 'HOURLY', 2),
('IMAGE_PROCESS', 'image-compress-frontend', 'LOGIN', 'DAILY', 30),
('IMAGE_PROCESS', 'image-compress-frontend', 'LOGIN', 'HOURLY', 8),

-- 前端数据转换工具
('DATA_CONVERT', 'data-convert-frontend', 'ANONYMOUS', 'DAILY', 8),
('DATA_CONVERT', 'data-convert-frontend', 'ANONYMOUS', 'HOURLY', 3),
('DATA_CONVERT', 'data-convert-frontend', 'LOGIN', 'DAILY', 40),
('DATA_CONVERT', 'data-convert-frontend', 'LOGIN', 'HOURLY', 10),

-- 其他工具默认限制
('DEFAULT', 'default', 'ANONYMOUS', 'DAILY', 5),
('DEFAULT', 'default', 'ANONYMOUS', 'HOURLY', 2),
('DEFAULT', 'default', 'LOGIN', 'DAILY', 25),
('DEFAULT', 'default', 'LOGIN', 'HOURLY', 6);

-- 验证表结构
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name IN ('tool_usage_limits', 'tool_usage_records')
ORDER BY table_name, ordinal_position;