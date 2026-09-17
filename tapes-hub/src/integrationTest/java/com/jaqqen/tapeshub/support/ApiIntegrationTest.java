package com.jaqqen.tapeshub.support;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * Base for every REST-level integration test: Tomcat + Postgres, HTTP Basic
 * credentials and CSRF token exchange
 *
 * <p>Executes proper requests against project actual filter chain.</p>
 *
 * <p>
 *     Spring's test context cache keys on the merged configuration: any subclass that adds
 *     {@code @MockitoBean}, {@code @TestPropertySource}, a different {@code @ActiveProfiles}, or an
 *     extra {@code @Import} forks the cache key and starts a second Postgres container.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Sql(statements = "TRUNCATE TABLE tape, genre CASCADE", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class ApiIntegrationTest {

    protected static final String SESSION_COOKIE = "JSESSIONID";
    protected static final String CSRF_ENDPOINT = "/api/csrf";

    @Value("${spring.security.user.name}")
    protected String user;

    @Value("${spring.security.user.password}")
    protected String password;

    @Autowired
    protected RestTestClient client;

    /** The body {@code CsrfController} returns. */
    protected record CsrfTokenResponse(String token, String parameterName, String headerName) {
    }

    /**
     * A token and the session it is bound to. The default {@code HttpSessionCsrfTokenRepository}
     * keeps the token in the session.
     */
    protected record Csrf(CsrfTokenResponse token, String sessionId) {
    }

    protected void basicAuthHeader(HttpHeaders headers) {
        headers.setBasicAuth(user, password);
    }

    /**
     * Fetches a token and the session holding it, the way a browser client would.
     *
     * <p>Done afresh for every write per test and not cached. HTTP Basic re-authenticates on
     * each request, and {@code CsrfAuthenticationStrategy} rotates the token whenever it does. Using the token  across two writes will result in one to fail.
     */
    protected Csrf csrf() {
        var result = client.get().uri(CSRF_ENDPOINT)
            .headers(this::basicAuthHeader)
            .exchange()
            .expectStatus().isOk()
            .expectBody(CsrfTokenResponse.class)
            .returnResult();

        ResponseCookie session = result.getResponseCookies().getFirst(SESSION_COOKIE);
        return new Csrf(
            Objects.requireNonNull(result.getResponseBody(), "no CSRF token in the response body"),
            Objects.requireNonNull(session, "no " + SESSION_COOKIE + " on the CSRF response").getValue());
    }

    /**
     * Sends the request with credentials, the CSRF header, and the session the token belongs to.
     */
    private RestTestClient.ResponseSpec authorizedExchange(RestTestClient.RequestHeadersSpec<?> spec) {
        Csrf csrf = csrf();
        return spec
            .headers(this::basicAuthHeader)
            .header(csrf.token().headerName(), csrf.token().token())
            .cookie(SESSION_COOKIE, csrf.sessionId())
            .exchange();
    }

    private static RestTestClient.RequestHeadersSpec<?> json(
        RestTestClient.RequestBodySpec spec, Object body) {
        return spec.contentType(MediaType.APPLICATION_JSON).body(body);
    }

    protected RestTestClient.ResponseSpec authedGet(String uri, Object... uriVariables) {
        return client.get().uri(uri, uriVariables)
            .headers(this::basicAuthHeader)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedGet(URI uri) {
        return client.get().uri(uri)
            .headers(this::basicAuthHeader)
            .exchange();
    }

    protected RestTestClient.ResponseSpec authedPost(String uri, Object body) {
        return authorizedExchange(json(client.post().uri(uri), body));
    }

    protected RestTestClient.ResponseSpec authedPut(String uri, Object body, Object... uriVariables) {
        return authorizedExchange(json(client.put().uri(uri, uriVariables), body));
    }

    protected RestTestClient.ResponseSpec authedPatch(String uri, Object body, Object... uriVariables) {
        return authorizedExchange(json(client.patch().uri(uri, uriVariables), body));
    }

    protected RestTestClient.ResponseSpec authedDelete(String uri, Object... uriVariables) {
        return authorizedExchange(client.delete().uri(uri, uriVariables));
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
