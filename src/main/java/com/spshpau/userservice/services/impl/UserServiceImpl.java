package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.config.CacheConfig;
import com.spshpau.userservice.dto.profiledto.*;
import com.spshpau.userservice.dto.userdto.UserDetailDto;
import com.spshpau.userservice.dto.userdto.UserSearchCriteria;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.ArtistProfile;
import com.spshpau.userservice.model.ProducerProfile;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import com.spshpau.userservice.repositories.UserRepository;
import com.spshpau.userservice.repositories.specifications.UserSpecification;
import com.spshpau.userservice.services.UserService;
import com.spshpau.userservice.services.exceptions.UserNotFoundException;
import com.spshpau.userservice.services.wrappers.MatchedUser;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserDetailDto mapUserToDetailDto(User user) {
        if (user == null) {
            return null;
        }
        UserDetailDto dto = new UserDetailDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setLocation(user.getLocation());
        dto.setActive(user.isActive());

        if (user.getArtistProfile() != null) {
            ArtistProfile apEntity = user.getArtistProfile();
            ArtistProfileDetailDto apDto = new ArtistProfileDetailDto();
            apDto.setId(apEntity.getId());
            apDto.setAvailability(apEntity.isAvailability());
            apDto.setBio(apEntity.getBio());
            apDto.setExperienceLevel(apEntity.getExperienceLevel());
            apDto.setGenres(apEntity.getGenres().stream()
                    .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                    .collect(Collectors.toSet()));
            apDto.setSkills(apEntity.getSkills().stream()
                    .map(s -> new SkillSummaryDto(s.getId(), s.getName()))
                    .collect(Collectors.toSet()));
            dto.setArtistProfile(apDto);
        }

        if (user.getProducerProfile() != null) {
            ProducerProfile ppEntity = user.getProducerProfile();
            ProducerProfileDetailDto ppDto = new ProducerProfileDetailDto();
            ppDto.setId(ppEntity.getId());
            ppDto.setAvailability(ppEntity.isAvailability());
            ppDto.setBio(ppEntity.getBio());
            ppDto.setExperienceLevel(ppEntity.getExperienceLevel());
            ppDto.setGenres(ppEntity.getGenres().stream()
                    .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                    .collect(Collectors.toSet()));
            dto.setProducerProfile(ppDto);
        }
        return dto;
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
    @Override
    @Transactional
    public UserDetailDto syncUserFromKeycloak(UUID keycloakId, String username, String email, String firstName, String lastName) {
        User user = userRepository.findById(keycloakId)
                .map(existingUser -> {
                    existingUser.setUsername(username);
                    existingUser.setEmail(email);
                    existingUser.setFirstName(firstName);
                    existingUser.setLastName(lastName);
                    existingUser.setActive(true); // Ensure active on sync
                    log.info("Updating existing user from Keycloak: {}", keycloakId);
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(keycloakId);
                    newUser.setUsername(username);
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setActive(true);
                    log.info("Creating new user from Keycloak: {}", keycloakId);
                    return newUser;
                });
        User savedUser = userRepository.save(user);
        if (savedUser.getArtistProfile() != null) savedUser.getArtistProfile().getGenres().size();
        if (savedUser.getProducerProfile() != null) savedUser.getProducerProfile().getGenres().size();
        return mapUserToDetailDto(savedUser);
    }

    @Override
    @Transactional
    public UserDetailDto updateUserLocation(UUID userId, String location) {
        User user = findUserOrThrow(userId);
        user.setLocation(location);
        User updatedUser = userRepository.save(user);
        return mapUserToDetailDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDetailDto> getUserDetailById(UUID userId) {
        return userRepository.findById(userId).map(user -> {
            if (user.getArtistProfile() != null) {
                user.getArtistProfile().getGenres().size();
                user.getArtistProfile().getSkills().size();
            }
            if (user.getProducerProfile() != null) {
                user.getProducerProfile().getGenres().size();
            }
            return mapUserToDetailDto(user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDetailDto> getUserDetailByUsername(String username) {
        return userRepository.findByUsername(username).map(user -> {
            if (user.getArtistProfile() != null) {
                user.getArtistProfile().getGenres().size();
                user.getArtistProfile().getSkills().size();
            }
            if (user.getProducerProfile() != null) {
                user.getProducerProfile().getGenres().size();
            }
            return mapUserToDetailDto(user);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserEntityById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setActive(false);
        userRepository.save(user);
        log.info("Deactivated user with ID: {}", userId);
    }

    @Override
    @Transactional
    public void reactivateUser(UUID userId) {
        User user = findUserOrThrow(userId);
        user.setActive(true);
        userRepository.save(user);
        log.info("Reactivated user with ID: {}", userId);
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_MATCHES_CACHE, key = "#currentUserId.toString() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public Page<UserSummaryDto> findMatches(UUID currentUserId, Pageable pageable) {
        log.info("--- Executing findMatches logic for user {} ---", currentUserId);

        // 1. Get current user and EAGERLY load their profile/genres
        User currentUser = userRepository.findById(currentUserId)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException("Active user not found for ID: " + currentUserId));

        // Load current user's profiles and associated data within the transaction
        ArtistProfile currentUserArtistProfile = currentUser.getArtistProfile();
        ProducerProfile currentUserProducerProfile = currentUser.getProducerProfile();
        Set<UUID> currentUserArtistGenreIds = new HashSet<>();
        Set<UUID> currentUserProducerGenreIds = new HashSet<>();

        if (currentUserArtistProfile != null) {
            currentUserArtistProfile.getGenres().forEach(g -> currentUserArtistGenreIds.add(g.getId()));
        }
        if (currentUserProducerProfile != null) {
            currentUserProducerProfile.getGenres().forEach(g -> currentUserProducerGenreIds.add(g.getId()));
        }

        if (currentUserArtistProfile == null && currentUserProducerProfile == null) {
            log.warn("User {} has no profile, cannot find matches.", currentUserId);
            return Page.empty(pageable);
        }

        // 2. Get IDs of users to exclude
        Set<UUID> usersBlockingCurrentUser = userRepository.findBlockerUserIdsByBlockedId(currentUserId);
        Set<UUID> usersBlockedByCurrentUser = userRepository.findBlockedUserIdsByBlockerId(currentUserId);
        Set<UUID> excludedUserIds = new HashSet<>(usersBlockingCurrentUser);
        excludedUserIds.addAll(usersBlockedByCurrentUser);
        excludedUserIds.add(currentUserId);

        // 3. Build Specification for initial filtering (active, not self, not blocked)
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (!excludedUserIds.isEmpty()) {
                predicates.add(root.get("id").in(excludedUserIds).not());
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("artistProfile", JoinType.LEFT).fetch("genres", JoinType.LEFT);
                root.fetch("producerProfile", JoinType.LEFT).fetch("genres", JoinType.LEFT);
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 4. Fetch ALL potential candidates
        List<User> candidates = userRepository.findAll(spec);
        log.info("Found {} total potential candidates for user {}", candidates.size(), currentUserId);

        // 5. Calculate Match Scores & Wrap Users
        List<MatchedUser> scoredMatches = candidates.stream()
                .map(candidate -> new MatchedUser(
                        candidate,
                        calculateMatchScore(currentUser, currentUserArtistProfile, currentUserProducerProfile, candidate)
                ))
                .collect(Collectors.toList());

        // 6. Sort by Score (Descending), then by username
        scoredMatches.sort(Comparator.comparing(MatchedUser::getScore).reversed()
                .thenComparing(mu -> mu.getUser().getUsername()));

        // 7. Apply Manual Pagination
        int totalMatches = scoredMatches.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalMatches);
        List<User> paginatedUsers = (start < end) ? scoredMatches.subList(start, end).stream()
                .map(MatchedUser::getUser)
                .toList()
                : List.of();

        // 8. Map to DTO
        List<UserSummaryDto> dtoList = paginatedUsers.stream()
                .map(this::mapUserToSummaryDto)
                .collect(Collectors.toList());

        // 9. Create PageImpl
        return new PageImpl<>(dtoList, pageable, totalMatches);
    }

    private double calculateMatchScore(User currentUser, ArtistProfile callerAp, ProducerProfile callerPp, User candidate) {
        double score = 0.0;
        ArtistProfile candidateAp = candidate.getArtistProfile();
        ProducerProfile candidatePp = candidate.getProducerProfile();

        // Scenario 1: Caller is Producer, Candidate is Artist
        if (callerPp != null && candidateAp != null) {
            score += 2.0; // Opposite profile -> + 2 points
            score += calculateExperienceScore(callerPp.getExperienceLevel(), candidateAp.getExperienceLevel());
            score += calculateGenreMatchScore(callerPp.getGenres(), candidateAp.getGenres());
            if (candidateAp.isAvailability()) {
                score += 10.0; // Available -> + 10 points
            }
        }

        // Scenario 2: Caller is Artist, Candidate is Producer
        if (callerAp != null && candidatePp != null) {
            score += 2.0; // Opposite profile -> + 2 points
            score += calculateExperienceScore(callerAp.getExperienceLevel(), candidatePp.getExperienceLevel());
            score += calculateGenreMatchScore(callerAp.getGenres(), candidatePp.getGenres());
            if (candidatePp.isAvailability()) {
                score += 10.0; // Available -> + 10 points
            }
        }

        log.trace("Calculated score for candidate {}: {}", candidate.getUsername(), score);
        return score;
    }

    // Helper to calculate experience score based on difference
    private double calculateExperienceScore(ExperienceLevel level1, ExperienceLevel level2) {
        if (level1 == null || level2 == null) return 0.0;

        int diff = Math.abs(level1.ordinal() - level2.ordinal());
        return switch (diff) {
            case 0 -> 20.0; // Exact match -> + 20 points
            case 1 -> 16.0;  // 1 level difference -> + 16 points
            case 2 -> 12.0;  // 2 levels difference -> + 12 points
            case 3 -> 8.0;  // 3 levels difference -> + 8 points
            case 4 -> 4.0; // Should not happen
            default -> 0.0; // Should not happen
        };
    }

    // Helper to calculate genre match score
    private double calculateGenreMatchScore(Set<Genre> genres1, Set<Genre> genres2) {
        if (genres1 == null || genres2 == null || genres1.isEmpty() || genres2.isEmpty()) {
            return 0.0;
        }
        Set<UUID> ids1 = genres1.stream().map(Genre::getId).collect(Collectors.toSet());
        long commonCount = genres2.stream().map(Genre::getId).filter(ids1::contains).count();
        return commonCount * 5.0; // Matching genre -> + 5 points
    }
}
