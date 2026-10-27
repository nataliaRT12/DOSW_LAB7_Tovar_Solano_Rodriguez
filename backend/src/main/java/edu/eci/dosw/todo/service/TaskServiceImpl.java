package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TaskResponse> findAll(Long ownerId) {
        return repository.findAllByOwnerId(ownerId).stream().map(this::toResponse).toList();
    }

    @Override
    public TaskResponse findById(Long id, Long ownerId) {
        return toResponse(getOrThrow(id, ownerId));
    }

    @Override
    public TaskResponse create(TaskCreateRequest request, Long ownerId) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setDueDate(request.dueDate());
        entity.setStatus(TaskStatus.PENDING);
        entity.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setOwner(userRepository.getReferenceById(ownerId));
        return toResponse(repository.save(entity));
    }

    @Override
    public TaskResponse update(Long id, TaskUpdateRequest request, Long ownerId) {
        TaskEntity entity = getOrThrow(id, ownerId);
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setStatus(request.status());
        entity.setPriority(request.priority());
        entity.setDueDate(request.dueDate());
        return toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id, Long ownerId) {
        repository.delete(getOrThrow(id, ownerId));
    }

    private TaskEntity getOrThrow(Long id, Long ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new TaskNotFoundException(id));
    }

    private TaskResponse toResponse(TaskEntity e) {
        return new TaskResponse(e.getId(), e.getTitle(), e.getDescription(),
                e.getStatus(), e.getPriority(), e.getDueDate(), e.getCreatedAt());
    }
}