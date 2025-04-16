package de.hse.focusflow.service.impl;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.model.User;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.Tag;
import de.hse.focusflow.repository.TaskRepository;
import de.hse.focusflow.repository.UserRepository;
import de.hse.focusflow.service.TaskService;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.repository.TagRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for TaskServiceImpl
 * Focusing on the createTask functionality
 */
@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID userId;
    private UUID teamId;
    private UUID tagId;
    private User testUser;
    private Team testTeam;
    private Tag testTag;
    private Set<UUID> tagIds;

    @BeforeEach
    void setUp() {
        // Initialize test data
        userId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        tagId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");

        testTeam = new Team();
        testTeam.setId(teamId);
        testTeam.setName("Test Team");

        testTag = new Tag();
        testTag.setId(tagId);
        testTag.setName("urgent");

        tagIds = new HashSet<>();
        tagIds.add(tagId);
    }

    @Test
    @DisplayName("Should create a valid task with all required fields")
    void createValidTask() {
        // Arrange
        String title = "Test Task";
        String shortDescription = "This is a test task";
        String longDescription = "This is a detailed description for the test task";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        TaskPriority priority = TaskPriority.HIGH;

        // Setup mocks for this test
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task task = taskService.createTask(
                title,
                shortDescription,
                longDescription,
                dueDate,
                priority,
                userId,
                null,
                userId,
                tagIds);

        // Assert
        assertNotNull(task);
        assertEquals(title, task.getTitle());
        assertEquals(shortDescription, task.getShortDescription());
        assertEquals(longDescription, task.getLongDescription());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(priority, task.getPriority());
        assertEquals(TaskStatus.OPEN, task.getStatus());
        assertEquals(testUser, task.getAssignee());
        assertNull(task.getTeam());
        assertEquals(testUser, task.getCreatedBy());
        assertEquals(1, task.getTags().size());
        assertTrue(task.getTags().contains(testTag));

        // Verify repository interactions
        verify(userRepository, times(2)).findById(userId); // 1 for creator, 1 for assignee
        verify(tagRepository).findById(tagId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should create a valid task using the DTO")
    void createValidTaskUsingDTO() {
        // Arrange
        TaskService.TaskDTO taskDTO = new TaskService.TaskDTO.Builder()
                .withTitle("Test Task")
                .withShortDescription("This is a test task")
                .withLongDescription("This is a detailed description")
                .withDueDate(LocalDateTime.now().plusDays(7))
                .withPriority(TaskPriority.HIGH)
                .withAssigneeId(userId)
                .withCreatedById(userId)
                .withTagIds(tagIds)
                .build();

        // Setup mocks for this test
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task task = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals(TaskStatus.OPEN, task.getStatus());
        assertEquals(testUser, task.getAssignee());

        // Verify repository interactions
        verify(userRepository, times(2)).findById(userId);
        verify(tagRepository).findById(tagId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should create a task assigned to a team")
    void createTaskWithTeamAssignment() {
        // Arrange
        String title = "Team Task";
        String shortDescription = "Task for team";

        // Setup mocks for this test
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(testTeam));
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(testTag));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task task = taskService.createTask(
                title,
                shortDescription,
                "Long description",
                LocalDateTime.now().plusDays(7),
                TaskPriority.MID,
                null, // No user assignee
                teamId, // Team assignee
                userId,
                tagIds);

        // Assert
        assertNotNull(task);
        assertEquals(title, task.getTitle());
        assertNull(task.getAssignee());
        assertEquals(testTeam, task.getTeam());

        // Verify repository interactions
        verify(userRepository).findById(userId); // Only for creator
        verify(teamRepository).findById(teamId);
        verify(tagRepository).findById(tagId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when title is null")
    void shouldThrowExceptionWhenTitleIsNull() {
        // Arrange
        String title = null;
        String shortDescription = "This is a test task";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        TaskPriority priority = TaskPriority.HIGH;

        // No need to setup mocks for validation failure

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        title,
                        shortDescription,
                        "Long description",
                        dueDate,
                        priority,
                        userId,
                        null,
                        userId,
                        tagIds));

        assertEquals("Title is required", exception.getMessage());

        // Verify no repository interactions
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("Should throw exception when title is too long")
    void shouldThrowExceptionWhenTitleIsTooLong() {
        // Arrange
        StringBuilder titleBuilder = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            titleBuilder.append("a");
        }
        String tooLongTitle = titleBuilder.toString(); // 101 characters

        // No need to setup mocks for validation failure

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        tooLongTitle,
                        "Short description",
                        "Long description",
                        LocalDateTime.now().plusDays(7),
                        TaskPriority.HIGH,
                        userId,
                        null,
                        userId,
                        tagIds));

        assertEquals("Title must be between 1 and 100 characters", exception.getMessage());

        // Verify no repository interactions
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("Should throw exception when due date is in the past")
    void shouldThrowExceptionWhenDueDateIsInPast() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        // No need to setup mocks for validation failure

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        "Test Task",
                        "Short description",
                        "Long description",
                        pastDate,
                        TaskPriority.HIGH,
                        userId,
                        null,
                        userId,
                        tagIds));

        assertEquals("Due date must be in the future", exception.getMessage());

        // Verify no repository interactions
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("Should throw exception when both assignee and team are provided")
    void shouldThrowExceptionWhenBothAssigneeAndTeamProvided() {
        // No need to setup mocks for validation failure

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        "Test Task",
                        "Short description",
                        "Long description",
                        LocalDateTime.now().plusDays(7),
                        TaskPriority.HIGH,
                        userId,
                        teamId,
                        userId,
                        tagIds));

        assertEquals("A task cannot be assigned to both a user and a team simultaneously", exception.getMessage());

        // Verify no repository interactions
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("Should throw exception when creator user not found")
    void shouldThrowExceptionWhenCreatorUserNotFound() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();

        // Setup specific mock response for this test
        when(userRepository.findById(eq(nonExistentUserId))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        "Test Task",
                        "Short description",
                        "Long description",
                        LocalDateTime.now().plusDays(7),
                        TaskPriority.HIGH,
                        userId,
                        null,
                        nonExistentUserId,
                        tagIds));

        assertEquals("Created by user not found", exception.getMessage());

        // Verify repository interactions
        verify(userRepository).findById(nonExistentUserId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("Should throw exception when assignee user not found")
    void shouldThrowExceptionWhenAssigneeUserNotFound() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();

        // Setup specific mock responses for this test
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser)); // For creator
        when(userRepository.findById(eq(nonExistentUserId))).thenReturn(Optional.empty()); // For assignee

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        "Test Task",
                        "Short description",
                        "Long description",
                        LocalDateTime.now().plusDays(7),
                        TaskPriority.HIGH,
                        nonExistentUserId,
                        null,
                        userId,
                        tagIds));

        assertEquals("Assignee not found", exception.getMessage());

        // Verify repository interactions
        verify(userRepository).findById(userId); // Creator lookup
        verify(userRepository).findById(nonExistentUserId); // Assignee lookup
        verifyNoInteractions(taskRepository); // No save should happen
    }

    @Test
    @DisplayName("Should throw exception when team not found")
    void shouldThrowExceptionWhenTeamNotFound() {
        // Arrange
        UUID nonExistentTeamId = UUID.randomUUID();

        // Setup specific mock responses for this test
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser)); // For creator
        when(teamRepository.findById(eq(nonExistentTeamId))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        "Test Task",
                        "Short description",
                        "Long description",
                        LocalDateTime.now().plusDays(7),
                        TaskPriority.HIGH,
                        null,
                        nonExistentTeamId,
                        userId,
                        tagIds));

        assertEquals("Team not found", exception.getMessage());

        // Verify repository interactions
        verify(userRepository).findById(userId);
        verify(teamRepository).findById(nonExistentTeamId);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    @DisplayName("Should throw exception when tag not found")
    void shouldThrowExceptionWhenTagNotFound() {
        // Arrange
        UUID nonExistentTagId = UUID.randomUUID();
        Set<UUID> invalidTagIds = new HashSet<>();
        invalidTagIds.add(nonExistentTagId);

        // Setup specific mock responses for this test
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
        when(teamRepository.findById(eq(teamId))).thenReturn(Optional.of(testTeam));
        when(tagRepository.findById(eq(nonExistentTagId))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(
                        "Test Task",
                        "Short description",
                        "Long description",
                        LocalDateTime.now().plusDays(7),
                        TaskPriority.HIGH,
                        null,
                        teamId,
                        userId,
                        invalidTagIds));

        assertTrue(exception.getMessage().contains("Tag with ID"));

        // Verify repository interactions
        verify(userRepository).findById(userId);
        verify(teamRepository).findById(teamId);
        verify(tagRepository).findById(nonExistentTagId);
        verifyNoInteractions(taskRepository);
    }
}