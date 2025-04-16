package de.hse.focusflow.service.impl;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.User;
import de.hse.focusflow.repository.TaskRepository;
import de.hse.focusflow.repository.TeamRepository;
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
class DashboardServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User testUser;
    private Team testTeam;
    private Task testTask;
    private List<Task> testTasks;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testTeam = new Team();
        testTeam.setId(UUID.randomUUID());
        testTeam.setName("Test Team");
        testTeam.setDescription("Test Description");
        testTeam.setTeamLead(testUser);
        testTeam.setMembers(new HashSet<>(Collections.singletonList(testUser)));

        testTask = new Task();
        testTask.setId(UUID.randomUUID());
        testTask.setTitle("Test Task");
        testTask.setShortDescription("Test Description");
        testTask.setDueDate(LocalDateTime.now().plusDays(1));
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setStatus(TaskStatus.OPEN);
        testTask.setAssignee(testUser);
        testTask.setTeam(testTeam);

        testTasks = Collections.singletonList(testTask);
    }

    @Test
    void getPersonalTasks_WithValidUserId_ShouldReturnTasks() {
        // Arrange
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(testTasks);

        // Act
        List<Task> result = dashboardService.getPersonalTasks(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void getTeamTasks_WithValidUserId_ShouldReturnTasks() {
        // Arrange
        List<Team> userTeams = Collections.singletonList(testTeam);
        when(teamRepository.findAllByMemberId(any(UUID.class))).thenReturn(userTeams);
        when(taskRepository.findByTeamId(any(UUID.class))).thenReturn(testTasks);

        // Act
        List<Task> result = dashboardService.getTeamTasks(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask, result.get(0));
    }

    @Test
    void getTaskCompletionRate_WithNoTasks_ShouldReturnZero() {
        // Arrange
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(Collections.emptyList());

        // Act
        double result = dashboardService.getTaskCompletionRate(testUser.getId());

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getTaskCompletionRate_WithTasks_ShouldCalculateRate() {
        // Arrange
        Task completedTask = new Task();
        completedTask.setStatus(TaskStatus.CLOSED);
        List<Task> tasks = Arrays.asList(testTask, completedTask);
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(tasks);

        // Act
        double result = dashboardService.getTaskCompletionRate(testUser.getId());

        // Assert
        assertEquals(50.0, result);
    }

    @Test
    void getTeamTaskCompletionRate_WithNoTasks_ShouldReturnZero() {
        // Arrange
        when(taskRepository.findByTeamId(any(UUID.class))).thenReturn(Collections.emptyList());

        // Act
        double result = dashboardService.getTeamTaskCompletionRate(testTeam.getId());

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getUserStatistics_ShouldReturnCorrectStats() {
        // Arrange
        Task completedTask = new Task();
        completedTask.setStatus(TaskStatus.CLOSED);

        Task inProgressTask = new Task();
        inProgressTask.setStatus(TaskStatus.IN_REVIEW);

        Task pendingTask = new Task();
        pendingTask.setStatus(TaskStatus.PENDING);

        Task overdueTask = new Task();
        overdueTask.setStatus(TaskStatus.OPEN);
        overdueTask.setDueDate(LocalDateTime.now().minusDays(1));

        List<Task> tasks = Arrays.asList(testTask, completedTask, inProgressTask, pendingTask, overdueTask);
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(tasks);

        // Act
        Map<String, Object> stats = dashboardService.getUserStatistics(testUser.getId());

        // Assert
        assertEquals(5, ((Number) stats.get("totalTasks")).intValue());
        assertEquals(1, ((Number) stats.get("completedTasks")).longValue());
        assertEquals(1, ((Number) stats.get("inProgressTasks")).longValue());
        assertEquals(1, ((Number) stats.get("pendingTasks")).longValue());
        assertEquals(1, ((Number) stats.get("overdueTasks")).longValue());
    }

    @Test
    void getUpcomingTasks_ShouldReturnTasksWithinDateRange() {
        // Arrange
        Task upcomingTask = new Task();
        upcomingTask.setDueDate(LocalDateTime.now().plusDays(2));
        List<Task> tasks = Arrays.asList(testTask, upcomingTask);
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(tasks);

        // Act
        List<Task> result = dashboardService.getUpcomingTasks(testUser.getId(), 3);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getOverdueTasks_ShouldReturnOverdueTasks() {
        // Arrange
        Task overdueTask = new Task();
        overdueTask.setDueDate(LocalDateTime.now().minusDays(1));
        overdueTask.setStatus(TaskStatus.OPEN);
        List<Task> tasks = Arrays.asList(testTask, overdueTask);
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(tasks);

        // Act
        List<Task> result = dashboardService.getOverdueTasks(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(overdueTask, result.get(0));
    }

    @Test
    void getUserTeams_ShouldReturnUserTeams() {
        // Arrange
        List<Team> teams = Collections.singletonList(testTeam);
        when(teamRepository.findAllByMemberId(any(UUID.class))).thenReturn(teams);

        // Act
        List<Team> result = dashboardService.getUserTeams(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTeam, result.get(0));
    }

    @Test
    void getTaskDistributionByStatus_ShouldReturnCorrectDistribution() {
        // Arrange
        Task closedTask = new Task();
        closedTask.setStatus(TaskStatus.CLOSED);
        List<Task> tasks = Arrays.asList(testTask, closedTask);
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(tasks);

        // Act
        Map<String, Long> distribution = dashboardService.getTaskDistributionByStatus(testUser.getId());

        // Assert
        assertNotNull(distribution);
        assertEquals(1, distribution.get("OPEN"));
        assertEquals(1, distribution.get("CLOSED"));
    }

    @Test
    void getTaskDistributionByPriority_ShouldReturnCorrectDistribution() {
        // Arrange
        Task lowPriorityTask = new Task();
        lowPriorityTask.setPriority(TaskPriority.LOW);
        List<Task> tasks = Arrays.asList(testTask, lowPriorityTask);
        when(taskRepository.findByAssigneeId(any(UUID.class))).thenReturn(tasks);

        // Act
        Map<String, Long> distribution = dashboardService.getTaskDistributionByPriority(testUser.getId());

        // Assert
        assertNotNull(distribution);
        assertEquals(1, distribution.get("HIGH"));
        assertEquals(1, distribution.get("LOW"));
    }
}