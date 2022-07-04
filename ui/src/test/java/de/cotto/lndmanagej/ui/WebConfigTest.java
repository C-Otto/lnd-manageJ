package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.ui.controller.param.SortByConverter;
import de.cotto.lndmanagej.ui.interceptor.StatusInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @InjectMocks
    WebConfig config;

    @Mock
    StatusInterceptor statusInterceptor;

    @Mock
    InterceptorRegistry interceptorRegistry;

    @Mock
    SortByConverter sortByConverter;

    @Mock
    FormatterRegistry formatterRegistry;

    @Test
    void addInterceptors() {
        config.addInterceptors(interceptorRegistry);
        verify(interceptorRegistry).addInterceptor(statusInterceptor);
    }

    @Test
    void addFormatters() {
        config.addFormatters(formatterRegistry);
        verify(formatterRegistry).addConverter(sortByConverter);
    }

}
