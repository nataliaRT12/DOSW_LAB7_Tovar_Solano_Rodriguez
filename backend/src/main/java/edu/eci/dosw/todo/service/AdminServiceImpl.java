package edu.eci.dosw.todo.service;

import edu.eci.dosw.todo.dto.AdminSummaryResponse;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public AdminServiceImpl(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public AdminSummaryResponse summary() {
        Map<TaskStatus, Long> byStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            byStatus.put(status, taskRepository.countByStatus(status));
        }
        return new AdminSummaryResponse(userRepository.count(), taskRepository.count(), byStatus);
    }
}
