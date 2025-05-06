package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.UserConnection;
import com.spshpau.userservice.services.enums.InteractionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserInteractionService {

    // --- Connections ---
    UserConnection sendConnectionRequest(UUID requesterId, UUID addresseeId);
    UserConnection acceptConnectionRequest(UUID acceptorId, UUID requesterId);
    void rejectConnectionRequest(UUID rejectorId, UUID requesterId);
    void removeConnection(UUID userId1, UUID userId2);

    Page<UserSummaryDto> getConnectionsDto(UUID userId, Pageable pageable);
    List<UserSummaryDto> getAllConnectionsDto(UUID userId);
    Page<UserSummaryDto> getPendingIncomingRequestsDto(UUID userId, Pageable pageable);
    Page<UserSummaryDto> getPendingOutgoingRequestsDto(UUID userId, Pageable pageable);

    // --- Blocking ---
    void blockUser(UUID blockerId, UUID blockedId);
    void unblockUser(UUID blockerId, UUID blockedId);
    Page<User> getBlockedUsers(UUID userId, Pageable pageable);

    // --- Status Checks ---
    InteractionStatus checkInteractionStatus(UUID viewingUserId, UUID targetUserId);
    boolean isBlocked(UUID userId1, UUID userId2);

}