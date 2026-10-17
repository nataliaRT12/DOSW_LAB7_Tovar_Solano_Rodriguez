package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;

    public TaskServiceImpl(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TaskResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TaskResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public TaskResponse create(TaskCreateRequest request) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setDueDate(request.dueDate());
        entity.setStatus(TaskStatus.PENDING);
        entity.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCategoryId(request.categoryId());
        return toResponse(repository.save(entity));
    }

    @Override
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        TaskEntity entity = getOrThrow(id);
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setStatus(request.status());
        entity.setPriority(request.priority());
        entity.setDueDate(request.dueDate());
        entity.setCategoryId(request.categoryId());
        return toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private TaskEntity getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    private TaskResponse toResponse(TaskEntity e) {
        return new TaskResponse(e.getId(), e.getTitle(), e.getDescription(),
                e.getStatus(), e.getPriority(), e.getDueDate(), e.getCreatedAt(), e.getCategoryId());
    }
}