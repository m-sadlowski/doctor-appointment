package pl.clinic.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clinic.app.model.Doctor;
public interface DoctorRepository  extends JpaRepository<Doctor,Long> {
}
