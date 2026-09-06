import type { Tape } from "../data/tapes";
import { Duration } from "luxon";


interface GenreDetails {
    id: string;
    name: string;
    description: string | null;
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

export async function fetchTapes(signal?: AbortSignal): Promise<Tape[]> {
    const response = await fetch("/api/tapes", { signal });
    if (!response.ok) {
        throw new Error(`Could not load tapes (${response.status} ${response.statusText})`);
    }
    console.error(response);

    const body: TapeResponse[] = await response.json();
    return body.map(toTape);
}
