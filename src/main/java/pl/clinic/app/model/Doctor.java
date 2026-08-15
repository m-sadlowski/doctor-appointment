package pl.clinic.app.model;

import jakarta.persistence.*;


/**
 * Encja reprezentująca lekarza w systemie.
 *
 * <p>Dziedziczy podstawowe dane osobowe (np. imię i nazwisko) z klasy {@link Person}
 * i rozszerza je o specjalizacje.</p>
 */
@Entity
public class Doctor extends Person {

    /**
     * Specjalizacja lekarza
     */
    private String specialization;

    // Getter'y i Setter'y
    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
