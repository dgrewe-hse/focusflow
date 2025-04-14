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
package de.hse.focusflow.service.impl;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.repository.TaskRepository;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;

    public DashboardServiceImpl(TaskRepository taskRepository, TeamRepository teamRepository) {
        this.taskRepository = taskRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Task> getPersonalTasks(UUID userId) {
        return taskRepository.findByAssigneeId(userId);
    }

    @Override
    public List<Task> getTeamTasks(UUID userId) {
        List<Team> userTeams = teamRepository.findAllByMemberId(userId);
        return userTeams.stream()
                .flatMap(team -> taskRepository.findByTeamId(team.getId()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public double getTaskCompletionRate(UUID userId) {
        List<Task> allTasks = taskRepository.findByAssigneeId(userId);
        if (allTasks.isEmpty()) {
            return 0.0;
        }

        long completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.CLOSED)
                .count();

        return (double) completedTasks / allTasks.size() * 100;
    }

    @Override
    public double getTeamTaskCompletionRate(UUID teamId) {
        List<Task> teamTasks = taskRepository.findByTeamId(teamId);
        if (teamTasks.isEmpty()) {
            return 0.0;
        }

        long completedTasks = teamTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.CLOSED)
                .count();

        return (double) completedTasks / teamTasks.size() * 100;
    }

    @Override
    public Map<String, Object> getUserStatistics(UUID userId) {
        List<Task> userTasks = taskRepository.findByAssigneeId(userId);

        return Map.of(
                "totalTasks", userTasks.size(),
                "completedTasks", userTasks.stream().filter(task -> task.getStatus() == TaskStatus.CLOSED).count(),
                "inProgressTasks", userTasks.stream().filter(task -> task.getStatus() == TaskStatus.IN_REVIEW).count(),
                "pendingTasks", userTasks.stream().filter(task -> task.getStatus() == TaskStatus.PENDING).count(),
                "overdueTasks", userTasks.stream().filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isBefore(LocalDateTime.now()) &&
                        task.getStatus() != TaskStatus.CLOSED).count());
    }

    @Override
    public Map<String, Object> getTeamStatistics(UUID teamId) {
        List<Task> teamTasks = taskRepository.findByTeamId(teamId);

        return Map.of(
                "totalTasks", teamTasks.size(),
                "completedTasks", teamTasks.stream().filter(task -> task.getStatus() == TaskStatus.CLOSED).count(),
                "inProgressTasks", teamTasks.stream().filter(task -> task.getStatus() == TaskStatus.IN_REVIEW).count(),
                "pendingTasks", teamTasks.stream().filter(task -> task.getStatus() == TaskStatus.PENDING).count(),
                "overdueTasks", teamTasks.stream().filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isBefore(LocalDateTime.now()) &&
                        task.getStatus() != TaskStatus.CLOSED).count());
    }

    @Override
    public List<Task> getUpcomingTasks(UUID userId, int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);

        List<Task> userTasks = taskRepository.findByAssigneeId(userId);
        return userTasks.stream()
                .filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isAfter(startDate) &&
                        task.getDueDate().isBefore(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getOverdueTasks(UUID userId) {
        List<Task> userTasks = taskRepository.findByAssigneeId(userId);
        return userTasks.stream()
                .filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isBefore(LocalDateTime.now()) &&
                        task.getStatus() != TaskStatus.CLOSED)
                .collect(Collectors.toList());
    }

    @Override
    public List<Team> getUserTeams(UUID userId) {
        return teamRepository.findAllByMemberId(userId);
    }

    @Override
    public Map<String, Long> getTaskDistributionByStatus(UUID userId) {
        List<Task> userTasks = taskRepository.findByAssigneeId(userId);

        return userTasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getStatus().name(),
                        Collectors.counting()));
    }

    @Override
    public Map<String, Long> getTaskDistributionByPriority(UUID userId) {
        List<Task> userTasks = taskRepository.findByAssigneeId(userId);

        return userTasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getPriority().name(),
                        Collectors.counting()));
    }
}