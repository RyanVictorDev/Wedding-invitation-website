package com.wedding.backend.auth.api;

import com.wedding.backend.auth.model.AdminUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(max = 100) String password
    ) {
    }

    public record LoginResponse(
            String token,
            String username
    ) {
    }

    public record UserResponse(
            Long id,
            String username,
            OffsetDateTime createdAt
    ) {
        public static UserResponse fromEntity(AdminUser user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
        }
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(min = 6, max = 100) String password
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank @Size(max = 100) String username,
            @Size(min = 6, max = 100) String password
    ) {
    }
}
