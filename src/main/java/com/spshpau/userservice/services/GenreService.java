package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GenreService {
    Genre createGenre(GenreDto genreDto);
    void deleteGenre(UUID genreId);
    Page<Genre> getAllGenres(Pageable pageable);
}
