# Task Manager REST API

A personal task manager REST API built with Java 17 and Spring Boot. The application supports task CRUD operations, an AI-powered task suggestion endpoint, an H2 in-memory database, a minimal browser-based frontend, and automated tests.

## Features

- Create, view, update, and delete tasks through REST endpoints
- Store tasks in an H2 in-memory database
- Generate structured task suggestions from natural-language input using the OpenAI Responses API
- Serve a minimal frontend directly from Spring Boot
- Run the complete application locally with one command
- Validate API behavior with unit and integration tests

## Technology Stack

- Java 17
- Spring Boot 4.0.6
- Maven with Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- H2 in-memory database
- Jakarta Bean Validation
- OpenAI Responses API
- JUnit 5, Mockito, and MockMvc
- HTML, CSS, and vanilla JavaScript

## Prerequisites

Install:

- Java 17
- Git

Verify Java is available:

```bash
java -version
```

## Quick Start

Clone the repository and enter the project directory:

```bash
git clone https://github.com/JZZhang04/task-manager.git
cd task-manager
```

Start the application with one command:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Once the application starts, open the frontend:

```text
http://localhost:8080
```

The REST API is available at:

```text
http://localhost:8080/tasks
```

The application starts and the CRUD functionality works without any external API credentials.

## Running AI-Powered Suggestions

The `POST /tasks/suggest` endpoint calls the OpenAI Responses API. To use this endpoint, configure an OpenAI API key with available API credits before starting the application.

macOS or Linux:

```bash
export OPENAI_API_KEY="your_api_key_here"
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY="your_api_key_here"
.\mvnw.cmd spring-boot:run
```

The application uses `gpt-4o-mini` by default. An alternative model can be supplied through the optional `OPENAI_MODEL` environment variable:

```bash
export OPENAI_MODEL="gpt-4o-mini"
```

No API keys are stored in this repository.

## Frontend

The application includes a minimal browser-based interface at:

```text
http://localhost:8080
```

The frontend allows a reviewer to:

- View all existing tasks
- Create a new task
- Update task status
- Delete a task
- Submit a natural-language description to the AI endpoint
- Inspect the structured AI suggestion
- Copy the suggestion into the task creation form

## Task Model

Each task contains the following fields:

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated identifier |
| `title` | String | Required task title |
| `description` | String | Optional task details |
| `dueDate` | Date | Optional due date in `YYYY-MM-DD` format |
| `priority` | Enum | `LOW`, `MEDIUM`, or `HIGH` |
| `status` | Enum | `TODO`, `IN_PROGRESS`, or `DONE` |

## REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks` | Retrieve all tasks |
| `GET` | `/tasks/{id}` | Retrieve a task by ID |
| `PUT` | `/tasks/{id}` | Update an existing task |
| `DELETE` | `/tasks/{id}` | Delete an existing task |
| `POST` | `/tasks/suggest` | Generate a structured task suggestion using OpenAI |

## CRUD API Examples

### Create a Task

Request:

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish take-home",
    "description": "Complete the API, frontend, tests, and README",
    "dueDate": "2026-05-25",
    "priority": "HIGH",
    "status": "TODO"
  }'
```

Example response:

```json
{
  "id": 1,
  "title": "Finish take-home",
  "description": "Complete the API, frontend, tests, and README",
  "dueDate": "2026-05-25",
  "priority": "HIGH",
  "status": "TODO"
}
```

### Retrieve All Tasks

```bash
curl http://localhost:8080/tasks
```

Example response:

```json
[
  {
    "id": 1,
    "title": "Finish take-home",
    "description": "Complete the API, frontend, tests, and README",
    "dueDate": "2026-05-25",
    "priority": "HIGH",
    "status": "TODO"
  }
]
```

### Retrieve One Task

```bash
curl http://localhost:8080/tasks/1
```

### Update a Task

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Finish take-home",
    "description": "Complete the API, frontend, tests, and README",
    "dueDate": "2026-05-25",
    "priority": "HIGH",
    "status": "IN_PROGRESS"
  }'
```

Example response:

```json
{
  "id": 1,
  "title": "Finish take-home",
  "description": "Complete the API, frontend, tests, and README",
  "dueDate": "2026-05-25",
  "priority": "HIGH",
  "status": "IN_PROGRESS"
}
```

### Delete a Task

```bash
curl -X DELETE http://localhost:8080/tasks/1
```

A successful delete returns:

```text
HTTP 204 No Content
```

## AI-Powered Task Suggestion Endpoint

### Endpoint

```http
POST /tasks/suggest
```

### Description

This endpoint accepts a natural-language task description and sends it to the OpenAI Responses API.

The AI model converts the description into a structured task JSON object containing:

- `title`
- `description`
- `dueDate`
- `priority`
- `status`

The request is stateless. Generating a suggestion does not automatically save a task to the database.

The application supplies a strict JSON schema to the model so the returned result matches the task structure expected by the API.

### Configuration

Set an OpenAI API key before invoking this endpoint:

```bash
export OPENAI_API_KEY="your_api_key_here"
```

