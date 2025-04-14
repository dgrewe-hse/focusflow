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

import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.User;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.repository.UserRepository;
import de.hse.focusflow.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamServiceImpl(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Team createTeam(String name, String description, UUID createdById) {
        validateTeamName(name);

        User teamLead = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (teamRepository.existsByName(name)) {
            throw new IllegalArgumentException("Team name already exists");
        }

        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        team.setTeamLead(teamLead);
        team.getMembers().add(teamLead); // Team lead is automatically a member

        return teamRepository.save(team);
    }

    @Override
    public Team getTeamById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    @Override
    public Team updateTeam(UUID teamId, String name, String description) {
        Team team = getTeamById(teamId);

        if (!team.getName().equals(name) && teamRepository.existsByName(name)) {
            throw new IllegalArgumentException("Team name already exists");
        }

        team.setName(name);
        team.setDescription(description);

        return teamRepository.save(team);
    }

    @Override
    public void deleteTeam(UUID teamId) {
        Team team = getTeamById(teamId);
        teamRepository.delete(team);
    }

    @Override
    public void addUserToTeam(UUID teamId, UUID userId, String role) {
        Team team = getTeamById(teamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (team.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is already a member of this team");
        }

        team.getMembers().add(user);
        teamRepository.save(team);
    }

    @Override
    public void removeUserFromTeam(UUID teamId, UUID userId) {
        Team team = getTeamById(teamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (team.getTeamLead().equals(user)) {
            throw new IllegalArgumentException("Cannot remove team lead from team");
        }

        if (!team.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of this team");
        }

        team.getMembers().remove(user);
        teamRepository.save(team);
    }

    @Override
    public List<User> getTeamMembers(UUID teamId) {
        Team team = getTeamById(teamId);
        return List.copyOf(team.getMembers());
    }

    @Override
    public List<Team> getTeamsByCreator(UUID userId) {
        return teamRepository.findAllByTeamLeadId(userId);
    }

    private void validateTeamName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Team name must be between 2 and 50 characters");
        }
    }
}