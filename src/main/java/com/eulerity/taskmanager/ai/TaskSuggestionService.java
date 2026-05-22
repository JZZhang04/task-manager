package com.eulerity.taskmanager.ai;

import com.eulerity.taskmanager.task.Priority;
import com.eulerity.taskmanager.task.TaskStatus;
import org.springframework.stereotype.Service;

@Service
public class TaskSuggestionService {

    private final AiTaskModelClient aiTaskModelClient;

    public TaskSuggestionService(AiTaskModelClient aiTaskModelClient) {
        this.aiTaskModelClient = aiTaskModelClient;
    }

    public SuggestedTask suggestTask(TaskSuggestionRequest request) {
        SuggestedTask suggestion = aiTaskModelClient.suggestTask(request.text());

        return new SuggestedTask(
                suggestion.title(),
                suggestion.description(),
                suggestion.dueDate(),
                suggestion.priority() == null ? Priority.MEDIUM : suggestion.priority(),
                suggestion.status() == null ? TaskStatus.TODO : suggestion.status()
        );
    }
}