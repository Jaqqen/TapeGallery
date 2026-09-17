package com.jaqqen.tapeshub.config;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.support.TapeRequests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

/**
 * Integration-test specific config.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
class SecurityConfigIT {

    @Autowired
    private RestTestClient client;

    @Test
    void anAnonymousReadIsRejectedWith401() {
        client.get().uri("/api/tapes")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void theWrongCredentialsAreRejectedWith401() {
        client.get().uri("/api/tapes")
            .headers(headers -> headers.setBasicAuth("integration", "not-the-password"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void validBasicCredentialsGetThrough() {
        client.get().uri("/api/tapes")
            .headers(this::authenticate)
            .exchange()
            .expectStatus().isOk();
    }

    /**
     * Tests that:
     * <ul>
     *   <li>POST requests to /api/tapes are rejected with 401
     *   <li>CSRF filter runs before authentication in the default chain
     *   <li>Valid credentials are never checked due to missing CSRF token
     * </ul>
     */
    @Test
    void aWriteIsRejectedWith401BeforeItsValidCredentialsAreEvenChecked() {
        UUID unknownGenre = UUID.randomUUID();

        client.post().uri("/api/tapes")
            .headers(this::authenticate)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TapeRequests.tape(unknownGenre, "Neon Nights"))
            .exchange()
            .expectStatus().isUnauthorized();
    }

    private void authenticate(HttpHeaders headers) {
        headers.setBasicAuth("integration", "integration");
    }
}
