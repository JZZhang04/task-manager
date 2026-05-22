package com.eulerity.taskmanager.task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        Priority priority,
        TaskStatus status
) {
}