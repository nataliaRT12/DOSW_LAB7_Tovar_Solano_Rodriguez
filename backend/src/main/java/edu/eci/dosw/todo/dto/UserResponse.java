package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {}
