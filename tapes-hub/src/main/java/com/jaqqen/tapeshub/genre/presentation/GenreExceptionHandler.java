package com.jaqqen.tapeshub.genre.presentation;

import com.jaqqen.tapeshub.genre.domain.GenreInUseException;
import com.jaqqen.tapeshub.genre.domain.GenreNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Scoped to {@link GenreController} on purpose: a single application-wide advice would have to
 * import both modules' exception types, which is exactly the boundary this structure protects.
 * Framework-level failures stay in {@code config.ApiExceptionHandler}.
 */
@RestControllerAdvice(assignableTypes = GenreController.class)
class GenreExceptionHandler {

    @ExceptionHandler(GenreNotFoundException.class)
    ProblemDetail handleNotFound(GenreNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Genre not found");
        return problem;
    }

    /** 409: the request is well formed, the genre's current state is what refuses it. */
    @ExceptionHandler(GenreInUseException.class)
    ProblemDetail handleInUse(GenreInUseException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Genre in use");
        return problem;
    }
}
