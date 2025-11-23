package pl.clinic.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clinic.app.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
}
