package contracts.tapes

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description """
The shelf. web-portal calls this on mount and maps it into its own Tape shape, so every field
below is load-bearing: `releaseDate` is sliced for the year, `genre` is flattened to its name, and
`duration` is formatted from milliseconds. Removing or renaming one breaks a deployed portal that
has not been rebuilt - which, shipping these two separately, is the normal case.
"""
    request {
        method GET()
        url "/api/tapes"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
                [
                    id          : "22222222-2222-2222-2222-222222222222",
                    title       : "Neon Nights",
                    subtitle    : "Director's Cut",
                    releaseDate : "1987-01-01",
                    genre       : [
                        id         : "11111111-1111-1111-1111-111111111111",
                        name       : "Sci-Fi",
                        description: "Spaceships and synthesizers",
                        createdAt  : "2026-01-01T00:00:00Z",
                        modifiedAt : "2026-01-02T00:00:00Z",
                        deletedAt  : null
                    ],
                    duration    : 6840000,
                    colors      : [
                        primary  : "#ff2d95",
                        secondary: "#00f0ff",
                        accent   : "#ffe156",
                        label    : "#1a1a2e"
                    ],
                    pattern     : "retro-blocks",
                    createdAt   : "2026-01-01T00:00:00Z",
                    modifiedAt  : "2026-01-02T00:00:00Z",
                    deletedAt   : null
                ],
                [
                        id          : "33333333-3333-3333-3333-333333333333",
                        title       : "Blooming Desert",
                        subtitle    : "Manager's budget",
                        releaseDate : "2001-11-12",
                        genre       : [
                                id         : "11111111-1111-1111-1111-111111111111",
                                name       : "Sci-Fi",
                                description: "Spaceships and synthesizers",
                                createdAt  : "2026-01-01T00:00:00Z",
                                modifiedAt : "2026-01-02T00:00:00Z",
                                deletedAt  : null
                        ],
                        duration    : 5_540_000,
                        colors      : [
                                primary  : "#ff2d95",
                                secondary: "#00f0ff",
                                accent   : "#ffe156",
                                label    : "#1a1a2e"
                        ],
                        pattern     : "diamonds",
                        createdAt   : "2026-01-01T00:00:00Z",
                        modifiedAt  : "2026-01-02T00:00:00Z",
                        deletedAt   : null
                ],
        ])
    }
}
