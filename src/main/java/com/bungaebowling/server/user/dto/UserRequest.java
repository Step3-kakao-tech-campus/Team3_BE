package com.bungaebowling.server.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserRequest {

    public record loginDto(
            @NotEmpty @Size(max=100, message = "최대 100자까지 입력 가능합니다.")
            String email,
            @NotEmpty @Size(max = 64, message = "최대 64자까지 입력 가능합니다.")
            String password
    ) {}
}
