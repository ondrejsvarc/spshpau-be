package com.spshpau.be.controller;

import com.spshpau.be.dto.profiledto.GenreDto;
import com.spshpau.be.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface GenreController {

    /** Create a new Genre */
    ResponseEntity<Genre> addGenre(@RequestBody GenreDto genreDto);

    /** Delete a Genre by ID */
    ResponseEntity<Void> deleteGenre(@PathVariable UUID genreId);

    /** Get all Genres with pagination */
    ResponseEntity<Page<Genre>> getAllGenres(Pageable pageable);
}
