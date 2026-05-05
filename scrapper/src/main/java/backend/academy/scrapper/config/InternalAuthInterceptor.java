package backend.academy.scrapper.config;

import backend.academy.scrapper.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InternalAuthInterceptor implements HandlerInterceptor {

    @Value("${app.scrapper.auth.header:X-Internal-Secret}")
    private String authHeaderName;

    @Value("${app.scrapper.auth.shared-secret:devpulse-internal-secret}")
    private String sharedSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestPath = request.getRequestURI();
        if (!requestPath.startsWith("/clients") && !requestPath.startsWith("/links")) {
            return true;
        }

        String authHeader = request.getHeader(authHeaderName);
        if (!sharedSecret.equals(authHeader)) {
            throw new UnauthorizedException("Некорректный межсервисный токен");
        }
        return true;
    }
}
