package com.timelogic.user.controller;

import com.timelogic.user.dto.UpdateUserRequest;
import com.timelogic.user.dto.UserRequest;
import com.timelogic.user.dto.UserResponse;
import com.timelogic.user.service.UserService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PageableAsQueryParam
    @GetMapping
    public Page<UserResponse> list(
        @Parameter(hidden = true)
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,
        @RequestParam(required = false) String q
    ) {
        return service.list(pageable, q);
    }
    
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }
    
    @PatchMapping("/{id}")
    public UserResponse patch(@PathVariable UUID id,
                              @RequestBody UpdateUserRequest request) {
        return service.partialUpdate(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}