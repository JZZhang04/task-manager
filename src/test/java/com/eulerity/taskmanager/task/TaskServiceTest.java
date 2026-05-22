package com.eulerity.taskmanager.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_savesTaskAndAppliesDefaultValues() {
        TaskRequest request = new TaskRequest(
                "Write README",
                "Document the REST API and AI endpoint",
                LocalDate.of(2026, 5, 25),
                null,
                null
        );

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        TaskResponse response = taskService.createTask(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();

        assertThat(savedTask.getTitle()).isEqualTo("Write README");
        assertThat(savedTask.getDescription()).isEqualTo("Document the REST API and AI endpoint");
        assertThat(savedTask.getDueDate()).isEqualTo(LocalDate.of(2026, 5, 25));
        assertThat(savedTask.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Write README");
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void getAllTasks_returnsMappedTaskResponses() {
        Task firstTask = storedTask(
                1L,
                "Build API",
                Priority.HIGH,
                TaskStatus.IN_PROGRESS
        );

        Task secondTask = storedTask(
                2L,
                "Add frontend",
                Priority.MEDIUM,
                TaskStatus.TODO
        );

        when(taskRepository.findAll()).thenReturn(List.of(firstTask, secondTask));

        List<TaskResponse> responses = taskService.getAllTasks();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).title()).isEqualTo("Build API");
        assertThat(responses.get(0).priority()).isEqualTo(Priority.HIGH);
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).title()).isEqualTo("Add frontend");
    }

    @Test
    void getTaskById_returnsTaskWhenItExists() {
        Task storedTask = storedTask(
                3L,
                "Test application",
                Priority.MEDIUM,
                TaskStatus.TODO
        );

        when(taskRepository.findById(3L)).thenReturn(Optional.of(storedTask));

        TaskResponse response = taskService.getTaskById(3L);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.title()).isEqualTo("Test application");
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void updateTask_updatesAndReturnsExistingTask() {
        Task existingTask = storedTask(
                4L,
                "Initial title",
                Priority.LOW,
                TaskStatus.TODO
        );

        TaskRequest request = new TaskRequest(
                "Updated title",
                "Updated description",
                LocalDate.of(2026, 5, 30),
                Priority.HIGH,
                TaskStatus.DONE
        );

        when(taskRepository.findById(4L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        TaskResponse response = taskService.updateTask(4L, request);

        verify(taskRepository).save(existingTask);

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.title()).isEqualTo("Updated title");
        assertThat(response.description()).isEqualTo("Updated description");
        assertThat(response.dueDate()).isEqualTo(LocalDate.of(2026, 5, 30));
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void deleteTask_deletesExistingTask() {
        Task existingTask = storedTask(
                5L,
                "Delete me",
                Priority.LOW,
                TaskStatus.TODO
        );

        when(taskRepository.findById(5L)).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(5L);

        verify(taskRepository).delete(existingTask);
    }

    private Task storedTask(Long id, String title, Priority priority, TaskStatus status) {
        Task task = new Task(
                title,
                "Description for " + title,
                LocalDate.of(2026, 5, 25),
                priority,
                status
        );
        task.setId(id);
        return task;
    }
}