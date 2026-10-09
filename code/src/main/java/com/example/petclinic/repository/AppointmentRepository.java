package com.example.petclinic.repository;

import java.time.LocalDateTime;
import java.util.*;
import com.example.petclinic.domain.entity.Appointment;
import com.example.petclinic.domain.enums.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.appointmentId = :id")
    Optional<Appointment> findLockedById(@Param("id") Long id);
    Page<Appointment> findByPetPetOwnerOwnerId(Long ownerId, Pageable pageable);
    Page<Appointment> findByPetPetOwnerOwnerIdAndStatus(Long ownerId, AppointmentStatus status, Pageable pageable);
    Optional<Appointment> findByAppointmentIdAndPetPetOwnerOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.appointmentId = :id and a.pet.petOwner.ownerId = :ownerId")
    Optional<Appointment> findLockedByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Query("select count(a) from Appointment a where a.status in :active "
        + "and a.appointmentDateTime = :time and (a.doctor.doctorId = :doctorId or a.pet.petId = :petId) "
        + "and (:excludeId is null or a.appointmentId <> :excludeId)")
    long countConflicts(@Param("doctorId") Long doctorId, @Param("petId") Long petId,
        @Param("time") LocalDateTime time, @Param("excludeId") Long excludeId,
        @Param("active") Collection<AppointmentStatus> active);

    List<Appointment> findByDoctorDoctorIdAndAppointmentDateTimeGreaterThanEqualAndAppointmentDateTimeLessThanAndStatusIn(
        Long doctorId, LocalDateTime start, LocalDateTime end, Collection<AppointmentStatus> statuses);
}
