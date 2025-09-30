create table if not exists click_counters
(
    id              uuid                                               not null
        primary key,
    counter_name    varchar(100)                                       not null
        unique,
    description     varchar(500),
    click_count     bigint                   default 0                 not null,
    enabled         boolean                  default true              not null,
    last_click_time timestamp with time zone,
    created_at      timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at      timestamp with time zone default CURRENT_TIMESTAMP not null,
    deleted         integer                  default 0                 not null,
    version         integer                  default 0                 not null
);

comment on table click_counters is '点击计数器表';

comment on column click_counters.id is '主键ID';

comment on column click_counters.counter_name is '计数器名称';

comment on column click_counters.description is '计数器描述';

comment on column click_counters.click_count is '点击次数';

comment on column click_counters.enabled is '是否启用';

comment on column click_counters.last_click_time is '最后点击时间';

comment on column click_counters.created_at is '创建时间';

comment on column click_counters.updated_at is '更新时间';

comment on column click_counters.deleted is '逻辑删除标记(0:未删除,1:已删除)';

comment on column click_counters.version is '版本号(乐观锁)';

alter table click_counters
    owner to weautotools;

create table if not exists tool_usage_limits
(
    id          uuid                     default gen_random_uuid() not null
        primary key,
    user_type   varchar(20)                                        not null,
    limit_type  varchar(20)                                        not null,
    limit_count integer                  default 0                 not null,
    enabled     boolean                  default true              not null,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP not null,
    deleted     integer                  default 0                 not null,
    version     integer                  default 0                 not null,
    tool_id     bigint                                             not null
);

comment on table tool_usage_limits is '工具使用限制配置表';

comment on column tool_usage_limits.id is '主键ID';

comment on column tool_usage_limits.user_type is '用户类型 (ANONYMOUS:匿名用户, LOGIN:登录用户)';

comment on column tool_usage_limits.limit_type is '限制类型 (DAILY:每日, HOURLY:每小时, TOTAL:总计)';

comment on column tool_usage_limits.limit_count is '限制次数';

comment on column tool_usage_limits.enabled is '是否启用';

comment on column tool_usage_limits.tool_id is '关联的工具ID（外键关联tools表）';

alter table tool_usage_limits
    owner to weautotools;

create index if not exists idx_tool_usage_limits_user_type
    on tool_usage_limits (user_type);

create index if not exists idx_tool_usage_limits_enabled
    on tool_usage_limits (enabled);

create index if not exists idx_tool_usage_limits_deleted
    on tool_usage_limits (deleted);

create index if not exists idx_tool_usage_limits_tool_id
    on tool_usage_limits (tool_id);

create index if not exists idx_tool_usage_limits_composite_new
    on tool_usage_limits (tool_id, user_type, enabled, deleted);

create index if not exists idx_tool_usage_limits_tool_id_user_type
    on tool_usage_limits (tool_id, user_type, enabled)
    where (deleted = 0);

create table if not exists tools
(
    id          uuid                     default gen_random_uuid()           not null
        primary key,
    tool_name   varchar(100)                                                 not null,
    tool_type   varchar(50)                                                  not null,
    description text,
    status      varchar(20)              default 'ACTIVE'::character varying not null,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP           not null,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP           not null,
    deleted     integer                  default 0                           not null,
    version     integer                  default 0                           not null
);

comment on table tools is '工具管理表';

comment on column tools.id is '主键ID';

comment on column tools.tool_name is '工具名称（唯一）';

comment on column tools.tool_type is '工具类型';

comment on column tools.description is '工具描述';

comment on column tools.status is '工具状态 (ACTIVE:活跃, INACTIVE:非活跃, DEPRECATED:已弃用)';

alter table tools
    owner to weautotools;

create index if not exists idx_tools_tool_type
    on tools (tool_type);

create index if not exists idx_tools_status
    on tools (status);

create index if not exists idx_tools_deleted
    on tools (deleted);

create index if not exists idx_tools_type_status
    on tools (tool_type, status)
    where (deleted = 0);

