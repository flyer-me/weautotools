-- 数据库表结构优化迁移脚本
-- WeAutoTools v1.0.3
-- 创建时间: 2025-09-16
-- 目的: 抽离工具信息到独立的tools表，优化数据库设计，减少数据冗余

-- =====================================================================================
-- 1. 创建工具管理表 (tools)
-- =====================================================================================

-- 工具管理表
CREATE TABLE IF NOT EXISTS tools (
    id BIGSERIAL PRIMARY KEY,
    tool_code VARCHAR(100) NOT NULL UNIQUE,     -- 工具代码（唯一标识）
    tool_name VARCHAR(100) NOT NULL,            -- 工具名称
    tool_type VARCHAR(50) NOT NULL,             -- 工具类型
    description TEXT,                            -- 工具描述
    category VARCHAR(50),                        -- 工具分类
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 工具状态 (ACTIVE, INACTIVE, DEPRECATED)
    is_frontend BOOLEAN NOT NULL DEFAULT false, -- 是否为前端工具
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,         -- 逻辑删除标记
    version INTEGER NOT NULL DEFAULT 0          -- 乐观锁版本号
);

-- tools 表索引
CREATE INDEX IF NOT EXISTS idx_tools_tool_code ON tools(tool_code);
CREATE INDEX IF NOT EXISTS idx_tools_tool_type ON tools(tool_type);
CREATE INDEX IF NOT EXISTS idx_tools_category ON tools(category);
CREATE INDEX IF NOT EXISTS idx_tools_status ON tools(status);
CREATE INDEX IF NOT EXISTS idx_tools_deleted ON tools(deleted);

-- 添加表注释
COMMENT ON TABLE tools IS '工具管理表';
COMMENT ON COLUMN tools.id IS '主键ID';
COMMENT ON COLUMN tools.tool_code IS '工具代码（唯一标识）';
COMMENT ON COLUMN tools.tool_name IS '工具名称';
COMMENT ON COLUMN tools.tool_type IS '工具类型';
COMMENT ON COLUMN tools.description IS '工具描述';
COMMENT ON COLUMN tools.category IS '工具分类';
COMMENT ON COLUMN tools.status IS '工具状态 (ACTIVE:活跃, INACTIVE:非活跃, DEPRECATED:已弃用)';
COMMENT ON COLUMN tools.is_frontend IS '是否为前端工具';

-- =====================================================================================
-- 2. 数据迁移：从现有表中提取工具信息
-- =====================================================================================

-- 从 tool_usage_limits 表中提取唯一的工具信息并插入到 tools 表
INSERT INTO tools (tool_code, tool_name, tool_type, description, category, status, is_frontend)
SELECT DISTINCT
    tool_name as tool_code,                    -- 使用 tool_name 作为工具代码
    tool_name,                                 -- 工具名称
    tool_type,                                 -- 工具类型
    CASE 
        WHEN tool_name LIKE '%qr-generate%' THEN '二维码生成工具'
        WHEN tool_name LIKE '%qr-decode%' THEN '二维码识别工具'
        WHEN tool_name LIKE '%image-compress%' THEN '图片压缩工具'
        WHEN tool_name LIKE '%image-format-convert%' THEN '图片格式转换工具'
        WHEN tool_name LIKE '%image-process%' THEN '图片处理工具'
        WHEN tool_name LIKE '%data-convert%' THEN '数据转换工具'
        WHEN tool_name = 'default' THEN '默认工具'
        ELSE '自动化工具'
    END as description,                        -- 根据工具名称生成描述
    CASE 
        WHEN tool_type = 'QR_CODE' THEN 'qrcode'
        WHEN tool_type = 'IMAGE_PROCESS' THEN 'image'
        WHEN tool_type = 'DATA_CONVERT' THEN 'data'
        ELSE 'base'
    END as category,                           -- 根据工具类型映射分类
    'ACTIVE' as status,                        -- 默认状态为活跃
    CASE 
        WHEN tool_name LIKE '%-frontend' THEN true
        ELSE false
    END as is_frontend                         -- 根据工具名称判断是否为前端工具
