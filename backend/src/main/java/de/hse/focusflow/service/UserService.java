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
import de.hse.focusflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Create a new user with full details
     *
     * @param email     User's email
     * @param password  User's password
     * @param firstName User's first name
     * @param lastName  User's last name
     * @return The created user
     * @throws IllegalArgumentException If validation fails
     */
    User createUser(String email, String password, String firstName, String lastName);

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
     * Get all users
     *
     * @return List of all users
     */
    List<User> getAllUsers();

    /**
     * Find users by name
     *
     * @param name Name to search for
     * @return List of users matching the name
     */
    List<User> findUsersByName(String name);

    /**
     * Find users by team ID
     *
     * @param teamId ID of the team
     * @return List of users in the team
     */
    List<User> findUsersByTeamId(UUID teamId);
}
