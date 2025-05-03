package com.spshpau.be.controller;

import com.spshpau.be.dto.userdto.LocationUpdateRequest;
import com.spshpau.be.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
     * Note: Using RequestParam here, could also use PathVariable like /username/{username}.
     *
     * @param username The username to search for.
     * @return ResponseEntity containing the User if found (200 OK), or 404 Not Found.
     */
    ResponseEntity<User> getUserByUsername(@RequestParam String username);

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
}
