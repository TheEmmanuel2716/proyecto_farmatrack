package com.farmatrack.vencimientos.exception;

import com.farmatrack.vencimientos.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, "VALIDACION_INVALIDA", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> constraint(ConstraintViolationException exception) {
        return response(HttpStatus.BAD_REQUEST, "VALIDACION_INVALIDA", exception.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponse> notFound(RecursoNoEncontradoException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getCodigo(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error interno.");
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, OffsetDateTime.now()));
    }
}
