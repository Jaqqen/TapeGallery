package com.jaqqen.tapeshub.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hands the CSRF token to clients that cannot read it out of a rendered form.
 *
 * <p>Standard approach: Token is bound to {@code JSESSIONID} ({@code HttpSessionCsrfTokenRepository}) and
 * has to be send by the existing {@code X-CSRF-TOKEN} header convention. It's not stored inside the cookie</p>
 */
@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        // {@code CsrfTokenArgumentResolver} supplies the XOR-encoded value that
        // {@code XorCsrfTokenRequestAttributeHandler} expects back - not the raw repository token.
        return new DefaultCsrfToken(token.getHeaderName(), token.getParameterName(), token.getToken());
    }
}
