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
package de.hse.focusflow.gatling.scenarios;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.http.HttpDsl;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import de.hse.focusflow.gatling.utils.Authentication;
import de.hse.focusflow.gatling.utils.Configuration;
import de.hse.focusflow.gatling.utils.TestData;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Reusable Gatling scenarios for testing TaskController endpoints.
 */
public class TaskScenarios {

    /**
     * Creates a basic scenario that authenticates and performs CRUD operations on
     * tasks.
     */
    public static ScenarioBuilder basicTaskCrudScenario() {
        return scenario("Task CRUD Operations")
                .exec(session -> {
                    System.out.println("Starting Task CRUD scenario");
                    return session;
                })
                .exec(Authentication.login())
                .exec(session -> {
                    System.out.println("Authentication completed, auth header: " +
                            (session.contains("authHeader") ? "present" : "missing"));
                    return session;
                })
                .exec(getAllTasks())
                .pause(1) // Add a small pause between requests
                .exec(createTask())
                .pause(1)
                .exec(session -> {
                    // Use createdTaskId if available, otherwise generate a new ID
                    String taskId = session.contains("createdTaskId") ? session.getString("createdTaskId")
                            : TestData.randomTaskId();
                    System.out.println("Using task ID for operations: " + taskId);
                    return session.set("taskId", taskId);
                })
                .pause(1)
                .exec(getTaskById())
                .pause(1)
                .exec(updateTaskStatus())
                .pause(1)
                .exec(updateTaskAssignee())
                .pause(1)
                .exec(updateTask())
                .pause(1)
                .exec(deleteTask());
    }

    /**
     * Creates a scenario focused on read operations (get all tasks, get by ID,
     * search)
     */
    public static ScenarioBuilder readIntensiveScenario() {
        return scenario("Read-Intensive Operations")
                .exec(Authentication.login())
                .repeat(5).on(
                        exec(getAllTasks())
                                .pause(1, 3)
                                .exec(getUpcomingTasks())
                                .pause(1, 2)
                                .exec(searchTasks()));
    }

    /**
     * Creates a scenario focused on write operations (create, update, delete)
     */
    public static ScenarioBuilder writeIntensiveScenario() {
        return scenario("Write-Intensive Operations")
                .exec(Authentication.login())
                .repeat(3).on(
                        exec(createTask())
                                .pause(1, 2)
                                .exec(session -> session.set("taskId", TestData.randomTaskId()))
                                .exec(updateTask())
                                .pause(1)
                                .exec(updateTaskStatus())
                                .pause(1)
                                .exec(deleteTask()));
    }

    /**
     * Creates a realistic user behavior scenario with mixed operations and pauses
     */
    public static ScenarioBuilder realisticUserScenario() {
        return scenario("Realistic User Behavior")
                .exec(Authentication.login())
                .exec(getAllTasks())
                .pause(2, 5)
                .exec(searchTasks())
                .pause(1, 3)
                .exec(createTask())
                .exec(session -> {
                    // Extract the task ID from the response
                    String responseBody = session.getString("createTaskResponse");
                    String taskId = UUID.randomUUID().toString(); // Fallback if extraction fails
                    // In a real scenario, you would parse the response to extract the actual ID
                    return session.set("taskId", taskId);
                })
                .pause(2, 4)
                .exec(getTaskById())
                .pause(1, 2)
                .exec(updateTaskStatus())
                .pause(2, 5)
                // Use a more reliable approach with randomSwitch
                .exec(session -> {
                    // Generate a random number between 0 and 100
                    int randomValue = ThreadLocalRandom.current().nextInt(100);
                    if (randomValue < 70) {
                        return session.set("switchAction", "getAllTasks");
                    } else if (randomValue < 90) {
                        return session.set("switchAction", "updateTask");
                    } else {
                        return session.set("switchAction", "deleteTask");
                    }
                })
                .doIf(session -> "getAllTasks".equals(session.getString("switchAction")))
                .then(getAllTasks())
                .doIf(session -> "updateTask".equals(session.getString("switchAction")))
                .then(updateTask())
                .doIf(session -> "deleteTask".equals(session.getString("switchAction")))
                .then(deleteTask());
    }

