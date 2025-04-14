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
package de.hse.focusflow.service.impl;

import de.hse.focusflow.model.Notification;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.User;
import de.hse.focusflow.repository.NotificationRepository;
import de.hse.focusflow.repository.TaskRepository;
import de.hse.focusflow.repository.UserRepository;
import de.hse.focusflow.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            TaskRepository taskRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Notification createTaskAssignmentNotification(UUID taskId, UUID assigneeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setTask(task);
        notification.setUser(user);
        notification.setMessage(String.format("You have been assigned to task: %s", task.getTitle()));
        notification.setReadAt(null);

        return notificationRepository.save(notification);
    }

    @Override
    public Notification createStatusChangeNotification(UUID taskId, String oldStatus, String newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Notification notification = new Notification();
        notification.setTask(task);
        notification.setUser(task.getAssignee());
        notification.setMessage(String.format(
                "Task '%s' status changed from %s to %s",
                task.getTitle(),
                oldStatus,
                newStatus));
        notification.setReadAt(null);

        return notificationRepository.save(notification);
    }

    @Override
    public Notification createDueDateApproachingNotification(UUID taskId, LocalDateTime dueDate) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Notification notification = new Notification();
        notification.setTask(task);
        notification.setUser(task.getAssignee());
        notification.setMessage(String.format(
                "Task '%s' is due on %s",
                task.getTitle(),
                dueDate.toString()));
        notification.setReadAt(null);

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void markAllNotificationsAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notificationRepository.delete(notification);
    }

    @Override
    public long getUnreadNotificationCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}