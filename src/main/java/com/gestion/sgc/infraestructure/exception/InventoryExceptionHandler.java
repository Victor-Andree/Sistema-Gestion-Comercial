package com.gestion.sgc.infraestructure.exception;

import com.gestion.sgc.infraestructure.controllers.ProductoController;
import com.gestion.sgc.infraestructure.controllers.StockController;
import com.gestion.sgc.infraestructure.controllers.TipoProductoController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {ProductoController.class, StockController.class, TipoProductoController.class})
public class InventoryExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> businessError(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",
                ex.getReason() == null ? "No se pudo completar la operación" : ex.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("message", "Revisa los datos del formulario", "errors", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> invalidBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "Los datos enviados no tienen el formato esperado"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integrityError(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(Map.of("message",
                "El registro está duplicado o tiene datos relacionados que impiden la operación"));
    }
}
