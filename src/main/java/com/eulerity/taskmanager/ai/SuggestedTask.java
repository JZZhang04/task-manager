package com.eulerity.taskmanager.ai;

import com.eulerity.taskmanager.task.Priority;
import com.eulerity.taskmanager.task.TaskStatus;

import java.time.LocalDate;

public record SuggestedTask(
        String title,
        String description,
        LocalDate dueDate,
        Priority priority,
        TaskStatus status
) {
}