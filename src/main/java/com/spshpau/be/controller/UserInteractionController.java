package com.spshpau.be.controller;

import com.spshpau.be.dto.userdto.UserSummaryDto;
import com.spshpau.be.model.User;
import com.spshpau.be.services.enums.InteractionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;
import java.util.UUID;

public interface UserInteractionController {

    // --- Connections ---
    ResponseEntity<?> sendRequest(@PathVariable UUID addresseeId, Jwt jwt);
    ResponseEntity<?> acceptRequest(@PathVariable UUID requesterId, Jwt jwt);
    ResponseEntity<Void> rejectRequest(@PathVariable UUID requesterId, Jwt jwt);
    ResponseEntity<Void> removeConnection(@PathVariable UUID otherUserId, Jwt jwt);
    ResponseEntity<Page<UserSummaryDto>> getMyConnections(Pageable pageable, Jwt jwt);
    ResponseEntity<List<UserSummaryDto>> getAllMyConnections(Jwt jwt);
    ResponseEntity<Page<UserSummaryDto>> getMyPendingIncoming(Pageable pageable, Jwt jwt);
    ResponseEntity<Page<UserSummaryDto>> getMyPendingOutgoing(Pageable pageable, Jwt jwt);

    // --- Blocking ---
    ResponseEntity<Void> blockUser(@PathVariable UUID blockedId, Jwt jwt);
    ResponseEntity<Void> unblockUser(@PathVariable UUID blockedId, Jwt jwt);
    ResponseEntity<Page<User>> getMyBlockedUsers(Pageable pageable, Jwt jwt);

    // --- Status ---
    ResponseEntity<InteractionStatus> getInteractionStatus(@PathVariable UUID otherUserId, Jwt jwt);

}
