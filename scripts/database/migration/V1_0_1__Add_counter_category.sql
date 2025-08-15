-- 数据库迁移脚本 V1.0.1
-- 为计数器表添加分类字段

-- 添加分类字段
ALTER TABLE click_counter 
ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'general';

-- 添加分类索引
CREATE INDEX IF NOT EXISTS idx_click_counter_category ON click_counter(category);

-- 添加字段注释
COMMENT ON COLUMN click_counter.category IS '计数器分类';

-- 更新现有数据的分类
UPDATE click_counter 
SET category = CASE 
    WHEN counter_name LIKE '%visit%' OR counter_name LIKE '%page%' THEN 'traffic'
    WHEN counter_name LIKE '%click%' OR counter_name LIKE '%button%' THEN 'interaction'
    WHEN counter_name LIKE '%api%' OR counter_name LIKE '%call%' THEN 'api'
    WHEN counter_name LIKE '%download%' OR counter_name LIKE '%upload%' THEN 'file'
    WHEN counter_name LIKE '%user%' OR counter_name LIKE '%registration%' THEN 'user'
    WHEN counter_name LIKE '%error%' THEN 'system'
    ELSE 'general'
END
WHERE category = 'general';
