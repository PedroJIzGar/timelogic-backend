package com.timelogic.user.mapper;

import com.timelogic.user.entity.User;
import com.timelogic.user.dto.UserRequest;
import com.timelogic.user.dto.UserResponse;
import com.timelogic.user.dto.UpdateUserRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Create: Request -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRequest request);

    // Entity -> Response
    UserResponse toResponse(User entity);

    // PUT "completo": pisa campos NO nulos del request
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UserRequest request, @MappingTarget User entity);

    // PATCH "parcial": ignora nulls
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void partialUpdate(UpdateUserRequest request, @MappingTarget User entity);

    // Normaliza email si viene informado
    @AfterMapping
    default void normalizeEmail(UserRequest req, @MappingTarget User entity) {
        if (req.email() != null) {
            entity.setEmail(req.email().toLowerCase());
        }
    }

    @AfterMapping
    default void normalizeEmail(UpdateUserRequest req, @MappingTarget User entity) {
        if (req.email() != null) {
            entity.setEmail(req.email().toLowerCase());
        }
    }
}
