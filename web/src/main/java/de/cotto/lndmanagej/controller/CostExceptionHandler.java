package de.cotto.lndmanagej.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class CostExceptionHandler {
    public CostExceptionHandler() {
        // default constructor
    }

    @ExceptionHandler(CostException.class)
    @ResponseStatus(BAD_REQUEST)
    public ResponseEntity<String> costException(CostException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
