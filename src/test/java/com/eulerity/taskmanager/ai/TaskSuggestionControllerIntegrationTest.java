package com.eulerity.taskmanager.ai;

import com.eulerity.taskmanager.task.Priority;
import com.eulerity.taskmanager.task.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskSuggestionController.class)
@Import(TaskSuggestionService.class)
class TaskSuggestionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiTaskModelClient aiTaskModelClient;

    @Test
    void postTasksSuggest_usesMockedModelClientAndReturnsStructuredSuggestion() throws Exception {
        String naturalLanguageRequest = "Urgent: submit the Eulerity project README tomorrow";

        SuggestedTask mockedSuggestion = new SuggestedTask(
                "Submit Eulerity project README",
                "Complete and submit the project README.",
                LocalDate.of(2026, 5, 22),
                Priority.HIGH,
                TaskStatus.TODO
        );

        given(aiTaskModelClient.suggestTask(naturalLanguageRequest))
                .willReturn(mockedSuggestion);

        String requestBody = """
                {
                  "text": "Urgent: submit the Eulerity project README tomorrow"
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit Eulerity project README"))
                .andExpect(jsonPath("$.description").value("Complete and submit the project README."))
                .andExpect(jsonPath("$.dueDate").value("2026-05-22"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(aiTaskModelClient).suggestTask(naturalLanguageRequest);
    }

    @Test
    void postTasksSuggest_appliesDefaultsWhenModelOmitsPriorityAndStatus() throws Exception {
        String naturalLanguageRequest = "Organize notes next week";

        SuggestedTask incompleteSuggestion = new SuggestedTask(
                "Organize notes",
                "Organize notes next week",
                LocalDate.of(2026, 5, 28),
                null,
                null
        );

        given(aiTaskModelClient.suggestTask(naturalLanguageRequest))
                .willReturn(incompleteSuggestion);

        String requestBody = """
                {
                  "text": "Organize notes next week"
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Organize notes"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postTasksSuggest_rejectsBlankInputWithoutCallingModelClient() throws Exception {
        String requestBody = """
                {
                  "text": " "
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.text").value("text is required"));

        verifyNoInteractions(aiTaskModelClient);
    }
}