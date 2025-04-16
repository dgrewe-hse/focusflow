package de.hse.focusflow.repository;

import de.hse.focusflow.model.Notification;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Create and persist a test user
        testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("Test123!@#12"); // Valid password that meets requirements
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persist(testUser);

        // Create and persist a test task
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setShortDescription("Test Description");
        testTask.setDueDate(LocalDateTime.now().plusDays(1));
        testTask.setPriority(TaskPriority.MID);
        testTask.setStatus(TaskStatus.OPEN);
        testTask.setCreatedBy(testUser);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
        testTask = entityManager.persist(testTask);

        entityManager.flush();
    }

    @Test
    void findByUserId_ShouldReturnNotifications() {
        // Arrange
        Notification notification = new Notification();
        notification.setMessage("Test notification");
        notification.setReadAt(LocalDateTime.now());
        notification.setUser(testUser);
        notification.setTask(testTask);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(notification);
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByUserId(testUser.getId());

        // Assert
        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(notification.getMessage(), notifications.get(0).getMessage());
    }

    @Test
    void findUnreadByUserId_ShouldReturnUnreadNotifications() {
        // Arrange
        Notification unreadNotification = new Notification();
        unreadNotification.setMessage("Unread notification");
        unreadNotification.setReadAt(LocalDateTime.now().plusDays(1)); // Future read date = unread
        unreadNotification.setUser(testUser);
        unreadNotification.setTask(testTask);
        unreadNotification.setCreatedAt(LocalDateTime.now());
        unreadNotification.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(unreadNotification);

        Notification readNotification = new Notification();
        readNotification.setMessage("Read notification");
        readNotification.setReadAt(LocalDateTime.now().minusDays(1)); // Past read date = read
        readNotification.setUser(testUser);
        readNotification.setTask(testTask);
        readNotification.setCreatedAt(LocalDateTime.now());
        readNotification.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(readNotification);

        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findUnreadByUserId(testUser.getId(),
                LocalDateTime.now());

        // Assert
        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(unreadNotification.getMessage(), notifications.get(0).getMessage());
    }

    @Test
    void findByTaskId_ShouldReturnNotifications() {
        // Arrange
        Notification notification = new Notification();
        notification.setMessage("Test notification");
        notification.setReadAt(LocalDateTime.now());
        notification.setUser(testUser);
        notification.setTask(testTask);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(notification);
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByTaskId(testTask.getId());

        // Assert
        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(notification.getMessage(), notifications.get(0).getMessage());
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnNotifications() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Notification notification = new Notification();
        notification.setMessage("Test notification");
        notification.setReadAt(LocalDateTime.now());
        notification.setUser(testUser);
        notification.setTask(testTask);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(notification);
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByCreatedAtBetween(startDate, endDate);

        // Assert
        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(notification.getMessage(), notifications.get(0).getMessage());
    }

    // @Test
    // void markAllAsRead_ShouldUpdateAllNotifications() {
    // // Arrange
    // LocalDateTime now = LocalDateTime.now();

    // Notification notification1 = new Notification();
    // notification1.setMessage("Notification 1");
    // notification1.setReadAt(now.plusDays(1)); // Unread
    // notification1.setUser(testUser);
    // notification1.setTask(testTask);
    // notification1.setCreatedAt(now);
    // notification1.setUpdatedAt(now);
    // entityManager.persist(notification1);

    // Notification notification2 = new Notification();
    // notification2.setMessage("Notification 2");
    // notification2.setReadAt(now.plusDays(1)); // Unread
    // notification2.setUser(testUser);
    // notification2.setTask(testTask);
    // notification2.setCreatedAt(now);
    // notification2.setUpdatedAt(now);
    // entityManager.persist(notification2);

    // entityManager.flush();

    // LocalDateTime readAt = LocalDateTime.now();

    // // Act
    // notificationRepository.markAllAsRead(testUser.getId(), readAt, readAt);

    // // Assert
    // List<Notification> updatedNotifications =
    // notificationRepository.findByUserId(testUser.getId());
    // assertNotNull(updatedNotifications);
    // assertEquals(2, updatedNotifications.size());
    // updatedNotifications.forEach(n -> {
    // assertNotNull(n.getReadAt());
    // // Check if the readAt time is within 1 second of the expected time
    // long timeDiff = Math
    // .abs(n.getReadAt().toEpochSecond(ZoneOffset.UTC) -
    // readAt.toEpochSecond(ZoneOffset.UTC));
    // assertTrue(timeDiff <= 1, "Time difference should be less than or equal to 1
    // second");
    // });
    // }
}