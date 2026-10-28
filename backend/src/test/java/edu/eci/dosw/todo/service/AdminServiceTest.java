package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.AdminSummaryResponse;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private AdminServiceImpl service;

    @Test
    void summary_shouldReturnAggregatedCounts() {
        when(userRepository.count()).thenReturn(3L);
        when(taskRepository.count()).thenReturn(6L);
        when(taskRepository.countByStatus(TaskStatus.PENDING)).thenReturn(3L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(1L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(2L);

        AdminSummaryResponse result = service.summary();

        assertThat(result.totalUsers()).isEqualTo(3);
        assertThat(result.totalTasks()).isEqualTo(6);
        assertThat(result.tasksByStatus())
                .containsEntry(TaskStatus.PENDING, 3L)
                .containsEntry(TaskStatus.IN_PROGRESS, 1L)
                .containsEntry(TaskStatus.COMPLETED, 2L);
    }
}
