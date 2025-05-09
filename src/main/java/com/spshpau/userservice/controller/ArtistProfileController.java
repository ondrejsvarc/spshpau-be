package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.ArtistProfile;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.Skill;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.Set;
import java.util.UUID;

public interface ArtistProfileController {

    /** Get current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> getMyArtistProfile(Jwt jwt);

    /** Create or fully update current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> createOrUpdateMyArtistProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /** Partially update current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> patchMyArtistProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /** Get genres for current user's Artist Profile */
    ResponseEntity<Set<GenreSummaryDto>> getMyArtistProfileGenres(Jwt jwt);

    /** Add a genre to current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> addGenreToMyArtistProfile(Jwt jwt, @PathVariable UUID genreId);

    /** Remove a genre from current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> removeGenreFromMyArtistProfile(Jwt jwt, @PathVariable UUID genreId);

    /** Get skills for current user's Artist Profile */
    ResponseEntity<Set<SkillSummaryDto>> getMyArtistProfileSkills(Jwt jwt);

    /** Add a skill to current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> addSkillToMyArtistProfile(Jwt jwt, @PathVariable UUID skillId);

    /** Remove a skill from current user's Artist Profile */
    ResponseEntity<ArtistProfileDetailDto> removeSkillFromMyArtistProfile(Jwt jwt, @PathVariable UUID skillId);

    /** Get Artist Profile by username */
    ResponseEntity<ArtistProfileDetailDto> getArtistProfileByUsername(@PathVariable String username);

    /** Get genres for Artist Profile by username */
    ResponseEntity<Set<GenreSummaryDto>> getArtistProfileGenresByUsername(@PathVariable String username);

    /** Get skills for Artist Profile by username */
    ResponseEntity<Set<SkillSummaryDto>> getArtistProfileSkillsByUsername(@PathVariable String username);
}
