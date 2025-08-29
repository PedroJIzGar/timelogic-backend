package com.timelogic.user.service;

import com.timelogic.user.entity.User;
import com.timelogic.user.dto.UpdateUserRequest;
import com.timelogic.user.dto.UserRequest;
import com.timelogic.user.dto.UserResponse;
import com.timelogic.user.error.NotFoundException;
import com.timelogic.user.mapper.UserMapper;
import com.timelogic.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final UserMapper mapper;

    @Override
    public UserResponse create(UserRequest r) {
        if (repo.existsByEmail(r.email())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = User.builder()
                .firstName(r.firstName())
                .lastName(r.lastName())
                .email(r.email().toLowerCase())
                .role(r.role())
                .active(r.active())
                .build();
        return toResponse(repo.save(u));
    }

    @Override
    public UserResponse get(UUID id) {
        return toResponse(find(id));
    }

    private static final Set<String> ALLOWED_SORTS =
            Set.of("id","firstName","lastName","email","role","active","createdAt","updatedAt");

    private Pageable sanitize(Pageable pageable) {
        List<Sort.Order> safeOrders = new ArrayList<>();

        for (Sort.Order o : pageable.getSort()) {
            String prop = o.getProperty();
            if (prop == null) continue;

            // Limpia [, ], " que manda Swagger y quédate solo con el nombre del campo
            prop = prop.replaceAll("[\\[\\]\"]", "");
            int comma = prop.indexOf(',');
            if (comma >= 0) prop = prop.substring(0, comma);

            if (ALLOWED_SORTS.contains(prop)) {
                safeOrders.add(new Sort.Order(o.getDirection(), prop));
            }
        }

        Sort sort = safeOrders.isEmpty() ? Sort.by(Sort.Order.desc("createdAt")) : Sort.by(safeOrders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    @Override
    public Page<UserResponse> list(Pageable pageable, String q) {
        pageable = sanitize(pageable); // 👈 usa el pageable saneado
        Page<User> page = (q != null && !q.isBlank())
                ? repo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, q, pageable)
                : repo.findAll(pageable);
        return page.map(mapper::toResponse);
    }
    
    @Override
    public UserResponse update(UUID id, UserRequest r) {
        User u = find(id);
        u.setFirstName(r.firstName());
        u.setLastName(r.lastName());
        u.setRole(r.role());
        u.setActive(r.active());
        // email editable si quieres, validando duplicados
        return toResponse(repo.save(u));
    }
    
    @Override
    public UserResponse partialUpdate(UUID id, UpdateUserRequest r) {
        User u = find(id);

        // si cambia el email, valida duplicado
        if (r.email() != null && !r.email().equalsIgnoreCase(u.getEmail())
                && repo.existsByEmail(r.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        mapper.partialUpdate(r, u);          // solo pisa campos no nulos
        return toResponse(repo.save(u));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("User not found");
        repo.deleteById(id);
    }

    private User find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
                u.getRole(), u.isActive(), u.getCreatedAt(), u.getUpdatedAt()
        );
    }
}