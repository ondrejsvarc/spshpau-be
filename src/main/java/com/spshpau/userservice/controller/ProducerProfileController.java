package com.spshpau.userservice.controller;

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
    /**
     * Get the Producer Profile for the currently authenticated user.
     */
    public ResponseEntity<ProducerProfile> getMyProducerProfile(Jwt jwt);

    /**
     * Create or fully update the Producer Profile for the currently authenticated user.
     */
    public ResponseEntity<ProducerProfile> createOrUpdateMyProducerProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /**
     * Partially update the Producer Profile for the currently authenticated user.
     */
    public ResponseEntity<ProducerProfile> patchMyProducerProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /**
     * Get the genres associated with the current user's Producer Profile.
     */
    public ResponseEntity<Set<Genre>> getMyProducerProfileGenres(Jwt jwt);

    /**
     * Add a pre-existing Genre to the current user's Producer Profile.
     */
    public ResponseEntity<ProducerProfile> addGenreToMyProducerProfile(Jwt jwt, @PathVariable UUID genreId);

    /**
     * Remove a Genre association from the current user's Producer Profile.
     */
    public ResponseEntity<ProducerProfile> removeGenreFromMyProducerProfile(Jwt jwt, @PathVariable UUID genreId);

    /**
     * Get a Producer Profile by username (publicly accessible).
     */
    public ResponseEntity<ProducerProfile> getProducerProfileByUsername(@PathVariable String username);

    /**
     * Get the genres associated with a Producer Profile by username (publicly accessible).
     */
    public ResponseEntity<Set<Genre>> getProducerProfileGenresByUsername(@PathVariable String username);
}
