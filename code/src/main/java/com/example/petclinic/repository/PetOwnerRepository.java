package com.example.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetOwnerRepository extends JpaRepository {

    // ตรวจสอบว่ามีอีเมลนี้ในระบบแล้วหรือยัง (ใช้ตอน Validation
    // ก่อนสร้างเจ้าของใหม่)
    boolean existsByEmail(String email);

    // ค้นหาข้อมูลเจ้าของด้วยอีเมล
    Optional findByEmail(String email);

    // ค้นหาข้อมูลเจ้าของด้วยเบอร์
    Optional findByPhone(String phone);
}