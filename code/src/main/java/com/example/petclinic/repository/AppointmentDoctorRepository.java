package com.example.petclinic.repository;

import java.util.Optional;
import com.example.petclinic.domain.entity.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Appointment-specific lock; leaves the Doctor module's repository unchanged. */
public interface AppointmentDoctorRepository extends JpaRepository<Doctor, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.doctorId = :id")
    Optional<Doctor> findLockedById(@Param("id") Long id);
}
