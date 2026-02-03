package kg.notifications.gateway.exception;

import jakarta.servlet.http.HttpServletRequest;
import kg.notifications.gateway.exception.BadRequestException;
import kg.notifications.gateway.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        log.warn("BadRequest: {} {}", req.getMethod(), req.getRequestURI());
        ErrorResponse body = new ErrorResponse(ex.getMessage(), "BAD_REQUEST", OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        log.info("NotFound: {} {}", req.getMethod(), req.getRequestURI());
        ErrorResponse body = new ErrorResponse(ex.getMessage(), "NOT_FOUND", OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("IllegalArgument: {} {} - {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        ErrorResponse body = new ErrorResponse(ex.getMessage(), "BAD_REQUEST", OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation error: {} {} - {}", req.getMethod(), req.getRequestURI(), fieldErrors);
        ErrorResponse body = new ErrorResponse("Validation failed: " + fieldErrors, "VALIDATION_ERROR", OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception for {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse body = new ErrorResponse("Internal server error", "INTERNAL_ERROR", OffsetDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
