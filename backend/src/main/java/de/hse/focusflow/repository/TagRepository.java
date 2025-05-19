package de.hse.focusflow.repository;

import de.hse.focusflow.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Tag entity operations
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Find a tag by its name (exact match)
     *
     * @param name the tag name to search for
     * @return an Optional containing the tag if found
     */
    Optional<Tag> findByName(String name);

    /**
     * Find tags by name (partial match)
     *
     * @param name the name to search for
     * @return list of tags matching the search
     */
    @Query("SELECT t FROM Tag t WHERE t.name LIKE CONCAT('%', :name, '%')")
    List<Tag> findByNameContaining(@Param("name") String name);

    /**
     * Check if a tag with the given name exists
     *
     * @param name the tag name to check
     * @return true if a tag with the name exists
     */
    boolean existsByName(String name);

    /**
     * Find all tags used in tasks assigned to a specific user
     *
     * @param userId the ID of the user
     * @return list of tags used in the user's tasks
     */
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.tasks task WHERE task.assignee.id = :userId")
    List<Tag> findTagsUsedByUser(@Param("userId") UUID userId);

    /**
     * Find all tags used in tasks assigned to a specific team
     *
     * @param teamId the ID of the team
     * @return list of tags used in the team's tasks
     */
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.tasks task WHERE task.team.id = :teamId")
    List<Tag> findTagsUsedByTeam(@Param("teamId") UUID teamId);

    /**
     * Count the number of tasks using each tag
     *
     * @return list of tag name and count pairs
     */
    @Query("SELECT t.name, COUNT(task) FROM Tag t JOIN t.tasks task GROUP BY t.name ORDER BY COUNT(task) DESC")
    List<Object[]> countTasksByTag();
}
