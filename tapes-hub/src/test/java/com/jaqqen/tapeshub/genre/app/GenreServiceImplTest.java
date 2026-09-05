package com.jaqqen.tapeshub.genre.app;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.app.dto.GenreRequest;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreName;
import com.jaqqen.tapeshub.genre.domain.GenreNotFoundException;
import com.jaqqen.tapeshub.genre.domain.GenreRepository;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The application ring in isolation: the repository is a mock, so what is under test is the
 * translation between {@code UUID}s at the edge and {@link GenreId}s inside, and the decision to
 * turn an empty {@link Optional} into a {@link GenreNotFoundException}.
 */
@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepoMock;

    @InjectMocks
    private GenreServiceImpl service;

    @Captor
    private ArgumentCaptor<Genre> saved;

    private static Genre genre(String name, @Nullable String description) {
        return Genre.create(new GenreName(name), description);
    }

    @Test
    void listMapsEveryGenreToItsDetails() {
        final String genre1Name = "Horror";
        final String genre1Description = "Built to frighten.";
        final Genre horror = genre(genre1Name, genre1Description);

        final String genre2Name = "Sci-Fi";
        final String genre2Description = null;
        final Genre sciFi = genre(genre2Name, genre2Description);

        when(genreRepoMock.findAll()).thenReturn(List.of(horror, sciFi));

        final List<GenreDetails> details = service.list();

        assertThat(details).containsExactly(
            new GenreDetails(horror.getId().value(), genre1Name, genre1Description),
            new GenreDetails(sciFi.getId().value(), genre2Name, genre2Description));
    }

    @Test
    void listOfNothingIsEmptyRatherThanNull() {
        when(genreRepoMock.findAll()).thenReturn(List.of());

        assertThat(service.list()).isEmpty();
    }

    @Test
    void getReturnsTheDetailsOfAnExistingGenre() {
        final String name = "Horror";
        final String description = "Built to frighten.";
        final Genre horror = genre(name, description);
        when(genreRepoMock.findById(horror.getId())).thenReturn(Optional.of(horror));

        assertThat(service.get(horror.getId().value()))
            .isEqualTo(new GenreDetails(horror.getId().value(), name, description));
    }

    @Test
    void getRejectsAnUnknownId() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.findById(new GenreId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(GenreNotFoundException.class)
            .isThrownBy(() -> service.get(id))
            .withMessage("No genre with id '%s'".formatted(id));
    }

    @Test
    void createGenreAndReturnsItsNewId() {
        final String name = "Western";
        final String description = "The frontier.";
        when(genreRepoMock.save(any())).thenAnswer(call -> call.getArgument(0));

        final GenreDetails created = service.create(new GenreRequest(name, description));

        verify(genreRepoMock).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo(new GenreName(name));
        assertThat(saved.getValue().getDescription()).isEqualTo(description);
        assertThat(created.id()).isEqualTo(saved.getValue().getId().value());
        assertThat(created.name()).isEqualTo(name);
    }

    @Test
    void replaceOverwritesNameAndDescriptionOnTheStoredGenre() {
        final Genre stored = genre("Sci-Fi", "old");
        when(genreRepoMock.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genreRepoMock.save(any())).thenAnswer(call -> call.getArgument(0));

        final String newName = "Science Fiction";
        final String newDescription = "new";
        final GenreDetails replaced = service.replace(stored.getId().value(),
            new GenreRequest(newName, newDescription));

        assertThat(replaced).isEqualTo(
            new GenreDetails(stored.getId().value(), newName, newDescription));
    }

    @Test
    void replaceWithoutADescriptionClearsIt() {
        final String name = "Sci-Fi";
        final Genre stored = genre(name, "old");
        when(genreRepoMock.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genreRepoMock.save(any())).thenAnswer(call -> call.getArgument(0));

        // PUT replaces the whole resource, so an omitted description means "no description",
        // not "leave the old one".
        assertThat(service.replace(stored.getId().value(), new GenreRequest(name, null)).description())
            .isNull();
    }

    @Test
    void replaceOfAnUnknownGenreSavesNothing() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.findById(new GenreId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(GenreNotFoundException.class)
            .isThrownBy(() -> service.replace(id, new GenreRequest("Western", null)));
        verify(genreRepoMock, never()).save(any());
    }

    @Test
    void deleteRemovesTheGenre() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.deleteById(new GenreId(id))).thenReturn(true);

        service.delete(id);

        verify(genreRepoMock).deleteById(new GenreId(id));
    }

    @Test
    void deleteOfAnUnknownGenreIsANotFound() {
        final UUID id = UUID.randomUUID();
        // The repository reports "there was nothing to delete" as false; the 404 is made here.
        when(genreRepoMock.deleteById(new GenreId(id))).thenReturn(false);

        assertThatExceptionOfType(GenreNotFoundException.class)
            .isThrownBy(() -> service.delete(id))
            .withMessage("No genre with id '%s'".formatted(id));
    }

    @Test
    void findByIdIsEmptyForAnUnknownGenre() {
        final GenreId id = GenreId.newId();
        when(genreRepoMock.findById(id)).thenReturn(Optional.empty());

        // The published API returns Optional rather than throwing: the tape module decides what a
        // missing genre means for it, and answers with 422 instead of 404.
        assertThat(service.findById(id)).isEmpty();
    }

    @Test
    void findByIdReturnsDetailsForAKnownGenre() {
        final String name = "Horror";
        final Genre horror = genre(name, null);
        when(genreRepoMock.findById(horror.getId())).thenReturn(Optional.of(horror));

        assertThat(service.findById(horror.getId()))
            .contains(new GenreDetails(horror.getId().value(), name, null));
    }

    @Test
    void findByNameDelegatesToTheRepository() {
        final String name = "Horror";
        final Genre horror = genre(name, null);
        when(genreRepoMock.findByName(name)).thenReturn(Optional.of(horror));

        assertThat(service.findByName(name)).map(GenreDetails::name).contains(name);
    }

    @Test
    void findByNameIsEmptyForAnUnknownName() {
        final String name = "Nope";
        when(genreRepoMock.findByName(name)).thenReturn(Optional.empty());

        assertThat(service.findByName(name)).isEmpty();
    }

    @Test
    void findAllByIdsKeysTheResultByIdentity() {
        final String horrorName = "Horror";
        final String sciFiName = "Sci-Fi";
        final Genre horror = genre(horrorName, null);
        final Genre sciFi = genre(sciFiName, null);
        final List<GenreId> ids = List.of(horror.getId(), sciFi.getId());
        when(genreRepoMock.findAllByIds(ids)).thenReturn(List.of(horror, sciFi));

        final Map<GenreId, GenreDetails> byId = service.findAllByIds(ids);

        assertThat(byId).hasSize(2);
        assertThat(byId).containsEntry(horror.getId(), new GenreDetails(horror.getId().value(), horrorName, null));
        assertThat(byId).containsEntry(sciFi.getId(), new GenreDetails(sciFi.getId().value(), sciFiName, null));
    }

    @Test
    void findAllByIdsOmitsIdsThatResolveToNothing() {
        final Genre horror = genre("Horror", null);
        final GenreId missing = GenreId.newId();
        final List<GenreId> ids = List.of(horror.getId(), missing);
        when(genreRepoMock.findAllByIds(ids)).thenReturn(List.of(horror));

        // Documented behaviour: unresolved ids are absent, not null-valued entries.
        assertThat(service.findAllByIds(ids)).containsOnlyKeys(horror.getId());
    }

    @Test
    void findAllByIdsSurvivesADuplicateKey() {
        final Genre horror = genre("Horror", null);
        final List<GenreId> ids = List.of(horror.getId());
        // Collectors.toMap throws on a duplicate key unless a merge function is supplied - this is
        // what proves the one in GenreServiceImpl is doing its job.
        when(genreRepoMock.findAllByIds(ids)).thenReturn(List.of(horror, horror));

        assertThat(service.findAllByIds(ids)).containsOnlyKeys(horror.getId());
    }
}
