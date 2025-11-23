package pl.clinic.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clinic.app.model.Patient;
public interface PatientRepository extends JpaRepository<Patient,Long> {
}
