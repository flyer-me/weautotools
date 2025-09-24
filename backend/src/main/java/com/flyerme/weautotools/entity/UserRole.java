package com.flyerme.weautotools.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRole {
    private Serializable userId;
    private Serializable roleId;
}
