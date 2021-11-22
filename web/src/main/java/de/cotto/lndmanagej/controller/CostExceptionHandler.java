package de.cotto.lndmanagej.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CostExceptionHandler extends ResponseEntityExceptionHandler {
    public CostExceptionHandler() {
        super();
    }

    @ExceptionHandler(CostException.class)
    public ResponseEntity<String> handleException(CostException exception) {
        return ResponseEntity
                .badRequest()
                .body(exception.getMessage());
    }
}