create table if not exists user_bindings
(
    binding_id        uuid                     default gen_random_uuid() not null
        primary key,
    user_id           varchar(100)                                       not null,
    provider          varchar(20)                                        not null,
    provider_user_id  text                                               not null,
    provider_union_id text,
    raw_info          jsonb                    default '{}'::jsonb,
    created_at        timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at        timestamp with time zone default CURRENT_TIMESTAMP,
    unique (provider, provider_user_id)
);

alter table user_bindings
    owner to weautotools;

create table if not exists roles
(
    id           uuid                     default gen_random_uuid() not null
        primary key,
    name         varchar(50)                                        not null
        unique,
    description  varchar(255),
    created_time timestamp with time zone default CURRENT_TIMESTAMP not null
);

alter table roles
    owner to weautotools;

create table if not exists oauth2_authorization_consent
(
    registered_client_id varchar(100)  not null,
    principal_name       varchar(200)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name)
);

alter table oauth2_authorization_consent
    owner to weautotools;

create table if not exists oauth2_authorization
(
    id                            varchar(100) not null
        primary key,
    registered_client_id          varchar(100) not null,
    principal_name                varchar(200) not null,
    authorization_grant_type      varchar(100) not null,
    authorized_scopes             varchar(1000)            default NULL::character varying,
    attributes                    text,
    state                         varchar(500)             default NULL::character varying,
    authorization_code_value      text,
    authorization_code_issued_at  timestamp with time zone default now(),
    authorization_code_expires_at timestamp with time zone,
    authorization_code_metadata   text,
    access_token_value            text,
    access_token_issued_at        timestamp with time zone default now(),
    access_token_expires_at       timestamp with time zone,
    access_token_metadata         text,
    access_token_type             varchar(100)             default NULL::character varying,
    access_token_scopes           varchar(1000)            default NULL::character varying,
    oidc_id_token_value           text,
    oidc_id_token_issued_at       timestamp with time zone default now(),
    oidc_id_token_expires_at      timestamp with time zone,
    oidc_id_token_metadata        text,
    refresh_token_value           text,
    refresh_token_issued_at       timestamp with time zone default now(),
    refresh_token_expires_at      timestamp with time zone,
    refresh_token_metadata        text,
    user_code_value               text,
    user_code_issued_at           timestamp with time zone default now(),
    user_code_expires_at          timestamp with time zone,
    user_code_metadata            text,
    device_code_value             text,
    device_code_issued_at         timestamp with time zone default now(),
    device_code_expires_at        timestamp with time zone,
    device_code_metadata          text
);

alter table oauth2_authorization
    owner to weautotools;

create table if not exists oauth2_registered_client
(
    id                            varchar(100)  not null
        primary key,
    client_id                     varchar(100)  not null,
    client_id_issued_at           timestamp with time zone default now(),
    client_secret                 varchar(200)             default NULL::character varying,
    client_secret_expires_at      timestamp with time zone,
    client_name                   varchar(200)  not null,
    client_authentication_methods varchar(1000) not null,
    authorization_grant_types     varchar(1000) not null,
    redirect_uris                 varchar(1000)            default NULL::character varying,
    post_logout_redirect_uris     varchar(1000)            default NULL::character varying,
    scopes                        varchar(1000) not null,
    client_settings               varchar(2000) not null,
    token_settings                varchar(2000) not null
);

alter table oauth2_registered_client
    owner to weautotools;

create table if not exists user_roles
(
    user_id uuid not null,
    role_id uuid not null,
    primary key (user_id, role_id)
);

alter table user_roles
    owner to weautotools;

create table if not exists users
(
    id                 uuid                                                   not null
        primary key,
    password_hash      varchar(255)             default ''::character varying not null,
    enabled            boolean                  default true,
    mobile             varchar(20)
        unique,
    nickname           varchar(50),
    avatar_url         text,
    created_at         timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at         timestamp with time zone default CURRENT_TIMESTAMP,
    deleted            integer                  default 0                     not null,
    version            integer                  default 0                     not null,
    email              varchar(50),
    credentials_expiry timestamp with time zone,
    locked_until       timestamp with time zone,
    failed_attempts    integer                  default 0
);

alter table users
    owner to weautotools;


