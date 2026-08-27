package com.jaqqen.tapeshub.tape.presentation;

import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.app.TapeService;
import com.jaqqen.tapeshub.tape.app.dto.CreateTapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.PatchTapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import com.jaqqen.tapeshub.tape.app.dto.UpdateTapeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        return service.findAll().stream().map(TapeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TapeResponse get(@PathVariable UUID id) {
        return TapeResponse.from(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TapeResponse> create(@Valid @RequestBody CreateTapeRequest request,
                                               UriComponentsBuilder uriBuilder) {
        Tape created = service.create(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/tapes/{id}")
                                   .build(created.getId().id()))
                .body(TapeResponse.from(created));
    }

    @PutMapping("/{id}")
    public TapeResponse replace(@PathVariable UUID id, @Valid @RequestBody UpdateTapeRequest request) {
        return TapeResponse.from(service.replace(id, request));
    }

    @PatchMapping("/{id}")
    public TapeResponse patch(@PathVariable UUID id, @Valid @RequestBody PatchTapeRequest request) {
        return TapeResponse.from(service.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
