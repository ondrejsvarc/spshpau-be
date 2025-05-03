package com.spshpau.be.services.impl;

import com.spshpau.be.dto.profiledto.ArtistProfileSummaryDto;
import com.spshpau.be.dto.profiledto.ProducerProfileSummaryDto;
import com.spshpau.be.dto.userdto.UserSearchCriteria;
import com.spshpau.be.dto.userdto.UserSummaryDto;
import com.spshpau.be.model.ArtistProfile;
import com.spshpau.be.model.ProducerProfile;
import com.spshpau.be.model.User;
import com.spshpau.be.repositories.UserRepository;
import com.spshpau.be.repositories.specifications.UserSpecification;
import com.spshpau.be.services.UserService;
import com.spshpau.be.services.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> findActiveUsers(UUID currentUserId, UserSearchCriteria criteria, Pageable pageable) {
        UserSpecification spec = new UserSpecification(criteria, currentUserId);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserSummaryDto> dtoList = userPage.getContent().stream()
                .map(this::mapUserToSummaryDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, userPage.getTotalElements());
    }

    private UserSummaryDto mapUserToSummaryDto(User user) {
        if (user == null) return null;

        UserSummaryDto dto = new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getLocation()
        );

        // --- Populate Profile Summaries ---

        // Artist Profile
        ArtistProfile ap = user.getArtistProfile();
        if (ap != null) {
            ArtistProfileSummaryDto apDto = new ArtistProfileSummaryDto();
            apDto.setAvailability(ap.isAvailability());
            apDto.setExperienceLevel(ap.getExperienceLevel());
            dto.setArtistProfile(apDto);
        }

        // Producer Profile
        ProducerProfile pp = user.getProducerProfile();
        if (pp != null) {
            ProducerProfileSummaryDto ppDto = new ProducerProfileSummaryDto();
            ppDto.setAvailability(pp.isAvailability());
            ppDto.setExperienceLevel(pp.getExperienceLevel());
            dto.setProducerProfile(ppDto);
        }

        return dto;
    }
}
