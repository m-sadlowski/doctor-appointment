package pl.clinic.app.model;

import jakarta.persistence.*;

@Entity
public class Doctor extends Person {

    private String specialization;

    // Getter'y i Setter'y
    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
