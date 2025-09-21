package com.flyerme.weautotools.dto;

public record AuthInfo(
        boolean authenticated,
        String userIdentifier,
        String username,
        Object principal
) {
    public static final AuthInfo ANONYMOUS = new AuthInfo(
            false, "ANONYMOUS", "anonymousUser", null);
}