package contracts.tapes

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description """
Posting a tape whose genre does not exist. web-portal can reach this for real: AddTapeForm fills its
dropdown once on mount, so a genre deleted between that load and the submit arrives here.

The status is the point. 422 rather than 404 is a deliberate choice - the tape URL is fine, the body
names a genre that is not there - and a status code is exactly the kind of thing COMPATIBILITY.md
counts as breaking to change. This pins it.
"""
    request {
        method POST()
        url "/api/tapes"
        headers {
            contentType applicationJson()
        }
        body(
            title      : "Neon Nights",
            releaseDate: "1987-01-01",
            genreId    : "99999999-9999-9999-9999-999999999999",
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
        status 422
        body(
            title   : "Unknown genre",
            status  : 422,
            detail  : "No genre with id '99999999-9999-9999-9999-999999999999'",
            instance: "/api/tapes"
        )
    }
}
