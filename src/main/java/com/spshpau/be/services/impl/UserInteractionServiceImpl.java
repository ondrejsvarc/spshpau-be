package com.spshpau.be.services.impl;

import com.spshpau.be.dto.userdto.UserSummaryDto;
import com.spshpau.be.model.User;
import com.spshpau.be.model.UserConnection;
import com.spshpau.be.model.enums.ConnectionStatus;
import com.spshpau.be.repositories.UserConnectionRepository;
import com.spshpau.be.repositories.UserRepository;
import com.spshpau.be.services.UserInteractionService;
import com.spshpau.be.services.enums.InteractionStatus;
import com.spshpau.be.services.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserInteractionServiceImpl implements UserInteractionService {

    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;

    // Helper to find user or throw exception
    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    // Check if user is active
    private void checkUserActive(User user) {
        if (!user.isActive()) {
            throw new UserNotActiveException("User " + user.getUsername() + " is deactivated.");
        }
    }

    // --- Connections ---

    @Override
    @Transactional
    public UserConnection sendConnectionRequest(UUID requesterId, UUID addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new ConnectionException("Cannot connect with oneself.");
        }
        User requester = findUserOrThrow(requesterId);
        User addressee = findUserOrThrow(addresseeId);
        checkUserActive(requester);
        checkUserActive(addressee);


        // Check for blocks
        if (isBlocked(requesterId, addresseeId)) {
            throw new BlockedException("Cannot send connection request; a block exists between users.");
        }

        // Check if connection already exists or is pending
        Optional<UserConnection> existingConnection = userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId);
        if (existingConnection.isPresent()) {
            throw new ConnectionException("Connection already exists or is pending between users.");
        }

        UserConnection newConnection = new UserConnection(requester, addressee);
        return userConnectionRepository.save(newConnection);
    }

    @Override
    @Transactional
    public UserConnection acceptConnectionRequest(UUID acceptorId, UUID requesterId) {
        UserConnection connection = userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(
                        requesterId, acceptorId, ConnectionStatus.PENDING)
                .orElseThrow(() -> new ConnectionException("Pending connection request not found from user " + requesterId));

        checkUserActive(connection.getAddressee());
        checkUserActive(connection.getRequester());

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connection.setAcceptTimestamp(LocalDateTime.now());
        return userConnectionRepository.save(connection);
    }

    @Override
    @Transactional
    public void rejectConnectionRequest(UUID rejectorId, UUID requesterId) {
        UserConnection connection = userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(
                        requesterId, rejectorId, ConnectionStatus.PENDING)
                .orElseThrow(() -> new ConnectionException("Pending connection request not found from user " + requesterId));

        userConnectionRepository.delete(connection);
    }

    @Override
    @Transactional
    public void removeConnection(UUID userId1, UUID userId2) {
        UserConnection connection = userConnectionRepository.findConnectionBetweenUsers(userId1, userId2)
                .filter(conn -> conn.getStatus() == ConnectionStatus.ACCEPTED)
                .orElseThrow(() -> new ConnectionException("Accepted connection not found between users " + userId1 + " and " + userId2));

        userConnectionRepository.delete(connection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getConnectionsDto(UUID userId, Pageable pageable) {
        Page<UserConnection> connectionsPage = userConnectionRepository.findAcceptedConnectionsForUser(userId, ConnectionStatus.ACCEPTED, pageable);
        List<UserSummaryDto> dtoList = connectionsPage.getContent().stream()
                .map(conn -> conn.getRequester().getId().equals(userId) ? conn.getAddressee() : conn.getRequester())
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName()))
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, connectionsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getPendingIncomingRequestsDto(UUID userId, Pageable pageable) {
        Page<UserConnection> requestsPage = userConnectionRepository.findByAddresseeIdAndStatus(userId, ConnectionStatus.PENDING, pageable);
        List<UserSummaryDto> dtoList = requestsPage.getContent().stream()
                .map(UserConnection::getRequester)
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName()))
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, requestsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getPendingOutgoingRequestsDto(UUID userId, Pageable pageable) {
        Page<UserConnection> requestsPage = userConnectionRepository.findByRequesterIdAndStatus(userId, ConnectionStatus.PENDING, pageable);
        List<UserSummaryDto> dtoList = requestsPage.getContent().stream()
                .map(UserConnection::getAddressee)
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName()))
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, requestsPage.getTotalElements());
    }


    // --- Blocking ---

    @Override
    @Transactional
    public void blockUser(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new ConnectionException("Cannot block oneself.");
        }
        User blocker = findUserOrThrow(blockerId);
        User blocked = findUserOrThrow(blockedId);
        checkUserActive(blocker);

        userConnectionRepository.findConnectionBetweenUsers(blockerId, blockedId)
                .ifPresent(userConnectionRepository::delete);

        boolean added = blocker.getBlockedUsers().add(blocked);
        if(added) {
            userRepository.save(blocker);
        }
    }

    @Override
    @Transactional
    public void unblockUser(UUID blockerId, UUID blockedId) {
        User blocker = findUserOrThrow(blockerId);
        User blocked = findUserOrThrow(blockedId);

        boolean removed = blocker.getBlockedUsers().remove(blocked);
        if (removed) {
            userRepository.save(blocker);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getBlockedUsers(UUID userId, Pageable pageable) {
        User user = findUserOrThrow(userId);
        List<User> blockedList = user.getBlockedUsers().stream().toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), blockedList.size());
        List<User> pageContent = (start <= end) ? blockedList.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, blockedList.size());
    }

    // --- Status Checks ---

    @Override
    @Transactional(readOnly = true)
    public InteractionStatus checkInteractionStatus(UUID viewingUserId, UUID targetUserId) {
        if (viewingUserId.equals(targetUserId)) return InteractionStatus.NONE;

        User viewingUser = findUserOrThrow(viewingUserId);
        User targetUser = findUserOrThrow(targetUserId);

        boolean blockedByYou = viewingUser.getBlockedUsers().contains(targetUser);
        boolean blockedByOther = targetUser.getBlockedUsers().contains(viewingUser);

        if (blockedByYou && blockedByOther) return InteractionStatus.BLOCKED_MUTUAL;
        if (blockedByYou) return InteractionStatus.BLOCKED_BY_YOU;
        if (blockedByOther) return InteractionStatus.BLOCKED_BY_OTHER;

        Optional<UserConnection> connectionOpt = userConnectionRepository.findConnectionBetweenUsers(viewingUserId, targetUserId);

        if (connectionOpt.isPresent()) {
            UserConnection connection = connectionOpt.get();
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                return InteractionStatus.CONNECTION_ACCEPTED;
            } else if (connection.getRequester().getId().equals(viewingUserId)) {
                return InteractionStatus.PENDING_OUTGOING;
            } else {
                return InteractionStatus.PENDING_INCOMING;
            }
        }

        return InteractionStatus.NONE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(UUID userId1, UUID userId2) {
        User user1 = findUserOrThrow(userId1);
        User user2 = findUserOrThrow(userId2);
        return user1.getBlockedUsers().contains(user2) || user2.getBlockedUsers().contains(user1);
    }
}
