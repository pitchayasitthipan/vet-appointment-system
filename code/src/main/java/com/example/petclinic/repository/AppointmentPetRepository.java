package com.example.petclinic.repository;

import java.util.List;
import java.util.Optional;
import com.example.petclinic.domain.entity.AppointmentPet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AppointmentPetRepository extends JpaRepository<AppointmentPet, Long> {
    List<AppointmentPet> findByPetOwnerOwnerIdOrderByPetNameAsc(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from AppointmentPet p where p.petId = :id")
    Optional<AppointmentPet> findLockedById(@Param("id") Long id);
}
