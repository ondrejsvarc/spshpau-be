package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.userdto.UserDetailDto;
import com.spshpau.userservice.dto.userdto.UserSearchCriteria;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDetailDto syncUserFromKeycloak(UUID keycloakId, String username, String email, String firstName, String lastName);
    UserDetailDto updateUserLocation(UUID userId, String location);
    Optional<UserDetailDto> getUserDetailById(UUID userId);
    Optional<UserDetailDto> getUserDetailByUsername(String username);
    Optional<User> getUserEntityById(UUID userId);
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
