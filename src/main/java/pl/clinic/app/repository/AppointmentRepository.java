package pl.clinic.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clinic.app.model.Appointment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    boolean existsByDoctor_IdAndDateTime(Long doctorId, LocalDateTime dateTime);
    boolean existsByDoctor_IdAndBookedTrue(Long doctorId);
    void deleteByDoctor_IdAndBookedFalse(Long doctorId);
    List<Appointment> findAllByOrderByDateTimeAsc();
    List<Appointment> findByBookedTrueOrderByDateTimeAsc();
    List<Appointment> findByBookedFalseOrderByDateTimeAsc();
    @Query("""
select a from Appointment a
where a.patient is null
 and lower(a.doctor.specialization) = lower(:spec)
order by a.dateTime asc
""")
    List<Appointment> findFreeBySpecialization(@Param("spec") String spec);

    @Query("""
select a from Appointment a
where a.patient is null
  and a.doctor.id = :doctorId
  and lower(a.doctor.specialization) = lower(:spec)
order by a.dateTime asc
""")
    List<Appointment> findFreeByDoctorAndSpecialization(@Param("doctorId") Long doctorId,
                                                        @Param("spec") String spec);
    List<Appointment> findByPatientIsNullOrderByDateTimeAsc();
    List<Appointment> findByPatientIsNullAndDoctor_IdOrderByDateTimeAsc(Long doctorId);
    List<Appointment> findByBookedTrueAndPatient_PhoneNumberOrderByDateTimeAsc(String phoneNumber);
}
