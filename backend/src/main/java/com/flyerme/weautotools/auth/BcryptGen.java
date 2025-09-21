package com.flyerme.weautotools.auth;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public class BcryptGen {
    public static void main(String[] args) {
        String rawSecret = "weautotools-client-secret0";
        String encoded = PasswordEncoderFactories
                .createDelegatingPasswordEncoder().encode(rawSecret);
        System.out.println("原始 secret: " + rawSecret);
        System.out.println("BCrypt 存库值:" + encoded);
    }
}
