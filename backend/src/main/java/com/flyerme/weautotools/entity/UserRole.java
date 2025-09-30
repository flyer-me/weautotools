package com.flyerme.weautotools.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRole {
    private UUID userId;
    private UUID roleId;
}
