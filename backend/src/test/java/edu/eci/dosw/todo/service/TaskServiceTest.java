package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskServiceImpl service;

    private TaskEntity buildEntity(Long id) {
        TaskEntity e = new TaskEntity();
        e.setId(id);
        e.setTitle("Terminar laboratorio DOSW");
        e.setDescription("Completar pruebas del backend");
        e.setStatus(TaskStatus.PENDING);
        e.setPriority(TaskPriority.HIGH);
        e.setDueDate(LocalDate.of(2026, 9, 25));
        e.setCreatedAt(LocalDateTime.of(2026, 9, 21, 10, 30));
        return e;
    }

    @Test
    void findAll_shouldReturnTasks() {
        when(repository.findAll()).thenReturn(List.of(buildEntity(1L), buildEntity(2L)));

        List<TaskResponse> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldReturnTaskWhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildEntity(1L)));

        TaskResponse result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Terminar laboratorio DOSW");
    }

    @Test
    void findById_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldCreateTask() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nueva tarea", "Descripción", TaskPriority.HIGH, LocalDate.of(2026, 9, 25), null);
        when(repository.save(any(TaskEntity.class))).thenAnswer(inv -> {
            TaskEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        TaskResponse result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Nueva tarea");
        assertThat(result.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 9, 25));
    }

    @Test
    void create_shouldAssignDefaultStatus() {
        TaskCreateRequest request = new TaskCreateRequest("Tarea", null, null, null, null);
        when(repository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(request);

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(repository).save(captor.capture());
        TaskEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(saved.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void update_shouldUpdateExistingTask() {
        when(repository.findById(1L)).thenReturn(Optional.of(buildEntity(1L)));
        when(repository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Título editado", "Backend y Front-end terminados",
                TaskStatus.IN_PROGRESS, TaskPriority.LOW, LocalDate.of(2026, 10, 1), null);

        TaskResponse result = service.update(1L, request);

        assertThat(result.title()).isEqualTo("Título editado");
        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.priority()).isEqualTo(TaskPriority.LOW);
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void update_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        TaskUpdateRequest request = new TaskUpdateRequest(
                "x", null, TaskStatus.PENDING, TaskPriority.LOW, null, null);

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteExistingTask() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}