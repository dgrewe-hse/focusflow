package de.hse.focusflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private Task task;
    private User assignee;
    private User creator;
    private Team team;
    private Tag tag;

    @BeforeEach
    void setUp() {
        task = new Task();
        assignee = new User();
        creator = new User();
        team = new Team();
        tag = new Tag();
    }

    @Test
    void testTaskCreation() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);

        task.setTitle("Implement Feature");
        task.setShortDescription("Implement new feature X");
        task.setLongDescription("Detailed description of feature X implementation");
        task.setDueDate(dueDate);
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.OPEN);
        task.setAssignee(assignee);
        task.setTeam(team);
        task.setCreatedBy(creator);

        assertEquals("Implement Feature", task.getTitle());
        assertEquals("Implement new feature X", task.getShortDescription());
        assertEquals("Detailed description of feature X implementation", task.getLongDescription());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(TaskStatus.OPEN, task.getStatus());
        assertEquals(assignee, task.getAssignee());
        assertEquals(team, task.getTeam());
        assertEquals(creator, task.getCreatedBy());
    }

    @Test
    void testTaskTagsRelationship() {
        Set<Tag> tags = new HashSet<>();
        tags.add(tag);
        task.setTags(tags);

        assertEquals(1, task.getTags().size());
        assertTrue(task.getTags().contains(tag));
    }

    @Test
    void testTaskStatusDefaultValue() {
        assertEquals(TaskStatus.OPEN, task.getStatus());
    }
}