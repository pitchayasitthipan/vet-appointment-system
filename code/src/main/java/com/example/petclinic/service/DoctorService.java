package com.example.petclinic.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.petclinic.dto.request.DoctorRequestDTO;
import com.example.petclinic.dto.response.DoctorResponseDTO;

public interface DoctorService {

    // ดูรายชื่อสัตวแพทย์แบบแบ่งหน้า (ใช้ใน REST API)
    Page<DoctorResponseDTO> getDoctors(Pageable pageable);

    // ดูรายชื่อสัตวแพทย์ทั้งหมด
    List<DoctorResponseDTO> getAllDoctors();

    // ดูข้อมูลสัตวแพทย์ตาม ID
    DoctorResponseDTO getDoctorById(Long id);

    // เพิ่มข้อมูลสัตวแพทย์
    DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO);

    // แก้ไขข้อมูลสัตวแพทย์
    DoctorResponseDTO updateDoctor(Long id, DoctorRequestDTO requestDTO);

    // ลบข้อมูลสัตวแพทย์
    void deleteDoctor(Long id);
}
