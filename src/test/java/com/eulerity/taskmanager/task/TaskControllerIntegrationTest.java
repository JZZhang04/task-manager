package com.eulerity.taskmanager.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void clearDatabase() {
        taskRepository.deleteAll();
    }

    @Test
    void postTasks_createsTask() throws Exception {
        String requestBody = """
                {
                  "title": "Finish Eulerity take-home",
                  "description": "Build and document the task manager API",
                  "dueDate": "2026-05-25",
                  "priority": "HIGH",
                  "status": "TODO"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/tasks/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Finish Eulerity take-home"))
                .andExpect(jsonPath("$.description").value("Build and document the task manager API"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-25"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));

        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void getTasks_returnsAllTasks() throws Exception {
        taskRepository.save(new Task(
                "Build API",
                "Create CRUD endpoints",
                LocalDate.of(2026, 5, 24),
                Priority.HIGH,
                TaskStatus.IN_PROGRESS
        ));

        taskRepository.save(new Task(
                "Add tests",
                "Verify endpoint behavior",
                LocalDate.of(2026, 5, 25),
                Priority.MEDIUM,
                TaskStatus.TODO
        ));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Build API"))
                .andExpect(jsonPath("$[1].title").value("Add tests"));
    }

    @Test
    void getTaskById_returnsRequestedTask() throws Exception {
        Task savedTask = taskRepository.save(new Task(
                "Create frontend",
                "Add a static index page",
                LocalDate.of(2026, 5, 26),
                Priority.MEDIUM,
                TaskStatus.TODO
        ));

        mockMvc.perform(get("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Create frontend"))
                .andExpect(jsonPath("$.description").value("Add a static index page"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void putTask_updatesExistingTask() throws Exception {
        Task savedTask = taskRepository.save(new Task(
                "Draft README",
                "Initial draft",
                LocalDate.of(2026, 5, 24),
                Priority.LOW,
                TaskStatus.TODO
        ));

        String requestBody = """
                {
                  "title": "Finalize README",
                  "description": "Include setup commands and endpoint examples",
                  "dueDate": "2026-05-27",
                  "priority": "HIGH",
                  "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(put("/tasks/{id}", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Finalize README"))
                .andExpect(jsonPath("$.description").value("Include setup commands and endpoint examples"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-27"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        assertThat(updatedTask.getTitle()).isEqualTo("Finalize README");
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTask_removesExistingTask() throws Exception {
        Task savedTask = taskRepository.save(new Task(
                "Temporary task",
                "Task to remove",
                LocalDate.of(2026, 5, 28),
                Priority.LOW,
                TaskStatus.TODO
        ));

        mockMvc.perform(delete("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(savedTask.getId())).isFalse();

        mockMvc.perform(get("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Task not found with id: " + savedTask.getId()));
    }

    @Test
    void postTasks_rejectsBlankTitle() throws Exception {
        String requestBody = """
                {
                  "title": " ",
                  "description": "Invalid task",
                  "priority": "MEDIUM",
                  "status": "TODO"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.title").value("title is required"));

        assertThat(taskRepository.findAll()).isEmpty();
    }
}