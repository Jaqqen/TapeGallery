/**
 * Tapes are fetched from tapes-hub at runtime and mapped
 * into this shape by `src/api/tapes.ts`.
 *
 * `year` and `duration` are presentation-formatted strings, not raw data: the API
 * sends an ISO release date and a duration in milliseconds.
 */
export interface Tape {
    id: string;
    title: string;
    subtitle?: string;
    year: string;
    genre: string;
    duration: string;
    colors: {
        primary: string;
        secondary: string;
        accent: string;
        label: string;
    };
    pattern: "stripes" | "gradient" | "geometric" | "retro-blocks" | "waves" | "diamonds";
}
