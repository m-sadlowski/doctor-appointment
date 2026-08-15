package pl.clinic.app.model;

import jakarta.persistence.Entity;


/**
 * Encja reprezentująca pacjenta w systemie.
 *
 * <p>Dziedziczy podstawowe dane osobowe z klasy {@link Person} i przechowuje numer telefonu</p>
 */
@Entity
public class Patient extends Person {

    /**
     * Numer telefonu pacjenta używany do wyszukiwania zarezerwowanych wizyt.
     */
    private String phoneNumber;

    // Getter'y i Setter'y
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
