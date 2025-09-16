-- 数据库迁移脚本 V1.0.1
-- 为计数器表添加分类字段

-- 添加分类字段
ALTER TABLE click_counter 
ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'general';

-- 添加分类索引
CREATE INDEX IF NOT EXISTS idx_click_counter_category ON click_counter(category);

-- 添加字段注释
COMMENT ON COLUMN click_counter.category IS '计数器分类';