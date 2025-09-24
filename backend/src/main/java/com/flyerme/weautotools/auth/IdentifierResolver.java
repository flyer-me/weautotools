package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.AuthInfo;
import com.flyerme.weautotools.util.IpUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class IdentifierResolver {
    private final CustomUserDetailsService userDetailsService;

    public IdentifierResolver(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 获取当前用户标识，登录状态时返回用户Id，匿名状态时返回会话ID
     */
    public AuthInfo getCurrentUserIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            var principal = authentication.getPrincipal();
            if (principal instanceof String userId) {
                // 情况 C：principal 存了 userId 当前系统使用的情况
                UserDetails userDetails = userDetailsService
                        .loadUserByUsername(userId);
                return new AuthInfo(true, userDetails.getUsername(), userDetails.getUsername());
            }else if (principal instanceof Long userId) {
                // 情况 B：Long 类型 userId
                UserDetails userDetails = userDetailsService
                        .loadUserByUsername(String.valueOf(userId));
                return new AuthInfo(true, userDetails.getUsername(), userDetails.getUsername());
            } else if (principal instanceof UserDetails userDetails) {
                // 情况 A：完整的用户对象
                return new AuthInfo(true, userDetails.getUsername(), userDetails.getUsername());
            }
            return new AuthInfo(true, null, authentication.getName());
        }
        // 对于匿名用户，返回处理后的ip作为标识符
        var request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        var hashIp = IpUtils.getClientIpAddress(request).hashCode();
        String fingerprint = Integer.toHexString(hashIp & 0xfffffff);
        return new AuthInfo(false, null, "HashedIp" + fingerprint);
    }
}
