package com.example.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetOwnerDetailRepository extends JpaRepository {
    // ค้นหารายละเอียดเจ้าของจาก ownerId ของ PetOwner
    Optional findByPetOwner_OwnerId(Long ownerId);
}