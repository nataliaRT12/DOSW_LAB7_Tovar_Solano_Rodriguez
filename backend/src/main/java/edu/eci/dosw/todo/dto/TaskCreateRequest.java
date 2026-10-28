package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 120, message = "El título no puede superar 120 caracteres")
        String title,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        TaskPriority priority,

        LocalDate dueDate
) {}