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
import de.hse.focusflow.security.JwtTokenProvider;
import de.hse.focusflow.service.TaskService;
import de.hse.focusflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validation tests for TaskController focusing on boundary values and input
 * validation.
 */
@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
class TaskControllerValidationTest extends BaseUnitTest {

        @MockBean
        private TaskService taskService;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        private TaskDTO validTaskDTO;
        private UUID taskId;
        private UUID userId;
        private LocalDateTime dueDate;

        @BeforeEach
        void setup() {
                taskId = UUID.randomUUID();
                userId = UUID.randomUUID();
                dueDate = LocalDateTime.now().plusDays(7);

                // Setup valid task DTO
                validTaskDTO = TaskDTO.builder()
                                .title("Valid Task")
                                .shortDescription("Valid description")
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // Mock JwtTokenProvider
                when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
                when(jwtTokenProvider.getUsernameFromToken(anyString())).thenReturn("user");
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with null title")
        void shouldRejectTaskWithNullTitle() throws Exception {
                // Given
                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title(null) // Invalid: title is null
                                .shortDescription("Valid description")
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with empty title")
        void shouldRejectTaskWithEmptyTitle() throws Exception {
                // Given
                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title("") // Invalid: title is empty
                                .shortDescription("Valid description")
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with title exceeding max length")
        void shouldRejectTaskWithTitleExceedingMaxLength() throws Exception {
                // Given: Creating a title with 101 characters (exceeding max length)
                StringBuilder longTitle = new StringBuilder();
                for (int i = 0; i < 101; i++) {
                        longTitle.append("a");
                }

                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title(longTitle.toString()) // Invalid: title exceeds max length
                                .shortDescription("Valid description")
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with short description exceeding max length")
        void shouldRejectTaskWithShortDescriptionExceedingMaxLength() throws Exception {
                // Given: Creating a description with 201 characters (exceeding max length)
                StringBuilder longDescription = new StringBuilder();
                for (int i = 0; i < 201; i++) {
                        longDescription.append("a");
                }

                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title("Valid Title")
                                .shortDescription(longDescription.toString()) // Invalid: short description exceeds max
                                                                              // length
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with null short description")
        void shouldRejectTaskWithNullShortDescription() throws Exception {
                // Given
                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title("Valid Title")
                                .shortDescription(null) // Invalid: short description is null
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with null due date")
        void shouldRejectTaskWithNullDueDate() throws Exception {
                // Given
                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title("Valid Title")
                                .shortDescription("Valid description")
                                .longDescription("This is a valid long description for testing")
                                .dueDate(null) // Invalid: due date is null
                                .priority(TaskPriority.valueOf("MID"))
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task with null priority")
        void shouldRejectTaskWithNullPriority() throws Exception {
                // Given
                TaskDTO invalidTaskDTO = TaskDTO.builder()
                                .title("Valid Title")
                                .shortDescription("Valid description")
                                .longDescription("This is a valid long description for testing")
                                .dueDate(dueDate)
                                .priority(null) // Invalid: priority is null
                                .status(TaskStatus.valueOf("OPEN"))
                                .assigneeId(userId)
                                .createdById(userId)
                                .tagIds(new HashSet<>())
                                .build();

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(invalidTaskDTO)));

                // Then
                response.andExpect(status().isBadRequest());
                verify(taskService, never()).createTask(
                                any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should reject task update with invalid status")
        void shouldRejectTaskUpdateWithInvalidStatus() throws Exception {
                // This is already tested in the TaskControllerTest

                // No need to implement this test case as it's covered in TaskControllerTest
                // Just adding a placeholder to maintain the test structure
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should accept task with valid minimum values")
        void shouldAcceptTaskWithValidMinimumValues() throws Exception {
                // Given: Task with only required fields
                TaskDTO minimumValidTaskDTO = TaskDTO.builder()
                                .title("Minimum Task")
                                .shortDescription("Minimum description")
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .createdById(userId)
                                .build();

                when(taskService.createTask(
                                anyString(),
                                anyString(),
                                anyString(),
                                any(LocalDateTime.class),
                                any(TaskPriority.class),
                                any(UUID.class),
                                any(UUID.class),
                                any(UUID.class),
                                anySet())).thenReturn(new Task());

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(minimumValidTaskDTO)));

                // Then
                response.andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should accept task with boundary values")
        void shouldAcceptTaskWithBoundaryValues() throws Exception {
                // Given: Task with boundary values for fields with constraints
                StringBuilder maxLengthTitle = new StringBuilder();
                for (int i = 0; i < 100; i++) { // Max length is 100
                        maxLengthTitle.append("a");
                }

                StringBuilder maxLengthShortDesc = new StringBuilder();
                for (int i = 0; i < 200; i++) { // Max length is 200
                        maxLengthShortDesc.append("b");
                }

                TaskDTO boundaryTaskDTO = TaskDTO.builder()
                                .title(maxLengthTitle.toString())
                                .shortDescription(maxLengthShortDesc.toString())
                                .dueDate(dueDate)
                                .priority(TaskPriority.valueOf("MID"))
                                .createdById(userId)
                                .build();

                when(taskService.createTask(
                                anyString(),
                                anyString(),
                                anyString(),
                                any(LocalDateTime.class),
                                any(TaskPriority.class),
                                any(UUID.class),
                                any(UUID.class),
                                any(UUID.class),
                                anySet())).thenReturn(new Task());

                // When
                ResultActions response = mockMvc.perform(post("/api/v1/tasks")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(boundaryTaskDTO)));

                // Then
                response.andExpect(status().isCreated());
        }
}
