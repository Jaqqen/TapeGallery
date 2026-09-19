import {type ChangeEvent, useCallback, useEffect, useState} from "react";
import {motion} from "framer-motion";
import {Duration} from "luxon";
import type {CSSProperties, SubmitEvent } from "react";
import type {TargetAndTransition} from "motion-dom";
import {createTape, fetchGenres} from "../api/tapes";
import type {GenreDetails, NewTape} from "../api/tapes";
import type {Tape} from "../data/tapes";

interface AddTapeFormProps {
    onCreated: (tape: Tape) => void;
    onClose: () => void;
}

interface InitStateProps {
    form: {
        overlay: TargetAndTransition;
        panel: TargetAndTransition;
    };
}

const initState: InitStateProps = {
    form: {
        overlay: {opacity: 0},
        panel: {opacity: 0, y: 30, scale: 0.97}
    }
}

const PATTERNS: Tape["pattern"][] = [
    "stripes",
    "gradient",
    "geometric",
    "retro-blocks",
    "waves",
    "diamonds",
];

interface DraftTape {
    title: string;
    subtitle: string;
    releaseDate: string;
    genreId: string;
    hours: string;
    minutes: string;
    pattern: Tape["pattern"];
    colors: Tape["colors"];
}

const emptyDraft: DraftTape = {
    title: "",
    subtitle: "",
    releaseDate: "",
    genreId: "",
    hours: "1",
    minutes: "30",
    pattern: "stripes",
    colors: {
        primary: "#ff2d95",
        secondary: "#00f0ff",
        accent: "#ffe156",
        label: "#1a1a2e",
    },
};

const COLOR_FIELDS: { key: keyof Tape["colors"]; label: string }[] = [
    {key: "primary", label: "Primary"},
    {key: "secondary", label: "Secondary"},
    {key: "accent", label: "Accent"},
    {key: "label", label: "Label"},
];

