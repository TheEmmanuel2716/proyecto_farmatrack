package com.farmatrack.inventario.exception;

import com.farmatrack.inventario.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage()).collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, "VALIDACION_INVALIDA", message);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> constraint(ConstraintViolationException e) {
        return response(HttpStatus.BAD_REQUEST, "VALIDACION_INVALIDA", e.getMessage());
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    ResponseEntity<ErrorResponse> malformedRequest(Exception e) {
        return response(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "El cuerpo debe ser JSON válido y usar Content-Type: application/json.");
    }
    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponse> notFound(RecursoNoEncontradoException e) {
        return response(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", e.getMessage());
    }
    @ExceptionHandler(DatoDuplicadoException.class)
    ResponseEntity<ErrorResponse> duplicate(DatoDuplicadoException e) {
        return response(HttpStatus.CONFLICT, "DATO_DUPLICADO", e.getMessage());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> dataIntegrity(DataIntegrityViolationException e) {
        return response(HttpStatus.CONFLICT, "DATO_DUPLICADO",
                "Ya existe un registro con esos datos únicos.");
    }
    @ExceptionHandler(LoteVencidoException.class)
    ResponseEntity<ErrorResponse> expired(LoteVencidoException e) {
        return response(HttpStatus.CONFLICT, "LOTE_VENCIDO", e.getMessage());
    }
    @ExceptionHandler(StockInsuficienteException.class)
    ResponseEntity<ErrorResponse> stock(StockInsuficienteException e) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, "STOCK_INSUFICIENTE", e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception e) {
        log.error("Error no controlado procesando la solicitud", e);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error interno.");
    }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, OffsetDateTime.now()));
    }
}
