package de.cotto.lndmanagej.ui.interceptor;

import de.cotto.lndmanagej.ui.StatusService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL;
import static de.cotto.lndmanagej.ui.dto.StatusModelFixture.STATUS_MODEL_NOT_SYNCED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusInterceptorTest {

    @InjectMocks
    private StatusInterceptor statusInterceptor;

    @Mock
    private StatusService statusService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    void preHandle_synced_noRedirect() throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
        boolean proceed = statusInterceptor.preHandle(request, response, handler);
        assertThat(proceed).isTrue();
        verifyNoInteractions(response);
    }

    @Test
    void preHandle_doNotProceed() throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
        boolean proceed = statusInterceptor.preHandle(request, response, handler);
        assertThat(proceed).isTrue();
        verifyNoInteractions(response);
    }

    @Test
    void preHandle_notSynced_redirect() throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL_NOT_SYNCED);
        boolean proceed = statusInterceptor.preHandle(request, response, handler);
        assertThat(proceed).isFalse();
        verify(response).sendRedirect("/status");
    }

    @Test
    void preHandle_statusPage_noRedirect() throws Exception {
        preHandleStatusNotSyncedExpectNoRedirect("/status");
    }

    @Test
    void preHandle_cssResource_noRedirect() throws Exception {
        preHandleStatusNotSyncedExpectNoRedirect("/css/main.css");
    }

    @Test
    void preHandle_jsResource_noRedirect() throws Exception {
        preHandleStatusNotSyncedExpectNoRedirect("/js/main.js");
    }

    @Test
    void preHandle_imageResource_noRedirect() throws Exception {
        preHandleStatusNotSyncedExpectNoRedirect("/images/favicon.png");
    }

    private void preHandleStatusNotSyncedExpectNoRedirect(String servletPath) throws Exception {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL_NOT_SYNCED);
        when(request.getServletPath()).thenReturn(servletPath);
        boolean proceed = statusInterceptor.preHandle(request, response, handler);
        assertThat(proceed).isTrue();
        verifyNoInteractions(response);
    }

    @Test
    void isResource() {
        assertThat(statusInterceptor.isResource(null)).isFalse();
        assertThat(statusInterceptor.isResource("/css/main.css")).isTrue();
    }

    @Test
    void isStatusPage_false() {
        assertThat(statusInterceptor.isStatusPage(null)).isFalse();
        assertThat(statusInterceptor.isStatusPage("/channels")).isFalse();
    }

    @Test
    void isStatusPage_true() {
        assertThat(statusInterceptor.isStatusPage("/status")).isTrue();
    }

    @Test
    void postHandle_modelNotNull_addStatusModel() {
        when(statusService.getStatus()).thenReturn(STATUS_MODEL);
        ModelAndView modelAndView = new ModelAndView();
        statusInterceptor.postHandle(request, response, handler, modelAndView);
        verify(statusService).getStatus();
        assertThat(modelAndView.getModel().containsKey("status")).isTrue();
        assertThat(modelAndView.getModel().get("status")).isEqualTo(STATUS_MODEL);
    }

    @Test
    void postHandle_noModel_addStatusModel() {
        ModelAndView modelAndView = null;
        statusInterceptor.postHandle(request, response, handler, modelAndView);
        verifyNoInteractions(statusService);
    }

}
