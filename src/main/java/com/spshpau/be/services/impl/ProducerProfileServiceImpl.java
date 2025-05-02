package com.spshpau.be.services.impl;

import com.spshpau.be.dto.profiledto.ProfileUpdateDto;
import com.spshpau.be.model.*;
import com.spshpau.be.repositories.*;
import com.spshpau.be.services.ProducerProfileService;
import com.spshpau.be.services.exceptions.GenreLimitExceededException;
import com.spshpau.be.services.exceptions.GenreNotFoundException;
import com.spshpau.be.services.exceptions.ProfileNotFoundException;
import com.spshpau.be.services.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProducerProfileServiceImpl implements ProducerProfileService {

    private final ProducerProfileRepository producerProfileRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    private static final int MAX_GENRES = 10;


    private ProducerProfile findProfileByUserIdOrThrow(UUID userId) {
        return producerProfileRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException("ProducerProfile not found for user ID: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProducerProfile> getProducerProfileByUserId(UUID userId) {
        return producerProfileRepository.findById(userId);
    }

    @Override
    @Transactional
    public ProducerProfile createOrUpdateProducerProfile(UUID userId, ProfileUpdateDto profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        ProducerProfile profile = producerProfileRepository.findById(userId)
                .orElse(new ProducerProfile());

        profile.setAvailability(profileData.getAvailability() != null ? profileData.getAvailability() : false);
        profile.setBio(profileData.getBio());
        profile.setExperienceLevel(profileData.getExperienceLevel());

        profile.setUser(user);

        return producerProfileRepository.save(profile);
    }


    @Override
    @Transactional
    public ProducerProfile patchProducerProfile(UUID userId, ProfileUpdateDto profileUpdateDto) {
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);

        if (profileUpdateDto.getAvailability() != null) {
            profile.setAvailability(profileUpdateDto.getAvailability());
        }
        if (profileUpdateDto.getBio() != null) {
            profile.setBio(profileUpdateDto.getBio());
        }
        if (profileUpdateDto.getExperienceLevel() != null) {
            profile.setExperienceLevel(profileUpdateDto.getExperienceLevel());
        }

        return producerProfileRepository.save(profile);
    }


    @Override
    @Transactional
    public ProducerProfile addGenreToProducerProfile(UUID userId, UUID genreId) {
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));

        // Optional: Enforce business rule limit
        if (profile.getGenres().size() >= MAX_GENRES) {
            throw new GenreLimitExceededException("Cannot add more than " + MAX_GENRES + " genres to ProducerProfile.");
        }

        profile.addGenre(genre);
        return producerProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public ProducerProfile removeGenreFromProducerProfile(UUID userId, UUID genreId) {
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));

        profile.removeGenre(genre);
        return producerProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Genre> getProducerProfileGenres(UUID userId) {
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);
        return Collections.unmodifiableSet(profile.getGenres());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProducerProfile> getProducerProfileByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        return producerProfileRepository.findById(userOpt.get().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Genre> getProducerProfileGenresByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        ProducerProfile profile = findProfileByUserIdOrThrow(user.getId());

        return Collections.unmodifiableSet(profile.getGenres());
    }
}

