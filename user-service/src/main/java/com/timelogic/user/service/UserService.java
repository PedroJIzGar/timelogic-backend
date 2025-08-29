package com.timelogic.user.service;

import com.timelogic.user.dto.UpdateUserRequest;
import com.timelogic.user.dto.UserRequest;
import com.timelogic.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse create(UserRequest request);
    UserResponse get(UUID id);
    Page<UserResponse> list(Pageable pageable, String q);
    UserResponse update(UUID id, UserRequest request);
    void delete(UUID id);
    UserResponse partialUpdate(UUID id, UpdateUserRequest request);
}