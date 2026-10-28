package edu.eci.dosw.todo.controller;

import edu.eci.dosw.todo.dto.LoginRequest;
import edu.eci.dosw.todo.dto.LoginResponse;
import edu.eci.dosw.todo.dto.RegisterRequest;
import edu.eci.dosw.todo.dto.UserResponse;
import edu.eci.dosw.todo.entity.UserRole;
import edu.eci.dosw.todo.exception.EmailAlreadyRegisteredException;
import edu.eci.dosw.todo.exception.InvalidCredentialsException;
import edu.eci.dosw.todo.security.SecurityConfig;
import edu.eci.dosw.todo.security.SecurityErrorHandler;
import edu.eci.dosw.todo.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, properties = "jwt.secret=test-secret-for-unit-tests-only-0123456789")
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService service;

    @Test
    void register_shouldReturn201WithoutPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("Ana Pérez", "ana@example.com", "ContrasenaDemo123!");
        when(service.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(1L, "Ana Pérez", "ana@example.com", UserRole.USER));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_shouldReturn400WhenPasswordIsTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest("Ana Pérez", "ana@example.com", "corta");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(service, never()).register(any());
    }

    @Test
    void register_shouldReturn400WhenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("Ana Pérez", "no-es-correo", "ContrasenaDemo123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenNameIsBlank() throws Exception {
        RegisterRequest request = new RegisterRequest(" ", "ana@example.com", "ContrasenaDemo123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("Ana Pérez", "ana@example.com", "ContrasenaDemo123!");
        when(service.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyRegisteredException("ana@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_shouldIgnoreRoleSentByClient() throws Exception {
        String body = """
                {"name":"Eva","email":"eva@example.com","password":"ContrasenaDemo123!","role":"ADMIN"}
                """;
        when(service.register(any(RegisterRequest.class)))
                .thenReturn(new UserResponse(2L, "Eva", "eva@example.com", UserRole.USER));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("ana@example.com", "ContrasenaDemo123!");
        when(service.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("Bearer", "signed.jwt.token", 1800));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("signed.jwt.token"))
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    @Test
    void login_shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("ana@example.com", "otraClave123");
        when(service.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }
}
