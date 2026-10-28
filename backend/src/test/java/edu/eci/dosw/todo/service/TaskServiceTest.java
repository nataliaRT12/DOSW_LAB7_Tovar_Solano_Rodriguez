package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.TaskCreateRequest;
import edu.eci.dosw.todo.dto.TaskResponse;
import edu.eci.dosw.todo.dto.TaskUpdateRequest;
import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.exception.TaskNotFoundException;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
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

    private static final Long ANA = 1L;
    private static final Long LUIS = 2L;

    @Mock
    private TaskRepository repository;

    @Mock
    private UserRepository userRepository;

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

    private UserEntity buildUser(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }

    @Test
    void findAll_shouldReturnOnlyOwnerTasks() {
        when(repository.findAllByOwnerId(ANA)).thenReturn(List.of(buildEntity(1L), buildEntity(2L)));

        List<TaskResponse> result = service.findAll(ANA);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        verify(repository, never()).findAll();
    }

    @Test
    void findById_shouldReturnTaskWhenExists() {
        when(repository.findByIdAndOwnerId(1L, ANA)).thenReturn(Optional.of(buildEntity(1L)));

        TaskResponse result = service.findById(1L, ANA);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Terminar laboratorio DOSW");
    }

    @Test
    void findById_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findByIdAndOwnerId(99L, ANA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L, ANA))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findById_shouldThrowExceptionWhenTaskBelongsToAnotherUser() {
        when(repository.findByIdAndOwnerId(25L, ANA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(25L, ANA))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void create_shouldCreateTask() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Nueva tarea", "Descripción", TaskPriority.HIGH, LocalDate.of(2026, 9, 25));
        when(userRepository.getReferenceById(ANA)).thenReturn(buildUser(ANA));
        when(repository.save(any(TaskEntity.class))).thenAnswer(inv -> {
            TaskEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        TaskResponse result = service.create(request, ANA);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Nueva tarea");
        assertThat(result.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 9, 25));
    }

    @Test
    void create_shouldAssignDefaultStatusAndOwner() {
        TaskCreateRequest request = new TaskCreateRequest("Tarea", null, null, null);
        when(userRepository.getReferenceById(ANA)).thenReturn(buildUser(ANA));
        when(repository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(request, ANA);

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(repository).save(captor.capture());
        TaskEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(saved.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getOwner().getId()).isEqualTo(ANA);
    }

    @Test
    void update_shouldUpdateExistingTask() {
        when(repository.findByIdAndOwnerId(1L, ANA)).thenReturn(Optional.of(buildEntity(1L)));
        when(repository.save(any(TaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Título editado", "Backend y Front-end terminados",
                TaskStatus.IN_PROGRESS, TaskPriority.LOW, LocalDate.of(2026, 10, 1));

        TaskResponse result = service.update(1L, request, ANA);

        assertThat(result.title()).isEqualTo("Título editado");
        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.priority()).isEqualTo(TaskPriority.LOW);
        assertThat(result.dueDate()).isEqualTo(LocalDate.of(2026, 10, 1));
    }

    @Test
    void update_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findByIdAndOwnerId(99L, ANA)).thenReturn(Optional.empty());
        TaskUpdateRequest request = new TaskUpdateRequest(
                "x", null, TaskStatus.PENDING, TaskPriority.LOW, null);

        assertThatThrownBy(() -> service.update(99L, request, ANA))
                .isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldNotModifyAnotherUserTask() {
        when(repository.findByIdAndOwnerId(25L, LUIS)).thenReturn(Optional.empty());
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Cambiada por Luis", null, TaskStatus.COMPLETED, TaskPriority.LOW, null);

        assertThatThrownBy(() -> service.update(25L, request, LUIS))
                .isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteExistingTask() {
        TaskEntity task = buildEntity(1L);
        when(repository.findByIdAndOwnerId(1L, ANA)).thenReturn(Optional.of(task));

        service.delete(1L, ANA);

        verify(repository).delete(task);
    }

    @Test
    void delete_shouldThrowExceptionWhenTaskDoesNotExist() {
        when(repository.findByIdAndOwnerId(99L, ANA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L, ANA))
                .isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_shouldNotDeleteAnotherUserTask() {
        when(repository.findByIdAndOwnerId(25L, LUIS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(25L, LUIS))
                .isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).delete(any());
        verify(repository, never()).deleteById(any());
    }
}
