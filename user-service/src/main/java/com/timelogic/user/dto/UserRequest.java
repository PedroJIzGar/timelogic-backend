package com.timelogic.user.dto;

import com.timelogic.user.entity.Role;
import jakarta.validation.constraints.*;

public record UserRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @NotBlank @Size(max = 160) String email,
        @NotNull Role role,
        @NotNull Boolean active
) {}