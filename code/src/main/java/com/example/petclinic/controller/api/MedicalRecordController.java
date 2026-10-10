package com.example.petclinic.controller.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.petclinic.dto.request.MedicalRecordRequestDTO;
import com.example.petclinic.dto.response.MedicalRecordResponseDTO;
import com.example.petclinic.service.MedicalRecordService;

import jakarta.validation.Valid;
import com.example.petclinic.controller.StaffAccess;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/medical-records")
@Tag(name = "Medical Records", description = "จัดการประวัติการรักษาและการฉีดวัคซีนของสัตว์เลี้ยง")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // ===== Constructor Injection =====
    // รับ Service เข้ามาใช้งานใน Controller
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    // ===== 1. ดูประวัติการรักษาแบบแบ่งหน้าและเรียงลำดับ =====
    // GET /api/v1/medical-records?page=0&size=10&sort=medicalRecordId,desc
    @Operation(
        summary = "ดูประวัติการรักษาทั้งหมด",
        description = "รองรับ Pagination และ Sorting ผ่าน page, size และ sort"
    )
    @GetMapping
    public ResponseEntity<Page<MedicalRecordResponseDTO>> getAllMedicalRecords(
            Pageable pageable,
            HttpSession session) {

        StaffAccess.requireStaff(session);

        Page<MedicalRecordResponseDTO> medicalRecords =
                medicalRecordService.getAllMedicalRecords(pageable);

        return ResponseEntity.ok(medicalRecords);
    }

    // ===== 2. ดูประวัติการรักษาตาม ID =====
    // GET /api/v1/medical-records/{id}
    @Operation(summary = "ดูรายละเอียดประวัติการรักษา",
            description = "ค้นหาประวัติการรักษาจากรหัสประวัติ")
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDTO> getMedicalRecordById(
            @PathVariable Long id,
            HttpSession session) {

        StaffAccess.requireStaff(session);

        MedicalRecordResponseDTO medicalRecord =
                medicalRecordService.getMedicalRecordById(id);

        return ResponseEntity.ok(medicalRecord);
    }

    // ===== 3. ดูประวัติการรักษาตามรหัสนัดหมาย =====
    // GET /api/v1/medical-records/appointment/{appointmentId}
    @Operation(summary = "ดูประวัติการรักษาตามนัดหมาย",
           description = "ค้นหาประวัติการรักษาทั้งหมดที่เกี่ยวข้องกับรหัสนัดหมาย")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<MedicalRecordResponseDTO>> getMedicalRecordsByAppointmentId(
            @PathVariable Long appointmentId,
            HttpSession session) {

        StaffAccess.requireStaff(session);

        List<MedicalRecordResponseDTO> medicalRecords =
                medicalRecordService.getMedicalRecordsByAppointmentId(appointmentId);

        return ResponseEntity.ok(medicalRecords);
    }   

    // ===== 4. เพิ่มประวัติการรักษา =====
    // POST /api/v1/medical-records
    @Operation(summary = "เพิ่มประวัติการรักษา",
           description = "บันทึกข้อมูลการวินิจฉัย การรักษา และวัคซีน")
    @PostMapping
    public ResponseEntity<MedicalRecordResponseDTO> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequestDTO requestDTO,
            HttpSession session) {

        StaffAccess.requireStaff(session);

        MedicalRecordResponseDTO createdMedicalRecord =
                medicalRecordService.createMedicalRecord(requestDTO);

        return new ResponseEntity<>(createdMedicalRecord, HttpStatus.CREATED);
    }

    // ===== 5. แก้ไขประวัติการรักษา =====
    // PUT /api/v1/medical-records/{id}
    @Operation(summary = "แก้ไขประวัติการรักษา",
           description = "แก้ไขข้อมูลประวัติการรักษาตามรหัสประวัติ")
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDTO> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordRequestDTO requestDTO,
            HttpSession session) {

        StaffAccess.requireStaff(session);

        MedicalRecordResponseDTO updatedMedicalRecord =
                medicalRecordService.updateMedicalRecord(id, requestDTO);

        return ResponseEntity.ok(updatedMedicalRecord);
    }

    // ===== 6. ลบประวัติการรักษา =====
    // DELETE /api/v1/medical-records/{id}
    @Operation(summary = "ลบประวัติการรักษา",
           description = "ลบประวัติการรักษาตามรหัสประวัติ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(
            @PathVariable Long id,
            HttpSession session) {

        StaffAccess.requireStaff(session);

        medicalRecordService.deleteMedicalRecord(id);

        return ResponseEntity.noContent().build();
    }
}