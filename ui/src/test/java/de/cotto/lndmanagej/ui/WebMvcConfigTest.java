package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.ui.interceptor.StatusModelInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTest {

    @InjectMocks
    WebMvcConfig config;

    @Mock
    StatusModelInterceptor statusModelInterceptor;

    @Mock
    InterceptorRegistry interceptorRegistry;

    @Test
    void addInterceptors() {
        config.addInterceptors(interceptorRegistry);
        verify(interceptorRegistry).addInterceptor(statusModelInterceptor);
    }

}
