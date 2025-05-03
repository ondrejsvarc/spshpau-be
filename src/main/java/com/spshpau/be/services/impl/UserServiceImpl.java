package com.spshpau.be.services.impl;

import com.spshpau.be.config.CacheConfig;
import com.spshpau.be.dto.profiledto.ArtistProfileSummaryDto;
import com.spshpau.be.dto.profiledto.ProducerProfileSummaryDto;
import com.spshpau.be.dto.userdto.UserSearchCriteria;
import com.spshpau.be.dto.userdto.UserSummaryDto;
import com.spshpau.be.model.ArtistProfile;
import com.spshpau.be.model.ProducerProfile;
import com.spshpau.be.model.User;
import com.spshpau.be.model.Skill;
import com.spshpau.be.model.Genre;
import com.spshpau.be.repositories.UserRepository;
import com.spshpau.be.repositories.specifications.UserSpecification;
import com.spshpau.be.services.UserService;
import com.spshpau.be.services.exceptions.UserNotFoundException;
import com.spshpau.be.services.wrappers.MatchedUser;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.USER_MATCHES_CACHE, key = "#currentUserId.toString() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()")
    public Page<UserSummaryDto> findMatches(UUID currentUserId, Pageable pageable) {
        // 1. Get current user and EAGERLY load their profile/genres/skills needed for matching
        User currentUser = userRepository.findById(currentUserId)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException("Active user not found for ID: " + currentUserId));

        // Directly access profiles and initialize collections needed for scoring
        Set<UUID> currentUserGenreIds = new HashSet<>();
        boolean callerIsArtist = false;
        boolean callerIsProducer = false;

        if (currentUser.getArtistProfile() != null) {
            currentUser.getArtistProfile().getGenres().forEach(g -> currentUserGenreIds.add(g.getId()));
            currentUser.getArtistProfile().getSkills().size();
            callerIsArtist = true;
        }
        if (currentUser.getProducerProfile() != null) {
            currentUser.getProducerProfile().getGenres().forEach(g -> currentUserGenreIds.add(g.getId()));
            callerIsProducer = true;
        }

        if (!callerIsArtist && !callerIsProducer) {
            return Page.empty(pageable);
        }

        final boolean findArtists = callerIsProducer;
        final boolean findProducers = callerIsArtist;


        // 2. Get IDs of users to exclude
        Set<UUID> usersBlockingCurrentUser = userRepository.findBlockerUserIdsByBlockedId(currentUserId);
        Set<UUID> usersBlockedByCurrentUser = userRepository.findBlockedUserIdsByBlockerId(currentUserId);
        Set<UUID> excludedUserIds = new HashSet<>(usersBlockingCurrentUser);
        excludedUserIds.addAll(usersBlockedByCurrentUser);
        excludedUserIds.add(currentUserId);


        // 3. Build Specification
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (!excludedUserIds.isEmpty()) {
                predicates.add(root.get("id").in(excludedUserIds).not());
            }

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("artistProfile", JoinType.LEFT).fetch("genres", JoinType.LEFT);
                root.fetch("artistProfile", JoinType.LEFT).fetch("skills", JoinType.LEFT);
                root.fetch("producerProfile", JoinType.LEFT).fetch("genres", JoinType.LEFT);
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 4. Fetch ALL potential candidates (active, not self, not blocked)
        List<User> candidates = userRepository.findAll(spec);


        // 5. Calculate Match Scores & Wrap Users
        List<MatchedUser> scoredMatches = new ArrayList<>();
        for (User candidate : candidates) {
            // Determine if the candidate has the required profile type(s) *before* scoring
            boolean hasTargetProfile = (findArtists && candidate.getArtistProfile() != null) ||
                    (findProducers && candidate.getProducerProfile() != null);

            if (!hasTargetProfile) {
                // Candidate doesn't have the needed profile type, add with score 0 for later inclusion
                scoredMatches.add(new MatchedUser(candidate, 0.0));
                continue;
            }

            // Calculate score only if profile type matches target
            double score = calculateMatchScore(currentUser, currentUserGenreIds, candidate, findArtists, findProducers);
            scoredMatches.add(new MatchedUser(candidate, score));
        }

        // 6. Sort by Score (Descending), then by username for stability
        scoredMatches.sort(Comparator.comparing(MatchedUser::getScore).reversed()
                .thenComparing(mu -> mu.getUser().getUsername()));

        // 7. Apply Manual Pagination
        int totalMatchesIncludingZeros = scoredMatches.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalMatchesIncludingZeros);
        List<User> paginatedUsers = (start <= end) ? scoredMatches.subList(start, end).stream()
                .map(MatchedUser::getUser)
                .toList()
                : List.of();

        // 8. Map to DTO
        List<UserSummaryDto> dtoList = paginatedUsers.stream()
                .map(this::mapUserToSummaryDto)
                .collect(Collectors.toList());

        // 9. Create PageImpl
        return new PageImpl<>(dtoList, pageable, totalMatchesIncludingZeros);
    }

    // Helper method to calculate score
    private double calculateMatchScore(User currentUser, Set<UUID> currentUserGenreIds, User candidate, boolean findArtists, boolean findProducers) {
        double score = 0.0;
        double genreMatchScore = 0;
        double availabilityScore = 0.0;
        double profileTypeScore = 1.0; // Base score for having the right profile type

        Set<UUID> candidateGenreIds = new HashSet<>();
        boolean targetAvailability = false;

        // Check candidate Artist Profile
        if (findArtists && candidate.getArtistProfile() != null) {
            ArtistProfile ap = candidate.getArtistProfile();
            ap.getGenres().forEach(g -> candidateGenreIds.add(g.getId()));
            targetAvailability = ap.isAvailability();
        }
        // Check candidate Producer Profile
        if (findProducers && candidate.getProducerProfile() != null) {
            ProducerProfile pp = candidate.getProducerProfile();
            pp.getGenres().forEach(g -> candidateGenreIds.add(g.getId()));
            if (!targetAvailability) {
                targetAvailability = pp.isAvailability();
            }
        }

        // Calculate genre score
        if (!currentUserGenreIds.isEmpty() && !candidateGenreIds.isEmpty()) {
            Set<UUID> intersection = new HashSet<>(currentUserGenreIds);
            intersection.retainAll(candidateGenreIds);
            genreMatchScore = intersection.size() * 10.0; // Weight factor for genres
        }

        // Calculate availability score boost
        if (targetAvailability) {
            availabilityScore = 50.0; // Weight factor for availability
        }

        // Combine Scores
        score = profileTypeScore + genreMatchScore + availabilityScore;

        return score;
    }
}
