package com.spshpau.be.services.impl;

import com.spshpau.be.dto.profiledto.ProfileUpdateDto;
import com.spshpau.be.model.*;
import com.spshpau.be.repositories.*;
import com.spshpau.be.services.ArtistProfileService;
import com.spshpau.be.services.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final ArtistProfileRepository artistProfileRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final SkillRepository skillRepository;

    private static final int MAX_GENRES = 10;
    private static final int MAX_SKILLS = 5;


    @Override
    @Transactional(readOnly = true)
    public Optional<ArtistProfile> getArtistProfileByUserId(UUID userId) {
        return artistProfileRepository.findById(userId);
    }

    private ArtistProfile findProfileByUserIdOrThrow(UUID userId) {
        return artistProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("ArtistProfile not found for user ID: " + userId));
    }

    @Override
    @Transactional
    public ArtistProfile createOrUpdateArtistProfile(UUID userId, ProfileUpdateDto profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        ArtistProfile profile = artistProfileRepository.findById(userId)
                .orElse(new ArtistProfile());

        profile.setAvailability(profileData.getAvailability() != null ? profileData.getAvailability() : false);
        profile.setBio(profileData.getBio());
        profile.setExperienceLevel(profileData.getExperienceLevel());

        profile.setUser(user);

        return artistProfileRepository.save(profile);
    }


    @Override
    @Transactional
    public ArtistProfile patchArtistProfile(UUID userId, ProfileUpdateDto profileUpdateDto) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);

        if (profileUpdateDto.getAvailability() != null) {
            profile.setAvailability(profileUpdateDto.getAvailability());
        }
        if (profileUpdateDto.getBio() != null) {
            profile.setBio(profileUpdateDto.getBio());
        }
        if (profileUpdateDto.getExperienceLevel() != null) {
            profile.setExperienceLevel(profileUpdateDto.getExperienceLevel());
        }

        return artistProfileRepository.save(profile);
    }


    @Override
    @Transactional
    public ArtistProfile addGenreToArtistProfile(UUID userId, UUID genreId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));

        if (profile.getGenres().size() >= MAX_GENRES) {
            throw new GenreLimitExceededException("Cannot add more than " + MAX_GENRES + " genres to ArtistProfile.");
        }

        profile.addGenre(genre);

        return artistProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public ArtistProfile removeGenreFromArtistProfile(UUID userId, UUID genreId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));

        profile.removeGenre(genre);

        return artistProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public ArtistProfile addSkillToArtistProfile(UUID userId, UUID skillId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with ID: " + skillId));

        if (profile.getSkills().size() >= MAX_SKILLS) {
            throw new SkillLimitExceededException("Cannot add more than " + MAX_SKILLS + " skills to ArtistProfile.");
        }

        profile.addSkill(skill);

        return artistProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public ArtistProfile removeSkillFromArtistProfile(UUID userId, UUID skillId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with ID: " + skillId));

        profile.removeSkill(skill);

        return artistProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Genre> getArtistProfileGenres(UUID userId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        return Collections.unmodifiableSet(profile.getGenres());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Skill> getArtistProfileSkills(UUID userId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        return Collections.unmodifiableSet(profile.getSkills());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArtistProfile> getArtistProfileByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> artistProfileRepository.findById(user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Genre> getArtistProfileGenresByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        ArtistProfile profile = findProfileByUserIdOrThrow(user.getId());
        return Collections.unmodifiableSet(profile.getGenres());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Skill> getArtistProfileSkillsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        ArtistProfile profile = findProfileByUserIdOrThrow(user.getId());
        return Collections.unmodifiableSet(profile.getSkills());
    }
}
