package com.jaqqen.tapeshub.tape.app;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.GenreService;
import com.jaqqen.tapeshub.tape.app.dto.PatchTapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeColorsDto;
import com.jaqqen.tapeshub.tape.app.dto.TapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import com.jaqqen.tapeshub.tape.domain.Colors;
import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeDuration;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapeNotFoundException;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import com.jaqqen.tapeshub.tape.domain.TapeTitle;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The tape module's application ring with both of its collaborators mocked - the repository, and the
 * {@link GenreService} that is the only way this module may learn anything about a genre.
 */
@ExtendWith(MockitoExtension.class)
class TapeServiceTest {

    private static final LocalDate RELEASED = LocalDate.of(1987, 1, 1);
    private static final Colors COLORS = new Colors("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");
    private static final TapeColorsDto COLORS_DTO = new TapeColorsDto("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");

    private static final GenreId ACTION_ID = GenreId.newId();
    private static final GenreDetails ACTION = new GenreDetails(ACTION_ID.value(), "Action", "Chases and stunts.");
    private static final GenreId SCI_FI_ID = GenreId.newId();
    private static final GenreDetails SCI_FI = new GenreDetails(SCI_FI_ID.value(), "Sci-Fi", null);

    @Mock
    private TapeRepository tapes;

    @Mock
    private GenreService genres;

    @InjectMocks
    private TapeService service;

    @Captor
    private ArgumentCaptor<Tape> saved;

    private static Tape tape(String title, @Nullable String subtitle, GenreId genre) {
        return Tape.create(new TapeTitle(title), TapeTitle.ofNullable(subtitle), RELEASED, genre,
            new TapeDuration(6_840_000), COLORS, TapePattern.STRIPES);
    }

    private static TapeRequest request() {
        return new TapeRequest("NEON NIGHTS", "The City Never Sleeps", RELEASED, ACTION_ID.value(),
            6_840_000, COLORS_DTO, TapePattern.STRIPES);
    }

    // --- list -------------------------------------------------------------------------------

    @Test
    void listResolvesEveryGenreInASingleLookup() {
        Tape neon = tape("NEON NIGHTS", null, ACTION_ID);
        Tape chrome = tape("CHROME HORIZON", null, SCI_FI_ID);
        when(tapes.findAll()).thenReturn(List.of(neon, chrome));
        when(genres.findAllByIds(anyCollection())).thenReturn(Map.of(ACTION_ID, ACTION, SCI_FI_ID, SCI_FI));

        List<TapeResponse> responses = service.list();

        assertThat(responses).map(TapeResponse::title).containsExactly("NEON NIGHTS", "CHROME HORIZON");
        assertThat(responses).map(TapeResponse::genre).containsExactly(ACTION, SCI_FI);
        // The whole point of findAllByIds: rendering a page of tapes must not fan out into one
        // genre lookup per row.
        verify(genres, never()).findById(any());
    }

    @Test
    void listAsksForEachGenreOnlyOnceEvenWhenTapesShareIt() {
        when(tapes.findAll()).thenReturn(List.of(
            tape("NEON NIGHTS", null, ACTION_ID),
            tape("SOLAR BURN", null, ACTION_ID),
            tape("CHROME HORIZON", null, SCI_FI_ID)));
        when(genres.findAllByIds(anyCollection())).thenReturn(Map.of(ACTION_ID, ACTION, SCI_FI_ID, SCI_FI));

        service.list();

        ArgumentCaptor<java.util.Collection<GenreId>> ids = ArgumentCaptor.captor();
        verify(genres).findAllByIds(ids.capture());
        assertThat(ids.getValue()).containsExactlyInAnyOrderElementsOf(Set.of(ACTION_ID, SCI_FI_ID));
    }

    @Test
    void listOfNothingLooksUpNothing() {
        when(tapes.findAll()).thenReturn(List.of());
        when(genres.findAllByIds(anyCollection())).thenReturn(Map.of());

        assertThat(service.list()).isEmpty();
    }

    @Test
    void listFailsLoudlyWhenATapePointsAtAGenreTheLookupDidNotReturn() {
        when(tapes.findAll()).thenReturn(List.of(tape("NEON NIGHTS", null, ACTION_ID)));
        when(genres.findAllByIds(anyCollection())).thenReturn(Map.of());

        // The two modules disagree. Reported as the same failure the one-at-a-time path reports,
        // rather than an NPE somewhere further down.
        assertThatExceptionOfType(UnknownGenreException.class)
            .isThrownBy(() -> service.list())
            .withMessage("No genre with id '%s'".formatted(ACTION_ID));
    }

    // --- get --------------------------------------------------------------------------------

