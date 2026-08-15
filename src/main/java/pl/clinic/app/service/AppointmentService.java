package pl.clinic.app.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pl.clinic.app.model.Appointment;
import pl.clinic.app.model.Patient;
import pl.clinic.app.model.Doctor;
import pl.clinic.app.repository.AppointmentRepository;
import pl.clinic.app.repository.PatientRepository;
import pl.clinic.app.repository.DoctorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Comparator;


/**
 * Serwis zawierający logikę biznesową aplikacji (wizyty, pacjenci, lekarze).
 *
 * <p>Klasa odpowiada m.in. za:</p>
 * <ul>
 *   <li>generowanie danych startowych przy pustej bazie (opcjonalnie),</li>
 *   <li>pobieranie i sortowanie wizyt oraz lekarzy,</li>
 *   <li>rezerwację i anulowanie wizyt,</li>
 *   <li>zarządzanie terminami (dodawanie/usuwanie) w panelu administratora,</li>
 *   <li>zarządzanie lekarzami (dodawanie/usuwanie) z uwzględnieniem reguł bezpieczeństwa.</li>
 * </ul>
 *
 * <p>Serwis korzysta z repozytoriów Spring Data JPA, które komunikują się z bazą H2
 * poprzez Hibernate (JPA/ORM).</p>
 */
