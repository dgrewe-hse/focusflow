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
package de.hse.focusflow.service;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.Team;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for Dashboard operations
 */
public interface DashboardService {

    /**
     * Get user's personal tasks
     *
     * @param userId ID of the user
     * @return List of personal tasks
     */
    List<Task> getPersonalTasks(UUID userId);

    /**
     * Get team tasks for a user
     *
     * @param userId ID of the user
     * @return List of team tasks
     */
    List<Task> getTeamTasks(UUID userId);

    /**
     * Get task completion rate for a user
     *
     * @param userId ID of the user
     * @return Completion rate as a percentage
     */
    double getTaskCompletionRate(UUID userId);

    /**
     * Get task completion rate for a team
     *
     * @param teamId ID of the team
     * @return Completion rate as a percentage
     */
    double getTeamTaskCompletionRate(UUID teamId);

    /**
     * Get basic statistics for a user
     *
     * @param userId ID of the user
     * @return Map of statistics
     */
    Map<String, Object> getUserStatistics(UUID userId);

    /**
     * Get basic statistics for a team
     *
     * @param teamId ID of the team
     * @return Map of statistics
     */
    Map<String, Object> getTeamStatistics(UUID teamId);

    /**
     * Get upcoming tasks for a user
     *
     * @param userId ID of the user
     * @param days   Number of days to look ahead
     * @return List of upcoming tasks
     */
    List<Task> getUpcomingTasks(UUID userId, int days);

    /**
     * Get overdue tasks for a user
     *
     * @param userId ID of the user
     * @return List of overdue tasks
     */
    List<Task> getOverdueTasks(UUID userId);

    /**
     * Get teams where the user is a member
     *
     * @param userId ID of the user
     * @return List of teams
     */
    List<Team> getUserTeams(UUID userId);

    /**
     * Get task distribution by status for a user
     *
     * @param userId ID of the user
     * @return Map of status to count
     */
    Map<String, Long> getTaskDistributionByStatus(UUID userId);

    /**
     * Get task distribution by priority for a user
     *
     * @param userId ID of the user
     * @return Map of priority to count
     */
    Map<String, Long> getTaskDistributionByPriority(UUID userId);

    /**
     * Data Transfer Object for Dashboard statistics
     */
    class DashboardStatsDTO {
        private UUID userId;
        private UUID teamId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        // Default constructor
        public DashboardStatsDTO() {
        }

        // Getters and Setters
        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getTeamId() {
            return teamId;
        }

        public void setTeamId(UUID teamId) {
            this.teamId = teamId;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
        }

        // Builder for fluent API
        public static class Builder {
            private final DashboardStatsDTO dto = new DashboardStatsDTO();

            public Builder withUserId(UUID userId) {
                dto.userId = userId;
                return this;
            }

            public Builder withTeamId(UUID teamId) {
                dto.teamId = teamId;
                return this;
            }

            public Builder withStartDate(LocalDateTime startDate) {
                dto.startDate = startDate;
                return this;
            }

            public Builder withEndDate(LocalDateTime endDate) {
                dto.endDate = endDate;
                return this;
            }

            public DashboardStatsDTO build() {
                return dto;
            }
        }
    }
}