package com.spshpau.be.controller;

import com.spshpau.be.dto.profiledto.ProfileUpdateDto;
import com.spshpau.be.model.ArtistProfile;
import com.spshpau.be.model.Genre;
import com.spshpau.be.model.Skill;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.Set;
import java.util.UUID;

public interface ArtistProfileController {

    /** Get current user's Artist Profile */
    ResponseEntity<ArtistProfile> getMyArtistProfile(Jwt jwt);

    /** Create or fully update current user's Artist Profile */
    ResponseEntity<ArtistProfile> createOrUpdateMyArtistProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /** Partially update current user's Artist Profile */
    ResponseEntity<ArtistProfile> patchMyArtistProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /** Get genres for current user's Artist Profile */
    ResponseEntity<Set<Genre>> getMyArtistProfileGenres(Jwt jwt);

    /** Add a genre to current user's Artist Profile */
    ResponseEntity<ArtistProfile> addGenreToMyArtistProfile(Jwt jwt, @PathVariable UUID genreId);

    /** Remove a genre from current user's Artist Profile */
    ResponseEntity<ArtistProfile> removeGenreFromMyArtistProfile(Jwt jwt, @PathVariable UUID genreId);

    /** Get skills for current user's Artist Profile */
    ResponseEntity<Set<Skill>> getMyArtistProfileSkills(Jwt jwt);

    /** Add a skill to current user's Artist Profile */
    ResponseEntity<ArtistProfile> addSkillToMyArtistProfile(Jwt jwt, @PathVariable UUID skillId);

    /** Remove a skill from current user's Artist Profile */
    ResponseEntity<ArtistProfile> removeSkillFromMyArtistProfile(Jwt jwt, @PathVariable UUID skillId);

    /** Get Artist Profile by username */
    ResponseEntity<ArtistProfile> getArtistProfileByUsername(@PathVariable String username);

    /** Get genres for Artist Profile by username */
    ResponseEntity<Set<Genre>> getArtistProfileGenresByUsername(@PathVariable String username);

    /** Get skills for Artist Profile by username */
    ResponseEntity<Set<Skill>> getArtistProfileSkillsByUsername(@PathVariable String username);
}
