package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.UserResponse;

public interface UserService {
    UserResponse findById(Long id);
}
