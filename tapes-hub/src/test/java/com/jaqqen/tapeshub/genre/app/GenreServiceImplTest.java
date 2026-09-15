package com.jaqqen.tapeshub.genre.app;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.GenreUsage;
import com.jaqqen.tapeshub.genre.app.dto.GenreRequest;
import com.jaqqen.tapeshub.genre.domain.*;
import com.jaqqen.tapeshub.shared.Lifecycle;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
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

    @Mock
    private GenreUsage usage;

    private GenreServiceImpl genreService;

    @Captor
    private ArgumentCaptor<Genre> genreArgumentCaptor;

    @BeforeEach
    void setUp() {
        genreService = new GenreServiceImpl(genreRepoMock, List.of(usage));
    }

    private static Genre genre(String name, @Nullable String description) {
        return Genre.create(new GenreName(name), description);
    }

    private static GenreDetails detailsOf(Genre genre) {
        final Lifecycle lifecycle = genre.getLifecycle();
        return new GenreDetails(genre.getId().value(), genre.getName().value(), genre.getDescription(),
            lifecycle.createdAt(), lifecycle.modifiedAt(), lifecycle.deletedAt());
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

        final List<GenreDetails> details = genreService.list();

        assertThat(details).containsExactly(detailsOf(horror), detailsOf(sciFi));
        assertThat(details).map(GenreDetails::name).containsExactly(genre1Name, genre2Name);
        assertThat(details).map(GenreDetails::description).containsExactly(genre1Description, genre2Description);
    }

    @Test
    void listOfNothingReturnsEmptyList() {
        when(genreRepoMock.findAll()).thenReturn(List.of());

        assertThat(genreService.list()).isEmpty();
    }

    @Test
    void checkGenreDetailsMatchGenreInformation() {
        final String name = "Horror";
        final String description = "Built to frighten.";
        final Genre horror = genre(name, description);
        final GenreId horrorId = horror.getId();

        when(genreRepoMock.findById(horrorId)).thenReturn(Optional.of(horror));

        assertThat(genreService.get(horrorId.value())).isEqualTo(detailsOf(horror));
    }

    @Test
    void getRejectsAnUnknownId() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.findById(new GenreId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(GenreNotFoundException.class)
            .isThrownBy(() -> genreService.get(id))
            .withMessage("No genre with id '%s'".formatted(id));
    }

    @Test
    void createGenreAndVerifyCreatedGenre() {
        final String name = "Western";
        final String description = "The frontier.";
        // call -> call.getArgument(0) - return 1st argument that was passed in and return it
        // here 'any' Genre that was passed in is returned by 'genreRepoMock.save(any())'
        when(genreRepoMock.save(any())).thenAnswer(call -> call.getArgument(0));

        final GenreDetails created = genreService.create(new GenreRequest(name, description));

        // verifies the execution of save and captures the argument that was passed to 'save'
        // through 'genreService.create'
        verify(genreRepoMock).save(genreArgumentCaptor.capture());
        assertThat(genreArgumentCaptor.getValue().getName()).isEqualTo(new GenreName(name));
        assertThat(genreArgumentCaptor.getValue().getDescription()).isEqualTo(description);
        assertThat(genreArgumentCaptor.getValue().getId().value()).isEqualTo(created.id());
        assertThat(created.name()).isEqualTo(name);
    }

    @Test
    void testAllFieldsAreReplaced() {
        final Genre stored = genre("Sci-Fi", "old");
        when(genreRepoMock.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genreRepoMock.save(any())).thenAnswer(call -> call.getArgument(0));

        final String newName = "Science Fiction";
        final String newDescription = "new";
        final GenreDetails replaced = genreService.replace(stored.getId().value(),
            new GenreRequest(newName, newDescription));

        assertThat(replaced.name()).isEqualTo(newName);
        assertThat(replaced.description()).isEqualTo(newDescription);
    }

    @Test
    void replaceWithoutADescriptionClearsIt() {
        final String name = "Sci-Fi";
        final Genre stored = genre(name, "old");
        when(genreRepoMock.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genreRepoMock.save(any())).thenAnswer(call -> call.getArgument(0));

        // PUT replaces the whole resource, so an omitted description means "no description",
        // not "leave the old one".
        assertThat(genreService.replace(stored.getId().value(), new GenreRequest(name, null)).description())
            .isNull();
    }

    @Test
    void replaceOfAnUnknownGenreSavesNothing() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.findById(new GenreId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(GenreNotFoundException.class)
            .isThrownBy(() -> genreService.replace(id, new GenreRequest("Western", null)));
        verify(genreRepoMock, never()).save(any());
    }

    @Test
    void deleteRemovesTheGenre() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.softDeleteById(new GenreId(id))).thenReturn(true);

        genreService.softDelete(id);

        verify(genreRepoMock).softDeleteById(new GenreId(id));
        assertThatNoException().isThrownBy(() -> genreService.softDelete(id));
    }

    @Test
    void checkForGenreUsagesOnDelete() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.softDeleteById(new GenreId(id))).thenReturn(true);

        genreService.softDelete(id);

        verify(usage).isInUse(new GenreId(id));
        assertThatNoException().isThrownBy(() -> genreService.softDelete(id));
    }

    @Test
    void rejectDeleteWhenGenreIsInUse() {
        final UUID id = UUID.randomUUID();
        when(usage.isInUse(new GenreId(id))).thenReturn(true);

        assertThatExceptionOfType(GenreInUseException.class)
            .isThrownBy(() -> genreService.softDelete(id))
            .withMessage("Genre '%s' is still in use and cannot be deleted".formatted(id));
        verify(genreRepoMock, never()).softDeleteById(any());
    }

    @Test
    void deleteOfAnUnknownGenreIsANotFound() {
        final UUID id = UUID.randomUUID();
        when(genreRepoMock.softDeleteById(new GenreId(id))).thenReturn(false);

        assertThatExceptionOfType(GenreNotFoundException.class)
            .isThrownBy(() -> genreService.softDelete(id))
            .withMessage("No genre with id '%s'".formatted(id));
    }

    @Test
    void findByIdIsEmptyForAnUnknownGenre() {
        final GenreId id = GenreId.newId();
        when(genreRepoMock.findById(id)).thenReturn(Optional.empty());

        assertThat(genreService.findById(id)).isEmpty();
    }

    @Test
    void findByIdReturnsDetailsForAKnownGenre() {
        final String name = "Horror";
        final Genre horror = genre(name, null);
        when(genreRepoMock.findById(horror.getId())).thenReturn(Optional.of(horror));

        assertThat(genreService.findById(horror.getId())).contains(detailsOf(horror));
        assertThat(genreService.findById(horror.getId())).map(GenreDetails::name).contains(name);
    }

    @Test
    void checkFindAllByIdsReturnsStoredGenres() {
        final String horrorName = "Horror";
        final String sciFiName = "Sci-Fi";
        final Genre horror = genre(horrorName, null);
        final Genre sciFi = genre(sciFiName, null);
        final List<GenreId> ids = List.of(horror.getId(), sciFi.getId());
        when(genreRepoMock.findAllByIds(ids)).thenReturn(List.of(horror, sciFi));

        final Map<GenreId, GenreDetails> byId = genreService.findAllByIds(ids);

        assertThat(byId).hasSize(2);
        assertThat(byId).containsEntry(horror.getId(), detailsOf(horror));
        assertThat(byId).containsEntry(sciFi.getId(), detailsOf(sciFi));
    }

    @Test
    void findAllByIdsReturnsOnlyFoundGenres() {
        final Genre horror = genre("Horror", null);
        final GenreId missing = GenreId.newId();
        final List<GenreId> ids = List.of(horror.getId(), missing);
        when(genreRepoMock.findAllByIds(ids)).thenReturn(List.of(horror));

        assertThat(genreService.findAllByIds(ids)).containsOnlyKeys(horror.getId());
    }
}
