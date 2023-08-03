package de.cotto.lndmanagej.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotFoundExceptionHandlerTest {
    private static final NotFoundException EXCEPTION = new NotFoundException();

    @InjectMocks
    private NotFoundExceptionHandler exceptionHandler;

    @Test
    void mapsToNotFound() {
        assertThat(exceptionHandler.notFoundException(EXCEPTION).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
