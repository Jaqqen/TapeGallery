package contracts.tapes

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description """
A rejected tape. This is the error shape web-portal actually renders: `toErrorMessage` in
src/api/tapes.ts reads `errors` first and joins it into the red line under the form, falling back to
`detail`. Both of those, and the RFC 9457 wrapper around them, are therefore part of the contract -
dropping `errors` would leave the form showing a bare status code.

The messages themselves are bean-validation defaults, so they are matched by pattern rather than
pinned: which fields failed is the contract, the library's wording for it is not.
"""
    request {
        method POST()
        url "/api/tapes"
        headers {
            contentType applicationJson()
        }
        body(
            title      : "",
            releaseDate: "1987-01-01",
            genreId    : "11111111-1111-1111-1111-111111111111",
            duration   : 0,
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
        status BAD_REQUEST()
        body(
            title   : "Validation failed",
            status  : 400,
            detail  : "The request body is not valid",
            instance: "/api/tapes",
            // Sorted by the handler, so the order is stable and can be asserted position by position.
            errors  : ["duration must be greater than 0", "title must not be blank"]
        )
        bodyMatchers {
            jsonPath('$.errors[0]', byRegex('duration .+'))
            jsonPath('$.errors[1]', byRegex('title .+'))
        }
    }
}
