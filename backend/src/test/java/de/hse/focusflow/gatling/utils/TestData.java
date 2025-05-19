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
package de.hse.focusflow.gatling.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating test data for Gatling simulations.
 */
public class TestData {
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Arrays of test data for random selection
    private static final String[] TASK_TITLES = {
            "Implement login feature",
            "Fix navigation bug",
            "Update documentation",
            "Design new UI",
            "Refactor database code",
            "Write unit tests",
            "Review pull request",
            "Setup CI pipeline",
            "Optimize API response",
            "Implement security measures"
    };

    private static final String[] TASK_DESCRIPTIONS = {
            "Implement secure login with JWT",
            "Navigation menu is not responsive on mobile",
            "Update API documentation for new endpoints",
            "Design new dashboard UI following material design",
            "Refactor database access layer to use JPA",
            "Add unit tests for all service layer methods",
            "Review and approve pull request #123",
            "Setup CI pipeline with GitHub Actions",
            "Optimize API responses by adding caching",
            "Implement XSS protection and CSRF tokens"
    };

    private static final String[] TASK_PRIORITIES = { "LOW", "MID", "HIGH" };
    private static final String[] TASK_STATUSES = { "OPEN", "PENDING", "COMPLETED", "CANCELLED" };

    /**
     * Generates a random task JSON for create/update operations
     */
    public static String createTaskJson() {
        String title = TASK_TITLES[random.nextInt(TASK_TITLES.length)];
        String shortDescription = TASK_DESCRIPTIONS[random.nextInt(TASK_DESCRIPTIONS.length)];
        String priority = TASK_PRIORITIES[random.nextInt(TASK_PRIORITIES.length)];
        String dueDate = LocalDateTime.now().plusDays(random.nextInt(30)).format(formatter);

        return String.format(
                "{" +
                        "\"title\": \"%s\"," +
                        "\"shortDescription\": \"%s\"," +
                        "\"longDescription\": \"Detailed description for %s\"," +
                        "\"dueDate\": \"%s\"," +
                        "\"priority\": \"%s\"," +
                        "\"tagIds\": []" +
                        "}",
                title + "-" + UUID.randomUUID().toString().substring(0, 8),
                shortDescription,
                title,
                dueDate,
                priority);
    }

    /**
     * Generates a random task status update JSON
     */
    public static String updateTaskStatusJson() {
        String status = TASK_STATUSES[random.nextInt(TASK_STATUSES.length)];
        return String.format("{\"status\": \"%s\"}", status);
    }

    /**
     * Generates a random UUID to use as a task ID
     */
    public static String randomTaskId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Provides a random assignee ID for task assignment
     */
    public static String randomAssigneeId() {
        return UUID.randomUUID().toString();
    }
}
