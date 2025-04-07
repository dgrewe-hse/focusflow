package de.hse.focusflow.repository;

import de.hse.focusflow.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity operations
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find a role by its name
     * 
     * @param name the role name to search for
     * @return an Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Check if a role with the given name exists
     * 
     * @param name the role name to check
     * @return true if a role with the name exists
     */
    boolean existsByName(String name);

    /**
     * Find all roles assigned to a specific user
     * 
     * @param userId the ID of the user
     * @return list of roles assigned to the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findAllByUserId(@Param("userId") UUID userId);
}