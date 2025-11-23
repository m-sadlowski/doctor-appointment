package pl.clinic.app.model;

import jakarta.persistence.Entity;

@Entity
public class Patient extends Person {

    private String phoneNumber;

    // Getter'y i Setter'y
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
