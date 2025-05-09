package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GenreService {
    GenreSummaryDto createGenre(GenreDto genreDto);
    void deleteGenre(UUID genreId);
    Page<GenreSummaryDto> getAllGenres(Pageable pageable);
}