export default function AddTapeForm({onCreated, onClose}: AddTapeFormProps) {
    const [draft, setDraft] = useState<DraftTape>(emptyDraft);
    const [genres, setGenres] = useState<GenreDetails[]>([]);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // Same reason as the shelf: StrictMode double-invokes effects in dev.
        const controller = new AbortController();

        fetchGenres(controller.signal)
            .then((loaded: GenreDetails[]) => {
                setGenres(loaded);
                // Preselect, so the required field is never silently empty.
                setDraft((prev) => prev.genreId ? prev : {...prev, genreId: loaded[0]?.id ?? ""});
            })
            .catch((cause: unknown) => {
                if (cause instanceof DOMException && cause.name === "AbortError") {
                    return;
                }
                setError(cause instanceof Error ? cause.message : "Could not load genres.");
            });

        return () => controller.abort();
    }, []);

    useEffect(() => {
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onClose();
            }
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [onClose]);

    const update = useCallback(<K extends keyof DraftTape>(key: K, value: DraftTape[K]) => {
        setDraft((prev) => ({...prev, [key]: value}));
    }, []);

    const updateColor = useCallback((key: keyof Tape["colors"], value: string) => {
        setDraft((prev) => ({...prev, colors: {...prev.colors, [key]: value}}));
    }, []);

    const handleSubmit = useCallback((event: SubmitEvent<HTMLFormElement>): void => {
        event.preventDefault();

        // The API stores a runtime in milliseconds and rejects anything that is not positive,
        // so the two inputs are folded back into one number here.
        const duration: number = Duration.fromObject({
            hours: Number(draft.hours) || 0,
            minutes: Number(draft.minutes) || 0,
        }).toMillis();

        if (duration <= 0) {
            setError("Runtime must be longer than zero.");
            return;
        }

        const payload: NewTape = {
            title: draft.title.trim(),
            // The column is nullable, so an untouched field must not become an empty string.
            subtitle: draft.subtitle.trim() || undefined,
            releaseDate: draft.releaseDate,
            genreId: draft.genreId,
            duration,
            colors: draft.colors,
            pattern: draft.pattern,
        };

        setIsSaving(true);
        setError(null);

        createTape(payload)
            .then((created: Tape) => {
                onCreated(created);
                onClose();
            })
            .catch((cause: unknown) => {
                setError(cause instanceof Error ? cause.message : "Could not save the tape.");
                setIsSaving(false);
            });
    }, [draft, onCreated, onClose]);

    return (
        <motion.div
            className="form-overlay"
            initial={initState.form.overlay}
            animate={{opacity: 1}}
            exit={initState.form.overlay}
            transition={{duration: 0.2}}
            onClick={onClose}
        >
            <motion.form
                className="form-panel"
                initial={initState.form.panel}
                animate={{opacity: 1, y: 0, scale: 1}}
                exit={initState.form.panel}
                transition={{duration: 0.25, ease: "easeOut"}}
                onClick={(event) => event.stopPropagation()}
                onSubmit={handleSubmit}
                style={{
                    "--tape-primary": draft.colors.primary,
                    "--tape-secondary": draft.colors.secondary,
                    "--tape-accent": draft.colors.accent,
                } as CSSProperties}
            >
                <button type="button" className="detail-close form-close" onClick={onClose}>
                    &times;
                </button>

                <h2 className="form-title">New Tape</h2>
                <p className="form-hint">Everything but the subtitle is required</p>

                <div className="form-grid">
                    <label className="form-field form-field-wide">
                        <span className="form-label">Title</span>
                        <input
                            className="form-input"
                            value={draft.title}
                            onChange={(e) => update("title", e.target.value)}
                            placeholder="Neon Nights"
                            maxLength={120}
                            required
                            autoFocus
                        />
                    </label>

                    <label className="form-field form-field-wide">
                        <span className="form-label">Subtitle</span>
                        <input
                            className="form-input"
                            value={draft.subtitle}
                            onChange={(e) => update("subtitle", e.target.value)}
                            placeholder="Optional"
                            maxLength={120}
                        />
                    </label>

                    <label className="form-field">
                        <span className="form-label">Release date</span>
                        <input
                            type="date"
                            className="form-input"
                            value={draft.releaseDate}
                            onChange={(e: ChangeEvent<HTMLInputElement>) => update("releaseDate", e.target.value)}
                            required
                        />
                    </label>

                    <label className="form-field">
                        <span className="form-label">Genre</span>
                        <select
                            className="form-input"
                            value={draft.genreId}
                            onChange={(e: ChangeEvent<HTMLSelectElement>) => update("genreId", e.target.value)}
                            required
                        >
                            {genres.length === 0 && <option value="">No genres available</option>}
                            {genres.map((genre) => (
                                <option key={genre.id} value={genre.id}>{genre.name}</option>
                            ))}
                        </select>
                    </label>

                    <div className="form-field">
                        <span className="form-label">Runtime</span>
                        <div className="form-duration">
                            <input
                                type="number"
                                className="form-input"
                                value={draft.hours}
                                onChange={(e: ChangeEvent<HTMLInputElement>) => update("hours", e.target.value)}
                                min={0}
                                max={23}
                                aria-label="Runtime hours"
                            />
                            <span className="form-unit">h</span>
                            <input
                                type="number"
                                className="form-input"
                                value={draft.minutes}
                                onChange={(e:ChangeEvent<HTMLInputElement>) => update("minutes", e.target.value)}
                                min={0}
                                max={59}
                                aria-label="Runtime minutes"
                            />
                            <span className="form-unit">min</span>
                        </div>
                    </div>

                    <label className="form-field">
                        <span className="form-label">Pattern</span>
                        <select
                            className="form-input"
                            value={draft.pattern}
                            onChange={(e:ChangeEvent<HTMLSelectElement>) => update("pattern", e.target.value as Tape["pattern"])}
                        >
                            {PATTERNS.map((pattern) => (
                                <option key={pattern} value={pattern}>{pattern}</option>
                            ))}
                        </select>
                    </label>

                    <div className="form-field form-field-wide">
                        <span className="form-label">Colors</span>
                        <div className="form-colors">
                            {COLOR_FIELDS.map(({key, label}) => (
                                <label key={key} className="form-color">
                                    <input
                                        type="color"
                                        className="form-color-input"
                                        value={draft.colors[key]}
                                        onChange={(e:ChangeEvent<HTMLInputElement>) => updateColor(key, e.target.value)}
                                        aria-label={`${label} colour`}
                                    />
                                    <span className="form-color-name">{label}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                </div>

                {error && <p className="form-error">{error}</p>}

                <div className="form-actions">
                    <button type="submit" className="detail-btn detail-btn-play" disabled={isSaving}>
                        {isSaving ? "Saving…" : "Add to Shelf"}
                    </button>
                    <button type="button" className="detail-btn detail-btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                </div>
            </motion.form>
        </motion.div>
    );
}
