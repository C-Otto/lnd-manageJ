package de.cotto.lndmanagej.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class NotFoundExceptionHandler extends ResponseEntityExceptionHandler {
    public NotFoundExceptionHandler() {
        super();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleException(@SuppressWarnings("unused") NotFoundException exception) {
        return ResponseEntity
                .notFound()
                .build();
    }
}