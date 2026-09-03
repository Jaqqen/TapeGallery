package com.jaqqen.tapeshub.tape.presentation;

import com.jaqqen.tapeshub.tape.app.UnknownGenreException;
import com.jaqqen.tapeshub.tape.domain.TapeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Scoped to {@link TapeController} on purpose: a single application-wide advice would have to import
 * both modules' exception types, which is exactly the boundary this structure protects.
 * Framework-level failures stay in {@code config.ApiExceptionHandler}.
 */
@RestControllerAdvice(assignableTypes = TapeController.class)
class TapeExceptionHandler {

    @ExceptionHandler(TapeNotFoundException.class)
    ProblemDetail handleNotFound(TapeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Tape not found");
        return problem;
    }

    /**
     * 422, not 404: the tape URL is fine, the body just names a genre that does not exist. A tape
     * cannot be stored without a genre, so there is nothing to fall back to.
     */
    @ExceptionHandler(UnknownGenreException.class)
    ProblemDetail handleUnknownGenre(UnknownGenreException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Unknown genre");
        return problem;
    }
}
