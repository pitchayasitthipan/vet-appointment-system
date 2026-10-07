package com.example.petclinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petclinic.domain.entity.MedicalRecord;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // ค้นหาประวัติการรักษาของนัดหมายและเรียงจากรายการล่าสุด
    List<MedicalRecord> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
}