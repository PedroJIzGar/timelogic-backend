package com.timelogic.user.dto;

import com.timelogic.user.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}