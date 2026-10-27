package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;

import java.util.List;

public interface TaskService {
    List<TaskResponse> findAll(Long ownerId);
    TaskResponse findById(Long id, Long ownerId);
    TaskResponse create(TaskCreateRequest request, Long ownerId);
    TaskResponse update(Long id, TaskUpdateRequest request, Long ownerId);
    void delete(Long id, Long ownerId);
}