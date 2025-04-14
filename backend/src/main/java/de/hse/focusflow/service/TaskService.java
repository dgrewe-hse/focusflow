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
package de.hse.focusflow.service;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

/**
 * Service interface for Task operations
 */
public interface TaskService {

    /**
     * Create a new task
     *
     * @param title            Task title
     * @param shortDescription Brief description of the task
     * @param longDescription  Detailed description of the task
     * @param dueDate          Task due date
     * @param priority         Task priority
     * @param assigneeId       ID of the user to whom the task is assigned (null if
     *                         assigned to team)
     * @param teamId           ID of the team to which the task is assigned (null if
     *                         assigned to user)
     * @param createdById      ID of the user who created the task
     * @param tagIds           Set of tag IDs to associate with the task
     * @return The created task
     * @throws IllegalArgumentException If validation fails
     */
    Task createTask(String title, String shortDescription, String longDescription,
            LocalDateTime dueDate, TaskPriority priority,
            UUID assigneeId, UUID teamId, UUID createdById, Set<UUID> tagIds);

    /**
     * Create a new task using TaskDTO
     *
     * @param taskDTO DTO containing task data
     * @return The created task
     * @throws IllegalArgumentException If validation fails
     */
    Task createTask(TaskDTO taskDTO);

    /**
     * Get task by ID
     *
     * @param taskId ID of the task to retrieve
     * @return The task if found
     * @throws RuntimeException If task not found
     */
    Task getTaskById(UUID taskId);

    /**
     * Update an existing task
     *
     * @param taskId           ID of the task to update
     * @param title            New task title
     * @param shortDescription New brief description
     * @param longDescription  New detailed description
     * @param dueDate          New due date
     * @param priority         New priority
     * @param status           New status
     * @param assigneeId       New assignee ID
     * @param teamId           New team ID
     * @param tagIds           New set of tag IDs
     * @return The updated task
     * @throws RuntimeException         If task not found
     * @throws IllegalArgumentException If validation fails
     */
    Task updateTask(UUID taskId, String title, String shortDescription, String longDescription,
            LocalDateTime dueDate, TaskPriority priority, TaskStatus status,
            UUID assigneeId, UUID teamId, Set<UUID> tagIds);

    /**
     * Delete a task
     *
     * @param taskId ID of the task to delete
     * @throws RuntimeException If task not found
     */
    void deleteTask(UUID taskId);

    /**
     * Search for tasks with filtering
     *
     * @param title      Optional title search term
     * @param status     Optional status filter
     * @param assigneeId Optional assignee filter
     * @param priority   Optional priority filter
     * @return List of matching tasks
     */
    List<Task> searchTasks(String title, TaskStatus status, UUID assigneeId, TaskPriority priority);

    /**
     * Get tasks assigned to a specific user
     *
     * @param userId ID of the user
     * @return List of tasks assigned to the user
     */
    List<Task> getTasksByAssignee(UUID userId);

    /**
     * Get tasks assigned to a specific team
     *
     * @param teamId ID of the team
     * @return List of tasks assigned to the team
     */
    List<Task> getTasksByTeam(UUID teamId);

    /**
     * Get tasks created by a specific user
     *
     * @param userId ID of the user
     * @return List of tasks created by the user
     */
    List<Task> getTasksByCreator(UUID userId);

    /**
     * Get upcoming tasks due within the specified number of days
     *
     * @param days Number of days to look ahead
     * @return List of upcoming tasks
     */
    List<Task> getUpcomingTasks(int days);

    /**
     * Get task statistics by status for a user
     *
     * @param userId ID of the user
     * @return Map of status to count
     */
    Map<TaskStatus, Long> getTaskStatsByUser(UUID userId);

    /**
     * Get task statistics by status for a team
     *
     * @param teamId ID of the team
     * @return Map of status to count
     */
    Map<TaskStatus, Long> getTaskStatsByTeam(UUID teamId);

    /**
     * Data Transfer Object for Task creation and updates
     */
    class TaskDTO {
        private String title;
        private String shortDescription;
        private String longDescription;
        private LocalDateTime dueDate;
        private TaskPriority priority;
        private TaskStatus status;
        private UUID assigneeId;
        private UUID teamId;
        private UUID createdById;
        private Set<UUID> tagIds;

        // Default constructor
        public TaskDTO() {
        }

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public void setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
        }

        public String getLongDescription() {
            return longDescription;
        }

        public void setLongDescription(String longDescription) {
            this.longDescription = longDescription;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }

        public TaskPriority getPriority() {
            return priority;
        }

        public void setPriority(TaskPriority priority) {
            this.priority = priority;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public void setStatus(TaskStatus status) {
            this.status = status;
        }

        public UUID getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(UUID assigneeId) {
            this.assigneeId = assigneeId;
        }

        public UUID getTeamId() {
            return teamId;
        }

        public void setTeamId(UUID teamId) {
            this.teamId = teamId;
        }

        public UUID getCreatedById() {
            return createdById;
        }

        public void setCreatedById(UUID createdById) {
            this.createdById = createdById;
        }

        public Set<UUID> getTagIds() {
            return tagIds;
        }

        public void setTagIds(Set<UUID> tagIds) {
            this.tagIds = tagIds;
        }

        // Builder for fluent API
        public static class Builder {
            private final TaskDTO dto = new TaskDTO();

            public Builder withTitle(String title) {
                dto.title = title;
                return this;
            }

            public Builder withShortDescription(String shortDescription) {
                dto.shortDescription = shortDescription;
                return this;
            }

            public Builder withLongDescription(String longDescription) {
                dto.longDescription = longDescription;
                return this;
            }

            public Builder withDueDate(LocalDateTime dueDate) {
                dto.dueDate = dueDate;
                return this;
            }

            public Builder withPriority(TaskPriority priority) {
                dto.priority = priority;
                return this;
            }

            public Builder withStatus(TaskStatus status) {
                dto.status = status;
                return this;
            }

            public Builder withAssigneeId(UUID assigneeId) {
                dto.assigneeId = assigneeId;
                return this;
            }

            public Builder withTeamId(UUID teamId) {
                dto.teamId = teamId;
                return this;
            }

            public Builder withCreatedById(UUID createdById) {
                dto.createdById = createdById;
                return this;
            }

            public Builder withTagIds(Set<UUID> tagIds) {
                dto.tagIds = tagIds;
                return this;
            }

            public TaskDTO build() {
                return dto;
            }
        }
    }
}