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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private TokenService tokenService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(repository, passwordEncoder, tokenService);
    }

    private UserEntity buildUser(String rawPassword) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("Ana Pérez");
        user.setEmail("ana@example.com");
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.USER);
        return user;
    }

    @Test
    void register_shouldCreateUserWithRoleUserAndHashedPassword() {
        when(repository.existsByEmail("ana@example.com")).thenReturn(false);
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponse result = service.register(
                new RegisterRequest("Ana Pérez", "ana@example.com", "ContrasenaDemo123!"));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getPasswordHash()).isNotEqualTo("ContrasenaDemo123!").startsWith("$2");
        assertThat(passwordEncoder.matches("ContrasenaDemo123!", saved.getPasswordHash())).isTrue();
        assertThat(result.role()).isEqualTo(UserRole.USER);
        assertThat(result.email()).isEqualTo("ana@example.com");
    }

    @Test
    void register_shouldNormalizeEmail() {
        when(repository.existsByEmail("ana@example.com")).thenReturn(false);
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = service.register(
                new RegisterRequest("  Ana Pérez ", "  Ana@Example.COM ", "ContrasenaDemo123!"));

        assertThat(result.email()).isEqualTo("ana@example.com");
        assertThat(result.name()).isEqualTo("Ana Pérez");
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        when(repository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Ana Pérez", "ANA@example.com", "ContrasenaDemo123!")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        UserEntity user = buildUser("ContrasenaDemo123!");
        when(repository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(tokenService.issue(user)).thenReturn("signed.jwt.token");
        when(tokenService.getExpirationSeconds()).thenReturn(1800L);

        LoginResponse result = service.login(new LoginRequest("Ana@example.com", "ContrasenaDemo123!"));

        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(result.expiresIn()).isEqualTo(1800L);
    }

    @Test
    void login_shouldThrowWhenPasswordIsWrong() {
        when(repository.findByEmail("ana@example.com"))
                .thenReturn(Optional.of(buildUser("ContrasenaDemo123!")));

        assertThatThrownBy(() -> service.login(new LoginRequest("ana@example.com", "otraClave123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenService, never()).issue(any());
    }

    @Test
    void login_shouldThrowWhenUserDoesNotExist() {
        when(repository.findByEmail("nadie@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@example.com", "ContrasenaDemo123!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
