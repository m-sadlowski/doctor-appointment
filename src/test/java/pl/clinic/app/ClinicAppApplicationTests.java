package pl.clinic.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import pl.clinic.app.model.Appointment;
import pl.clinic.app.model.Doctor;
import pl.clinic.app.model.Patient;
import pl.clinic.app.repository.AppointmentRepository;
import pl.clinic.app.repository.DoctorRepository;
import pl.clinic.app.repository.PatientRepository;
import pl.clinic.app.service.AppointmentService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.properties")
@Transactional
class ClinicAppApplicationTests {

    @Autowired
    private Environment environment;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void removeDemoDataFromTestDatabase() {
        // Production @PostConstruct still runs. Each scenario gets its own empty
        // transactional fixture, independent of the generated dates and IDs.
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        appointmentRepository.flush();
    }

    @Test
    void contextLoadsWithTestProfileAndTestOnlyAdminPassword() {
        assertThat(environment.getActiveProfiles()).contains("test");
        assertThat(environment.getProperty("app.admin-password"))
                .isEqualTo("test-only-admin-password");
        assertThat(appointmentService).isNotNull();
        assertThat(appointmentRepository.count()).isZero();
        assertThat(doctorRepository.count()).isZero();
        assertThat(patientRepository.count()).isZero();
    }

    @Test
    void connectsToInMemoryDatabaseInsteadOfLocalFile() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL())
                    .startsWith("jdbc:h2:mem:clinic-test-")
                    .doesNotContain("jdbc:h2:file:", "./data/clinicdb");
        }
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto"))
                .isEqualTo("create-drop");
        assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class))
                .isFalse();
    }

    @Test
    void persistsAppointmentWithDoctorAndPatientRelationships() {
        Doctor doctor = new Doctor();
        doctor.setFirstName("Anna");
        doctor.setLastName("Kowalska");
        doctor.setSpecialization("Internista");
        doctor = doctorRepository.saveAndFlush(doctor);

        Patient patient = new Patient();
        patient.setFirstName("Jan");
        patient.setLastName("Nowak");
        patient.setPhoneNumber("501234567");
        patient = patientRepository.saveAndFlush(patient);

        LocalDateTime dateTime = LocalDateTime.of(2030, 5, 20, 10, 0);
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDateTime(dateTime);
        appointment.setBooked(true);
        appointmentRepository.saveAndFlush(appointment);
        entityManager.clear();

        assertThat(appointmentRepository.findByBookedTrueAndPatient_PhoneNumberOrderByDateTimeAsc("501234567"))
                .singleElement().satisfies(saved -> {
                    assertThat(saved.getDoctor().getFullName()).isEqualTo("Anna Kowalska");
                    assertThat(saved.getPatient().getFullName()).isEqualTo("Jan Nowak");
                    assertThat(saved.getDateTime()).isEqualTo(dateTime);
                    assertThat(saved.isBooked()).isTrue();
                });
    }

}
