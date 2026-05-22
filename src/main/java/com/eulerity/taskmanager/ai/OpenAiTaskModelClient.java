package com.eulerity.taskmanager.ai;

import com.eulerity.taskmanager.common.AiConfigurationException;
import com.eulerity.taskmanager.common.AiSuggestionException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;

@Component
public class OpenAiTaskModelClient implements AiTaskModelClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiTaskModelClient(
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    @Override
    public SuggestedTask suggestTask(String naturalLanguageText) {
        validateConfiguration();

        ObjectNode requestBody = buildRequestBody(naturalLanguageText);

        try {
            JsonNode responseBody = restClient.post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            String structuredOutput = extractStructuredOutput(responseBody);

            return objectMapper.readValue(structuredOutput, SuggestedTask.class);
        } catch (RestClientResponseException ex) {
            throw new AiSuggestionException(
                    "AI model request failed with HTTP status " + ex.getStatusCode().value() + ".",
                    ex
            );
        } catch (RestClientException ex) {
            throw new AiSuggestionException(
                    "Unable to reach the AI model provider.",
                    ex
            );
        } catch (JacksonException ex) {
            throw new AiSuggestionException(
                    "AI model returned an invalid structured task response.",
                    ex
            );
        }
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiConfigurationException(
                    "OPENAI_API_KEY is not configured. Set the environment variable to use task suggestions."
            );
        }
    }

    private ObjectNode buildRequestBody(String naturalLanguageText) {
        ObjectNode requestBody = objectMapper.createObjectNode();

        requestBody.put("model", model);

        ArrayNode input = requestBody.putArray("input");

        input.addObject()
                .put("role", "system")
                .put(
                        "content",
                        """
                        Convert the user's natural-language task description into a structured task.
                        Today's date is %s.
                        Use an ISO date in YYYY-MM-DD format for dueDate when the user gives a recognizable due date.
                        Use null for dueDate when no date can be inferred.
                        Choose priority from LOW, MEDIUM, or HIGH.
                        Always set status to TODO.
                        Keep the title concise and actionable.
                        """.formatted(LocalDate.now())
                );

        input.addObject()
                .put("role", "user")
                .put("content", naturalLanguageText);

        ObjectNode format = requestBody
                .putObject("text")
                .putObject("format");

        format.put("type", "json_schema");
        format.put("name", "task_suggestion");
        format.put("strict", true);

        ObjectNode schema = format.putObject("schema");
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");

        properties.putObject("title")
                .put("type", "string");

        properties.putObject("description")
                .put("type", "string");

        ObjectNode dueDate = properties.putObject("dueDate");
        dueDate.putArray("type")
                .add("string")
                .add("null");

        ObjectNode priority = properties.putObject("priority");
        priority.put("type", "string");
        priority.putArray("enum")
                .add("LOW")
                .add("MEDIUM")
                .add("HIGH");

        ObjectNode status = properties.putObject("status");
        status.put("type", "string");
        status.putArray("enum")
                .add("TODO");

        schema.putArray("required")
                .add("title")
                .add("description")
                .add("dueDate")
                .add("priority")
                .add("status");

        schema.put("additionalProperties", false);

        return requestBody;
    }

    private String extractStructuredOutput(JsonNode responseBody) {
        if (responseBody == null) {
            throw new AiSuggestionException("AI model returned an empty response.");
        }

        for (JsonNode outputItem : responseBody.path("output")) {
            for (JsonNode contentItem : outputItem.path("content")) {
                if ("output_text".equals(contentItem.path("type").asText())
                        && contentItem.hasNonNull("text")) {
                    return contentItem.path("text").asText();
                }
            }
        }

        throw new AiSuggestionException(
                "AI model response did not contain a structured task result."
        );
    }
}