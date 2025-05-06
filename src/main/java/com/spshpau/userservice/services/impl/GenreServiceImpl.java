package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.repositories.GenreRepository;
import com.spshpau.userservice.services.GenreService;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    @Transactional
    public Genre createGenre(GenreDto genreDto) {
        Optional<Genre> existingGenre = genreRepository.findByNameIgnoreCase(genreDto.getName());
        if (existingGenre.isPresent()) {
            throw new DuplicateException("Genre with name '" + genreDto.getName() + "' already exists.");
        }

        Genre newGenre = new Genre(genreDto.getName());
        Genre savedGenre = genreRepository.save(newGenre);
        return savedGenre;
    }

    @Override
    @Transactional
    public void deleteGenre(UUID genreId) {
        if (!genreRepository.existsById(genreId)) {
            throw new GenreNotFoundException("Genre not found with ID: " + genreId);
        }

        try {
            genreRepository.deleteById(genreId);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Cannot delete genre with ID " + genreId + " because it is currently assigned to one or more profiles.");
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Genre> getAllGenres(Pageable pageable) {
        return genreRepository.findAll(pageable);
    }
}
