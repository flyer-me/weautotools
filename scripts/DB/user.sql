-- 用户表
CREATE TABLE users (
    user_id         BIGSERIAL PRIMARY KEY,   -- 内部用户ID
    mobile          VARCHAR(20) UNIQUE,      -- 手机号，跨平台统一标识
    nickname        VARCHAR(50),             -- 昵称
    avatar_url      TEXT,                    -- 头像地址
    create_time     TIMESTAMP DEFAULT NOW(), -- 创建时间
    update_time     TIMESTAMP DEFAULT NOW()  -- 更新时间
);

-- 平台账号绑定表
CREATE TABLE user_bindings (
    binding_id              BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL,            -- 逻辑关联 users.user_id
    provider                VARCHAR(20) NOT NULL,       -- 平台类型: wechat / alipay / app
    provider_user_id        TEXT NOT NULL,              -- 平台唯一ID（如openid、alipay_user_id、app_uuid）
    provider_unionid        TEXT,                       -- unionid
    raw_info                JSONB DEFAULT '{}',         -- 第三方返回的原始资料
    create_time             TIMESTAMP DEFAULT NOW(),
    update_time             TIMESTAMP DEFAULT NOW(),
    UNIQUE(provider, provider_user_id)                  -- 保证同一平台的ID唯一
);
