package edu.eci.dosw.todo.dto;

import edu.eci.dosw.todo.entity.TaskStatus;

import java.util.Map;

public record AdminSummaryResponse(
        long totalUsers,
        long totalTasks,
        Map<TaskStatus, Long> tasksByStatus
) {}
