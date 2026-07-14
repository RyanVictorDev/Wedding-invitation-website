package com.wedding.backend.auth.service;

import com.wedding.backend.auth.api.AuthDtos.LoginRequest;
import com.wedding.backend.auth.api.AuthDtos.LoginResponse;
import com.wedding.backend.auth.config.JwtService;
import com.wedding.backend.auth.repository.AdminUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        var user = adminUserRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}