The API key must have available API credits. Without a configured key, or without available provider quota, the CRUD functionality still works but AI suggestion requests cannot be completed.

### Example Request

```bash
curl -X POST http://localhost:8080/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "text": "High priority: submit my take-home by 2026-05-23 and include the README."
  }'
```

### Example Response Shape

```json
{
  "title": "Submit take-home",
  "description": "Submit the take-home project with the README included.",
  "dueDate": "2026-05-23",
  "priority": "HIGH",
  "status": "TODO"
}
```

The exact wording of `title` and `description` may vary because they are model-generated. The response structure and enum values are constrained by the schema provided to the model.

## Validation and Error Handling

The `title` field is required when creating or updating a task.

Example invalid request:

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "priority": "MEDIUM",
    "status": "TODO"
  }'
```

Example validation response:

```json
{
  "timestamp": "2026-05-22T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "title": "title is required"
  }
}
```

Requesting a task that does not exist returns `404 Not Found`.

If `OPENAI_API_KEY` is not configured, the AI suggestion endpoint returns a service-unavailable error indicating that the key must be supplied.

If the external AI provider cannot complete a request, the endpoint returns an upstream-provider error without exposing credentials.

## H2 Database Console

The application uses an in-memory H2 database.

While the application is running, the H2 console is available at:

```text
http://localhost:8080/h2-console
```

Connection settings:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:tasksdb` |
| User Name | `sa` |
| Password | Leave blank |

Because the database is in memory, stored tasks are cleared when the application stops.

## Running Tests

Run all automated tests with:

```bash
./mvnw test
```

The test suite includes:

- Service-layer happy path tests for task operations
- Integration tests for all CRUD REST endpoints
- Validation and not-found behavior tests
- AI suggestion endpoint integration tests with the external model-client boundary mocked

Current test result:

```text
The project was verified locally with Java 17 using `./mvnw test`
```

The AI tests use a mocked `AiTaskModelClient`, so automated tests do not require an OpenAI API key or make external API calls.

## Project Structure

```text
src/
├── main/
│   ├── java/com/eulerity/taskmanager/
│   │   ├── TaskManagerApplication.java
│   │   ├── task/
│   │   │   ├── Task.java
│   │   │   ├── Priority.java
│   │   │   ├── TaskStatus.java
│   │   │   ├── TaskRepository.java
│   │   │   ├── TaskRequest.java
│   │   │   ├── TaskResponse.java
│   │   │   ├── TaskMapper.java
│   │   │   ├── TaskService.java
│   │   │   └── TaskController.java
│   │   ├── ai/
│   │   │   ├── AiTaskModelClient.java
│   │   │   ├── OpenAiTaskModelClient.java
│   │   │   ├── TaskSuggestionRequest.java
│   │   │   ├── SuggestedTask.java
│   │   │   ├── TaskSuggestionService.java
│   │   │   └── TaskSuggestionController.java
│   │   └── common/
│   │       ├── ApiError.java
│   │       ├── AiConfigurationException.java
│   │       ├── AiSuggestionException.java
│   │       ├── GlobalExceptionHandler.java
│   │       └── TaskNotFoundException.java
│   └── resources/
│       ├── application.properties
│       └── static/index.html
└── test/
    └── java/com/eulerity/taskmanager/
        ├── task/
        │   ├── TaskServiceTest.java
        │   └── TaskControllerIntegrationTest.java
        └── ai/
            └── TaskSuggestionControllerIntegrationTest.java
```

## Design Decisions

### Layered Backend Architecture

The application separates responsibilities into:

- Controllers for HTTP request and response handling
- Services for application logic
- Repositories for persistence
- DTOs for API request and response contracts
- Exception handlers for consistent error responses
- A model-client boundary for external AI provider integration

### AI Model Client Abstraction

The AI endpoint depends on the `AiTaskModelClient` interface rather than embedding external API logic inside the controller.

`OpenAiTaskModelClient` is the external-provider implementation and sends stateless task-suggestion requests to OpenAI.

This abstraction makes the AI endpoint independently testable: integration tests replace the external model client with a mock and verify that the endpoint returns well-structured JSON without performing paid network calls.

### Lightweight Frontend

The frontend is implemented as a static `index.html` file served directly by Spring Boot. This avoids a separate JavaScript build pipeline and allows reviewers to run the complete local application with a single Maven command.

## Security

- No API keys are included in the source code or repository.
- The OpenAI API key is read from the `OPENAI_API_KEY` environment variable.
- Task data is stored only in the local in-memory H2 database.
- Automated tests mock the external AI request and do not require secrets.

## Reviewer Quick Start

Run the API and frontend locally:

```bash
git clone https://github.com/JZZhang04/task-manager.git
cd task-manager
./mvnw spring-boot:run
```

Open:

```text
http://localhost:8080
```

Run automated tests:

```bash
./mvnw test
```

To evaluate the AI-powered endpoint, start the application with an OpenAI API key that has available API credits:

```bash
OPENAI_API_KEY="your_api_key_here" ./mvnw spring-boot:run
```