FROM tool_usage_limits 
WHERE deleted = 0
ON CONFLICT (tool_code) DO NOTHING;           -- 避免重复插入

-- =====================================================================================
-- 3. 修改现有表结构
-- =====================================================================================

-- 3.1 为 tool_usage_limits 表添加 tool_id 字段
ALTER TABLE tool_usage_limits ADD COLUMN IF NOT EXISTS tool_id BIGINT;

-- 3.2 为 tool_usage_records 表添加 tool_id 字段
ALTER TABLE tool_usage_records ADD COLUMN IF NOT EXISTS tool_id BIGINT;

-- =====================================================================================
-- 4. 数据迁移：更新 tool_id 字段
-- =====================================================================================

-- 4.1 更新 tool_usage_limits 表的 tool_id 字段
UPDATE tool_usage_limits 
SET tool_id = t.id
FROM tools t
WHERE tool_usage_limits.tool_name = t.tool_code
AND tool_usage_limits.deleted = 0;

-- 4.2 更新 tool_usage_records 表的 tool_id 字段
UPDATE tool_usage_records 
SET tool_id = t.id
FROM tools t
WHERE tool_usage_records.tool_name = t.tool_code
AND tool_usage_records.deleted = 0;

-- =====================================================================================
-- 5. 添加约束和索引（优化后）
-- =====================================================================================

-- 5.1 为新字段添加非空约束（在数据迁移完成后）
-- 注意：这里先不设置 NOT NULL，等数据完全迁移后再手动设置

-- 5.2 添加新的索引
-- tool_usage_limits 表新索引
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_tool_id ON tool_usage_limits(tool_id);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_composite_new ON tool_usage_limits(tool_id, user_type, enabled, deleted);

-- tool_usage_records 表新索引
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_tool_id ON tool_usage_records(tool_id);
CREATE INDEX IF NOT EXISTS idx_tool_usage_records_composite_new ON tool_usage_records(user_identifier, tool_id, usage_time);

-- =====================================================================================
-- 6. 添加新字段的注释
-- =====================================================================================

COMMENT ON COLUMN tool_usage_limits.tool_id IS '关联的工具ID（外键关联tools表）';
COMMENT ON COLUMN tool_usage_records.tool_id IS '关联的工具ID（外键关联tools表）';

-- =====================================================================================
-- 7. 验证数据完整性
-- =====================================================================================

-- 验证工具表数据
SELECT COUNT(*) as tools_count FROM tools WHERE deleted = 0;

-- 验证 tool_usage_limits 表的 tool_id 字段是否正确填充
SELECT 
    COUNT(*) as total_records,
    COUNT(tool_id) as filled_tool_id,
    COUNT(*) - COUNT(tool_id) as missing_tool_id
FROM tool_usage_limits 
WHERE deleted = 0;

-- 验证 tool_usage_records 表的 tool_id 字段是否正确填充
SELECT 
    COUNT(*) as total_records,
    COUNT(tool_id) as filled_tool_id,
    COUNT(*) - COUNT(tool_id) as missing_tool_id
FROM tool_usage_records 
WHERE deleted = 0;

-- 检查是否有遗漏的工具（在限制表中但不在工具表中）
SELECT DISTINCT tool_name, tool_type
FROM tool_usage_limits tul
WHERE tul.deleted = 0
AND NOT EXISTS (
    SELECT 1 FROM tools t 
    WHERE t.tool_code = tul.tool_name AND t.deleted = 0
);

-- =====================================================================================
-- 注意事项：
-- 1. 此脚本执行后，应用程序需要更新以使用新的 tool_id 字段
-- 2. 建议在低峰期执行，因为涉及数据迁移
-- 3. 执行前请备份数据库
-- 4. 旧的 tool_type 和 tool_name 字段暂时保留，等应用程序完全迁移后再删除
-- =====================================================================================