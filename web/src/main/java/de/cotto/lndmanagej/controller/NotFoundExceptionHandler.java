package de.cotto.lndmanagej.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotFoundExceptionHandler {
    public NotFoundExceptionHandler() {
        // default constructor
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Void> notFoundException(@SuppressWarnings("unused") Exception exception) {
        return ResponseEntity.notFound().build();
    }
}
