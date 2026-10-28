package edu.eci.dosw.todo.controller;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.security.SecurityConfig;
import edu.eci.dosw.todo.security.SecurityErrorHandler;
import edu.eci.dosw.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TaskController.class, properties = "jwt.secret=test-secret-for-unit-tests-only-0123456789")
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService service;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor ana() {
        return jwt().jwt(token -> token.subject("1"));
    }

    private TaskResponse buildResponse(Long id) {
        return new TaskResponse(id, "Terminar laboratorio DOSW", "Completar pruebas del backend",
                TaskStatus.PENDING, TaskPriority.HIGH,
                LocalDate.of(2026, 9, 25), LocalDateTime.of(2026, 9, 21, 10, 30));
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        when(service.findAll(1L)).thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/v1/tasks").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findById_shouldReturn200WhenTaskExists() throws Exception {
        when(service.findById(1L, 1L)).thenReturn(buildResponse(1L));

        mockMvc.perform(get("/api/v1/tasks/1").with(ana()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void findById_shouldReturn404WhenTaskDoesNotExist() throws Exception {
        when(service.findById(99L, 1L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/v1/tasks/99").with(ana()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task with id 99 was not found"));
    }

    @Test
    void create_shouldReturn201WhenRequestIsValid() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
                "Terminar laboratorio DOSW", "Completar pruebas del backend",
                TaskPriority.HIGH, LocalDate.of(2026, 9, 25));
        when(service.create(any(TaskCreateRequest.class), eq(1L))).thenReturn(buildResponse(1L));

        mockMvc.perform(post("/api/v1/tasks").with(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_shouldReturn400WhenTitleIsBlank() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest("", "Sin título", TaskPriority.LOW, null);

        mockMvc.perform(post("/api/v1/tasks").with(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void update_shouldReturn200WhenTaskExists() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Terminar laboratorio DOSW", "Backend y Front-end terminados",
                TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDate.of(2026, 9, 25));
        when(service.update(eq(1L), any(TaskUpdateRequest.class), eq(1L))).thenReturn(buildResponse(1L));

        mockMvc.perform(put("/api/v1/tasks/1").with(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn404WhenTaskDoesNotExist() throws Exception {
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Título", null, TaskStatus.PENDING, TaskPriority.LOW, null);
        when(service.update(eq(99L), any(TaskUpdateRequest.class), eq(1L)))
                .thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(put("/api/v1/tasks/99").with(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204WhenTaskExists() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1").with(ana()))
                .andExpect(status().isNoContent());

        verify(service).delete(1L, 1L);
    }

    @Test
    void delete_shouldReturn404WhenTaskDoesNotExist() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(service).delete(99L, 1L);

        mockMvc.perform(delete("/api/v1/tasks/99").with(ana()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verify(service, never()).findAll(any());
    }

    @Test
    void findAll_shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer token.invalido.123"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).findAll(any());
    }
}