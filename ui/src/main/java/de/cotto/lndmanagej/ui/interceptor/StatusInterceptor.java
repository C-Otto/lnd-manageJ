package de.cotto.lndmanagej.ui.interceptor;

import de.cotto.lndmanagej.ui.StatusService;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;

@Component
public class StatusInterceptor implements HandlerInterceptor {

    private final StatusService statusService;

    public StatusInterceptor(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        StatusModel status = statusService.getStatus();
        String path = request.getServletPath();
        if (disallowRedirect(status, path)) {
            return true;
        }
        response.sendRedirect("/status");
        return false;
    }

    private boolean disallowRedirect(StatusModel status, String path) {
        if (status.synced()) {
            return true;
        }
        return isResource(path) || isStatusPage(path) || isApi(path);
    }

    boolean isStatusPage(@Nullable String path) {
        return "/status".equals(path);
    }

    boolean isResource(@Nullable String path) {
        return path != null && (path.contains("/css/") || path.contains("/js/") || path.contains("/images/"));
    }

    boolean isApi(@Nullable String path) {
        return path != null && (path.contains("/api/") || path.contains("/legacy/"));
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable ModelAndView modelAndView
    ) {
        if (modelAndView != null) {
            modelAndView.getModel().put("status", statusService.getStatus());
        }
    }
}
