package com.example.petclinic.repository;

import java.util.List;
import java.util.Optional;
import com.example.petclinic.domain.entity.Pet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.Repository;

/** Reads and locks shared Pet rows; all Pet writes belong to the Pet module. */
public interface AppointmentPetRepository extends Repository<Pet, Long> {
    Optional<Pet> findById(Long id);
    List<Pet> findByPetOwnerOwnerIdOrderByNameAsc(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.petId = :id")
    Optional<Pet> findLockedById(@Param("id") Long id);
}
