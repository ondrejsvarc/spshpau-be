package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.ArtistProfile;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.Skill;


import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ArtistProfileService {

    Optional<ArtistProfileDetailDto> getArtistProfileByUserId(UUID userId);
    ArtistProfileDetailDto createOrUpdateArtistProfile(UUID userId, ProfileUpdateDto profileData);
    ArtistProfileDetailDto patchArtistProfile(UUID userId, ProfileUpdateDto profileUpdateDto);
    ArtistProfileDetailDto addGenreToArtistProfile(UUID userId, UUID genreId);
    ArtistProfileDetailDto removeGenreFromArtistProfile(UUID userId, UUID genreId);
    ArtistProfileDetailDto addSkillToArtistProfile(UUID userId, UUID skillId);
    ArtistProfileDetailDto removeSkillFromArtistProfile(UUID userId, UUID skillId);
    Set<GenreSummaryDto> getArtistProfileGenres(UUID userId);
    Set<SkillSummaryDto> getArtistProfileSkills(UUID userId);
    Optional<ArtistProfileDetailDto> getArtistProfileByUsername(String username);
    Set<GenreSummaryDto> getArtistProfileGenresByUsername(String username);
    Set<SkillSummaryDto> getArtistProfileSkillsByUsername(String username);
}
