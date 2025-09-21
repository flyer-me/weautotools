-- WeAutoTools 数据库表结构初始化脚本
-- 执行前请确保已连接到 weautotools 数据库

-- =============================================
-- 点击计数器表
-- =============================================

CREATE TABLE IF NOT EXISTS click_counters (
    id              BIGSERIAL                    PRIMARY KEY,
    counter_name    VARCHAR(100)                 NOT NULL UNIQUE,
    description     VARCHAR(500),
    click_count     BIGINT                       NOT NULL DEFAULT 0,
    enabled         BOOLEAN                      NOT NULL DEFAULT true,
    last_click_time TIMESTAMPTZ,
    created_at      TIMESTAMPTZ                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INTEGER                      NOT NULL DEFAULT 0,
    version         INTEGER                      NOT NULL DEFAULT 0
);

-- 表注释
COMMENT ON TABLE click_counters IS '点击计数器表';
COMMENT ON COLUMN click_counters.id IS '主键ID';
COMMENT ON COLUMN click_counters.counter_name IS '计数器名称';
COMMENT ON COLUMN click_counters.description IS '计数器描述';
COMMENT ON COLUMN click_counters.click_count IS '点击次数';
COMMENT ON COLUMN click_counters.enabled IS '是否启用';
COMMENT ON COLUMN click_counters.last_click_time IS '最后点击时间';
COMMENT ON COLUMN click_counters.created_at IS '创建时间';
COMMENT ON COLUMN click_counters.updated_at IS '更新时间';
COMMENT ON COLUMN click_counters.deleted IS '逻辑删除标记(0:未删除,1:已删除)';
COMMENT ON COLUMN click_counters.version IS '版本号(乐观锁)';

-- 索引
CREATE INDEX IF NOT EXISTS idx_click_counter_name ON click_counters(counter_name);
CREATE INDEX IF NOT EXISTS idx_click_counter_enabled ON click_counters(enabled);
CREATE INDEX IF NOT EXISTS idx_click_counter_deleted ON click_counters(deleted);
CREATE INDEX IF NOT EXISTS idx_click_counter_enabled_deleted ON click_counters(enabled, deleted);
CREATE INDEX IF NOT EXISTS idx_click_counter_created_at ON click_counters(created_at);

-- =============================================
-- 工具使用限制配置表
-- =============================================
CREATE TABLE IF NOT EXISTS tool_usage_limits (
    id          BIGSERIAL    PRIMARY KEY,
    user_type   VARCHAR(20)  NOT NULL,
    limit_type  VARCHAR(20)  NOT NULL,
    limit_count INTEGER      NOT NULL DEFAULT 0,
    enabled     BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER      NOT NULL DEFAULT 0,
    version     INTEGER      NOT NULL DEFAULT 0,
    tool_id     BIGINT       NOT NULL
);

-- 表注释
COMMENT ON TABLE tool_usage_limits IS '工具使用限制配置表';
COMMENT ON COLUMN tool_usage_limits.id IS '主键ID';
COMMENT ON COLUMN tool_usage_limits.user_type IS '用户类型 (ANONYMOUS:匿名用户, LOGIN:登录用户)';
COMMENT ON COLUMN tool_usage_limits.limit_type IS '限制类型 (DAILY:每日, HOURLY:每小时, TOTAL:总计)';
COMMENT ON COLUMN tool_usage_limits.limit_count IS '限制次数';
COMMENT ON COLUMN tool_usage_limits.enabled IS '是否启用';
COMMENT ON COLUMN tool_usage_limits.tool_id IS '关联的工具ID（外键关联tools表）';

-- 索引
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_user_type ON tool_usage_limits(user_type);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_enabled ON tool_usage_limits(enabled);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_deleted ON tool_usage_limits(deleted);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_tool_id ON tool_usage_limits(tool_id);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_composite_new ON tool_usage_limits(tool_id, user_type, enabled, deleted);
CREATE INDEX IF NOT EXISTS idx_tool_usage_limits_tool_id_user_type ON tool_usage_limits(tool_id, user_type, enabled) WHERE deleted = 0;

