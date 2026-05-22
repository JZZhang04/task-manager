package com.eulerity.taskmanager.ai;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@CrossOrigin
public class TaskSuggestionController {

    private final TaskSuggestionService taskSuggestionService;

    public TaskSuggestionController(TaskSuggestionService taskSuggestionService) {
        this.taskSuggestionService = taskSuggestionService;
    }

    @PostMapping("/suggest")
    public SuggestedTask suggestTask(@Valid @RequestBody TaskSuggestionRequest request) {
        return taskSuggestionService.suggestTask(request);
    }
}