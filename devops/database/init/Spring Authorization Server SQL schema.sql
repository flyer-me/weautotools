-- https://github.com/spring-projects/spring-authorization-server/wiki/Spring-Authorization-Server-1.1-Migration-Guide
DROP TABLE if exists oauth2_authorization_consent;
CREATE TABLE oauth2_authorization_consent
(
    registered_client_id varchar(100)  NOT NULL,
    principal_name       varchar(200)  NOT NULL,
    authorities          varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);


/*
IMPORTANT:
    If using PostgreSQL, update ALL columns defined with 'text' to 'text',
    as PostgreSQL does not support the 'text' data type.
*/
DROP TABLE if exists oauth2_authorization;
CREATE TABLE oauth2_authorization
(
    id                            varchar(100) NOT NULL,
    registered_client_id          varchar(100) NOT NULL,
    principal_name                varchar(200) NOT NULL,
    authorization_grant_type      varchar(100) NOT NULL,
    authorized_scopes             varchar(1000) DEFAULT NULL,
    attributes                    text          DEFAULT NULL,
    state                         varchar(500)  DEFAULT NULL,
    authorization_code_value      text          DEFAULT NULL,
    authorization_code_issued_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    authorization_code_expires_at TIMESTAMP WITH TIME ZONE,
    authorization_code_metadata   text          DEFAULT NULL,
    access_token_value            text          DEFAULT NULL,
    access_token_issued_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    access_token_expires_at       TIMESTAMP WITH TIME ZONE,
    access_token_metadata         text          DEFAULT NULL,
    access_token_type             varchar(100)  DEFAULT NULL,
    access_token_scopes           varchar(1000) DEFAULT NULL,
    oidc_id_token_value           text          DEFAULT NULL,
    oidc_id_token_issued_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    oidc_id_token_expires_at      TIMESTAMP WITH TIME ZONE,
    oidc_id_token_metadata        text          DEFAULT NULL,
    refresh_token_value           text          DEFAULT NULL,
    refresh_token_issued_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    refresh_token_expires_at      TIMESTAMP WITH TIME ZONE,
    refresh_token_metadata        text          DEFAULT NULL,
    user_code_value               text          DEFAULT NULL,
    user_code_issued_at           TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    user_code_expires_at          TIMESTAMP WITH TIME ZONE,
    user_code_metadata            text          DEFAULT NULL,
    device_code_value             text          DEFAULT NULL,
    device_code_issued_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    device_code_expires_at        TIMESTAMP WITH TIME ZONE,
    device_code_metadata          text          DEFAULT NULL,
    PRIMARY KEY (id)
);

DROP TABLE if exists oauth2_registered_client;
CREATE TABLE oauth2_registered_client
(
    id                            varchar(100)  NOT NULL,
    client_id                     varchar(100)  NOT NULL,
    client_id_issued_at           TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    client_secret                 varchar(200)  DEFAULT NULL,
    client_secret_expires_at      TIMESTAMP WITH TIME ZONE,
    client_name                   varchar(200)  NOT NULL,
    client_authentication_methods varchar(1000) NOT NULL,
    authorization_grant_types     varchar(1000) NOT NULL,
    redirect_uris                 varchar(1000) DEFAULT NULL,
    post_logout_redirect_uris     varchar(1000) DEFAULT NULL,
    scopes                        varchar(1000) NOT NULL,
    client_settings               varchar(2000) NOT NULL,
    token_settings                varchar(2000) NOT NULL,
    PRIMARY KEY (id)
);


INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_name,
    client_authentication_methods, authorization_grant_types, redirect_uris,post_logout_redirect_uris,
    scopes, client_settings, token_settings
) VALUES (
          '1',
          'weautotools-client',
          NOW(),
          '{bcrypt}$2a$10$lL7nku/7vykhKljnOQCpRekDSy1s2G6hbzH8lx.UNr33GsfNSXRsu',
          'weautotools-client',
          'client_secret_basic',
          'authorization_code,refresh_token',
          'http://127.0.0.1:8080/login/oauth2/code/weautotools-web-client',
          'http://127.0.0.1:8080',
          'openid,profile,email',
          '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
          '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",7200.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",2592000.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}'
         );
