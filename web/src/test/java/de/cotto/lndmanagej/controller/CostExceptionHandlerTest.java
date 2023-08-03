package de.cotto.lndmanagej.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CostExceptionHandlerTest {
    private static final CostException EXCEPTION = new CostException("abc");

    @InjectMocks
    private CostExceptionHandler costExceptionHandler;

    @Test
    void mapsToBadRequest() {
        assertThat(costExceptionHandler.costException(EXCEPTION).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void returnsMessageInBody() {
        assertThat(costExceptionHandler.costException(EXCEPTION).getBody())
                .isEqualTo(EXCEPTION.getMessage());
    }
}
