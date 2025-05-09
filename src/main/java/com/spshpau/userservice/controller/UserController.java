package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.userdto.LocationUpdateRequest;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;


public interface UserController {

    /**
     * Retrieves the details of the currently authenticated user.
     *
     * @param jwt The JWT token representing the authenticated principal.
     * @return ResponseEntity containing the User if found (200 OK), or 401/404 if auth fails or user not synced.
     */
    ResponseEntity<User> getCurrentUser(Jwt jwt);

    /**
     * Retrieves a user by their username.
     *
     * @param username The username to search for.
     * @return ResponseEntity containing the User if found (200 OK), or 404 Not Found.
     */
    ResponseEntity<User> getUserByUsername(String username);

    /**
     * Retrieves a user by their id.
     *
     * @param userId The id to search for.
     * @return ResponseEntity containing the UserSummaryDto if found (200 OK), or 404 Not Found.
     */
    ResponseEntity<UserSummaryDto> getUserById(UUID userId);

    /**
     * Updates the location for the currently authenticated user.
     *
     * @param jwt                 The JWT token representing the authenticated principal.
     * @param locationUpdateRequest DTO containing the new location.
     * @return ResponseEntity containing the updated User (200 OK), or error status.
     */
    ResponseEntity<String> updateCurrentUserLocation(Jwt jwt, @RequestBody LocationUpdateRequest locationUpdateRequest);

    /**
     * Synchronizes the currently authenticated user's data from Keycloak
     * into the local database. Assumes user is authenticated via Spring Security/Keycloak.
     * The implementation will need to extract user details from the security context.
     *
     * @param jwt Represents the currently authenticated user (e.g., from Spring Security).
     * @return ResponseEntity containing the synchronized User (200 OK) or an error status.
     */
    ResponseEntity<User> syncUserWithKeycloak(Jwt jwt);

    ResponseEntity<Void> deactivateUser(@PathVariable UUID userId);
    ResponseEntity<Void> reactivateUser(@PathVariable UUID userId);

    /**
     * Searches/filters active users based on provided criteria, excluding the caller.
     *
     * @param jwt         The JWT token representing the authenticated principal.
     * @param searchTerm  Optional String to try to match with username, firstName and lastName.
     * @param genreIds    Optional list of Genre UUIDs to filter by.
     * @param skillIds    Optional list of Skill UUIDs to filter by.
     * @param hasArtist   Optional boolean to filter by artist profile existence.
     * @param hasProducer Optional boolean to filter by producer profile existence.
     * @param pageable    Pagination information.
     * @return A paginated list of UserSummaryDto matching the criteria.
     */
    ResponseEntity<Page<UserSummaryDto>> searchUsers(
            Jwt jwt,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) List<UUID> genreIds,
            @RequestParam(required = false) List<UUID> skillIds,
            @RequestParam(required = false) Boolean hasArtist,
            @RequestParam(required = false) Boolean hasProducer,
            @RequestParam(required = false) ExperienceLevel artistExperienceLevel,
            @RequestParam(required = false) Boolean artistAvailability,
            @RequestParam(required = false) ExperienceLevel producerExperienceLevel,
            @RequestParam(required = false) Boolean producerAvailability,
            Pageable pageable
    );

    /**
     * Finds potential collaborators for the current user based on matching genres and availability.
     * @param jwt The JWT token representing the authenticated principal.
     * @param pageable Pagination information.
     * @return A paginated list of matched users (UserSummaryDto), ranked by relevance.
     */
    ResponseEntity<Page<UserSummaryDto>> findMatches(Jwt jwt, Pageable pageable);
}
