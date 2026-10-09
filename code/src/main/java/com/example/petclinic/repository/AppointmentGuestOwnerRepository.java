package com.example.petclinic.repository;

import java.util.List;
import com.example.petclinic.domain.entity.PetOwner;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/** Read adapter for appointment guests, leaving the owner's CRUD repository unchanged. */
public interface AppointmentGuestOwnerRepository extends JpaRepository<PetOwner, Long> {
    @Query("select o from PetOwner o where replace(replace(o.phone, ' ', ''), '-', '') = :phone")
    List<PetOwner> findByNormalizedPhone(@Param("phone") String phone);
}
