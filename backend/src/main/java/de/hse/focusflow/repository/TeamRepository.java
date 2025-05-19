package de.hse.focusflow.repository;

import de.hse.focusflow.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Team entity operations
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    /**
     * Find a team by its name
     *
     * @param name the team name to search for
     * @return an Optional containing the team if found
     */
    Optional<Team> findByName(String name);

    /**
     * Check if a team with the given name exists
     *
     * @param name the team name to check
     * @return true if a team with the name exists
     */
    boolean existsByName(String name);

    /**
     * Find all teams led by a specific user
     *
     * @param teamLeadId the ID of the team lead
     * @return list of teams led by the user
     */
    List<Team> findAllByTeamLeadId(UUID teamLeadId);

    /**
     * Find all teams that a user is a member of
     *
     * @param userId the ID of the user
     * @return list of teams the user is a member of
     */
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    List<Team> findAllByMemberId(@Param("userId") UUID userId);

    /**
     * Find teams by name (partial match)
     *
     * @param name the name to search for
     * @return list of teams matching the search
     */
    @Query("SELECT t FROM Team t WHERE t.name LIKE CONCAT('%', :name, '%')")
    List<Team> findByNameContaining(@Param("name") String name);
}
