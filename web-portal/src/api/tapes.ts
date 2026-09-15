import type { Tape } from "../data/tapes";
import { Duration } from "luxon";


export interface GenreDetails {
    id: string;
    name: string;
    description: string | null;
    /** ISO-8601 instant, e.g. "2026-09-10T12:00:00Z". When the genre was first stored. */
    createdAt: string;
    /** ISO-8601 instant. When it last changed. */
    modifiedAt: string;
    /**
     * Always null over the wire: tapes-hub deletes are soft, and a deleted genre is filtered out of
     * every response before it gets here. Declared so the shape matches the API rather than a subset
     * of it.
     */
    deletedAt: string | null;
}

interface TapeResponse {
    id: string;
    title: string;
    subtitle: string | null;
    /** ISO-8601 date, e.g. "1987-01-01". */
    releaseDate: string;
    genre: GenreDetails;
    /** Runtime in milliseconds. */
    duration: number;
    colors: Tape["colors"];
    /** via @JsonValue. */
    pattern: Tape["pattern"];
    /**
     * ISO-8601 instant. Not to be confused with `releaseDate`: that is when the film came out, this
     * is when the catalogue entry was written.
     */
    createdAt: string;
    /** ISO-8601 instant. When the catalogue entry last changed. */
    modifiedAt: string;
    /** Always null over the wire - see {@link GenreDetails.deletedAt}. */
    deletedAt: string | null;
}

/** Body of `POST /api/tapes`. A tape names its genre by id; genres are their own resource. */
export interface NewTape {
    title: string;
    subtitle?: string;
    /** ISO-8601 date, e.g. "1987-01-01". */
    releaseDate: string;
    genreId: string;
    /** Runtime in milliseconds, must be positive. */
    duration: number;
    colors: Tape["colors"];
    pattern: Tape["pattern"];
}

/** RFC 9457 problem body, which is what tapes-hub returns for every 4xx. */
interface ProblemDetail {
    title?: string;
    detail?: string;
    /** Field-level messages, set by the validation handler only. */
    errors?: string[];
}

/** 6840000 -> "1h 54min"; under an hour drops the hours segment. */
export function formatDuration(milliseconds: number): string {
    const objectFormat = Duration.fromMillis(milliseconds).shiftTo('hours', 'minutes').toObject();

    const hours: number | undefined = objectFormat.hours ?? 0;
    const minutes: number | undefined = objectFormat.minutes ?? 0;
    return hours > 0 ? `${hours}h ${minutes}min` : `${minutes}min`;
}

function toTape(dto: TapeResponse): Tape {
    return {
        id: dto.id,
        title: dto.title,
        // `subtitle` is optional in the UI, so a wire null must become undefined.
        subtitle: dto.subtitle ?? undefined,
        year: dto.releaseDate.slice(0, 4),
        genre: dto.genre.name,
        duration: formatDuration(dto.duration),
        colors: dto.colors,
        pattern: dto.pattern,
    };
}

async function toErrorMessage(response: Response, fallback: string): Promise<string> {
    let problem: ProblemDetail | null = null;
    try {
        problem = await response.json() as ProblemDetail;
    } catch {
        problem = null;
    }

    if (problem?.errors?.length) {
        return problem.errors.join(", ");
    }
    if (problem?.detail) {
        return problem.detail;
    }
    return `${fallback} (${response.status} ${response.statusText})`;
}

export async function fetchTapes(signal?: AbortSignal): Promise<Tape[]> {
    const response = await fetch("/api/tapes", { signal });
    if (!response.ok) {
        throw new Error(`Could not load tapes (${response.status} ${response.statusText})`);
    }

    const body: TapeResponse[] = await response.json();
    return body.map(toTape);
}

export async function fetchGenres(signal?: AbortSignal): Promise<GenreDetails[]> {
    const response: Response = await fetch("/api/genres", { signal });
    if (!response.ok) {
        throw new Error(`Could not load genres (${response.status} ${response.statusText})`);
    }

    return await response.json() as GenreDetails[];
}

/** Returns the stored tape, so the caller can put it on the shelf without refetching the list. */
export async function createTape(tape: NewTape, signal?: AbortSignal): Promise<Tape> {
    const response: Response = await fetch("/api/tapes", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(tape),
        signal,
    });
    if (!response.ok) {
        throw new Error(await toErrorMessage(response, "Could not save the tape"));
    }

    return toTape(await response.json() as TapeResponse);
}
