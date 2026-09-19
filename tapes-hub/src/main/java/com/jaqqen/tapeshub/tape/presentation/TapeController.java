package com.jaqqen.tapeshub.tape.presentation;

import com.jaqqen.tapeshub.tape.app.TapeService;
import com.jaqqen.tapeshub.tape.app.dto.PatchTapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tapes")
public class TapeController {

    private final TapeService service;

    public TapeController(TapeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TapeResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public TapeResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<TapeResponse> create(@Valid @RequestBody TapeRequest request,
                                               UriComponentsBuilder uriBuilder) {
        TapeResponse created = service.create(request);
        return ResponseEntity
            .created(uriBuilder.path("/api/tapes/{id}").build(created.id()))
            .body(created);
    }

    @PutMapping("/{id}")
    public TapeResponse replace(@PathVariable UUID id, @Valid @RequestBody TapeRequest request) {
        return service.replace(id, request);
    }

    @PatchMapping("/{id}")
    public TapeResponse patch(@PathVariable UUID id, @Valid @RequestBody PatchTapeRequest request) {
        return service.patch(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
