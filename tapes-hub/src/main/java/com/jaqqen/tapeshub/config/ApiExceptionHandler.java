package com.jaqqen.tapeshub.config;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Framework-level failures that any endpoint can produce, and nothing else.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Thrown by the value objects when a payload passes bean validation but violates an invariant. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem("Invalid request", ex.getMessage());
    }

    /** A path segment that is not a UUID, e.g. GET /api/tapes/neon-nights. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem("Invalid request",
            "'" + ex.getValue() + "' is not a valid identifier");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .sorted()
            .toList();
        ProblemDetail problem = problem("Validation failed",
            "The request body is not valid");
        problem.setProperty("errors", errors);
        return problem;
    }

    /** Malformed JSON, or an unknown value for a closed field such as {@code pattern}. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem("Malformed request body", ex.getMostSpecificCause().getMessage());
    }

    private static ProblemDetail problem(String title, @Nullable String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(title);
        return problem;
    }
}
