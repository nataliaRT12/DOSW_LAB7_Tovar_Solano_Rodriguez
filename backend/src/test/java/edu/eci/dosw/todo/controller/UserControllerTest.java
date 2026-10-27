package edu.eci.dosw.todo.controller;

import edu.eci.dosw.todo.dto.UserResponse;
import edu.eci.dosw.todo.entity.UserRole;
import edu.eci.dosw.todo.security.SecurityConfig;
import edu.eci.dosw.todo.security.SecurityErrorHandler;
import edu.eci.dosw.todo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, properties = "jwt.secret=test-secret-for-unit-tests-only-0123456789")
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @Test
    void me_shouldReturnProfileOfTokenSubject() throws Exception {
        when(service.findById(1L)).thenReturn(new UserResponse(1L, "Ana Pérez", "ana@example.com", UserRole.USER));

        mockMvc.perform(get("/api/v1/users/me").with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void me_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).findById(any());
    }
}
