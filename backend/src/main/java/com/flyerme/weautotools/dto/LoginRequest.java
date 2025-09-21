package com.flyerme.weautotools.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jetbrains.annotations.NotNull;

public record LoginRequest(
        @NotBlank String Identifier,
        @NotBlank @Size(min = 6, max = 100) String password)
{
    @NotNull
    @Override
    public String toString() {
        return "LoginRequest{username='%s', password='***'}".formatted(Identifier);
    }
}
