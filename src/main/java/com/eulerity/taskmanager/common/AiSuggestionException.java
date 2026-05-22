package com.eulerity.taskmanager.common;

public class AiSuggestionException extends RuntimeException {

    public AiSuggestionException(String message) {
        super(message);
    }

    public AiSuggestionException(String message, Throwable cause) {
        super(message, cause);
    }
}