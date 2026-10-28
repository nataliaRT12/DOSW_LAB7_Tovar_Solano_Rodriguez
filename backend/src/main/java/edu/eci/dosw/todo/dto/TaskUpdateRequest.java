package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 120, message = "El título no puede superar 120 caracteres")
        String title,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        @NotNull(message = "El estado es obligatorio")
        TaskStatus status,

        @NotNull(message = "La prioridad es obligatoria")
        TaskPriority priority,

        LocalDate dueDate
) {}