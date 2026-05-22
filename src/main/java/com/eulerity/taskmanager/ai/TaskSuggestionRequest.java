package com.eulerity.taskmanager.ai;

import jakarta.validation.constraints.NotBlank;

public record TaskSuggestionRequest(
        @NotBlank(message = "text is required")
        String text
) {
}