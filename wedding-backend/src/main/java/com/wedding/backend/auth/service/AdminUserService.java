package com.wedding.backend.auth.service;

import com.wedding.backend.auth.api.AuthDtos.CreateUserRequest;
import com.wedding.backend.auth.api.AuthDtos.UpdateUserRequest;
import com.wedding.backend.auth.api.AuthDtos.UserResponse;
import com.wedding.backend.auth.model.AdminUser;
import com.wedding.backend.auth.repository.AdminUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return adminUserRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (adminUserRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
        }

        AdminUser user = new AdminUser();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return UserResponse.fromEntity(adminUserRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        AdminUser user = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!user.getUsername().equalsIgnoreCase(request.username())
                && adminUserRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
        }

        user.setUsername(request.username().trim());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return UserResponse.fromEntity(adminUserRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (adminUserRepository.count() <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível excluir o último usuário");
        }

        if (!adminUserRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        adminUserRepository.deleteById(id);
    }
}
