package edu.eci.dosw.todo.controller;

import edu.eci.dosw.todo.dto.CategoryCreateRequest;
import edu.eci.dosw.todo.dto.CategoryResponse;
import edu.eci.dosw.todo.dto.CategoryUpdateRequest;
import edu.eci.dosw.todo.entity.CategoryColor;
import edu.eci.dosw.todo.exception.CategoryNameAlreadyExistsException;
import edu.eci.dosw.todo.exception.CategoryNotFoundException;
import edu.eci.dosw.todo.security.SecurityConfig;
import edu.eci.dosw.todo.security.SecurityErrorHandler;
import edu.eci.dosw.todo.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CategoryController.class, properties = "jwt.secret=test-secret-for-unit-tests-only-0123456789")
@Import({SecurityConfig.class, SecurityErrorHandler.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService service;

    @Test
    void findAll_shouldReturn200() throws Exception {
        when(service.findAll()).thenReturn(List.of(new CategoryResponse(1L, "Estudio", CategoryColor.BLUE)));

        mockMvc.perform(get("/api/v1/categories").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Estudio"))
                .andExpect(jsonPath("$[0].color").value("BLUE"));
    }

    @Test
    void findAll_shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).findAll();
    }

    @Test
    void findById_shouldReturn404WhenCategoryDoesNotExist() throws Exception {
        when(service.findById(99L)).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(get("/api/v1/categories/99").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category with id 99 was not found"));
    }

    @Test
    void create_shouldReturn201WhenRequestIsValid() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("Trabajo", CategoryColor.GREEN);
        when(service.create(any(CategoryCreateRequest.class)))
                .thenReturn(new CategoryResponse(2L, "Trabajo", CategoryColor.GREEN));

        mockMvc.perform(post("/api/v1/categories").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void create_shouldReturn400WhenNameIsTooShort() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("ab", CategoryColor.GREEN);

        mockMvc.perform(post("/api/v1/categories").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any());
    }

    @Test
    void create_shouldReturn400WhenNameAlreadyExists() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("Trabajo", CategoryColor.GREEN);
        when(service.create(any(CategoryCreateRequest.class)))
                .thenThrow(new CategoryNameAlreadyExistsException("Trabajo"));

        mockMvc.perform(post("/api/v1/categories").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Category with name 'Trabajo' already exists"));
    }

    @Test
    void update_shouldReturn200WhenCategoryExists() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Universidad", CategoryColor.ORANGE);
        when(service.update(eq(1L), any(CategoryUpdateRequest.class)))
                .thenReturn(new CategoryResponse(1L, "Universidad", CategoryColor.ORANGE));

        mockMvc.perform(put("/api/v1/categories/1").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Universidad"));
    }

    @Test
    void create_shouldReturn400WhenColorIsNotValid() throws Exception {
        mockMvc.perform(post("/api/v1/categories").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Trabajo\",\"color\":\"RED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or invalid value in request body"));
    }

    @Test
    void findById_shouldReturn400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/categories/abc").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }
}