@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    /**
     * Tworzy serwis i wstrzykuje wymagane repozytoria.
     *
     * @param appointmentRepository repozytorium wizyt
     * @param doctorRepository repozytorium lekarzy
     * @param patientRepository repozytorium pacjentów
     */
    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Inicjalizacja danych startowych (seed) przy pustej bazie danych.
     *
     * <p>Metoda uruchamia się automatycznie po starcie aplikacji. Jeśli w bazie znajdują się już
     * lekarze lub wizyty, inicjalizacja jest pomijana. W przeciwnym razie tworzeni są przykładowi
     * lekarze oraz wolne terminy wizyt na kolejne 7 dni.</p>
     *
     * <p>Cel: ułatwienie demonstracji działania aplikacji bez konieczności ręcznego dodawania danych.</p>
     */
    @PostConstruct
    public void init(){
        if(doctorRepository.count() > 0 || appointmentRepository.count() > 0){
            //sprawdzenie; jezeli cos jest juz w bazie = nie rob nic
            return;
        }
        Doctor d1 = new Doctor();
        d1.setFirstName("Filip");
        d1.setLastName("Adamczyk");
        d1.setSpecialization("Ginekolog");

        Doctor d2 = new Doctor();
        d2.setFirstName("Marcin");
        d2.setLastName("Najmun");
        d2.setSpecialization("Gastrolog");

        Doctor d3 = new Doctor();
        d3.setFirstName("Michał");
        d3.setLastName("Woinski");
        d3.setSpecialization("Internista");

        d1 = doctorRepository.save(d1);
        d2 = doctorRepository.save(d2);
        d3 = doctorRepository.save(d3);

        List<Doctor> doctors = List.of(d1, d2, d3);

        LocalDate today = LocalDate.now();
        LocalDate to = today.plusDays(7);

        for(LocalDate date = today; date.isBefore(to); date = date.plusDays(1)){
            int[] hours = {9, 11, 13};
                for(int hour : hours) {
                    for (Doctor doctor : doctors) {
                        Appointment appt = new Appointment();
                        appt.setDoctor(doctor);
                        appt.setDateTime(LocalDateTime.of(date, LocalTime.of(hour, 0)));
                        appt.setBooked(false);
                        appt.setPatient(null);

                        appointmentRepository.save(appt); // zapis wizyt w H2
                    }
                }

        }
    }

    /**
     * Zwraca wszystkie wizyty zapisane w bazie danych (bez sortowania).
     *
     * @return lista wszystkich wizyt
     */
    public List<Appointment> getAllAppointments(){
        return appointmentRepository.findAll();
    }
    /**
     * Zwraca wszystkie wizyty posortowane rosnąco według daty i godziny.
     *
     * @return lista wszystkich wizyt posortowana po dacie
     */
    public List<Appointment> getAllAppointmentsSorted(){
        List<Appointment> list = appointmentRepository.findAll();
        list.sort(Comparator.comparing(Appointment::getDateTime));
        return list;
    }

    /**
     * Wyszukuje wizytę po identyfikatorze.
     *
     * @param id identyfikator wizyty
     * @return znaleziony obiekt {@link Appointment}
     * @throws IllegalArgumentException jeśli nie znaleziono wizyty o podanym ID
     */
    public Appointment findById(long id){
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("nie znaleziono wizyty"));
    }

    /**
     * Rezerwuje wolny termin wizyty dla pacjenta.
     *
     * <p>Metoda sprawdza, czy termin nie jest zajęty. Następnie tworzy rekord pacjenta,
     * zapisuje go w bazie oraz przypina do wizyty (ustawiając {@code booked=true}).</p>
     *
     * @param id identyfikator terminu wizyty
     * @param firstName imię pacjenta
     * @param lastName nazwisko pacjenta
     * @param phoneNumber numer telefonu pacjenta (zostanie znormalizowany)
     * @throws IllegalStateException jeśli termin jest już zajęty
     */
    public void bookAppointment(Long id, String firstName, String lastName, String phoneNumber) {
        Appointment appt = findById(id);

        if(appt.isBooked()){
            throw new IllegalStateException("Ten termin jest juz zajety");
        }

        Patient p = new Patient();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setPhoneNumber(normalizePhone(phoneNumber));

        p = patientRepository.save(p);

        appt.setPatient(p);
        appt.setBooked(true);

        appointmentRepository.save(appt);
    }
    /**
     * Zwraca listę wszystkich lekarzy posortowaną alfabetycznie po imieniu i nazwisku.
     *
     * @return lista lekarzy posortowana po nazwie wyświetlanej
     */
    public List<Doctor>  getAllDoctors(){
        List<Doctor> list = doctorRepository.findAll();
        list.sort(Comparator.comparing(Doctor::getFullName));
        return list;
    }

    /**
     * Zwraca listę wolnych terminów wizyt dla pacjenta, posortowaną po dacie rosnąco.
     *
     * @return lista wolnych terminów posortowana po dacie
     */
    public List<Appointment> getAvailableAppointmentsSorted(){
        return appointmentRepository.findByPatientIsNullOrderByDateTimeAsc();
    }

    /**
     * Dodaje nowy wolny termin wizyty w panelu administratora.
     *
     * @param doctorId identyfikator lekarza
     * @param dateTime data i godzina terminu
     * @throws IllegalStateException jeśli termin dla lekarza o tej dacie już istnieje
     * @throws IllegalArgumentException jeśli lekarz o podanym ID nie istnieje
     */
    public void addAppointmentSlot(Long doctorId, LocalDateTime dateTime){
        if(appointmentRepository.existsByDoctor_IdAndDateTime(doctorId,  dateTime)){
            throw new IllegalStateException("Ten termin jest juz zajety przez tego lekarza");
        }
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("nie znaleziono lekarza"));
        Appointment appt = new Appointment();
        appt.setDoctor(doctor);
        appt.setDateTime(dateTime);
        appt.setBooked(false);
        appt.setPatient(null);
        appointmentRepository.save(appt);
    }
    /**
     * Anuluje lub usuwa termin wizyty w panelu administratora.
     *
     * @param id identyfikator terminu wizyty
     * @throws IllegalArgumentException jeśli nie znaleziono wizyty o podanym ID
     */
    public void cancelAppointment(Long id){
        Appointment appt = findById(id);
        if(appt.isBooked()){
            appt.setBooked(false);
            appt.setPatient(null);
            appointmentRepository.save(appt);
        }else {
            appointmentRepository.deleteById(id);
        }
    }

    /**
     * Zwraca wolne terminy wizyt dla pacjenta z opcjonalnym filtrowaniem.
     *
     * <p>W przypadku filtrowania po specjalizacji wykorzystywane jest zapytanie {@code @Query}
     * (JPQL) w repozytorium, aby zapewnić poprawne działanie i sortowanie.</p>
     *
     * @param doctorId opcjonalny identyfikator lekarza
     * @param specialization opcjonalna specjalizacja lekarza
     * @return lista wolnych terminów posortowana po dacie rosnąco
     */
    public List<Appointment> getAvailableAppointmentsFiltered(Long doctorId, String specialization) {
        boolean hasDoctor = (doctorId != null);
        boolean hasSpec = (specialization != null && !specialization.isBlank());

        if (hasDoctor && hasSpec) {
            return appointmentRepository.findFreeByDoctorAndSpecialization(doctorId, specialization);
        }
        if (hasDoctor) {
            return appointmentRepository.findByPatientIsNullAndDoctor_IdOrderByDateTimeAsc(doctorId);
        }
        if (hasSpec) {
            return appointmentRepository.findFreeBySpecialization(specialization);
        }
        return appointmentRepository.findByPatientIsNullOrderByDateTimeAsc();
    }
    /**
     * Zwraca listę wizyt do panelu administratora z filtrem statusu.
     *
     * @param status filtr statusu (domyślnie {@code all})
     * @return lista wizyt posortowana po dacie rosnąco
     */
    public List<Appointment> getAppointmentsForAdmin(String status) {
        if ("booked".equalsIgnoreCase(status)) {
            return appointmentRepository.findByBookedTrueOrderByDateTimeAsc();
        }
        if ("free".equalsIgnoreCase(status)) {
            return appointmentRepository.findByBookedFalseOrderByDateTimeAsc();
        }
        return appointmentRepository.findAllByOrderByDateTimeAsc();
    }

    /**
     * Zwraca listę unikalnych specjalizacji dostępnych w systemie.
     *
     * @return lista specjalizacji (bez powtórzeń)
     */
    public List<String> getAllSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }

    /**
     * Zwraca listę zarezerwowanych wizyt pacjenta na podstawie numeru telefonu.
     *
     * @param phoneNumber numer telefonu użyty przy rezerwacji
     * @return lista zarezerwowanych wizyt posortowana po dacie rosnąco
     */
    public List<Appointment> getBookedAppointmentsByPhone(String phoneNumber) {
        String normalized = normalizePhone(phoneNumber);
        return appointmentRepository.findByBookedTrueAndPatient_PhoneNumberOrderByDateTimeAsc(normalized);
    }

    /**
     * Anuluje rezerwację wizyty po weryfikacji numeru telefonu pacjenta.
     *
     * @param appointmentId identyfikator wizyty
     * @param phoneNumber numer telefonu do weryfikacji
     * @throws IllegalStateException jeśli wizyta nie jest zarezerwowana
     * @throws IllegalArgumentException jeśli numer telefonu nie pasuje do rezerwacji
     */
    public void cancelByPatientPhone(Long appointmentId, String phoneNumber) {
        Appointment appt = findById(appointmentId);
        if (!appt.isBooked() || appt.getPatient() == null) {
            throw new IllegalStateException("Wizyta nie jest zarezerwowana.");
        }

        String normalized = normalizePhone(phoneNumber);
        String stored = normalizePhone(appt.getPatient().getPhoneNumber());

        if (!normalized.equals(stored)) {
            throw new IllegalArgumentException("Nieprawidłowy numer telefonu");
        }

        appt.setBooked(false);
        appt.setPatient(null);
        appointmentRepository.save(appt);
    }

    /**
     * Normalizuje numer telefonu do postaci zawierającej wyłącznie cyfry.
     *
     * @param phone numer telefonu w dowolnym formacie
     * @return numer telefonu zawierajacy tylko cyfry (lub pusty string dla null)
     */
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("\\D", "");
    }

    /**
     * Dodaje nowego lekarza do systemu.
     *
     * @param firstName imię lekarza
     * @param lastName nazwisko lekarza
     * @param specialization specjalizacja lekarza
     * @throws IllegalArgumentException jeśli którekolwiek pole jest puste
     */
    public void addDoctor(String firstName, String lastName, String specialization) {
        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || specialization == null || specialization.isBlank()) {
            throw new IllegalArgumentException("Wszystkie pola lekarza są wymagane.");
        }
        Doctor d = new Doctor();
        d.setFirstName(firstName.trim());
        d.setLastName(lastName.trim());
        d.setSpecialization(specialization.trim());

        doctorRepository.save(d);
    }

    /**
     * Usuwa lekarza z systemu z uwzglednieniem regul bezpieczenstwa.
     *
     * <p>Jeśli lekarz ma zarezerwowane wizyty, operacja jest blokowana.
     * W przeciwnym razie usuwane są wszystkie wolne terminy lekarza, a następnie usuwany jest lekarz.</p>
     *
     * @param id identyfikator lekarza
     * @throws IllegalStateException jeśli istnieją zarezerwowane wizyty lekarza
     */
    public void delteDoctor(Long id){
        if(appointmentRepository.existsByDoctor_IdAndBookedTrue(id)){
            throw new IllegalStateException("Nie można usunąć lekarza | istnieją zarezerowane wizyty");
        }
        // usuwanie wolnych 'slotow'
        appointmentRepository.deleteByDoctor_IdAndBookedFalse(id);
        // dopiero teraz usuwanie lekarza;
        doctorRepository.deleteById(id);
    }
}