    @Test
    void getReturnsTheTapeWithItsGenreExpanded() {
        Tape neon = tape("NEON NIGHTS", "The City Never Sleeps", ACTION_ID);
        when(tapes.findById(neon.getId())).thenReturn(Optional.of(neon));
        when(genres.findById(ACTION_ID)).thenReturn(Optional.of(ACTION));

        TapeResponse response = service.get(neon.getId().value());

        assertThat(response.id()).isEqualTo(neon.getId().value());
        assertThat(response.title()).isEqualTo("NEON NIGHTS");
        assertThat(response.subtitle()).isEqualTo("The City Never Sleeps");
        assertThat(response.releaseDate()).isEqualTo(RELEASED);
        assertThat(response.duration()).isEqualTo(6_840_000);
        assertThat(response.colors()).isEqualTo(COLORS_DTO);
        assertThat(response.pattern()).isEqualTo(TapePattern.STRIPES);
        // Requests name a genre by id; responses carry the whole thing, so no second round trip.
        assertThat(response.genre()).isEqualTo(ACTION);
    }

    @Test
    void getOfAnUnknownTapeIsANotFound() {
        UUID id = UUID.randomUUID();
        when(tapes.findById(new TapeId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(TapeNotFoundException.class)
            .isThrownBy(() -> service.get(id))
            .withMessage("No tape with id '%s'".formatted(id));
    }

    // --- create -----------------------------------------------------------------------------

    @Test
    void createBuildsTheTapeFromTheRequest() {
        when(genres.findById(ACTION_ID)).thenReturn(Optional.of(ACTION));
        when(tapes.save(any())).thenAnswer(call -> call.getArgument(0));

        TapeResponse created = service.create(request());

        verify(tapes).save(saved.capture());
        Tape tape = saved.getValue();
        assertThat(tape.getTitle()).isEqualTo(new TapeTitle("NEON NIGHTS"));
        assertThat(tape.getSubtitle()).isEqualTo(new TapeTitle("The City Never Sleeps"));
        assertThat(tape.getGenre()).isEqualTo(ACTION_ID);
        assertThat(tape.getColors()).isEqualTo(COLORS);
        assertThat(created.id()).isEqualTo(tape.getId().value());
        assertThat(created.genre()).isEqualTo(ACTION);
    }

    @Test
    void createTreatsABlankSubtitleAsNoSubtitle() {
        when(genres.findById(ACTION_ID)).thenReturn(Optional.of(ACTION));
        when(tapes.save(any())).thenAnswer(call -> call.getArgument(0));

        TapeResponse created = service.create(new TapeRequest("NEON NIGHTS", "   ", RELEASED,
            ACTION_ID.value(), 6_840_000, COLORS_DTO, TapePattern.STRIPES));

        assertThat(created.subtitle()).isNull();
    }

    @Test
    void createRejectsAnUnknownGenreBeforeSavingAnything() {
        when(genres.findById(ACTION_ID)).thenReturn(Optional.empty());

        // 422 rather than 404: the URL is fine, the body names a genre that does not exist - and a
        // tape cannot be stored without one, so there is nothing to fall back to.
        assertThatExceptionOfType(UnknownGenreException.class)
            .isThrownBy(() -> service.create(request()))
            .withMessage("No genre with id '%s'".formatted(ACTION_ID));
        verify(tapes, never()).save(any());
    }

    // --- replace ----------------------------------------------------------------------------

    @Test
    void replaceOverwritesTheStoredTape() {
        Tape stored = tape("NEON NIGHTS", null, ACTION_ID);
        when(genres.findById(SCI_FI_ID)).thenReturn(Optional.of(SCI_FI));
        when(tapes.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(tapes.save(any())).thenAnswer(call -> call.getArgument(0));

        TapeResponse replaced = service.replace(stored.getId().value(), new TapeRequest(
            "CHROME HORIZON", "Beyond the Last Frontier", LocalDate.of(1984, 1, 1),
            SCI_FI_ID.value(), 7_920_000,
            new TapeColorsDto("#00b4d8", "#0077b6", "#90e0ef", "#023e8a"), TapePattern.GRADIENT));

        assertThat(replaced.id()).isEqualTo(stored.getId().value());
        assertThat(replaced.title()).isEqualTo("CHROME HORIZON");
        assertThat(replaced.genre()).isEqualTo(SCI_FI);
        assertThat(replaced.pattern()).isEqualTo(TapePattern.GRADIENT);
    }

    @Test
    void replaceOfAnUnknownTapeIsANotFound() {
        UUID id = UUID.randomUUID();
        when(genres.findById(ACTION_ID)).thenReturn(Optional.of(ACTION));
        when(tapes.findById(new TapeId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(TapeNotFoundException.class)
            .isThrownBy(() -> service.replace(id, request()));
        verify(tapes, never()).save(any());
    }

    @Test
    void replaceWithAnUnknownGenreNeverTouchesTheTape() {
        when(genres.findById(ACTION_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(UnknownGenreException.class)
            .isThrownBy(() -> service.replace(UUID.randomUUID(), request()));
        verify(tapes, never()).findById(any());
        verify(tapes, never()).save(any());
    }

    // --- patch ------------------------------------------------------------------------------

    @Test
    void patchChangesOnlyTheFieldsThatWereSent() {
        Tape stored = tape("NEON NIGHTS", "The City Never Sleeps", ACTION_ID);
        when(tapes.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genres.findById(ACTION_ID)).thenReturn(Optional.of(ACTION));
        when(tapes.save(any())).thenAnswer(call -> call.getArgument(0));

        TapeResponse patched = service.patch(stored.getId().value(), new PatchTapeRequest(
            "NEON NIGHTS II", null, null, null, null, null, null));

        assertThat(patched.title()).isEqualTo("NEON NIGHTS II");
        assertThat(patched.subtitle()).isEqualTo("The City Never Sleeps");
        assertThat(patched.releaseDate()).isEqualTo(RELEASED);
        assertThat(patched.duration()).isEqualTo(6_840_000);
        assertThat(patched.colors()).isEqualTo(COLORS_DTO);
        assertThat(patched.pattern()).isEqualTo(TapePattern.STRIPES);
        assertThat(patched.genre()).isEqualTo(ACTION);
    }

    @Test
    void anEmptyPatchLeavesTheTapeAsItWas() {
        Tape stored = tape("NEON NIGHTS", null, ACTION_ID);
        when(tapes.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genres.findById(ACTION_ID)).thenReturn(Optional.of(ACTION));
        when(tapes.save(any())).thenAnswer(call -> call.getArgument(0));

        TapeResponse patched = service.patch(stored.getId().value(),
            new PatchTapeRequest(null, null, null, null, null, null, null));

        assertThat(patched.title()).isEqualTo("NEON NIGHTS");
        assertThat(patched.genre()).isEqualTo(ACTION);
    }

    @Test
    void patchCanChangeEveryField() {
        Tape stored = tape("NEON NIGHTS", null, ACTION_ID);
        when(tapes.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genres.findById(SCI_FI_ID)).thenReturn(Optional.of(SCI_FI));
        when(tapes.save(any())).thenAnswer(call -> call.getArgument(0));

        TapeResponse patched = service.patch(stored.getId().value(), new PatchTapeRequest(
            "CHROME HORIZON", "Beyond the Last Frontier", LocalDate.of(1984, 1, 1),
            SCI_FI_ID.value(), 7_920_000,
            new TapeColorsDto("#00b4d8", "#0077b6", "#90e0ef", "#023e8a"), TapePattern.GRADIENT));

        assertThat(patched.title()).isEqualTo("CHROME HORIZON");
        assertThat(patched.subtitle()).isEqualTo("Beyond the Last Frontier");
        assertThat(patched.releaseDate()).isEqualTo(LocalDate.of(1984, 1, 1));
        assertThat(patched.genre()).isEqualTo(SCI_FI);
        assertThat(patched.duration()).isEqualTo(7_920_000);
        assertThat(patched.colors()).isEqualTo(new TapeColorsDto("#00b4d8", "#0077b6", "#90e0ef", "#023e8a"));
        assertThat(patched.pattern()).isEqualTo(TapePattern.GRADIENT);
    }

    @Test
    void patchValidatesAReclassificationOnTheWayOut() {
        Tape stored = tape("NEON NIGHTS", null, ACTION_ID);
        when(tapes.findById(stored.getId())).thenReturn(Optional.of(stored));
        when(genres.findById(SCI_FI_ID)).thenReturn(Optional.empty());

        // The genre is resolved after the fields are applied, which is what makes a PATCH that only
        // moves the tape to a non-existent genre fail rather than persist a dangling reference.
        assertThatExceptionOfType(UnknownGenreException.class)
            .isThrownBy(() -> service.patch(stored.getId().value(), new PatchTapeRequest(
                null, null, null, SCI_FI_ID.value(), null, null, null)))
            .withMessage("No genre with id '%s'".formatted(SCI_FI_ID));
        verify(tapes, never()).save(any());
    }

    @Test
    void patchOfAnUnknownTapeIsANotFound() {
        UUID id = UUID.randomUUID();
        when(tapes.findById(new TapeId(id))).thenReturn(Optional.empty());

        assertThatExceptionOfType(TapeNotFoundException.class)
            .isThrownBy(() -> service.patch(id, new PatchTapeRequest(
                null, null, null, null, null, null, null)));
    }

    // --- delete -----------------------------------------------------------------------------

    @Test
    void deleteRemovesTheTape() {
        UUID id = UUID.randomUUID();
        when(tapes.deleteById(new TapeId(id))).thenReturn(true);

        service.delete(id);

        verify(tapes).deleteById(new TapeId(id));
    }

    @Test
    void deleteOfAnUnknownTapeIsANotFound() {
        UUID id = UUID.randomUUID();
        when(tapes.deleteById(new TapeId(id))).thenReturn(false);

        assertThatExceptionOfType(TapeNotFoundException.class)
            .isThrownBy(() -> service.delete(id))
            .withMessage("No tape with id '%s'".formatted(id));
    }
}