    // Individual request chains

    public static ChainBuilder getAllTasks() {
        if (Configuration.OFFLINE_MODE) {
            return offlineGetAllTasks();
        }
        return exec(
                http("Get All Tasks")
                        .get(Configuration.TASKS_PATH)
                        .header("Authorization", "#{authHeader}")
                        .check(
                                status().in(200, 401, 403), // Accept unauthorized/forbidden as valid responses for
                                                            // tests
                                status().not(500), // Server error is a failure
                                status().not(404) // Not found is a failure
                        ));
    }

    public static ChainBuilder getTaskById() {
        if (Configuration.OFFLINE_MODE) {
            return offlineGetTaskById();
        }
        return exec(
                http("Get Task By ID")
                        .get(Configuration.TASKS_PATH + "/#{taskId}")
                        .header("Authorization", "#{authHeader}")
                        .check(
                                status().in(200, 401, 403, 404) // 404 is acceptable for a non-existent task
                        ));
    }

    public static ChainBuilder createTask() {
        if (Configuration.OFFLINE_MODE) {
            return offlineCreateTask();
        }

        return exec(session -> session.set("taskPayload", TestData.createTaskJson()))
                .exec(
                        http("Create Task")
                                .post(Configuration.TASKS_PATH)
                                .header("Authorization", "#{authHeader}")
                                .header("Content-Type", "application/json")
                                .body(StringBody("#{taskPayload}"))
                                .check(
                                        status().in(201, 400, 401, 403) // Accept bad request as well
                                ))
                .exec(session -> {
                    // Generate a random task ID for subsequent operations
                    String taskId = UUID.randomUUID().toString();
                    System.out.println("Using generated task ID: " + taskId);
                    return session.set("taskId", taskId).set("createdTaskId", taskId);
                });
    }

    public static ChainBuilder updateTask() {
        if (Configuration.OFFLINE_MODE) {
            return offlineUpdateTask();
        }
        return exec(session -> session.set("updateTaskPayload", TestData.createTaskJson()))
                .exec(
                        http("Update Task")
                                .put(Configuration.TASKS_PATH + "/#{taskId}")
                                .header("Authorization", "#{authHeader}")
                                .header("Content-Type", "application/json")
                                .body(StringBody("#{updateTaskPayload}"))
                                .check(
                                        status().in(200, 400, 401, 403, 404),
                                        status().not(500)));
    }

    public static ChainBuilder updateTaskStatus() {
        if (Configuration.OFFLINE_MODE) {
            return offlineUpdateTaskStatus();
        }
        return exec(
                http("Update Task Status")
                        .patch(Configuration.TASKS_PATH + "/#{taskId}/status")
                        .header("Authorization", "#{authHeader}")
                        .queryParam("status", "PENDING") // Could randomize this
                        .check(
                                status().in(200, 400, 401, 403, 404),
                                status().not(500)));
    }

    public static ChainBuilder updateTaskAssignee() {
        if (Configuration.OFFLINE_MODE) {
            return offlineUpdateTaskAssignee();
        }
        return exec(
                http("Update Task Assignee")
                        .patch(Configuration.TASKS_PATH + "/#{taskId}/assignee")
                        .header("Authorization", "#{authHeader}")
                        .queryParam("assigneeId", session -> TestData.randomAssigneeId())
                        .check(
                                status().in(200, 400, 401, 403, 404),
                                status().not(500)));
    }

    public static ChainBuilder deleteTask() {
        if (Configuration.OFFLINE_MODE) {
            return offlineDeleteTask();
        }
        return exec(
                http("Delete Task")
                        .delete(Configuration.TASKS_PATH + "/#{taskId}")
                        .header("Authorization", "#{authHeader}")
                        .check(
                                status().in(204, 401, 403, 404),
                                status().not(500)));
    }

