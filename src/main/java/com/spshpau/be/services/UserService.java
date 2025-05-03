package com.spshpau.be.services;

import com.spshpau.be.dto.userdto.UserSearchCriteria;
import com.spshpau.be.dto.userdto.UserSummaryDto;
import com.spshpau.be.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User syncUserFromKeycloak(UUID keycloakId, String username, String email, String firstName, String lastName);
    void updateUserLocation(UUID userId, String location);
    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByUsername(String username);
    void deactivateUser(UUID userId);
    void reactivateUser(UUID userId);
    Page<UserSummaryDto> findActiveUsers(UUID currentUserId, UserSearchCriteria criteria, Pageable pageable);

    /**
     * Finds matching users based on profile type, genre overlap, and availability.
     * Excludes self, inactive users, and blocked users. Ranks results.
     *
     * @param currentUserId The UUID of the user initiating the search.
     * @param pageable Pagination information.
     * @return A paginated list of matched users as UserSummaryDto.
     */
    Page<UserSummaryDto> findMatches(UUID currentUserId, Pageable pageable);
}
