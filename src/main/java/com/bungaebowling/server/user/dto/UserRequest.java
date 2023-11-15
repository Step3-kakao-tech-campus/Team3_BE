package com.bungaebowling.server.user.dto;

import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {

    public record LoginDto(
            @NotEmpty @Size(max = 100, message = "최대 100자까지 입력 가능합니다.")
            String email,
            @NotEmpty @Size(max = 64, message = "최대 64자까지 입력 가능합니다.")
            String password
    ) {
    }

    public record JoinDto(
            @Size(max = 20, message = "최대 20자까지 입니다.")
            @Pattern(regexp = "[a-zA-Z0-9가-힣]*", message = "한글, 영문, 숫자만 입력 가능합니다.")
            String name,
            @NotEmpty @Size(max = 100, message = "최대 100자까지 입니다.")
            @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 아닙니다.")
            String email,
            @NotEmpty @Size(min = 8, max = 64, message = "8자에서 64자 이내여야 합니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&!])[a-zA-Z\\d@#$%^&!]+$", message = "영문, 숫자, 특수문자가 포함되어야 합니다.")
            String password,
            @NotEmpty
            String districtId
    ) {
        public User createUser(District district, String encodedPassword) {
            return User.builder()
                    .name(name)
                    .email(email)
                    .role(Role.ROLE_PENDING)
                    .password(encodedPassword)
                    .district(district)
                    .build();
        }
    }

    public record UpdatePasswordDto(
            @NotEmpty
            @NotEmpty @Size(max = 64, message = "최대 64자까지 입력 가능합니다.")
            String password,
            @NotEmpty @Size(min = 8, max = 64, message = "8자에서 64자 이내여야 합니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&!])[a-zA-Z\\d@#$%^&!]+$", message = "영문, 숫자, 특수문자가 포함되어야 합니다.")
            String newPassword
    ) {
    }

    public record ConfirmEmailDto(
            @NotEmpty
            String token
    ) {
    }

    public record ConfirmEmailAndSendTempPasswordDto(
            @NotEmpty
            String token
    ) {
    }

    public record SendVerificationMailForPasswordResetDto(
            @NotEmpty @Size(max = 100, message = "최대 100자까지 입니다.")
            @Pattern(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 아닙니다.")
            String email
    ) {
    }

}
