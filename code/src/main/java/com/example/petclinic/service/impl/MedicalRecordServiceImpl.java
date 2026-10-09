package com.example.petclinic.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petclinic.domain.entity.MedicalRecord;
import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.exception.ResourceNotFoundException;
import com.example.petclinic.repository.MedicalRecordRepository;
import com.example.petclinic.service.MedicalRecordService;
import com.example.petclinic.service.AppointmentService;

    @Service
    @Transactional
    public class MedicalRecordServiceImpl implements MedicalRecordService {

    // Repository สำหรับจัดการข้อมูลประวัติการรักษา
    private final MedicalRecordRepository medicalRecordRepository;

    // Service ของ Appointment ใช้ตรวจสอบข้อมูลนัดหมายก่อนบันทึกประวัติ
    private final AppointmentService appointmentService;

    // Constructor Injection
    // รับ Repository และ Service ผ่าน Constructor
    // เพื่อให้ Spring จัดการ Dependency และสะดวกต่อการเขียน Unit Test
    public MedicalRecordServiceImpl(
            MedicalRecordRepository medicalRecordRepository,
            AppointmentService appointmentService) {

        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentService = appointmentService;
    }

    // ===== ส่วนค้นหาข้อมูลทั้งหมด =====
    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> getAllMedicalRecords() {
        return medicalRecordRepository.findAll().stream()
                .map(MedicalRecordResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ===== ส่วนค้นหาข้อมูลแบบแบ่งหน้าและเรียงลำดับ =====
    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponseDTO> getAllMedicalRecords(Pageable pageable) {

        // ให้ Repository ดึงข้อมูลตามหน้าที่ร้องขอ
        return medicalRecordRepository.findAll(pageable)
                .map(MedicalRecordResponseDTO::fromEntity);
    }

    // ===== ส่วนค้นหาข้อมูลตาม ID =====
    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponseDTO getMedicalRecordById(Long id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "ไม่พบประวัติการรักษาที่มีรหัส: " + id));

        return MedicalRecordResponseDTO.fromEntity(medicalRecord);
    }

    // ===== ส่วนค้นหาประวัติตามรหัสนัดหมาย =====
    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> getMedicalRecordsByAppointmentId(Long appointmentId) {
        return medicalRecordRepository
                .findByAppointmentIdOrderByCreatedAtDesc(appointmentId)
                .stream()
                .map(MedicalRecordResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ===== ส่วนเพิ่มประวัติการรักษา =====
    @Override
    public MedicalRecordResponseDTO createMedicalRecord(
            MedicalRecordRequestDTO requestDTO) {

        // ตรวจว่านัดหมายมีจริงและเสร็จสิ้นแล้วก่อนสร้างประวัติ
        appointmentService.requireCompletedForMedicalRecord(
                requestDTO.getAppointmentId());

        // ใช้ Builder Pattern สร้างประวัติการรักษาจากข้อมูลที่รับมา
        // กำหนดค่าทีละฟิลด์ แล้ว build() เพื่อสร้าง Object จริง
        MedicalRecord medicalRecord = MedicalRecord.builder()
            .appointmentId(requestDTO.getAppointmentId())
            .diagnosis(requestDTO.getDiagnosis())
            .treatment(requestDTO.getTreatment())
            .vaccineName(requestDTO.getVaccineName())
            .vaccineDate(requestDTO.getVaccineDate())
            .nextVaccineDate(requestDTO.getNextVaccineDate())
            .notes(requestDTO.getNotes())
            .build();

        MedicalRecord savedMedicalRecord =
                medicalRecordRepository.save(medicalRecord);

        return MedicalRecordResponseDTO.fromEntity(savedMedicalRecord);
    }

    // ===== ส่วนแก้ไขประวัติการรักษา =====
    @Override
    public MedicalRecordResponseDTO updateMedicalRecord(
            Long id,
            MedicalRecordRequestDTO requestDTO) {

        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "ไม่พบประวัติการรักษาที่มีรหัส: " + id));

        // ตรวจว่านัดหมายเสร็จสิ้นแล้วก่อนแก้ไขประวัติ
        appointmentService.requireCompletedForMedicalRecord(
                requestDTO.getAppointmentId());

        medicalRecord.setAppointmentId(requestDTO.getAppointmentId());
        medicalRecord.setDiagnosis(requestDTO.getDiagnosis());
        medicalRecord.setTreatment(requestDTO.getTreatment());
        medicalRecord.setVaccineName(requestDTO.getVaccineName());
        medicalRecord.setVaccineDate(requestDTO.getVaccineDate());
        medicalRecord.setNextVaccineDate(requestDTO.getNextVaccineDate());
        medicalRecord.setNotes(requestDTO.getNotes());

        MedicalRecord updatedMedicalRecord =
                medicalRecordRepository.save(medicalRecord);

        return MedicalRecordResponseDTO.fromEntity(updatedMedicalRecord);
    }

    // ===== ส่วนลบประวัติการรักษา =====
    @Override
    public void deleteMedicalRecord(Long id) {

        if (!medicalRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "ไม่พบประวัติการรักษาที่มีรหัส: " + id);
        }

        medicalRecordRepository.deleteById(id);
    }
}