package io.github.raissaamaral.mapadevagas.exception;

import io.github.raissaamaral.mapadevagas.application.ApplicationNotFoundException;
import io.github.raissaamaral.mapadevagas.application.InvalidStatusChangeException;
import io.github.raissaamaral.mapadevagas.user.EmailAlreadyRegisteredException;
import io.github.raissaamaral.mapadevagas.user.InvalidCredentialsException;
import io.github.raissaamaral.mapadevagas.user.TooManyLoginAttemptsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiError error = new ApiError(400, "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Never return ex.getMessage() here: it comes from the JSON parser and
    // may expose internal class and package names to the client
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedBody(HttpMessageNotReadableException ex) {
        ApiError error = new ApiError(400, "Malformed request body", Map.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Safe to return ex.getMessage(): the message is defined in
    // ApplicationNotFoundException and only contains the requested id
    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ApplicationNotFoundException ex) {
        ApiError error = new ApiError(404, ex.getMessage(), Map.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ex.getName() is safe (parameter name defined in the controller);
    // ex.getMessage() is not (mentions internal Java types)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiError error = new ApiError(400, "Invalid value for parameter: " + ex.getName(), Map.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Safe to return ex.getMessage(): the message is defined in
    // InvalidStatusChangeException and only contains the status
    @ExceptionHandler(InvalidStatusChangeException.class)
    public ResponseEntity<ApiError> handleInvalidStatusChange(InvalidStatusChangeException ex) {
        ApiError error = new ApiError(400, ex.getMessage(), Map.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Safe to return ex.getMessage(): the message is defined in
    // EmailAlreadyRegisteredException and contains no user data
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        ApiError error = new ApiError(409, ex.getMessage(), Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Safe to return ex.getMessage(): it is the same generic message for an
    // unknown email and a wrong password
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        ApiError error = new ApiError(401, ex.getMessage(), Map.of());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Safe to return ex.getMessage(): fixed message, defined in
    // TooManyLoginAttemptsException
    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ApiError> handleTooManyLoginAttempts(TooManyLoginAttemptsException ex) {
        ApiError error = new ApiError(429, ex.getMessage(), Map.of());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
}