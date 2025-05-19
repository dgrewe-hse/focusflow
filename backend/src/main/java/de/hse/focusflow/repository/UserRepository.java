package de.hse.focusflow.repository;

import de.hse.focusflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their email address
     *
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists
     *
     * @param email the email to check
     * @return true if a user with the email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users who are members of a specific team
     *
     * @param teamId the ID of the team
     * @return list of users who are members of the team
     */
    @Query("SELECT u FROM User u JOIN u.teams t WHERE t.id = :teamId")
    List<User> findAllByTeamId(@Param("teamId") UUID teamId);

    /**
     * Find users by name (partial match on first or last name)
     *
     * @param name the name to search for
     * @return list of users matching the search
     */
    @Query("SELECT u FROM User u WHERE u.firstName LIKE CONCAT('%', :name, '%') OR u.lastName LIKE CONCAT('%', :name, '%')")
    List<User> findByName(@Param("name") String name);
}
