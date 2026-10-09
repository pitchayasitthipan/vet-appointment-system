package com.example.petclinic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;

public interface MedicalRecordService {

    // ดูประวัติการรักษาทั้งหมด
    List<MedicalRecordResponseDTO> getAllMedicalRecords();

    // ดูประวัติการรักษาแบบแบ่งหน้าและเรียงลำดับ
    Page<MedicalRecordResponseDTO> getAllMedicalRecords(Pageable pageable);

    // ดูประวัติการรักษาตาม ID
    MedicalRecordResponseDTO getMedicalRecordById(Long id);

    // ดูประวัติการรักษาของนัดหมาย
    List<MedicalRecordResponseDTO> getMedicalRecordsByAppointmentId(Long appointmentId);

    // เพิ่มประวัติการรักษา
    MedicalRecordResponseDTO createMedicalRecord(MedicalRecordRequestDTO requestDTO);

    // แก้ไขประวัติการรักษา
    MedicalRecordResponseDTO updateMedicalRecord(Long id, MedicalRecordRequestDTO requestDTO);

    // ลบประวัติการรักษา
    void deleteMedicalRecord(Long id);
}