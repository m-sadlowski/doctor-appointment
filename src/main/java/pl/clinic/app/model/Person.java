package pl.clinic.app.model;

import jakarta.persistence.*;

@MappedSuperclass // abstrakcja; nie tworzy osobnej tabeli
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
    //metoda pomocnicza
    public String getFullName() {
        return FirstName + " " + LastName;
    }
}
