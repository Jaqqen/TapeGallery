package com.jaqqen.tapeshub.genre.presentation;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.app.GenreService;
import com.jaqqen.tapeshub.genre.app.dto.GenreRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Genres are their own resource. A tape only ever points at one of these by id, which is why the
 * tape endpoints have no way to create a genre as a side effect.
 */
@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService service;

    public GenreController(GenreService service) {
        this.service = service;
    }

    @GetMapping
    public List<GenreDetails> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public GenreDetails get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<GenreDetails> create(@Valid @RequestBody GenreRequest request,
                                               UriComponentsBuilder uriBuilder) {
        GenreDetails created = service.create(request);
        return ResponseEntity
            .created(uriBuilder.path("/api/genres/{id}").build(created.id()))
            .body(created);
    }

    @PutMapping("/{id}")
    public GenreDetails replace(@PathVariable UUID id, @Valid @RequestBody GenreRequest request) {
        return service.replace(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
