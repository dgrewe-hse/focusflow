package de.hse.focusflow.service;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.model.User;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.Tag;
import de.hse.focusflow.repository.TaskRepository;
import de.hse.focusflow.repository.UserRepository;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.repository.TagRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of the TaskService interface
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TagRepository tagRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository,
            TeamRepository teamRepository, TagRepository tagRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Task createTask(String title, String shortDescription, String longDescription,
            LocalDateTime dueDate, TaskPriority priority,
            UUID assigneeId, UUID teamId, UUID createdById, Set<UUID> tagIds) {

        // Validate required fields
        validateRequiredFields(title, shortDescription, dueDate, priority, createdById);

        // Validate title length
        if (title.length() < 1 || title.length() > 100) {
            throw new IllegalArgumentException("Title must be between 1 and 100 characters");
        }

        // Validate short description length
        if (shortDescription.length() > 200) {
            throw new IllegalArgumentException("Short description must be at most 200 characters");
        }

        // Validate due date
        if (dueDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }

        // Validate assignee and team
        if (assigneeId != null && teamId != null) {
            throw new IllegalArgumentException("A task cannot be assigned to both a user and a team simultaneously");
        }

        // Fetch entities from repositories
        User assignee = null;
        Team team = null;
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new IllegalArgumentException("Created by user not found"));

        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
        }

        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        }

        Set<Tag> tags = new HashSet<>();
        if (tagIds != null && !tagIds.isEmpty()) {
            tagIds.forEach(tagId -> {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("Tag with ID " + tagId + " not found"));
                tags.add(tag);
            });
        }

        // Create new task
        Task task = new Task();
        task.setTitle(title);
        task.setShortDescription(shortDescription);
        task.setLongDescription(longDescription);
        task.setDueDate(dueDate);
        task.setPriority(priority);
        task.setStatus(TaskStatus.OPEN); // New tasks always start with OPEN status
        task.setAssignee(assignee);
        task.setTeam(team);
        task.setCreatedBy(createdBy);
        task.setTags(tags);

        // Save and return
        return taskRepository.save(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Task createTask(TaskDTO taskDTO) {
        return createTask(
                taskDTO.getTitle(),
                taskDTO.getShortDescription(),
                taskDTO.getLongDescription(),
                taskDTO.getDueDate(),
                taskDTO.getPriority(),
                taskDTO.getAssigneeId(),
                taskDTO.getTeamId(),
                taskDTO.getCreatedById(),
                taskDTO.getTagIds());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Task updateTask(UUID taskId, String title, String shortDescription, String longDescription,
            LocalDateTime dueDate, TaskPriority priority, TaskStatus status,
            UUID assigneeId, UUID teamId, Set<UUID> tagIds) {

        // Fetch existing task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));

        // Validate required fields
        if (title != null) {
            if (title.length() < 1 || title.length() > 100) {
                throw new IllegalArgumentException("Title must be between 1 and 100 characters");
            }
            task.setTitle(title);
        }

        if (shortDescription != null) {
            if (shortDescription.length() > 200) {
                throw new IllegalArgumentException("Short description must be at most 200 characters");
            }
            task.setShortDescription(shortDescription);
        }

        if (longDescription != null) {
            task.setLongDescription(longDescription);
        }

        if (dueDate != null) {
            if (dueDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Due date must be in the future");
            }
            task.setDueDate(dueDate);
        }

        if (priority != null) {
            task.setPriority(priority);
        }

        if (status != null) {
            task.setStatus(status);
        }

        // Validate assignee and team
        if (assigneeId != null && teamId != null) {
            throw new IllegalArgumentException("A task cannot be assigned to both a user and a team simultaneously");
        }

        // Update assignee if provided
        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
            task.setAssignee(assignee);
            task.setTeam(null); // Clear team if assigning to user
        } else if (assigneeId == null && teamId == null) {
            // If both are null, leave as is
        } else {
            task.setAssignee(null); // Clear assignee if assigning to team or removing both
        }

        // Update team if provided
        if (teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found"));
            task.setTeam(team);
            task.setAssignee(null); // Clear assignee if assigning to team
        }

        // Update tags if provided
        if (tagIds != null) {
            Set<Tag> tags = new HashSet<>();
            tagIds.forEach(tagId -> {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("Tag with ID " + tagId + " not found"));
                tags.add(tag);
            });
            task.setTags(tags);
        }

        // Save and return
        return taskRepository.save(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found with ID: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Task> searchTasks(String title, TaskStatus status, UUID assigneeId, TaskPriority priority) {
        return taskRepository.searchTasks(title, status, assigneeId, priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByAssignee(UUID userId) {
        return taskRepository.findByAssigneeId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByTeam(UUID teamId) {
        return taskRepository.findByTeamId(teamId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByCreator(UUID userId) {
        return taskRepository.findByCreatedById(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Task> getUpcomingTasks(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);
        return taskRepository.findUpcomingTasks(now, endDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> getTaskStatsByUser(UUID userId) {
        List<Object[]> results = taskRepository.countTasksByStatusForUser(userId);
        return convertToStatusMap(results);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> getTaskStatsByTeam(UUID teamId) {
        List<Object[]> results = taskRepository.countTasksByStatusForTeam(teamId);
        return convertToStatusMap(results);
    }

    /**
     * Validates that all required fields are present
     */
    private void validateRequiredFields(String title, String shortDescription,
            LocalDateTime dueDate, TaskPriority priority, UUID createdById) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (shortDescription == null || shortDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Short description is required");
        }

        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required");
        }

        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }

        if (createdById == null) {
            throw new IllegalArgumentException("Created by user ID is required");
        }
    }

    /**
     * Converts a list of Object[] (status, count) to a Map<TaskStatus, Long>
     */
    private Map<TaskStatus, Long> convertToStatusMap(List<Object[]> results) {
        Map<TaskStatus, Long> statsMap = new EnumMap<>(TaskStatus.class);

        // Initialize all statuses with zero count
        for (TaskStatus status : TaskStatus.values()) {
            statsMap.put(status, 0L);
        }

        // Update with actual counts
        for (Object[] result : results) {
            TaskStatus status = (TaskStatus) result[0];
            Long count = ((Number) result[1]).longValue();
            statsMap.put(status, count);
        }

        return statsMap;
    }
}