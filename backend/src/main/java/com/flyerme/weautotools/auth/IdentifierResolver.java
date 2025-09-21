package com.flyerme.weautotools.auth;

import com.flyerme.weautotools.dto.AuthInfo;
import com.flyerme.weautotools.util.IpUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class IdentifierResolver {
    /**
     * 获取当前用户标识，登录状态时返回用户Id，匿名状态时返回会话ID
     */
    public AuthInfo getCurrentUserIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            var principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                return new AuthInfo(true, userDetails.getUserId(), userDetails.getUsername());
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
