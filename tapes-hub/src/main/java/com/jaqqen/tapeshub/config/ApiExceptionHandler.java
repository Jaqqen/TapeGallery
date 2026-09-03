package com.jaqqen.tapeshub.config;

import com.jaqqen.tapeshub.shared.InvalidIdentifierException;
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
 *
 * <p>It deliberately imports no feature module's internals - only the identifier shared kernel,
 * which exists so cross-cutting types like {@link InvalidIdentifierException} do not have to live
 * inside {@code tape} or {@code genre}. What a missing tape or a missing genre means is each
 * module's own business, handled by its own advice next to its controller.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Thrown by the value objects when a payload passes bean validation but violates an invariant. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    /** Thrown by TapeId/GenreId when constructed without a UUID - a missing identifier, not a malformed one. */
    @ExceptionHandler(InvalidIdentifierException.class)
    public ProblemDetail handleInvalidIdentifier(InvalidIdentifierException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid identifier", ex.getMessage());
    }

    /** A path segment that is not a UUID, e.g. GET /api/tapes/neon-nights. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request",
            "'" + ex.getValue() + "' is not a valid identifier");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .sorted()
            .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed",
            "The request body is not valid");
        problem.setProperty("errors", errors);
        return problem;
    }

    /** Malformed JSON, or an unknown value for a closed field such as {@code pattern}. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request body", ex.getMostSpecificCause().getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
