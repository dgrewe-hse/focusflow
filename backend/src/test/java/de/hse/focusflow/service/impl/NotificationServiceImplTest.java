package de.hse.focusflow.service.impl;

import de.hse.focusflow.model.Notification;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.User;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.repository.NotificationRepository;
import de.hse.focusflow.repository.TaskRepository;
import de.hse.focusflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Task testTask;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testTask = new Task();
        testTask.setId(UUID.randomUUID());
        testTask.setTitle("Test Task");
        testTask.setAssignee(testUser);

        testNotification = new Notification();
        testNotification.setId(UUID.randomUUID());
        testNotification.setTask(testTask);
        testNotification.setUser(testUser);
        testNotification.setMessage("Test notification");
        testNotification.setReadAt(null);
    }

    @Test
    void createTaskAssignmentNotification_WithValidData_ShouldCreateNotification() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTask));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(testNotification.getId());
            return notification;
        });

        // Act
        Notification result = notificationService.createTaskAssignmentNotification(testTask.getId(), testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testTask, result.getTask());
        assertEquals(testUser, result.getUser());
        assertTrue(result.getMessage().contains("You have been assigned to task: " + testTask.getTitle()));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createTaskAssignmentNotification_WithInvalidTaskId_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.createTaskAssignmentNotification(testTask.getId(), testUser.getId());
        });
    }

    @Test
    void createStatusChangeNotification_WithValidData_ShouldCreateNotification() {
        // Arrange
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(testNotification.getId());
            return notification;
        });

        // Act
        Notification result = notificationService.createStatusChangeNotification(
                testTask.getId(),
                TaskStatus.OPEN.name(),
                TaskStatus.IN_REVIEW.name());

        // Assert
        assertNotNull(result);
        assertEquals(testTask, result.getTask());
        assertEquals(testUser, result.getUser());
        assertTrue(result.getMessage().contains("Task '" + testTask.getTitle() + "' status changed from "
                + TaskStatus.OPEN + " to " + TaskStatus.IN_REVIEW));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createDueDateApproachingNotification_WithValidData_ShouldCreateNotification() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        testTask.setDueDate(dueDate);
        when(taskRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(testNotification.getId());
            return notification;
        });

        // Act
        Notification result = notificationService.createDueDateApproachingNotification(testTask.getId(), dueDate);

        // Assert
        assertNotNull(result);
        assertEquals(testTask, result.getTask());
        assertEquals(testUser, result.getUser());
        assertTrue(result.getMessage().contains("Task '" + testTask.getTitle() + "' is due on " + dueDate.toString()));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getUserNotifications_ShouldReturnNotifications() {
        // Arrange
        List<Notification> notifications = Collections.singletonList(testNotification);
        when(notificationRepository.findByUserId(any(UUID.class))).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUserNotifications(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotification, result.get(0));
    }

    @Test
    void markNotificationAsRead_WithValidId_ShouldUpdateNotification() {
        // Arrange
        when(notificationRepository.findById(any(UUID.class))).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.markNotificationAsRead(testNotification.getId());

        // Assert
        assertNotNull(testNotification.getReadAt());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAllNotificationsAsRead_ShouldUpdateAllNotifications() {
        // Arrange
        LocalDateTime currentTime = LocalDateTime.now();
        doNothing().when(notificationRepository).markAllAsRead(any(UUID.class), any(LocalDateTime.class),
                any(LocalDateTime.class));

        // Act
        notificationService.markAllNotificationsAsRead(testUser.getId());

        // Assert
        verify(notificationRepository).markAllAsRead(eq(testUser.getId()), any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void deleteNotification_WithValidId_ShouldDeleteNotification() {
        // Arrange
        when(notificationRepository.findById(any(UUID.class))).thenReturn(Optional.of(testNotification));
        doNothing().when(notificationRepository).delete(any(Notification.class));

        // Act
        notificationService.deleteNotification(testNotification.getId());

        // Assert
        verify(notificationRepository).delete(any(Notification.class));
    }

    @Test
    void getUnreadNotificationCount_ShouldReturnCorrectCount() {
        // Arrange
        LocalDateTime currentTime = LocalDateTime.now();
        when(notificationRepository.countUnreadByUserId(any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(5L);

        // Act
        long count = notificationService.getUnreadNotificationCount(testUser.getId());

        // Assert
        assertEquals(5L, count);
        verify(notificationRepository).countUnreadByUserId(eq(testUser.getId()), any(LocalDateTime.class));
    }
}