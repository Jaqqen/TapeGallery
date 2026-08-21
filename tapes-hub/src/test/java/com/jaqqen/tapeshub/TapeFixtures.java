package com.jaqqen.tapeshub;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapeColors;
import com.jaqqen.tapeshub.domain.tape.TapePattern;

/** Shared test data, taken verbatim from the web-portal's tapes.ts. */
public final class TapeFixtures {

    public static final String NEON_NIGHTS_JSON = """
            {
              "title": "NEON NIGHTS",
              "subtitle": "The City Never Sleeps",
              "year": "1987",
              "genre": "Action",
              "duration": "1h 54min",
              "rating": "R",
              "description": "A rogue detective navigates the neon-lit streets.",
              "colors": {"primary": "#ff006e", "secondary": "#8338ec", "accent": "#ffbe0b", "label": "#1a1a2e"},
              "pattern": "stripes"
            }""";

    public static Tape neonNights() {
        return new Tape(
                "neon-nights",
                "NEON NIGHTS",
                "The City Never Sleeps",
                "1987",
                "Action",
                "1h 54min",
                "R",
                "A rogue detective navigates the neon-lit streets.",
                new TapeColors("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e"),
                TapePattern.STRIPES
        );
    }

    private TapeFixtures() {
    }
}
