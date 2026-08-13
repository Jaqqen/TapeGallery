export interface Tape {
    id: string;
    title: string;
    subtitle?: string;
    year: string;
    genre: string;
    duration: string;
    rating: string;
    description: string;
    colors: {
        primary: string;
        secondary: string;
        accent: string;
        label: string;
    };
    pattern: "stripes" | "gradient" | "geometric" | "retro-blocks" | "waves" | "diamonds";
}

export const tapes: Tape[] = [
    {
        id: "neon-nights",
        title: "NEON NIGHTS",
        subtitle: "The City Never Sleeps",
        year: "1987",
        genre: "Action",
        duration: "1h 54min",
        rating: "R",
        description: "A rogue detective navigates the neon-lit streets of a dystopian metropolis to uncover a conspiracy that reaches the highest levels of power.",
        colors: {primary: "#ff006e", secondary: "#8338ec", accent: "#ffbe0b", label: "#1a1a2e"},
        pattern: "stripes",
    },
    {
        id: "chrome-horizon",
        title: "CHROME HORIZON",
        subtitle: "Beyond the Last Frontier",
        year: "1984",
        genre: "Sci-Fi",
        duration: "2h 12min",
        rating: "PG-13",
        description: "In the year 2099, a crew of space pioneers embarks on a journey to the edge of the known universe aboard the starship Horizon.",
        colors: {primary: "#00b4d8", secondary: "#0077b6", accent: "#90e0ef", label: "#023e8a"},
        pattern: "gradient",
    },
    {
        id: "velvet-thunder",
        title: "VELVET THUNDER",
        year: "1989",
        genre: "Thriller",
        duration: "1h 38min",
        rating: "R",
        description: "A jazz musician stumbles into the criminal underworld when she witnesses a murder at an underground club in downtown Los Angeles.",
        colors: {primary: "#9b5de5", secondary: "#f15bb5", accent: "#fee440", label: "#240046"},
        pattern: "geometric",
    },
    {
        id: "solar-burn",
        title: "SOLAR BURN",
        subtitle: "No Escape from the Heat",
        year: "1986",
        genre: "Action",
        duration: "1h 47min",
        rating: "R",
        description: "When a solar flare knocks out the power grid, an ex-military survivalist must protect a small desert town from marauding raiders.",
        colors: {primary: "#ff7b00", secondary: "#ff0000", accent: "#ffdd00", label: "#3d0000"},
        pattern: "retro-blocks",
    },
    {
        id: "midnight-frequency",
        title: "MIDNIGHT FREQUENCY",
        year: "1991",
        genre: "Horror",
        duration: "1h 32min",
        rating: "R",
        description: "A late-night radio host begins receiving mysterious signals that predict horrifying events — events that only she can prevent.",
        colors: {primary: "#2d00f7", secondary: "#6a00f4", accent: "#e500a4", label: "#0a0a23"},
        pattern: "waves",
    },
    {
        id: "turbo-kid",
        title: "TURBO KID",
        subtitle: "Full Speed Ahead",
        year: "1988",
        genre: "Adventure",
        duration: "1h 42min",
        rating: "PG",
        description: "A teenage BMX champion discovers a hidden portal to a radical dimension where he must race against time to save both worlds.",
        colors: {primary: "#00f5d4", secondary: "#00bbf9", accent: "#f15bb5", label: "#0b132b"},
        pattern: "stripes",
    },
    {
        id: "steel-rain",
        title: "STEEL RAIN",
        year: "1985",
        genre: "War",
        duration: "2h 05min",
        rating: "R",
        description: "In the trenches of a futuristic battlefield, a platoon of mech-suit soldiers fights for survival against an unstoppable enemy force.",
        colors: {primary: "#606c38", secondary: "#283618", accent: "#dda15e", label: "#1b1b1b"},
        pattern: "diamonds",
    },
    {
        id: "crystal-palace",
        title: "CRYSTAL PALACE",
        subtitle: "A World of Wonder",
        year: "1990",
        genre: "Fantasy",
        duration: "1h 56min",
        rating: "PG",
        description: "A young girl discovers a magical crystal that transports her to an enchanted kingdom, where she must restore the balance of light and shadow.",
        colors: {primary: "#d4a5ff", secondary: "#a855f7", accent: "#67e8f9", label: "#1e1b4b"},
        pattern: "geometric",
    },
    {
        id: "red-line",
        title: "RED LINE",
        year: "1983",
        genre: "Crime",
        duration: "1h 49min",
        rating: "R",
        description: "An undercover cop infiltrates a ruthless street racing syndicate, but the deeper she goes, the harder it becomes to remember which side she's on.",
        colors: {primary: "#dc2626", secondary: "#991b1b", accent: "#fbbf24", label: "#1c1917"},
        pattern: "retro-blocks",
    },
    {
        id: "phantom-signal",
        title: "PHANTOM SIGNAL",
        subtitle: "They Are Listening",
        year: "1992",
        genre: "Sci-Fi",
        duration: "1h 44min",
        rating: "PG-13",
        description: "Scientists at a remote radio telescope pick up an alien signal that seems to contain a warning — but decoding it may already be too late.",
        colors: {primary: "#06d6a0", secondary: "#118ab2", accent: "#ef476f", label: "#073b4c"},
        pattern: "waves",
    },
    {
        id: "desert-mirage",
        title: "DESERT MIRAGE",
        year: "1986",
        genre: "Western",
        duration: "2h 01min",
        rating: "PG-13",
        description: "A lone drifter rides into a ghost town on the edge of the Mojave, searching for a legendary treasure that may not even exist.",
        colors: {primary: "#e9c46a", secondary: "#f4a261", accent: "#e76f51", label: "#264653"},
        pattern: "gradient",
    },
    {
        id: "black-ice",
        title: "BLACK ICE",
        subtitle: "Cold-Blooded Justice",
        year: "1993",
        genre: "Thriller",
        duration: "1h 51min",
        rating: "R",
        description: "A forensic investigator in Anchorage tracks a serial killer whose victims are only found when the spring thaw reveals what winter buried.",
        colors: {primary: "#94a3b8", secondary: "#475569", accent: "#38bdf8", label: "#0f172a"},
        pattern: "diamonds",
    },
];
