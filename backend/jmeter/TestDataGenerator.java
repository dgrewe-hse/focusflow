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
package de.hse.focusflow.jmeter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Random;

/**
 * Test data generator for JMeter load tests.
 * This class provides utility methods to generate random test data
 * for task creation and other operations in the FocusFlow API.
 */
public class TestDataGenerator {

    private static final Random RANDOM = new Random();

    private static final String[] TASK_TITLES = {
            "Complete project documentation",
            "Update customer database",
            "Fix UI bugs in dashboard",
            "Implement new search functionality",
            "Review pull requests",
            "Prepare presentation slides",
            "Design new landing page",
            "Create marketing materials",
            "Schedule team meeting",
            "Research new technologies"
    };

    private static final String[] SHORT_DESCRIPTIONS = {
            "Quick task that needs attention",
            "Important customer-facing issue",
            "Internal improvement task",
            "Documentation update needed",
            "Technical debt to address",
            "Feature enhancement request",
            "Performance optimization"
    };

    private static final String[] PRIORITIES = { "LOW", "MID", "HIGH" };
    private static final String[] STATUSES = { "OPEN", "PENDING", "IN_PROGRESS", "COMPLETED" };

    /**
     * Generate a random task title.
     *
     * @return a random task title
     */
    public static String randomTaskTitle() {
        return TASK_TITLES[RANDOM.nextInt(TASK_TITLES.length)] + " " + System.currentTimeMillis();
    }

    /**
     * Generate a random short description.
     *
     * @return a random short description
     */
    public static String randomShortDescription() {
        return SHORT_DESCRIPTIONS[RANDOM.nextInt(SHORT_DESCRIPTIONS.length)];
    }

    /**
     * Generate a random detailed description.
     *
     * @return a random detailed description
     */
    public static String randomLongDescription() {
        StringBuilder description = new StringBuilder();
        description.append("This task involves the following steps:\n");
        int steps = RANDOM.nextInt(3) + 2; // 2-4 steps

        for (int i = 1; i <= steps; i++) {
            description.append(i).append(". ");
            description.append(SHORT_DESCRIPTIONS[RANDOM.nextInt(SHORT_DESCRIPTIONS.length)]);
            description.append("\n");
        }

        description.append("\nAdditional notes: This was generated for testing purposes.");
        return description.toString();
    }

    /**
     * Generate a random priority.
     *
     * @return a random priority value
     */
    public static String randomPriority() {
        return PRIORITIES[RANDOM.nextInt(PRIORITIES.length)];
    }

    /**
     * Generate a random status.
     *
     * @return a random status value
     */
    public static String randomStatus() {
        return STATUSES[RANDOM.nextInt(STATUSES.length)];
    }

    /**
     * Generate a random UUID.
     *
     * @return a random UUID as string
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a future date.
     *
     * @param daysInFuture number of days in the future
     * @return formatted date string in ISO format
     */
    public static String futureDateString(int daysInFuture) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(daysInFuture);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return futureDate.format(formatter);
    }

    /**
     * Generate JSON for a new task.
     *
     * @param userId User ID to use for created by/assignee
     * @param teamId Team ID to assign task to
     * @return JSON string representing a task
     */
    public static String generateTaskJson(String userId, String teamId) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"title\": \"").append(randomTaskTitle()).append("\",\n");
        json.append("  \"shortDescription\": \"").append(randomShortDescription()).append("\",\n");
        json.append("  \"longDescription\": \"").append(randomLongDescription().replace("\n", "\\n")).append("\",\n");
        json.append("  \"dueDate\": \"").append(futureDateString(RANDOM.nextInt(14) + 1)).append("\",\n");
        json.append("  \"priority\": \"").append(randomPriority()).append("\",\n");
        json.append("  \"status\": \"").append(randomStatus()).append("\",\n");
        json.append("  \"createdById\": \"").append(userId).append("\",\n");
        json.append("  \"assigneeId\": \"").append(userId).append("\",\n");
        json.append("  \"teamId\": \"").append(teamId).append("\",\n");
        json.append("  \"tagIds\": []\n");
        json.append("}");
        return json.toString();
    }

    /**
     * Main method to test the data generator.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String userId = randomUUID();
        String teamId = randomUUID();

        System.out.println("=== Sample Task JSON ===");
        System.out.println(generateTaskJson(userId, teamId));

        System.out.println("\n=== Sample Data ===");
        System.out.println("Title: " + randomTaskTitle());
        System.out.println("Description: " + randomShortDescription());
        System.out.println("Priority: " + randomPriority());
        System.out.println("Status: " + randomStatus());
        System.out.println("Due Date: " + futureDateString(7));
    }
}
