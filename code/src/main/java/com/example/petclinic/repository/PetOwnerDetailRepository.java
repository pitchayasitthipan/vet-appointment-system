package com.example.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petclinic.domain.entity.PetOwnerDetail;

@Repository
public interface PetOwnerDetailRepository extends JpaRepository<PetOwnerDetail, Long> {
    // ค้นหารายละเอียดเจ้าของจาก ownerId ของ PetOwner
    Optional<PetOwnerDetail> findByPetOwner_OwnerId(Long ownerId);
}