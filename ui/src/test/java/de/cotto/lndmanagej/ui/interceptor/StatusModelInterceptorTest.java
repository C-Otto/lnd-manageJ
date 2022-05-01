package de.cotto.lndmanagej.ui.interceptor;

import de.cotto.lndmanagej.ui.StatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static de.cotto.lndmanagej.controller.dto.StatusModelFixture.STATUS_MODEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusModelInterceptorTest {

    @InjectMocks
    private StatusModelInterceptor statusModelInterceptor;

    @Mock
    private StatusService statusService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    void postHandle_modelNotNull_addStatusModel() {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
        ModelAndView modelAndView = new ModelAndView();
        statusModelInterceptor.postHandle(request, response, handler, modelAndView);
        assertTrue(modelAndView.getModel().containsKey("status"));
        assertEquals(modelAndView.getModel().get("status"), STATUS_MODEL);
    }

}