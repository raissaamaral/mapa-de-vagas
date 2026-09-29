package io.github.raissaamaral.mapadevagas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
