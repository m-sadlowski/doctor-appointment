package pl.clinic.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clinic.app.model.Appointment;
import pl.clinic.app.model.Doctor;
import pl.clinic.app.model.Patient;
import pl.clinic.app.repository.AppointmentRepository;
import pl.clinic.app.repository.DoctorRepository;
import pl.clinic.app.repository.PatientRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final Long APPOINTMENT_ID = 10L;
    private static final Long DOCTOR_ID = 20L;
    private static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.of(2030, 5, 20, 10, 0);

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        // No Spring context: demo-data initialization and database access do not run.
        service = new AppointmentService(appointmentRepository, doctorRepository, patientRepository);
    }

    @Test
    void booksFreeAppointmentWithSavedPatientAndNormalizedPhone() {
        Appointment appointment = appointment(false, null);
        Patient savedPatient = new Patient();
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        service.bookAppointment(APPOINTMENT_ID, "Jan", "Nowak", "+48 501-234-567");

        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(patientCaptor.capture());
        assertThat(patientCaptor.getValue().getFirstName()).isEqualTo("Jan");
        assertThat(patientCaptor.getValue().getLastName()).isEqualTo("Nowak");
        assertThat(patientCaptor.getValue().getPhoneNumber()).isEqualTo("48501234567");
        assertThat(appointment.isBooked()).isTrue();
        assertThat(appointment.getPatient()).isSameAs(savedPatient);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rejectsBookingOccupiedAppointmentWithoutOverwritingPatient() {
        Patient existingPatient = new Patient();
        Appointment appointment = appointment(true, existingPatient);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.bookAppointment(APPOINTMENT_ID, "Jan", "Nowak", "501234567"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(appointment.isBooked()).isTrue();
        assertThat(appointment.getPatient()).isSameAs(existingPatient);
        verifyNoInteractions(patientRepository);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsBookingMissingAppointmentWithoutCreatingPatient() {
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.bookAppointment(APPOINTMENT_ID, "Jan", "Nowak", "501234567"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(patientRepository);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void cancelsBookedAppointmentAndKeepsSlotAvailable() {
        Appointment appointment = appointment(true, new Patient());
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        service.cancelAppointment(APPOINTMENT_ID);

        assertThat(appointment.isBooked()).isFalse();
        assertThat(appointment.getPatient()).isNull();
        verify(appointmentRepository).save(appointment);
        verify(appointmentRepository, never()).deleteById(any());
        verifyNoInteractions(patientRepository);
    }

    @Test
    void cancellingFreeAppointmentDeletesSlotInsteadOfSavingIt() {
        Appointment appointment = appointment(false, null);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        service.cancelAppointment(APPOINTMENT_ID);

        verify(appointmentRepository).deleteById(APPOINTMENT_ID);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void patientCanCancelReservationUsingEquivalentPhoneFormat() {
        Patient patient = new Patient();
        patient.setPhoneNumber("48501234567");
        Appointment appointment = appointment(true, patient);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        service.cancelByPatientPhone(APPOINTMENT_ID, "+48 501-234-567");

        assertThat(appointment.isBooked()).isFalse();
        assertThat(appointment.getPatient()).isNull();
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rejectsPatientCancellationWithDifferentPhone() {
        Patient patient = new Patient();
        patient.setPhoneNumber("501234567");
        Appointment appointment = appointment(true, patient);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelByPatientPhone(APPOINTMENT_ID, "999888777"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(appointment.isBooked()).isTrue();
        assertThat(appointment.getPatient()).isSameAs(patient);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsPatientCancellationOfFreeSlot() {
        Appointment appointment = appointment(false, null);
        when(appointmentRepository.findById(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.cancelByPatientPhone(APPOINTMENT_ID, "501234567"))
                .isInstanceOf(IllegalStateException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(appointmentRepository, never()).deleteById(any());
    }

    @Test
    void addsFreeAppointmentSlotForExistingDoctor() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.of(doctor));

        service.addAppointmentSlot(DOCTOR_ID, APPOINTMENT_TIME);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        Appointment saved = captor.getValue();
        assertThat(saved.getDoctor()).isSameAs(doctor);
        assertThat(saved.getDateTime()).isEqualTo(APPOINTMENT_TIME);
        assertThat(saved.isBooked()).isFalse();
        assertThat(saved.getPatient()).isNull();
    }

    @Test
    void rejectsDuplicateSlotForSameDoctorAndTime() {
        when(appointmentRepository.existsByDoctor_IdAndDateTime(DOCTOR_ID, APPOINTMENT_TIME))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addAppointmentSlot(DOCTOR_ID, APPOINTMENT_TIME))
                .isInstanceOf(IllegalStateException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verifyNoInteractions(doctorRepository);
    }

    @Test
    void rejectsSlotForMissingDoctor() {
        when(doctorRepository.findById(DOCTOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addAppointmentSlot(DOCTOR_ID, APPOINTMENT_TIME))
                .isInstanceOf(IllegalArgumentException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void blocksDeletingDoctorWithActiveReservationsWithoutDeletingSlots() {
        when(appointmentRepository.existsByDoctor_IdAndBookedTrue(DOCTOR_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.delteDoctor(DOCTOR_ID))
                .isInstanceOf(IllegalStateException.class);

        verify(appointmentRepository, never()).deleteByDoctor_IdAndBookedFalse(any());
        verifyNoInteractions(doctorRepository);
    }

    private Appointment appointment(boolean booked, Patient patient) {
        Appointment appointment = new Appointment();
        appointment.setId(APPOINTMENT_ID);
        appointment.setDateTime(APPOINTMENT_TIME);
        appointment.setBooked(booked);
        appointment.setPatient(patient);
        return appointment;
    }
}
