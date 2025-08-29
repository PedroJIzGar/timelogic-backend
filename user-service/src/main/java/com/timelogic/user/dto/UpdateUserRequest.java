package com.timelogic.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.timelogic.user.entity.Role; // ajusta el package si tu Role está en otro
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateUserRequest(
        @Size(max = 80)  String firstName,
        @Size(max = 80)  String lastName,
        @Email @Size(max = 160) String email,
        Role role,
        Boolean active
) {}
