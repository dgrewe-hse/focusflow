package de.hse.focusflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA configuration for enabling auditing features
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

    /**
     * Bean to provide the current auditor (user) for JPA auditing
     * This will be used to automatically set createdBy and lastModifiedBy fields if
     * needed in the future
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            // For now, we're not setting the auditor user ID
            // This can be extended later if needed to track which user created/modified
            // entities
            return Optional.empty();
        };
    }
}
