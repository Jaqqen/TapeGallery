package contracts.genres

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description """
The genre list. web-portal's AddTapeForm fills its dropdown from this and posts back the `id`, so
the pair (id, name) is the contract - the rest is carried for clients that want it.
"""
    request {
        method GET()
        url "/api/genres"
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([[
            id         : "11111111-1111-1111-1111-111111111111",
            name       : "Sci-Fi",
            description: "Spaceships and synthesizers",
            createdAt  : "2026-01-01T00:00:00Z",
            modifiedAt : "2026-01-02T00:00:00Z",
            deletedAt  : null
        ]])
    }
}
