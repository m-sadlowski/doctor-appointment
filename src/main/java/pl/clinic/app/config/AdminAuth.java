package pl.clinic.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor autoryzujący dostęp do panelu administratora na podstawie sesji HTTP.
 *
 */
public class AdminAuth implements HandlerInterceptor {
    /**
     * Klucz atrybutu w sesji HTTP informujący, że administrator jest zalogowany.
     */
    public static final String ADMIN_AUTH_SESSION_KEY = "admin_auth";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        boolean logged = session !=null && Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_SESSION_KEY));
        if(!logged){
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