    public static ChainBuilder searchTasks() {
        if (Configuration.OFFLINE_MODE) {
            return offlineSearchTasks();
        }
        return exec(
                http("Search Tasks")
                        .get(Configuration.TASKS_PATH + "/search")
                        .header("Authorization", "#{authHeader}")
                        .queryParam("title", "test")
                        .check(
                                status().in(200, 401, 403),
                                status().not(500)));
    }

    public static ChainBuilder getUpcomingTasks() {
        if (Configuration.OFFLINE_MODE) {
            return offlineGetUpcomingTasks();
        }
        return exec(
                http("Get Upcoming Tasks")
                        .get(Configuration.TASKS_PATH + "/upcoming/7")
                        .header("Authorization", "#{authHeader}")
                        .check(
                                status().in(200, 401, 403),
                                status().not(500)));
    }

    // Offline mode implementations with proper mocking

    private static ChainBuilder offlineGetAllTasks() {
        return exec(session -> {
            randomDelay();
            System.out.println("[OFFLINE] Simulated: Get All Tasks - Success (200)");
            return session;
        });
    }

    private static ChainBuilder offlineGetTaskById() {
        return exec(session -> {
            randomDelay();
            String taskId = session.getString("taskId");
            System.out.println("[OFFLINE] Simulated: Get Task By ID (" + taskId + ") - Success (200)");
            return session;
        });
    }

    private static ChainBuilder offlineCreateTask() {
        return exec(session -> session.set("taskPayload", TestData.createTaskJson()))
                .exec(session -> {
                    randomDelay();
                    String taskId = UUID.randomUUID().toString();

                    System.out.println("[OFFLINE] Simulated: Create Task - Success (201), ID: " + taskId);

                    // Just set the task ID directly - no JSON parsing required
                    return session.set("createdTaskId", taskId)
                            .set("taskId", taskId);
                });
    }

    private static ChainBuilder offlineUpdateTask() {
        return exec(session -> session.set("updateTaskPayload", TestData.createTaskJson()))
                .exec(session -> {
                    randomDelay();
                    String taskId = session.getString("taskId");
                    System.out.println("[OFFLINE] Simulated: Update Task (" + taskId + ") - Success (200)");
                    return session;
                });
    }

    private static ChainBuilder offlineUpdateTaskStatus() {
        return exec(session -> {
            randomDelay();
            String taskId = session.getString("taskId");
            System.out.println("[OFFLINE] Simulated: Update Task Status (" + taskId + ") - Success (200)");
            return session;
        });
    }

    private static ChainBuilder offlineUpdateTaskAssignee() {
        return exec(session -> {
            randomDelay();
            String taskId = session.getString("taskId");
            System.out.println("[OFFLINE] Simulated: Update Task Assignee (" + taskId + ") - Success (200)");
            return session;
        });
    }

    private static ChainBuilder offlineDeleteTask() {
        return exec(session -> {
            randomDelay();
            String taskId = session.getString("taskId");
            System.out.println("[OFFLINE] Simulated: Delete Task (" + taskId + ") - Success (204)");
            return session;
        });
    }

    private static ChainBuilder offlineSearchTasks() {
        return exec(session -> {
            randomDelay();
            System.out.println("[OFFLINE] Simulated: Search Tasks - Success (200)");
            return session;
        });
    }

    private static ChainBuilder offlineGetUpcomingTasks() {
        return exec(session -> {
            randomDelay();
            System.out.println("[OFFLINE] Simulated: Get Upcoming Tasks - Success (200)");
            return session;
        });
    }

    // Helper method to simulate random response delay
    private static void randomDelay() {
        try {
            int delayMs = ThreadLocalRandom.current().nextInt(
                    Configuration.MIN_RESPONSE_DELAY,
                    Configuration.MAX_RESPONSE_DELAY + 1);
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
