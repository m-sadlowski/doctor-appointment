package pl.clinic.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClinicAppApplication {
    /**
     * Punkt wejścia aplikacji.
     *
     * <p>Uruchamia wbudowany serwer (np. Tomcat) oraz inicjalizuje kontekst Springa.</p>
     *
     */
    public static void main(String[] args) {
        SpringApplication.run(ClinicAppApplication.class, args);
    }

}
