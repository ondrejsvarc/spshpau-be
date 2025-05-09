package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.userdto.UserDetailDto;
import com.spshpau.userservice.dto.userdto.UserSearchCriteria;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.*;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import com.spshpau.userservice.repositories.UserRepository;
import com.spshpau.userservice.repositories.specifications.UserSpecification;
import com.spshpau.userservice.services.exceptions.UserNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UUID userId;
    private UUID keycloakId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        keycloakId = UUID.randomUUID();

        sampleUser = new User();
        sampleUser.setId(userId);
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@example.com");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("User");
        sampleUser.setLocation("Test Location");
        sampleUser.setActive(true);
    }

    // --- Test for syncUserFromKeycloak ---
    @Test
    void syncUserFromKeycloak_whenUserExists_shouldUpdateAndReturnDto() {
        User existingUserInDb = new User();
        existingUserInDb.setId(keycloakId);
        existingUserInDb.setUsername("oldUsername");
        existingUserInDb.setEmail("old@example.com");
        existingUserInDb.setFirstName("OldFirst");
        existingUserInDb.setLastName("OldLast");
        existingUserInDb.setActive(true);

        when(userRepository.findById(keycloakId)).thenReturn(Optional.of(existingUserInDb));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UserDetailDto resultDto = userService.syncUserFromKeycloak(
                keycloakId,
                "newUsername",
                "new@example.com",
                "NewFirst",
                "NewLast"
        );

        assertNotNull(resultDto);
        assertEquals(keycloakId, resultDto.getId());
        assertEquals("newUsername", resultDto.getUsername());
        assertEquals("new@example.com", resultDto.getEmail());
        assertEquals("NewFirst", resultDto.getFirstName());
        assertEquals("NewLast", resultDto.getLastName());
        assertTrue(resultDto.isActive());

        verify(userRepository).findById(keycloakId);
        verify(userRepository).save(any(User.class));

        User savedUser = userCaptor.getValue();
        assertEquals(keycloakId, savedUser.getId());
        assertEquals("newUsername", savedUser.getUsername());
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("NewFirst", savedUser.getFirstName());
        assertEquals("NewLast", savedUser.getLastName());
        assertTrue(savedUser.isActive());
    }

    @Test
    void syncUserFromKeycloak_whenUserDoesNotExist_shouldCreateAndReturnDto() {
        when(userRepository.findById(keycloakId)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));


        UserDetailDto resultDto = userService.syncUserFromKeycloak(keycloakId, "newUser", "newuser@example.com", "New", "User");

        assertNotNull(resultDto);
        assertEquals(keycloakId, resultDto.getId());
        assertEquals("newUser", resultDto.getUsername());
        assertEquals("newuser@example.com", resultDto.getEmail());
        assertEquals("New", resultDto.getFirstName());
        assertEquals("User", resultDto.getLastName());
        assertTrue(resultDto.isActive());

        verify(userRepository).findById(keycloakId);
        verify(userRepository).save(any(User.class));

        User capturedUser = userCaptor.getValue();
        assertEquals(keycloakId, capturedUser.getId());
        assertEquals("newUser", capturedUser.getUsername());
    }

    @Test
    void syncUserFromKeycloak_withArtistProfile_shouldInitializeGenresAndSkills() {
        ArtistProfile ap = new ArtistProfile();
        ap.setId(keycloakId);
        ap.setGenres(new HashSet<>(Set.of(new Genre("Rock"))));
        ap.setSkills(new HashSet<>(Set.of(new Skill("Guitar"))));
        sampleUser.setArtistProfile(ap);
        ap.setUser(sampleUser);


        when(userRepository.findById(keycloakId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserDetailDto resultDto = userService.syncUserFromKeycloak(keycloakId, "username", "email", "first", "last");

        assertNotNull(resultDto.getArtistProfile());
        assertEquals(1, resultDto.getArtistProfile().getGenres().size());
        assertEquals(1, resultDto.getArtistProfile().getSkills().size());
    }


    // --- Test for updateUserLocation ---
    @Test
    void updateUserLocation_whenUserExists_shouldUpdateAndReturnDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            assertEquals("New Location", userToSave.getLocation());
            return userToSave;
        });

        UserDetailDto resultDto = userService.updateUserLocation(userId, "New Location");

        assertNotNull(resultDto);
        assertEquals(userId, resultDto.getId());
        assertEquals("New Location", resultDto.getLocation());
        verify(userRepository).findById(userId);
        verify(userRepository).save(sampleUser);
    }

    @Test
    void updateUserLocation_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUserLocation(userId, "New Location");
        });

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Test for getUserDetailById ---
    @Test
    void getUserDetailById_whenUserExists_noProfiles_shouldReturnDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        Optional<UserDetailDto> resultOpt = userService.getUserDetailById(userId);

        assertTrue(resultOpt.isPresent());
        UserDetailDto resultDto = resultOpt.get();
        assertEquals(userId, resultDto.getId());
        assertNull(resultDto.getArtistProfile());
        assertNull(resultDto.getProducerProfile());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserDetailById_whenUserExists_withArtistProfile_shouldReturnDtoWithProfile() {
        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(userId);
        artistProfile.setBio("Artist Bio");
        artistProfile.setExperienceLevel(ExperienceLevel.BEGINNER);
        artistProfile.setAvailability(true);
        Genre genre1 = new Genre("Rock"); genre1.setId(UUID.randomUUID());
        Skill skill1 = new Skill("Guitar"); skill1.setId(UUID.randomUUID());
        artistProfile.setGenres(new HashSet<>(Set.of(genre1)));
        artistProfile.setSkills(new HashSet<>(Set.of(skill1)));
        sampleUser.setArtistProfile(artistProfile);
        artistProfile.setUser(sampleUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        Optional<UserDetailDto> resultOpt = userService.getUserDetailById(userId);

        assertTrue(resultOpt.isPresent());
        UserDetailDto resultDto = resultOpt.get();
        assertNotNull(resultDto.getArtistProfile());
        assertEquals("Artist Bio", resultDto.getArtistProfile().getBio());
        assertEquals(1, resultDto.getArtistProfile().getGenres().size());
        assertEquals(1, resultDto.getArtistProfile().getSkills().size());
    }

    @Test
    void getUserDetailById_whenUserNotFound_shouldReturnEmptyOptional() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Optional<UserDetailDto> result = userService.getUserDetailById(userId);
        assertTrue(result.isEmpty());
    }

    // --- Test for getUserDetailByUsername ---
    @Test
    void getUserDetailByUsername_whenUserExists_shouldReturnDto() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(sampleUser));

        Optional<UserDetailDto> resultOpt = userService.getUserDetailByUsername(username);

        assertTrue(resultOpt.isPresent());
        assertEquals(sampleUser.getId(), resultOpt.get().getId());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getUserDetailByUsername_whenUserNotFound_shouldReturnEmptyOptional() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        Optional<UserDetailDto> result = userService.getUserDetailByUsername("unknown");
        assertTrue(result.isEmpty());
    }


    // --- Test for getUserEntityById ---
    @Test
    void getUserEntityById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        Optional<User> result = userService.getUserEntityById(userId);
        assertTrue(result.isPresent());
        assertEquals(sampleUser, result.get());
    }

    @Test
    void getUserEntityById_whenUserNotFound_shouldReturnEmpty() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserEntityById(userId);
        assertTrue(result.isEmpty());
    }

    // --- Test for deactivateUser ---
    @Test
    void deactivateUser_whenUserExists_shouldDeactivate() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.deactivateUser(userId);

        assertFalse(sampleUser.isActive());
        verify(userRepository).findById(userId);
        verify(userRepository).save(sampleUser);
    }

    @Test
    void deactivateUser_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(userId));
    }

    // --- Test for reactivateUser ---
    @Test
    void reactivateUser_whenUserExists_shouldReactivate() {
        sampleUser.setActive(false); // Pre-condition
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.reactivateUser(userId);

        assertTrue(sampleUser.isActive());
        verify(userRepository).findById(userId);
        verify(userRepository).save(sampleUser);
    }

    @Test
    void reactivateUser_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.reactivateUser(userId));
    }


    // --- Test for findActiveUsers ---
    @Test
    void findActiveUsers_shouldCallRepoAndMapToDto() {
        UUID currentUserId = UUID.randomUUID();
        UserSearchCriteria criteria = new UserSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User(); user1.setId(UUID.randomUUID()); user1.setUsername("user1");
        User user2 = new User(); user2.setId(UUID.randomUUID()); user2.setUsername("user2");
        List<User> userList = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        Page<UserSummaryDto> resultPage = userService.findActiveUsers(currentUserId, criteria, pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("user1", resultPage.getContent().get(0).getUsername());

        verify(userRepository).findAll(any(UserSpecification.class), eq(pageable));
    }

    // --- Tests for findMatches ---
    @Test
    void findMatches_whenCurrentUserNotFound_shouldThrowUserNotFoundException() {
        UUID currentUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.findMatches(currentUserId, pageable);
        });
    }

    @Test
    void findMatches_whenCurrentUserInactive_shouldThrowUserNotFoundException() {
        UUID currentUserId = UUID.randomUUID();
        sampleUser.setId(currentUserId);
        sampleUser.setActive(false);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sampleUser));

        assertThrows(UserNotFoundException.class, () -> {
            userService.findMatches(currentUserId, pageable);
        });
    }

    @Test
    void findMatches_whenCurrentUserHasNoProfiles_shouldReturnEmptyPage() {
        UUID currentUserId = UUID.randomUUID();
        sampleUser.setId(currentUserId);
        sampleUser.setArtistProfile(null);
        sampleUser.setProducerProfile(null);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sampleUser));

        Page<UserSummaryDto> result = userService.findMatches(currentUserId, pageable);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(currentUserId);
        verify(userRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void findMatches_withCurrentUserArtist_findsProducerCandidates_andScores() {
        UUID currentUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("username"));

        // Current User (Artist)
        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername("currentUserArtist");
        currentUser.setActive(true);
        ArtistProfile currentUserAp = new ArtistProfile();
        currentUserAp.setId(currentUserId);
        currentUserAp.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        Genre commonGenre = new Genre("Rock"); commonGenre.setId(UUID.randomUUID());
        currentUserAp.setGenres(Set.of(commonGenre));
        currentUser.setArtistProfile(currentUserAp);
        currentUserAp.setUser(currentUser);


        // Candidate User 1 (Producer, good match)
        User candidate1 = new User();
        candidate1.setId(UUID.randomUUID());
        candidate1.setUsername("candidateProducerGood");
        candidate1.setActive(true);
        ProducerProfile candidate1Pp = new ProducerProfile();
        candidate1Pp.setId(candidate1.getId());
        candidate1Pp.setAvailability(true);
        candidate1Pp.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        candidate1Pp.setGenres(Set.of(commonGenre));
        candidate1.setProducerProfile(candidate1Pp);
        candidate1Pp.setUser(candidate1);


        // Candidate User 2 (Producer, less match)
        User candidate2 = new User();
        candidate2.setId(UUID.randomUUID());
        candidate2.setUsername("candidateProducerBad");
        candidate2.setActive(true);
        ProducerProfile candidate2Pp = new ProducerProfile();
        candidate2Pp.setId(candidate2.getId());
        candidate2Pp.setAvailability(false);
        candidate2Pp.setExperienceLevel(ExperienceLevel.EXPERT);
        Genre otherGenre = new Genre("Pop"); otherGenre.setId(UUID.randomUUID());
        candidate2Pp.setGenres(Set.of(otherGenre));
        candidate2.setProducerProfile(candidate2Pp);
        candidate2Pp.setUser(candidate2);

        // Candidate User 3
        User candidate3 = new User();
        candidate3.setId(UUID.randomUUID());
        candidate3.setUsername("candidateArtist");
        candidate3.setActive(true);
        ArtistProfile candidate3Ap = new ArtistProfile();
        candidate3Ap.setId(candidate3.getId());
        candidate3.setArtistProfile(candidate3Ap);
        candidate3Ap.setUser(candidate3);


        List<User> allCandidates = Arrays.asList(candidate1, candidate2, candidate3);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findBlockerUserIdsByBlockedId(currentUserId)).thenReturn(Collections.emptySet());
        when(userRepository.findBlockedUserIdsByBlockerId(currentUserId)).thenReturn(Collections.emptySet());
        when(userRepository.findAll(any(Specification.class))).thenReturn(allCandidates);


        Page<UserSummaryDto> resultPage = userService.findMatches(currentUserId, pageable);

        assertNotNull(resultPage);
        assertEquals(3, resultPage.getTotalElements());
        assertFalse(resultPage.getContent().isEmpty());

        assertEquals("candidateProducerGood", resultPage.getContent().get(0).getUsername());
        if (resultPage.getContent().size() > 1) {
            assertEquals("candidateProducerBad", resultPage.getContent().get(1).getUsername());
        }


        verify(userRepository).findAll(any(Specification.class));
    }
}