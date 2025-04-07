package de.hse.focusflow.repository;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Task entity operations
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Find tasks by title containing the given text (case insensitive)
     * 
     * @param title the title text to search for
     * @return list of tasks matching the search
     */
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> findByTitleContaining(@Param("title") String title);

    /**
     * Find tasks by their status
     * 
     * @param status the task status to filter by
     * @return list of tasks with the given status
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Find tasks assigned to a specific user
     * 
     * @param assigneeId the ID of the assignee
     * @return list of tasks assigned to the user
     */
    List<Task> findByAssigneeId(UUID assigneeId);

    /**
     * Find tasks assigned to a specific team
     * 
     * @param teamId the ID of the team
     * @return list of tasks assigned to the team
     */
    List<Task> findByTeamId(UUID teamId);

    /**
     * Find tasks by priority
     * 
     * @param priority the task priority to filter by
     * @return list of tasks with the given priority
     */
    List<Task> findByPriority(TaskPriority priority);

    /**
     * Find tasks with due dates before the given date
     * 
     * @param date the date to compare against
     * @return list of tasks due before the given date
     */
    List<Task> findByDueDateBefore(LocalDateTime date);

    /**
     * Find tasks with due dates approaching in the next few days
     * 
     * @param startDate the start date (today)
     * @param endDate   the end date (e.g. 3 days from now)
     * @return list of tasks due within the date range
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status <> 'CLOSED'")
    List<Task> findUpcomingTasks(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find tasks created by a specific user
     * 
     * @param createdById the ID of the task creator
     * @return list of tasks created by the user
     */
    List<Task> findByCreatedById(UUID createdById);

    /**
     * Search tasks by various criteria
     * 
     * @param title      optional title filter
     * @param status     optional status filter
     * @param assigneeId optional assignee filter
     * @param priority   optional priority filter
     * @return list of tasks matching all provided criteria
     */
    @Query("SELECT t FROM Task t WHERE " +
            "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
            "(:priority IS NULL OR t.priority = :priority)")
    List<Task> searchTasks(
            @Param("title") String title,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId,
            @Param("priority") TaskPriority priority);

    /**
     * Count tasks by status for a specific user
     * 
     * @param assigneeId the ID of the assignee
     * @return count of tasks grouped by status
     */
    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t WHERE t.assignee.id = :assigneeId GROUP BY t.status")
    List<Object[]> countTasksByStatusForUser(@Param("assigneeId") UUID assigneeId);

    /**
     * Count tasks by status for a specific team
     * 
     * @param teamId the ID of the team
     * @return count of tasks grouped by status
     */
    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t WHERE t.team.id = :teamId GROUP BY t.status")
    List<Object[]> countTasksByStatusForTeam(@Param("teamId") UUID teamId);
}