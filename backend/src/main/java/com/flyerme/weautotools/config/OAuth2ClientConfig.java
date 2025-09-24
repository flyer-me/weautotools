package com.flyerme.weautotools.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

/**
 * OAuth2客户端配置
 * 配置支持password grant type的客户端
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@Configuration
public class OAuth2ClientConfig {


    /**
     * 配置OAuth2客户端
     * 使用JdbcTemplate存储客户端信息
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        
        // 检查并创建weautotools-client客户端
        if (repository.findByClientId("weautotools-client") == null) {
            RegisteredClient client = createWeAutoToolsClient();
            repository.save(client);
        }

        if (repository.findByClientId("weautotools-frontend-client") == null) {
            RegisteredClient client = createWeAutoToolsFrontendClient();
            repository.save(client);
        }
        return repository;
    }

    private RegisteredClient createWeAutoToolsClient() {
        return RegisteredClient.withId("weautotools-client")
                .clientId("weautotools-client")
                .clientSecret("{bcrypt}$2a$10$57kzNaHQfsHhMOxymErQlOeGfkW.lNc98kSeMYrBAVcxGIAGBEKMO") // weautotools-secret
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://localhost:8080/login/oauth2/code/weautotools-client")
                .scope("read")
                .scope("write")
                .scope("openid")
                .scope("profile")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(2))
                        .refreshTokenTimeToLive(Duration.ofDays(30))
                        .reuseRefreshTokens(true)
                        .build())
                .build();
    }

    // 注册 SPA 客户端（公共客户端 + PKCE）
    private RegisteredClient createWeAutoToolsFrontendClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("weautotools-frontend-client") // 前端应用的客户端ID
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:5173/callback")  // 前端应用的回调地址 (登录成功后跳转)
                .postLogoutRedirectUri("http://localhost:5173/logout-success")
                // 5. 定义允许的权限范围 (scope)
                .scope(OidcScopes.OPENID) // 必须，用于OIDC流程
                .scope(OidcScopes.PROFILE) // 获取用户信息
                .scope("api.read") // 自定义API权限
                .scope("api.write")
                // 6. 关键：开启PKCE要求
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false) // TODO 验证
                        .build())
                .build();
    }
}
