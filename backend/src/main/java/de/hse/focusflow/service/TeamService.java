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

import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Team operations
 */
public interface TeamService {

    /**
     * Create a new team
     *
     * @param name        Team name
     * @param description Team description
     * @param createdById ID of the user creating the team
     * @return The created team
     * @throws IllegalArgumentException If validation fails
     */
    Team createTeam(String name, String description, UUID createdById);

    /**
     * Get team by ID
     *
     * @param teamId ID of the team to retrieve
     * @return The team if found
     * @throws RuntimeException If team not found
     */
    Team getTeamById(UUID teamId);

    /**
     * Update team information
     *
     * @param teamId      ID of the team to update
     * @param name        New team name
     * @param description New team description
     * @return The updated team
     * @throws RuntimeException If team not found
     */
    Team updateTeam(UUID teamId, String name, String description);

    /**
     * Delete a team
     *
     * @param teamId ID of the team to delete
     * @throws RuntimeException If team not found
     */
    void deleteTeam(UUID teamId);

    /**
     * Add a user to a team
     *
     * @param teamId ID of the team
     * @param userId ID of the user to add
     * @param role   Role of the user in the team
     * @throws RuntimeException If team or user not found
     */
    void addUserToTeam(UUID teamId, UUID userId, String role);

    /**
     * Remove a user from a team
     *
     * @param teamId ID of the team
     * @param userId ID of the user to remove
     * @throws RuntimeException If team or user not found
     */
    void removeUserFromTeam(UUID teamId, UUID userId);

    /**
     * Get all members of a team
     *
     * @param teamId ID of the team
     * @return List of team members
     * @throws RuntimeException If team not found
     */
    List<User> getTeamMembers(UUID teamId);

    /**
     * Get all teams created by a user
     *
     * @param userId ID of the user
     * @return List of teams
     */
    List<Team> getTeamsByCreator(UUID userId);

    /**
     * Data Transfer Object for Team creation and updates
     */
    class TeamDTO {
        private String name;
        private String description;
        private UUID createdById;

        // Default constructor
        public TeamDTO() {
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public UUID getCreatedById() {
            return createdById;
        }

        public void setCreatedById(UUID createdById) {
            this.createdById = createdById;
        }

        // Builder for fluent API
        public static class Builder {
            private final TeamDTO dto = new TeamDTO();

            public Builder withName(String name) {
                dto.name = name;
                return this;
            }

            public Builder withDescription(String description) {
                dto.description = description;
                return this;
            }

            public Builder withCreatedById(UUID createdById) {
                dto.createdById = createdById;
                return this;
            }

            public TeamDTO build() {
                return dto;
            }
        }
    }
}