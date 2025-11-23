package pl.clinic.app.model;

import jakarta.persistence.*;
 // zamiana klasy appointment na tabele w H2 - encja

import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Doctor doctor;

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
