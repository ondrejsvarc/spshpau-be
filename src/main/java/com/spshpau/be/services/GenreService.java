package com.spshpau.be.services;

import com.spshpau.be.dto.profiledto.GenreDto;
import com.spshpau.be.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GenreService {
    Genre createGenre(GenreDto genreDto);
    void deleteGenre(UUID genreId);
    Page<Genre> getAllGenres(Pageable pageable);
}
