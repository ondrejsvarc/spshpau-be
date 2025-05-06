package com.spshpau.userservice.controller.impl;

import com.spshpau.userservice.controller.ArtistProfileController;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.model.ArtistProfile;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.Skill;
import com.spshpau.userservice.services.ArtistProfileService;
import com.spshpau.userservice.services.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users/artist-profile")
@RequiredArgsConstructor
public class ArtistProfileControllerImpl implements ArtistProfileController {

    private final ArtistProfileService artistProfileService;

    // --- Helper Method ---
    private UUID getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is missing");
        }
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token missing subject claim");
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user identifier in token");
        }
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ArtistProfile> getMyArtistProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        return artistProfileService.getArtistProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist profile not found for current user"));
    }

    @Override
    @PutMapping("/me/create")
    public ResponseEntity<ArtistProfile> createOrUpdateMyArtistProfile(@AuthenticationPrincipal Jwt jwt,
                                                                       @RequestBody ProfileUpdateDto profileData) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ArtistProfile profile = artistProfileService.createOrUpdateArtistProfile(userId, profileData);
            return ResponseEntity.ok(profile);
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found, cannot create/update profile", ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving artist profile", ex);
        }
    }

    @Override
    @PatchMapping("/me/patch")
    public ResponseEntity<ArtistProfile> patchMyArtistProfile(@AuthenticationPrincipal Jwt jwt,
                                                              @RequestBody ProfileUpdateDto profileData) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ArtistProfile profile = artistProfileService.patchArtistProfile(userId, profileData);
            return ResponseEntity.ok(profile);
        } catch (ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error patching artist profile", ex);
        }
    }

    @Override
    @GetMapping("/me/genres")
    public ResponseEntity<Set<Genre>> getMyArtistProfileGenres(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            Set<Genre> genres = artistProfileService.getArtistProfileGenres(userId);
            return ResponseEntity.ok(genres);
        } catch (ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @Override
    @PostMapping("/me/genres/add/{genreId}")
    public ResponseEntity<ArtistProfile> addGenreToMyArtistProfile(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable UUID genreId) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ArtistProfile updatedProfile = artistProfileService.addGenreToArtistProfile(userId, genreId);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileNotFoundException | GenreNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (GenreLimitExceededException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding genre", ex);
        }
    }

    @Override
    @DeleteMapping("/me/genres/remove/{genreId}")
    public ResponseEntity<ArtistProfile> removeGenreFromMyArtistProfile(@AuthenticationPrincipal Jwt jwt,
                                                                        @PathVariable UUID genreId) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ArtistProfile updatedProfile = artistProfileService.removeGenreFromArtistProfile(userId, genreId);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileNotFoundException | GenreNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing genre", ex);
        }
    }

    @Override
    @GetMapping("/me/skills")
    public ResponseEntity<Set<Skill>> getMyArtistProfileSkills(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            Set<Skill> skills = artistProfileService.getArtistProfileSkills(userId);
            return ResponseEntity.ok(skills);
        } catch (ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @Override
    @PostMapping("/me/skills/add/{skillId}")
    public ResponseEntity<ArtistProfile> addSkillToMyArtistProfile(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable UUID skillId) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ArtistProfile updatedProfile = artistProfileService.addSkillToArtistProfile(userId, skillId);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileNotFoundException | SkillNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (SkillLimitExceededException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding skill", ex);
        }
    }

    @Override
    @DeleteMapping("/me/skills/remove/{skillId}")
    public ResponseEntity<ArtistProfile> removeSkillFromMyArtistProfile(@AuthenticationPrincipal Jwt jwt,
                                                                        @PathVariable UUID skillId) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ArtistProfile updatedProfile = artistProfileService.removeSkillFromArtistProfile(userId, skillId);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileNotFoundException | SkillNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing skill", ex);
        }
    }

    @Override
    @GetMapping("/{username}")
    public ResponseEntity<ArtistProfile> getArtistProfileByUsername(@PathVariable String username) {
        return artistProfileService.getArtistProfileByUsername(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist profile not found for username: " + username);
                });
    }

    @Override
    @GetMapping("/{username}/genres")
    public ResponseEntity<Set<Genre>> getArtistProfileGenresByUsername(@PathVariable String username) {
        try {
            Set<Genre> genres = artistProfileService.getArtistProfileGenresByUsername(username);
            return ResponseEntity.ok(genres);
        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist profile or user not found for username: " + username, ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving genres", ex);
        }
    }

    @Override
    @GetMapping("/{username}/skills")
    public ResponseEntity<Set<Skill>> getArtistProfileSkillsByUsername(@PathVariable String username) {
        try {
            Set<Skill> skills = artistProfileService.getArtistProfileSkillsByUsername(username);
            return ResponseEntity.ok(skills);
        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist profile or user not found for username: " + username, ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving skills", ex);
        }
    }
}
