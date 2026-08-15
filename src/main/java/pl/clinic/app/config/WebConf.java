package pl.clinic.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * Konfiguracja Spring MVC dla aplikacji.
 *
 * <p>Rejestruje interceptory obsługujące dodatkową logikę dla wybranych ścieżek URL.
 * W tej aplikacji wykorzystywany jest interceptor, który zabezpiecza
 * dostęp do panelu administratora.</p>
 */
@Configuration
public class WebConf implements WebMvcConfigurer {
    /**
     * Rejestruje interceptory w aplikacji oraz definiuje, dla jakich ścieżek mają działać.
     *
     * <p>Interceptor jest uruchamiany dla wszystkich adresów zaczynających się od
     * {@code /admin/}</p>
     *
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuth())
                .addPathPatterns("/admin/**");
    }
}
