package contracts.tapes

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description """
Creating a tape. The 201 carries the stored tape so the portal can put it straight on the shelf
without refetching, and the Location header has to be absolute - behind a reverse proxy that is
only true because of server.forward-headers-strategy in application-prod.yaml.
"""
    request {
        method POST()
        url "/api/tapes"
        headers {
            contentType applicationJson()
        }
        body(
            title      : "Neon Nights",
            subtitle   : "Director's Cut",
            releaseDate: "1987-01-01",
            genreId    : "11111111-1111-1111-1111-111111111111",
            duration   : 6840000,
            colors     : [
                primary  : "#ff2d95",
                secondary: "#00f0ff",
                accent   : "#ffe156",
                label    : "#1a1a2e"
            ],
            pattern    : "retro-blocks"
        )
    }
    response {
        status CREATED()
        headers {
            contentType applicationJson()
        }
        body(
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
        )
    }
}
