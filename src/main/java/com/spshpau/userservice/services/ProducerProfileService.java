package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.ProducerProfile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProducerProfileService {

    Optional<ProducerProfileDetailDto> getProducerProfileByUserId(UUID userId);
    ProducerProfileDetailDto createOrUpdateProducerProfile(UUID userId, ProfileUpdateDto profileData);
    ProducerProfileDetailDto patchProducerProfile(UUID userId, ProfileUpdateDto profileUpdateDto);
    ProducerProfileDetailDto addGenreToProducerProfile(UUID userId, UUID genreId);
    ProducerProfileDetailDto removeGenreFromProducerProfile(UUID userId, UUID genreId);
    Set<GenreSummaryDto> getProducerProfileGenres(UUID userId);
    Optional<ProducerProfileDetailDto> getProducerProfileByUsername(String username);
    Set<GenreSummaryDto> getProducerProfileGenresByUsername(String username);
}
