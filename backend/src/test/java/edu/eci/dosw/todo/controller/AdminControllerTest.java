package edu.eci.dosw.todo.controller;

import edu.eci.dosw.todo.dto.AdminSummaryResponse;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.security.SecurityConfig;
import edu.eci.dosw.todo.security.SecurityErrorHandler;
import edu.eci.dosw.todo.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminController.class, properties = "jwt.secret=test-secret-for-unit-tests-only-0123456789")
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService service;

    @Test
    void summary_shouldReturn200ForAdmin() throws Exception {
        when(service.summary()).thenReturn(new AdminSummaryResponse(3, 5,
                Map.of(TaskStatus.PENDING, 2L, TaskStatus.IN_PROGRESS, 1L, TaskStatus.COMPLETED, 2L)));

        mockMvc.perform(get("/api/v1/admin/summary")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalTasks").value(5))
                .andExpect(jsonPath("$.tasksByStatus.PENDING").value(2));
    }

    @Test
    void summary_shouldReturn403ForUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/summary")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(service, never()).summary();
    }

    @Test
    void summary_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/summary"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).summary();
    }
}
