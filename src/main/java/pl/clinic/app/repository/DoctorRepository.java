package pl.clinic.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.clinic.app.model.Doctor;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface DoctorRepository  extends JpaRepository<Doctor,Long> {
    @Query("select distinct d.specialization from Doctor d order by d.specialization")
    List<String> findDistinctSpecializations();
}
