/*
 * Copyright (c) 2025 FocusFlow Project Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.hse.focusflow.controller;

import de.hse.focusflow.config.TestSecurityConfig;
import de.hse.focusflow.dto.TaskDTO;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.model.User;
import de.hse.focusflow.security.JwtTokenProvider;
import de.hse.focusflow.service.TaskService;
import de.hse.focusflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
class TaskControllerTest extends BaseUnitTest {

        @MockBean
        private TaskService taskService;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        private UUID taskId;
        private UUID userId;
        private UUID teamId;
        private Task mockTask;
        private TaskDTO taskDTO;
        private List<Task> mockTasks;

        @BeforeEach
        void setUp() {
                taskId = UUID.randomUUID();
                userId = UUID.randomUUID();
                teamId = UUID.randomUUID();

                // Create a mock task
                mockTask = new Task();
                mockTask.setId(taskId);
                mockTask.setTitle("Test Task");
                mockTask.setShortDescription("Test Description");
                mockTask.setLongDescription("Long description for testing");
                mockTask.setDueDate(LocalDateTime.now().plusDays(7));
                mockTask.setPriority(TaskPriority.valueOf("MID"));
                mockTask.setStatus(TaskStatus.valueOf("OPEN"));

                // Create mock user for assignee and creator
                User mockUser = new User();
                mockUser.setId(userId);
                mockTask.setAssignee(mockUser);
                mockTask.setCreatedBy(mockUser);

                // Create a list of mock tasks
                mockTasks = Arrays.asList(mockTask);

                // Create task DTO for request bodies
                taskDTO = TaskDTO.builder()
                                .title("Test Task")
                                .shortDescription("Test Description")
                                .longDescription("Long description for testing")
                                .dueDate(LocalDateTime.now().plusDays(7))
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .teamId(teamId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // Mock JwtTokenProvider to handle authentication
                when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
                when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("user");
        }

        @Nested
        @DisplayName("GET /api/v1/tasks")
        class GetAllTasks {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return all tasks when authenticated")
                void shouldReturnAllTasksWhenAuthenticated() throws Exception {
                        // Given
                        given(taskService.searchTasks(null, null, null, null)).willReturn(mockTasks);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(1)))
                                        .andExpect(jsonPath("$.data[0].title", is("Test Task")));
                }

                @Test
                @DisplayName("Should return 401 when not authenticated")
                void shouldReturn401WhenNotAuthenticated() throws Exception {
                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks")
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isUnauthorized());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/{id}")
        class GetTaskById {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return task when ID exists")
                void shouldReturnTaskWhenIdExists() throws Exception {
                        // Given
                        given(taskService.getTaskById(taskId)).willReturn(mockTask);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.id", is(taskId.toString())))
                                        .andExpect(jsonPath("$.data.title", is("Test Task")));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return 404 when ID does not exist")
                void shouldReturn404WhenIdDoesNotExist() throws Exception {
                        // Given
                        given(taskService.getTaskById(taskId))
                                        .willThrow(new de.hse.focusflow.exception.ResourceNotFoundException(
                                                        "Task not found"));

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/{id}", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("POST /api/v1/tasks")
        class CreateTask {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should create task with valid data")
                void shouldCreateTaskWithValidData() throws Exception {
                        // Given
                        given(taskService.createTask(
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(LocalDateTime.class),
                                        any(TaskPriority.class),
                                        any(UUID.class),
                                        any(UUID.class),
                                        any(UUID.class),
                                        anySet())).willReturn(mockTask);

                        // When
                        ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(taskDTO)));

                        // Then
                        response.andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.title", is("Test Task")));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return 400 with invalid data")
                void shouldReturn400WithInvalidData() throws Exception {
                        // Given
                        taskDTO.setTitle(null); // Violation of @NotBlank constraint

                        // When
                        ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(taskDTO)));

                        // Then
                        response.andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("PUT /api/v1/tasks/{id}")
        class UpdateTask {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should update task with valid data")
                void shouldUpdateTaskWithValidData() throws Exception {
                        // Given
                        given(taskService.updateTask(
                                        eq(taskId),
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(LocalDateTime.class),
                                        any(TaskPriority.class),
                                        any(TaskStatus.class),
                                        any(UUID.class),
                                        any(UUID.class),
                                        anySet())).willReturn(mockTask);

                        // When
                        ResultActions response = mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(taskDTO)));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.title", is("Test Task")));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return 404 when updating non-existent task")
                void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
                        // Given
                        given(taskService.updateTask(
                                        eq(taskId),
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(LocalDateTime.class),
                                        any(TaskPriority.class),
                                        any(TaskStatus.class),
                                        any(UUID.class),
                                        any(UUID.class),
                                        anySet()))
                                        .willThrow(new de.hse.focusflow.exception.ResourceNotFoundException(
                                                        "Task not found"));

                        // When
                        ResultActions response = mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(taskDTO)));

                        // Then
                        response.andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("DELETE /api/v1/tasks/{id}")
        class DeleteTask {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should delete task when ID exists")
                void shouldDeleteTaskWhenIdExists() throws Exception {
                        // Given
                        doNothing().when(taskService).deleteTask(taskId);

                        // When
                        ResultActions response = mockMvc.perform(delete("/api/v1/tasks/{id}", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isNoContent());
                        verify(taskService, times(1)).deleteTask(taskId);
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return 404 when deleting non-existent task")
                void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
                        // Given
                        doThrow(new de.hse.focusflow.exception.ResourceNotFoundException("Task not found"))
                                        .when(taskService).deleteTask(taskId);

                        // When
                        ResultActions response = mockMvc.perform(delete("/api/v1/tasks/{id}", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/search")
        class SearchTasks {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should find tasks with search parameters")
                void shouldFindTasksWithSearchParameters() throws Exception {
                        // Given
                        given(taskService.searchTasks("Test", TaskStatus.valueOf("OPEN"), userId,
                                        TaskPriority.valueOf("MID")))
                                        .willReturn(mockTasks);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/search")
                                        .with(csrf())
                                        .param("title", "Test")
                                        .param("status", "OPEN")
                                        .param("assigneeId", userId.toString())
                                        .param("priority", "MID")
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(1)))
                                        .andExpect(jsonPath("$.data[0].title", is("Test Task")));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return empty list when no tasks match")
                void shouldReturnEmptyListWhenNoTasksMatch() throws Exception {
                        // Given
                        given(taskService.searchTasks("Nonexistent", TaskStatus.valueOf("OPEN"), userId,
                                        TaskPriority.valueOf("MID")))
                                        .willReturn(Collections.emptyList());

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/search")
                                        .with(csrf())
                                        .param("title", "Nonexistent")
                                        .param("status", "OPEN")
                                        .param("assigneeId", userId.toString())
                                        .param("priority", "MID")
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(0)));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/assignee/{userId}")
        class GetTasksByAssignee {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return tasks by assignee")
                void shouldReturnTasksByAssignee() throws Exception {
                        // Given
                        given(taskService.getTasksByAssignee(userId)).willReturn(mockTasks);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/assignee/{userId}", userId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(1)))
                                        .andExpect(jsonPath("$.data[0].assigneeId", is(userId.toString())));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/team/{teamId}")
        class GetTasksByTeam {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return tasks by team")
                void shouldReturnTasksByTeam() throws Exception {
                        // Given
                        given(taskService.getTasksByTeam(teamId)).willReturn(mockTasks);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/team/{teamId}", teamId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(1)));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/creator/{userId}")
        class GetTasksByCreator {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return tasks by creator")
                void shouldReturnTasksByCreator() throws Exception {
                        // Given
                        given(taskService.getTasksByCreator(userId)).willReturn(mockTasks);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/creator/{userId}", userId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(1)))
                                        .andExpect(jsonPath("$.data[0].createdById", is(userId.toString())));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/upcoming/{days}")
        class GetUpcomingTasks {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return upcoming tasks within days")
                void shouldReturnUpcomingTasksWithinDays() throws Exception {
                        // Given
                        given(taskService.getUpcomingTasks(7)).willReturn(mockTasks);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/upcoming/{days}", 7)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data", hasSize(1)));
                        verify(taskService).getUpcomingTasks(7);
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should handle negative days parameter")
                void shouldHandleNegativeDaysParameter() throws Exception {
                        // Configure the service to return an IllegalArgumentException for negative days
                        given(taskService.getUpcomingTasks(-1))
                                        .willThrow(new IllegalArgumentException("Days must be positive"));

                        // When using the regular mockMvc
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/upcoming/{days}", -1)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then - we expect a 400 Bad Request
                        response.andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/stats/user/{userId}")
        class GetTaskStatsByUser {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return task stats by user")
                void shouldReturnTaskStatsByUser() throws Exception {
                        // Given
                        Map<TaskStatus, Long> stats = new HashMap<>();
                        stats.put(TaskStatus.valueOf("OPEN"), 5L);
                        stats.put(TaskStatus.valueOf("PENDING"), 3L);
                        stats.put(TaskStatus.valueOf("CLOSED"), 2L);

                        given(taskService.getTaskStatsByUser(userId)).willReturn(stats);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/stats/user/{userId}", userId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.OPEN", is(5)))
                                        .andExpect(jsonPath("$.data.PENDING", is(3)))
                                        .andExpect(jsonPath("$.data.CLOSED", is(2)));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/tasks/stats/team/{teamId}")
        class GetTaskStatsByTeam {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return task stats by team")
                void shouldReturnTaskStatsByTeam() throws Exception {
                        // Given
                        Map<TaskStatus, Long> stats = new HashMap<>();
                        stats.put(TaskStatus.valueOf("OPEN"), 10L);
                        stats.put(TaskStatus.valueOf("PENDING"), 5L);
                        stats.put(TaskStatus.valueOf("CLOSED"), 3L);

                        given(taskService.getTaskStatsByTeam(teamId)).willReturn(stats);

                        // When
                        ResultActions response = mockMvc.perform(get("/api/v1/tasks/stats/team/{teamId}", teamId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.OPEN", is(10)))
                                        .andExpect(jsonPath("$.data.PENDING", is(5)))
                                        .andExpect(jsonPath("$.data.CLOSED", is(3)));
                }
        }

        @Nested
        @DisplayName("PATCH /api/v1/tasks/{id}/status")
        class UpdateTaskStatus {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should update task status")
                void shouldUpdateTaskStatus() throws Exception {
                        // Given
                        Task task = new Task();
                        task.setId(taskId);
                        task.setTitle("Test Task");
                        task.setShortDescription("Short Description");
                        task.setLongDescription("Long Description");
                        task.setDueDate(LocalDateTime.now().plusDays(7));
                        task.setPriority(TaskPriority.valueOf("MID"));
                        task.setStatus(TaskStatus.valueOf("OPEN"));

                        given(taskService.getTaskById(taskId)).willReturn(task);

                        task.setStatus(TaskStatus.valueOf("PENDING"));
                        given(taskService.updateTask(
                                        eq(taskId),
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(LocalDateTime.class),
                                        any(TaskPriority.class),
                                        eq(TaskStatus.valueOf("PENDING")),
                                        any(UUID.class),
                                        any(UUID.class),
                                        anySet())).willReturn(task);

                        // When
                        ResultActions response = mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                                        .with(csrf())
                                        .param("status", "PENDING")
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.status", is("PENDING")));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should return 400 with invalid status")
                void shouldReturn400WithInvalidStatus() throws Exception {
                        // Given
                        given(taskService.getTaskById(any()))
                                        .willThrow(new IllegalArgumentException("Invalid status"));

                        // When
                        ResultActions response = mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
                                        .with(csrf())
                                        .param("status", "INVALID_STATUS")
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("PATCH /api/v1/tasks/{id}/assignee")
        class UpdateTaskAssignee {

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should update task assignee")
                void shouldUpdateTaskAssignee() throws Exception {
                        // Given
                        UUID newAssigneeId = UUID.randomUUID();

                        Task task = new Task();
                        task.setId(taskId);
                        task.setTitle("Test Task");
                        task.setShortDescription("Short Description");
                        task.setLongDescription("Long Description");
                        task.setDueDate(LocalDateTime.now().plusDays(7));
                        task.setPriority(TaskPriority.valueOf("MID"));
                        task.setStatus(TaskStatus.valueOf("OPEN"));

                        User user = new User();
                        user.setId(userId);
                        task.setAssignee(user);

                        given(taskService.getTaskById(taskId)).willReturn(task);

                        User newAssignee = new User();
                        newAssignee.setId(newAssigneeId);
                        task.setAssignee(newAssignee);

                        given(taskService.updateTask(
                                        eq(taskId),
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(LocalDateTime.class),
                                        any(TaskPriority.class),
                                        any(TaskStatus.class),
                                        eq(newAssigneeId),
                                        any(UUID.class),
                                        anySet())).willReturn(task);

                        // When
                        ResultActions response = mockMvc.perform(patch("/api/v1/tasks/{id}/assignee", taskId)
                                        .with(csrf())
                                        .param("assigneeId", newAssigneeId.toString())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.assigneeId", is(newAssigneeId.toString())));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should unassign task when assigneeId is not provided")
                void shouldUnassignTaskWhenAssigneeIdIsNotProvided() throws Exception {
                        // Given
                        Task task = new Task();
                        task.setId(taskId);
                        task.setTitle("Test Task");
                        task.setShortDescription("Short Description");
                        task.setLongDescription("Long Description");
                        task.setDueDate(LocalDateTime.now().plusDays(7));
                        task.setPriority(TaskPriority.valueOf("MID"));
                        task.setStatus(TaskStatus.valueOf("OPEN"));

                        User user = new User();
                        user.setId(userId);
                        task.setAssignee(user);

                        given(taskService.getTaskById(taskId)).willReturn(task);

                        task.setAssignee(null);

                        given(taskService.updateTask(
                                        eq(taskId),
                                        anyString(),
                                        anyString(),
                                        anyString(),
                                        any(LocalDateTime.class),
                                        any(TaskPriority.class),
                                        any(TaskStatus.class),
                                        eq(null),
                                        any(UUID.class),
                                        anySet())).willReturn(task);

                        // When
                        ResultActions response = mockMvc.perform(patch("/api/v1/tasks/{id}/assignee", taskId)
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON));

                        // Then
                        response.andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.data.assigneeId", nullValue()));
                }
        }
}