-- =============================================
-- 工具管理表
-- =============================================
CREATE TABLE IF NOT EXISTS tools (
    id          BIGSERIAL    PRIMARY KEY,
    tool_name   VARCHAR(100) NOT NULL,
    tool_type   VARCHAR(50)  NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER      NOT NULL DEFAULT 0,
    version     INTEGER      NOT NULL DEFAULT 0
);

-- 表注释
COMMENT ON TABLE tools IS '工具管理表';
COMMENT ON COLUMN tools.id IS '主键ID';
COMMENT ON COLUMN tools.tool_name IS '工具名称（唯一）';
COMMENT ON COLUMN tools.tool_type IS '工具类型';
COMMENT ON COLUMN tools.description IS '工具描述';
COMMENT ON COLUMN tools.status IS '工具状态 (ACTIVE:活跃, INACTIVE:非活跃, DEPRECATED:已弃用)';

-- 索引
CREATE INDEX IF NOT EXISTS idx_tools_tool_type ON tools(tool_type);
CREATE INDEX IF NOT EXISTS idx_tools_status ON tools(status);
CREATE INDEX IF NOT EXISTS idx_tools_deleted ON tools(deleted);
CREATE INDEX IF NOT EXISTS idx_tools_type_status ON tools(tool_type, status) WHERE deleted = 0;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id                      BIGSERIAL    PRIMARY KEY,
    password_hash           VARCHAR(255) NOT NULL DEFAULT '',
    role                    VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER',
    enabled                 BOOLEAN      DEFAULT true,
    account_non_locked      BOOLEAN      DEFAULT true,
    credentials_non_expired BOOLEAN      DEFAULT true,
    account_non_expired     BOOLEAN      DEFAULT true,
    mobile                  VARCHAR(20)  UNIQUE,
    nickname                VARCHAR(50),
    avatar_url              TEXT,
    create_at               TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP,
    update_at               TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER      NOT NULL DEFAULT 0,
    version     INTEGER      NOT NULL DEFAULT 0
);

-- 表注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '主键ID';
COMMENT ON COLUMN users.password_hash IS '密码哈希值';
COMMENT ON COLUMN users.role IS '用户角色';
COMMENT ON COLUMN users.enabled IS '是否启用';
COMMENT ON COLUMN users.account_non_locked IS '账户是否未被锁定';
COMMENT ON COLUMN users.credentials_non_expired IS '凭证是否未过期';
COMMENT ON COLUMN users.account_non_expired IS '账户是否未过期';
COMMENT ON COLUMN users.mobile IS '手机号';
COMMENT ON COLUMN users.nickname IS '昵称';
COMMENT ON COLUMN users.avatar_url IS '头像地址';
COMMENT ON COLUMN users.create_at IS '创建时间';
COMMENT ON COLUMN users.update_at IS '更新时间';

-- =============================================
-- 平台账号绑定表
-- =============================================
CREATE TABLE IF NOT EXISTS user_bindings (
    binding_id        BIGSERIAL   PRIMARY KEY,
    user_id           BIGINT      NOT NULL,
    provider          VARCHAR(20) NOT NULL,
    provider_user_id  TEXT        NOT NULL,
    provider_union_id TEXT,
    raw_info          JSONB       DEFAULT '{}',
    create_at         TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_at         TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (provider, provider_user_id)
);

-- 表注释
COMMENT ON TABLE user_bindings IS '平台账号绑定表';
COMMENT ON COLUMN user_bindings.binding_id IS '绑定主键ID';
COMMENT ON COLUMN user_bindings.user_id IS '用户ID';
COMMENT ON COLUMN user_bindings.provider IS '平台类型 (wechat/alipay/app)';
COMMENT ON COLUMN user_bindings.provider_user_id IS '平台唯一ID';
COMMENT ON COLUMN user_bindings.provider_union_id IS '平台UnionID';
COMMENT ON COLUMN user_bindings.raw_info IS '原始信息';
COMMENT ON COLUMN user_bindings.create_at IS '创建时间';
COMMENT ON COLUMN user_bindings.update_at IS '更新时间';


CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL, -- 如 "ADMIN", "USER"
                       description VARCHAR(255),
                       created_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO roles (name, description) VALUES ('ROLE_USER', '普通角色');
INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', '管理员');

drop table if exists user_roles;
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id)
);