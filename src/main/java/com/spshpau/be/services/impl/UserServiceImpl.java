package com.spshpau.be.services.impl;

import com.spshpau.be.model.User;
import com.spshpau.be.repositories.UserRepository;
import com.spshpau.be.services.UserService;
import com.spshpau.be.services.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Creates or updates a user in the local database based on Keycloak info.
     * This method would typically be called after successful authentication.
     *
     * @param keycloakId      The user's ID from Keycloak (e.g., token.getSubject())
     * @param username        Username from Keycloak (e.g., token.getPreferredUsername())
     * @param email           Email from Keycloak (e.g., token.getEmail())
     * @param firstName       First name from Keycloak (e.g., token.getGivenName())
     * @param lastName        Last name from Keycloak (e.g., token.getFamilyName())
     * @return The created or updated User entity.
     */
    @Transactional
    @Override
    public User syncUserFromKeycloak(UUID keycloakId, String username, String email, String firstName, String lastName) {
        // Try to find the user by their Keycloak ID
        Optional<User> existingUserOpt = userRepository.findById(keycloakId);

        User user;
        if (existingUserOpt.isPresent()) {
            // User exists, update their details (except ID)
            user = existingUserOpt.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            System.out.println("Updating existing user: " + keycloakId);
        } else {
            // User does not exist, create a new one
            user = new User();
            user.setId(keycloakId);
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setLocation(null);
            System.out.println("Creating new user: " + keycloakId);
        }

        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUserLocation(UUID userId, String location) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setLocation(location);
        userRepository.save(user);
        System.out.println("Updated location for user " + userId + " to " + location);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void reactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setActive(true);
        userRepository.save(user);
    }
}
