package pl.clinic.app.model;

import jakarta.persistence.*;
 // zamiana klasy appointment na tabele w H2 - encja

import java.time.LocalDateTime;

/**
 * Encja reprezentująca termin wizyty w przychodni.
 *
 * <p>Obiekt jest mapowany przez JPA/Hibernate na tabelę w bazie danych (H2).
 * Termin wizyty należy do konkretnego lekarza ({@link Doctor}) i może być
 * zarezerwowany przez pacjenta ({@link Patient}).</p>
 *
 */
@Entity
public class Appointment {
    /** Klucz główny rekordu wizyty (generowany automatycznie przez bazę danych). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lekarz, do którego przypisany jest termin wizyty.
     *
     * <p>Relacja wiele-do-jednego: wiele terminów może należeć do jednego lekarza.</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private Doctor doctor;

    /**
     * Pacjent przypisany do terminu (jeśli wizyta została zarezerwowana).
     *
     * <p>Gdy termin jest wolny, wartość jest {@code null}.</p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private Patient patient;

    private LocalDateTime dateTime;
    private boolean booked; // booked czy nie booked(boolean)

    // Getter'y i Setter'y
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Doctor getDoctor() {
        return doctor;
    }
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
    public Patient getPatient() {
        return patient;
    }
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getDateTime(){
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime){
        this.dateTime = dateTime;
    }
    public boolean isBooked(){
        return booked;
    }
    public void setBooked(boolean booked){
        this.booked = booked;
    }
}
