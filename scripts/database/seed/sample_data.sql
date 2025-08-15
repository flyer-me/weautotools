-- WeAutoTools 示例数据插入脚本
-- 执行前请确保表结构已创建

-- 插入示例计数器数据
INSERT INTO click_counter (counter_name, description, click_count, enabled, created_at, updated_at) VALUES
('homepage_visits', '首页访问计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('button_clicks', '按钮点击计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('api_calls', 'API调用计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('download_count', '下载次数计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user_registration', '用户注册计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('file_upload', '文件上传计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('search_queries', '搜索查询计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('error_count', '错误统计计数器', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (counter_name) DO NOTHING;

-- 验证插入的数据
SELECT 
    counter_name,
    description,
    click_count,
    enabled,
    created_at
FROM click_counter 
ORDER BY created_at;
