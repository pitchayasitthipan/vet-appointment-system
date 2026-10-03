package com.example.petclinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petclinic.domain.entity.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // ตรวจสอบว่ามีอีเมลนี้ในระบบแล้วหรือไม่
    boolean existsByEmail(String email);

    // ตรวจสอบว่ามีอีเมลนี้ในระบบโดยไม่รวม doctor_id ของตัวเอง (ใช้ตอนแก้ไขข้อมูล)
    boolean existsByEmailAndDoctorIdNot(String email, Long doctorId);

    // ค้นหาข้อมูลสัตวแพทย์ด้วยอีเมล
    Optional<Doctor> findByEmail(String email);
}
