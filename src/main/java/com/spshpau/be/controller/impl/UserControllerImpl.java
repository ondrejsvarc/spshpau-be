package com.spshpau.be.controller.impl;

import com.spshpau.be.controller.UserController;
import com.spshpau.be.dto.userdto.LocationUpdateRequest;
import com.spshpau.be.model.User;
import com.spshpau.be.services.UserService;
import com.spshpau.be.services.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Autowired
    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    // Helper method to extract UUID from JWT
    private UUID getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is missing");
        }
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token missing subject claim");
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user identifier in token");
        }
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userUuid = getUserIdFromJwt(jwt);
        return userService.getUserById(userUuid)
                .map(user -> {
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    // User doesn't have profile yet
                    return syncUserWithKeycloak(jwt);
                });
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<User> getUserByUsername(@RequestParam String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PutMapping("/me/location")
    public ResponseEntity<String> updateCurrentUserLocation(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestBody LocationUpdateRequest locationUpdateRequest) {
        UUID userUuid = getUserIdFromJwt(jwt);
        String newLocation = locationUpdateRequest.getLocation();

        // Basic validation for location
        if (newLocation == null || newLocation.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userService.updateUserLocation(userUuid, newLocation);
            return ResponseEntity.ok("Successfully updatet location of user " + userUuid + " to " + newLocation + ".");
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", ex);
        }
    }

    @Override
    @PutMapping("/me/sync")
    public ResponseEntity<User> syncUserWithKeycloak(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // --- Extract details from the authenticated user (JWT token) ---
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        // Basic validation
        if (keycloakId == null || username == null || email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        UUID keycloakUuid;
        try {
            // *** Convert the String ID from JWT subject to UUID ***
            keycloakUuid = UUID.fromString(keycloakId);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format received from Keycloak token subject: " + keycloakId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            User syncedUser = userService.syncUserFromKeycloak(keycloakUuid, username, email, firstName, lastName);
            return ResponseEntity.ok(syncedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deactivating user", ex);
        }
    }

    @PutMapping("/{userId}/reactivate")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> reactivateUser(@PathVariable UUID userId) {
        try {
            userService.reactivateUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reactivating user", ex);
        }
    }
}
