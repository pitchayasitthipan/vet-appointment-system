package com.example.petclinic.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.service.MedicalRecordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // ===== Constructor Injection =====
    // รับ Service เข้ามาใช้งานใน Controller
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    // ===== 1. ดูประวัติการรักษาทั้งหมด =====
    // GET /api/medical-records
    @GetMapping
    public ResponseEntity<List<MedicalRecordResponseDTO>> getAllMedicalRecords() {
        List<MedicalRecordResponseDTO> medicalRecords =
                medicalRecordService.getAllMedicalRecords();

        return ResponseEntity.ok(medicalRecords);
    }

    // ===== 2. ดูประวัติการรักษาตาม ID =====
    // GET /api/medical-records/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDTO> getMedicalRecordById(
            @PathVariable Long id) {

        MedicalRecordResponseDTO medicalRecord =
                medicalRecordService.getMedicalRecordById(id);

        return ResponseEntity.ok(medicalRecord);
    }

    // ===== 3. ดูประวัติการรักษาตามรหัสนัดหมาย =====
    // GET /api/medical-records/appointment/{appointmentId}
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<MedicalRecordResponseDTO>> getMedicalRecordsByAppointmentId(
            @PathVariable Long appointmentId) {

        List<MedicalRecordResponseDTO> medicalRecords =
                medicalRecordService.getMedicalRecordsByAppointmentId(appointmentId);

        return ResponseEntity.ok(medicalRecords);
    }

    // ===== 4. เพิ่มประวัติการรักษา =====
    // POST /api/medical-records
    @PostMapping
    public ResponseEntity<MedicalRecordResponseDTO> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequestDTO requestDTO) {

        MedicalRecordResponseDTO createdMedicalRecord =
                medicalRecordService.createMedicalRecord(requestDTO);

        return new ResponseEntity<>(
                createdMedicalRecord,
                HttpStatus.CREATED);
    }

    // ===== 5. แก้ไขประวัติการรักษา =====
    // PUT /api/medical-records/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDTO> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequestDTO requestDTO) {

        MedicalRecordResponseDTO updatedMedicalRecord =
                medicalRecordService.updateMedicalRecord(id, requestDTO);

        return ResponseEntity.ok(updatedMedicalRecord);
    }

    // ===== 6. ลบประวัติการรักษา =====
    // DELETE /api/medical-records/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long id) {

        medicalRecordService.deleteMedicalRecord(id);

        return ResponseEntity.noContent().build();
    }
}