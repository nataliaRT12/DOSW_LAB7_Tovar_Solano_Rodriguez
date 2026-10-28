package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.LoginRequest;
import edu.eci.dosw.todo.dto.LoginResponse;
import edu.eci.dosw.todo.dto.RegisterRequest;
import edu.eci.dosw.todo.dto.UserResponse;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.entity.UserRole;
import edu.eci.dosw.todo.exception.EmailAlreadyRegisteredException;
import edu.eci.dosw.todo.exception.InvalidCredentialsException;
import edu.eci.dosw.todo.repository.UserRepository;
import edu.eci.dosw.todo.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        UserEntity user = new UserEntity();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        return toResponse(repository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity user = repository.findByEmail(normalize(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return new LoginResponse("Bearer", tokenService.issue(user), tokenService.getExpirationSeconds());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
