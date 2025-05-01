package com.spshpau.be.services;

import com.spshpau.be.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User syncUserFromKeycloak(UUID keycloakId, String username, String email, String firstName, String lastName);
    void updateUserLocation(UUID userId, String location);
    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByUsername(String username);
}
