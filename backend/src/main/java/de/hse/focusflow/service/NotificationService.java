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

import de.hse.focusflow.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Notification operations
 */
public interface NotificationService {

    /**
     * Create a notification for task assignment
     *
     * @param taskId     ID of the task
     * @param assigneeId ID of the user being assigned
     * @return The created notification
     * @throws RuntimeException If task or user not found
     */
    Notification createTaskAssignmentNotification(UUID taskId, UUID assigneeId);

    /**
     * Create a notification for task status change
     *
     * @param taskId    ID of the task
     * @param oldStatus Previous status
     * @param newStatus New status
     * @return The created notification
     * @throws RuntimeException If task not found
     */
    Notification createStatusChangeNotification(UUID taskId, String oldStatus, String newStatus);

    /**
     * Create a notification for approaching due date
     *
     * @param taskId  ID of the task
     * @param dueDate Task due date
     * @return The created notification
     * @throws RuntimeException If task not found
     */
    Notification createDueDateApproachingNotification(UUID taskId, LocalDateTime dueDate);

    /**
     * Get all notifications for a user
     *
     * @param userId ID of the user
     * @return List of notifications
     */
    List<Notification> getUserNotifications(UUID userId);

    /**
     * Mark a notification as read
     *
     * @param notificationId ID of the notification
     * @throws RuntimeException If notification not found
     */
    void markNotificationAsRead(UUID notificationId);

    /**
     * Mark all notifications as read for a user
     *
     * @param userId ID of the user
     */
    void markAllNotificationsAsRead(UUID userId);

    /**
     * Delete a notification
     *
     * @param notificationId ID of the notification
     * @throws RuntimeException If notification not found
     */
    void deleteNotification(UUID notificationId);

    /**
     * Get unread notification count for a user
     *
     * @param userId ID of the user
     * @return Number of unread notifications
     */
    long getUnreadNotificationCount(UUID userId);

    /**
     * Data Transfer Object for Notification creation
     */
    class NotificationDTO {
        private UUID taskId;
        private UUID userId;
        private String type;
        private String message;
        private LocalDateTime createdAt;

        // Default constructor
        public NotificationDTO() {
        }

        // Getters and Setters
        public UUID getTaskId() {
            return taskId;
        }

        public void setTaskId(UUID taskId) {
            this.taskId = taskId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        // Builder for fluent API
        public static class Builder {
            private final NotificationDTO dto = new NotificationDTO();

            public Builder withTaskId(UUID taskId) {
                dto.taskId = taskId;
                return this;
            }

            public Builder withUserId(UUID userId) {
                dto.userId = userId;
                return this;
            }

            public Builder withType(String type) {
                dto.type = type;
                return this;
            }

            public Builder withMessage(String message) {
                dto.message = message;
                return this;
            }

            public Builder withCreatedAt(LocalDateTime createdAt) {
                dto.createdAt = createdAt;
                return this;
            }

            public NotificationDTO build() {
                return dto;
            }
        }
    }
}