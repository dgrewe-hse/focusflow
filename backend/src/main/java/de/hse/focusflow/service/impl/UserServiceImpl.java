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

import de.hse.focusflow.model.User;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.repository.UserRepository;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    // Password validation pattern
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    public UserServiceImpl(
            UserRepository userRepository,
            TeamRepository teamRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String email, String password) {
        validateEmail(email);
        validatePassword(password);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(""); // Default empty first name
        user.setLastName(""); // Default empty last name

        return userRepository.save(user);
    }

    @Override
    public User createUser(String email, String password, String firstName, String lastName) {
        validateEmail(email);
        validatePassword(password);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return userRepository.save(user);
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void updatePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<Team> getUserTeams(UUID userId) {
        return teamRepository.findAllByMemberId(userId);
    }

    @Override
    public String getUserRoleInTeam(UUID userId, UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Check if user is team lead
        if (team.getTeamLead().getId().equals(userId)) {
            return "TEAM_LEAD";
        }

        // Check if user is a member
        if (team.getMembers().stream().anyMatch(member -> member.getId().equals(userId))) {
            return "MEMBER";
        }

        throw new RuntimeException("User is not a member of this team");
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findUsersByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public List<User> findUsersByTeamId(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return new ArrayList<>(team.getMembers());
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters long and contain at least one digit, " +
                            "one lowercase letter, one uppercase letter, and one special character");
        }
    }
}
