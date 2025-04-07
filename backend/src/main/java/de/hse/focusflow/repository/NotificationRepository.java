package de.hse.focusflow.repository;

import de.hse.focusflow.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Notification entity operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a specific user
     * 
     * @param userId the ID of the user
     * @return list of notifications for the user
     */
    List<Notification> findByUserId(UUID userId);

    /**
     * Find unread notifications for a specific user
     * 
     * @param userId the ID of the user
     * @return list of unread notifications for the user
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.readAt IS NULL")
    List<Notification> findUnreadByUserId(@Param("userId") UUID userId);

    /**
     * Find notifications related to a specific task
     * 
     * @param taskId the ID of the task
     * @return list of notifications related to the task
     */
    List<Notification> findByTaskId(UUID taskId);

    /**
     * Find notifications created within a time range
     * 
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of notifications created within the time range
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count unread notifications for a user
     * 
     * @param userId the ID of the user
     * @return count of unread notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.readAt IS NULL")
    long countUnreadByUserId(@Param("userId") UUID userId);

    /**
     * Mark notifications as read for a specific user
     * 
     * @param userId the ID of the user
     * @param readAt the timestamp to set as read time
     * @return number of affected rows
     */
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.user.id = :userId AND n.readAt IS NULL")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
}