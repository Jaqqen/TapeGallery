package com.jaqqen.tapeshub.support;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * Base for every REST-level integration test: a real Tomcat port, a real Postgres (shared across
 * every subclass with this exact set of annotations - see the note below), and real HTTP Basic
 * credentials travelling through the whole filter chain via {@link IntegrationSecurityConfiguration}.
 *
 * <p>Spring's test context cache keys on the merged configuration: any subclass that adds
 * {@code @MockitoBean}, {@code @TestPropertySource}, a different {@code @ActiveProfiles}, or an
 * extra {@code @Import} forks the cache key and starts a second Postgres container.
 * {@code SecurityConfigIT} does this deliberately, to see the real, un-overridden security chain.
 *
 * <p>State resets with a class-level {@code TRUNCATE} rather than {@code @Transactional}: the test
 * method and the Tomcat request thread run on different threads in different transactions, so a
 * test-side rollback would not undo anything the server already committed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("it")
@Import({TestcontainersConfiguration.class, IntegrationSecurityConfiguration.class})
@Sql(statements = "TRUNCATE TABLE tape, genre CASCADE", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class ApiIntegrationTest {

    protected static final String USER = "integration";
    protected static final String PASSWORD = "integration";

    @Autowired
    protected RestTestClient client;

    /** Adds the credentials {@link IntegrationSecurityConfiguration} accepts to any request. */
    protected static void authenticate(HttpHeaders headers) {
        headers.setBasicAuth(USER, PASSWORD);
    }

    protected RestTestClient.ResponseSpec authedGet(String uri, Object... uriVariables) {
        return client.get().uri(uri, uriVariables)
            .headers(ApiIntegrationTest::authenticate)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedGet(URI uri) {
        return client.get().uri(uri)
            .headers(ApiIntegrationTest::authenticate)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedPost(String uri, Object body) {
        return client.mutate().build().post().uri(uri)
            .headers(ApiIntegrationTest::authenticate)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedPut(String uri, Object body, Object... uriVariables) {
        return client.put().uri(uri, uriVariables)
            .headers(ApiIntegrationTest::authenticate)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedPatch(String uri, Object body, Object... uriVariables) {
        return client.patch().uri(uri, uriVariables)
            .headers(ApiIntegrationTest::authenticate)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedDelete(String uri, Object... uriVariables) {
        return client.delete().uri(uri, uriVariables)
            .headers(ApiIntegrationTest::authenticate)
            .exchange();
    }

    /** The schema starts empty: a tape has nowhere to point until a genre exists. */
    protected UUID createGenre(String name) {
        GenreDetails genre = authedPost("/api/genres", TapeRequests.genre(name))
            .expectStatus().isCreated()
            .expectBody(GenreDetails.class)
            .returnResult()
            .getResponseBody();
        return Objects.requireNonNull(genre).id();
    }

    protected UUID createTape(UUID genreId, String title) {
        TapeResponse tape = authedPost("/api/tapes", TapeRequests.tape(genreId, title))
            .expectStatus().isCreated()
            .expectBody(TapeResponse.class)
            .returnResult()
            .getResponseBody();
        return Objects.requireNonNull(tape).id();
    }
}
