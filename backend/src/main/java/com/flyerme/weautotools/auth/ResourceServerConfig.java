package com.flyerme.weautotools.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    private final UserIdAuthenticationSuccessHandler successHandler;

    public ResourceServerConfig(UserIdAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    // 2) 业务 API（返回 401/403，不跳登录页；无状态）
    @Bean @Order(2)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
                /*.exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));*/

        return http.build();
    }


    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**","/auth/**").permitAll()
                        .anyRequest().authenticated()
                ).csrf(AbstractHttpConfigurer::disable // 关闭 CSRF
                );
                //.formLogin(login -> login.successHandler(successHandler));

        return http.build();
        /*
        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/assets/**","/auth/**", "/click-counter/**",
                                "/actuator/health","/.well-known/**","/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                //.formLogin(withDefaults())  // 配置表单登录，用于用户通过浏览器登录
                // 禁用CSRF，因为我们使用JWT，是无状态的
                //.csrf(AbstractHttpConfigurer::disable);
                .cors(Customizer.withDefaults());

        return http.build();*/
    }

}
