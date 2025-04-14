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

import de.hse.focusflow.model.User;
import de.hse.focusflow.model.Team;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for User operations
 */
public interface UserService {

    /**
     * Register a new user
     *
     * @param email    User's email
     * @param password User's password
     * @return The created user
     * @throws IllegalArgumentException If validation fails
     */
    User registerUser(String email, String password);

    /**
     * Authenticate a user
     *
     * @param email    User's email
     * @param password User's password
     * @return The authenticated user
     * @throws RuntimeException If authentication fails
     */
    User loginUser(String email, String password);

    /**
     * Get user by ID
     *
     * @param userId ID of the user to retrieve
     * @return The user if found
     * @throws RuntimeException If user not found
     */
    User getUserById(UUID userId);

    /**
     * Get user by email
     *
     * @param email Email of the user to retrieve
     * @return The user if found
     * @throws RuntimeException If user not found
     */
    User getUserByEmail(String email);

    /**
     * Update user's password
     *
     * @param userId          ID of the user
     * @param currentPassword Current password
     * @param newPassword     New password
     * @throws RuntimeException If password update fails
     */
    void updatePassword(UUID userId, String currentPassword, String newPassword);

    /**
     * Get all teams where the user is a member
     *
     * @param userId ID of the user
     * @return List of teams
     */
    List<Team> getUserTeams(UUID userId);

    /**
     * Get user's role in a specific team
     *
     * @param userId ID of the user
     * @param teamId ID of the team
     * @return User's role in the team
     * @throws RuntimeException If user is not a member of the team
     */
    String getUserRoleInTeam(UUID userId, UUID teamId);

    /**
     * Data Transfer Object for User registration and updates
     */
    class UserDTO {
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Default constructor
        public UserDTO() {
        }

        // Getters and Setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        // Builder for fluent API
        public static class Builder {
            private final UserDTO dto = new UserDTO();

            public Builder withEmail(String email) {
                dto.email = email;
                return this;
            }

            public Builder withPassword(String password) {
                dto.password = password;
                return this;
            }

            public Builder withFirstName(String firstName) {
                dto.firstName = firstName;
                return this;
            }

            public Builder withLastName(String lastName) {
                dto.lastName = lastName;
                return this;
            }

            public UserDTO build() {
                return dto;
            }
        }
    }
}