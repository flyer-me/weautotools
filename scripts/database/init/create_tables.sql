-- WeAutoTools 数据库表结构初始化脚本
-- 执行前请确保已连接到 weautotools 数据库

-- 创建点击计数器表
CREATE TABLE IF NOT EXISTS click_counter (
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

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_click_counter_name ON click_counter(counter_name);
CREATE INDEX IF NOT EXISTS idx_click_counter_enabled ON click_counter(enabled);
CREATE INDEX IF NOT EXISTS idx_click_counter_deleted ON click_counter(deleted);
CREATE INDEX IF NOT EXISTS idx_click_counter_created_at ON click_counter(created_at);
CREATE INDEX IF NOT EXISTS idx_click_counter_enabled_deleted ON click_counter(enabled, deleted);

-- 添加表注释
COMMENT ON TABLE click_counter IS '点击计数器表';
COMMENT ON COLUMN click_counter.id IS '主键ID';
COMMENT ON COLUMN click_counter.counter_name IS '计数器名称';
COMMENT ON COLUMN click_counter.description IS '计数器描述';
COMMENT ON COLUMN click_counter.click_count IS '点击次数';
COMMENT ON COLUMN click_counter.enabled IS '是否启用';
COMMENT ON COLUMN click_counter.last_click_time IS '最后点击时间';
COMMENT ON COLUMN click_counter.created_at IS '创建时间';
COMMENT ON COLUMN click_counter.updated_at IS '更新时间';
COMMENT ON COLUMN click_counter.deleted IS '逻辑删除标记(0:未删除,1:已删除)';
COMMENT ON COLUMN click_counter.version IS '版本号(乐观锁)';

-- 验证表结构
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'click_counter' 
ORDER BY ordinal_position;
