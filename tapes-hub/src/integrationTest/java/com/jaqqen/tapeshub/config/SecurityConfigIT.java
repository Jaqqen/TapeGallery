package com.jaqqen.tapeshub.config;

import com.jaqqen.tapeshub.support.ApiIntegrationTest;
import com.jaqqen.tapeshub.support.TapeRequests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

/**
 * Tests that:
 * <ul>
 *   <li>Authentication is enforced on all endpoints
 *   <li>Invalid credentials are rejected
 *   <li>CSRF tokens are required for write operations
 *   <li>Tokens are bound to their session
 *   <li>Valid credentials with correct CSRF tokens are allowed through
 * </ul>
 */
class SecurityConfigIT extends ApiIntegrationTest {

    @Test
    void anAnonymousReadIsRejectedWith401() {
        client.get().uri("/api/tapes")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void theWrongCredentialsAreRejectedWith401() {
        client.get().uri("/api/tapes")
            .headers(headers -> headers.setBasicAuth(user, "not-the-password"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void validBasicCredentialsGetThrough() {
        client.get().uri("/api/tapes")
            .headers(this::basicAuthHeader)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void theTokenEndpointIsItselfBehindAuthentication() {
        client.get().uri(CSRF_ENDPOINT)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    /**
     * Tests that:
     * <ul>
     *   <li>POST requests without a CSRF token are rejected with 403 (not 401)
     *   <li>Rejection happens before credentials are validated
     *   <li>CSRF failure is treated as access denied, not an authentication failure like 401
     * </ul>
    */
    @Test
    void aWriteWithoutATokenIsRejectedBeforeItsValidCredentialsAreEvenChecked() {
        UUID unknownGenre = UUID.randomUUID();

        client.post().uri("/api/tapes")
            .headers(this::basicAuthHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TapeRequests.tape(unknownGenre, "Neon Nights"))
            .exchange()
            .expectStatus().isForbidden();
    }

    /** The token is kept in the session, so presenting it without that session proves nothing. */
    @Test
    void aWriteCarryingTheTokenButNotItsSessionIsRejected() {
        Csrf csrf = csrf();

        client.post().uri("/api/genres")
            .headers(this::basicAuthHeader)
            .header(csrf.token().headerName(), csrf.token().token())
            .contentType(MediaType.APPLICATION_JSON)
            .body(TapeRequests.genre("Horror"))
            .exchange()
            .expectStatus().isForbidden();
    }

    /** A token from one session is not a token for another. */
    @Test
    void aWriteCarryingAnotherSessionsTokenIsRejected() {
        Csrf mine = csrf();
        Csrf someoneElse = csrf();

        client.post().uri("/api/genres")
            .headers(this::basicAuthHeader)
            .header(mine.token().headerName(), someoneElse.token().token())
            .cookie(SESSION_COOKIE, mine.sessionId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(TapeRequests.genre("Horror"))
            .exchange()
            .expectStatus().isForbidden();
    }

    /** The positive half: token and session together, and the write lands. */
    @Test
    void aWriteCarryingBothTheTokenAndItsSessionSucceeds() {
        Csrf csrf = csrf();

        client.post().uri("/api/genres")
            .headers(this::basicAuthHeader)
            .header(csrf.token().headerName(), csrf.token().token())
            .cookie(SESSION_COOKIE, csrf.sessionId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(TapeRequests.genre("Horror"))
            .exchange()
            .expectStatus().isCreated();
    }
}
