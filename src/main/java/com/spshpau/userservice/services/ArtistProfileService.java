package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.model.ArtistProfile;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.Skill;


import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ArtistProfileService {

    /**
     * Get the ArtistProfile for a given User UUID.
     */
    Optional<ArtistProfile> getArtistProfileByUserId(UUID userId);

    /**
     * Creates or fully updates the ArtistProfile for a given User UUID.
     * If profile exists, it's overwritten. If not, it's created.
     */
    ArtistProfile createOrUpdateArtistProfile(UUID userId, ProfileUpdateDto profileData);


    /**
     * Partially updates the ArtistProfile details for a given User UUID.
     * Only non-null fields in the DTO are updated.
     */
    ArtistProfile patchArtistProfile(UUID userId, ProfileUpdateDto profileUpdateDto);


    /**
     * Adds an existing Genre to the ArtistProfile.
     */
    ArtistProfile addGenreToArtistProfile(UUID userId, UUID genreId);

    /**
     * Removes a Genre from the ArtistProfile.
     */
    ArtistProfile removeGenreFromArtistProfile(UUID userId, UUID genreId);

    /**
     * Adds an existing Skill to the ArtistProfile.
     */
    ArtistProfile addSkillToArtistProfile(UUID userId, UUID skillId);

    /**
     * Removes a Skill from the ArtistProfile.
     */
    ArtistProfile removeSkillFromArtistProfile(UUID userId, UUID skillId);

    /**
     * Get all Genres associated with the ArtistProfile.
     */
    Set<Genre> getArtistProfileGenres(UUID userId);

    /**
     * Get all Skills associated with the ArtistProfile.
     */
    Set<Skill> getArtistProfileSkills(UUID userId);

    /**
     * Finds an ArtistProfile based on the associated User's username.
     * @param username The username of the User.
     * @return Optional containing the ArtistProfile if found, otherwise empty.
     */
    Optional<ArtistProfile> getArtistProfileByUsername(String username);

    /**
     * Gets the Set of Genres for an ArtistProfile based on the associated User's username.
     * Throws EntityNotFoundException if the User or their Profile doesn't exist.
     * @param username The username of the User.
     * @return Set of Genres.
     */
    Set<Genre> getArtistProfileGenresByUsername(String username);

    /**
     * Gets the Set of Skills for an ArtistProfile based on the associated User's username.
     * Throws EntityNotFoundException if the User or their Profile doesn't exist.
     * @param username The username of the User.
     * @return Set of Skills.
     */
    Set<Skill> getArtistProfileSkillsByUsername(String username);
}
