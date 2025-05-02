package com.spshpau.be.services;

import com.spshpau.be.dto.profiledto.ProfileUpdateDto;
import com.spshpau.be.model.Genre;
import com.spshpau.be.model.ProducerProfile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProducerProfileService {

    Optional<ProducerProfile> getProducerProfileByUserId(UUID userId);

    ProducerProfile createOrUpdateProducerProfile(UUID userId, ProfileUpdateDto profileData);

    ProducerProfile patchProducerProfile(UUID userId, ProfileUpdateDto profileUpdateDto);

    ProducerProfile addGenreToProducerProfile(UUID userId, UUID genreId);

    ProducerProfile removeGenreFromProducerProfile(UUID userId, UUID genreId);

    Set<Genre> getProducerProfileGenres(UUID userId);

    /**
     * Finds a ProducerProfile based on the associated User's username.
     * @param username The username of the User.
     * @return Optional containing the ProducerProfile if found, otherwise empty.
     */
    Optional<ProducerProfile> getProducerProfileByUsername(String username);

    /**
     * Gets the Set of Genres for a ProducerProfile based on the associated User's username.
     * Throws EntityNotFoundException if the User or their Profile doesn't exist.
     * @param username The username of the User.
     * @return Set of Genres.
     */
    Set<Genre> getProducerProfileGenresByUsername(String username);
}
