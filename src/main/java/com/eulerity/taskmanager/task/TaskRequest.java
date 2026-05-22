package com.eulerity.taskmanager.task;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "title is required")
        String title,

        String description,

        LocalDate dueDate,

        Priority priority,

        TaskStatus status
) {
}