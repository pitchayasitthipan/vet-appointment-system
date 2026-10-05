package com.example.petclinic.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.petclinic.dto.request.DoctorRequestDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;
import com.example.petclinic.service.DoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // 1. ดูรายชื่อสัตวแพทย์ทั้งหมด (GET /api/doctors)
    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        List<DoctorResponseDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    // 2. ดูข้อมูลสัตวแพทย์ตาม ID (GET /api/doctors/{id})
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    // 3. เพิ่มข้อมูลสัตวแพทย์ (POST /api/doctors)
    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorRequestDTO requestDTO) {
        DoctorResponseDTO createdDoctor = doctorService.createDoctor(requestDTO);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
    }

    // 4. แก้ไขข้อมูลสัตวแพทย์ (PUT /api/doctors/{id})
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDTO requestDTO) {
        DoctorResponseDTO updatedDoctor = doctorService.updateDoctor(id, requestDTO);
        return ResponseEntity.ok(updatedDoctor);
    }

    // 5. ลบข้อมูลสัตวแพทย์ (DELETE /api/doctors/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "ลบข้อมูลสัตวแพทย์รหัส " + id + " เรียบร้อยแล้ว");
        return ResponseEntity.ok(response);
    }
}
