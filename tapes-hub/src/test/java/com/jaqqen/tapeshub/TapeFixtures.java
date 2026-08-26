package com.jaqqen.tapeshub;

/** Shared test data, taken verbatim from the web-portal's tapes.ts. */
public final class TapeFixtures {

//    /** Fixed so tests can assert on Location headers and payloads without capturing. */
//    public static final UUID NEON_NIGHTS_ID;
//    public static final UUID PHANTOM_SIGNAL_ID;
//    public static final UUID NEON_NIGHTS_COLORS_ID;
//    public static final UUID PHANTOM_SIGNAL_COLORS_ID;
//
//    static {
//        final Function<Integer, UUID> toUUID = uuidStart -> UUID.fromString(uuidStart + "-1111-4111-8111-111111111111");
//        NEON_NIGHTS_ID = toUUID.apply(11111111);
//        NEON_NIGHTS_COLORS_ID = toUUID.apply(11111112);
//        PHANTOM_SIGNAL_ID = toUUID.apply(11111113);
//        PHANTOM_SIGNAL_COLORS_ID = toUUID.apply(11111114);
//
//    }
//    /** A request body: no id - the server mints it. */
//    public static final String NEON_NIGHTS_JSON = """
//            {
//              "title": "NEON NIGHTS",
//              "subtitle": "The City Never Sleeps",
//              "year": "1987",
//              "genre": "Action",
//              "duration": "1h 54min",
//              "rating": "R",
//              "description": "A rogue detective navigates the neon-lit streets.",
//              "colors": {"primary": "#ff006e", "secondary": "#8338ec", "accent": "#ffbe0b", "label": "#1a1a2e"},
//              "pattern": "stripes"
//            }""";
//
//    /** Mirrors {@link #NEON_NIGHTS_JSON}, so request and expectation cannot drift apart. */
//    public static Tape neonNights() {
//        return new Tape(
//                NEON_NIGHTS_ID,
//                "NEON NIGHTS",
//                "The City Never Sleeps",
//                "1987",
//                "Action",
//                "1h 54min",
//                "R",
//                "A rogue detective navigates the neon-lit streets.",
//                new TapeColors(NEON_NIGHTS_COLORS_ID,"#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e"),
//                TapePattern.STRIPES
//        );
//    }
//
//    public static Tape phantomSignal() {
//        return new Tape(
//                PHANTOM_SIGNAL_ID,
//                "PHANTOM SIGNAL",
//                "They Are Listening",
//                "1992",
//                "Sci-Fi",
//                "1h 44min",
//                "PG-13",
//                "Scientists at a remote radio telescope pick up an alien signal that seems to contain a warning — but decoding it may already be too late.",
//                new TapeColors("#06d6a0", "#118ab2", "#ef476f", "#073b4c"),
//                TapePattern.WAVES
//        );
//    }
//    private TapeFixtures() {
//    }
}
