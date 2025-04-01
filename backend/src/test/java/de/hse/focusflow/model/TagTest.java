package de.hse.focusflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    private Tag tag;
    private Task task;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        task = new Task();
    }

    @Test
    void testTagCreation() {
        tag.setName("bug");

        assertEquals("bug", tag.getName());
    }

    @Test
    void testTagTasksRelationship() {
        Set<Task> tasks = new HashSet<>();
        tasks.add(task);
        tag.setTasks(tasks);

        assertEquals(1, tag.getTasks().size());
        assertTrue(tag.getTasks().contains(task));
    }
}