package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.model.ProducerProfile;
import com.spshpau.userservice.model.Genre;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;
import java.util.UUID;

public interface ProducerProfileController {
    /** Get the Producer Profile for the currently authenticated user. */
    ResponseEntity<ProducerProfileDetailDto> getMyProducerProfile(Jwt jwt);

    /** Create or fully update the Producer Profile for the currently authenticated user. */
    ResponseEntity<ProducerProfileDetailDto> createOrUpdateMyProducerProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /** Partially update the Producer Profile for the currently authenticated user. */
    ResponseEntity<ProducerProfileDetailDto> patchMyProducerProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /** Get the genres associated with the current user's Producer Profile. */
    ResponseEntity<Set<GenreSummaryDto>> getMyProducerProfileGenres(Jwt jwt);

    /** Add a pre-existing Genre to the current user's Producer Profile. */
    ResponseEntity<ProducerProfileDetailDto> addGenreToMyProducerProfile(Jwt jwt, @PathVariable UUID genreId);

    /** Remove a Genre association from the current user's Producer Profile. */
    ResponseEntity<ProducerProfileDetailDto> removeGenreFromMyProducerProfile(Jwt jwt, @PathVariable UUID genreId);

    /** Get a Producer Profile by username (publicly accessible). */
    ResponseEntity<ProducerProfileDetailDto> getProducerProfileByUsername(@PathVariable String username);

    /** Get the genres associated with a Producer Profile by username (publicly accessible). */
    ResponseEntity<Set<GenreSummaryDto>> getProducerProfileGenresByUsername(@PathVariable String username);
}
