package pl.clinic.app.model;

import jakarta.persistence.*;


/**
 * Klasa bazowa (mapped superclass) dla encji przechowujących dane osobowe.
 *
 * <p>Adnotacja {@link MappedSuperclass} oznacza, że klasa nie posiada własnej tabeli w bazie,
 * ale jej pola są dziedziczone i mapowane do tabel klas potomnych.</p>
 */
@MappedSuperclass
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private  String FirstName;
    private String LastName;

    public long getId() {
        return id;
    }
    public String getFirstName() {
        return FirstName;
    }
    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }
    public String getLastName() {
        return LastName;
    }
    public void setLastName(String lastName) {
        this.LastName = lastName;
    }
    /**
     * Zwraca pełną nazwę osoby w postaci „Imię Nazwisko”.
     *
     * @return pełne imię i nazwisko
     */
    public String getFullName() {
        return FirstName + " " + LastName;
    }
}
