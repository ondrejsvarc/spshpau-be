package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.model.Genre;
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
