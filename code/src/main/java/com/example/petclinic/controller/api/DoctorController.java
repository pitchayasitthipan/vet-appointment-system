package com.example.petclinic.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.example.petclinic.controller.StaffAccess;
import com.example.petclinic.dto.request.DoctorRequestDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;
import com.example.petclinic.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

// ดูรายชื่อสัตวแพทย์ได้ทุกคน (ลูกค้าต้องใช้เลือกหมอตอนจองนัด)
// เพิ่ม/แก้ไข/ลบ ทำได้เฉพาะเจ้าหน้าที่ (ใส่รหัสที่หน้า Staff Only แล้ว) ไม่งั้นได้ 403
@RestController
@Tag(name = "Doctors", description = "จัดการข้อมูลสัตวแพทย์และตารางเวร")
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // 1. ดูรายชื่อสัตวแพทย์ (แบ่งหน้า) เช่น GET /api/v1/doctors?page=0&size=10&sort=firstName
    @Operation(summary = "ดึงรายชื่อสัตวแพทย์ (แบ่งหน้า + เรียงลำดับ)")
    @GetMapping
    public ResponseEntity<Page<DoctorResponseDTO>> getDoctors(
            @PageableDefault(size = 10, sort = "doctorId", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(doctorService.getDoctors(pageable));
    }

    // 2. ดูรายชื่อสัตวแพทย์ทั้งหมดไม่แบ่งหน้า (ใช้ในช่องเลือกหมอของหน้าจองนัด)
    @Operation(summary = "ดึงรายชื่อสัตวแพทย์ทั้งหมด (ไม่แบ่งหน้า สำหรับช่องเลือก)")
    @GetMapping("/all")
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // 3. ดูข้อมูลสัตวแพทย์ตาม ID (GET /api/v1/doctors/{id})
    @Operation(summary = "ดึงข้อมูลสัตวแพทย์ตาม Id")
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    // 4. เพิ่มข้อมูลสัตวแพทย์ (POST /api/v1/doctors)
    @Operation(summary = "เพิ่มข้อมูลสัตวแพทย์ [เฉพาะเจ้าหน้าที่]")
    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO requestDTO, HttpSession session) {
        StaffAccess.requireStaff(session);
        DoctorResponseDTO createdDoctor = doctorService.createDoctor(requestDTO);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
    }

    // 5. แก้ไขข้อมูลสัตวแพทย์ (PUT /api/v1/doctors/{id})
    @Operation(summary = "แก้ไขข้อมูลสัตวแพทย์ [เฉพาะเจ้าหน้าที่]")
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDTO requestDTO,
            HttpSession session) {
        StaffAccess.requireStaff(session);
        DoctorResponseDTO updatedDoctor = doctorService.updateDoctor(id, requestDTO);
        return ResponseEntity.ok(updatedDoctor);
    }

    // 6. ลบข้อมูลสัตวแพทย์ (DELETE /api/v1/doctors/{id})
    @Operation(summary = "ลบข้อมูลสัตวแพทย์ [เฉพาะเจ้าหน้าที่] (ยังมีนัดหมายอยู่จะลบไม่ได้ 409)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, HttpSession session) {
        StaffAccess.requireStaff(session);
        doctorService.deleteDoctor(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "ลบข้อมูลสัตวแพทย์รหัส " + id + " เรียบร้อยแล้ว");
        return ResponseEntity.ok(response);
    }
}
