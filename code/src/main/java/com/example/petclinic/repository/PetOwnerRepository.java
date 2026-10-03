package com.example.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petclinic.domain.entity.PetOwner;

@Repository
public interface PetOwnerRepository extends JpaRepository<PetOwner, Long> {

    // ตรวจสอบว่ามีอีเมลนี้ในระบบแล้วหรือยัง
    boolean existsByEmail(String email);

    // ค้นหาข้อมูลเจ้าของด้วยอีเมล
    Optional<PetOwner> findByEmail(String email);

    // ค้นหาข้อมูลเจ้าของด้วยเบอร์
    Optional<PetOwner> findByPhone(String phone);
}
