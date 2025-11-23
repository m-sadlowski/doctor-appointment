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

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * beta --- generacja na 7 dni do przodu
     * metoda wykouje sie tylko raz po starcji app
     */
    @PostConstruct
    public void init(){
        if(appointmentRepository.count() > 0){
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
    //zwrocenie wszystkich wizyt z bazy

    public List<Appointment> getAllAppointments(){
        return appointmentRepository.findAll();
    }

    public Appointment findById(long id){
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("nie znaleziono wizyty"));
    }
    public void bookAppointment(Long id, String firstName, String lastName, String phoneNumber) {
        Appointment appt = findById(id);

        Patient p = new Patient();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setPhoneNumber(phoneNumber);

        p = patientRepository.save(p);

        appt.setPatient(p);
        appt.setBooked(true);

        appointmentRepository.save(appt);
    }
}
